package cs321.create;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class GeneBankCreateBTreeArgumentsTest {

    String testFileName = "testfile.gbk";

    @BeforeClass
    public static void beforeAll() {
        File file = new File("testfile.gbk");  // Literal because of @BeforeClass
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating test file");
        }
    }

    @AfterClass
    public static void afterAll() {
        File file = new File("testfile.gbk");  // Literal because of @AfterClass
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }
    }

    @Test
    public void testConstructor() {
        // Should succeed
        new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
    }

    @Test
    public void testEqualsTrue() {
        GeneBankCreateBTreeArguments args1 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        GeneBankCreateBTreeArguments args2 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args1.equals(args2));
    }

    @Test
    public void testEqualsFalse() {
        GeneBankCreateBTreeArguments args1 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        GeneBankCreateBTreeArguments args2 = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 1);  // Different debug
        assert(!args1.equals(args2));
    }

    @Test
    public void testGetUseCache() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getUseCache());
    }

    @Test
    public void testGetDegree() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getDegree() == 2);
    }

    @Test
    public void testGetGbkFileName() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getGbkFileName().equals(testFileName));
    }

    @Test
    public void testGetSubsequenceLength() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getSubsequenceLength() == 3);
    }

    @Test
    public void testGetCacheSize() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getCacheSize() == 0);
    }

    @Test
    public void testGetDebugLevel() {
        GeneBankCreateBTreeArguments args = new GeneBankCreateBTreeArguments(true, 2, testFileName, 3, 0, 0);
        assert(args.getDebugLevel() == 0);
    }

    @Test
    public void testFromStringArgsHappyPath() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(gbkArgs.getUseCache());
        assert(gbkArgs.getDegree() == 2);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 100);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    @Test
    public void testFromStringWithoutOptionalsHappyPath() {
        String[] args = {"--cache=0", "--degree=0", "--gbkfile=testfile.gbk", "--length=3"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(!gbkArgs.getUseCache());
        assert(gbkArgs.getDegree() == 0);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 0);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    @Test
    public void testFromStringArgsAllows0Degree() {
        String[] args = {"--cache=1", "--degree=0", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0"};
        GeneBankCreateBTreeArguments gbkArgs = GeneBankCreateBTreeArguments.fromStringArgs(args);
        assert(gbkArgs.getUseCache());
        assert(gbkArgs.getDegree() == 0);
        assert(gbkArgs.getGbkFileName().equals(testFileName));
        assert(gbkArgs.getSubsequenceLength() == 3);
        assert(gbkArgs.getCacheSize() == 100);
        assert(gbkArgs.getDebugLevel() == 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--cache", "1", "--degree", "2", "--gbkfile", "testfile.gbk", "--length", "3", "--cachesize", "100", "--debug", "0"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testsUnknownArgument() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100", "--debug=0", "--unknown=1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--cache=0", "--degree=2", "--length=3"};  // missing gbkfile
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheValue() {
        String[] args = {"--cache=2", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=100"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNotSpecifiedWhenCacheRequested() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3"};  // missing cachesize
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNegative() {
        String[] args = {"--cache=-1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=-1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDegree() {
        String[] args = {"--cache=0", "--degree=1", "--gbkfile=testfile.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDegree() {
        String[] args = {"--cache=0", "--degree=-1", "--gbkfile=testfile.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentFile() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=nonexistent.gbk", "--length=3"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthOver31() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=32"};  // length > 31
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthUnder1() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=0"};  // length < 1
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeUnder100() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=99"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeOver10000() {
        String[] args = {"--cache=1", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--cachesize=10001"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugUnder0() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--debug=-1"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugOver1() {
        String[] args = {"--cache=0", "--degree=2", "--gbkfile=testfile.gbk", "--length=3", "--debug=2"};
        // Should throw an exception
        GeneBankCreateBTreeArguments.fromStringArgs(args);
    }
}
