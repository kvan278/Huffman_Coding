/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, <NAME1> and <NAME2), this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID:
 *  email address:
 *  Grader name:
 *
 *  Student 2
 *  UTEID:
 *  email address:
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor {
	
	// instance variables
	private IHuffViewer myViewer;
	private int currFormat;
	private int orgBits;
	private int compressedBits;
	private int preprocessedBits;
	private int[] wordFreq;
	private int treeBitsNum;
	private Map<Integer, String> map;
	private TreeNode tree;
	private String debugString;

	/**
	 * Preprocess data so that compression is possible --- count characters/create
	 * tree/store state so that a subsequent call to compress will work. The
	 * InputStream is <em>not</em> a BitInputStream, so wrap it in one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what kind
	 *                     of header to use, standard count format, standard tree
	 *                     format, or possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note, to
	 *         determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic number,
	 *         the header format number, the header to reproduce the tree, AND the
	 *         actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */

	public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
		currFormat = headerFormat;
		// creating all necessary data using the CompressedBits class (orignial bits,
		// compressed bits, frequency array, tree size, map);
		CompressedBits curFile = new CompressedBits(in);
		orgBits = curFile.getOrgBits();
		compressedBits = curFile.getCompressedBits();
		wordFreq = curFile.getArrayFreq();
		treeBitsNum = curFile.getTreeBitsNum();
		map = curFile.getMap();
		tree = curFile.getTree();
		debugString = curFile.showDebug();
		// adding the bits for magic "number" && format "number" (each numbers are 32
		// bits);
		compressedBits += IHuffConstants.BITS_PER_INT * 2;
		if (currFormat == IHuffConstants.STORE_COUNTS) {
			// header formats of STORE_COUNTS - frequency ints (each 32) of all 256 alphabet
			// size;
			compressedBits += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
		} else if (currFormat == IHuffConstants.STORE_TREE) {
			// header formats of STORE_TREE - tree representation size is 32 bits + size of
			// the tree
			compressedBits += treeBitsNum + IHuffConstants.BITS_PER_INT;
		}
		// returning the pre-compressedBits by taking the originalBits minus the TOTAL
		// bits of
		// the compressed file (magic number bits (32), format number bits (32), header
		// bits, and
		// the actual encoded bits);
		preprocessedBits = orgBits - compressedBits;
		// show debugging in GUI
		// uncomment the following two lines to show debug
		showString("PREPROCESSED BITS: " + preprocessedBits);
		showString(debugString);
		return preprocessedBits;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously been
	 * pre-processed via <code>preprocessCompress</code> storing state used by this
	 * call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger than
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
		// checking preconditions to see if the file has already been preprocessed
		// if yes, continue to check if the client is forcing it to compress or not
		// force = true: compress no matter the size outcome is smaller or larger
		// force = false: compress only when the size outcome is smaller;
		if ((force == true && preprocessedBits < 0) || preprocessedBits >= 0) {
			BitOutputStream outputFile = new BitOutputStream(out);
			// first writing out the 32 bit int for magic number into the new file;
			outputFile.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
			// second writing out the 32 bit int for the format number into the new file
			// (format number is known during preprocess process);
			outputFile.writeBits(IHuffConstants.BITS_PER_INT, currFormat);
			// if the format is STORE_COUNTS, the header will be printing out 32 bits int
			// of the frequencies of each word in order (1-256)
			// (not including the pseudo because it will be write in last after the actual
			// encoded part
			if (currFormat == IHuffConstants.STORE_COUNTS) {
				for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
					outputFile.writeBits(IHuffConstants.BITS_PER_INT, wordFreq[i]);
				}
				// if the formati is STORE_TREE, the header will be store 32 bits int of the
				// tree representation for the file [tree size + (leaf nodes * 9)] and this
				// number can be retrieved from calling the getTreeBitSum method from the
				// CompresedBit class
			} else if (currFormat == IHuffConstants.STORE_TREE) {
				outputFile.writeBits(IHuffConstants.BITS_PER_INT, treeBitsNum);
				writeTreeHeader(outputFile, tree);
			}
			// printing out the actual data as encoded binary numbers instead of regular 8
			// bits
			// binary numbers, the encoded binary numbers is from the map that were created
			// during
			// the pre-compress process;
			printOutCompressedData(new BitInputStream(in), outputFile);
			// finally printing out the pseudo code into the end of the compressed file;
			String peof = map.get(IHuffConstants.PSEUDO_EOF);
			for (int i = 0; i < peof.length(); i++) {
				if (peof.charAt(i) == '1') {
					outputFile.writeBits(1, 1);
				} else {
					outputFile.writeBits(1, 0);
				}
			}
			outputFile.close();
		}
		// returning the TOTAL number of bits in the compressed file;
		// uncomment the following two lines to show debug
		showString("TOTAL BIT COMPRESSED: " + compressedBits);
		showString(debugString);
		return compressedBits;
	}

	/*
	 * STORE_COUNTS format wouldn't need helper method because it can be easily code
	 * directly in the parent method since it doesn't require any recursion
	 */

	// helper method to write the header for files that is compressing using
	// STORE_TREE format
	private void writeTreeHeader(BitOutputStream out, TreeNode node) {
		// if it is leaf, write 1 and then write 9 bits representation of the value
		if (node.isLeaf()) {
			out.writeBits(1, 1);
			out.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());
		} else {
			// if it is not leaf, write 0 and go to the next node;
			out.writeBits(1, 0);
			writeTreeHeader(out, node.getLeft());
			writeTreeHeader(out, node.getRight());
		}
	}

	// actual method to print out the encoded data using information from the map
	// that we created
	// during the pre-compressed process;
	private void printOutCompressedData(BitInputStream input, BitOutputStream output) throws IOException {
		int currBits = input.readBits(IHuffConstants.BITS_PER_WORD);
		while (currBits != -1) {
			String currString = map.get(currBits);
			for (int i = 0; i < currString.length(); i++) {
				if (currString.charAt(i) == '1') {
					output.writeBits(1, 1);
				} else {
					output.writeBits(1, 0);
				}
			}
			currBits = input.readBits(IHuffConstants.BITS_PER_WORD);
		}
		input.close();
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		BitInputStream input = new BitInputStream(in);
		BitOutputStream output = new BitOutputStream(out);
		int magicNum = input.readBits(IHuffConstants.BITS_PER_INT);
		int currFormat = input.readBits(IHuffConstants.BITS_PER_INT);
		// checking if the file starting with the correct magic number
		if (magicNum != IHuffConstants.MAGIC_NUMBER) {
			myViewer.showError("Error reading compressed file. \n" + "File did not start with the huff magic number.");
			return -1;
		}
		// using decompressedBits class to take in BitInputStream data and BitOutputStream data
		// as well as the format of the file and use it to generate new uncompressed file
		// *** see the class for more details on how it works ***
		DecompressedBits decompressedFile = new DecompressedBits(input, output, currFormat);
		// return the total bits of the new file
		int resultBits = decompressedFile.getBits();
		// showing debugging on the GUI
		// uncomment the following 2 lines to show debug
		showString("DECOMPRESSED BITS: " + resultBits);
		showString(decompressedFile.showDebug());
		// close streams
		input.close();
		output.close();
		return resultBits;
	}

	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	private void showString(String s) {
		if (myViewer != null) {
			myViewer.update(s);
		}
	}
}
