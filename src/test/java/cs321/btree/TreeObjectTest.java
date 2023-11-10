package cs321.btree;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TreeObjectTest {

    @Test
    public void testConstructor() {
        TreeObject treeObject = new TreeObject(99L, 3);

    }

    @Test
    public void testDefaultConstructor() {
        TreeObject treeObject = new TreeObject(99L);

    }

    @Test
    public void testConstructorBadFrequency() {
        try {
            TreeObject treeObject = new TreeObject(99L, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetSubsequence() {
        TreeObject treeObject = new TreeObject(99L, 3);
        assertEquals(99L, treeObject.getSubsequence());
    }

    @Test
    public void testIncrementFrequency() {
        TreeObject treeObject = new TreeObject(99L, 3);
        treeObject.incrementFrequency();
        assertEquals(4, treeObject.getCount());
    }

    @Test
    public void testGetFrequency() {
        TreeObject treeObject = new TreeObject(99L, 3);
        assertEquals(3, treeObject.getCount());
    }

    @Test
    public void testGetByteSize() {
        assertEquals(12, TreeObject.BYTE_SIZE);
    }

    @Test
    public void testFromByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0, 0, 0, 0, 0, 0, 0, 99, 0, 0, 0, 3});
        TreeObject treeObject = TreeObject.fromByteBuffer(buffer);
        assertEquals(99L, treeObject.getSubsequence());
        assertEquals(3, treeObject.getCount());
    }

    @Test
    public void testWriteToByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        TreeObject treeObject = new TreeObject(99L, 3);
        treeObject.writeToByteBuffer(buffer);
        assertEquals(99L, buffer.getLong(0));
        assertEquals(3, buffer.getInt(8));
    }

    @Test
    public void testCompare() {
        TreeObject treeObject = new TreeObject(99L, 3);
        TreeObject treeObject2 = new TreeObject(99L, 4);
        assertEquals(0, treeObject.compareTo(treeObject2));
    }
}
