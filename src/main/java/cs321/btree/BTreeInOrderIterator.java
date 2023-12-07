package cs321.btree;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * An {@link Iterator} for a {@link BTree} that traverses the tree in order.
 * <p>
 * Note that this implementation does not throw a {@link java.util.ConcurrentModificationException} if the tree is
 * modified. This decision was made because it would introduce a degree of coupling between the BTree and this class,
 * and the project specification does not introduce a circumstance in the command line programs where a BTree would be
 * modified while being iterated over. If we were to build to a broader specification, we would implement this
 * exception, but for the sake of time, we did not.
 *
 * @author Derek Caplinger
 * @author Matt Youngberg
 */
class BTreeInOrderIterator implements Iterator<TreeObject> {

    /**
     * A {@link NodeFrame} represents a node in the {@link BTree} that's maintained in memory between invocations of
     * {@link BTreeInOrderIterator#next()} to know what to process next. Since height is assumed to be small and
     * the footprint of our own stack not too large, this is a fast approach that is better than having to seek to
     * the next node from root every time.
     */
    private static class NodeFrame {

        /**
         * The {@link BTreeNode} that this {@link NodeFrame} represents.
         */
        public BTreeNode node;

        /**
         * The index of the next key to process in the {@link BTreeNode} that this {@link NodeFrame} represents.
         */
        public int index;

        /**
         * A flag to indicate whether the next thing to process is a child of the {@link BTreeNode} that this
         * {@link NodeFrame} represents.
         */
        public boolean processChildNext;

        /**
         * Construct a new {@link NodeFrame} for the given {@link BTreeNode}.
         *
         * @param node  the {@link BTreeNode} to construct a {@link NodeFrame} for
         */
        public NodeFrame(BTreeNode node) {
            this.node = node;
            this.index = 0;
            this.processChildNext = false;
        }
    }

    /**
     * A stack to maintain the state of the iterator between invocations of {@link BTreeInOrderIterator#next()}.
     * <p>
     * One of the downsides of this implementation is that it requires a stack frame for every node in the tree that
     * is part of the ancestry of the node currently being processed. However, because BTrees tend to be very shallow
     * in height, even for big <code>n</code> this is not a huge concern.
     */
    private final Stack<NodeFrame> stack;

    /**
     * A reference to the {@link BTree} provided upon construction for the sake of invoking
     * {@link BTree#getNode(long)}.
     */
    private final BTree btree;

    /**
     * Create a new {@link BTreeInOrderIterator} for the given {@link BTreeNode}.
     *
     * @param btree the {@link BTreeNode} that will act as the subject for iteration.
     */
    public BTreeInOrderIterator(BTree btree) throws IOException {
        this.stack = new Stack<>();
        this.btree = btree;

        if (btree.getSize() > 0) {
            NodeFrame nodeFrame = new NodeFrame(btree.root);
            this.stack.push(nodeFrame);
            pushAllLeftNodes(btree.root.childPositions[0]); // Initialize the stack with the leftmost path of the tree
        }
    }

    /**
     * Determine if there are more elements to process.
     *
     * @return true if there are more elements to process, false otherwise
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

        NodeFrame frame = stack.peek();

        if (frame.node.leaf) {
            TreeObject obj = frame.node.keys[frame.index];
            frame.index++;
            if (frame.index == frame.node.keyCount) {
                popFrameAndFinishedAncestors();
            }
            return obj;
        } else {  // Guaranteed to have children
            if (frame.processChildNext) {  // if not a leaf, there is guaranteed to be a child at position index + 1
                try {
                    pushAllLeftNodes(frame.node.childPositions[frame.index + 1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                frame.processChildNext = false;
                frame.index++;
                return next();
            } else {  // process key at index
                TreeObject obj = frame.node.keys[frame.index];
                frame.processChildNext = true;
//                frame.index++;
                return obj;
            }
        }
    }

    /**
     * Push all the left nodes of the node at the given position onto the stack. Used as a way to get the stack in
     * a state to process the minimum element in the subtree rooted at the node at the given position.
     *
     * @param position  the position on disk of the node to push all the left nodes of onto the stack
     */
    private void pushAllLeftNodes(long position) throws IOException {
        if (position == 0) {
            return;
        }

        NodeFrame nodeFrame;
        nodeFrame = new NodeFrame(btree.getNode(position));

        stack.push(nodeFrame);

        pushAllLeftNodes(nodeFrame.node.childPositions[0]);
    }

    /**
     * Pop a given node and all its finished ancestors off the stack.
     */
    private void popFrameAndFinishedAncestors() {
        stack.pop();  // Pop the node

        while (!stack.isEmpty()) {
            NodeFrame parent = stack.peek();
            if (parent.index == parent.node.keyCount) {
                stack.pop();
            } else {
                break;  // Parent has more keys or children to process
            }
        }
    }
}
