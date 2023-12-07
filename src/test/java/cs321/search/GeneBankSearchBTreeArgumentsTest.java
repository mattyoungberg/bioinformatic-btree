package cs321.search;

import cs321.create.GeneBankCreateBTreeArguments;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link GeneBankSearchBTreeArguments}.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchBTreeArgumentsTest {

    /**
     * Query file name for testing.
     */
    private String queryFileName = "queryfile.gbk";

    /**
     * BTree file name for testing.
     */
    private String btreeFileName = "btreefile.gbk";

    /**
     * Creates the test files before all tests are run.
     */
    @BeforeClass
    public static void beforeAll() {
        File file = new File("queryfile.gbk");  // Literal because of @BeforeClass
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating test file");
        }

        file = new File("btreefile.gbk");  // Literal because of @BeforeClass
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating test file");
        }
    }

    /**
     * Deletes the test files after all tests are run.
     */
    @AfterClass
    public static void afterAll() {
        File file = new File("queryfile.gbk");  // Literal because of @AfterClass
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }

        file = new File("btreefile.gbk");  // Literal because of @AfterClass
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }
    }

    /**
     * Tests the constructor.
     */
    @Test
    public void testConstructor() {
        // Should succeed
        new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
    }

    /**
     * Tests the equals method when two objects are equal.
     */
    @Test
    public void testEqualsTrue() {
        GeneBankSearchBTreeArguments args1 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        GeneBankSearchBTreeArguments args2 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args1.equals(args2));
    }

    /**
     * Tests the equals method when two objects are not equal.
     */
    @Test
    public void testEqualsFalse() {
        GeneBankSearchBTreeArguments args1 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        GeneBankSearchBTreeArguments args2 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 1);  // Different debug
        assert(!args1.equals(args2));
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#useCache()} returns the correct value.
     */
    @Test
    public void testUseCache() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.useCache());
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getDegree()} returns the correct value.
     */
    @Test
    public void testGetDegree() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getDegree() == 2);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getBtreeFileName()} returns the correct value.
     */
    @Test
    public void testGetBTreeFileName() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getBtreeFileName().equals(btreeFileName));
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getSubsequenceLength()} returns the correct value.
     */
    @Test
    public void getSubsequenceLength() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getSubsequenceLength() == 3);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getQueryFileName()} returns the correct value.
     */
    @Test
    public void getQueryFileName() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getQueryFileName().equals(queryFileName));
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getCacheSize()} returns the correct value.
     */
    @Test
    public void testGetCacheSize() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getCacheSize() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#getDebugLevel()} returns the correct value.
     */
    @Test
    public void testGetDebugLevel() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments.
     */
    @Test
    public void testFromStringArgsHappyPath() {
        String[] args = {"--cache=1", "--degree=2", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments arguments = GeneBankSearchBTreeArguments.fromStringArgs(args);
        assert(arguments.useCache());
        assert(arguments.getDegree() == 2);
        assert(arguments.getBtreeFileName().equals("btreefile.gbk"));
        assert(arguments.getSubsequenceLength() == 3);
        assert(arguments.getQueryFileName().equals("queryfile.gbk"));
        assert(arguments.getCacheSize() == 100);
        assert(arguments.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments without optional arguments.
     */
    @Test
    public void testFromStringWithoutOptionalsHappyPath() {
        String[] args = {"--cache=0", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk"};
        GeneBankSearchBTreeArguments arguments = GeneBankSearchBTreeArguments.fromStringArgs(args);
        assert(!arguments.useCache());
        assert(arguments.getDegree() == 0);
        assert(arguments.getBtreeFileName().equals("btreefile.gbk"));
        assert(arguments.getSubsequenceLength() == 3);
        assert(arguments.getQueryFileName().equals("queryfile.gbk"));
        assert(arguments.getCacheSize() == 0);
        assert(arguments.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments and a degree of 0.
     */
    @Test
    public void testFromStringArgsAllows0Degree() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments arguments = GeneBankSearchBTreeArguments.fromStringArgs(args);
        assert(arguments.useCache());
        assert(arguments.getDegree() == 0);
        assert(arguments.getBtreeFileName().equals("btreefile.gbk"));
        assert(arguments.getSubsequenceLength() == 3);
        assert(arguments.getQueryFileName().equals("queryfile.gbk"));
        assert(arguments.getCacheSize() == 100);
        assert(arguments.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * the arguments are given in a random order.
     */
    @Test
    public void testFromStringArgsRandomOrder() {
        String[] args = {"--degree=0", "--cachesize=100", "--cache=1", "--length=3", "--btreefile=btreefile.gbk", "--queryfile=queryfile.gbk", "--debug=0"};
        GeneBankSearchBTreeArguments arguments = GeneBankSearchBTreeArguments.fromStringArgs(args);
        assert(arguments.useCache());
        assert(arguments.getDegree() == 0);
        assert(arguments.getBtreeFileName().equals("btreefile.gbk"));
        assert(arguments.getSubsequenceLength() == 3);
        assert(arguments.getQueryFileName().equals("queryfile.gbk"));
        assert(arguments.getCacheSize() == 100);
        assert(arguments.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given arguments are in a bad
     * format.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--cache", "1", "--degree", "0", "--btreefile", "btreefile.gbk", "--length", "3", "--queryfile", "queryfile.gbk", "--cachesize", "100", "--debug", "0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given an unknown argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownArgument() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0", "--unknown=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given a string with missing
     * arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--cachesize=100", "--debug=0"};  // missing queryfile
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given a bad value for the
     * cache argument
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheValue() {
        String[] args = {"--cache=2", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when not given a value for the
     * cache size but a cache was specified.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNotSpecifiedWhenCacheRequested() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the cachesize is negative
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNegative() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=-1", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the degree is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDegreeValue() {
        String[] args = {"--cache=1", "--degree=-1", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the degree is negative
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDegree() {
        String[] args = {"--cache=1", "--degree=-1", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when an invalid BTree file is
     * given.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentBTreeFile() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=nonexistent.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given an invalid length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthOver31() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=32", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when given an invalid length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadeLengthUnder1() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=0", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when an invalid query file is
     * given.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentQueryFile() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=nonexistent.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the cache size is under 100.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeUnder100() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=99", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the cache size is over
     * 10000.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeOver10000() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=10001", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the debug level is under 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugUnder0() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=-1"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchBTreeArguments#fromStringArgs(String[])} throws when the debug level is over 1.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugOver1() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=2"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }
}
