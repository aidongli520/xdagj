/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.discovery.peers;

import io.xdag.utils.discoveryutils.bytes.BytesValue;
import io.xdag.utils.discoveryutils.bytes.RLPOutput;

import java.security.SecureRandom;

public interface Peer extends PeerId{
    Endpoint getEndpoint();

    /**
     * Generates a random peer ID in a secure manner.
     *
     * @return The generated peer ID.
     */
    static BytesValue randomId() {
        final byte[] id = new byte[37];
//        final byte[] id = new byte[34];
        new SecureRandom().nextBytes(id);
        return BytesValue.wrap(id);
    }

    /**
     * Encodes this peer to its RLP representation.
     *
     * @param out The RLP output stream to which to write.
     */
    default void writeTo(final RLPOutput out) {
        out.startList();
        getEndpoint().encodeInline(out);
        out.writeBytesValue(getId());
        out.endList();
    }
}
