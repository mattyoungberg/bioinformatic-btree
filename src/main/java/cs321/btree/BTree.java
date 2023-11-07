package cs321.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BTree implements BTreeInterface
{
	//write in metadata? if so metada size and read and write methods need adjustment
	private int treeSize;
	private int degree;
	private int numNodes; 
	private int height; 
	
	private int METADATA_SIZE = Long.BYTES;
	private long nextDiskAddress = METADATA_SIZE;
	private FileChannel file;
	private ByteBuffer buffer;
	private int nodeSize;
	
	private long rootAddress = METADATA_SIZE;
	private Node root;
	
	/**
	 * Construct a BTree on disk if it does not exist 
	 * otherwise read metadata for a tree that does already exist
	 *
	 * @param fileName file name to store BTree on Disk
	 * @param degree desired degree for the BTree
	 * @throws IOException
	 */
	public BTree(File fileName, int degree)throws IOException{
		this.degree = degree;
		Node r = new Node(this.degree, false);//dummy root node 
		nodeSize = r.getDiskSize();
		buffer = ByteBuffer.allocateDirect(nodeSize);
		
		try {
			if (!fileName.exists()) {
				fileName.createNewFile();
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
				file = dataFile.getChannel();
				writeMetaData();
			} else { 
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
				file = dataFile.getChannel();
				readMetaData();
				root = diskRead(rootAddress);
			}
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
		
		return numNodes;
	}

	@Override
	public int getHeight() {
		
		return height;
	}

	@Override
	public void delete(long key) {
		// TODO Auto-generated method stub 
		// does not need to be implemented 
		
	}

	@Override
	public void insert(TreeObject obj) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	private void splitRoot() {
		// TODO generate private support method to split root node when needed. 
	}
	
	private void splitChild() {
		// TODO generate private support method to split child node when needed. 
	}
	
	private void insertNonFull() {
		// TODO generate non-full insertion support method 
	}
	
	

	@Override
	public void dumpToFile(PrintWriter out) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	

	@Override
	public TreeObject search(long key) throws IOException {
		Node x = root;
		boolean isFound = false;
		while (!isFound) {
			int i =0;
			while (i < x.numKeys && key > x.treeObjects[i].getValue()) {
				i ++;
			}
			if ( i < x.numKeys && x.treeObjects[i].getValue() == key) {
				isFound = true; 
				return x.treeObjects[i];
			} else if (x.isLeaf) {
				return null;
			} else {
				x = diskRead(x.childPointers[i]);
			}
		}
		return null; //should not reach this
	}
	
	/**
	 * Calculate the max number of keys for a node given the desired degree
	 * 
	 * @return return the max number of keys a node can store
	 */
	private int getMaxKeyCount() {
		return 2 * degree -1;
	}
	
	/**
	 * Calculate the max number of child pointers for a node given the desired degree
	 * 
	 * @return return the max number of child pointer a node can store
	 */
	private int getMaxChildCount () {
		return 2 * degree;
	}
	
    /**
     * Read the metadata from the data file.
     * @throws IOException
     */

	public void readMetaData() throws IOException{
		file.position(0);
		
		ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);
		
		tmpbuffer.clear();
		file.read(tmpbuffer);
		
		tmpbuffer.flip();
		rootAddress = tmpbuffer.getLong();
	}
	
	/**
     * Write the metadata to the data file.
     * @throws IOException
     */
    public void writeMetaData() throws IOException {
        file.position(0);

        ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

        tmpbuffer.clear();
        tmpbuffer.putLong(rootAddress);

        tmpbuffer.flip();
        file.write(tmpbuffer);
    }
    
    /**
     * Reads a node from the disk and returns a Node object built from it.
     * @param diskAddress the byte offset for the node in the data file
     * @return the Node object
     * @throws IOException
     */
    public Node diskRead(long diskAddress) throws IOException {
        if (diskAddress == 0) return null;
        
        file.position(diskAddress);
        buffer.clear();

        file.read(buffer);
        buffer.flip();

        byte flag = buffer.get(); // read a byte for isLeaf
        boolean leaf = false;
        if (flag == 1)
            leaf = true;
        
        int n = buffer.getInt(); // read number of keys
        /* create array of appropriate size for keys 
         * read the stored value and frequency count for each tree object
         * create tree object and insert into appropriate position  
         */
        TreeObject[] keys = new TreeObject[getMaxKeyCount()];  
        for (int i = 0; i < n; i++) {
        	long value = buffer.getLong();
        	long frequency = buffer.getLong();
        	keys[i] = new TreeObject(value, frequency);
        }
        /*
         * create array of appropriate length for child pointers 
         * fill in the array with values read from disk
         */
        long[] pointers = new long[getMaxChildCount()];
        for (int j = 0; j < n + 1; j++) {
        	pointers[j] = buffer.getLong(); 
        }
        //create new node and assign values accordingly 
        Node x = new Node(degree, false);
        x.isLeaf = leaf;
        x.numKeys = n;
        x.treeObjects = keys;
        x.childPointers = pointers;
        x.address = diskAddress;

        return x;
    }
    
    /**
     * Writes a node to the disk at the specified disk offset *in the Node object).
     * @param x the Node to write
     * @throws IOException
     */
    public void diskWrite(Node x) throws IOException {
        file.position(x.address);
        buffer.clear();
        //write is leaf
        if (x.isLeaf) 
            buffer.put((byte)1);
        else
            buffer.put((byte)0);
        //write numKeys
        buffer.putInt(x.numKeys);
        //write TreeObjectArray;
        for (int i = 0; i < x.numKeys; i++) {
        	buffer.putLong(x.treeObjects[i].getValue());
        	buffer.putLong(x.treeObjects[i].getFrequency());
        }
        //write childPointers
        for (int j = 0; j< x.numKeys + 1; j++) {
        	buffer.putLong(x.childPointers[j]);
        }
        
        buffer.flip();
        file.write(buffer);
    }
    
    /**
     * Cleanup at the end. Writes the root node and metadata and closes the data file.
     * @throws IOException
     */
    public void finishUp() throws IOException {
        diskWrite(root);
        writeMetaData();
        file.close();
    }
    
	
	
	private class Node {
		private long address;
		private boolean isLeaf;
		private int numKeys;
		
		private TreeObject[] treeObjects;
		private long[] childPointers;
		
		/**
		 * This constructor takes in a degree and constructs an empty node 
		 * with the arrays for the keys and the child pointers set to their max values
		 * The node is generate assuming it is not a leaf and the current number of keys stored is set to 0  
		 * 
		 * @param degree the degree (t) of the BTree this node belongs to
		 * @param onDisk a boolean to signal if this node is on disk or not 
		 */
		public Node(int degree, boolean onDisk) {
			this.treeObjects = new TreeObject[getMaxKeyCount()];
			this.childPointers = new long[getMaxChildCount()];
			isLeaf = false;
			numKeys = 0;
			if (onDisk) {
				address = nextDiskAddress;
				nextDiskAddress += nodeSize;
			}
		}
		

		/**
		 * Calculate the size of a node as stored on disk (in bytes)
		 * 
		 * @return size of the node on disk
		 */
		public int getDiskSize() {
			//boolean stored as 1 byte since it is not defined in java
			return 1 + Integer.BYTES + TreeObject.getDiskSize() * getMaxKeyCount() + Long.BYTES * getMaxChildCount(); 
		}

		/**
		 * @return the isLeaf
		 */
		public boolean isLeaf() {
			return isLeaf;
		}
		
		/**
		 * @param isLeaf the isLeaf to set
		 */
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}

		/**
		 * @return the numKeys
		 */
		public int getNumKeys() {
			return numKeys;
		}
		/**
		 * @param numKeys the numKeys to set
		 */
		public void setNumKeys(int numKeys) {
			this.numKeys = numKeys;
		}
	
	}//end Node inner class

}
