package cs321.create;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to represent the arguments to the GeneBankCreateBTree program.
 * <p>
 * Create a new instance via the factory method {@link #fromStringArgs(String[])}. This will parse the string array of
 * arguments from the command line, run validation, and return a new {@link GeneBankCreateBTreeArguments} object.
 * <p>
 * The usage, per the project spec, is as follows:
 * <p>
 * <pre>
 * java -jar build/libs/GeneBankCreateBTree.jar --cache=&lt;0|1&gt;  --degree=&lt;btree-degree&gt;
 *  --gbkfile=&lt;gbk-file&gt; --length=&lt;sequence-length&gt; [--cachesize=&lt;n&gt;] [--debug=&lt;0|1&gt;]
 * </pre>
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankCreateBTreeArguments {

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
     * The file name of the GBK file. Should exist on disk
     */
    private final String gbkFileName;

    /**
     * The subsequence length. 1-31
     */
    private final int subsequenceLength;

    /**
     * The cache size. 100-10,000. Will be 0 if unset.
     */
    private final int cacheSize;

    /**
     * The debug level. 0 or 1
     */
    private final int debugLevel;

    /**
     * Create a new {@link GeneBankCreateBTreeArguments} object.
     *
     * @param useCache              whether to use a cache. 0 or 1
     * @param degree                the degree of the BTree. 0 or >=2
     * @param gbkFileName           the file name of the GBK file. Should exist on disk
     * @param subsequenceLength     the subsequence length. 1-31
     * @param cacheSize             the cache size. 100-10,000
     * @param debugLevel            the debug level. 0 or 1
     */
    GeneBankCreateBTreeArguments(boolean useCache, int degree, String gbkFileName, int subsequenceLength, int cacheSize, int debugLevel) {
        this.useCache = useCache;
        this.degree = degree;
        this.gbkFileName = gbkFileName;
        this.subsequenceLength = subsequenceLength;
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

        GeneBankCreateBTreeArguments other = (GeneBankCreateBTreeArguments) obj;

        if (cacheSize != other.cacheSize) {
            return false;
        }
        if (getDebugLevel() != other.getDebugLevel()) {
            return false;
        }
        if (getDegree() != other.getDegree()) {
            return false;
        }

        if (getGbkFileName() == null) {
            if (other.getGbkFileName() != null) {
                return false;
            }
        } else {
            if (!getGbkFileName().equals(other.getGbkFileName())) {
                return false;
            }
        }
        if (subsequenceLength != other.subsequenceLength) {
            return false;
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
    public String toString() {
        return "GeneBankCreateBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + getDegree() +
                ", gbkFileName='" + getGbkFileName() + '\'' +
                ", subsequenceLength=" + subsequenceLength +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + getDebugLevel() +
                '}';
    }

    /**
     * Get whether to use a cache.
     *
     * @return a boolean that specifies whether a cache should be used.
     */
    public boolean useCache() {
        return useCache;
    }

	/**
     * Get the degree.
     * <p>
     * Will be 0 if the degree was not specified, which means the degree should be calculated.
     *
	 * @return the degree
	 */
	public int getDegree() {
		return degree;
	}

    /**
     * Get the file name of the GBK file.
     * <p>
     * Will be a valid file on the system.
     *
     * @return the gbkFileName
     */
    public String getGbkFileName() {
        return gbkFileName;
    }

    /**
     * Get the subsequence length that should be encoded into longs.
     * <p>
     * Will be between 1 and 31, inclusive.
     *
     * @return the subsequenceLength
     */
    public int getSubsequenceLength() {
        return subsequenceLength;
    }

    /**
     * Get the cache size.
     * <p>
     * Will be 0 if the object would return false from {@link #useCache()}.
     *
     * @return the cache size
     */
    public int getCacheSize() {
        return cacheSize;
    }


	/**
     * Get the debug level.
     *
	 * @return the debugLevel
	 */
	public int getDebugLevel() {
		return debugLevel;
	}

    /**
     * Create a GeneBankCreateBTreeArguments object from a string array of arguments, typically the command line args.
     * <p>
     * A test located this functionality within the `GeneBankCreateBTree.java` file, but it seems better classified as
     * a job of the `GeneBankCreateBTreeArguments`, via a static factory. That way, the {@link GeneBankCreateBTree}
     * class doesn't have to know about <i>how</i> arguments are parsed, just that they are, and that logic and its
     * correctness is maintained by the class that actually manages them. In short: putting the logic here can be seen
     * as a form of lessening the degree of collaboration between the two classes, which is generally a good thing when
     * it can be done.
     *
     * @param args  the string array of arguments
     * @return      a GeneBankCreateBTreeArguments object
     */
    public static GeneBankCreateBTreeArguments fromStringArgs(String[] args) {
        Map<String, String> argMap = new HashMap<>();

        Set<String> requiredArgs = new HashSet<>(Arrays.asList("cache", "degree", "gbkfile", "length"));
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

        // Gbkfile
        String gbkfileValue = argMap.get("gbkfile");
        File gbkfile = new File(gbkfileValue);
        if (!gbkfile.exists()) {
            throw new IllegalArgumentException("Invalid argument. GBK file not found: gbkfile=" + gbkfileValue);
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
        return new GeneBankCreateBTreeArguments(
                argMap.get("cache").equals("1"),
                argMap.get("degree").equals("0") ? 0 : Integer.parseInt(argMap.get("degree")),
                argMap.get("gbkfile"),
                Integer.parseInt(argMap.get("length")),
                Integer.parseInt(argMap.get("cachesize")),
                Integer.parseInt(argMap.get("debug"))
        );
    }
}
