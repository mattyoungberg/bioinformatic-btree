package cs321.btree;

/**
 * A {@link BTreeException} is thrown on failure to construct a {@link BTree}.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
public class BTreeException extends Exception {

    /**
     * Construct a new {@link BTreeException} with the given message.
     *
     * @param message The message to associate with this {@link BTreeException}.
     */
    public BTreeException(String message) {
        super(message);
    }
}
