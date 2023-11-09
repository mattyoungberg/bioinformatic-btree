package cs321.btree;

import cs321.create.SequenceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class BTree implements BTreeInterface
{

    /**
     * The size of the metadata for the {@link BTree} in bytes.
     */
    private final int METADATA_SIZE = Integer.BYTES + Long.BYTES;  // t + root position

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

    /**
     * The root node of the {@link BTree}.
     */
    private BTreeNode root;

    /**
     * The length of the DNA subsequences that each {@link TreeObject} encodes in a `long`.
     */
    private int k;  // sequence length

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
        private void pushAllLeftNodes(long position){
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

    public BTree(String fileName) {

    }

    public BTree(int t, String fileName) {

    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int getNumberOfNodes() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void delete(long key) {

    }

    @Override
    public void insert(TreeObject obj) throws IOException {

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
        return null;
    }

    /**
     * Not part of the BTreeInterface, but required by given tests.
     *
     * @return The array of keys in sorted order.
     */
    public long[] getSortedKeyArray() {
        return null;
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

}
