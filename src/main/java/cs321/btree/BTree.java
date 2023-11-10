package cs321.btree;

import cs321.create.SequenceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * A BTree class that stores a series of TreeObjects using a standard BTree implementation provided by BTreeInterface.
 * The BTree tracks information on the overall structure including the total number of nodes the degree used in the BTree and the total number of keys/ TreeObjects stored in the structure
 * A BTree can be constructed from a file read from disk or used to create a new file for a Btree with an option to provide a specific degree for the newly created BTree. 
 * 
 * 
 * @author Derek Caplinger
 * @author Justin Mello
 * @author Matt Youngberg
 *
 */
public class BTree implements BTreeInterface
{
    /**
     * The size of the metadata for the {@link BTree} in bytes.
     */
    private final int METADATA_SIZE = Integer.BYTES + Long.BYTES + Integer.BYTES;  // t + root position + count

    /**
     * The {@link FileChannel} that the {@link BTree} is stored in.
     */
    private FileChannel fileChannel;

    /**
     * The {@link ByteBuffer} that is used to read and write metadata to the {@link BTree}.
     */
    private ByteBuffer metadataBuffer;

    /**
     * The {@link ByteBuffer} that is used to read and write {@link BTreeNode}s to the {@link BTree}.
     */
    private ByteBuffer nodeBuffer;
	
	//Metadata for each BTree file
	private long rootAddress;
	private int treeSize;
	private int degree;
	// TODO getNumNodes
	// TODO getHeight
	private BTreeNode root;

    /**
     * An {@link Iterator} for a {@link BTree} that traverses the tree in order.
     */
    private class BTreeInOrderIterator implements Iterator<TreeObject> {

        /**
         * A {@link Frame} represents a node in the {@link BTree} and maintains between invocations of
         * {@link BTreeInOrderIterator#next()} to know what to process next.
         */
        private class Frame {
            public BTreeNode node;
            public int index;
            public boolean processChildNext;

            public Frame(BTreeNode node) {
                this.node = node;
                this.index = 0;
                this.processChildNext = false;
            }
        }

        /**
         * A stack to maintain the state of the iterator.
         * <p>
         * One of the downsides of this implementation is that it requires a stack frame for every node in the tree that
         * is part of the ancestry of the node currently being processed.
         * <p>
         * TODO we need to profile for the memory footprint of this thing
         */
        private final Stack<Frame> stack;

        /**
         * Create a new {@link BTreeInOrderIterator} for the given {@link BTreeNode}.
         *
         * @param root  the {@link BTreeNode} that will act as the root for iteration. Should normally be
         *              {@link BTree#root}
         */
        public BTreeInOrderIterator(BTreeNode root) {
            this.stack = new Stack<>();

            Frame frame = new Frame(root);
            this.stack.push(frame);

            pushAllLeftNodes(root.childPositions[0]); // Initialize the stack with the leftmost path of the tree
        }

        /**
         * Determine if there are more elements to process.
         *
         * @return  true if there are more elements to process, false otherwise
         */
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /**
         * Get the next {@link TreeObject} in the {@link BTree}, using in-order traversal.
         *
         * @return  the next {@link TreeObject} in the {@link BTree}
         */
        @Override
        public TreeObject next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            Frame frame = stack.peek();

            if (frame.processChildNext) {
                // Process the child: update this node, traverse down, call next.
                frame.processChildNext = false;
                pushAllLeftNodes(frame.node.childPositions[frame.index]);
                return next();
            } else {
                // Index beyond keyCount determines if we're done processing the node. If so, pop and call again
                if (frame.index == frame.node.keyCount) {
                    stack.pop();
                    return next();
                }
                // Process the key
                TreeObject obj = frame.node.keys[frame.index];
                frame.processChildNext = true;
                frame.index++;
                return obj;
            }
        }

