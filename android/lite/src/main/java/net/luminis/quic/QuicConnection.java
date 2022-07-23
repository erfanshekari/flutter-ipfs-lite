/*
 * Copyright © 2020, 2021, 2022 Peter Doornbosch
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
package net.luminis.quic;

import net.luminis.quic.QuicStream;

import java.net.InetSocketAddress;
import java.util.function.Consumer;


public interface QuicConnection {

    Version getQuicVersion();

    void setMaxAllowedBidirectionalStreams(int max);

    void setMaxAllowedUnidirectionalStreams(int max);

    void setDefaultStreamReceiveBufferSize(long size);

    QuicStream createStream(boolean bidirectional);

    void setPeerInitiatedStreamCallback(Consumer<QuicStream> streamConsumer);

    void close();

    void close(QuicConstants.TransportErrorCode applicationError, String errorReason);

    Statistics getStats();

    boolean isConnected();

    InetSocketAddress getRemoteAddress();
}
