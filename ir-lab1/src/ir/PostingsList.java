/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
    	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
    	return list.get( i );
    }
    
    public void addEntry(PostingsEntry entry) {
		int i = 0;
		if (list.size() == 0) {
			list.add(entry);
		} else {
			for (i = list.size()-1; i >= 0; i--) {
				PostingsEntry e = list.get(i);
				if (e.docID == entry.docID) {
					// add offset to entry not entry.
					e.addOffsets(entry.offsets);
					break;
				} else if (e.docID > entry.docID) {
					// place here to keep sorted.
					list.add(i, entry);
					break;
				} else if (i == list.size()-1) {
					list.add(entry);
				}
			}
		}
		
	}

    //
    //  YOUR CODE HERE
    //
}
	

			   
