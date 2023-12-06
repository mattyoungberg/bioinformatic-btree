package cs321.search;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class to represent the arguments to the GeneBankSearchBTree program.
 * <p>
 * Create a new instance via the factory method {@link #fromStringArgs(String[])}. This will parse the string array of
 * arguments from the command line, run validation, and return a new {@link GeneBankSearchBTreeArguments} object.
 * <p>
 * The usage, per the project spec, is as follows:
 * <p>
 * <pre>
 * java java -jar build/libs/GeneBankSearchBTree.jar --cache=&lt;0|1&gt;  --degree=&lt;btree-degree&gt;
 *  --btreefile=&lt;b-tree-file&gt; --length=&lt;sequence-length&gt; --queryfile=&lt;query-file&gt;
 *  [--cachesize=&lt;n&gt;] [--debug=&lt;0|1&gt;]
 * </pre>
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchBTreeArguments {
	
	 /**
     * A pattern that string arguments must match.
     */
    private static final Pattern argPattern = Pattern.compile("^--([a-zA-Z]+)=([0-9a-zA-Z._/\\\\:-]+)$");
	
	/**
     * Whether to use a cache. 0 or 1
     */
	private final boolean useCache;
    
	/**
     * The degree of the BTree. 0 or >=2
     */
	private final int degree;
    
	/**
     * The file name of the BTree file.
     */
	private final String btreeFileName;
    
	/**
     * The subsequence length. 1-31
     */
	private final int subsequenceLength;
    
	/**
     * The file name of the Query file.
     */
	private final String queryFileName;
    
	/**
     * The cache size. 100-10,000. Will be 0 if unset.
     */
	private final int cacheSize;
    
	/**
     * The debug level. 0 or 1
     */
	private final int debugLevel;

	/**
     * Create a new {@link GeneBankSearchBTreeArguments} object.
     *
     * @param useCache              whether to use a cache. 0 or 1
     * @param degree                the degree of the BTree. 0 or >=2
     * @param btreeFileName         the file name of the BTree file. Should exist on disk
     * @param subsequenceLength     the subsequence length. 1-31
     * @param queryFileName			the file name of the Query file.
     * @param cacheSize             the cache size. 100-10,000
     * @param debugLevel            the debug level. 0 or 1
     */
    public GeneBankSearchBTreeArguments(boolean useCache, int degree, String btreeFileName, int subsequenceLength, String queryFileName, int cacheSize, int debugLevel) {
        this.useCache = useCache;
        this.degree = degree;
        this.btreeFileName = btreeFileName;
        this.subsequenceLength = subsequenceLength;
        this.queryFileName = queryFileName;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        GeneBankSearchBTreeArguments other = (GeneBankSearchBTreeArguments) obj;

        if (cacheSize != other.cacheSize) {
            return false;
        }
        if (getDebugLevel() != other.getDebugLevel()) {
            return false;
        }
        if (getDegree() != other.getDegree()) {
            return false;
        }
        if (getBtreeFileName() == null) {
            if (other.getBtreeFileName() != null) {
                return false;
            }
        } else {
            if (!getBtreeFileName().equals(other.getBtreeFileName())) {
                return false;
            }
        }
        if (subsequenceLength != other.subsequenceLength) {
            return false;
        }
        if (getQueryFileName() == null) {
            if (other.getQueryFileName() != null) {
                return false;
            }
        }
        else {
            if (!getQueryFileName().equals(other.getQueryFileName())) {
                return false;
            }
        }
        if (useCache != other.useCache) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        //this method was generated using an IDE
        return "GeneBankSearchBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + getDegree() +
                ", btreeFileName='" + getBtreeFileName() + '\'' +
                ", subsequenceLength=" + getSubsequenceLength() +
                ", queryFileName='" + getQueryFileName() + '\'' +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + getDebugLevel() +
                '}';
    }


    /**
     * Get whether to use a cache.
     *
     * @return whether to use a cache
     */
    public boolean useCache() {
        return useCache;
    }

    /**
     * @return the degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @return the btreeFileName
     */
    public String getBtreeFileName() {
        return btreeFileName;
    }

    /**
	 * @return the subsequenceLength
	 */
	public int getSubsequenceLength() {
		return subsequenceLength;
	}

    /**
     * @return the queryFileName
     */
    public String getQueryFileName() {
        return queryFileName;
    }

    /**
     * Get the cache size.
     * <p>
     * Will be 0 if the object would return false from {@link #useCache()}.
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
	 * @return the debugLevel
	 */
	public int getDebugLevel() {
		return debugLevel;
	}
	
	 /**
     * Create a GeneBankSearchBTreeArguments object from a string array of arguments, typically the command line args.
     * <p>
     * Similarly to GeneBankCreateBTreeArguments this method has been added to the arguments class that manages what the arguments should be 
     * to be parsed and validated 
     *
     * @param args  the string array of arguments
     * @return      a GeneBankSearchBTreeArguments object
     */
    public static GeneBankSearchBTreeArguments fromStringArgs(String[] args) {
        Map<String, String> argMap = new HashMap<>();

        Set<String> requiredArgs = new HashSet<>(Arrays.asList("cache", "degree", "btreefile", "length", "queryfile"));
        Set<String> optionalArgs = new HashSet<>(Arrays.asList("cachesize", "debug"));

        // Get all strings into argMap
        Matcher matcher;
        for (String argStr : args) {
            matcher = argPattern.matcher(argStr);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid argument: " + argStr);
            }

            String[] argComponents = argStr.split("=");
            String param = argComponents[0].substring(2);
            String arg = argComponents[1];

            if (!requiredArgs.contains(param) && !optionalArgs.contains(param)) {
                throw new IllegalArgumentException("Invalid argument: " + argStr);
            } else {
                argMap.put(param, arg);
            }
        }

        // Check for required arguments
        for (String requiredArg : requiredArgs) {
            if (!argMap.containsKey(requiredArg)) {
                throw new IllegalArgumentException("Missing required argument: " + requiredArg);
            }
        }

        // Check for optional arguments
        for (String optionalArg : optionalArgs) {
            if (!argMap.containsKey(optionalArg)) {
                argMap.put(optionalArg, null);
            }
        }

        // Check for invalid arguments and consistency

        // Cache
        String cacheValue = argMap.get("cache");
        if (!cacheValue.equals("0") && !cacheValue.equals("1")) {
            throw new IllegalArgumentException("Invalid argument. Cache should be 0 or 1: cache=" + cacheValue);
        }

        // Degree
        String degreeValue = argMap.get("degree");
        if (!degreeValue.matches("[0-9]+")) {
            throw new IllegalArgumentException("Invalid argument. Degree should be a non-negative integer: degree=" + degreeValue);
        }
        int degree = Integer.parseInt(degreeValue);
        if (degree < 0 || degree == 1) {
            throw new IllegalArgumentException("Invalid argument. Degree should be 0, or >=2: degree=" + degreeValue);
        }

        // btreefile
        String btreefileValue = argMap.get("btreefile");
        File btreefile = new File(btreefileValue);
        if (!btreefile.exists()) {
            throw new IllegalArgumentException("Invalid argument. GBK file not found: gbkfile=" + btreefileValue);
        }

        // Length
        String lengthValue = argMap.get("length");
        if (!lengthValue.matches("[0-9]+")) {
            throw new IllegalArgumentException("Invalid argument. Length must be a number: length=" + lengthValue);
        }
        int length = Integer.parseInt(lengthValue);
        if (length < 1 || length > 31) {
            throw new IllegalArgumentException("Invalid argument. Length must be 1-31: length=" + lengthValue);
        }
        
        // queryfile
        String queryfileValue = argMap.get("queryfile");
        File queryfile = new File(queryfileValue);
        if (!queryfile.exists()) {
            throw new IllegalArgumentException("Invalid argument. Query file not found: queryfile=" + queryfileValue);
        }

        // Cache size
        String cachesizeValue = argMap.get("cachesize");
        if (cachesizeValue != null) {
            if (!cachesizeValue.matches("[0-9]+")) {
                throw new IllegalArgumentException("Invalid argument. Cache size must be a number: cachesize=" + cachesizeValue);
            }
            int cachesize = Integer.parseInt(cachesizeValue);
            if (cachesize < 100 || cachesize > 10000) {
                throw new IllegalArgumentException("Invalid argument. Cache size must be between 100-10,000: cachesize=" + cachesizeValue);
            }
            if (argMap.get("cache").equals("0")) {  // specified not to have a cache
                throw new IllegalArgumentException("Invalid argument. Cannot specify cache size with no cache: cachesize=" + cachesizeValue + "; cache=" + cacheValue);
            }
        } else {
            if (cacheValue.equals("1")) {
                throw new IllegalArgumentException("Invalid argument. Must specify cache size when using cache: cachesize=" + cachesizeValue + "; cache=" + cacheValue);
            }
            argMap.put("cachesize", "0");
        }

        // Debug
        String debugValue = argMap.get("debug");
        if (debugValue != null) {
            if (!debugValue.matches("[0-9]+")) {
                throw new IllegalArgumentException("Invalid argument. Debug level must be a number: debug=" + debugValue);
            }
            int debug = Integer.parseInt(debugValue);
            if (debug < 0 || debug > 1) {
                throw new IllegalArgumentException("Invalid argument. Debug level must be 0 or 1: debug=" + debugValue);
            }
        } else {
            argMap.put("debug", "0");
            debugValue = "0";
        }

        // Create and return the arguments object
        return new GeneBankSearchBTreeArguments(
                argMap.get("cache").equals("1"),
                argMap.get("degree").equals("0") ? 0 : Integer.parseInt(argMap.get("degree")),
                argMap.get("btreefile"),
                Integer.parseInt(argMap.get("length")),
                argMap.get("queryfile"),
                Integer.parseInt(argMap.get("cachesize")),
                Integer.parseInt(argMap.get("debug"))
        );
    }
}
