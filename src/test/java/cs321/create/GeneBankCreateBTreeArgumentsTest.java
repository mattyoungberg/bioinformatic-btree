package cs321.create;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link GeneBankCreateBTreeArguments}
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankCreateBTreeArgumentsTest {

    /**
     * The name of the test file to use for testing.
     */
    String testFileName = "testfile.gbk";

    /**
     * Creates a test file to use for testing.
     */
    @BeforeClass
    public static void beforeAll() {
        File file = new File("testfile.gbk");  // Literal because of @BeforeClass
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating test file");
        }
    }

    /**
     * Deletes the test file used for testing.
     */
    @AfterClass
    public static void afterAll() {
        File file = new File("testfile.gbk");  // Literal because of @AfterClass
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
        new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
    }

    /**
     * Tests the equals method when two objects are equal.
     */
    @Test
    public void testEqualsTrue() {
        GeneBankCreateBTreeArguments args1 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        GeneBankCreateBTreeArguments args2 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args1.equals(args2));
    }

    /**
     * Tests the equals method when two objects are not equal.
     */
    @Test
    public void testEqualsFalse() {
        GeneBankCreateBTreeArguments args1 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        GeneBankCreateBTreeArguments args2 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 1);  // Different debug
        assert(!args1.equals(args2));
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#useCache()} method returns the correct value.
     */
    @Test
    public void testUseCache() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.useCache());
    }


    /**
     * Tests that {@link GeneBankCreateBTreeArguments#getDegree()} method returns the correct value.
     */
    @Test
    public void testGetDegree() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getDegree() == 2);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#getGbkFileName()} method returns the correct value.
     */
    @Test
    public void testGetGbkFileName() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getGbkFileName().equals(testFileName));
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#getSubsequenceLength()} method returns the correct value.
     */
    @Test
    public void testGetSubsequenceLength() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getSubsequenceLength() == 3);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#getCacheSize()} method returns the correct value.
     */
    @Test
    public void testGetCacheSize() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getCacheSize() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#getDebugLevel()} method returns the correct value.
     */
    @Test
    public void testGetDebugLevel() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments.
     */
    @Test
    public void testFromStringArgsHappyPath() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(gbkArgs.useCache());
        assert(gbkArgs.getDegree() == 2);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 100);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments without optional arguments.
     */
    @Test
    public void testFromStringWithoutOptionalsHappyPath() {
        String[] args = {"--cache=0", "--degree=0", "--gbkfile=testfile.gbk", "--length=3"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(!gbkArgs.useCache());
        assert(gbkArgs.getDegree() == 0);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 0);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * given correct arguments and a degree of 0.
     */
    @Test
    public void testFromStringArgsAllows0Degree() {
        String[] args = {"--cache=1", "--degree=0", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(gbkArgs.useCache());
        assert(gbkArgs.getDegree() == 0);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 100);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} method returns the correct value when
     * the arguments are given in a random order.
     */
    @Test
    public void testFromStringArgsRandomOrder() {
        String[] args = {"--degree=0", "--cachesize=100", "--cache=1", "--length=3", "--gbkfile=testfile.gbk", "--debug=0"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(gbkArgs.useCache());
        assert(gbkArgs.getDegree() == 0);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 100);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given arguments are in a bad
     * format.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--cache", "1", "--degree", "2", "--gbkfile", "testfile.gbk", "--length", "3", "--cachesize", "100", "--debug", "0"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given an unknown argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownArgument() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0", "--unknown=1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given a string with missing
     * arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--cache=0", "--degree=2", "--length=3"};  // missing gbkfile
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given a bad value for the
     * cache argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheValue() {
        String[] args = {"--cache=2", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when not given a value for the
     * cache size but a cache was specified.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNotSpecifiedWhenCacheRequested() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3"};  // missing cachesize
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when the cachesize is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNegative() {
        String[] args = {"--cache=-1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=-1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when the degree is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDegree() {
        String[] args = {"--cache=0", "--degree=1", "--gbkfile=testfile.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when the degree is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDegree() {
        String[] args = {"--cache=0", "--degree=-1", "--gbkfile=testfile.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when an invalid file is given.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentFile() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=nonexistent.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given an invalid length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthOver31() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=32"};  // length > 31
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given an invalid length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthUnder1() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=0"};  // length < 1
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given an invalid cache size.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeUnder100() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=99"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when given an invalid cache size.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeOver10000() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=10001"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when the debug level is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugUnder0() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--debug=-1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankCreateBTreeArguments#fromStringArgs(String[])} throws when the debug level is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugOver1() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--debug=2"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }
}
