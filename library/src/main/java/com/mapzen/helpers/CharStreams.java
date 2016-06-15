package com.mapzen.helpers;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Provides utility methods for working with character streams.
 *
 * <p>All method parameters must be non-null unless documented otherwise.
 *
 * <p>Some of the methods in this class take arguments with a generic type of
 * {@code Readable & Closeable}. A {@link java.io.Reader} implements both of
 * those interfaces. Similarly for {@code Appendable & Closeable} and
 * {@link java.io.Writer}.
 *
 * @author Chris Nokleberg
 * @author Bin Zhu
 * @author Colin Decker
 * @since 1.0
 */
public final class CharStreams {
    private static final int BUF_SIZE = 0x800; // 2K chars (4K bytes)

    /**
     * Reads all characters from a {@link Readable} object into a {@link String}.
     * Does not close the {@code Readable}.
     *
     * @param r the object to read from
     * @return a string containing all the characters
     * @throws IOException if an I/O error occurs
     */
    public static String toString(Readable r) throws IOException {
        return toStringBuilder(r).toString();
    }

    /**
     * Reads all characters from a {@link Readable} object into a new
     * {@link StringBuilder} instance. Does not close the {@code Readable}.
     *
     * @param r the object to read from
     * @return a {@link StringBuilder} containing all the characters
     * @throws IOException if an I/O error occurs
     */
    private static StringBuilder toStringBuilder(Readable r) throws IOException {
        StringBuilder sb = new StringBuilder();
        copy(r, sb);
        return sb;
    }

    /**
     * Copies all characters between the {@link Readable} and {@link Appendable}
     * objects. Does not close or flush either object.
     *
     * @param from the object to read from
     * @param to the object to write to
     * @return the number of characters copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(Readable from, Appendable to) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        CharBuffer buf = CharBuffer.allocate(BUF_SIZE);
        long total = 0;
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            total += buf.remaining();
            buf.clear();
        }
        return total;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
