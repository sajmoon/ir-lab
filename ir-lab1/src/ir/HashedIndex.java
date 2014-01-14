/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */

package ir;

import org.apache.log4j.Logger;

import java.util.*;


/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
    Logger logger = Logger.getLogger("ir.lab.hashedindex");

    /**
     * The index as a hashtable.
     */


    /**
     * Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset) {
        //logger.info("Insert " + token + ", " + docID + ", " + offset);
        PostingsList list = new PostingsList();
        HashMap<String, PostingsEntry> tokenList = new HashMap<>();

        if (index.containsKey(token))
            list = index.get(token);

        if (inverseIndex.containsKey(docID))
            tokenList = inverseIndex.get(docID);

		/* Add this occurance of the word to the list for the document */
        PostingsEntry entry = new PostingsEntry(docID);
        entry.addOffset(offset);
        list.addEntry(entry);

		/* Create a inverseIndex aka a tokenList for this doc */
        PostingsEntry tokenEntry = new PostingsEntry(docID, token);
        tokenList.put(token, tokenEntry);

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

            PostingsList resultList = unionSearch(query.terms);

            logger.info("Result lenght: " + resultList.size());
            HashMap<String, Double> query_score = getQueryScore(query.terms);

            double euc_length_of_query = 0d;
            for (String term : query.terms) {
                euc_length_of_query += Math.pow(query_score.get(term), 2);
            }

            euc_length_of_query = Math.sqrt(euc_length_of_query);

            logger.info("got query euclidean length: " + euc_length_of_query );

            for (int i = 0; i < resultList.size(); i++) {

                // För varje resultat
                double value = 0.0d;
                int docID = resultList.get(i).docID;

                for (String term : query.terms) {
                    double tf_idf = get_tf_idf_in_document(docID, term);
                    value += (Math.pow(tf_idf * query_score.get(term),2));
                }

                value = Math.sqrt(value);

                double euc_length_of_doc = 0.0d;

                Set<String> terms = inverseIndex.get(docID).keySet();

                for (String term : terms) {
                     euc_length_of_doc += Math.pow(get_tf_idf_in_document(docID, term), 2);
                }

                euc_length_of_doc = Math.sqrt(euc_length_of_doc);

                double finalScore = ( value / (euc_length_of_query * euc_length_of_doc) );
                //logger.info("final score is " + finalScore);
                resultList.get(i).score = finalScore;
            }

            resultList.sort();
            return resultList;
        }
        return null;
    }

    private HashMap<String, Double> getQueryScore(LinkedList<String> terms) {
        HashMap<String,Double> query_score = new HashMap<>();

        for (String term : terms) {
            // Get total number of documents where the term is present ()
            if (index.get(term) == null) {
                query_score.put(term, 0d);
                continue;
            }
            int docsWithTerm = index.get(term).size();

            // Get the number of terms in the query
            int termsInDoc = terms.size();

            // Get the number of occurrences of a specific word in a specific doc
            // antal gånger ordet finns i queryn
            //int tokenCountInDocument = index.get(token).getDoc(docID).offsets.size();
            int tokenCountInDocument = 1;

            // Total count of documents
            int docCount = inverseIndex.entrySet().size();

            double tf_idf = calc_tf_idf(termsInDoc, docsWithTerm, tokenCountInDocument, docCount);
            //return calc_tf_idf(docsWithTerm, termsInDoc, tokenCountInDocument, docCount);
            query_score.put(term, tf_idf);
        }

        return query_score;
    }

    private double get_tf_idf_in_document(int docID, String term) {
        if (!inverseIndex.get(docID).containsKey(term))
            return 0.0d;
        return inverseIndex.get(docID).get(term).score;
    }

    public PostingsList unionSearch(List<String> terms) {
        System.out.println("Union search");

        PostingsList result = new PostingsList();

        for (int i = 0; i < terms.size(); i++) {

            PostingsList r = getPostings(terms.get(i));

            if (r == null)
                break;
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

    public void updateScore() {
        System.out.println("Update score..");

        Iterator<Map.Entry<String, PostingsList>> it = index.entrySet().iterator();

        double startTime = System.currentTimeMillis();
        while (it.hasNext()) {

            Map.Entry<String, PostingsList> pairs = it.next();


            for (int i = 0; i < pairs.getValue().size(); i++) {

                String token = pairs.getKey();

                int docID = pairs.getValue().get(i).docID;

                double score = calculateScore(token, docID);
                index.get(token).get(i).score = score;
                inverseIndex.get(docID).get(token).score = score;

                // set score in entry to a value. tf * idf = (3/100) * (10.000.000 / 1.000)
            }
        }

        double endTime = System.currentTimeMillis();

        System.out.println("updating took: " + (endTime - startTime) + "ms");
    }

    private double calculateScore(String token, int docID) {
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
        // tf for a specific word = occurrences in doc
        double tf = (double) tokenCountInDocument;// / (double) termsInDoc;

        // idf = count of docs / docs with term
        double idf = Math.log10((double) docCount / (double) docsWithTerm);

        double score = tf * idf;
        return score;
    }
}
