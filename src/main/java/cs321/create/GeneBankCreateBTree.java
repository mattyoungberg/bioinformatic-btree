package cs321.create;

import java.io.*;
import java.util.List;
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

    private static void printUsageAndExit(String errorMessage)
    {
        System.out.println("java -jar build/libs/GeneBankCreateBTree.jar --cache=<0|1>  --degree=<btree-degree> ");
        System.out.println("\t--gbkfile=<gbk-file> --length=<sequence-length> [--cachesize=<n>] [--debug=0|1]");
        System.exit(1);
    }

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
            if (degreeValue < 2 || degreeValue > 31 || degreeValue == 0) {
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
            if (degreeValue < 0 || degreeValue > 31) {
                throw new IllegalArgumentException("Degree must be between 0 and " + 31);
            }

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
    
    private static void createBTree (GeneBankCreateBTreeArguments args) {
    	try {
    		Path filePath = Paths.get(args.getGbkFileName());
    		GeneBankSubsequenceIterator gbkSubseqIterator = new GeneBankSubsequenceIterator(filePath, args.getSubsequenceLength());

    		String btreeFileName = args.getGbkFileName().substring(0, args.getGbkFileName().indexOf(".")) + "BTreeFile";
    		BTree bTree = new BTree(args.getDegree(), btreeFileName);

    		int subsequencesInserted = 0;

    		while (gbkSubseqIterator.hasNext()) {
    			String tempSubsequenceString = gbkSubseqIterator.next();
    			long tempSubsequenceLong = SequenceUtils.dnaStringToLong(tempSubsequenceString);
    			TreeObject tempTreeObject = new TreeObject(tempSubsequenceLong);

    			try {
    				bTree.insert(tempTreeObject);
    				subsequencesInserted++;
    			} catch (IOException e) {
    				System.out.println(e.getMessage());
    				e.printStackTrace();
    				System.exit(1);
    			}
    		}

    		bTree.finishUp();

    		if (args.getDebugLevel() >= 0) {
    			System.out.println("Successfully inserted " + subsequencesInserted + " subsequences!");
    		}

    		if (args.getDebugLevel() >= 1) {
    			PrintWriter printWriter = new PrintWriter(btreeFileName + "dump");
    			bTree.dumpToFile(printWriter);
    		}
    	} catch (Exception e) {
    		// Handle any exceptions specific to creating the B-tree from genetic data
    		System.out.println("Error creating B-tree: " + e.getMessage());
    		e.printStackTrace();
    	}
    }
}
