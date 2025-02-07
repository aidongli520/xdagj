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
package io.xdag.utils.discoveryutils.bytes;

import io.vertx.core.buffer.Buffer;

import java.security.MessageDigest;
import java.util.Arrays;

import static com.google.common.base.Preconditions.*;

public class ArrayWrappingBytesValue extends AbstractBytesValue {

    protected final byte[] bytes;
    protected final int offset;
    protected final int length;

    public ArrayWrappingBytesValue(final byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    ArrayWrappingBytesValue(final byte[] bytes, final int offset, final int length) {
        checkNotNull(bytes, "Invalid 'null' byte array provided");
        checkArgument(length >= 0, "Invalid negative length provided");
        if (bytes.length > 0) {
            checkElementIndex(offset, bytes.length);
        }
        checkArgument(
                offset + length <= bytes.length,
                "Provided length %s is too big: the value has only %s bytes from offset %s",
                length,
                bytes.length - offset,
                offset);

        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    private byte[] extractOrGetArrayUnsafe() {
        if (offset == 0 && length == bytes.length) {
            return bytes;
        }

        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public byte get(final int i) {
        // Check bounds because while the array access would throw, the error message would be confusing
        // for the caller.
        checkElementIndex(i, size());
        return bytes[offset + i];
    }

    @Override
    public BytesValue slice(final int index, final int length) {
        if (index == 0 && length == size()) {
            return this;
        }
        if (length == 0) {
            return BytesValue.EMPTY;
        }

        checkElementIndex(index, size());
        checkArgument(
                index + length <= size(),
                "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
                length,
                size(),
                size() - index,
                index);

        return length == Bytes32.SIZE
                ? new ArrayWrappingBytes32(bytes, offset + index)
                : new ArrayWrappingBytesValue(bytes, offset + index, length);
    }

    byte[] arrayCopy() {
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    @Override
    public BytesValue copy() {
        // Because MutableArrayWrappingBytesValue overrides this, we know we are immutable. We may
        // retain more than necessary however.
        if (offset == 0 && length == bytes.length) {
            return this;
        }

        return new ArrayWrappingBytesValue(arrayCopy());
    }

    @Override
    public MutableBytesValue mutableCopy() {
        return new MutableArrayWrappingBytesValue(arrayCopy());
    }

    @Override
    public int commonPrefixLength(final BytesValue other) {
        if (!(other instanceof ArrayWrappingBytesValue)) {
            return super.commonPrefixLength(other);
        }
        final ArrayWrappingBytesValue o = (ArrayWrappingBytesValue) other;
        int i = 0;
        while (i < length && i < o.length && bytes[offset + i] == o.bytes[o.offset + i]) {
            i++;
        }
        return i;
    }

    @Override
    public void update(final MessageDigest digest) {
        digest.update(bytes, offset, length);
    }

    @Override
    public void copyTo(final MutableBytesValue dest) {
        checkArgument(
                dest.size() == size(),
                "Cannot copy %s bytes to destination of non-equal size %s",
                size(),
                dest.size());

        copyTo(dest, 0);
    }

    @Override
    public void copyTo(final MutableBytesValue destination, final int destinationOffset) {
        if (!(destination instanceof MutableArrayWrappingBytesValue)) {
            super.copyTo(destination, destinationOffset);
            return;
        }

        // Special casing an empty source or the following checks might throw (even though we have
        // nothing to copy anyway) and this gets inconvenient for generic methods using copyTo() as
        // they may have to special case empty values because of this. As an example,
        // concatenate(EMPTY, EMPTY) would need to be special cased without this.
        if (size() == 0) {
            return;
        }

        checkElementIndex(destinationOffset, destination.size());
        checkArgument(
                destination.size() - destinationOffset >= size(),
                "Cannot copy %s bytes, destination has only %s bytes from index %s",
                size(),
                destination.size() - destinationOffset,
                destinationOffset);

        final MutableArrayWrappingBytesValue d = (MutableArrayWrappingBytesValue) destination;
        System.arraycopy(bytes, offset, d.bytes, d.offset + destinationOffset, size());
    }

    @Override
    public void appendTo(final Buffer buffer) {
        buffer.appendBytes(bytes, offset, length);
    }

    @Override
    public byte[] getArrayUnsafe() {
        return extractOrGetArrayUnsafe();
    }
}
