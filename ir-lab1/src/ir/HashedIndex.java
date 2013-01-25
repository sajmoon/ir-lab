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
    	PostingsList result = new PostingsList();
    	if ( queryType == Index.INTERSECTION_QUERY) {
    		// Intersektion Query
    		
    		PostingsList firstQuery = getPostings(query.terms.get(0));
    		PostingsList secondQuery = null; 
    		if (query.terms.size() == 1) {
    			// No second terms, return the first result
    			return firstQuery;
    			
    		} else if (query.terms.size() > 1) {
    			// more terms in query
    			secondQuery = getPostings(query.terms.get(2));
    			System.out.println("term(1): " + query.terms.get(1));
    			if (query.terms.get(1).equals("eller")) {
    				System.out.println("Interseciton search");
    				int i = 0;
    				int j = 0;
    				PostingsEntry e1 = firstQuery.get(i);
    				PostingsEntry e2 = secondQuery.get(i);
    				while(e1 != null && e2 != null) {
    					try {
	    					if (e1.docID == e2.docID) {
	    						result.addEntry(e1);
	    						i++;
	    						j++;
	    						e1 = firstQuery.get(i);
	    						e2 = secondQuery.get(i);
	    					} else if (e1.docID < e2.docID) {
	    						i++;
	    						e1 = firstQuery.get(i);
	    					} else {
	    						j++;
	    						e2 = secondQuery.get(j);
	    					}
    					} catch (Exception e) {
    						// TODO Exception? Fix this!
    						System.out.println("We got an exception in HasedIndex.search for intersection search");
    						break;
    					}
    				}
    				
    			}
    		}
    	} else if ( queryType == 1) {
    		// Phrased Query
    	}
    	return result;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
