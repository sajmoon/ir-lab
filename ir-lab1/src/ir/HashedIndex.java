/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */

package ir;

import java.util.*;
import java.util.Map.Entry;


/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /**
     * The index as a hashtable.
     */
    private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>(); // token, postings
    private HashMap<Integer, LinkedList<String>> inverseIndex = new HashMap<Integer, LinkedList<String>>(); // docID, tokens

    /**
     * Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset) {
        PostingsList list = new PostingsList();
        LinkedList<String> tokenList = new LinkedList<String>();

        if (index.containsKey(token))
            list = index.get(token);

        if (inverseIndex.containsKey(docID))
            tokenList = inverseIndex.get(docID);

		
		/* Add this occurance of the word to the list for the document */
        PostingsEntry entry = new PostingsEntry(docID);
        entry.addOffset(offset);
        list.addEntry(entry);

		/* Create a inverseIndex aka a tokenList for this doc */
        tokenList.add(token);

        if (!index.containsKey(token))
            index.put(token, list);

        if (!inverseIndex.containsKey(docID)) {
            inverseIndex.put(docID, tokenList);
        }
    }


    /**
     * Returns the postings for a specific term, or null
     * if the term is not in the index.
     */
    public PostingsList getPostings(String token) {
        System.out.println("Searching for " + token);
        PostingsList list = index.get(token);

        return list;
    }

    /**
     * Searches the index for postings matching the query.
     */
    public PostingsList search(Query query, int queryType, int rankingType) {
        if (queryType == Index.INTERSECTION_QUERY) {
            // Intersektion Query

            if (query.terms.size() == 1) {
                // No second terms, return the first result
                PostingsList firstQuery = getPostings(query.terms.get(0));

                return firstQuery;
            } else if (query.terms.size() > 1) {
                // more terms in query
                return intersectionSearch(query.terms);
            }

        } else if (queryType == Index.PHRASE_QUERY) {
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
                if (p1.size() == 0 || p2.size() == 0) {
                    continue;
                }
                PostingsEntry e1 = p1.get(i);
                PostingsEntry e2 = p2.get(j);

                while (j < p2.size() && i < p1.size()) {
                    e1 = p1.get(i);
                    e2 = p2.get(j);
                    if (e1.docID == e2.docID) {
                        for (int pp1 = 0; pp1 < e1.offsets.size(); pp1++) {
                            for (int pp2 = 0; pp2 < e2.offsets.size(); pp2++) {
                                if ((Math.abs(e1.offsets.get(pp1) - e2.offsets.get(pp2))) <= DISTBETWEEN) {
                                    // Dom är nära varandra.
                                    answer.addEntry(e2);
                                }
                            }
                        }
                        i++;
                        j++;
                    } else if (e1.docID < e2.docID) {
                        i++;
                    } else {
                        j++;
                    }
                }
                p1 = answer;
            }
            return answer;
        } else if (queryType == Index.RANKED_QUERY) {
            // Ranked query.
            System.out.println("Ranked query");

            PostingsList p1 = unionSearch(query.terms);

            ArrayList<Vector<Double>> vectors = createVectors(p1, query.terms);

            // Vector<Double> queryV = getTFIDFforQuery(query.terms) );


            for (int i = 0; i < vectors.size(); i++) {
                //	double dotProd = cosSim(queryV, vectors.get(i));
                //	p1.get(i).score = dotProd;
            }


            p1.sort();

            return p1;
        }
        return null;
    }


    //private Vector<Double> getTFIDFforQuery(LinkedList<String> terms) {
    //	Vector<Double> v;
