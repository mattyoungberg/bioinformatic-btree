package cs321.btree;

import cs321.create.SequenceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Implements a BTree, which is optimized for large amounts of data that cannot fit in memory, given the proper degree.
 * <p>
 * The {@link BTree} tracks information on the overall structure including the total number of nodes, the degree used,
 * and the total number of {@link TreeObject}s stored in the structure.
 * <p>
 * A BTree can be constructed from a file, or created from scratch. If a file is given, the {@link BTree} will read the
 * metadata from the file and construct the root node in memory. If a file is not given, the {@link BTree} will create
 * a new file and write the metadata to it, and then construct the root node in memory.
 * <p>
 * A {@link BTree}'s file is not guaranteed to be in a valid state until the {@link BTree#finishUp()} method is called.
 * If you are making modifications to a {@link BTree}, you must call {@link BTree#finishUp()} to ensure that the file
 * is in a valid state, probably in a try/finally statement.
 * <p>
 * Given this class is dependent on its representation on disk, it is useful to describe its layout. The first segement
 * of the file is always the metadata of the {@link BTree}, which includes the following fields:
 * <p>
 * <ul>
 *     <li>The degree of the {@link BTree}</li>
 *     <li>The position of the root node of the {@link BTree}</li>
 *     <li>The number of keys in the {@link BTree}</li>
 *     <li>The height of the {@link BTree}</li>
 * </ul>
 * <p>
 * After which, it will begin to store its nodes. The nodes do not dynamically expand, as that would necessitate a
 * rewrite of the entire file. Instead, the {@link BTree} will allocate the max amount of space for each node, and
 * only use the space that it needs. For this reason, it is recommended you allow this class to calculate the optimal
 * degree for a block size of 4096B, as that will maximize the size of a given node given the block size. Each node
 * stores the following fields:
 * <p>
 * <ul>
 *     <li>Its keys</li>
 *     <li>Its children positions in the file</li>
 * </ul>
 * <p>
 * As such, you can visualize the layout of the file as follows (if the {@link BTree} was tracking 3 nodes at optimal
 * block size):
 * <p>
 * <pre>
 * +----------------+--------------------------------+--------------------------------+
 * |   Metadata     |            Node 1              |            Node 2              |
 * |     20B        |            ~4096B              |            ~4096B              |
 * +----------------+--------------------------------+--------------------------------+
 * </pre>
 *
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 *
 */
public class BTree implements BTreeInterface, Iterable<TreeObject> {
    /**
     * The size of the metadata for the {@link BTree} in bytes.
     */
    private final int METADATA_SIZE = Long.BYTES + Integer.BYTES * 3;  // t + root position + count + height

    /**
     * The {@link FileChannel} that the {@link BTree} is stored in.
     */
    private final FileChannel fileChannel;

    /**
     * The {@link ByteBuffer} that is used to read and write metadata to the {@link BTree}.
     */
    private final ByteBuffer metadataBuffer;

    /**
     * The {@link ByteBuffer} that is used to read and write {@link BTreeNode}s to the {@link BTree}.
     */
    private final ByteBuffer nodeBuffer;

	/**
	 * The minimum degree of the {@link BTree}
	 */
	private int t;

	/**
	 * The position on disk of the root node of the {@link BTree}
	 */
	private long rootPosition;

	/**
	 * The number of keys in the {@link BTree}, for quick access
	 */
	private int keyCount;

	/**
	 * The height of the {@link BTree}, for quick access
	 */
	private int height;

	/**
	 * The path to the file that stores the {@link BTree} on disk
	 */
	private Path filePath;

	/**
	 * The root node of the {@link BTree}, which is always in memory
	 */
	BTreeNode root;  // Package private for BTreeInOrderIterator

	/**
	 * Construct a BTree that already exists on disk, or if not, create a new one with an optimal degree.
	 * <p>
	 * {@link BTree}s are designed to maximize their degree <code>t</code> under a constraint of 4096KB disk blocks.
	 *
	 * @param fileName 		file name that stores the {@link BTree} on disk
	 */
	public BTree(String fileName) throws BTreeException {
		this.filePath = Paths.get(fileName);
		boolean exists = filePath.toFile().exists();
		if (exists) {
			// Open the file for processing, get FileChannel
			try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
				this.fileChannel = file.getChannel();
			} catch (IOException e) {
				throw new BTreeException(e.getMessage());  // Given tests only take BTreeException
			}

			// Read metadata in, prepare for reading root
			this.metadataBuffer = ByteBuffer.allocateDirect(METADATA_SIZE);
			readMetaData();  // sets t, rootAddress, keyCount, height

			// Allocate nodeBuffer, set root node
			this.nodeBuffer = ByteBuffer.allocateDirect(BTreeNode.getByteSize(t));
			try {
				this.root = diskRead(rootPosition);
			} catch (IOException e) {
				throw new BTreeException(e.getMessage());  // Given tests only take BTreeException
			}
		} else {  // Copied code from other constructor; not DRY, but we can't call the other constructor
			this.t = calculateOptimalT();
			this.rootPosition = METADATA_SIZE;  // Manually set before fileChannel is initialized
			this.keyCount = 0;
			this.height = 0;

			// Open the file for processing, get FileChannel
			try {
				this.fileChannel = new RandomAccessFile(fileName, "rw").getChannel();
			} catch (FileNotFoundException e) {				// Given test `testBTreeCreate` only expects BTreeException,
				throw new BTreeException(e.getMessage());  	// so we cast here, instead of raising an I/O based one.
			}

			// Write metadata to file
			this.metadataBuffer = ByteBuffer.allocateDirect(METADATA_SIZE);
			try {
				writeMetaData();
			} catch (IOException e) {
				throw new BTreeException(e.getMessage());
			}

			// Allocate nodeBuffer, write root node
			this.nodeBuffer = ByteBuffer.allocateDirect(BTreeNode.getByteSize(t));
			try {
				createBTree();  // See page 506 of textbook, B-TREE-CREATE(T)
			} catch (IOException e) {
				throw new BTreeException(e.getMessage());  // Given test `testBTreeCreate` only take BTreeException
			}
		}
	}

	/**
	 * Construct a new BTree with the given degree.
	 * <p>
	 * If the given degree is 0, the optimal degree will be calculated for a block size of 4096 bytes.
	 *
	 * @param degree		integer value for the desired degree of BTree
	 * @param fileName		file name that will the {@link BTree} on disk
	 */
	public BTree(int degree, String fileName) throws BTreeException {
		this.filePath = Paths.get(fileName);
		if (degree == 0) {
			this.t = calculateOptimalT();
		}  else if (degree == 1) {
			throw new IllegalArgumentException("Degree must be greater than 1");
		} else {
			this.t = degree;
		}
		this.rootPosition = METADATA_SIZE;  // Manually set before fileChannel is initialized
		this.keyCount = 0;
		this.height = 0;

		// Open the file for processing, get FileChannel
		try {
			this.fileChannel = new RandomAccessFile(fileName, "rw").getChannel();
		} catch (FileNotFoundException e) {				// Given test `testBTreeCreateDegree` only expects
			throw new BTreeException(e.getMessage());  	// BTreeException, so we cast here, instead of raising an I/O.
		}

		// Write metadata to file
		this.metadataBuffer = ByteBuffer.allocateDirect(METADATA_SIZE);
		try {
			writeMetaData();
		} catch (IOException e) {
			throw new BTreeException(e.getMessage());
		}

		// Allocate nodeBuffer, write root node
		this.nodeBuffer = ByteBuffer.allocateDirect(BTreeNode.getByteSize(t));
		try {
			createBTree();  // See page 506 of textbook, B-TREE-CREATE(T)
		} catch (IOException e) {
			throw new BTreeException(e.getMessage());  // Given test `testBTreeCreateDegree` only take BTreeException
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSize() {
		return keyCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDegree() {
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfNodes() {
        try {
			return (int) (fileChannel.size() - METADATA_SIZE) / BTreeNode.getByteSize(t);
		} catch (IOException e) {
			throw new RuntimeException(e);  // BTreeInterface does not specify IOException, throwing as a runtime one.
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
        return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(long key) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert(TreeObject obj) throws IOException {
		// See page 508 of textbook, B-TREE-INSERT(T, k)
		if (root.keyCount == BTreeNode.getMaxKeyCount(t)) {
			BTreeNode s = splitRoot();
			insertNonFull(s, obj, rootPosition);
		} else {
			insertNonFull(root, obj, rootPosition);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In order to dump to a file, you must set the {@link TreeObject#subsequenceLength} class variable first.
	 */
	@Override
	public void dumpToFile(PrintWriter out) throws IOException {
		if (TreeObject.subsequenceLength == -1) {
			throw new IllegalStateException("TreeObject.subsequenceLength must be set before dumping to a file");
		}
		BTreeInOrderIterator iterator = new BTreeInOrderIterator(this);
		while (iterator.hasNext()) {
			TreeObject obj;
			try {
				obj = iterator.next();
			} catch (RuntimeException e) {
				throw new IOException(e);  // Iterator can't throw IOException, so we wrap it in a RuntimeException
			}
			String subSeqString = SequenceUtils.longToDnaString(obj.getSubsequence(), TreeObject.subsequenceLength);
			out.println(subSeqString + " " + obj.getCount());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreeObject search(long key) throws IOException {
		return searchRecursive(root, new TreeObject(key));
	}

	/**
	 * Get an {@link Iterator} for the {@link BTree} that performs inorder traversal.
	 *
	 * @return an {@link Iterator} for the {@link BTree} that performs inorder traversal
	 */
	public Iterator<TreeObject> iterator() {
		try {
			return new BTreeInOrderIterator(this);
		} catch (IOException e) {
			throw new RuntimeException(e);  // Iterable can't throw IOException, so we wrap it in a RuntimeException
		}
	}

	/**
	 * Finalize the state of the file on disk at the end of the lifetime of the {@link BTree} in memory.
	 * <p>
	 * Note that clients must wield this method to ensure a valid {@link BTree} file on disk. <b>However, this method is
	 * NOT required by {@link BTreeInterface}.</b> This means that you cannot wield the ADT in any way when creating a
	 * {@link BTree} or otherwise modifying one.
	 *
	 * @throws IOException	if an I/O error occurs
	 */
	public void finishUp() throws IOException, SQLException {
		diskWrite(root, rootPosition);
		writeMetaData();
		fileChannel.close();
		BTreeSQLiteDBBuilder.create(this, filePath);
	}

	/**
	 * Get the keys of the {@link BTree} in sorted order.
	 * <p>
	 * Note that this method exists for a specific test that was given in the project files. It's not invoked anywhere
	 * else as far as I know.
	 *
	 * @return	an array of the keys of the {@link BTree} in sorted order
	 */
	public long[] getSortedKeyArray() throws IOException {  // Doesn't throw IOExceptions directly, but tests expect it.
		Iterator<TreeObject> iter = this.iterator();		// Iterator masks IOExceptions with RuntimeExceptions to
		long[] keys = new long[keyCount];					// meet interface.
		for (int i = 0; i < keyCount; i++) {
			keys[i] = iter.next().getSubsequence();
		}
		return keys;
	}

	/**
	 * Create a new {@link BTreeNode} and set it as the root of the {@link BTree}.
	 */
	private void createBTree() throws IOException {
		BTreeNode newRoot = new BTreeNode(t);
		newRoot.leaf = true;
		newRoot.keyCount = 0;
		diskWrite(newRoot, rootPosition);
		this.root = newRoot;
	}

	/**
	 * Split the root in preparation for an insertion.
	 *
	 * @return				the new root of the {@link BTree}
	 * @throws IOException	if an I/O error occurs
	 */
	private BTreeNode splitRoot() throws IOException {
		// See page 509 of textbook, B-TREE-SPLIT-ROOT(T)
		BTreeNode s = new BTreeNode(t);
		s.leaf = false;
		s.keyCount = 0;
		s.childPositions[0] = rootPosition;
		root = s;
		rootPosition = getNextPosition();
		diskWrite(root, rootPosition);  // Has to be written to disk so splitChild can figure out where new node goes
		splitChild(s, 0, rootPosition);
		height++;  // height can only increase at split of the root node: pg 508, near the bottom
		return s;
	}

	/**
	 * Splits the child of a {@link BTreeNode} at the given position.
	 *
	 * @param x			the parent {@link BTreeNode} of the child to split, <b>assumed to be non-full</b>.
	 * @param i			the index of the childPosition to split in {@link BTreeNode#childPositions}.
	 * @param xPosition	the position of x in the file, to write back updates
	 */
	private void splitChild(BTreeNode x, int i, long xPosition) throws IOException {
		// See page 507 of textbook, B-TREE-SPLIT-CHILD(x, i)
        BTreeNode y = diskRead(x.childPositions[i]);						// full node to split
        BTreeNode z = new BTreeNode(t);  									// z will take half of y
		z.leaf = y.leaf;
		z.keyCount = BTreeNode.getMinKeyCount(t);
		for (int j = 0; j <= BTreeNode.getMinKeyCount(t) - 1; j++) {		// z gets y's greatest keys...
			z.keys[j] = y.keys[j + t];
			y.keys[j + t] = null;											// ... and y loses them
		}
		if (!y.leaf) {
			for (int j = 0; j <= BTreeNode.getMinChildCount(t) - 1; j++) {	// ... and its corresponding children
				z.childPositions[j] = y.childPositions[j + t];
				y.childPositions[j + t] = 0;								// ... and y loses them
			}
		}
		y.keyCount = BTreeNode.getMinKeyCount(t);
		for (int j = x.keyCount; j >= i + 1; j--) {							// y keeps t-1 keys
			x.childPositions[j+1] = x.childPositions[j];					// shift x's children to the right...
		}
		x.childPositions[i + 1] = getNextPosition();                       	// ... to make room for z as a child
		for (int j = x.keyCount - 1; j >= i; j--) {							// shift the corresponding keys in x
			x.keys[j+1]= x.keys[j];
		}
		x.keys[i] = y.keys[BTreeNode.getMinKeyCount(t)];					// insert y's median key
		y.keys[BTreeNode.getMinKeyCount(t)] = null;							// y's median key goes to x
		x.keyCount++;														// x has gained a child
		diskWrite(y, x.childPositions[i]);
		diskWrite(z, x.childPositions[i + 1]);
		diskWrite(x, xPosition);
	}

	/**
	 * Inserts a {@link TreeObject} into a {@link BTreeNode} that is assumed to be non-full.
	 *
	 * @param x			the {@link BTreeNode} to insert into
	 * @param k			the {@link TreeObject} to insert
	 * @param xPosition	the position of x in the file, to write back updates
	 */
	private void insertNonFull(BTreeNode x, TreeObject k, long xPosition) throws IOException {
		int i = x.keyCount - 1;
		if (x.leaf) {																// inserting into a leaf
			while (i >= 0 && k.getSubsequence() < x.keys[i].getSubsequence()) {		// shift keys in x to make room for k
				x.keys[i+1] = x.keys[i];
				i--;
			}
			if (i >= 0 && k.getSubsequence() == x.keys[i].getSubsequence()) {		// Check if we have a duplicate
				x.keys[i].incrementFrequency();
				diskWrite(x, xPosition);
				return;
			}
			x.keys[i+1] = k;														// insert k in x
			x.keyCount++;															//  now x has 1 more key
			diskWrite(x, xPosition);
			keyCount++;  															// increment BTree keyCount
		} else {
			while (i >= 0 && k.getSubsequence() < x.keys[i].getSubsequence()) {		// find the child where k belongs
				i--;
			}
			i++;
			BTreeNode y = diskRead(x.childPositions[i]);
			if (y.keyCount == BTreeNode.getMaxKeyCount(t)) {						// split the child if it is full
				splitChild(x, i, xPosition);
//				x = diskRead(xPosition);											// reread y after split...
				y = diskRead(x.childPositions[i]);									// reread y after split...
				if (k.getSubsequence() > x.keys[i].getSubsequence()) {				// does k go into x.c[i] or x.c[i+1]
					i++;
					y = diskRead(x.childPositions[i]);
				}
			}
			for (int j = 0; j < x.keyCount; j++) {									// double check after split that
				if (k.getSubsequence() == x.keys[j].getSubsequence()) {				// target node didn't move up
					x.keys[j].incrementFrequency();
					diskWrite(x, xPosition);
					return;
				}
			}
			insertNonFull(y, k, x.childPositions[i]);
		}
	}

	/**
	 * Searches for a sequence in the given {@link BTree} recursively.
	 * <p>
	 * This algorithm is based on the pseudocode provided in the textbook on page 505 of the textbook. As such, the
	 * public method {@link BTree#search(long)} is a wrapper to start the calls for this method.
	 *
	 * @param node			the node to search
	 * @param obj			the TreeObject to search for
	 * @return				the TreeObject if found, null otherwise
	 * @throws IOException	if an I/O error occurs
	 */
	private TreeObject searchRecursive(BTreeNode node, TreeObject obj) throws IOException {
		// See page 505 of textbook, B-TREE-SEARCH(x, k)
		int i = 0;
		while (i < node.keyCount && obj.getSubsequence() > node.keys[i].getSubsequence()) {
			i++;
		}
		if (i < node.keyCount && obj.getSubsequence() == node.keys[i].getSubsequence()) {
			return node.keys[i];
		} else if (node.leaf) {
			return null;
		} else {
			BTreeNode child = diskRead(node.childPositions[i]);
			return searchRecursive(child, obj);
		}
	}

	/**
	 * Calculate the optimal minimum degree <code>t</code> such that the maximum amount of nodes can fit in 4096 bytes
	 * of disk space.
	 *
	 * @return	the optimal minimum degree <code>t</code>
	 */
	private int calculateOptimalT() {
		int t = 0;
		while (BTreeNode.getByteSize(t) <= 4096) {
			t++;
		}
		return t - 1;
	}
	
    /**
     * Read the metadata from {@link BTree#fileChannel} and set its corresponding fields in the instance.
	 * <p>
	 * This method will set the following fields:
	 * <ul>
	 *     <li>{@link BTree#t}</li>
	 *     <li>{@link BTree#rootPosition}</li>
	 *     <li>{@link BTree#keyCount}</li>
	 *     <li>{@link BTree#height}</li>
 *     </ul>
	 * <p>
	 * This method is to only be used upon instance construction; metadata read directly from the disk is not guaranteed
	 * to be correct in the middle of the lifetime of the instance in memory (per the project specification). Metadata
	 * on disk is only guaranteed to be correct upon the opening the file, and after an invocation of
	 * {@link BTree#finishUp}.
	 * <p>
	 * Metadata is stored in memory in the following sequence:
	 * <p>
	 * <pre>
	 * +--------+----------------+--------+--------+
	 * |   t    |  rootPosition  | keyCnt | height |
	 * |  4B    |      8B        |  4B    |  4B    |
	 * +--------+----------------+--------+--------+
	 * </pre>
     */

	private void readMetaData() {
		try {
			fileChannel.position(0);
			metadataBuffer.clear();
			fileChannel.read(metadataBuffer);
			metadataBuffer.flip();
		} catch (IOException e) {
			throw new RuntimeException(e);  // Tests do not expect an IOException, throwing as RuntimeException
		}

		this.t = metadataBuffer.getInt();
		this.rootPosition = metadataBuffer.getLong();
		this.keyCount = metadataBuffer.getInt();
		this.height = metadataBuffer.getInt();
	}
	
	/**
     * Write the metadata to the {@link BTree#fileChannel}.
	 * <p>
	 * The data included in writing are the following fields:
	 * <p>
	 * <ul>
	 *     <li>{@link BTree#t}</li>
	 *     <li>{@link BTree#rootPosition}</li>
	 *     <li>{@link BTree#keyCount}</li>
	 *     <li>{@link BTree#height}</li>
	 * </ul>
	 * <p>
	 * Metadata is written to the file only upon 1) construction of a new {@link BTree} and 2) after an invocation of
	 * {@link BTree#finishUp}. It is not guaranteed to be correct in the middle of the lifetime of the instance in
	 * memory (per the project specification).
	 * <p>
	 * Metadata is stored in memory in the following sequence:
	 * <p>
	 * <pre>
	 * +--------+----------------+--------+--------+
	 * |   t    |  rootPosition  | keyCnt | height |
	 * |  4B    |      8B        |  4B    |  4B    |
	 * +--------+----------------+--------+--------+
	 * </pre>
     */
    private void writeMetaData() throws IOException {
        metadataBuffer.clear();
		metadataBuffer.putInt(t);
		metadataBuffer.putLong(rootPosition);
		metadataBuffer.putInt(keyCount);
		metadataBuffer.putInt(height);
		metadataBuffer.flip();

		fileChannel.position(0);
		fileChannel.write(metadataBuffer);
    }

    /**
     * Reads a {@link BTreeNode} from the {@link BTree#fileChannel}.
     *
     * @param position      the byte offset for the node in the data file
     * @return              the {@link BTreeNode} read from the disk
     */
    BTreeNode diskRead(long position) throws IOException {  // Package private for BTreeInOrderIterator
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative");
        }

        if (position < METADATA_SIZE) {
            throw new IllegalArgumentException("cannot read node from tree metadata");
        }

		fileChannel.position(position);
		nodeBuffer.clear();
		fileChannel.read(nodeBuffer);
		nodeBuffer.flip();

        return BTreeNode.fromByteBuffer(nodeBuffer, getDegree());
    }

    /**
     * Writes a {@link BTreeNode} to the {@link BTree#fileChannel}.
     *
     * @param node          the node to write
     * @param position      the byte offset for the node in the data file
     */
    private void diskWrite(BTreeNode node, long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative");
        }

        if (position < METADATA_SIZE) {
            throw new IllegalArgumentException("cannot write node to tree metadata");
        }

		fileChannel.position(position);
		nodeBuffer.clear();
		node.writeToByteBuffer(nodeBuffer);
		nodeBuffer.flip();
		fileChannel.write(nodeBuffer);
		fileChannel.force(true);
    }

	/**
	 * Get the next position in the {@link BTree#fileChannel} to which you can write a new {@link BTreeNode}.
	 * <p>
	 * This method really just acts as a wrapper to catch any {@link IOException}s that can occur while reading the
	 * {@link BTree#fileChannel}'s size.
	 *
	 * @return	the next position in the {@link BTree#fileChannel} to which you can write a new {@link BTreeNode}
	 */
	private long getNextPosition() {
		try {
			return fileChannel.size();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
