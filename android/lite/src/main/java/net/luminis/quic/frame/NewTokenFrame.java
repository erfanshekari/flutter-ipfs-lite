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
package net.luminis.quic.frame;

import net.luminis.quic.InvalidIntegerEncodingException;
import net.luminis.quic.VariableLengthInteger;
import net.luminis.quic.log.Logger;
import net.luminis.quic.packet.QuicPacket;
import net.luminis.tls.util.ByteUtils;

import java.nio.ByteBuffer;
import java.time.Instant;

/**
 * Represents a new token frame.
 * https://www.rfc-editor.org/rfc/rfc9000.html#name-new_token-frames
 */
public class NewTokenFrame extends QuicFrame {

    private byte[] newToken;

    public NewTokenFrame() {
    }

    public NewTokenFrame(byte[] token) {
        newToken = token;
    }

    public NewTokenFrame parse(ByteBuffer buffer, Logger log) throws InvalidIntegerEncodingException {
        buffer.get();

        int tokenLength = VariableLengthInteger.parse(buffer);
        newToken = new byte[tokenLength];
        buffer.get(newToken);

        log.debug("Got New Token: ", newToken);

        return this;
    }

    @Override
    public int getFrameLength() {
        return 1 + VariableLengthInteger.bytesNeeded(newToken.length) + newToken.length;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put((byte) 0x07);
        VariableLengthInteger.encode(newToken.length, buffer);
        buffer.put(newToken);
    }

    @Override
    public String toString() {
        return "NewTokenFrame[" + ByteUtils.bytesToHex(newToken) + "]";
    }

    @Override
    public void accept(FrameProcessor3 frameProcessor, QuicPacket packet, Instant timeReceived) {
        frameProcessor.process(this, packet, timeReceived);
    }

    public byte[] getToken() {
        return newToken;
    }
}
