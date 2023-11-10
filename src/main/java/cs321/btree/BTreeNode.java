package cs321.btree;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A {@link BTreeNode} represents a node in a {@link BTree}.
 * <p>
 * Access the keys of this {@link BTreeNode} by operating directly on {@link BTreeNode#keys}. Access the children of
 * this {@link BTreeNode} by operating directly on {@link BTreeNode#childPositions}.
 * <p>
 * Note that there is a degree of determinism that is required to read and write these nodes reliably from disk. Though
 * `t` (or the minimum degree of the {@link BTree}) is taken is as a parameter in many of these methods, it is expected
 * that the {@link BTree} will continue to call with the same value for `t` as previous calls. This is because the `t`
 * value determines the size of the {@link BTreeNode}, and thus the size of the {@link ByteBuffer} that is required to
 * read and write the node. If the `t` value changes, the size of the {@link BTreeNode} will be incorrect, and the
 * read/write operations will fail. A {@link BTreeNode} does not track `t` explicitly because it is expected that the
 * caller, the {@link BTree}, will track it.
 *
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 */
public class BTreeNode {

    /**
     * The keys of this {@link BTreeNode}, all {@link TreeObject}s.
     */
    public TreeObject[] keys;

    /**
     * The positions on disk of the children of this {@link BTreeNode}.
     */
    public long[] childPositions;

    /**
     * Whether this {@link BTreeNode} is a leaf node (implying it has no children).
     */
    public boolean leaf;

    /**
     * The number of keys that this {@link BTreeNode} currently holds.
     */
    public int keyCount;

    /**
     * Whether this {@link BTreeNode} has been persisted to disk.
     */
    public boolean persisted = true;  // TODO I'm still debating whether we need this...


    /**
     * Construct a {@link BTreeNode} from disk with the given keys and children positions.
     * <p>
     * This is intended to be used for reconstruction of a {@link BTreeNode} from disk. <b>Do not use this constructor
     * when creating a new {@link BTreeNode} as the result of a split or other creation mechanism.</b> Instead, use
     * {@link BTreeNode#BTreeNode(int)}.
     *
     * @param keys              The keys of this {@link BTreeNode}.
     * @param childPositions    The positions on disk of the children of this {@link BTreeNode}.
     */
    public BTreeNode(TreeObject[] keys, long[] childPositions) {
        this.keys = keys;
        this.childPositions = childPositions;

        // Derive leaf
        this.leaf = true;
        for (long child : childPositions) {
            if (child != 0) {
                this.leaf = false;
                break;
            }
        }

        // Derive keyCount
        this.keyCount = 0;
        for (TreeObject key : keys) {
            if (key != null) {
                this.keyCount++;
            }
        }
    }

    /**
     * Construct a <i>new</i> {@link BTreeNode} with the given minimum degree.
     * <p>
     * <b>Do not use this method when reconstructing a {@link BTreeNode} from disk.</b> Instead, use
     * {@link BTreeNode#BTreeNode(TreeObject[], long[])}.
     *
     * @param t The minimum degree of the {@link BTree} that this {@link BTreeNode} is a part of.
     */
    public BTreeNode(int t) {
        this(new TreeObject[getMaxKeyCount(t)], new long[getMaxChildCount(t)]);
        this.persisted = false;
    }

    /**
     * Get the derived size of this {@link BTreeNode} in bytes given the minimum degree of the {@link BTree} that this
     * {@link BTreeNode} is a part of.
     *
     * @param t The minimum degree of the {@link BTree} that this {@link BTreeNode} is a part of.
     * @return  The derived size of this {@link BTreeNode} in bytes.
     */
    public static int getByteSize(int t) {
        return (TreeObject.BYTE_SIZE * getMaxKeyCount(t)) + (Long.BYTES * getMaxChildCount(t));
    }

    /**
     * Write this {@link BTreeNode} to the given {@link ByteBuffer}.
     * <p>
     * No matter how populated this node is, it will always write the same number of bytes to the {@link ByteBuffer}.
     * This is because of the inefficiencies that would be introduced if the {@link ByteBuffer} had to be resized
     * dynamically on disk. Instead, the {@link ByteBuffer} is allocated ahead of time to be the size of the
     * {@link BTreeNode}, as determined by its minimum degree `t`.
     *
     * @param buffer The {@link ByteBuffer} to write this {@link BTreeNode} to.
     */
    public void writeToByteBuffer(ByteBuffer buffer) {
        for (TreeObject key : keys) {
            if (key == null) {
                // Write a default value or leave the space blank.
                // For example, write TreeObject.BYTE_SIZE number of zero bytes.
                buffer.put(new byte[TreeObject.BYTE_SIZE]);
            } else {
                key.writeToByteBuffer(buffer);
            }
        }
        for (long child : childPositions) {
            buffer.putLong(child);
        }
    }

    /**
     * Read a {@link BTreeNode} from the given {@link ByteBuffer} with the given minimum degree.
     *
     * @param buffer    The {@link ByteBuffer} to read the {@link BTreeNode} from. This {@link ByteBuffer} must be
     *                  positioned at the start of the {@link BTreeNode} to read and have at least the number of bytes
     *                  required to read the {@link BTreeNode}. This can be determined by calling
     *                  {@link BTreeNode#getByteSize(int)}.
     * @param t         The minimum degree of the {@link BTree} that this {@link BTreeNode} is a part of.
     * @return          A new {@link BTreeNode} with the data from the {@link ByteBuffer}.
     */
    public static BTreeNode fromByteBuffer(ByteBuffer buffer, int t) {
        if (buffer.remaining() < getByteSize(t)) {
            throw new BufferUnderflowException();
        }

        int keyCount = getMaxKeyCount(t);
        int childCount = getMaxChildCount(t);

        TreeObject[] keys = new TreeObject[keyCount];
        long[] children = new long[childCount];

        for (int i = 0; i < keyCount; i++) {
            // Read the next TreeObject.BYTE_SIZE bytes to determine if it's a null object.
            byte[] potentialNull = new byte[TreeObject.BYTE_SIZE];
            buffer.get(potentialNull);

            // Determine if the bytes read represent a null object.
            boolean isNull = true;
            for (byte b : potentialNull) {
                if (b != 0) {
                    isNull = false;
                    break;
                }
            }

            if (!isNull) {
                // Move the buffer back and read the actual TreeObject.
                buffer.position(buffer.position() - TreeObject.BYTE_SIZE);
                keys[i] = TreeObject.fromByteBuffer(buffer);
            } else {
                keys[i] = null; // Handle null TreeObject
            }
        }
        for (int i = 0; i < childCount; i++) {
            children[i] = buffer.getLong();
        }

        return new BTreeNode(keys, children);
    }

    /**
     * Get how many keys a {@link BTreeNode} with the given minimum degree `t` can hold.
     * <p>
     * This method exists to encapsulate and control the arithmetic surrounding the number of keys a {@link BTreeNode}
     * can hold.
     *
     * @param t The minimum degree of the {@link BTree} that this {@link BTreeNode} is a part of.
     * @return  The number of keys a {@link BTreeNode} with the given minimum degree `t` can hold.
     */
    private static int getMaxKeyCount(int t) { return 2 * t - 1; }

    /**
     * Get how many children a {@link BTreeNode} with the given minimum degree `t` can hold.
     * <p>
     * This method exists to encapsulate and control the arithmetic surrounding the number of children a
     * {@link BTreeNode} can hold.
     *
     * @param t The minimum degree of the {@link BTree} that this {@link BTreeNode} is a part of.
     * @return  The number of children a {@link BTreeNode} with the given minimum degree `t` can hold.
     */
    private static int getMaxChildCount(int t) { return 2 * t; }
}
