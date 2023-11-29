package cs321.btree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

/**
 * A utility class for creating a SQLite database file for a BTree.
 *
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 */
public class BTreeSQLiteDBBuilder {

    /**
     * Drops the Sequences table if it exists.
     */
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS Sequences;";

    /**
     * Drops the index on the Encoding column of the Sequences table if it exists.
     */
    private static final String DROP_INDEX = "DROP INDEX IF EXISTS idx_sequence;";

    /**
     * Creates the Sequences table if it does not exist.
     */
    private static final String CREATE_TABLE = "CREATE TABLE Sequences (Encoding INTEGER PRIMARY KEY, Frequency INTEGER NOT NULL);";

    /**
     * Creates an index on the Encoding column of the Sequences table if it does not exist.
     */
    private static final String CREATE_INDEX = "CREATE INDEX IF NOT EXISTS idx_sequence ON Sequences (Encoding);";

    /**
     * Batch size of inserts into the database.
     */
    private static final int BATCH_SIZE = 1000;

    /**
     * Creates a SQLite database file for the given BTree and the path whose file created it.
     *
     * @param btree         the BTree to create the database for
     * @param path          the path whose file created the BTree
     * @throws SQLException if an error occurs while creating the database
     */
    public static void create(BTree btree, Path path) throws SQLException {
        // Derive the database filename
        String dbFilename = getFileName(btree, path);

        // Get and configure connection
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilename)) {
            connection.setAutoCommit(false);

            // Initialize database with table and index
            initializeDB(connection);

            // Insert all elements from the BTree into the database
            Iterator<TreeObject> iter = btree.iterator();
            batchInsert(connection, iter);
        }
    }

    /**
     * Derive the database filename from the BTree and the path whose file created it, per the project specification.
     *
     * @param btree the BTree to create the database for
     * @param path  the path whose file created the BTree
     * @return      the database filename
     */
    private static String getFileName(BTree btree, Path path) {
        if (Files.exists(path)  && Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a file");
        }
        String fileName = path.toString();
        String base = fileName.substring(0, fileName.lastIndexOf(".gbk"));
        return base + "." + TreeObject.subsequenceLength + ".db";
    }

    /**
     * Create the Sequences table and index. Drops the Sequences table and its index if they already exist.
     *
     * @param connection    the connection to the database
     * @throws SQLException if an error occurs while creating the database
     */
    private static void initializeDB(Connection connection) throws SQLException {
        try (Statement tableStmt = connection.createStatement()) {
            tableStmt.execute(DROP_TABLE);
            tableStmt.execute(DROP_INDEX);
            tableStmt.execute(CREATE_TABLE);
            tableStmt.execute(CREATE_INDEX);
            connection.commit();
        }
    }

    /**
     * Insert all elements from the BTree into the database in batches of the given size.
     *
     * @param connection    the connection to the database
     * @param iter          the iterator over the BTree
     * @throws SQLException if an error occurs while inserting into the database
     */
    private static void batchInsert(Connection connection, Iterator<TreeObject> iter) throws SQLException {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        while (iter.hasNext()) {
            if (count == 0) {
                // Start a new batch
                sb.setLength(0); // Reset the StringBuilder
                sb.append("INSERT INTO Sequences (Encoding, Frequency) VALUES ");
            }

            TreeObject obj = iter.next();
            sb.append(encodeTreeObject(obj));
            count++;

            // Check if the batch is full or if there are no more elements
            if (count == BATCH_SIZE || !iter.hasNext()) {
                sb.append(";"); // End the current batch
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sb.toString());
                    connection.commit();
                }
                count = 0; // Reset the counter for the next batch
            } else {
                sb.append(", "); // Continue the current batch
            }
        }
    }

    /**
     * Encode a TreeObject as a string for insertion into the database.
     *
     * @param obj   the TreeObject to encode
     * @return      the String encoding of the TreeObject to be included in an INSERT statement
     */
    private static String encodeTreeObject(TreeObject obj) {
        return "(" + obj.getSubsequence() + ", " + obj.getCount() + ")";
    }

}
