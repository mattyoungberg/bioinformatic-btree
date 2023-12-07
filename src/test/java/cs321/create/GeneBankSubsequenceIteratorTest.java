package cs321.create;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link GeneBankSubsequenceIterator}.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSubsequenceIteratorTest {

    /**
     * Tests that the constructor works.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void canConstruct() throws IOException {
        Path path = getCondensedTest0GBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    /**
     * Tests that the constructor throws an {@link IllegalArgumentException} when a negative subsequence length is
     * given.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithNegativeK() throws IOException {
        Path path = getCondensedTest0GBK();
        new GeneBankSubsequenceIterator(path, -1);
    }

    /**
     * Tests that the constructor throws an {@link IllegalArgumentException} when an empty file is given.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithEmpty() throws IOException {
        Path path = getEmptyGBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    //////////////////////////////////////////////////////////////////////////
    // MAIN TESTS: These are testing a condensed version of the test0.gbk file
    //////////////////////////////////////////////////////////////////////////

    /**
     * Tests the {@link GeneBankSubsequenceIterator} against the condensed version of the test0.gbk file with sequence
     * length 1.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void iterateWithSubsequence1() throws IOException {
        Path path = getCondensedTest0GBK();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 1);
        String expectedSequence = "gatcctccatatcaacggtatctccacctcaggtttagatctcaacaacggaaccattgccgactaacc";
        for (int i = 0; i < expectedSequence.length(); i++) {
            assertTrue(iterator.hasNext());
            assertEquals(Character.toString(expectedSequence.charAt(i)), iterator.next());
        }
    }

    /**
     * Tests the {@link GeneBankSubsequenceIterator} against the condensed version of the test0.gbk file with sequence
     * length 10.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void iterateWithSubsequence10() throws IOException {
        Path path = getCondensedTest0GBK();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 10);

        List<String> expectedSubsequences = Arrays.asList(
                "gatcctccat", "atcctccata", "tcctccatat", "caacggtatc", "aacggtatct", "acggtatctc", "cggtatctcc",
                "ggtatctcca", "gtatctccac", "tatctccacc", "atctccacct", "tctccacctc", "ctccacctca", "tccacctcag",
                "ccacctcagg", "cacctcaggt", "acctcaggtt", "cctcaggttt", "ctcaggttta", "tcaggtttag", "caggtttaga",
                "aggtttagat", "ggtttagatc", "gtttagatct", "tttagatctc", "ttagatctca", "tagatctcaa", "agatctcaac",
                "gatctcaaca", "atctcaacaa", "tctcaacaac", "ctcaacaacg", "tcaacaacgg", "caacaacgga", "aacaacggaa",
                "acaacggaac", "caacggaacc", "aacggaacca", "acggaaccat", "cggaaccatt", "ggaaccattg", "gaaccattgc",
                "aaccattgcc", "accattgccg", "ccattgccga"
        );

        for (String expectedSubsequence : expectedSubsequences) {
            assertTrue(iterator.hasNext());
            assertEquals(expectedSubsequence, iterator.next());
        }

        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the {@link GeneBankSubsequenceIterator} against the condensed version of the test0.gbk file with sequence
     * length 6.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void iterateWithSubsequence6() throws IOException {
        Path path = getCondensedTest0GBK();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 6);

        // First block of sequences
        String[] expectedSequencesFirstBlock = {
                "gatcct", "atcctc", "tcctcc", "cctcca", "ctccat", "tccata", "ccatat", "caacgg", "aacggt", "acggta",
                "cggtat", "ggtatc", "gtatct", "tatctc", "atctcc", "tctcca", "ctccac", "tccacc", "ccacct", "cacctc",
                "acctca", "cctcag", "ctcagg", "tcaggt", "caggtt", "aggttt", "ggttta", "gtttag", "tttaga", "ttagat",
                "tagatc", "agatct", "gatctc", "atctca", "tctcaa", "ctcaac", "tcaaca", "caacaa", "aacaac", "acaacg",
                "caacgg", "aacgga", "acggaa", "cggaac", "ggaacc", "gaacca", "aaccat", "accatt", "ccattg", "cattgc",
                "attgcc", "ttgccg", "tgccga"
        };

        // Second block of sequences
        String[] expectedSequencesSecondBlock = {
                "ctaacc" // The only subsequence in the second block
        };

        for (String expectedSequence : expectedSequencesFirstBlock) {
            assertTrue(iterator.hasNext());
            assertEquals(expectedSequence, iterator.next());
        }

        for (String expectedSequence : expectedSequencesSecondBlock) {
            assertTrue(iterator.hasNext());
            assertEquals(expectedSequence, iterator.next());
        }

        assertFalse(iterator.hasNext());
    }

    //////////////////////////////////////////////////////////////////////////////
    // CORNER CASES: Testing eccentric qualities that might throw off the Iterator
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Tests the corner case that the .gbk file has a capital letter. Project specifications state that the .gbk file
     * may or may not have capital letters, so this test ensures that the iterator can handle both.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void iterateCapitalizedWithSubsequence1() throws IOException {
        Path path = getCondensedCapitalizedTest0GBK();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 1);
        String expectedSequence = "gatcctccatatcaacggtatctccacctcaggtttagatctcaacaacggaaccattgccgactaacc";
        for (int i = 0; i < expectedSequence.length(); i++) {
            assertTrue(iterator.hasNext());
            assertEquals(Character.toString(expectedSequence.charAt(i)), iterator.next());
        }
    }

    /**
     * Tests the corner case that there is an empty ORIGIN block with no sequence.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithEmptyOrigin() throws IOException {
        Path path = getEmptyOriginGBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    /**
     * Tests the corner case that the .gbk file starts with an N.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void canHandleSubsequenceStartingWithN() throws IOException {
        Path path = getStartsN();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 3);
        assertTrue(iterator.hasNext());
        assertEquals("aaa", iterator.next());
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the corner case that the .gbk line ending ends with an N.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test
    public void canHandleLineThatEndsInN() throws IOException {
        Path path = getEndsN();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 9);
        assertTrue(iterator.hasNext());
        assertEquals("aaaaaaaaa", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("aaaaaaaaa", iterator.next());
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the corner case that the subsequence in the .gbk file isn't of sufficient length.
     *
     * @throws IOException If there is an error reading the file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void subsequenceRequiredIsBiggerThanInFile() throws IOException {
        Path path = getEndsN();
        new GeneBankSubsequenceIterator(path, 100);
    }

    //////////////////////////////////
    // Path getters for resource files
    //////////////////////////////////

    /**
     * Gets the {@link Path} for a resource file.
     *
     * @param resourceName  The name of the resource file.
     * @return              The path to the resource file.
     */
    private Path getPathForResource(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + resourceName);
        }
        try {
            return Paths.get(resource.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourceName, e);
        }
    }


    /**
     * Gets the {@link Path} for the condensed version of the test0.gbk file.
     *
     * @return The path for the condensed version of the test0.gbk file.
     */
    private Path getCondensedTest0GBK() {
        return getPathForResource("cs321/create/condensed_test0.gbk");
    }

    /**
     * Gets the {@link Path} for the condensed version of the capitalized test0.gbk file.
     *
     * @return The path for the condensed version of the capitalized test0.gbk file.
     */
    private Path getCondensedCapitalizedTest0GBK() {
        return getPathForResource("cs321/create/condensedCapitalized_test0.gbk");
    }

    /**
     * Gets the {@link Path} for the empty.gbk file.
     *
     * @return The path for the empty.gbk file.
     */
    private Path getEmptyGBK() {
        return getPathForResource("cs321/create/empty.gbk");
    }

    /**
     * Gets the {@link Path} for the emptyOrigin.gbk file.
     *
     * @return  The path for the emptyOrigin.gbk file.
     */
    private Path getEmptyOriginGBK() {
        return getPathForResource("cs321/create/emptyOrigin.gbk");
    }

    /**
     * Gets the {@link Path} for the startsN.gbk file.
     *
     * @return The path for the startsN.gbk file.
     */
    private Path getStartsN() {
        return getPathForResource("cs321/create/startsN.gbk");
    }

    /**
     * Gets the {@link Path} for the endsN.gbk file.
     *
     * @return The path for the endsN.gbk file.
     */
    private Path getEndsN() {
        return getPathForResource("cs321/create/endsN.gbk");
    }

}
