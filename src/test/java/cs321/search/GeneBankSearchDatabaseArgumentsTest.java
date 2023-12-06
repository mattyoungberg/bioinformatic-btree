package cs321.search;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class GeneBankSearchDatabaseArgumentsTest {

    String dbFileName = "fake.db";
    String queryFileName = "queryFake";

    Path dbPath = Paths.get(dbFileName);

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

    @Test
    public void testConstructor() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(dbFileName, args.getDatabasePath().toString());
        assertEquals(queryFileName, args.getQueryFileName());
    }

    @Test
    public void testEqualsTrue() {
        GeneBankSearchDatabaseArguments args1 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        GeneBankSearchDatabaseArguments args2 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(args1, args2);
    }

    @Test
    public void testEqualsFalse() {
        GeneBankSearchDatabaseArguments args1 = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        GeneBankSearchDatabaseArguments args2 = new GeneBankSearchDatabaseArguments(Paths.get("fakeish.db"), queryFileName);  // different db file name
        assertNotEquals(args1, args2);
    }

    @Test
    public void testGetDatabasePath() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(Paths.get(dbFileName), queryFileName);
        assertEquals(args.getDatabasePath(), dbPath);
    }

    @Test
    public void testGetQueryFileName() {
        GeneBankSearchDatabaseArguments args = new GeneBankSearchDatabaseArguments(dbPath, queryFileName);
        assertEquals(args.getQueryFileName(), queryFileName);
    }

    @Test
    public void testFromStringArgsHappyPath() {
        String[] args = {"--database=fake.db", "--queryfile=queryFake"};
        GeneBankSearchDatabaseArguments arguments = GeneBankSearchDatabaseArguments.fromStringArgs(args);
        assertEquals(arguments.getDatabasePath().toString(), dbFileName);
        assertEquals(arguments.getQueryFileName(), queryFileName);
    }

    @Test
    public void testFromStringArgsRandomOrder() {
        String[] args = {"--queryfile=queryFake", "--database=fake.db"};
        GeneBankSearchDatabaseArguments arguments = GeneBankSearchDatabaseArguments.fromStringArgs(args);
        assertEquals(arguments.getDatabasePath().toString(), dbFileName);
        assertEquals(arguments.getQueryFileName(), queryFileName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArgumentFormat() {
        String[] args = {"--queryfile", "queryFake", "--database", "fake.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownArgument() {
        String[] args = {"--queryfile=queryFake", "--database=fake.db", "--unknown=unknown"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArgument() {
        String[] args = {"--queryfile=queryFake"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentDB() {
        String[] args = {"--queryfile=queryFake", "--database=nonexistent.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentQueryFile() {
        String[] args = {"--queryfile=nonexistent", "--database=fake.db"};
        GeneBankSearchDatabaseArguments.fromStringArgs(args);
    }

}
