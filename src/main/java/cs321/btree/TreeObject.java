package cs321.btree;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A {@link TreeObject} represents a subsequence of DNA and the frequency with which it occurred during the construction
 * of its BTree.
 * <p>
 * Get the subsequence of this {@link TreeObject} by calling {@link TreeObject#getSubsequence()}. Get the frequency of
 * this {@link TreeObject} by calling {@link TreeObject#getCount()}. Increment the frequency of this
 * {@link TreeObject} by calling {@link TreeObject#incrementFrequency()}.
 * <p>
 * You can create a {@link TreeObject} from a {@link ByteBuffer} by calling
 * {@link TreeObject#fromByteBuffer(ByteBuffer)}. You can write a {@link TreeObject} to a
 * {@link ByteBuffer} by calling {@link TreeObject#writeToByteBuffer(ByteBuffer)}.
 * <p>
 * The {@link TreeObject#BYTE_SIZE} constant is the number of bytes that a {@link TreeObject} takes
 * up in a {@link ByteBuffer}, and is useful in allocating the space ahead of time.
 *
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 */
public class TreeObject implements Comparable<TreeObject> {
    /**
     * The size of a {@link TreeObject} in bytes.
     */
    public static final int BYTE_SIZE = Long.BYTES + Integer.BYTES;  // subsequence + frequency

    /**
     * The DNA subsequence of this {@link TreeObject}, represented as a long.
     */
    private final long subsequence;

    /**
     * The frequency that this subsequence occurs during the construction of its containing {@link BTree}.
     */
    private int frequency;

    /**
     * Create a new {@link TreeObject} with the given subsequence and frequency.
     * This is only intended to be used when reading a {@link TreeObject} from disk; this is provided for by the
     * {@link TreeObject#fromByteBuffer(ByteBuffer)} method.
     *
     * @param subsequence   The subsequence of this TreeObject.
     * @param frequency     The frequency of this TreeObject.
     */
    public TreeObject(long subsequence, int frequency)
    {
        if (frequency < 1) {  // Must be true + guards against bad BTreeNode reads
            throw new IllegalArgumentException("Frequency must be greater than 0.");
        }

        this.subsequence = subsequence;
        this.frequency = frequency;
    }

    /**
     * Create a new {@link TreeObject} with the given subsequence and a frequency of 1.
     *
     * @param subsequence The subsequence of this TreeObject.
     */
    public TreeObject(long subsequence) {
        this(subsequence, 1);
    }

    /**
     * Get the subsequence of this {@link TreeObject}.
     *
     * @return The subsequence of this {@link TreeObject}.
     */
    public long getSubsequence() {
        return subsequence;
    }

    /**
     * Increment the frequency of this {@link TreeObject} by 1.
     */
    public void incrementFrequency() {
        frequency++;
    }

    /**
     * Get the frequency of this {@link TreeObject}.
     * <p>
     * This is called getCount due to the project-given tests calling it this way. However, all the
     * documentation refers to the number as "frequency".
     *
     * @return The frequency of this {@link TreeObject}.
     */
    public int getCount() {
        return frequency;
    }

    /**
     * Create a new {@link TreeObject} from the next bytes to be consumed in a {@link ByteBuffer}.
     * This method assumes that the buffer has at least {@link TreeObject#BYTE_SIZE} remaining bytes.
     * If there are not enough bytes remaining, an {@link BufferUnderflowException} will be thrown.
     *
     * @param buffer                    The {@link ByteBuffer} to read from.
     * @return                          A new {@link TreeObject} with the data from the {@link ByteBuffer}.
     * @throws BufferUnderflowException if the buffer does not have enough remaining bytes.
     */
    public static TreeObject fromByteBuffer(ByteBuffer buffer) {
        if (buffer.remaining() < BYTE_SIZE) {
            throw new BufferUnderflowException();
        }
        long subsequence = buffer.getLong();
        int frequency = buffer.getInt();
        return new TreeObject(subsequence, frequency);
    }

    /**
     * Write the data of this {@link TreeObject} instance to a {@link ByteBuffer}.
     * This method assumes that the buffer has been properly allocated with enough space.
     * If the buffer does not have enough space, an {@link BufferOverflowException} will be thrown.
     *
     * @param buffer                    The {@link ByteBuffer} to write to.
     * @throws BufferOverflowException  if the buffer does not have enough space.
     */
    public void writeToByteBuffer(ByteBuffer buffer) {
        if (buffer.remaining() < BYTE_SIZE) {
            throw new BufferOverflowException();
        }
        buffer.putLong(subsequence);
        buffer.putInt(frequency);
    }

    /**
     * Compare this {@link TreeObject} to another {@link TreeObject} by their subsequence.
     *
     * @param treeObject    The {@link TreeObject} to compare to.
     * @return              -1 if this {@link TreeObject} is less than the other {@link TreeObject}, 1 if this
     */
    @Override
    public int compareTo(TreeObject treeObject) {
        return Long.compare(this.subsequence, treeObject.subsequence);
    }
}
