package fr.univparis.metro;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Double;
import java.lang.Integer;

public class PriorityQueue<E> {

    private class Pair {
        E val;
	Double key;
	
	public Pair(E v, Double k) {
	    val = v;
	    key = k;
	}
    }
	    
    private ArrayList<Pair> tree;
    private HashMap<E, Integer> index;

    
    public PriorityQueue() {
	tree = new ArrayList<Pair>();
	index = new HashMap<E, Integer>();
    }

    
    /**
     * Returns true if this priority queue contains no elements.
     * @return true if this priority queue contains no elements
     */
    public boolean isEmpty() {
	return tree.isEmpty();
    }
    

    /** 
     * Inserts the specified element into this priority queue given a key (priority).
     * @param val the element to add
     * @param key the priority of val
     */ 
    public boolean add(E val, Double key) {
	if( index.containsKey(val) || val == null )
	    return false;

	Pair entry = new Pair(val, key);
	tree.add( entry );
    
	int i = tree.size() - 1;

	siftUp(i);
	
	return true;
    }

    /**
     * Move node i up in the tree, as long as needed; used to restore heap condition after insertion. 
     */
    private void siftUp(int i) {
	Pair entry = tree.get(i);
	
	while(i >= 1 && tree.get((i-1)/2).key > entry.key) {
	    tree.set(i, tree.get((i-1)/2)); 
	    index.put(tree.get((i-1)/2).val, i);

	    i = (i-1)/2;	    
	}
	
	tree.set(i, entry);
	index.put(entry.val, i);
    }
    
    /** 
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     * @return the head of this queue, or null if this queue is empty
     */
    public E poll() {
	if( tree.isEmpty() )
	    return null;
	
	Pair root = tree.get(0);
	Pair last = tree.remove( tree.size() - 1 );

	if( null == index.remove(root.val) )
	    throw new IllegalStateException();

	if( !tree.isEmpty() ) {	
	    tree.set(0, last);	
	    index.put(last.val, 0);

	    siftDown(0);
	}
	return root.val;
    }


    /** 
     * Move node i down in the tree; used to restore heap condition after deletion
     */
    private void siftDown(int i) {
	int left = 2*i + 1;
	int right = 2*i + 2;
	int win = i;

	if( left < tree.size() && tree.get(left).key < tree.get(win).key )
	    win = left;
        if ( right < tree.size() && tree.get(right).key < tree.get(win).key )
	    win = right;

	if( win != i ) {
	    swap(win, i);
	    index.put( tree.get(i).val, win);
	    index.put( tree.get(win).val, i);
	    siftDown(win);
	}
    }

    private void swap(int i, int j) {
	Pair tmp = tree.get(i);
	tree.set(i, tree.get(j));
	tree.set(j, tmp);
    }


    /**
     * Update the priority of element val with key. The new key must be lesser than the old key
     * @param val the element for which we want to update its priority
     * @param key the new key lesser than the previous key associated to val
     * @return true if the priority was successfully updated
     */
    public boolean updatePriority(E val, Double key) {
	if( !index.containsKey(val) )
	    return false;

	int i = index.get(val); // index of val in tree
	
	if( key >= tree.get(i).key )
	    return false;
	
	tree.get(i).key = key; // updating the key

	siftUp(i);
	
	return true;
    }
}
