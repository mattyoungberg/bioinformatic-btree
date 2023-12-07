package cs321.btree;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Tests for the TreeObject class.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class TreeObjectTest {

    /**
     * Tests the constructor the reconsitutes an object from disk.
     */
    @Test
    public void testConstructor() {
        new TreeObject(99L, 3);
    }

    /**
     * Tests the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        new TreeObject(99L);
    }

    /**
     * Tests the constructor with a bad frequency.
     */
    @Test
    public void testConstructorBadFrequency() {
        try {
            TreeObject treeObject = new TreeObject(99L, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * Tests that the subsequence can be properly retrieved.
     */
    @Test
    public void testGetSubsequence() {
        TreeObject treeObject = new TreeObject(99L, 3);
        assertEquals(99L, treeObject.getSubsequence());
    }

    /**
     * Tests that the frequency can be properly incremented.
     */
    @Test
    public void testIncrementFrequency() {
        TreeObject treeObject = new TreeObject(99L, 3);
        treeObject.incrementFrequency();
        assertEquals(4, treeObject.getCount());
    }

    /**
     * Tests that the frequency can be properly retrieved when set in the constructor for reconsitution.
     */
    @Test
    public void testGetFrequency() {
        TreeObject treeObject = new TreeObject(99L, 3);
        assertEquals(3, treeObject.getCount());
    }

    /**
     * Tests that expectations regarding the {@link TreeObject} and its bytes size are correctly aligned.
     */
    @Test
    public void testGetByteSize() {
        assertEquals(12, TreeObject.BYTE_SIZE);
    }

    /**
     * Tests that the TreeObject can be properly reconstituted from a {@link ByteBuffer}.
     */
    @Test
    public void testFromByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0, 0, 0, 0, 0, 0, 0, 99, 0, 0, 0, 3});
        TreeObject treeObject = TreeObject.fromByteBuffer(buffer);
        assertEquals(99L, treeObject.getSubsequence());
        assertEquals(3, treeObject.getCount());
    }

    /**
     * Tests that the TreeObject can be properly written to a {@link ByteBuffer}.
     */
    @Test
    public void testWriteToByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        TreeObject treeObject = new TreeObject(99L, 3);
        treeObject.writeToByteBuffer(buffer);
        assertEquals(99L, buffer.getLong(0));
        assertEquals(3, buffer.getInt(8));
    }

    /**
     * Tests a simple case that the TreeObject can be properly compared to another TreeObject.
     */
    @Test
    public void testCompare() {
        TreeObject treeObject = new TreeObject(99L, 3);
        TreeObject treeObject2 = new TreeObject(99L, 4);
        assertEquals(0, treeObject.compareTo(treeObject2));
    }
}
