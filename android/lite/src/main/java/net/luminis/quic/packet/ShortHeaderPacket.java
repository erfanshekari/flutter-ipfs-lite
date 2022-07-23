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
package net.luminis.quic.packet;

import net.luminis.quic.*;
import net.luminis.quic.crypto.Keys;
import net.luminis.quic.frame.QuicFrame;
import net.luminis.quic.log.Logger;
import net.luminis.tls.util.ByteUtils;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ShortHeaderPacket extends QuicPacket {

    protected short keyPhaseBit;

    /**
     * Constructs an empty short header packet for use with the parse() method.
     * @param quicVersion
     */
    public ShortHeaderPacket(Version quicVersion) {
        this.quicVersion = quicVersion;
    }

    /**
     * Constructs a short header packet for sending (client role).
     * @param quicVersion
     * @param destinationConnectionId
     * @param frame
     */
    public ShortHeaderPacket(Version quicVersion, byte[] destinationConnectionId, QuicFrame frame) {
        this.quicVersion = quicVersion;
        this.destinationConnectionId = destinationConnectionId;
        frames = new ArrayList<>();
        if (frame != null) {
            frames.add(frame);
        }
    }

    @Override
    public void parse(ByteBuffer buffer, Keys keys, long largestPacketNumber, Logger log, int sourceConnectionIdLength) throws DecryptionException, InvalidPacketException {
        log.debug("Parsing " + this.getClass().getSimpleName());
        if (buffer.remaining() < 1 + sourceConnectionIdLength) {
            throw new InvalidPacketException();
        }
        if (buffer.position() != 0) {
            // parsePacketNumberAndPayload method requires packet to start at 0.
            throw new IllegalStateException();
        }
        byte flags = buffer.get();
        checkPacketType(flags);

        // https://tools.ietf.org/html/draft-ietf-quic-transport-24#section-5.1
        // "Packets with short headers (Section 17.3) only include the
        //   Destination Connection ID and omit the explicit length.  The length
        //   of the Destination Connection ID field is expected to be known to
        //   endpoints."
        byte[] packetConnectionId = new byte[sourceConnectionIdLength];
        destinationConnectionId = packetConnectionId;
        buffer.get(packetConnectionId);
        log.debug("Destination connection id", packetConnectionId);

        try {
            parsePacketNumberAndPayload(buffer, flags, buffer.limit() - buffer.position(), keys, largestPacketNumber, log);
            keys.confirmKeyUpdateIfInProgress();
        }
        catch (DecryptionException cantDecrypt) {
            keys.cancelKeyUpdateIfInProgress();
            throw cantDecrypt;
        }
        finally {
            packetSize = buffer.position() - 0;
        }
    }

    @Override
    protected void setUnprotectedHeader(byte decryptedFlags) {
        keyPhaseBit = (short) ((decryptedFlags & 0x04) >> 2);
    }

    @Override
    public int estimateLength(int additionalPayload) {
        return 1
                + destinationConnectionId.length
                + 1  // packet number length: will usually be just 1, actual value cannot be computed until packet number is known
                + getFrames().stream().mapToInt(f -> f.getFrameLength()).sum() + additionalPayload
                // https://tools.ietf.org/html/draft-ietf-quic-tls-27#section-5.4.2
                // "The ciphersuites defined in [TLS13] - (...) - have 16-byte expansions..."
                + 16;
    }

    @Override
    public EncryptionLevel getEncryptionLevel() {
        return EncryptionLevel.App;
    }

    @Override
    public PnSpace getPnSpace() {
        return PnSpace.App;
    }

    @Override
    public byte[] generatePacketBytes(Long packetNumber, Keys keys) {
        this.packetNumber = packetNumber;

        ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        byte flags;
        // https://tools.ietf.org/html/draft-ietf-quic-transport-17#section-17.3
        // "|0|1|S|R|R|K|P P|"
        // "Spin Bit (S):  The sixth bit (0x20) of byte 0 is the Latency Spin
        //      Bit, set as described in [SPIN]."
        // "Reserved Bits (R):  The next two bits (those with a mask of 0x18) of
        //      byte 0 are reserved. (...) The value included prior to protection MUST be set to 0. "
        flags = 0x40;  // 0100 0000
        keyPhaseBit = keys.getKeyPhase();
        flags = (byte) (flags | (keyPhaseBit << 2));
        flags = encodePacketNumberLength(flags, packetNumber);
        buffer.put(flags);
        buffer.put(destinationConnectionId);

        byte[] encodedPacketNumber = encodePacketNumber(packetNumber);
        buffer.put(encodedPacketNumber);

        ByteBuffer frameBytes = generatePayloadBytes(encodedPacketNumber.length);
        protectPacketNumberAndPayload(buffer, encodedPacketNumber.length, frameBytes, 0, keys);

        buffer.limit(buffer.position());
        packetSize = buffer.limit();
        byte[] packetBytes = new byte[packetSize];
        buffer.rewind();
        buffer.get(packetBytes);

        packetSize = packetBytes.length;

        return packetBytes;
    }

    @Override
    public PacketProcessor.ProcessResult accept(PacketProcessor processor, Instant time) {
        return processor.process(this, time);
    }

    protected void checkPacketType(byte flags) {
        if ((flags & 0xc0) != 0x40) {
            // Programming error: this method shouldn't have been called if packet is not a Short Frame
            throw new RuntimeException();
        }
    }

    public byte[] getDestinationConnectionId() {
        return destinationConnectionId;
    }

    @Override
    public String toString() {
        return "Packet "
                + (isProbe? "P": "")
                + getEncryptionLevel().name().charAt(0) + "|"
                + (packetNumber >= 0? packetNumber: ".") + "|"
                + "S" + keyPhaseBit + "|"
                + ByteUtils.bytesToHex(destinationConnectionId) + "|"
                + packetSize + "|"
                + frames.size() + "  "
                + frames.stream().map(f -> f.toString()).collect(Collectors.joining(" "));
    }

}
