package cs321.search;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import cs321.btree.BTreeException;
import cs321.common.ParseArgumentException;

public class GeneBankSearchDatabase
{

    public static void main(String[] args) throws Exception
    {
        //System.out.println("Hello world from cs321.search.GeneBankSearchDatabase.main");
        GeneBankSearchDatabaseArguments geneBankSearchDatabaseArguments = parseArgumentsAndHandleExceptions(args);
        searchDatabase(geneBankSearchDatabaseArguments);

    }
    
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

	private static void printUsageAndExit(String errorMessage) {
		
		System.out.println(errorMessage);
	    System.out.println("Usage: java -jar build/libs/GeneBankSearchDatabase.jar --database=<SQLite-database-path> ");
	    System.out.println("\t --queryfile=<query-file>");
	    System.exit(1);
	    
	}

	private static GeneBankSearchDatabaseArguments parseArguments(String[] args) throws ParseArgumentException {
		try {
            return parsePositionalArgs(args);  // try parsing as positional first; tests require it
        } catch (ParseArgumentException e) {
            // ignore; parse as named args
        }

        try {
            return parseNamedArgs(args);  // try parsing as named arguments
        } catch (ParseArgumentException e) {
            throw new ParseArgumentException(e.getMessage());  // No fallback if this doesn't work
        }
	}
	
	private static GeneBankSearchDatabaseArguments parsePositionalArgs(String[] args) throws ParseArgumentException {
		if (args.length != 2) {
            throw new ParseArgumentException("Invalid arguments. Required: SQLite-database-path queryfile");
        }

        try {
            // parse 'SQLite-database-path' and getPath using file name
            Path pathValue = FileSystems.getDefault().getPath(args[0]);
            
            
            // Don't validate 'queryfile' here; given tests can't handle it.
            // It will be validated when it is opened
            String queryFileValue = args[1];


            

            return new GeneBankSearchDatabaseArguments(
                    pathValue,
                    queryFileValue
            );

        } catch (InvalidPathException e) {
            throw new ParseArgumentException("Invalid Path " + e.getMessage());
        } 
	}

	private static GeneBankSearchDatabaseArguments parseNamedArgs(String[] args) throws ParseArgumentException {
		Options options = new Options();

        // DatabasePath
        Option databasePath = new Option("database", true, "Path to SQLite database");
        databasePath.setRequired(true);
        options.addOption(databasePath);
        
    	// queryfile
        Option queryfile = new Option("queryfile", true, "Path to the input query file");
        queryfile.setRequired(true);
        options.addOption(queryfile);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            
           
            Path databasePathValue = FileSystems.getDefault().getPath(cmd.getOptionValue("database"));

            // Don't validate 'queryfile' here; given tests can't handle it.
            // It will be validated when it is opened
            String queryfileValue = cmd.getOptionValue("queryfile");

        } catch (Exception e) {
            throw new ParseArgumentException(e.getMessage());
        }

        return new GeneBankSearchDatabaseArguments(
        		FileSystems.getDefault().getPath(cmd.getOptionValue("database")),
                cmd.getOptionValue("queryfile")
                
        );
	}

	private static void searchDatabase(GeneBankSearchDatabaseArguments args) throws SQLException, IOException {
		Connection connection = null;

		
			String databasePath = args.getDatabasePath().toString();
			//verify file exists before trying to open
			Path filePath = Paths.get(databasePath);

			// check this file exists
			if(!filePath.toFile().exists()) {
				throw new IOException("Database file does not exist or was not located");
			}
			
			try {//open SQlite connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

			//create scanner to read query file
			Scanner input = new Scanner(Paths.get(args.getQueryFileName()));
			String subsequenceString = null;

			//define query and prepared statement for repeated searches
			String query = "SELECT * FROM Sequences WHERE Encoding = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);

			//execute database searches and process results set to print output 
			while (input.hasNext()) {
				subsequenceString = input.nextLine().trim();
				pstmt.setString(1, subsequenceString);
				ResultSet results = pstmt.executeQuery();


				System.out.println("Search Results");
				printSearchResults(results);
		
			}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}

	
	}
	
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

}
