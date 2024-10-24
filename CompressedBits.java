import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
/*
 * This CompressedBits class is mostly use preCompress and also compress method of the SimpleHuffProcessor
 * this class is creating a new objects which takes in an InputStream from the SimpleHuffProcessor class,
 * read through the InputStream and returns the necessary informations such as array of frequencies, tree,
 * map, as well as the number of bits that has been encoded.
 */
public class CompressedBits {
	
	// instance variables
	private BitInputStream currStream;
	private int[] wordFreq;
	private TreeNode tree;
	private Map<Integer, String> map;
	
	// constructor for CompressedBits class
	public CompressedBits(InputStream in) throws IOException {
		currStream = new BitInputStream(in);
		createArray();
		createTree();
		createMap();
	}
	
	// call this method in SimpleHuffProcessor to return the array of frequency of the file
	public int[] getArrayFreq() throws IOException {
		return wordFreq;
	}
	
	// helper method to create the array of frequency using information from InputStream
	private void createArray() throws IOException {
		wordFreq = new int[IHuffConstants.ALPH_SIZE + 1];
		int currBits = currStream.readBits(IHuffConstants.BITS_PER_WORD);
		while (currBits != -1) {
			wordFreq[currBits]++;
			currBits = currStream.readBits(IHuffConstants.BITS_PER_WORD);
		}
		// adding the pseudo at the end of the array;
		wordFreq[IHuffConstants.PSEUDO_EOF] = 1;
		currStream.close();
	}
	
	// call this method in SimpleHuffProcessor to return the tree of the file
	public TreeNode getTree() {
		return tree;
	}
	
	// helper method to create the tree for the file using information from the array of frequencies
	private void createTree() {
		PriorityQueue314<TreeNode> queue = new PriorityQueue314<>();
		// creating a new queue and add values from the array that have frequencies that are > 0;
		// (including pseudo b/c pseudo frequency is always 1)
		for (int i = 0; i < IHuffConstants.ALPH_SIZE + 1; i++) {
			if (wordFreq[i] > 0) {
				queue.enqueue(new TreeNode(i, wordFreq[i]));
			}
		}
		// adding --> removing --> add again into the queue until there is only 1 node left (tree node);
		while (queue.size() > 1) {
			TreeNode first = queue.dequeue();
			TreeNode second = queue.dequeue();
			TreeNode combined = new TreeNode(first, first.getFrequency() + second.getFrequency(), second);
			queue.enqueue(combined);
		}
		tree = queue.dequeue();
	}
	
	// call this method in the SimpleHuffProcessor to get the map of the encoded file
	// Integer of the map is store the ASCII Decimal # (array index)
	// String of the map is the string of the binary number that was encoded for the file
	public Map<Integer, String> getMap() {
		return map;
	}
	
	// helper method for map to set an empty map to encoded map
	private void createMap() {
		map = new HashMap<>();
		mapRecursion(tree, map, "");
	}
	
	// helper method to recurse through the tree of the file and creating strings that represents
	// the encoded binary # for the new compressed file.
	private void mapRecursion(TreeNode node, Map<Integer, String> map, String s) {
		if (node.isLeaf()) {
			map.put(node.getValue(), s);
		} else {
			mapRecursion(node.getLeft(), map, s + '0');
			mapRecursion(node.getRight(), map, s + '1');
		}
	}
	
	// call this method in the SimpleHuffProcessor to get the original bits of the file
	// (before compressing)
	public int getOrgBits() {
		int bits = 0;
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			bits += IHuffConstants.BITS_PER_WORD * wordFreq[i];
		}
		return bits;
	}
	
	// call this method in the SimpleHuffProcessor to the the encoded bits of the file
	// (after compressing)
	// (not including - magic number bits, format bits, and header bits)
	public int getCompressedBits() {
		int bits = 0;
		for (int k : map.keySet()) {
			if (k != IHuffConstants.PSEUDO_EOF) {
				bits += wordFreq[k] * map.get(k).length();
			}
		}
		bits += map.get(IHuffConstants.PSEUDO_EOF).length();
		return bits;
	}
	
	// getting the total number that would represent the tree [tree size + (# of leaf nodes * 9)]
	public int getTreeBitsNum() {
		return treeSizeRecursion(tree);
	}
	
	// helper method to get the total number of representation of the tree;
	private int treeSizeRecursion(TreeNode node) {
		if (node.isLeaf()) {
			return 1 + (1 + IHuffConstants.BITS_PER_WORD);
		} else {
			return 1 + treeSizeRecursion(node.getLeft()) + treeSizeRecursion(node.getRight());
		}
	}
	
	// this method return string to put in the parameter showString of
	// the SimpleHuffProcessor class to show chars, encoded bits data, frequencies
	// for debugging purposes only
	public String showDebug() {
		Map<Integer, String> sortedMap = new TreeMap<>(map);
		StringBuilder s = new StringBuilder();
		for(int k : sortedMap.keySet()) {
			char c = (char)	k;
			s.append("" + k + "  " + c + "  " + map.get(k) + " frequencies: " + wordFreq[k] + "\n" );
		}
		return s.toString();
	}
}