        /**
         * Push all the left nodes of the node at the given position onto the stack. Used as a way to get the stack in
         * a state to process the minimum element in the subtree rooted at the node at the given position.
         *
         * @param position  the position on disk of the node to push all the left nodes of onto the stack
         */
        private void pushAllLeftNodes(long position) {
            if (position == 0) {
                return;
            }

            Frame frame;
            try {
                frame = new Frame(diskRead(position));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            stack.push(frame);

            pushAllLeftNodes(frame.node.childPositions[0]);
        }
    }
    // END INNER CLASS

	
	/**
	 * Construct a BTree on disk if it does not exist 
	 * otherwise read metadata for a tree that does already exist
	 *
	 * @param fileName file name to store BTree on Disk
	 * @throws IOException
	 */
	public BTree(String fileName) throws IOException {
        // TODO merge with BTreeNode methods.
		BTreeNode r = new BTreeNode(new TreeObject[]);//dummy root node
		nodeSize = r.getDiskSize();
		buffer = ByteBuffer.allocateDirect(nodeSize);
		
		try {
			if (!btreeFile.exists()) {
				btreeFile.createNewFile();
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
				file = dataFile.getChannel();
				//initialize metadata
				degree = calcOptimalDegree();
				treeSize = 0;
				numNodes = 1;
				height = 1;
				writeMetaData();
			} else { 
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw").getChannel();
				file = dataFile.getChannel();
				readMetaData();
				root = diskRead(rootAddress);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Construct a new BTree on disk with the requested degree
	 *
	 * @param degree integer value for the desired degree of BTree
	 * @param fileName file name to store BTree on Disk
	 * @throws IOException
	 */
	public BTree(int degree, String fileName)throws IOException{
        // TODO merge with BTreeNode methods
		File btreeFile = new File(fileName);
		
		Node r = new Node(false);//dummy root node 
		nodeSize = r.getDiskSize();
		buffer = ByteBuffer.allocateDirect(nodeSize);
		
		try {
				btreeFile.createNewFile();
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
				file = dataFile.getChannel();
				this.degree = degree ;//a new file will is create with the specified degree
				treeSize = 0;
				numNodes = 1;
				height = 1;
				writeMetaData();
							 
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}
	
	

	@Override
	public long getSize() {
		return treeSize;
	}

	@Override
	public int getDegree() {
		return degree;
	}

	@Override
	public int getNumberOfNodes() {
        return -1;  // TODO do math
	}

	@Override
	public int getHeight() {
        return -1;  // TODO do math
	}

	@Override
	public void delete(long key) {
		// TODO
	}

	@Override
	public void insert(TreeObject obj) throws IOException {
        // TODO merge w/ BTreeNode methods
		if(root.keyCount == (2*degree-1)) {  // TODO make maxKeys on node public
			BTreeNode s = splitRoot();
			insertNonFull(s, obj);
		} else {
			insertNonFull(root,obj);
		}
		
	}
	
	private BTreeNode splitRoot() throws IOException {
        // TODO merge w/ BTreeNode methods
		BTreeNode newNode = new BTreeNode(true);//new root node needs to be written to file
        BTreeNode tempNode = root;// store root temporarily
		newNode.childPositions[0] = tempNode.address;//set address for newNodes first childPointer to the previous root we are splitting
		root = newNode;// change root node to new node
		rootAddress= newNode.address;// update root address
		splitChild(newNode, 0);//proceed to split the child node
		return newNode;
	}
	
	private void splitChild(BTreeNode parent, int childPointer ) throws IOException {
        BTreeNode y = diskRead(parent.childPositions[childPointer]);
        BTreeNode z = new BTreeNode(true);
		z.leaf = y.leaf;
		z.keyCount = degree - 1;
		for (int j = 0; j < degree - 2; j++) {
			z.keys[j] = y.keys[j + degree];
		}
		if (!y.leaf) {
			for (int j = 0; j < degree - 1;j++) {
				z.childPositions[j] = z.childPositions[j + degree];
			}
		}
		y.keyCount = degree - 1;
		for (int j = parent.keyCount; j > degree + 1; j--) {
			parent.childPositions[j+1] = parent.childPositions[j];
		}
		parent.childPositions[childPointer + 1] = z.address;  // TODO
		for (int j = parent.keyCount - 1; j > childPointer; j-- ) {
			parent.keys[j+1]= parent.keys[j];
		}
		parent.keys[childPointer] = y.keys[degree - 1];
		parent.keyCount++;
		diskWrite(y);  // TODO
		diskWrite(z);  // TODO
		diskWrite(parent);  // TODO, also potentially skip if root.
	}
	
	private void insertNonFull(BTreeNode x, TreeObject obj) throws IOException {
		int i = x.keyCount - 1;
		if (x.leaf) {
			while (i >= 0 && obj.getSubsequence() < x.keys[i].getSubsequence()){
				x.keys[i+1] = x.keys[i];
				i--;
			}
			x.keys[i+1] = obj;
			x.keyCount++;
			diskWrite(x);  // TODO
			treeSize++;  //increment tree size another key was inserted
		} else {
			while (i >= 0 && obj.getSubsequence() < x.keys[i].getSubsequence()){
				i--;
			}
			i++; 
			BTreeNode y = diskRead(x.childPositions[i]);
			if (y.keyCount == (2*degree -1)) {  // TODO merge w/ BTreeNode
				splitChild(x , i);
				if (obj.getSubsequence() > x.keys[i].getSubsequence()) {
					i ++; 
					y = diskRead(x.childPositions[i]);
				}
			}
			insertNonFull(y, obj);
		}
		
	}

    /**
     * Dumps the contents of the {@link BTree} to the given {@link PrintWriter}.
     *<p>
     * The format will be:
     * <p>
     * <code>
     * a 1543<p>
     * c 1115<p>
     * g 866<p>
     * t 1638<p>
     * </code>
     *
     * @param out           {@link PrintWriter} object representing output
     */
    @Override
    public void dumpToFile(PrintWriter out) {
        BTreeInOrderIterator iterator = new BTreeInOrderIterator(root);
        while (iterator.hasNext()) {
            TreeObject obj = iterator.next();
            String subSeqString = SequenceUtils.longToDnaString(obj.getSubsequence(), k);
            out.println(subSeqString + " " + obj.getCount());
        }
    }
	
	

	@Override
	public TreeObject search(long key) throws IOException {
		BTreeNode x = root;
		boolean isFound = false;
		while (!isFound) {
			int i =0;
			while (i < x.keyCount && key > x.keys[i].getSubsequence()) {
				i ++;
			}
			if ( i < x.keyCount && x.keys[i].getSubsequence() == key) {
				isFound = true; 
				return x.keys[i];
			} else if (x.leaf) {
				return null;
			} else {
				x = diskRead(x.childPositions[i]);
			}
		}
		return null; //should not reach this
	}
	
	private int calcOptimalDegree() {
		return Math.floorDiv((4095 -Integer.BYTES + TreeObject.getDiskSize()), (2*(TreeObject.getDiskSize()+Long.BYTES)));  // TODO revisit w/ revised metadata structure
	}
	
	/**
	 * Calculate the max number of keys for a node given the desired degree
	 * 
	 * @return return the max number of keys a node can store
	 */
	private int getMaxKeyCount() {  // TODO merge w/ BTreeNode
		return 2 * degree -1;
	}
	
	/**
	 * Calculate the max number of child pointers for a node given the desired degree
	 * 
	 * @return return the max number of child pointer a node can store
	 */
	private int getMaxChildCount () {  // TODO merge w/ BTreeNode
		return 2 * degree;
	}
	
    /**
     * Read the metadata from the data file.
     * @throws IOException
     */

	public void readMetaData() throws IOException{
		fileChannel.position(0);
		
		ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);
		
		tmpbuffer.clear();
		fileChannel.read(tmpbuffer);
		
		tmpbuffer.flip();
        // TODO revisit
		rootAddress = tmpbuffer.getLong();
		treeSize = tmpbuffer.getInt();
		degree = tmpbuffer.getInt();
	}
	
	/**
     * Write the metadata to the data file.
     * @throws IOException
     */
    public void writeMetaData() throws IOException {
        fileChannel.position(0);

        metadataBuffer.clear();
        metadataBuffer.putLong(rootAddress);
        metadataBuffer.putInt(treeSize);
        metadataBuffer.putInt(degree);

        metadataBuffer.flip();
        fileChannel.write(metadataBuffer);
    }

    /**
     * Reads a {@link BTreeNode} from the {@link BTree#fileChannel}.
     *
     * @param position      the byte offset for the node in the data file
     * @return              the {@link BTreeNode} read from the disk
     * @throws IOException  if an I/O error occurs
     */
    private BTreeNode diskRead(long position) throws IOException {
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
     * @throws IOException  if an I/O error occurs
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

        node.persisted = true;
    }
    
    /**
     * Cleanup at the end. Writes the root node and metadata and closes the data file.
     * @throws IOException
     */
    public void finishUp() throws IOException {
        diskWrite(root);  // TODO
        writeMetaData();
        fileChannel.close();
    }
}
