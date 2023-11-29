package cs321.search;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.lang.String;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import cs321.btree.BTree;
import cs321.btree.BTreeException;
import cs321.btree.TreeObject;

import cs321.common.ParseArgumentException;
import cs321.create.SequenceUtils;


/**
 * A program that search a BTree file from a query file.
 * <p>
 * Run `java -jar build/libs/GeneBankSearchBTree.jar --help` for usage information.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchBTree
{

	 /**
     * The entry point for the GeneBankSearchBTree program.
     * <p>
     * This method will parse the command line arguments and then search a BTree file based on the provided Query file.
     * Output will be generated based on the selected debug level.
     *
     * @param args command line arguments provided with program call
     * @throws BTreeException if there is an error creating the BTree
     * @throws IOException    if there is an error reading the gbk file or writing the BTree file
     * @throws SQLException   if there is an error creating/writing to a SQLite database
     */
    public static void main(String[] args) throws Exception
    {
        //System.out.println("Hello world from cs321.search.GeneBankSearchBTree.main");
        GeneBankSearchBTreeArguments geneBankSearchBTreearguments = parseArgumentsAndHandleExceptions(args);
        searchBTree(geneBankSearchBTreearguments);
    }

    /**
     * This method takes in the command line arguments and will parse the arguments to return an object containing the
     * appropriate arguments or print a usage method and exit the program
     *
     * @param args command line arguments provided with program call
     * @return validated {@link GeneBankSearchBTreeArguments} object
     */
	private static GeneBankSearchBTreeArguments parseArgumentsAndHandleExceptions(String[] args) {
		
		GeneBankSearchBTreeArguments geneBankSearchBTreeArguments = null;
		try
		{
			geneBankSearchBTreeArguments = parseArguments(args);
		}
		catch (ParseArgumentException e) {
			printUsageAndExit(e.getMessage());
		}
		
		return geneBankSearchBTreeArguments;
	}

	 /**
     * This method prints when invalid arguments are detected and will print an appropriately formatted usage example
     * and exit the program.
     *
     * @param errorMessage error message provided by parse argument exception
     * @param exitCode     exit code to be used when exiting the program
     */
	private static void printUsageAndExit(String errorMessage) {
		
		System.out.println(errorMessage);
	    System.out.println("Usage: java -jar build/libs/GeneBankSearchBTree.jar --cache=<0|1>  --degree=<btree-degree> ");
	    System.out.println("\t --btreefile=<b-tree-file> --length=<sequence-length> --queryfile=<query-file>");
	    System.out.println("\t [--cachesize=<n>] [--debug=0|1]");
	    System.exit(1);
	    
	}

	 /**
     * This method accepts the command line arguments, args, and attempts to parse the arguments.
     * <p>
     * Invalid argument entries will throw a ParseArgumentException to be handled in generating error message.
     *
     * @param args command line arguments provided by user.
     * @throws ParseArgumentException exception issue encountered parsing arguments
     * @return validate {@link GeneBankSearchBTreeArguments} object
     */
	private static GeneBankSearchBTreeArguments parseArguments(String[] args) throws ParseArgumentException {
		try {
            return GeneBankSearchBTreeArguments.fromStringArgs(args);  
        } catch (IllegalArgumentException e) {
           throw new ParseArgumentException(e.getMessage());
        }
	}
	
	
	/**
	 * This method will take in the command line arguments and use them to search the provided BTree for the subsequences listed in the 
	 * provided query file. 
	 * The results of the search will be output to the standard output stream. 
	 * 
	 * Debug level 0 outputs a list of subsequences and their frequency count
	 * Debug level 1 will add a separate line to declare what subsequence is being searched and if it was found or not.  
	 * 
	 * @param args				Command Line arguments provided in the program call
	 * @throws BTreeException	If there is an error locating the BTree File 
	 * @throws IOException		if there is an error encountered opening or reading the query file. 
	 */
	private static void searchBTree(GeneBankSearchBTreeArguments args) throws BTreeException, IOException {
		String btreeFileName = args.getBtreeFileName();
		Path filePath = Paths.get(btreeFileName);

		// double-check this file exists
		if(!filePath.toFile().exists()) {
			throw new BTreeException("File does not exist or was not located");
		}

		//open BTree file to begin reading BTree
		BTree searchTree = new BTree(btreeFileName);

		// open scanner to read query file in  

		Scanner input = new Scanner(new FileReader(args.getQueryFileName()));//open scanner to read query file
		String query; 
		long queryLong;
		System.out.println("Search Results: ");

		while (input.hasNext()) { // use while loop to convert query string to long and search BTree until each subsequence is searched
			query = input.nextLine().trim(); // initiate next search string
			if (args.getDebugLevel()>= 1) {
				System.out.println("Searching for subsequence: " + query);
			}
			queryLong = SequenceUtils.dnaStringToLong(query);
			TreeObject result = searchTree.search(queryLong);
			if (result != null) {
				if (args.getDebugLevel()>= 1) {
					System.out.println(query + " was found!");
				}
				System.out.println(query + " " + result.getCount());
			}else {
				if (args.getDebugLevel()>= 1) {
					System.out.println(query + " was not found!");
				}
				System.out.println(query + " 0");
			}
		}
		input.close();		

	}



}

