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
	private int METADATA_SIZE = Long.BYTES + (4 * Integer.BYTES);
	private long nextDiskAddress = METADATA_SIZE;
	private FileChannel file;
	private ByteBuffer buffer;
	private int nodeSize;
	
	//Metadata for each BTree file
	private long rootAddress = METADATA_SIZE;
	private int treeSize;
	private int degree;
	private int numNodes; 
	private int height;
	
	
	private Node root;
	
	/**
	 * Construct a BTree on disk if it does not exist 
	 * otherwise read metadata for a tree that does already exist
	 *
	 * @param fileName file name to store BTree on Disk
	 * @throws IOException
	 */
	public BTree(File fileName)throws IOException{
		
		Node r = new Node(false);//dummy root node 
		nodeSize = r.getDiskSize();
		buffer = ByteBuffer.allocateDirect(nodeSize);
		
		try {
			if (!fileName.exists()) {
				fileName.createNewFile();
				RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
				file = dataFile.getChannel();
				//initialize metadata
				degree = calcOptimalDegree();
				treeSize = 0;
				numNodes = 1;
				height = 1;
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
	
	/**
	 * Construct a new BTree on disk with the requested degree
	 *
	 * @param degree integer value for the desired degree of BTree
	 * @param fileName file name to store BTree on Disk
	 * @throws IOException
	 */
	public BTree(File fileName, int degree)throws IOException{
		
		Node r = new Node(false);//dummy root node 
		nodeSize = r.getDiskSize();
		buffer = ByteBuffer.allocateDirect(nodeSize);
		
		try {
				fileName.createNewFile();
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
		if(root.numKeys == (2*degree-1)) {
			Node s = splitRoot();
			insertNonFull(s, obj);
		} else {
			insertNonFull(root,obj);
		}
		
	}
	
	private Node splitRoot() throws IOException {
		Node newNode = new Node(false);//new root node will not be written to memory until finish is called and BTree file is closed
		Node tempNode = root;// store root temporarily 
		newNode.childPointers[0] = nextDiskAddress;//set address for node we are splitting
		tempNode.address = nextDiskAddress;// update address for new child node
		diskWrite(tempNode);//write new child node to disk 
		nextDiskAddress += nodeSize; // increment next disk address so the next address does not overwrite a previous node
		root = newNode;// change root node to new node
		splitChild(newNode, 0);
		return newNode;
	}
	
	private void splitChild(Node parent, int childPointer ) throws IOException {
		Node y = diskRead(parent.childPointers[childPointer]);
		Node z = new Node(true);
		z.isLeaf = y.isLeaf;
		z.numKeys = degree - 1;
		for (int j = 0; j < degree - 2; j++) {
			z.treeObjects[j] = y.treeObjects[j + degree];
		}
		if (!y.isLeaf) {
			for (int j = 0; j < degree - 1;j++) {
				z.childPointers[j] = z.childPointers[j + degree];
			}
		}
		y.numKeys = degree - 1;
		for (int j = parent.numKeys; j > degree + 1; j--) {
			parent.childPointers[j+1] = parent.childPointers[j];
		}
		parent.childPointers[childPointer + 1] =z.address;
		for (int j = parent.numKeys - 1; j > childPointer; j-- ) {
			parent.treeObjects[j+1]= parent.treeObjects[j];
		}
		parent.treeObjects[childPointer] = y.treeObjects[degree - 1];
		parent.numKeys++;
		diskWrite(y);
		diskWrite(z);
		diskWrite(parent);
	}
	
	private void insertNonFull(Node x, TreeObject obj) throws IOException {
		int i = x.numKeys - 1;
		if (x.isLeaf) {
			while (i >= 0 && obj.getValue()<x.treeObjects[i].getValue()){
				x.treeObjects[i+1] = x.treeObjects[i];
				i --;
			}
			x.treeObjects[i+1] = obj;
			x.numKeys ++;
			diskWrite(x);
		} else {
			while (i >= 0 && obj.getValue()<x.treeObjects[i].getValue()){
				i --; 
			}
			i++; 
			Node y = diskRead(x.childPointers[i]);
			if (y.numKeys == (2*degree -1)) {
				splitChild(x , i);
				if (obj.getValue() > x.treeObjects[i].getValue()) {
					i ++; 
					y = diskRead(x.childPointers[i]);
				}
			}
			insertNonFull(y,obj);	
		}
		
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
	
	private int calcOptimalDegree() {
		return Math.floorDiv((4095 -Integer.BYTES + TreeObject.getDiskSize()), (2*(TreeObject.getDiskSize()+Long.BYTES))); 
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
		treeSize = tmpbuffer.getInt();
		degree = tmpbuffer.getInt();
		numNodes = tmpbuffer.getInt();
		height = tmpbuffer.getInt();
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
        tmpbuffer.putInt(treeSize);
        tmpbuffer.putInt(degree);
        tmpbuffer.putInt(numNodes);
        tmpbuffer.putInt(height);

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
        	int frequency = buffer.getInt();
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
        Node x = new Node(false);
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
        	buffer.putInt(x.treeObjects[i].getFrequency());
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
		 * This constructor constructs an empty node 
		 * with the arrays for the keys and the child pointers set to their max values
		 * The node is generated assuming it is not a leaf and the current number of keys stored is set to 0  
		 * 
		 * @param onDisk a boolean to signal if this node is on disk or not 
		 */
		public Node(boolean onDisk) {
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
