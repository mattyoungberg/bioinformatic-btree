package cs321.create;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import cs321.btree.BTree;
import cs321.btree.BTreeException;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;

/**
 * A program that creates a BTree file from a gbk file.
 * <p>
 * Run `java -jar build/libs/GeneBankCreateBTree.jar --help` for usage information.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankCreateBTree {

    /**
     * The entry point for the GeneBankCreateBTree program.
     * <p>
     * This method will parse the command line arguments and then create a BTree file based on the provided arguments.
     *
     * @param args              command line arguments provided with program call
     * @throws BTreeException   if there is an error creating the BTree
     * @throws IOException      if there is an error reading the gbk file or writing the BTree file
     * @throws SQLException     if there is an error creating/writing to a SQLite database
     */
    public static void main(String[] args) throws BTreeException, IOException, SQLException {
        // Print usage if requested
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printUsageAndExit("", 0);
        }

        // Parse arguments
        GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = parseArgumentsAndHandleExceptions(args);

        // Run program
        createBTree(geneBankCreateBTreeArguments);
    }

    /**
     * This method takes in the command line arguments and will parse the arguments to return an object containing the
     * appropriate arguments or print a usage method and exit the program
     *
     * @param args  command line arguments provided with program call
     * @return      validated {@link GeneBankCreateBTreeArguments} object
     */
    private static GeneBankCreateBTreeArguments parseArgumentsAndHandleExceptions(String[] args) {
        GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = null;
        try {
            geneBankCreateBTreeArguments = parseArguments(args);
        } catch (ParseArgumentException e) {
            printUsageAndExit(e.getMessage(), 1);
        }
        return geneBankCreateBTreeArguments;
    }

    /**
     * This method accepts the command line arguments, args, and attempts to parse the arguments on position first and,
     * if not able, will attempt to parse arguments by name.
     * <p>
     * Invalid argument entries will throw a ParseArgumentException to be handled in generating error message.
     *
     * @param args                      command line arguments provided by user.
     * @throws ParseArgumentException   exception issue encountered parsing arguments
     * @return                          validated {@link GeneBankCreateBTreeArguments} object
     */
    public static GeneBankCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException {
        try {
            return GeneBankCreateBTreeArguments.fromStringArgs(args);
        } catch (IllegalArgumentException e) {
            throw new ParseArgumentException(e.getMessage());
        }
    }

    /**
     * This method prints when invalid arguments are detected and will print an appropriately formatted usage example
     * and exit the program.
     *
     * @param errorMessage error message provided by parse argument exception
     * @param exitCode     exit code to be used when exiting the program
     */
    private static void printUsageAndExit(String errorMessage, int exitCode) {
        System.err.println(errorMessage);
        System.err.println("Usage: java -jar build/libs/GeneBankCreateBTree.jar --cache=<0|1>  --degree=<btree-degree> ");
        System.err.println("\t--gbkfile=<gbk-file> --length=<sequence-length> [--cachesize=<n>] [--debug=0|1]");
        System.exit(exitCode);
    }

    /**
     * This method will take in the command line arguments and, using a new GeneBankSubsequenceIterator, read through
     * the provided gbkfile. It will create a new BTree file similarly named to the provided gbkfile. After converting
     * the subsequences to tree objects they will be inserted into the BTree. Exceptions thrown while inserting into
     * BTree will print the message and exit the program with a nonzero status.
     * <p>
     * Debug level 0 prints a sum of all subsequences added to the BTree, frequency increases of duplicates will be
     * considered a successful addition.
     * <p>
     * Debug level 1 uses the BTreeDumpToFile to output the created tree to a dump file, dump is appended to the
     * filename to designate.
     *
     * @param args              command line arguments provided with program call
     * @throws BTreeException   if there is an error creating the BTree
     * @throws IOException      if there is an error reading the gbk file or writing the BTree file
     * @throws SQLException     if there is an error creating/writing to a SQLite database
     */
    private static void createBTree(GeneBankCreateBTreeArguments args) throws BTreeException, IOException, SQLException {
        TreeObject.subsequenceLength = args.getSubsequenceLength();

        // Get the base file name of the gbk file for persisting BTree file to project root directory
        String gbkFilePathString = args.getGbkFileName();
        Path gbkFilePath = Paths.get(gbkFilePathString);
        String gbkFileBaseName = gbkFilePath.getFileName().toString(); // Extracts just the base name
        String btreeFileName = gbkFileBaseName + ".btree.data." + args.getSubsequenceLength() + "." + args.getDegree();

        System.err.println();
        System.err.println("Creating BTree from file \"" + gbkFilePathString + "\"...");
        BTree bTree;
        if (args.useCache()) {
            bTree = new BTree(args.getDegree(), btreeFileName, args.getCacheSize());
        } else {
            bTree = new BTree(args.getDegree(), btreeFileName);
        }
        int subsequencesInserted = 0;

        String subsequenceString;
        long subsequenceEncoded;

        try (GeneBankSubsequenceIterator subsequenceIterator = new GeneBankSubsequenceIterator(gbkFilePath, args.getSubsequenceLength())) {
            while (subsequenceIterator.hasNext()) {
                subsequenceString = subsequenceIterator.next();
                subsequenceEncoded = SequenceUtils.dnaStringToLong(subsequenceString);
                TreeObject tempTreeObject = new TreeObject(subsequenceEncoded);
                bTree.insert(tempTreeObject);
                subsequencesInserted++;
            }
        }

        System.err.println("BTree created successfully.");
        System.err.println();
        System.err.println("Summary:");
        System.err.println("-------");
        System.err.printf("%-30s%,10d%n", "Subsequences inserted:", subsequencesInserted);
        System.err.printf("%-30s%,10d%n", "Subsequence length:", args.getSubsequenceLength());
        System.err.printf("%-30s%,10d%n", "BTree degree:", bTree.getDegree());
        System.err.printf("%-30s%,10d%n", "Total number of objects:", bTree.getSize());
        System.err.printf("%-30s%,10d%n", "Total number of nodes:", bTree.getNumberOfNodes());
        System.err.printf("%-30s%,10d%n", "Height of final tree:", bTree.getHeight());
        if (args.useCache()) {
            System.err.printf("%-30s%,9.2f%%%n", "Cache hit rate:", bTree.getCacheHitRate() * 100);
        }
        System.err.println();

        if (args.getDebugLevel() == 1) {
            System.err.printf("Dumping BTree contents to file...%n");
            PrintWriter printWriter = new PrintWriter("dump");  // This is what `create-btrees.sh` expects
            bTree.dumpToFile(printWriter);
            printWriter.close();
            System.err.println("Done.");
            System.err.println();
        }

        System.err.println("Finalizing BTree on disk and creating SQLite database...");
        bTree.finishUp();
        System.err.println("Done.");
        System.err.println();
    }
}
