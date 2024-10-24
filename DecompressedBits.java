import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// this class's purpose is to take in the information from the SimpleHuffProcessor class (uncompressed method)
// and use it to generate new file using SimpleHuffProcessor OutputStream.
public class DecompressedBits {
	
	// instance variables
	private BitInputStream inStream;
	private int[] wordFreq;
	private int[] treeArray;
	private TreeNode tree;
	private Map<Integer, String> map;
	private int treeSize;
	private int numBits;
	private int format;
	
	// constructor for DecompressedBits class
	// takes in BitsInputStream, BitOutputStreams and the format number
	public DecompressedBits(BitInputStream in, BitOutputStream out, int format) throws IOException {
		inStream = in;
		treeArray = new int[IHuffConstants.ALPH_SIZE + 1];
		this.format = format;
		// checking the format to see if the file is under STORE_COUNTS or STORE_TREE format
		if (format == IHuffConstants.STORE_COUNTS) {
			// if it is STORE_COUNTS format, create an array from its header and create a
			// tree out of that array
			createArray();
			createTFA();
		} else if (format == IHuffConstants.STORE_TREE) {
			// if it is STORE_TREE format, create a tree directly from the data from its header
			treeSize = inStream.readBits(IHuffConstants.BITS_PER_INT);
			tree = createTFT(tree, inStream);
		}
		// creating a map for debugging purposes (data, chars and frequencies)
		createMap();
		// creating a pointer node to go through the tree;
		TreeNode node = tree;
		numBits = 0;
		int curBit = inStream.readBits(1);
		while (curBit != -1) {
			if (curBit == 1) {
				node = node.getRight();
			} else if (curBit == 0) {
				node = node.getLeft();
			}
			if (node.isLeaf()) {
				// not adding the PSEUDO because uncompress wouldn't contain PSEUDO;
				if (node.getValue() != IHuffConstants.PSEUDO_EOF) {
					out.writeBits(IHuffConstants.BITS_PER_WORD, node.getValue());
					//creating an array of frequencies as reading the file for debugging purposes.
					treeArray[node.getValue()]++;
					// adding BITS_PER_WORD (8) because each word/char will be 8 bits
					numBits += IHuffConstants.BITS_PER_WORD;
				} else {
					break;
				}
				// the pointer node return back to the root
				node = tree;
			}
			// read in next bit in the file
			curBit = inStream.readBits(1);
		}
		inStream.close();
		out.close();
	}
	
	// call this method in SimpleHuffProcessor to get the total number of bits
	// of the de-compressed file
	public int getBits() {
		return numBits;
	}
	
	// helper method to create an array out of the compressed file's data
	// if the file format is under STORE_COUNTS only
	private void createArray() throws IOException {
		wordFreq = new int[IHuffConstants.ALPH_SIZE + 1];
		for (int i = 0; i <= IHuffConstants.ALPH_SIZE; i++) {
			if (i == IHuffConstants.ALPH_SIZE) {
				wordFreq[i] = 1;
			} else {
				wordFreq[i] = inStream.readBits(IHuffConstants.BITS_PER_INT);
			}
		}
	}
	
	// method to create a tree out of the array that were created
	// (STORE_COUNTS format)
	private void createTFA() {
		PriorityQueue314<TreeNode> queue = new PriorityQueue314<>();
		for (int i = 0; i < IHuffConstants.ALPH_SIZE + 1; i++) {
			if (wordFreq[i] > 0) {
				queue.enqueue(new TreeNode(i, wordFreq[i]));
			}
		}
		while (queue.size() > 1) {
			TreeNode first = queue.dequeue();
			TreeNode second = queue.dequeue();
			TreeNode combined = new TreeNode(first, first.getFrequency() + second.getFrequency(), second);
			queue.enqueue(combined);
		}
		tree = queue.dequeue();
	}
	
	// method to create a tree directly from the data of the file's header
	// (STORE_TREE)
	private TreeNode createTFT(TreeNode node, BitInputStream input) throws IOException {
		int bit = input.readBits(1);
		if (bit == 0) {
			// creating a new node (internal node)
			node = new TreeNode(-1, 0);
			node.setLeft(createTFT(node.getLeft(), input));
			node.setRight(createTFT(node.getRight(), input));
			return node;
		} else {
			// if it is not an internal node
			// create a new node with no children
			return new TreeNode(input.readBits(IHuffConstants.BITS_PER_WORD + 1), 0);
		}
	}
	
	
	// method to traverse through the tree to find the new bit data of the new file
	private void mapRecursion(TreeNode node, Map<Integer, String> map, String s) {
		if (node.isLeaf()) {
			map.put(node.getValue(), s);
		} else {
			mapRecursion(node.getLeft(), map, s + '0');
			mapRecursion(node.getRight(), map, s + '1');
		}
	}
	
	// method to actually create a map using the above method;
	private void createMap() {
		map = new HashMap<>();
		mapRecursion(tree, map, "");
	}

	// method to return string to put in the parameter of showString in the SimpleHuffProcessor class
	public String showDebug() {
		Map<Integer, String> sortedMap = new TreeMap<>(map);
		StringBuilder s = new StringBuilder();
		for (int k : sortedMap.keySet()) {
			char c = (char) k;
			if (format == IHuffConstants.STORE_COUNTS) {
				s.append("" + k + "  " + c + "  " + map.get(k) + " frequencies: " + wordFreq[k] + "\n");
			} else if (format == IHuffConstants.STORE_TREE) {
				s.append("" + k + "  " + c + "  " + map.get(k) + " frequencies: " + treeArray[k] + "\n");
			}
		}
		return s.toString();
	}
}
