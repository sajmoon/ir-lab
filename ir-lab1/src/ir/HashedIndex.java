/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;


import java.util.HashMap;
import java.util.HashSet;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
    	PostingsList list = new PostingsList();
    	
    	if (index.containsKey(token)) {
    		list = index.get(token);
    	}
    	PostingsEntry entry = new PostingsEntry(docID);
    	list.addEntry(entry);
    	if (!index.containsKey(token)) {
    		index.put(token, list);
    	}
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
    	HashSet<Integer> uniqueDocuments = new HashSet<Integer>();

    	PostingsList returnPostings = new PostingsList();
    	PostingsList list = index.get(token);
    	
    	for (int i = 0; i < list.size(); i++) {
    		PostingsEntry e = list.get(i);
    		if (!uniqueDocuments.contains(e.docID) ) {
    			uniqueDocuments.add(e.docID);
    			returnPostings.addEntry(e);
    		}
    	}
    	
    	return returnPostings;
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {
    	return getPostings(query.terms.get(0));
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
