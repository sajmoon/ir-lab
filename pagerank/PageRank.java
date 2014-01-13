/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
      int noOfDocs = readDocs( filename );
      computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
      int fileIndex = 0;
      try {
        System.err.print( "Reading file... " );
        BufferedReader in = new BufferedReader( new FileReader( filename ));
        String line;
        while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
          int index = line.indexOf( ";" );
          String title = line.substring( 0, index );
          Integer fromdoc = docNumber.get( title );
          //  Have we seen this document before?
          if ( fromdoc == null ) {	
            // This is a previously unseen doc, so add it to the table.
            fromdoc = fileIndex++;
            docNumber.put( title, fromdoc );
            docName[fromdoc] = title;
          }
          // Check all outlinks.
          StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
          while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
            String otherTitle = tok.nextToken();
            Integer otherDoc = docNumber.get( otherTitle );
            if ( otherDoc == null ) {
              // This is a previousy unseen doc, so add it to the table.
              otherDoc = fileIndex++;
              docNumber.put( otherTitle, otherDoc );
              docName[otherDoc] = otherTitle;
            }
            // Set the probability to 0 for now, to indicate that there is
            // a link from fromdoc to otherDoc.
            if ( link.get(fromdoc) == null ) {
              link.put(fromdoc, new Hashtable<Integer,Boolean>());
            }
            if ( link.get(fromdoc).get(otherDoc) == null ) {
              link.get(fromdoc).put( otherDoc, true );
              out[fromdoc]++;
            }
          }
        }
        if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
          System.err.print( "stopped reading since documents table is full. " );
        }
        else {
          System.err.print( "done. " );
        }
        // Compute the number of sinks.
        for ( int i=0; i<fileIndex; i++ ) {
          if ( out[i] == 0 )
            numberOfSinks++;
        }
      } catch ( FileNotFoundException e ) {
        System.err.println( "File " + filename + " not found!" );
      } catch ( IOException e ) {
        System.err.println( "Error reading file " + filename );
      }
      System.err.println( "Read " + fileIndex + " number of documents" );
      return fileIndex;
    }


    /* --------------------------------------------- */

    double diffOfVector(double[] x, double[] xp, int length) {
      double diff = 0d;
      for (int i = 0; i < length; i++) {
        diff += x[i] - xp[i];
      }
      return diff;
    }
    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
      // set startvärdet till ett snitt

      // skapa P eller G
      double[][] P = new double[numberOfDocs][numberOfDocs];
      // den är svingles!

      // Beräkna P.
      for (int i = 0; i < numberOfDocs; i++) {
        double ones = calculateOnesInDoc(i, numberOfDocs);
        for (int j = 0; j < numberOfDocs; j++) {
          if (ones != 0) {
            P[i][j] = ( (getFromPosition(i, j) / ones));// * (BORED) ) + ((1d-BORED)/numberOfDocs);
            /* assert(1d-BORED > 0); */
            /* System.out.println("[" + i + "][" + j + "]:"); */
            /* System.out.println("P[i][j]: "+ P[i][j]); */
            /* System.out.println("ones: "+ ones); */
            /* System.out.println("pos: "+ getFromPosition(i,j) / ones); */
          } else {
            P[i][j] = 1d / numberOfDocs;
            /* System.out.println("no ones: " + (1dgcc /numberOfDocs)); */
          }
          /* assert P[i][j] > 0; */
        }
      }

      // Power iterations
      
      /* printMatrix(P, 10); */
      // initial guess
      double [] x = new double[numberOfDocs];
      for (int xi = 0; xi < numberOfDocs; xi++) {
        x[xi] = (1d/numberOfDocs);
      }

      System.out.println("P computed. Doing power iterations.");
      System.out.println("");
      int i = 0;
      while(i < 5) {
        /* System.out.println(x.toString()); */
        x = matrixMultiply(x, P, numberOfDocs);
        /* System.out.println(x); */
        i++;
        System.out.print(i + " ");
      }

      System.out.println("\nIterations complete");
      /* printMatrix(P, 100); */
      List<Score> result = new ArrayList<Score>();
      
      double maxSoFar = 0;
      for (int k = 0; k < numberOfDocs; k++) {
        
        result.add(new Score(x[k], docName[k]));
      }

      Collections.sort(result);
      
      int collectionCount = 0;
      for( Score s : result ) {
        if (collectionCount < 50 || collectionCount > 8640)
          System.out.println(collectionCount + ". " + s.name + " : " + s.score);
        if (s.name.equals("669"))
          System.out.println ("---------- " + collectionCount + " " + s.score);
        collectionCount++;
      }

    }

    void printMatrix(double[][] P, int length) {
      for( int i = 0; i < length; i++ ) {
        System.out.print("[");
        for (int j = 0; j < length; j++) {
          System.out.print(P[i][j]+",");
        }
        System.out.println("]");
      }
    }

    public double[] matrixMultiply(double[] x, double[][] P, int numberOfDocs) {
      for (int i = 0; i < numberOfDocs; i++) {
        double result = 0;
        for (int j = 0; j < numberOfDocs; j++) {
          /* assert(x[j]>=0); */
          /* assert(P[i][j]>=0); */
          result += (x[j]*P[j][i]);
        }
        x[i] = result;
      }
      return x;
    }

    public double getFromPosition(int i, int j) {
      Hashtable<Integer, Boolean> theDocument = link.get(i);

      if (theDocument == null) {
        return 0;
      }
      if (theDocument.containsKey(j)) {
        return 1;
      }
      return 0;
    }

    public double calculateOnesInDoc(int docID, int numOfDocs) {
      return out[docID];
      /* Hashtable<Integer, Boolean> theDocument = link.get(docID); */
      /* if (theDocument == null) { return 0; } */
      /* double result = 0d; */
      /* for (int i = 0; i < numOfDocs; i++) { */
      /*   if (theDocument.containsKey(i)) { */
      /*     result++; */
      /*   } */
      /* } */
      /* return result; */
    }

    private class Score implements Comparable<Score> {
      double score;
      String name;

      public Score(double score, String name) {
        this.score = score;
        this.name = name;
      }

      @Override
      public int compareTo(Score o) {
        return score < o.score? 1 : score > o.score? -1 : 0;
      }
    }

    /* --------------------------------------------- */

    public static void main( String[] args ) {
    if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
    }
    else {
	    new PageRank( args[0] );
    }
  }
}
