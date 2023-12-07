package cs321.search;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests for {@link GeneBankSearchDatabaseArguments}.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchDatabaseArgumentsTest {

    /**
     * DB file name for testing.
     */
    private String dbFileName = "fake.db";

    /**
     * Query file name for testing.
     */
    private String queryFileName = "queryFake";

    /**
     * {@link Path} of the DB file for testing.
     */
    Path dbPath = Paths.get(dbFileName);

    /**
     * Creates the fake DB file and query file for testing.
     */
    @BeforeClass
    public static void beforeAll() {
        File file = new File("fake.db");
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating fake.db");
        }

        file = new File("queryFake");
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating queryFake");
        }
    }

    /**
     * Deletes the fake DB file and query file for testing.
     */
    @AfterClass
    public static void afterAll() {
        File file = new File("fake.db");
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }

        file = new File("queryFake");
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }
    }

    /**
     * Tests the constructor.
     */
    @Test
    public void testConstructor() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(dbFileName, args.getDatabasePath().toString());
        assertEquals(queryFileName, args.getQueryFileName());
    }

    /**
     * Tests the equals method when two objects are equal.
     */
    @Test
    public void testEqualsTrue() {
        GeneBankSearchDatabaseArguments args1 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        GeneBankSearchDatabaseArguments args2 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(args1, args2);
    }

    /**
     * Tests the equals method when two objects are not equal.
     */
    @Test
    public void testEqualsFalse() {
        GeneBankSearchDatabaseArguments args1 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        GeneBankSearchDatabaseArguments args2 = new GeneBankSearchDatabaseArguments(Paths.get("fakeish.db"), queryFileName);  // different db file name
        assertNotEquals(args1, args2);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#getDatabasePath()} returns the correct {@link Path}.
     */
    @Test
    public void testGetDatabasePath() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(Paths.get(dbFileName), queryFileName);
        assertEquals(args.getDatabasePath(), dbPath);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#getQueryFileName()} returns the correct query file name.
     */
    @Test
    public void testGetQueryFileName() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(args.getQueryFileName(), queryFileName);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} returns the correct value when given
     * the correct arguments.
     */
    @Test
    public void testFromStringArgsHappyPath() {
        String[] args = {"--database=fake.db", "--queryfile=queryFake"};
        GeneBankSearchDatabaseArguments arguments = GeneBankSearchDatabaseArguments.fromStringArgs(args);
        assertEquals(arguments.getDatabasePath().toString(), dbFileName);
        assertEquals(arguments.getQueryFileName(), queryFileName);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} returns the correct value when given
     * the arguments are given in a random order.
     */
    @Test
    public void testFromStringArgsRandomOrder() {
        String[] args = {"--queryfile=queryFake", "--database=fake.db"};
        GeneBankSearchDatabaseArguments arguments = GeneBankSearchDatabaseArguments.fromStringArgs(args);
        assertEquals(arguments.getDatabasePath().toString(), dbFileName);
        assertEquals(arguments.getQueryFileName(), queryFileName);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} throws when given arguments in a bad
     * format.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--queryfile", "queryFake", "--database", "fake.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} throws when given an unknown
     * argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownArgument() {
        String[] args = {"--queryfile=queryFake", "--database=fake.db", "--unknown=unknown"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} throws when missing an argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--queryfile=queryFake"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} throws when given a bad db file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentDB() {
        String[] args = {"--queryfile=queryFake", "--database=nonexistent.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    /**
     * Tests that {@link GeneBankSearchDatabaseArguments#fromStringArgs(String[])} throws when given a bad query file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentQueryFile() {
        String[] args = {"--queryfile=nonexistent", "--database=fake.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }
}
