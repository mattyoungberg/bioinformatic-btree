package cs321.create;

import cs321.common.ParseArgumentException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link GeneBankCreateBTree} class.
 *
 * @author Bogdan Dit
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankCreateBTreeTest {

    /**
     * Creates a test file for the tests to use.
     */
    @BeforeClass
    public static void setUpClass() {
        File testFile = new File("fileNameGbk.gbk");
        if (!testFile.exists()) {
            try {
                testFile.createNewFile();
            } catch (IOException e) {
                fail("Could not create test file");
            }
        }
    }

    /**
     * Deletes the test file created by the tests.
     */
    @AfterClass
    public static void tearDownClass() {
        File testFile = new File("fileNameGbk.gbk");
        if (testFile.exists() && testFile.isFile()) {
            testFile.delete();
        }
    }

    /**
     * A given project tests that the arguments are parsed correctly.
     * <p>
     * This is much more thoroughly tested by the {@link GeneBankCreateBTreeArgumentsTest} class.
     *
     * @throws ParseArgumentException
     */
    @Test
    public void parse4CorrectArgumentsTest() throws ParseArgumentException
    {
        // Changed per Piazza question
        String[] args = new String[4];
        args[0] = "--cache=0";
        args[1] = "--degree=20";
        args[2] = "--gbkfile=fileNameGbk.gbk";
        args[3] = "--length=13";

        GeneBankCreateBTreeArguments expectedConfiguration = new GeneBankCreateBTreeArguments(false, 20, "fileNameGbk.gbk", 13, 0, 0);
        GeneBankCreateBTreeArguments actualConfiguration = GeneBankCreateBTree.parseArguments(args);
        assertEquals(expectedConfiguration, actualConfiguration);
    }

}
