/*  Student information for assignment:
 *
 *  On MY honor, Khanh Van, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 : Khanh Van
 *  UTEID: kqv69
 *  email address: kvan27082002@gmail.com
 *  Grader name: Skyler
 *
 *
 */

import java.util.LinkedList;

/*
 * PriorityQueue314 class is to handle TreeNodes of the files, organize the TreeNodes that
 * is enqueue in a priority order (smaller frequencies go to the front)
 * (same frequencies, the one that is enqueue later goes to the back);
 */

public class PriorityQueue314<E extends Comparable<? super E>> {
	private LinkedList<TreeNode> list;
	
	// Constructor for PriorityQueue314
	public PriorityQueue314() {
		list = new LinkedList<>();
	}
	
	// enqueue or add method into the queue in priority order (smaller goes to front - larger goes to back)
	// (same frequencies, enqueue later goes to back);
	public boolean enqueue(TreeNode otherNode) {
		if (list.isEmpty()) {
			list.add(otherNode);
			return true;
		} else if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				if (otherNode.compareTo(list.get(i)) < 0) {
					list.add(i, otherNode);
					return true;
				} else if (otherNode.compareTo(list.getLast()) >= 0) {
					list.add(otherNode);
					return true;
				}
			}
		}
		return false;
	}
	
	// call this method in the main java class to get peek at the front element;
	public TreeNode front() {
		return list.peekFirst();
	}

	// call this method in the main java class to dequeue or remove the element and return it;
	public TreeNode dequeue() {
		return list.removeFirst();
	}
	
	// call this method in the main java class to see if the queue is empty;
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	// call this method in the main java class to see the size of the queue;
	public int size() {
		return list.size();
	}

	// call this method to return String representation of the queue;
	public String toString() {
		return list.toString();
	}
}