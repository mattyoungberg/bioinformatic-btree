package cs321.btree;

import cs321.create.SequenceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link BTreeInOrderIterator}.
 *
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 */
public class BTreeInOrderIteratorTest {

    /**
     * The testFile that will get created for every test.
     */
    private static final File testFile = new File("Test_BTree.tmp");

    /**
     * The expected order of elements inserted into the BTree for Homework 5, Question 1.
     */
    private static final long[] h5q1Array = { 112L, 121L, 113L, 120L, 114L, 119L, 115L, 118L, 116L };

    /**
     * Deletes the test file if it exists.
     */
    @Before
    public void setUp() {
        if (testFile.exists() && !testFile.isDirectory()) {
            testFile.delete();
        }
    }

    /**
     * Deletes the test file if it exists.
     */
    @After
    public void tearDown() {
        if (testFile.exists() && !testFile.isDirectory()) {
            testFile.delete();
        }
    }

    /**
     * Tests that the iterator can indeed be constructed.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if construction of the BTreeInOrderIterator fails
     */
    @Test
    public void canConstruct() throws BTreeException, IOException {
        new BTreeInOrderIterator(new BTree(2, testFile.getName()));
    }

    /**
     * Tests that an iterator correctly handles a single element that's been inserted 10 times.
     *
     * @throws BTreeException
     * @throws IOException
     */
    @Test
    public void testMultipleInsertions() throws BTreeException, IOException {
        BTree btree = new BTree(2, testFile.getName());
        for (int i = 0; i < 10; i++) {
            btree.insert(new TreeObject(1L));
        }

        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);
        assertTrue(iter.hasNext());
        TreeObject next = iter.next();
        assertEquals(1L, next.getSubsequence());
        assertEquals(10, next.getCount());
        assertFalse(iter.hasNext());
    }

    //////////////////////////////////////////////////
    // Homework 5, Question 1 scenario
    // Insert {p, y, q, x, r, w, s, v, t} successively
    // into BTree of degree t = 2
    //////////////////////////////////////////////////

    /**
     * Helper method to create a BTree for Homework 5, Question 1.
     *
     * @param count             The number of elements to insert into the BTree. For example, if you want to insert
     *                          {p, y, q, x, r, w, s, v, t}, then <code>count</code> should be 9.
     * @return                  the BTree with the given elements inserted
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into the BTree fails
     */
    private BTree getBTreeH5Q1(int count) throws BTreeException, IOException {
        BTree btree = new BTree(2, testFile.getName());
        for (int i = 0; i < count; i++) {
            btree.insert(new TreeObject(h5q1Array[i]));
        }
        return btree;
    }

    /**
     * Tests step one of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step1() throws BTreeException, IOException {
        int count = 1;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step two of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step2() throws BTreeException, IOException {
        int count = 2;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step three of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step3() throws BTreeException, IOException {
        int count = 3;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step four of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step4() throws BTreeException, IOException {
        int count = 4;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step five of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step5() throws BTreeException, IOException {
        int count = 5;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 114L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step six of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step6() throws BTreeException, IOException {
        int count = 6;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 114L, 119L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step seven of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step7() throws BTreeException, IOException {
        int count = 7;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 114L, 115L, 119L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step eight of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step8() throws BTreeException, IOException {
        int count = 8;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 114L, 115L, 118L, 119L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests step nine of Homework 5, Question 1.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void h5q1Step9() throws BTreeException, IOException {
        int count = 9;
        BTree btree = getBTreeH5Q1(count);
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        long[] expected = new long[] { 112L, 113L, 114L, 115L, 116L, 118L, 119L, 120L, 121L };

        for (int i = 0; i < count; i++) {
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSubsequence(), expected[i]);
        }
        assertFalse(iter.hasNext());
    }

    //////////////////////////////////////
    // END Homework 5, Question 1 scenario
    //////////////////////////////////////

    /**
     * Tests that the iterator iterates in order given a randomized permutation of the numbers 0 to 9999.
     * <p>
     * This is a "stress test". Though its height won't get above 1, it will test the iterator's ability to iterate
     * over a large number of elements.
     * <p>
     * This test takes about ten seconds on my machine (Z-series i7 Intel processor, 32GB DDR5 RAM, SSD).
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void testTenThousandElements() throws BTreeException, IOException {
        BTree btree = new BTree(testFile.getName());  // picks optimal degree

        // Create and populate the list
        List<Long> elements = new ArrayList<>();
        for (long i = 0; i < 10000; i++) {
            elements.add(i);
        }

        // Shuffle the list
        Collections.shuffle(elements);

        // Insert elements from the shuffled list and log every 1000 insertions
        for (Long element : elements) {
            btree.insert(new TreeObject(element));
        }

        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);
        long expectedValue = 0;
        while (iter.hasNext()) {
            assertEquals(iter.next().getSubsequence(), expectedValue);
            expectedValue++;
        }
    }

    /**
     * Tests that the iterator returns correctly even with a tall tree.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void testHeight() throws BTreeException, IOException {
        BTree btree = new BTree(2, testFile.getName());

        // Create and populate the list
        List<Long> elements = new ArrayList<>();
        for (long i = 0; i < 100; i++) {
            elements.add(i);
        }

        // Shuffle the list
        Collections.shuffle(elements);

        // Insert elements from the shuffled list and log every 1000 insertions
        for (Long element : elements) {
            btree.insert(new TreeObject(element));
        }

        assertEquals(4, btree.getHeight());  // Should break so we can change this test if encoding changes

        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);
        long expectedValue = 0;
        while (iter.hasNext()) {
            assertEquals(iter.next().getSubsequence(), expectedValue);
            expectedValue++;
        }
    }

    ///////////////
    // Corner cases
    ///////////////

    /**
     * Tests that the iterator behaves properly when the BTree is empty.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void emptyTree() throws BTreeException, IOException {
        BTree btree = new BTree(testFile.getName());
        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail("Expected NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
            // pass
        }
    }

    //////////////////////////////////////
    // Found bugs whose tests verify fixes
    //////////////////////////////////////

    /**
     * Tests the general case that we can insert DNA bases and iterate over them in order.
     * <p>
     * The original bug here stemmed from an error in the insertion procedure. The bug would show itself any time that
     * a duplicate was attempting to be inserted into a leaf node. The procedure would copy the references of the
     * existing elements to make room for the new element, but it would do so before checking for duplicates. It would
     * find the duplicate correctly, increment its frequency, and return early, but the copies that had been made to
     * make space for the insertion were preemptive; when an insertion ultimately did not happen, two references to a
     * single `TreeObject` would exist in the leaf node, and the iterator would return the same element twice.
     *
     * @throws BTreeException   if construction of the BTree fails
     * @throws IOException      if insertion into or iteration over the BTree fails
     */
    @Test
    public void testAddingSubsequences() throws BTreeException, IOException {
        BTree btree = new BTree(2, testFile.getName());
        TreeObject.subsequenceLength = 1;

        long a = SequenceUtils.dnaStringToLong("a");
        long t = SequenceUtils.dnaStringToLong("t");
        long c = SequenceUtils.dnaStringToLong("c");
        long g = SequenceUtils.dnaStringToLong("g");

        // Insertion order matters here to duplicate the bug

        btree.insert(new TreeObject(a));

        btree.insert(new TreeObject(t));
        btree.insert(new TreeObject(t));
        btree.insert(new TreeObject(t));
        btree.insert(new TreeObject(t));

        btree.insert(new TreeObject(c));
        btree.insert(new TreeObject(c));

        btree.insert(new TreeObject(g));
        btree.insert(new TreeObject(g));  // This is the insertion that would cause the bug
        btree.insert(new TreeObject(g));

        BTreeInOrderIterator iter = new BTreeInOrderIterator(btree);

        // A
        assertTrue(iter.hasNext());
        TreeObject aObj = iter.next();
        assertEquals(0L, aObj.getSubsequence());
        assertEquals(1, aObj.getCount());

        // C
        assertTrue(iter.hasNext());
        TreeObject cObj = iter.next();
        assertEquals(1L, cObj.getSubsequence());
        assertEquals(2, cObj.getCount());

        // G
        assertTrue(iter.hasNext());
        TreeObject gObj = iter.next();
        assertEquals(2L, gObj.getSubsequence());
        assertEquals(3, gObj.getCount());

        // T
        assertTrue(iter.hasNext());
        TreeObject tObj = iter.next();
        assertEquals(3L, tObj.getSubsequence());
        assertEquals(4, tObj.getCount());

        assertFalse(iter.hasNext());
    }
}