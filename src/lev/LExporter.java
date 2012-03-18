/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lev;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A BufferedOutputStream with easy to use write functions.
 * @author Justin Swanson
 */
public class LExporter {

    private BufferedOutputStream output;

    /**
     *
     * @param path Path to open an exporter to.
     * @throws FileNotFoundException
     */
    public LExporter(String path) throws FileNotFoundException {
        openOutput(path);
    }

    /**
     *
     * @param path Path to open a channel to.
     * @throws FileNotFoundException
     */
    public final void openOutput(String path) throws FileNotFoundException {
        Ln.makeDirs(path);
        output = new BufferedOutputStream(new FileOutputStream(path));
    }

    /**
     * Writes a byte array to the file.
     * @param array
     * @throws IOException
     */
    public void write(byte[] array) throws IOException {
        output.write(array);
    }

    /**
     * Writes a byte array to the file.
     * @param array
     * @param size Minimum byte size.  Output will be appended with zeros until size is met.
     * @throws IOException
     */
    public void write(byte[] array, int size) throws IOException {
        write(array);
        if (size - array.length > 0) {
            writeZeros(size - array.length);
        }
    }

    /**
     *
     * @param size Number of zero bytes to write.
     * @throws IOException
     */
    public void writeZeros(int size) throws IOException {
        output.write(new byte[size]);
    }

    /**
     * Writes an integer to the file.  Can span multiple bytes if the integer is large.
     * @param input Integer to output.
     * @param size Minimum byte size.  Output will be appended with zeros until size is met.
     * @throws java.io.IOException
     */
    public void write(int input, int size) throws IOException {
        write(Ln.toByteArray(input, size, size));
    }

    public void write(int input) throws IOException {
	write(Ln.toByteArray(input));
    }

    /**
     * Writes a string to the output (no null terminator).
     * @param input
     * @throws java.io.IOException
     */
    public void write(String input) throws java.io.IOException {
        write(input, 0);
    }

    /**
     * Writes a string to the output (no null terminator).
     * @param input
     * @param size Minimum byte size.  Output will be appended with zeros until size is met.
     * @throws IOException
     */
    public void write(String input, int size) throws IOException {
        write(Ln.toByteArray(input), size);
    }

    /**
     * Writes a float to the file.
     * @param input
     * @throws IOException
     */
    public void write(float input) throws IOException {
        ByteBuffer out = ByteBuffer.allocate(4);
        out.putInt(Integer.reverseBytes(Float.floatToIntBits(input)));
        write(out.array(), 4);
    }

    /**
     * Flushes buffer and closes output stream.
     * @throws IOException
     */
    public void close() throws IOException {
        output.close();
    }
}