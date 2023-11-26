package cs321.create;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;




public class GeneBankCreateBTree
{

	public static void main(String[] args) throws Exception
	{
		//System.out.println("Hello world from cs321.create.GeneBankCreateBTree.main");
		GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = parseArgumentsAndHandleExceptions(args);
		createBTree(geneBankCreateBTreeArguments);

	}

	/**
	 * This method takes in the command line arguments and will parse the arguments to return an object containing the appropriate arguments or print a usage method and exit the program
	 *  
	 * @param args - command line arguments provided with program call. 
	 * @return - validated arguments object
	 */
	private static GeneBankCreateBTreeArguments parseArgumentsAndHandleExceptions(String[] args)
	{
		GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = null;
		try
		{
			geneBankCreateBTreeArguments = parseArguments(args);
		}
		catch (ParseArgumentException e)
		{
			printUsageAndExit(e.getMessage());
		}
		return geneBankCreateBTreeArguments;
	}

	/**
	 * This method prints when invalid arguments are detected and will print an appropriately formatted usage example and exit the program with a nonzero status. 
	 *  
	 * @param errorMessage - error message provided by parse argument exception 
	 */
	private static void printUsageAndExit(String errorMessage)
	{
		System.out.println(errorMessage);
		System.out.println("Usage: java -jar build/libs/GeneBankCreateBTree.jar --cache=<0|1>  --degree=<btree-degree> ");
		System.out.println("\t--gbkfile=<gbk-file> --length=<sequence-length> [--cachesize=<n>] [--debug=0|1]");
		System.exit(1);
	}

	/**
	 * This method accepts the command line arguments, args, and attempts to parse the arguments on position first and if not able will attempt to parse arguments by name. 
	 * invalid argument entries will throw a ParseArgumentException to be handled in generating error message
	 * @param args- command line arguments provided by user.
	 * @return - validate GeneBankCreateBTreeArguments object
	 * @throws ParseArgumentException - exception issue encounter parsing arguments
	 */
	public static GeneBankCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException {
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

	/**
	 * This method parses the command line arguments passed based on the expected order input base on usage example. 
	 * Invalid arguments will generate an error message to describe the encountered issue.  
	 * 
	 * @param args - command line arguments entered by user
	 * @return - validated GeneBankCreateBTreeArguments object
	 * @throws ParseArgumentException - exception with helpful description of encountered issue
	 */
	public static GeneBankCreateBTreeArguments parsePositionalArgs(String[] args) throws ParseArgumentException {
		if (args.length < 4) {
			throw new ParseArgumentException("Insufficient arguments. Required: cache degree gbkfile length");
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

			// Don't validate 'gbkfile' here; given tests can't handle it.
			// It will be validated when it is opened
			String gbkfileValue = args[2];

			// Validate and parse 'length'
			int lengthValue = Integer.parseInt(args[3]);
			if (lengthValue < 1 || lengthValue > 31) {
				throw new IllegalArgumentException("Sequence length must be between 1 and 31");
			}

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

			return new GeneBankCreateBTreeArguments(
					cacheValue == 1,
					degreeValue,
					gbkfileValue,
					lengthValue,
					cacheSize,
					debugLevel
					);

		} catch (NumberFormatException e) {
			throw new ParseArgumentException("Invalid number format: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ParseArgumentException("Argument validation error: " + e.getMessage());
		}
	}

	/**
	 * This method utilizes the apache commons cli package to generate options for each command line argument available for the program and uses the 
	 * available methods to create and validate each command line argument as a separate Option. 
	 * @param args - command line arguments provided in program call
	 * @return - validated GeneBankCreateBTreeArguments object
	 * @throws ParseArgumentException- exception with helpful description of encountered issue
	 */
	public static GeneBankCreateBTreeArguments parseNamedArgs(String[] args) throws ParseArgumentException {
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

		// gbkfile
		Option gbkfile = new Option("gbkfile", true, "Path to the input .gbk file");
		gbkfile.setRequired(true);
		options.addOption(gbkfile);

		// length
		Option length = new Option("length", true, "Sequence length (between 1 and 31)");
		length.setType(Integer.class);
		length.setRequired(true);
		options.addOption(length);

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
				throw new IllegalArgumentException("Degree must be between 0 and " + 31);
			}
			//changed to not allow degree 1 to exist as degree 0 is only available to allow the program to select an optimal degree avoids exception from BTree. 

			// Don't validate 'gbkfile' here; given tests can't handle it.
			// It will be validated when it is opened
			String gbkfileValue = cmd.getOptionValue("gbkfile");

			// Validate 'length'
			int lengthValue = Integer.parseInt(cmd.getOptionValue("length"));
			if (lengthValue < 1 || lengthValue > 31) {
				throw new IllegalArgumentException("Sequence length must be between 1 and 31");
			}

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

		return new GeneBankCreateBTreeArguments(
				"1".equals(cmd.getOptionValue("cache")),
				Integer.parseInt(cmd.getOptionValue("degree")),
				cmd.getOptionValue("gbkfile"),
				Integer.parseInt(cmd.getOptionValue("length")),
				cmd.hasOption("cachesize") ? Integer.parseInt(cmd.getOptionValue("cachesize")) : 0,
						cmd.hasOption("debug") ? Integer.parseInt(cmd.getOptionValue("debug")) : 0
				);
	}

	/**
	 * This method will take in the command line arguments and, using a new GeneBankSubsequenceIterator,
	 * read through the provided gbkfile. It will also create a new BTree file similarly named to the provided gbkfile with an appended BTreeFile. 
	 * After converting the subsequences to tree objects they will be inserted into the BTree. Exceptions thrown while inserting into BTree will 
	 * print the message and exit the program with a nonzero status. 
	 * debug level 0 prints a sum of all subsequences added to the BTree, frequency increases of duplicates will be considered a successful addition. 
	 * debug level 1 uses the BTreeDumpToFile to output the created tree to a dump file, dump is appended to the filename to designate. 
	 * 
	 * @param args Appropriately validated command line arguments
	 * @throws Exception 
	 */
	private static void createBTree (GeneBankCreateBTreeArguments args) throws Exception {


		try {
			Path filePath = Paths.get(args.getGbkFileName());
			try (GeneBankSubsequenceIterator gbkSubseqIterator = new GeneBankSubsequenceIterator(filePath, args.getSubsequenceLength())) {

				String btreeFileName = args.getGbkFileName()+ "btree." + args.getSubsequenceLength() + "." + args.getDegree();
				BTree bTree = new BTree(args.getDegree(), btreeFileName);

				int subsequencesInserted = 0;

				while (gbkSubseqIterator.hasNext()) {
					String tempSubsequenceString = gbkSubseqIterator.next();
					long tempSubsequenceLong = SequenceUtils.dnaStringToLong(tempSubsequenceString);
					TreeObject tempTreeObject = new TreeObject(tempSubsequenceLong);
					tempTreeObject.subsequenceLength = args.getSubsequenceLength();

					bTree.insert(tempTreeObject);
					subsequencesInserted++;
				}

				bTree.finishUp();

				if (args.getDebugLevel() >= 0) {
					System.out.println("Successfully inserted " + subsequencesInserted + " subsequences!");
				}

				if (args.getDebugLevel() >= 1) {
					PrintWriter printWriter = new PrintWriter(args.getGbkFileName() + ".dump." + args.getSubsequenceLength());
					bTree.dumpToFile(printWriter);
				}
			} 
		} finally { 

		}

	}
}
