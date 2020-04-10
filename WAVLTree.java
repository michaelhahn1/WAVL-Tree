import java.util.LinkedList;
import java.util.function.Function;

/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree. (Haupler, Sen & Tarajan ‘15)
 *
 */

public class WAVLTree {
	private WAVLNode _root;
	private WAVLNode _min;
	private WAVLNode _max;

	public WAVLTree() {
		this._root = new ExternaLWavlNode(null);
		this._min = new ExternaLWavlNode(null);
		this._max = new ExternaLWavlNode(null);
	}

	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 *
	 */
	public boolean empty() {
		return this._root.getSubtreeSize() == 0;
	}

	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree otherwise,
	 * returns null
	 */
	public String search(int k) {
		WAVLNode node = getNodeByKey(k);
		return node.getValue();
	}

	/**
	 * search by key in the tree returns node iff node.isInnerNode() &&
	 * node.getKey() == key else returns ExternalNode
	 */

	private WAVLNode getNodeByKey(int key) {
		WAVLNode node = this._root;
		while (node.isInnerNode()) {
			int nodeKey = node.getKey();

			if (nodeKey == key)
				return node;

			node = nodeKey > key ? node.getLeftNode() : node.getRightNode();
		}
		return node;
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the WAVL tree. the tree must remain
	 * valid (keep its invariants). returns the number of rebalancing operations, or
	 * 0 if no rebalancing operations were necessary. returns -1 if an item with key
	 * k already exists in the tree.
	 */
	public int insert(int k, String i) {
		int steps = -1;
		WAVLNode node = insertNodeInTree(k, i);
		if (node != null) {
			updateMinMaxNodesAtInsert(node);
			steps = rebalanceTree(node);
			updateNodeBranchAfterRebalance(node.getParent());
		}
		return steps;
	}

	private void updateMinMaxNodesAtInsert(WAVLNode node) {
		if (!_min.isInnerNode() || _min.getKey() > node.getKey())
			_min = node;
		if (!_max.isInnerNode() || _max.getKey() < node.getKey())
			_max = node;
	}

	/**
	 * inserts node as a leaf in tree
	 */

	private WAVLNode insertNodeInTree(int key, String value) {
		if (empty()) {
			this._root = new WAVLNode(key, value, null);
			return this._root;
		}

		WAVLNode parent = getPotentialParent(key);
		return insertChildNode(key, value, parent);
	}

	/**
	 * finds a node in the tree suitable to be the parent of the node we will insert
	 */

	private WAVLNode getPotentialParent(int key) {
		WAVLNode parent = null;
		WAVLNode node = this._root;

		while (node.isInnerNode()) {
			parent = node;
			if (node.getKey() == key)
				return null;
			node = node.getKey() > key ? node.getLeftNode() : node.getRightNode();
		}
		return parent;
	}

	/**
	 * inserts a node as the son of another node
	 */

	private WAVLNode insertChildNode(int key, String value, WAVLNode parent) {
		if (parent == null || !parent.isInnerNode())
			return null;

		WAVLNode node = new WAVLNode(key, value, parent);
		if (parent.getKey() > key) {
			parent.setLeft(node);
		} else {
			parent.setRight(node);
		}

		return node;
	}

	/**
	 * rebalances the tree after insertion returns number of rebalancing steps
	 * needed for
	 */

	private int rebalanceTree(WAVLNode node) {
		int steps = 0;

		WAVLNode parent = node.getParent();

		while (parent != null) {
			if (parent.isLeftChild(node)) {
				parent.decLeftDiff();
			} else {
				parent.decRightDiff();
			}
			if (requiresPromotion(parent)) { // case1: promote
				parent.incLeftDiff();
				parent.incRightDiff();
				steps += 1;
			} else if (requiresSingleRotation(parent)) { // case2: single rotation
				singleRotate(node, parent);
				return steps + 2;
			} else if (requiresDoubleRotation(parent)) { // case3: double rotation
				if (parent.isLeftChild(node))
					doubleRotateFromLeft(node, parent);
				else
					doubleRotateFromRight(node, parent);
				return steps + 5;
			} else // case B: node is valid (1,1), no need for rebalancing
				return steps;

			node = parent;
			parent = parent.getParent();
		}
		return steps;
	}

	/**
	 * checks if case 1 of insert rebalancing is needed
	 */

	private boolean requiresPromotion(WAVLNode node) {
		return (node.getLeftDiff() == 0 || node.getRightDiff() == 0)
				&& (node.getLeftDiff() == 1 || node.getRightDiff() == 1);
	}

	/**
	 * checks if case 2 of insert rebalancing is needed
	 */

	private boolean requiresSingleRotation(WAVLNode node) {
		return (node.getLeftDiff() == 0 && node.getRightDiff() == 2 && node.getLeftNode().getRightDiff() == 2)
				|| (node.getRightDiff() == 0 && node.getLeftDiff() == 2 && node.getRightNode().getLeftDiff() == 2);
	}

	/**
	 * checks if case 3 of insert rebalancing is needed
	 */

	private boolean requiresDoubleRotation(WAVLNode node) {
		return (node.getLeftDiff() == 0 && node.getLeftNode().getLeftDiff() == 2)
				|| (node.getRightDiff() == 0 && node.getRightNode().getRightDiff() == 2);
	}

	private void singleRotate(WAVLNode node, WAVLNode parent) {
		parent.setLeftDiff(1);
		parent.setRightDiff(1);
		node.setLeftDiff(1);
		node.setRightDiff(1);
		rotate(node);
	}

	private void doubleRotateFromLeft(WAVLNode node, WAVLNode parent) {
		WAVLNode rightChild = node.getRightNode();
		parent.setLeftDiff(rightChild.getRightDiff());
		parent.setRightDiff(1);
		node.setLeftDiff(1);
		node.setRightDiff(rightChild.getLeftDiff());
		rightChild.setLeftDiff(1);
		rightChild.setRightDiff(1);
		rotate(rightChild);
		rotate(rightChild);
	}

	private void doubleRotateFromRight(WAVLNode node, WAVLNode parent) {
		WAVLNode leftChild = node.getLeftNode();
		parent.setRightDiff(leftChild.getLeftDiff());
		parent.setLeftDiff(1);
		node.setRightDiff(1);
		node.setLeftDiff(leftChild.getRightDiff());
		leftChild.setLeftDiff(1);
		leftChild.setRightDiff(1);
		rotate(leftChild);
		rotate(leftChild);
	}

	private void rotate(WAVLNode node) {
		WAVLNode parent = node.getParent();
		WAVLNode ancestor = parent.getParent();
		node.setParent(ancestor);
		if (ancestor == null)
			this._root = node;
		else {
			if (ancestor.isLeftChild(parent)) {
				ancestor.setLeft(node);
			} else {
				ancestor.setRight(node);
			}
		}
		parent.setParent(node);

		if (parent.isLeftChild(node)) {
			parent.setLeft(node.getRightNode());
			if (node.getRightNode() != null) {
				node.getRightNode().setParent(parent);
			}
			node.setRight(parent);

		} else {
			parent.setRight(node.getLeftNode());
			if (node.getLeftNode() != null) {
				node.getLeftNode().setParent(parent);
			}

			node.setLeft(parent);
		}
		updateNodeSize(parent);
		updateNodeSize(node);
	}

	private void updateNodeSize(WAVLNode node) {
		node.setSize(node.getLeftNode().getSubtreeSize() + node.getRightNode().getSubtreeSize() + 1);
	}

	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were needed. returns -1 if an
	 * item with key k was not found in the tree.
	 */
	public int delete(int k) {
		WAVLNode node = getNodeByKey(k);
		if (!node.isInnerNode())
			return -1;

		if (node.isBinary()) {
			WAVLNode successor = node.getRightNode().min();
			node.updateKeyAndValue(successor);
			node = successor;
		}

		WAVLNode parent = deleteNode(node);

		int steps = rebalanceAfterDeletion(parent);
		updateNodeBranchAfterRebalance(parent);
		updateMinMaxNodesAfterDeletion(k);
		return steps;
	}

	private void updateMinMaxNodesAfterDeletion(int deletedKey) {
		if (empty()) {
			this._min = new ExternaLWavlNode(null);
			this._max = new ExternaLWavlNode(null);
		} else {
			if (this._min.getKey() == deletedKey)
				this._min = this._root.min();

			if (this._max.getKey() == deletedKey)
				this._max = this._root.max();

		}
	}

	private WAVLNode deleteNode(WAVLNode node) {
		WAVLNode parent = node.getParent();
		WAVLNode child = node.getLeftNode().isInnerNode() ? node.getLeftNode() : node.getRightNode();
		child.setParent(parent);

		if (parent == null) {
			this._root = child;
		} else {
			if (parent.isLeftChild(node)) {
				parent.setLeft(child);
				parent.incLeftDiff();

			} else {
				parent.setRight(child);
				parent.incRightDiff();
			}
		}
		return parent;
	}

	/**
	 * rebalances the tree after deletion rebalancing returns number of steps needed
	 * for
	 */
	private int rebalanceAfterDeletion(WAVLNode node) {
		int steps = 0;
		while (node != null) {
			if (requiresDemotion(node)) {
				demote(node);
				steps += 1;
			} else if (requiresDoubleDemotion(node)) {
				doubleDemote(node);
				steps += 2;
			} else if (requiresSingleDeleteRotation(node)) {
				return steps + singleDeleteRotation(node) + 3;
			} else if (requiresDeleteDoubleRotation(node)) {
				doubleDeleteRotation(node);
				return steps + 5;
			}
			node = node.getParent();
		}

		return steps;
	}

	private void demote(WAVLNode node) {
		node.decLeftDiff();
		node.decRightDiff();
		if (node.getParent() != null) {
			if (node.getParent().isLeftChild(node))
				node.getParent().incLeftDiff();
			else
				node.getParent().incRightDiff();
		}
	}

	private void doubleDemote(WAVLNode node) {
		if (node.getLeftDiff() == 3)
			demote(node.getRightNode());
		else
			demote(node.getLeftNode());
		demote(node);
	}

	private int singleDeleteRotation(WAVLNode node) {
		if (node.getLeftDiff() == 3) {
			node.decLeftDiff();
			node.setRightDiff(node.getRightNode().getLeftDiff());
			node.getRightNode().incRightDiff();
			node.getRightNode().setLeftDiff(1);
			rotate(node.getRightNode());
		} else {
			node.decRightDiff();
			node.setLeftDiff(node.getLeftNode().getRightDiff());
			node.getLeftNode().incLeftDiff();
			node.getLeftNode().setRightDiff(1);
			rotate(node.getLeftNode());
		}
		if (node.getLeftDiff() == 2 && node.getRightDiff() == 2) {
			demote(node);
			return 1;
		}
		return 0;
	}

	private void doubleDeleteRotation(WAVLNode node) {
		if (node.getLeftDiff() == 3) {
			WAVLNode grandson = node.getRightNode().getLeftNode();

			node.setLeftDiff(1);
			node.setRightDiff(grandson.getLeftDiff());

			node.getRightNode().decRightDiff();
			node.getRightNode().setLeftDiff(grandson.getRightDiff());

			grandson.setLeftDiff(2);
			grandson.setRightDiff(2);

			rotate(grandson);
			rotate(grandson);

		} else {
			WAVLNode grandson = node.getLeftNode().getRightNode();

			node.setRightDiff(1);
			node.setLeftDiff(grandson.getRightDiff());

			node.getLeftNode().decLeftDiff();
			node.getLeftNode().setRightDiff(grandson.getLeftDiff());

			grandson.setRightDiff(2);
			grandson.setLeftDiff(2);

			rotate(grandson);
			rotate(grandson);

		}
	}

	/**
	 * checks if case 1 of rebalancing after deletion is needed
	 */

	private boolean requiresDemotion(WAVLNode node) {
		return (node.getRightDiff() == 3 && node.getLeftDiff() == 2)
				|| (node.getLeftDiff() == 3 && node.getRightDiff() == 2)
				|| (node.getLeftDiff() == 2 && node.getRightDiff() == 2 && node.isLeaf());
	}

	/**
	 * checks if case 2 of rebalancing after deletion is needed
	 */

	private boolean requiresDoubleDemotion(WAVLNode node) {
		return (node.getRightDiff() == 3 && node.getLeftDiff() == 1 && node.getLeftNode().getLeftDiff() == 2
				&& node.getLeftNode().getRightDiff() == 2)
				|| (node.getLeftDiff() == 3 && node.getRightDiff() == 1 && node.getRightNode().getRightDiff() == 2
						&& node.getRightNode().getLeftDiff() == 2);
	}

	/**
	 * checks if case 3 of rebalancing after deletion is needed
	 */

	private boolean requiresSingleDeleteRotation(WAVLNode node) {
		return (node.getRightDiff() == 3 && node.getLeftDiff() == 1 && node.getLeftNode().getLeftDiff() == 1)
				|| (node.getLeftDiff() == 3 && node.getRightDiff() == 1 && node.getRightNode().getRightDiff() == 1);
	}

	/**
	 * checks if case 4 of rebalancing after deletion is needed
	 */

	private boolean requiresDeleteDoubleRotation(WAVLNode node) {
		return (node.getRightDiff() == 3 && node.getLeftDiff() == 1 && node.getLeftNode().getLeftDiff() == 2)
				|| (node.getLeftDiff() == 3 && node.getRightDiff() == 1 && node.getRightNode().getRightDiff() == 2);
	}

	private void updateNodeBranchAfterRebalance(WAVLNode node) {
		while (node != null) {
			updateNodeSize(node);
			node = node.getParent();
		}
	}

	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree, or null if
	 * the tree is empty
	 */
	public String min() {
		return this._min.getValue();
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree, or null if the
	 * tree is empty
	 */
	public String max() {
		return this._max.getValue();
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree, or an empty array
	 * if the tree is empty.
	 */
	public int[] keysToArray() {
		if (this.empty())
			return new int[0];

		Integer[] arr = new Integer[this._root.getSubtreeSize()];
		Function<WAVLNode, Integer> filler = (WAVLNode node) -> {
			return node.getKey();
		};
		fillArrayInOrder(arr, filler);
		int[] results = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
			results[i] = arr[i];
		return results;
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty.
	 */
	public String[] infoToArray() {
		if (this.empty())
			return new String[0];

		String[] arr = new String[this._root.getSubtreeSize()];
		Function<WAVLNode, String> filler = (WAVLNode node) -> {
			return node.getValue();
		};
		fillArrayInOrder(arr, filler);
		return arr;
	}

	/**
	 * Algorithm for a non-recursive in-order traversal returns an array in which
	 * nodes are sorted by their keys
	 */

	private <T> void fillArrayInOrder(T[] arr, Function<WAVLNode, T> filler) {
		int index = 0;
		WAVLNode node = this._root;
		LinkedList<WAVLNode> stack = new LinkedList<>();
		addLeftBranch(node, stack);

		while (!stack.isEmpty()) {
			node = stack.removeLast();
			arr[index] = filler.apply(node);
			index++;
			addLeftBranch(node.getRightNode(), stack);
		}
	}

	/**
	 * adds the left branch of a tree to a LinkedList each node is added last
	 */

	private void addLeftBranch(WAVLNode node, LinkedList<WAVLNode> stack) {
		while (node.isInnerNode()) {
			stack.addLast(node);
			node = node.getLeftNode();
		}
	}

	/**
	 * public int size()
	 *
	 * Returns the number of inner nodes in the tree.
	 *
	 */
	public int size() {
		return this._root.getSubtreeSize();
	}

	/**
	 * public WAVLNode getRoot()
	 *
	 * Returns the root WAVL node, or null if the tree is empty
	 *
	 */
	public WAVLNode getRoot() {
		return this._root;
	}

	/**
	 * public int select(int i)
	 *
	 * Returns the value of the i'th smallest key (return -1 if tree is empty)
	 * Example 1: select(1) returns the value of the node with minimal key Example
	 * 2: select(size()) returns the value of the node with maximal key Example 3:
	 * select(2) returns the value 2nd smallest minimal node, i.e the value of the
	 * node minimal node's successor
	 *
	 */
	public String select(int i) {
		i--;

		if (empty() || this.size() < i + 1)
			return null;

		WAVLNode node = this._root;

		while (i >= 0) {
			int leftSize = node.getLeftNode().getSubtreeSize();
			if (i == leftSize)
				return node.getValue();
			if (i < leftSize)
				node = node.getLeftNode();
			else {
				i = i - leftSize - 1;
				node = node.getRightNode();
			}
		}
		return null;
	}

	class WAVLNode {
		private int _key;
		private String _value;
		private WAVLNode _left;
		private WAVLNode _right;
		private WAVLNode _parent;
		private int _leftDiff;
		private int _rightDiff;
		private int _size;

		public WAVLNode(int key, String value, WAVLNode parent) {
			this._key = key;
			this._value = value;
			this._left = new ExternaLWavlNode(this);
			this._right = new ExternaLWavlNode(this);
			this._parent = parent;
			this._leftDiff = 1;
			this._rightDiff = 1;
			this._size = 1;
		}

		public WAVLNode(WAVLNode parent) {
			this._key = -1;
			this._value = null;
			this._parent = parent;
			this._left = null;
			this._right = null;
			this._leftDiff = 1;
			this._rightDiff = 1;
			this._size = 0;
		}

		public int getRank() {
			return getLeftDiff() + getLeftNode().getRank();
		}

		public int getKey() {
			return this._key;

		}

		public String getValue() {
			return this._value;
		}

		private WAVLNode getLeftNode() {
			return this._left;
		}
		
		public WAVLNode getLeft() {			
			WAVLNode left = getLeftNode();
			return left == null || !left.isInnerNode() ? null : left;
		}

		public void setLeft(WAVLNode node) {
			this._left = node;
		}

		private WAVLNode getRightNode() {
			return this._right;
		}

		public WAVLNode getRight() {			
			WAVLNode right = getRightNode();
			return right == null || !right.isInnerNode() ? null : right;
		}
		
		public void setRight(WAVLNode node) {
			this._right = node;
		}

		public WAVLNode getParent() {
			return this._parent;
		}

		public void setParent(WAVLNode parent) {
			this._parent = parent;
		}

		public void setRightDiff(int rightDiff) {
			this._rightDiff = rightDiff;
		}

		public int getRightDiff() {
			return this._rightDiff;
		}

		public void setLeftDiff(int leftDiff) {
			this._leftDiff = leftDiff;
		}

		public int getLeftDiff() {
			return this._leftDiff;
		}

		/**
		 * returns true if node is inner node always returns true, external nodes have a
		 * different class
		 */

		public boolean isInnerNode() {
			return true;
		}

		/**
		 * returns the size of the subtree in which node is the root
		 */

		public int getSubtreeSize() {
			return this._size;
		}

		/**
		 * sets the size of the subtree in which node is the root
		 */

		public void setSize(int size) {
			this._size = size;
		}

		/**
		 * returns true if node has to children and false otherwise
		 */

		public boolean isBinary() {
			return getLeftNode().isInnerNode() && getRightNode().isInnerNode();
		}

		/**
		 * returns true if node is a leaf and false otherwise
		 */

		public boolean isLeaf() {
			return !getLeftNode().isInnerNode() && !getRightNode().isInnerNode();
		}

		/**
		 * decreases by 1 the rank difference between a node and his left child
		 */

		public void decLeftDiff() {
			this._leftDiff--;
		}

		/**
		 * decreases by 1 the rank difference between a node and his right child
		 */

		public void decRightDiff() {
			this._rightDiff--;
		}

		/**
		 * increases by 1 the rank difference between a node and his right child
		 */

		public void incLeftDiff() {
			this._leftDiff++;
		}

		/**
		 * increases by 1 the rank difference between a node and his right child
		 */

		public void incRightDiff() {
			this._rightDiff++;
		}

		/**
		 * returns true if the left child of a node is the input node otherwise, returns
		 * false
		 */

		public boolean isLeftChild(WAVLNode node) {
			return this.getLeftNode() == node;
		}

		/**
		 * returns the node with the minimal key in the subtree in which node is the
		 * root
		 */

		public WAVLNode min() {
			WAVLNode node = this;

			while (node.getLeftNode().isInnerNode())
				node = node.getLeftNode();

			return node;
		}

		/**
		 * returns the node with the maximal key in the subtree in which node is the
		 * root
		 */

		public WAVLNode max() {
			WAVLNode node = this;

			while (node.getRightNode().isInnerNode())
				node = node.getRightNode();

			return node;
		}

		public void updateKeyAndValue(WAVLNode node) {
			this._key = node.getKey();
			this._value = node.getValue();
		}
	}

	/**
	 * ExternaLWavlNode represent external WAVL nodes extends WAVLNode
	 */

	class ExternaLWavlNode extends WAVLNode {
		public ExternaLWavlNode(WAVLNode parent) {
			super(parent);
		}

		@Override
		public int getRank() {
			return -1;
		}

		@Override
		public boolean isInnerNode() {
			return false;
		}

		@Override
		public int getSubtreeSize() {
			return 0;
		}
	}
}
