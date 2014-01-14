/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */

package ir;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {

    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     * Creates a new empty Query
     */
    public Query() {
    }

    /**
     * Creates a new Query from a string of words
     */
    public Query(String queryString) {
        StringTokenizer tok = new StringTokenizer(queryString);
        while (tok.hasMoreTokens()) {
            terms.add(tok.nextToken());
            weights.add(new Double(1));
        }
    }

    /**
     * Returns the number of terms
     */
    public int size() {
        return terms.size();
    }

    /**
     * Returns a shallow copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        queryCopy.terms = (LinkedList<String>) terms.clone();
        queryCopy.weights = (LinkedList<Double>) weights.clone();
        return queryCopy;
    }

    /**
     * Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Indexer indexer) {
        // results contain the ranked list from the current search
        // docIsRelevant contains the users feedback on which of the 10 first hits are relevant

        //
        //  YOUR CODE HERE
        //
        double ALPHA = 0.9;
        double BETA = 0.2;

        // vi minskar vikten på de termer vi redan har.
        for (int i = 0; i < terms.size(); i++) {
            changeWeight(i, ALPHA);
        }

        int countOfRelevantDocuments = countRelevantDocuments(docIsRelevant);

        for (int i = 0; i < docIsRelevant.length; i++) {
            if (docIsRelevant[i]) {
                int docID = results.get(i).docID;
                HashMap<String, PostingsEntry> docTerms = indexer.index.inverseIndex.get(docID);

                // docTerms är nu en hashMap av alla termer i det documentet.
                // Vi borde plocka ut de viktigaste.
                // Typ sortera på score, men kanske inte just nu.

                for (String term : docTerms.keySet()) {
                    if (terms.contains(term)) {
                        // Ska inte ändra vikten. Right? JO! LEts do it!!
//                        changeWeight(term, BETA);
                        for (int j = 0; j < weights.size(); j++) {
                            weights.set(j,weights.get(j) + (docTerms.get(term).score ) * BETA * (1/countOfRelevantDocuments));
                        }
                    } else {
                        terms.add(term);
                        weights.add( (docTerms.get(term).score ) * BETA * (1/countOfRelevantDocuments));
                    }
                }
            }
        }
        kanDuNormalizeraQueryWeightTack();
    }

    public void kanDuNormalizeraQueryWeightTack() {
        double totalWeight = 0d;
        for (double w : weights) {
            totalWeight += w;
        }

        System.out.println("total weight of query: " + totalWeight);

        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / totalWeight);
        }

        totalWeight = 0d;
        for (double w : weights) {
            totalWeight += w;
        }

        System.out.println("Total weight after normalization: " + totalWeight);
    }

    private int countRelevantDocuments(boolean[] docs) {
        int count = 0;
        for (int i = 0; i < docs.length; i++) {
            if (docs[i])
                count++;
        }
        return count;
    }
    private void changeWeight(int termIndex, double factor) {
        weights.set(termIndex, weights.get(termIndex)*factor);
    }
}

    
