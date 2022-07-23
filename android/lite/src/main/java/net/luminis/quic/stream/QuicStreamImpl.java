/*
 * Copyright © 2019, 2020, 2021, 2022 Peter Doornbosch
 *
 * This file is part of Kwik, an implementation of the QUIC protocol in Java.
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.stream;

import net.luminis.quic.EncryptionLevel;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicConnectionImpl;
import net.luminis.quic.QuicStream;
import net.luminis.quic.StreamData;
import net.luminis.quic.Version;
import net.luminis.quic.frame.*;
import net.luminis.quic.log.Logger;
import net.luminis.quic.log.NullLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static net.luminis.quic.EncryptionLevel.App;

import threads.lite.LogUtils;


public class QuicStreamImpl implements QuicStream {
    private static final String TAG = QuicStreamImpl.class.getSimpleName();

    protected static final float receiverMaxDataIncrementFactor = 0.10f;

    protected final Version quicVersion;
    protected final int streamId;
    protected final QuicConnectionImpl connection;
    protected final FlowControl flowController;
    protected final Logger log;
    private final StreamOutputStream outputStream;

    private long receiverFlowControlLimit;
    private long lastCommunicatedMaxData;
    private final long receiverMaxDataIncrement;
    private volatile long lastOffset = -1;
    private int sendBufferSize = 50 * 1024;

    private SortedSet<StreamElement> frames = new TreeSet<>();
    private long processedToOffset = 0;

    
    public QuicStreamImpl(int streamId, QuicConnectionImpl connection, FlowControl flowController) {
        this(Version.getDefault(), streamId, connection, flowController, new NullLogger());
    }

    public QuicStreamImpl(int streamId, QuicConnectionImpl connection, FlowControl flowController, Logger log) {
        this(Version.getDefault(), streamId, connection, flowController, log);
    }

    public QuicStreamImpl(Version quicVersion, int streamId, QuicConnectionImpl connection, FlowControl flowController, Logger log) {
        this(quicVersion, streamId, connection, flowController, log, null);
    }

    QuicStreamImpl(Version quicVersion, int streamId, QuicConnectionImpl connection, FlowControl flowController, Logger log, Integer sendBufferSize) {
        this.quicVersion = quicVersion;
        this.streamId = streamId;
        this.connection = connection;
        this.flowController = flowController;
        if (sendBufferSize != null && sendBufferSize > 0) {
            this.sendBufferSize = sendBufferSize;
        }
        this.log = log;

        outputStream = createStreamOutputStream();

        flowController.streamOpened(this);
        receiverFlowControlLimit = connection.getInitialMaxStreamData();
        lastCommunicatedMaxData = receiverFlowControlLimit;
        receiverMaxDataIncrement = (long) (receiverFlowControlLimit * receiverMaxDataIncrementFactor);
    }



    protected synchronized boolean addi(StreamElement frame) {
        if (frame.getUpToOffset() > processedToOffset) {
            frames.add(frame);
            return true;
        } else {
            return false;
        }
    }

    void add(StreamFrame frame) {
        boolean added = addi(frame);
        if (added) {
            if(consumer != null) {
                broadcast();
            }
        }
    }

    private void broadcast() {
        Iterator<StreamElement> iterator = frames.iterator();

        int bytesRead = 0;
        while (iterator.hasNext()) {
            StreamFrame nextFrame = (StreamFrame) iterator.next();
            if (nextFrame.getOffset() <= processedToOffset) {
                if (nextFrame.getUpToOffset() > processedToOffset) {

                    StreamData streamData = new StreamData();
                    streamData.data = nextFrame.getStreamData();
                    streamData.fin = nextFrame.isFinal();

                    bytesRead += streamData.data.length;

                    try {
                        consumer.accept(streamData);
                    } catch (Throwable throwable) {
                        LogUtils.error(TAG, throwable);
                    }
                    processedToOffset = nextFrame.getUpToOffset();

                    iterator.remove();
                }
            } else {
                break;
            }
        }
        if (bytesRead > 0) {
            updateAllowedFlowControl(bytesRead);
        }

    }

    private void updateAllowedFlowControl(int bytesRead) {
        // Slide flow control window forward (with as much bytes as are read)
        receiverFlowControlLimit += bytesRead;
        connection.updateConnectionFlowControl(bytesRead);
        // Avoid sending flow control updates with every single read; check diff with last send max data
        if (receiverFlowControlLimit - lastCommunicatedMaxData > receiverMaxDataIncrement) {
            connection.send(new MaxStreamDataFrame(streamId, receiverFlowControlLimit), this::retransmitMaxData, true);
            lastCommunicatedMaxData = receiverFlowControlLimit;
        }
    }

    private void retransmitMaxData(QuicFrame lostFrame) {
        connection.send(new MaxStreamDataFrame(streamId, receiverFlowControlLimit), this::retransmitMaxData);
        LogUtils.error(TAG, "Retransmitted max stream data, because lost frame " + lostFrame);
    }

    private Consumer<StreamData> consumer;

    public void setConsumer(Consumer<StreamData> consumer) {
        this.consumer = consumer;
    }

    @Override
    public QuicConnection getConnection() {
        return connection;
    }


    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }


    protected boolean isStreamEnd(long offset) {
        return lastOffset >= 0 && offset >= lastOffset;
    }

    @Override
    public int getStreamId() {
        return streamId;
    }

    @Override
    public boolean isUnidirectional() {
        // https://tools.ietf.org/html/draft-ietf-quic-transport-23#section-2.1
        // "The second least significant bit (0x2) of the stream ID distinguishes
        //   between bidirectional streams (with the bit set to 0) and
        //   unidirectional streams (with the bit set to 1)."
        return (streamId & 0x0002) == 0x0002;
    }

    @Override
    public boolean isClientInitiatedBidirectional() {
        // "Client-initiated streams have even-numbered stream IDs (with the bit set to 0)"
        return (streamId & 0x0003) == 0x0000;
    }

    @Override
    public boolean isServerInitiatedBidirectional() {
        // "server-initiated streams have odd-numbered stream IDs"
        return (streamId & 0x0003) == 0x0001;
    }

    @Override
    public void closeInput(long applicationProtocolErrorCode) {
        stopInput(applicationProtocolErrorCode);
    }

    @Override
    public void resetStream(long errorCode) {
        outputStream.reset(errorCode);
    }

    @Override
    public String toString() {
        return "Stream " + streamId;
    }

    protected StreamOutputStream createStreamOutputStream() {
        return new StreamOutputStream();
    }

    /**
     * Terminates the receiving input stream (abruptly). Is called when peer sends a RESET_STREAM frame
     *
     * This method is intentionally package-protected, as it should only be called by the StreamManager class.
     *
     * @param errorCode
     * @param finalSize
     */
    void terminateStream(long errorCode, long finalSize) {

    }



    private void stopInput(long errorCode) {
        if (! allDataReceived()) {
            connection.send(new StopSendingFrame(quicVersion, streamId, errorCode), this::retransmitStopInput, true);
        }
    }

    private void retransmitStopInput(QuicFrame lostFrame) {
        assert(lostFrame instanceof StopSendingFrame);

        if (! allDataReceived()) {
            connection.send(lostFrame, this::retransmitStopInput);
        }
    }

    /**
     * Determines whether all data (up to stream end offset) is received (but might have not been read)
     *
     * @return  true if all data has been received, false otherwise
     */
    protected synchronized boolean allDataReceived() {
        if (isStreamEnd(processedToOffset)) {
            return true;
        }
        else {
            long completeUpTo = processedToOffset;
            Iterator<StreamElement> iterator = frames.iterator();

            while (iterator.hasNext()) {
                StreamElement nextFrame = iterator.next();
                if (nextFrame.getOffset() <= completeUpTo) {
                    if (nextFrame.getUpToOffset() > completeUpTo) {
                        completeUpTo = nextFrame.getUpToOffset();
                    }
                } else {
                    // There is a hole between
                    break;
                }
            }
            return isStreamEnd(completeUpTo);
        }
    }

    public void abort() {
    }

    protected class StreamOutputStream extends OutputStream implements FlowControlUpdateListener {

        // Minimum stream frame size: frame type (1), stream id (1..8), offset (1..8), length (1..2), data (1...)
        // Note that in practice stream id and offset will seldom / never occupy 8 bytes, so the minimum leaves more room for data.
        private static final int MIN_FRAME_SIZE = 1 + 8 + 8 + 2 + 1;

        private final ByteBuffer END_OF_STREAM_MARKER = ByteBuffer.allocate(0);
        private final Object lock = new Object();

        // Send queue contains stream bytes to send in order. The position of the first byte buffer in the queue determines the next byte(s) to send.
        private final Queue<ByteBuffer> sendQueue = new ConcurrentLinkedDeque<>();
        //private final int maxBufferSize;
        private final AtomicInteger bufferedBytes;
        //private final ReentrantLock bufferLock;
        //private final Condition notFull;
        // Current offset is the offset of the next byte in the stream that will be sent.
        // Thread safety: only used by sender thread, so no synchronization needed.
        private long currentOffset;
        // Closed indicates whether the OutputStream is closed, meaning that no more bytes can be written by caller.
        // Thread safety: only use by caller
        private boolean closed;
        // Send request queued indicates whether a request to send a stream frame is queued with the sender. Is used to avoid multiple requests being queued.
        // Thread safety: read/set by caller and by sender thread, so must be synchronized; guarded by lock
        private volatile boolean sendRequestQueued;
        // Reset indicates whether the OutputStream has been reset.
        private volatile boolean reset;
        private volatile long resetErrorCode;
        // Stream offset at which the stream was last blocked, for detecting the first time stream is blocked at a certain offset.
        private long blockedOffset;

        StreamOutputStream() {
            //maxBufferSize = sendBufferSize;
            bufferedBytes = new AtomicInteger();
            //bufferLock = new ReentrantLock();
            //notFull = bufferLock.newCondition();

            flowController.register(QuicStreamImpl.this, this);
        }

        @Override
        public void write(byte[] data) throws IOException {
            write(data, 0, data.length);
        }

        @Override
        public void write(byte[] data, int off, int len) throws IOException {
            checkState();
            /*
            if (len > maxBufferSize) {
                // Buffering all would break the contract (because this method copies _all_ data) but splitting and
                // writing smaller chunks (and waiting for each individual chunk to be buffered successfully) does not.
                int halfBuffersize = maxBufferSize / 2;
                int times = len / halfBuffersize;
                for (int i = 0; i < times; i++) {
                    // Each individual write will probably block, but by splitting the writes in half buffer sizes
                    // avoids that the buffer needs to be emptied completely before a new block can be added (which
                    // could have severed negative impact on performance as the sender might have to wait for the caller
                    // to fill the buffer again).
                    write(data, off + i * halfBuffersize, halfBuffersize);
                }
                int rest = len % halfBuffersize;
                if (rest > 0) {
                    write(data, off + times * halfBuffersize, rest);
                }
                return;
            }*/
            /*
            int availableBufferSpace = maxBufferSize - bufferedBytes.get();
            if (len > availableBufferSpace) {
                // Wait for enough buffer space to become available
                bufferLock.lock();
                try {
                    while (maxBufferSize - bufferedBytes.get() < len) {
                        checkState();

                        try {
                            notFull.await();
                        } catch (InterruptedException e) {
                            throw new InterruptedIOException();
                        }
                    }
                }
                finally {
                    bufferLock.unlock();
                }
            }*/

            sendQueue.add(ByteBuffer.wrap(Arrays.copyOfRange(data, off, off + len)));
            bufferedBytes.getAndAdd(len);
            synchronized (lock) {
                if (! sendRequestQueued) {
                    sendRequestQueued = true;
                    connection.send(this::sendFrame, MIN_FRAME_SIZE, getEncryptionLevel(), this::retransmitStreamFrame, true);
                }
            }
        }

        @Override
        public void write(int dataByte) throws IOException {
            // Terrible for performance of course, but that is calling this method anyway.
            byte[] data = new byte[] { (byte) dataByte };
            write(data, 0, 1);
        }

        @Override
        public void flush() throws IOException {
            checkState();
            // No-op, this implementation sends data as soon as possible.
        }

        @Override
        public void close() throws IOException {
            if (!closed && !reset) {
                sendQueue.add(END_OF_STREAM_MARKER);
                closed = true;
                synchronized (lock) {
                    if (! sendRequestQueued) {
                        sendRequestQueued = true;
                        connection.send(this::sendFrame, MIN_FRAME_SIZE, getEncryptionLevel(), this::retransmitStreamFrame, true);
                    }
                }
            }
        }

        private void checkState() throws IOException {
            if (closed || reset) {
                throw new IOException("output stream " + (closed? "already closed": "is reset"));
            }
        }

        QuicFrame sendFrame(int maxFrameSize) {
            if (reset) {
                return null;
            }
            synchronized (lock) {
                sendRequestQueued = false;
            }

            if (!sendQueue.isEmpty()) {
                long flowControlLimit = flowController.getFlowControlLimit(QuicStreamImpl.this);
                assert (flowControlLimit >= currentOffset);

                int maxBytesToSend = bufferedBytes.get();
                if (flowControlLimit > currentOffset || maxBytesToSend == 0) {
                    int nrOfBytes = 0;
                    StreamFrame dummy = new StreamFrame(quicVersion, streamId, currentOffset, new byte[0], false);
                    maxBytesToSend = Integer.min(maxBytesToSend, maxFrameSize - dummy.getFrameLength() - 1);  // Take one byte extra for length field var int
                    int maxAllowedByFlowControl = (int) (flowController.increaseFlowControlLimit(QuicStreamImpl.this, currentOffset + maxBytesToSend) - currentOffset);
                    maxBytesToSend = Integer.min(maxAllowedByFlowControl, maxBytesToSend);

                    byte[] dataToSend = new byte[maxBytesToSend];
                    boolean finalFrame = false;
                    while (nrOfBytes < maxBytesToSend && !sendQueue.isEmpty()) {
                        ByteBuffer buffer = sendQueue.peek();
                        int position = nrOfBytes;
                        if (buffer.remaining() <= maxBytesToSend - nrOfBytes) {
                            // All bytes remaining in buffer will fit in stream frame
                            nrOfBytes += buffer.remaining();
                            buffer.get(dataToSend, position, buffer.remaining());
                            sendQueue.poll();
                        }
                        else {
                            // Just part of the buffer will fit in (and will fill up) the stream frame
                            buffer.get(dataToSend, position, maxBytesToSend - nrOfBytes);
                            nrOfBytes = maxBytesToSend;  // Short form of: nrOfBytes += (maxBytesToSend - nrOfBytes)
                        }
                    }
                    if (!sendQueue.isEmpty() && sendQueue.peek() == END_OF_STREAM_MARKER) {
                        finalFrame = true;
                        sendQueue.poll();
                    }
                    if (nrOfBytes == 0 && !finalFrame) {
                        // Nothing to send really
                        return null;
                    }

                    bufferedBytes.getAndAdd(-1 * nrOfBytes);
                    /*
                    bufferLock.lock();
                    try {
                        notFull.signal();
                    } finally {
                        bufferLock.unlock();
                    }*/

                    if (nrOfBytes < maxBytesToSend) {
                        // This can happen when not enough data is buffer to fill a stream frame, or length field is 1 byte (instead of 2 that was counted for)
                        dataToSend = Arrays.copyOfRange(dataToSend, 0, nrOfBytes);
                    }
                    StreamFrame streamFrame = new StreamFrame(quicVersion, streamId, currentOffset, dataToSend, finalFrame);
                    currentOffset += nrOfBytes;

                    if (!sendQueue.isEmpty()) {
                        synchronized (lock) {
                            sendRequestQueued = true;
                        }
                        // There is more to send, so queue a new send request.
                        connection.send(this::sendFrame, MIN_FRAME_SIZE, getEncryptionLevel(), this::retransmitStreamFrame, true);
                    }

                    if (streamFrame.isFinal()) {
                        // Done! Retransmissions may follow, but don't need flow control.
                        flowController.unregister(QuicStreamImpl.this);
                        flowController.streamClosed(QuicStreamImpl.this);
                    }
                    return streamFrame;
                }
                else {
                    // So flowControlLimit <= currentOffset
                    // Check if this condition hasn't been handled before
                    if (currentOffset != blockedOffset) {
                        // Not handled before, remember this offset, so this isn't executed twice for the same offset
                        blockedOffset = currentOffset;
                        // And let peer know
                        // https://www.rfc-editor.org/rfc/rfc9000.html#name-data-flow-control
                        // "A sender SHOULD send a STREAM_DATA_BLOCKED or DATA_BLOCKED frame to indicate to the receiver
                        //  that it has data to write but is blocked by flow control limits."
                        connection.send(this::sendBlockReason, StreamDataBlockedFrame.getMaxSize(streamId), App, this::retransmitSendBlockReason, true);
                    }
                }
            }
            return null;
        }

        @Override
        public void streamNotBlocked(int streamId) {
            // Stream might have been blocked (or it might have filled the flow control window exactly), queue send request
            // and let sendFrame method determine whether there is more to send or not.
            connection.send(this::sendFrame, MIN_FRAME_SIZE, getEncryptionLevel(), this::retransmitStreamFrame, false);  // No need to flush, as this is called while processing received message
        }

        /**
         * Sends StreamDataBlockedFrame or DataBlockedFrame to the peer, provided the blocked condition is still true.
         * @param maxFrameSize
         * @return
         */
        private QuicFrame sendBlockReason(int maxFrameSize) {
            // Retrieve actual block reason; could be "none" when an update has been received in the meantime.
            BlockReason blockReason = flowController.getFlowControlBlockReason(QuicStreamImpl.this);
            QuicFrame frame = null;
            switch (blockReason) {
                case STREAM_DATA_BLOCKED:
                    frame = new StreamDataBlockedFrame(quicVersion, streamId, currentOffset);
                    break;
                case DATA_BLOCKED:
                    frame = new DataBlockedFrame(flowController.getConnectionDataLimit());
                    break;
            }
            return frame;
        }

        private void retransmitSendBlockReason(QuicFrame quicFrame) {
            connection.send(this::sendBlockReason, StreamDataBlockedFrame.getMaxSize(streamId), App, this::retransmitSendBlockReason, true);
        }

        private void retransmitStreamFrame(QuicFrame frame) {
            assert(frame instanceof StreamFrame);
            if (! reset) {
                connection.send(frame, this::retransmitStreamFrame);
                if(LogUtils.isDebug()) {
                    LogUtils.error(TAG, "Retransmitted lost stream frame " + frame);
                }
            }
        }

        protected EncryptionLevel getEncryptionLevel() {
            return App;
        }

        private void restart() {
            currentOffset = 0;
            sendQueue.clear();
            sendRequestQueued = false;
        }

        /**
         * https://www.rfc-editor.org/rfc/rfc9000.html#name-operations-on-streams
         * "reset the stream (abrupt termination), resulting in a RESET_STREAM frame (Section 19.4) if the stream was
         *  not already in a terminal state."
         * @param errorCode
         */
        protected void reset(long errorCode) {
            if (!closed && !reset) {
                reset = true;
                resetErrorCode = errorCode;
                // Use sender callback to ensure current offset used in reset frame is accessed by sender thread.
                connection.send(this::createResetFrame, ResetStreamFrame.getMaximumFrameSize(streamId, errorCode), App, this::retransmitResetFrame, true);
                // Ensure write is not blocked because of full write buffer
                /*
                bufferLock.lock();
                try {
                    notFull.signal();
                }
                finally {
                    bufferLock.unlock();
                }*/
            }
        }

        private QuicFrame createResetFrame(int maxFrameSize) {
            assert(reset == true);
            return new ResetStreamFrame(streamId, resetErrorCode, currentOffset);
        }

        private void retransmitResetFrame(QuicFrame frame) {
            assert(frame instanceof ResetStreamFrame);
            connection.send(frame, this::retransmitResetFrame);
        }
    }

    /**
     * Resets the output stream so data can again be send from the start of the stream (offset 0). Note that in such
     * cases the caller must (again) provide the data to be sent.
     */
    protected void resetOutputStream() {
        // TODO: this is currently not thread safe, see comment in EarlyDataStream how to fix.
        outputStream.restart();
    }

    public void close() throws IOException {
        // Note that QUIC specification does not define application protocol error codes.
        // By absence of an application specified error code, the arbitrary code 0 is used.
        stopInput(0);
    }
}
