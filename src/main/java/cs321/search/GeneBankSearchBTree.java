package cs321.search;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.lang.String;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import cs321.btree.BTree;
import cs321.btree.BTreeException;
import cs321.btree.TreeObject;

import cs321.common.ParseArgumentException;

import cs321.create.SequenceUtils;

public class GeneBankSearchBTree
{

    public static void main(String[] args) throws Exception
    {
        System.out.println("Hello world from cs321.search.GeneBankSearchBTree.main");
        GeneBankSearchBTreeArguments geneBankSearchBTreearguments = parseArgumentsAndHandleExceptions(args);
        searchBTree(geneBankSearchBTreearguments);
    }

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

	private static void printUsageAndExit(String errorMessage) {
		
		System.out.println(errorMessage);
	    System.out.println("Usage: java -jar build/libs/GeneBankCreateBTree.jar --cache=<0|1>  --degree=<btree-degree> ");
	    System.out.println("\t --btreefile=<b-tree-file> --length=<sequence-length> --queryfile=<query-file>");
	    System.out.println("\t [--cachesize=<n>] [--debug=0|1]");
	    System.exit(1);
	    
	}

	private static GeneBankSearchBTreeArguments parseArguments(String[] args) throws ParseArgumentException {
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
	
	private static GeneBankSearchBTreeArguments parsePositionalArgs(String[] args) throws ParseArgumentException {
		if (args.length < 5) {
            throw new ParseArgumentException("Insufficient arguments. Required: cache degree btreefile length and queryfile");
        }

        try {
            // Validate and parse 'cache'
            int cacheValue = Integer.parseInt(args[0]);
            if (cacheValue < 0 || cacheValue > 1) {
                throw new IllegalArgumentException("Cache must be 0 or 1");
            }

            // Validate and parse 'degree'
            int degreeValue = Integer.parseInt(args[1]);
            if ((degreeValue < 2 || degreeValue > 31 )&& degreeValue != 0) {
                throw new IllegalArgumentException("Degree must be 0 or be between 2 and 31");
                //changed to not allow degree 1 to exist as degree 0 is only available to allow the program to select an optimal degree avoids exception from BTree. 
            }

            // Don't validate 'BTreefile' here; given tests can't handle it.
            // It will be validated when it is opened
            String btreeFileValue = args[2];

            // Validate and parse 'length'
            int lengthValue = Integer.parseInt(args[3]);
            if (lengthValue < 1 || lengthValue > 31) {
                throw new IllegalArgumentException("Sequence length must be between 1 and 31");
            }
            
         // Don't validate 'queryfile' here; given tests can't handle it.
            // It will be validated when it is opened
            String queryFileValue = args[4];


            // Validate and parse 'cachesize' if 'cache' is 1 if it is present
            int cacheSize = 0;
            if (args.length >= 5) {
                cacheSize = Integer.parseInt(args[4]);
                if (cacheValue == 1 && (cacheSize < 100 || cacheSize > 10000)) {
                    throw new IllegalArgumentException("Cache size must be between 100 and 10000");
                }
            }

            // Validate and parse 'debug' if present
            int debugLevel = 0;
            if (args.length == 6) {
                debugLevel = Integer.parseInt(args[5]);
                if (debugLevel < 0 || debugLevel > 1) {
                    throw new IllegalArgumentException("Debug level must be 0 or 1");
                }
            }

            return new GeneBankSearchBTreeArguments(
                    cacheValue == 1,
                    degreeValue,
                    btreeFileValue,
                    lengthValue,
                    queryFileValue,
                    cacheSize,
                    debugLevel
            );

        } catch (NumberFormatException e) {
            throw new ParseArgumentException("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ParseArgumentException("Argument validation error: " + e.getMessage());
        }
	}

	private static GeneBankSearchBTreeArguments parseNamedArgs(String[] args) throws ParseArgumentException {
		Options options = new Options();

        // cache
        Option cache = new Option("cache", true, "Use cache (1) or no cache (0)");
        cache.setType(Integer.class);
        cache.setRequired(true);
        options.addOption(cache);

        // degree
        Option degree = new Option("degree", true, "Degree of the B-Tree");
        degree.setType(Integer.class);
        degree.setRequired(true);
        options.addOption(degree);

        // btreefile
        Option btreefile = new Option("btreefile", true, "Path to the input btree file");
        btreefile.setRequired(true);
        options.addOption(btreefile);

        // length
        Option length = new Option("length", true, "Sequence length (between 1 and 31)");
        length.setType(Integer.class);
        length.setRequired(true);
        options.addOption(length);
        
    	// queryfile
        Option queryfile = new Option("queryfile", true, "Path to the input query file");
        queryfile.setRequired(true);
        options.addOption(queryfile);

        // cachesize (optional)
        Option cachesize = new Option("cachesize", true, "Cache size (between 100 and 10000)");
        cachesize.setType(Integer.class);
        options.addOption(cachesize);

        // debug (optional)
        Option debug = new Option("debug", true, "Debug level (0 or 1)");
        debug.setType(Integer.class);
        debug.setOptionalArg(true);
        options.addOption(debug);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            // Validate 'cache'
            int cacheValue = Integer.parseInt(cmd.getOptionValue("cache"));
            if (cacheValue < 0 || cacheValue > 1) {
                throw new IllegalArgumentException("Cache must be 0 or 1");
            }
            

            // Validate 'degree'
            int degreeValue = Integer.parseInt(cmd.getOptionValue("degree"));
            if ((degreeValue < 2 || degreeValue > 31) && degreeValue != 0) {
                throw new IllegalArgumentException("Degree must be 0 or be between 2 and 31");
            }
          //changed to not allow degree 1 to exist as degree 0 is only available to allow the program to select an optimal degree avoids exception from BTree. 

            // Don't validate 'btreefile' here; given tests can't handle it.
            // It will be validated when it is opened
            String btreefileValue = cmd.getOptionValue("btreefile");

            // Validate 'length'
            int lengthValue = Integer.parseInt(cmd.getOptionValue("length"));
            if (lengthValue < 1 || lengthValue > 31) {
                throw new IllegalArgumentException("Sequence length must be between 1 and 31");
            }
            
            // Don't validate 'queryfile' here; given tests can't handle it.
            // It will be validated when it is opened
            String queryfileValue = cmd.getOptionValue("queryfile");

            // Validate 'cachesize' if 'cache' is 1
            if ("1".equals(cmd.getOptionValue("cache"))) {
                int cacheSizeValue = Integer.parseInt(cmd.getOptionValue("cachesize"));
                if (cacheSizeValue < 100 || cacheSizeValue > 10000) {
                    throw new IllegalArgumentException("Cache size must be between 100 and 10000");
                }
            }

            // Validate 'debug' if present
            if (cmd.hasOption("debug")) {
                int debugValue = Integer.parseInt(cmd.getOptionValue("debug"));
                if (debugValue < 0 || debugValue > 1) {
                    throw new IllegalArgumentException("Debug level must be 0 or 1");
                }
            }

        } catch (Exception e) {
            throw new ParseArgumentException(e.getMessage());
        }

        return new GeneBankSearchBTreeArguments(
                "1".equals(cmd.getOptionValue("cache")),
                Integer.parseInt(cmd.getOptionValue("degree")),
                cmd.getOptionValue("btreefile"),
                Integer.parseInt(cmd.getOptionValue("length")),
                cmd.getOptionValue("queryfile"),
                cmd.hasOption("cachesize") ? Integer.parseInt(cmd.getOptionValue("cachesize")) : 0,
                cmd.hasOption("debug") ? Integer.parseInt(cmd.getOptionValue("debug")) : 0
        );
	}

	private static void searchBTree(GeneBankSearchBTreeArguments args) throws BTreeException, IOException {
		String btreeFileName = args.getBtreeFileName();
		Path filePath = Paths.get(btreeFileName);

		// check this file exists
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

