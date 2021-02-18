package com.microsoft.sqlserver.jdbc;

import java.io.InputStream;

/**
 * Provides an implementation of a "pollable" InputStream which can poll()
 * a connection without consuming a byte, determining if the connection has
 * been broken. Any bytes read by poll() calls are returned to subsequent
 * read() requests.
 */
public class PollableInputStream extends InputStream {

    /**
     * The underlying stream.
     */
    private InputStream stream;

    /**
     * Bytes that have been read by a poll(s).
     */
    private byte cachedBytes[];

    /**
     * How many bytes have been cached.
     */
    private int cachedLength;

    /**
     * The constructor accepts an InputStream to setup the
     * object.
     * 
     * @param inputStream
     *        The InputStream to parse.
     */
    public PollableInputStream(InputStream inputStream) {
        this.stream = inputStream;
        this.cachedBytes = new byte[10];
        this.cachedLength = 0;
    }

    /**
     * Poll the stream to verify connectivity.
     * 
     * @return true if the stream is readable.
     * @throws IOException
     *         If an I/O exception occurs.
     */
    public boolean poll() throws IOException {
        byte b = this.stream.read();
        
        // if we got here, a byte was read and we need to save it

        // Increase the size of the cache, if needed (should be very rare).
        if (this.cachedBytes.length <= cachedLength) {
            byte temp[] = new byte[this.cachedBytes.length + 10];
            for (int i = 0; i < this.cachedBytes.length; i++) {
                temp[i] = this.cachedBytes[i];
            }

            this.cachedBytes = temp;
        }

        this.cachedLength++;
        this.cachedBytes[this.cachedLength] = b;

        return true;
    }

    /*
    * Read a single byte from the stream.
    *
    * @return The character that was read from the stream.
    * @throws IOException
    *         If an I/O exception occurs. 
    */
    @Override
    public int read() throws IOException
    {
        if (this.cachedLength == 0) {
           return this.stream.read();
        }

        int result = this.cachedBytes[0];
        this.cachedLength--;
        for (int i = 0; i < this.peekLength; i++) {
            this.cachedBytes[i] = this.cachedBytes[i + 1];
        }

        return result;
    }
}
