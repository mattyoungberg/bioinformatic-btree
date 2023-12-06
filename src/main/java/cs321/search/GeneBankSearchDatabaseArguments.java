package cs321.search;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to represent the arguments to the GeneBankSearchDatabase program.
 * <p>
 * Create a new instance via the factory method {@link #fromStringArgs(String[])}. This will parse the string array of
 * arguments from the command line, run validation, and return a new {@link GeneBankSearchDatabaseArguments} object.
 * <p>
 * The usage, per the project spec, is as follows:
 * <p>
 * <pre>
 * java -jar  java -jar build/libs/GeneBankSearchDatabase.jar --database=&lt;SQLite-database-path&gt;
 * --queryfile=&lt;query-file&gt;
 * </pre>
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class GeneBankSearchDatabaseArguments {

	/**
	 * A pattern that string arguments must match.
	 */
	private static final Pattern argPattern = Pattern.compile("^--([a-zA-Z]+)=([0-9a-zA-Z._/\\\\:-]+)$");

	/**
	 * A {@link Path} object to the location of the SQLite Database created alongside the {@link cs321.btree.BTree}.
	 */
	private final Path databasePath;

	/**
	 * The file name of the Query file. Should exist on disk.
	 */
	private final String queryFileName;

	/**
	 * Create a new {@link GeneBankSearchDatabaseArguments} object.
	 *
	 * @param databasePath  The path to the SQLite database created alongside the {@link cs321.btree.BTree}
	 * @param queryFileName the file name of the Query file. Should exist on disk
	 *
	 */
	public GeneBankSearchDatabaseArguments(Path databasePath, String queryFileName) {
		this.databasePath = databasePath;
		this.queryFileName = queryFileName;
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
		GeneBankSearchDatabaseArguments other = (GeneBankSearchDatabaseArguments) obj;
		if (getQueryFileName() == null) {
			if (other.getQueryFileName() != null) {
				return false;
			}
		} else {
			if (!getQueryFileName().equals(other.getQueryFileName())) {
				return false;
			}
		}
		if (getDatabasePath() == null) {
			if (other.getDatabasePath() != null) {
				return false;
			}
		} else {
			if (!getDatabasePath().equals(other.getDatabasePath())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "GeneBankSearchDatabaseArguments{" + "dataBasePath=" + getDatabasePath() + ", queryFileName='"
				+ getQueryFileName() + '\'' + '}';
	}

	/**
	 * Get the path to the SQLite database created alongside {@link cs321.btree.BTree}.
	 *
	 * @return the databasePath
	 */
	public Path getDatabasePath() {
		return databasePath;
	}

	/**
	 * Get the file name of the Query file. Should exist on disk.
	 *
	 * @return the queryFileName
	 */
	public String getQueryFileName() {
		return queryFileName;
	}

	/**
	 * Create a GeneBankSearchDatabaseArguments object from a string array of arguments, typically the command line
	 * args.
	 * <p>
	 * Similarly to GeneBankCreateBTreeArguments this method has been added to the arguments class that manages what the
	 * arguments should be to be parsed and validated
	 * 
	 * @param args	the String array of arguments
	 * @return		a {@link GeneBankSearchDatabaseArguments} object
	 */
	public static GeneBankSearchDatabaseArguments fromStringArgs(String[] args) {
		Map<String, String> argMap = new HashMap<>();

		Set<String> requiredArgs = new HashSet<>(Arrays.asList("database", "queryfile"));

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

			if (!requiredArgs.contains(param)) {
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

		// Check for invalid arguments and consistency

		// database path
		String databasePathValue = argMap.get("database");
		File databaseFile = new File(databasePathValue);
		if (!databaseFile.exists()) {
			throw new IllegalArgumentException("Invalid argument. database file not found: database=" + databasePathValue);
		}

		// queryfile
		String queryFileValue = argMap.get("queryfile");
		File queryfile = new File(queryFileValue);
		if (!queryfile.exists()) {
			throw new IllegalArgumentException("Invalid argument. query file not found: queryfile=" + queryFileValue);
		}

		// Create and return the arguments object
		return new GeneBankSearchDatabaseArguments(Paths.get(databasePathValue), argMap.get("queryfile"));
	}
}
