package cs321.create;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GeneBankSubsequenceIteratorTest {

    @Test
    public void canConstruct() throws IOException {
        Path path = getCondensedTest0GBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithNegativeK() throws IOException {
        Path path = getCondensedTest0GBK();
        new GeneBankSubsequenceIterator(path, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithEmpty() throws IOException {
        Path path = getEmptyGBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    //////////////////////////////////////////////////////////////////////////
    // MAIN TESTS: These are testing a condensed version of the test0.gbk file
    //////////////////////////////////////////////////////////////////////////

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

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructWithEmptyOrigin() throws IOException {
        Path path = getEmptyOriginGBK();
        new GeneBankSubsequenceIterator(path, 3);
    }

    @Test
    public void canHandleSubsequenceStartingWithN() throws IOException {
        Path path = getStartsN();
        GeneBankSubsequenceIterator iterator = new GeneBankSubsequenceIterator(path, 3);
        assertTrue(iterator.hasNext());
        assertEquals("aaa", iterator.next());
        assertFalse(iterator.hasNext());
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void subsequenceRequiredIsBiggerThanInFile() throws IOException {
        Path path = getEndsN();
        new GeneBankSubsequenceIterator(path, 100);
    }

    //////////////////////////////////
    // Path getters for resource files
    //////////////////////////////////

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


    private Path getCondensedTest0GBK() {
        return getPathForResource("cs321/create/condensed_test0.gbk");
    }

    private Path getCondensedCapitalizedTest0GBK() {
        return getPathForResource("cs321/create/condensedCapitalized_test0.gbk");
    }

    private Path getEmptyGBK() {
        return getPathForResource("cs321/create/empty.gbk");
    }

    private Path getEmptyOriginGBK() {
        return getPathForResource("cs321/create/emptyOrigin.gbk");
    }

    private Path getStartsN() {
        return getPathForResource("cs321/create/startsN.gbk");
    }

    private Path getEndsN() {
        return getPathForResource("cs321/create/endsN.gbk");
    }

}
