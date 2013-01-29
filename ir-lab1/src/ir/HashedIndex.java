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
import java.util.List;


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
		entry.addOffset(offset);
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
		System.out.println("Searching for " + token);
		PostingsList list = index.get(token);

		return list;
	}


	/**
	 *  Searches the index for postings matching the query.
	 */
	public PostingsList search( Query query, int queryType, int rankingType ) {
		if ( queryType == Index.INTERSECTION_QUERY) {
			// Intersektion Query

			if (query.terms.size() == 1) {
				// No second terms, return the first result
				PostingsList firstQuery = getPostings(query.terms.get(0));

				return firstQuery;

			} else if (query.terms.size() > 1) {
				// more terms in query
				return intersectionSearch(query.terms);
			}

		} else if ( queryType ==  Index.PHRASE_QUERY) {
			if (query.terms.size() == 1) {
				return getPostings(query.terms.get(0));
			}
			System.out.println("Phrased Query");
			PostingsList answer = new PostingsList();

			int DISTBETWEEN = 1;
			// kör för de första 2 först. "November eller"
			System.out.println("p1: " + query.terms.get(0));
			PostingsList p1 = getPostings(query.terms.get(0));
			for (int postings = 1; postings < query.terms.size(); postings++) {
				answer = new PostingsList();
				
				System.out.println("p2: " + query.terms.get(postings));
				PostingsList p2 = getPostings(query.terms.get(postings));

				int i = 0;
				int j = 0;
				if (p1.size() == 0 || p2.size() == 0 ) {
					continue;
				}
				PostingsEntry e1 = p1.get(i);
				PostingsEntry e2 = p2.get(j);

				while ( j < p2.size() && i < p1.size()) {
					e1 = p1.get(i);
					e2 = p2.get(j);
					if (e1.docID == e2.docID) {
						for (int pp1 = 0; pp1 < e1.offsets.size(); pp1++) {
							for (int pp2 = 0; pp2 < e2.offsets.size(); pp2++) {
								if ((Math.abs(e1.offsets.get(pp1) - e2.offsets.get(pp2))) <= DISTBETWEEN ) {
									// Dom är nära varandra.
									answer.addEntry(e2);
								}
							}
						}
						i++; j++;
					} else if (e1.docID < e2.docID) {
						i++;
					} else {
						j++;
					}
				}
				p1 = answer;
			}
			return answer;
		}
		return null;
	}

	public PostingsList intersectionSearch(List<String> terms) {
		System.out.println("Interseciton search");
		PostingsList result = new PostingsList();

		PostingsList r1 = getPostings(terms.get(0));

		for (int i = 0; i < terms.size(); i++) {
			result = new PostingsList();
			PostingsList r2 = getPostings(terms.get(i));

			int j = 0;
			int k = 0;

			PostingsEntry e1 = r1.get(j);
			PostingsEntry e2 = r2.get(k);
			while(j < r1.size() && k < r2.size()) {
				e1 = r1.get(j);
				e2 = r2.get(k);
				try {
					if (e1.docID == e2.docID) {
						result.addEntry(e1);
						j++;
						k++;
					} else if (e1.docID < e2.docID) {
						j++;
					} else if (e1.docID > e2.docID) {
						k++;
					} else {
						break;
					}

				} catch (Exception e) {
					// TODO Exception? Fix this!
					System.out.println("We got an exception in HasedIndex.search for intersection search");
					e.printStackTrace();
					break;
				}
			}
			r1 = result;

		}

		return result;
	}

	/**
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
