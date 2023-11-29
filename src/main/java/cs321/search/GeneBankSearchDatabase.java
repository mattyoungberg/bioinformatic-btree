package cs321.search;


import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

import cs321.common.ParseArgumentException;
import cs321.create.GeneBankCreateBTreeArguments;
import cs321.create.SequenceUtils;




/**
 * A program that searches a given SQLite database created alongside BTree.
 * <p>
 * Run `java -jar build/libs/GeneBankSearchDatabase.jar --help` for usage information.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchDatabase
{

	/**
	 * The entry point for the GeneBankSearchDatabase program
	 * 
	 * This method will parse the command line arguments and then search the provided SQLite database based on the given query file
	 * 
	 * @param args				command line arguments to database path and query file
	 * @throws SQLException		If there is an error in the SQLite connection
	 * @throws IOException		if there is an error reading the query file
	 */
	public static void main(String[] args) throws SQLException, IOException
	{
		//System.out.println("Hello world from cs321.search.GeneBankSearchDatabase.main");
		GeneBankSearchDatabaseArguments geneBankSearchDatabaseArguments = parseArgumentsAndHandleExceptions(args);
		searchDatabase(geneBankSearchDatabaseArguments);

	}

	/**
	 * This method takes in the command line arguments and will parse the arguments to return an object containing the
	 * appropriate arguments or print a usage method and exit the program
	 *
	 * @param args command line arguments provided with program call
	 * @return validated {@link GeneBankSearchDatabaseArguments} object
	 */
	private static GeneBankSearchDatabaseArguments parseArgumentsAndHandleExceptions(String[] args) {

		GeneBankSearchDatabaseArguments geneBankSearchDatabaseArguments = null;
		try
		{
			geneBankSearchDatabaseArguments = parseArguments(args);
		}
		catch (ParseArgumentException e) {
			printUsageAndExit(e.getMessage());
		}

		return geneBankSearchDatabaseArguments;
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
		System.out.println("Usage: java -jar build/libs/GeneBankSearchDatabase.jar --database=<SQLite-database-path> ");
		System.out.println("\t --queryfile=<query-file>");
		System.exit(1);

	}

	/**
     * This method accepts the command line arguments, args, and attempts to parse the arguments on position first and,
     * if not able, will attempt to parse arguments by name.
     * <p>
     * Invalid argument entries will throw a ParseArgumentException to be handled in generating error message.
     *
     * @param args command line arguments provided by user.
     * @throws ParseArgumentException exception issue encountered parsing arguments
     * @return validate {@link GeneBankSearchDatabaseArguments} object
     */ 
	private static GeneBankSearchDatabaseArguments parseArguments(String[] args) throws ParseArgumentException {
		try {
			return GeneBankSearchDatabaseArguments.fromStringArgs(args);  
		} catch (IllegalArgumentException e) {
			throw new ParseArgumentException(e.getMessage());
		}
	}	

	/**
	 * This is the driving method of the GeneBankSearchDatabase class. Using the provided arguments it connects to a SQLite database created
	 * alongside a BTree file and uses the provided Query file to print the results of the search against the provided database. 
	 * 
	 * @param args				Parsed and validated command line arguments provided by user 
	 * @throws SQLException		if an error occurred with the database connection
	 * @throws IOException		if an error occured reading the query file			
	 */
	private static void searchDatabase(GeneBankSearchDatabaseArguments args) throws SQLException, IOException {
		String databasePath = args.getDatabasePath().toString();
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath)){

			//create scanner to read query file
			Scanner input = new Scanner(Paths.get(args.getQueryFileName()));
			String subsequenceString = null;
			String subseqComplementString = null; 

			//define query and prepared statement for repeated searches
			String query = "SELECT * FROM Sequences WHERE Encoding = ? OR Encoding = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);

			//execute database searches and process results set to print output 
			while (input.hasNext()) {
				subsequenceString = input.nextLine().trim();
				subseqComplementString = getComplement(subsequenceString);
				pstmt.setString(1, subsequenceString);
				pstmt.setString(2, subseqComplementString);
				ResultSet results = pstmt.executeQuery();


				System.out.println("Search Results");
				printSearchResults(results);
			}
		}
	}

	
	/**
	 * This helper method takes in a returned result set for a database query and prints all columns of the result set to standard output
	 * 
	 * @param results			result set returned from SQLite query statement
	 * @throws SQLException		if an error occurred in the SQLite connection
	 */
	private static void printSearchResults(ResultSet results) throws SQLException {
		ResultSetMetaData resultsData = results.getMetaData();
		int numCol = resultsData.getColumnCount();
		while (results.next()) {
			for (int i = 1; i <= numCol; i++) {
				System.out.print(results.getString(i) + " ");
			}
			System.out.println();
		}
	}
	
	
	/**
	 * A helper method that takes in a String representation of a DNA subsequence and returns a String representation of its complement
	 * 
	 * @param origSubsequence	String representation of a DNA subsequence 
	 * @return					String representation of the Complement to the provided DNA Subsequence. 
	 */
	private static String getComplement(String origSubsequence) {
		int subsequenceLength = origSubsequence.length();
		long subsequenceComplement = SequenceUtils.getComplement(SequenceUtils.dnaStringToLong(origSubsequence), subsequenceLength);
		return SequenceUtils.longToDnaString(subsequenceComplement, subsequenceLength);
	}

}
