/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public List<Integer> offsets;

    public PostingsEntry(int inputDocID) {
    	docID = inputDocID;
    	score = 0;
    	offsets = new ArrayList<Integer>();
    }
    
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }

    public void addOffsets(List<Integer> offsets) {
    	this.offsets.addAll(offsets);
    }
    
    public void addOffset(Integer offset) {
    	offsets.add(offset);
    }
    //
    //  YOUR CODE HERE
    //

}

    
