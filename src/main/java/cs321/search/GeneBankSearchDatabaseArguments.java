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
 * Create a new instance via the factory method
 * {@link #fromStringArgs(String[])}. This will parse the string array of
 * arguments from the command line, run validation, and return a new
 * {@link GeneBankSearchDataBaseArguments} object.
 * <p>
 * The usage, per the project spec, is as follows:
 * <p>
 * 
 * <pre>
* java -jar  java -jar build/libs/GeneBankSearchDatabase.jar --database=<SQLite-database-path> --queryfile=<query-file>
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
	 * A Path object to the location of the SQLite Database created alongside the
	 * BTree.
	 */
	private final Path databasePath;

	/**
	 * The file name of the Query file. Should exist on disk
	 */
	private final String queryFileName;

	/**
	 * Create a new {@link GeneBankSearchDatabaseArguments} object.
	 *
	 * @param databasePath  The path to the SQLite database created alongside BTree
	 * @param queryFileName the file name of the Query file. Should exist on disk
	 *
	 */
	public GeneBankSearchDatabaseArguments(Path databasePath, String queryFileName) {
		this.databasePath = databasePath;
		this.queryFileName = queryFileName;
	}

	@Override
	public boolean equals(Object obj) {
		// this method was generated using an IDE
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

	@Override
	public String toString() {
		// this method was generated using an IDE
		return "GeneBankSearchDatabaseArguments{" + "dataBasePath=" + getDatabasePath() + ", queryFileName='"
				+ getQueryFileName() + '\'' + '}';
	}

	/**
	 * @return the databasePath
	 */
	public Path getDatabasePath() {
		return databasePath;
	}

	/**
	 * @return the queryFileName
	 */
	public String getQueryFileName() {
		return queryFileName;
	}

	/**
	 * Create a GeneBankSearchDatabaseArguments object from a string array of
	 * arguments, typically the command line args.
	 * <p>
	 * Similarly to GeneBankCreateBTreeArguments this method has been added to the
	 * arguments class that manages what the arguments should be to be parsed and
	 * validated
	 * 
	 * @param  args the string array of arguments
	 * @return      a GeneBankSearchDatabaseArguments object
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
		String databasePathString = argMap.get("database");
		try {
			Path databasePath = Paths.get(databasePathString);
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException(
					"Invalid Path argument: " + databasePathString + " unable to be resolved.");
		}

		// queryfile
		String queryFileValue = argMap.get("queryfile");
		File queryfile = new File(queryFileValue);
		if (!queryfile.exists()) {
			throw new IllegalArgumentException("Invalid argument. query file not found: queryfile=" + queryFileValue);
		}

		// Create and return the arguments object
		return new GeneBankSearchDatabaseArguments(Paths.get(databasePathString), argMap.get("queryfile"));
	}

}
