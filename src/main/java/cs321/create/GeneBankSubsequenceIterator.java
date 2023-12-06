package cs321.create;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * An {@link Iterator} that iterates over all subsequences in a GeneBank file, returning them as String objects.
 * <p>
 * Elements outputted by this iterator will work nicely with the {@link SequenceUtils} class.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSubsequenceIterator implements Iterator<String>, AutoCloseable {

    /**
     * The set of all valid DNA bases, upper or lower case.
     */
    private static final Set<Character> DNA_BASES = new HashSet<>(Arrays.asList('A', 'C', 'G', 'T', 'a', 'c', 'g', 't'));

    /**
     * The size of the byte buffer used to read from the file channel.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * The character set assumed by Genebank file schemes.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * The decoder used to translate a ByteBuffer into a CharBuffer.
     */
    private final CharsetDecoder decoder = CHARSET.newDecoder();

    /**
     * The subsequence as an array, useful for only having to make one-by-one changes to the subsequence, which is a
     * fact relied upon by this class's algorithms.
     */
    private final char[] subsequenceArr;

    /**
     * The byte buffer used to read from the file channel. Once read, it will immediately be converted to a CharBuffer.
     */
    private final ByteBuffer byteBuffer;

    /**
     * The file channel connected to the Genebank file.
     */
    private final FileChannel fileChannel;

    /**
     * The character buffer that's currently being read. Note that many character buffers (derivative of the byte
     * buffer) will be created in the process of iteration, but never more than one will be read at a time.
     */
    private CharBuffer charBuffer;

    /**
     * The index where the next character of a subsequence can be placed; Also where a read can begin if translating the
     * {@link #subsequenceArr} into a {@link String}.
     */
    private int subsequenceIdx;

    /**
     * The number of characters that have been placed into the {@link #subsequenceArr}. This is used mainly to assure
     * that we keep on reading in characters until we have a full array that we can begin returning subsequences from.
     */
    private int subsequenceFillCount;

    /**
     * Whether the iterator has reached the end of the file.
     */
    private boolean done;

    /**
     * Create a new {@link GeneBankSubsequenceIterator} that iterates over all subsequences in the GeneBank file
     * situated at the given path.
     *
     * @param path                      The path where the Genebank file is located.
     * @param subsequenceLength         The length of the subsequences desired.
     * @throws IOException              If there is an error reading from the fileChannel.
     * @throws IllegalArgumentException If the subsequenceLength is less than 1.
     * @throws IllegalArgumentException If the file does not contain a sequence.
     */
    public GeneBankSubsequenceIterator(Path path, int subsequenceLength) throws IOException {
        if (subsequenceLength < 1) {
            throw new IllegalArgumentException("subsequenceLength must be greater than 0");
        }

        this.byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.fileChannel = FileChannel.open(path, StandardOpenOption.READ);

        // Initialize subsequence
        this.subsequenceArr = new char[subsequenceLength];
        Arrays.fill(this.subsequenceArr, '\0');
        this.subsequenceIdx = 0;
        this.subsequenceFillCount = 0;

        fileChannel.position(findNextOrigin(0L));
        this.done = !advanceToNextCharacter();

        if (this.done) {
            throw new IllegalArgumentException("File does not contain a sequence or one of sufficient length");
        }
    }

    /**
     * Whether there are more subsequences to be returned from the Genebank file.
     *
     * @return `true` if there are more subsequences to be returned from the Genebank file, and `false` if there are
     *         not.
     */
    @Override
    public boolean hasNext() {
        return !this.done;
    }

    /**
     * Get the next subsequence in the GeneBank file.
     *
     * @return The next subsequence in the GeneBank file.
     */
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more subsequences available");
        }

        // At the end of any invocation of `next`, the character buffer should be ready to return a valid DNA base on
        // the next call to `get`. This means that after the subsequence is ready to return, this algo should get all
        // buffers in a state where the next call to `get` will return a valid DNA base OR provide context to this
        // Iterator that we have consumed as many subsequences that the file has to offer.

        // Get the subsequence to return
        char next = charBuffer.get();  // Guaranteed to be a DNA Base
        this.subsequenceArr[this.subsequenceIdx] = Character.toLowerCase(next);
        this.subsequenceIdx = (this.subsequenceIdx + 1) % this.subsequenceArr.length;
        this.subsequenceFillCount++;
        String subsequence = subsequenceToString();  // Value to return

        // Get the object in a state to either 1) return the next subsequence, or 2) alert that we've exhausted the file
        try {
            boolean hasNextSubsequence = advanceToNextCharacter();
            if (!hasNextSubsequence) {
                this.done = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return the subsequence
        return subsequence;
    }

    /**
     * Advances the {@link #charBuffer} to a position where the next character in the subsequence can be read.
     * <p>
     * Returns `true` if the {@link #charBuffer} has been advanced to a position where the next character in the
     * subsequence can be read, and `false` if the {@link #fileChannel} has been exhausted.
     *
     * @return              `true` if the {@link #charBuffer} has been advanced to a position where the next character
 *                          in the subsequence can be read, and `false` if the {@link #fileChannel} has been exhausted.
     * @throws IOException  If there is an error reading from the fileChannel.
     */
    private boolean advanceToNextCharacter() throws IOException {
        char current;
        do {
            boolean succeeded = guaranteeCharBuffer();
            if (!succeeded) {
                return false;
            }

            // Read `charBuffer`
            current = charBuffer.get();

            if (current == 'n' || current == 'N') {  // End subsequence, but continue through the charBuffer
                resetSubsequence();
            } else if (current == '/') {  // End of block; look for new ORIGIN if it exists, or see if we're done
                resetSubsequence();
                int bytesRead = consumeCharBuffer();
                long loc = findNextOrigin(fileChannel.position() - bytesRead);
                if (loc == fileChannel.size()) {
                    return false;
                }
                fileChannel.position(loc);
                consumeCharBuffer();  // Will get reloaded w/ guaranteeCharBuffer
            } else if (DNA_BASES.contains(current)) {
                subsequenceArr[subsequenceIdx] = Character.toLowerCase(current);
                subsequenceIdx = (subsequenceIdx + 1) % subsequenceArr.length;
                if (subsequenceFillCount < subsequenceArr.length) {
                    subsequenceFillCount++;
                }
            }
        } while (subsequenceFillCount < subsequenceArr.length || !DNA_BASES.contains(current));

        // We have a subsequence ready; we need to undo the last read character
        charBuffer.position(charBuffer.position() - 1);
        subsequenceIdx = (subsequenceIdx - 1 + subsequenceArr.length) % subsequenceArr.length;
        subsequenceArr[subsequenceIdx] = '\0';
        subsequenceFillCount--;
        return true;
    }


    /**
     * Find the starting position of the next ORIGIN block within the Genebank File.
     * <p>
     * Note that this will not return the position of the keyword ORIGIN, but rather the start of the line after its
     * declaration.
     *
     * @param start         The starting position to begin searching from.
     * @return              The starting position of the next ORIGIN block.
     * @throws IOException  If there is an error reading from the fileChannel.
     */
    private long findNextOrigin(long start) throws IOException {
        char c;
        String searchString = "ORIGIN";
        StringBuilder rollingWindow = new StringBuilder(searchString.length());
        boolean originFound = false;

        fileChannel.position(start);
        byteBuffer.clear();

        while (fileChannel.read(byteBuffer) != -1) {  // Read in next 1024 bytes
            byteBuffer.flip();
            charBuffer = decoder.decode(byteBuffer);  // Translate to UTF-8
            while (charBuffer.hasRemaining()) {
                c = charBuffer.get();
                if (!originFound) {
                    rollingWindow.append(c);

                    // Keep the rolling window the same size as the search string
                    if (rollingWindow.length() > searchString.length()) {
                        rollingWindow.deleteCharAt(0);
                    }

                    // Check if the rolling window matches the search string
                    if (rollingWindow.toString().equals(searchString)) {
                        originFound = true;
                        rollingWindow.setLength(0); // Clear the rolling window
                    }
                } else {
                    // After "ORIGIN" is found, look for the newline character
                    if (c == '\n') {
                        // Return the byte position right after the newline
                        return fileChannel.position() - consumeCharBuffer();
                    } else if (c != ' ') {
                        // If any character other than space or carriage return is found, reset
                        originFound = false;
                    }
                }
            }
            byteBuffer.clear();
        }
        return fileChannel.size();  // if not found
    }

    /**
     * Resets all fields relating to the current subsequence.
     * <p>
     * This should be called anytime a '/' or an 'n' is encountered in the {@link #charBuffer}, as they effectively
     * reset the current subsequence.
     */
    private void resetSubsequence() {
        Arrays.fill(this.subsequenceArr, '\0');
        this.subsequenceFillCount = 0;
        this.subsequenceIdx = 0;
    }

    /**
     * Guarantees that the {@link #charBuffer} is ready to be read from.
     * <p>
     * The method aims to guarantee that `get()` can be called on the {@link #charBuffer} without throwing an
     * {@link java.nio.BufferUnderflowException}. If there are remaining characters in {@link #charBuffer}, then this
     * method does nothing. If there are no remaining characters in {@link #charBuffer}, then this method will attempt
     * to read from the {@link #fileChannel} and guarantee the {@link #charBuffer} after the method returns.
     * <p>
     * This method will return `true` if the {@link #charBuffer} has remaining characters after this method is called,
     * and `false` if the {@link #fileChannel} has been exhausted.
     *
     * @return              `true` if the {@link #charBuffer} has remaining characters after this method is called, and
     *                      `false` if the {@link #fileChannel} has been exhausted.
     * @throws IOException  If there is an error reading from the fileChannel.
     */
    private boolean guaranteeCharBuffer() throws IOException {
        if (charBuffer != null && charBuffer.hasRemaining()) {
            return true;
        }

        if (fileChannel.position() == fileChannel.size()) {
            return false;
        }

        byteBuffer.clear();
        int bytesRead = fileChannel.read(byteBuffer);

        if (bytesRead == -1) {
            // End of the file channel has been reached
            return false;
        }

        byteBuffer.flip();
        charBuffer = decoder.decode(byteBuffer);

        return charBuffer.hasRemaining();
    }

    /**
     * Consumes the remainder of the {@link #charBuffer} and returns how many bytes remained.
     * <p>
     * This method is intended to be used when an `//` is encountered in the {@link #charBuffer}, indicating the end of
     * an ORIGIN block. The returned value is useful to combine with the current {@link #fileChannel} position in order
     * to start a search for the next origin or EOF in the file.
     *
     * @return  The number of bytes consumed from the {@link #charBuffer}.
     */
    private int consumeCharBuffer() {
        if (charBuffer == null || !charBuffer.hasRemaining()) {
            return 0;
        }

        int count = 0;
        char current;
        while (charBuffer.hasRemaining()) {
            current = charBuffer.get();
            count += Character.toString(current).getBytes(GeneBankSubsequenceIterator.CHARSET).length;
        }

        return count;
    }

    /**
     * Interprets the current {@link #subsequenceArr} as a String, to be returned by calls to {@link #next()}.
     * <p>
     * This method encapsulates the loop-around logic necessary to produce a string in the correct order.
     *
     * @return  The current {@link #subsequenceArr} as a String.
     */
    private String subsequenceToString() {
        int currentIdx;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subsequenceArr.length; i++) {
            currentIdx = (subsequenceIdx + i) % subsequenceArr.length;
            sb.append(subsequenceArr[currentIdx]);
        }
        return sb.toString();
    }

    /**
     * Close the file channel.
     *
     * @throws IOException  If there is an error closing the file channel.
     */
    @Override
    public void close() throws IOException {
        fileChannel.close();
        this.byteBuffer.clear();
        consumeCharBuffer();
        this.done = true;
    }
}
