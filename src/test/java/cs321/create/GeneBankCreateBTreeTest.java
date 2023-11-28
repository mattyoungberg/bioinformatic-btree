package cs321.create;

import cs321.common.ParseArgumentException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GeneBankCreateBTreeTest
{
    private String[] args;
    private GeneBankCreateBTreeArguments expectedConfiguration;
    private GeneBankCreateBTreeArguments actualConfiguration;

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

    @AfterClass
    public static void tearDownClass() {
        File testFile = new File("fileNameGbk.gbk");
        if (testFile.exists() && testFile.isFile()) {
            testFile.delete();
        }
    }

    @Test
    public void parse4CorrectArgumentsTest() throws ParseArgumentException
    {
        // Changed per Piazza question
        args = new String[4];
        args[0] = "--cache=0";
        args[1] = "--degree=20";
        args[2] = "--gbkfile=fileNameGbk.gbk";
        args[3] = "--length=13";

        expectedConfiguration = new GeneBankCreateBTreeArguments(false, 20, "fileNameGbk.gbk", 13, 0, 0);
        actualConfiguration = GeneBankCreateBTree.parseArguments(args);
        assertEquals(expectedConfiguration, actualConfiguration);
    }

}