//		return 1.0d;
//		// Get total number of documents where the term is present ()
//				int docsWithTerm = index.get(token).size();
////				
////				// Get the number of terms in any document
//				int termsInDoc = inverseIndex.get(docID).size();
////				
////				// Get the number of occurrences of a specific word in a specific doc
//				int tokenCountInDocument = index.get(token).getDoc(docID).offsets.size(); 
////				
////				// Total count of documents
//				int docCount = inverseIndex.entrySet().size();
////				
//				return calc_tf_idf(docsWithTerm, termsInDoc, tokenCountInDocument, docCount);

    //for ()
    //return calc_tf_idf(1, terms.size(), terms.size(), 1);

    //}

    public PostingsList unionSearch(List<String> terms) {
        System.out.println("union search");

        PostingsList result = new PostingsList();

        for (int i = 0; i < terms.size(); i++) {

            PostingsList r = getPostings(terms.get(i));

            for (int j = 0; j < r.size(); j++) {
                PostingsEntry e = r.get(j);
                if (result.getDoc(e.docID) == null) {
                    result.addEntry(e);
                }
            }
        }

        return result;
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
            if (r1 == null) {
                return r1;
            }
            if (r2 == null) {
                return r2;
            }
            PostingsEntry e1 = r1.get(j);
            PostingsEntry e2 = r2.get(k);
            while (j < r1.size() && k < r2.size()) {
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
     * No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }


    private ArrayList<Vector<Double>> createVectors(PostingsList p, LinkedList<String> terms) {
        // hashmap k = docid v = vecotr
        ArrayList<Vector<Double>> vectors = new ArrayList<Vector<Double>>();

        for (int d = 0; d < p.size(); d++) {

            // for each document.
            int docID = p.get(d).docID;

            Vector<Double> v = new Vector<Double>();

            // för varje term i queryn så
            // ska vectorn vara scoret för det ordet i det documentet.

            for (int q = 0; q < terms.size(); q++) {
                // if the term in the query is in the doc
                if (inverseIndex.get(docID).contains(terms.get(q))) {
                    System.out.println("add to vecotr: " + docID + " term: " + terms.get(q));
                    PostingsEntry e = index.get(terms.get(q)).getDoc(docID);
                    if (e == null)
                        v.add(0.0d);
                    else
                        v.add(e.score);
                }
            }

            vectors.add(v);
            // return a vector
        }
        return vectors;
    }

    public void updateScore() {
        System.out.println("Update score..");


        Iterator<Entry<String, PostingsList>> it = index.entrySet().iterator();

        double startTime = System.currentTimeMillis();
        while (it.hasNext()) {

            Map.Entry<String, PostingsList> pairs = it.next();

            // System.out.println("it:next => " + pairs.getKey() + " -> " + pairs.getValue().size());


            for (int i = 0; i < pairs.getValue().size(); i++) {

                String token = pairs.getKey();

                int docID = pairs.getValue().get(i).docID;


//				System.out.println("tf-idf: " + score);

                index.get(token).get(i).score = getScore(token, docID);

                // set score in entry to a value. tf * idf = (3/100) * (10.000.000 / 1.000)
            }
        }

        double endTime = System.currentTimeMillis();

        System.out.println("updating took: " + (endTime - startTime) + "ms");
    }

    private double getScore(String token, int docID) {
        // Get total number of documents where the term is present ()
        int docsWithTerm = index.get(token).size();

        // Get the number of terms in any document
        int termsInDoc = inverseIndex.get(docID).size();

        // Get the number of occurrences of a specific word in a specific doc
        int tokenCountInDocument = index.get(token).getDoc(docID).offsets.size();

        // Total count of documents
        int docCount = inverseIndex.entrySet().size();

        return calc_tf_idf(docsWithTerm, termsInDoc, tokenCountInDocument, docCount);

    }

    private double calc_tf_idf(int docsWithTerm, int termsInDoc, int tokenCountInDocument, int docCount) {
        // tf for a specific word = occurrences in doc / total number of words in that doc
        double tf = (double) tokenCountInDocument / (double) termsInDoc;

        // idf = count of docs / docs with term
        double idf = Math.log10((double) docCount / (double) docsWithTerm);

        double score = tf * idf;
        return score;
    }

    private double cosSim(Vector<Double> v1, Vector<Double> v2) {
        double d = dotProduct(v1, v2);
        double e1 = euclidianLength(v1);
        double e2 = euclidianLength(v1);
        return d / (Math.abs(e1) * Math.abs(e2));

    }

    private double euclidianLength(Vector<Double> v) {
        double sum = 0.0d;
        for (int i = 0; i < v.size(); i++) {
            sum += Math.pow(v.get(i), 2);
        }
        return sum;
    }

    private Double dotProduct(Vector<Double> v1, Vector<Double> v2) {
        if (v1.size() != v2.size())
            return 0.0d; // om de har fel dimensioner

        Double sum = 0.0d;
        for (int i = 0; i < v1.size(); i++)
            sum += v1.get(i) * v2.get(i);

        return sum;
    }
}
