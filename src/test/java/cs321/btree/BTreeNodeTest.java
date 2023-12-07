package cs321.btree;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Tests for the {@link BTreeNode} class.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class BTreeNodeTest {

    /**
     * Test the constructor for a {@link BTreeNode} that is stored on disk.
     * <p>
     * Note that we don't actually have to read from disk; it's called the `disk constructor` because that's when its
     * wielded.
     */
    @Test
    public void testDiskConstructor() {
        new BTreeNode(new TreeObject[3], new long[4]);
    }

    /**
     * Test the constructor for a new {@link BTreeNode}.
     */
    @Test
    public void testNewConstructor() {
        new BTreeNode(2);
    }

    /**
     * Test the {@link BTreeNode#getByteSize(int)} method when t = 2.
     */
    @Test
    public void testGetByteSizeT2() {
        int t = 2;
        int size = BTreeNode.getByteSize(t);
        assert size == ((2 * t - 1) * TreeObject.BYTE_SIZE) + (2 * t * Long.BYTES);
    }

    /**
     * Test the {@link BTreeNode#getByteSize(int)} method when t = 3.
     */
    @Test
    public void testGetByteSizeT3() {
        int t = 3;
        int size = BTreeNode.getByteSize(t);
        assert size == ((2 * t - 1) * TreeObject.BYTE_SIZE) + (2 * t * Long.BYTES);
    }

    /**
     * Test that a {@link BTreeNode} can be written to a {@link ByteBuffer} correctly.
     */
    @Test
    public void testWriteToByteBuffer() {
        int t = 3;
        BTreeNode node = new BTreeNode(t);
        ByteBuffer buffer = ByteBuffer.allocate(BTreeNode.getByteSize(t));
        node.writeToByteBuffer(buffer);
        buffer.position(0);
        assert buffer.remaining() == BTreeNode.getByteSize(t);
    }

    /**
     * Test that a {@link BTreeNode} can be read from a {@link ByteBuffer} correctly.
     */
    @Test
    public void testReadFromByteBuffer() {
        int t = 3;
        BTreeNode writeNode = new BTreeNode(t);
        ByteBuffer buffer = ByteBuffer.allocate(BTreeNode.getByteSize(t));
        writeNode.writeToByteBuffer(buffer);
        buffer.position(0);
        BTreeNode readNode = BTreeNode.fromByteBuffer(buffer, t);
        assert readNode.keys.length == writeNode.keys.length;
        assert readNode.childPositions.length == writeNode.childPositions.length;
        assert readNode.leaf;
        assert readNode.keyCount == 0;
    }

    /**
     * Tests that a {@link BTreeNode} can be written to a {@link ByteBuffer} and then read from that {@link ByteBuffer}
     * correctly.
     */
    @Test
    public void testWriteReadCombination() {
        int t = 3;
        BTreeNode writeNode = new BTreeNode(t);
        writeNode.keys[0] = new TreeObject(0, 1);
        writeNode.keys[1] = new TreeObject(1, 3);
        writeNode.keys[2] = new TreeObject(2, 4);
        writeNode.childPositions[0] = 1;
        writeNode.childPositions[1] = 2;
        writeNode.childPositions[2] = 3;
        writeNode.childPositions[3] = 4;
        writeNode.leaf = false;
        writeNode.keyCount = 3;
        ByteBuffer buffer = ByteBuffer.allocate(BTreeNode.getByteSize(t));
        writeNode.writeToByteBuffer(buffer);
        buffer.flip();
        BTreeNode readNode = BTreeNode.fromByteBuffer(buffer, t);
        assert readNode.keys.length == writeNode.keys.length;
        assert readNode.childPositions.length == writeNode.childPositions.length;
        assert readNode.keys[0].getSubsequence() == writeNode.keys[0].getSubsequence();
        assert readNode.keys[1].getSubsequence() == writeNode.keys[1].getSubsequence();
        assert readNode.keys[2].getSubsequence() == writeNode.keys[2].getSubsequence();
        assert readNode.keys[0].getCount() == writeNode.keys[0].getCount();
        assert readNode.keys[1].getCount() == writeNode.keys[1].getCount();
        assert readNode.keys[2].getCount() == writeNode.keys[2].getCount();
        assert readNode.childPositions[0] == writeNode.childPositions[0];
        assert readNode.childPositions[1] == writeNode.childPositions[1];
        assert readNode.childPositions[2] == writeNode.childPositions[2];
        assert readNode.childPositions[3] == writeNode.childPositions[3];
        assert readNode.leaf == writeNode.leaf;
        assert readNode.keyCount == writeNode.keyCount;
    }

    /**
     * Tests that a {@link BTreeNode} can be written to a {@link ByteBuffer} and then read from that {@link ByteBuffer}
     * correctly when there are nulls in the keys array.
     */
    @Test
    public void testWriteReadCombinationWithNulls() {
        int t = 3;
        BTreeNode writeNode = new BTreeNode(t);
        writeNode.keys[0] = new TreeObject(0, 1);
        writeNode.keys[1] = new TreeObject(2, 4);
        writeNode.keys[2] = null;  // explicit
        writeNode.childPositions[0] = 1;
        writeNode.childPositions[1] = 2;
        writeNode.childPositions[2] = 3;
        writeNode.childPositions[3] = 0;  // Default value
        writeNode.leaf = false;
        writeNode.keyCount = 2;
        ByteBuffer buffer = ByteBuffer.allocate(BTreeNode.getByteSize(t));
        writeNode.writeToByteBuffer(buffer);
        buffer.position(0);
        BTreeNode readNode = BTreeNode.fromByteBuffer(buffer, t);
        assert readNode.keys.length == writeNode.keys.length;
        assert readNode.childPositions.length == writeNode.childPositions.length;
        assert readNode.keys[0].getSubsequence() == writeNode.keys[0].getSubsequence();
        assert readNode.keys[1].getSubsequence() == writeNode.keys[1].getSubsequence();
        assert readNode.keys[2] == writeNode.keys[2];
        assert readNode.keys[0].getCount() == writeNode.keys[0].getCount();
        assert readNode.keys[1].getCount() == writeNode.keys[1].getCount();
        assert readNode.keys[2] == writeNode.keys[2];
        assert readNode.childPositions[0] == writeNode.childPositions[0];
        assert readNode.childPositions[1] == writeNode.childPositions[1];
        assert readNode.childPositions[2] == writeNode.childPositions[2];
        assert readNode.childPositions[3] == writeNode.childPositions[3];
        assert readNode.leaf == writeNode.leaf;
        assert readNode.keyCount == writeNode.keyCount;
    }
}
