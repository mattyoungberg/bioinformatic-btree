package cs321.search;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class GeneBankSearchBTreeArgumentsTest {

    String queryFileName = "queryfile.gbk";
    String btreeFileName = "btreefile.gbk";

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

    @Test
    public void testConstructor() {
        // Should succeed
        new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
    }

    @Test
    public void testEqualsTrue() {
        GeneBankSearchBTreeArguments args1 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        GeneBankSearchBTreeArguments args2 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args1.equals(args2));
    }

    @Test
    public void testEqualsFalse() {
        GeneBankSearchBTreeArguments args1 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        GeneBankSearchBTreeArguments args2 = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 1);  // Different debug
        assert(!args1.equals(args2));
    }

    @Test
    public void testUseCache() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.useCache());
    }

    @Test
    public void testGetDegree() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getDegree() == 2);
    }

    @Test
    public void testGetBTreeFileName() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getBtreeFileName().equals(btreeFileName));
    }

    @Test
    public void getSubsequenceLength() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getSubsequenceLength() == 3);
    }

    @Test
    public void getQueryFileName() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getQueryFileName().equals(queryFileName));
    }

    @Test
    public void testGetCacheSize() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getCacheSize() == 0);
    }

    @Test
    public void testGetDebugLevel() {
        GeneBankSearchBTreeArguments args = new GeneBankSearchBTreeArguments(true, 2, btreeFileName, 3, queryFileName, 0, 0);
        assert(args.getDebugLevel() == 0);
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--cache", "1", "--degree", "0", "--btreefile", "btreefile.gbk", "--length", "3", "--queryfile", "queryfile.gbk", "--cachesize", "100", "--debug", "0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownArgument() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0", "--unknown=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--cachesize=100", "--debug=0"};  // missing queryfile
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheValue() {
        String[] args = {"--cache=2", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNotSpecifiedWhenCacheRequested() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheSizeNegative() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=-1", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDegreeValue() {
        String[] args = {"--cache=1", "--degree=-1", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDegree() {
        String[] args = {"--cache=1", "--degree=-1", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentBTreeFile() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=nonexistent.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadLengthOver31() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=32", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadeLengthUnder1() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=0", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentQueryFile() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=nonexistent.gbk", "--cachesize=100", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeUnder100() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=99", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadCacheSizeOver10000() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=10001", "--debug=0"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugUnder0() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=-1"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDebugOver1() {
        String[] args = {"--cache=1", "--degree=0", "--btreefile=btreefile.gbk", "--length=3", "--queryfile=queryfile.gbk", "--cachesize=100", "--debug=2"};
        GeneBankSearchBTreeArguments.fromStringArgs(args);
    }
}
