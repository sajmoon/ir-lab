/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;


import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 *   A graphical interface to the information retrieval system.
 */
public class SearchGUI extends JFrame {

    /**  The indexer creating the search index. */
    Indexer indexer;

	/**  The query posed by the user, used in search() and relevanceFeedbackSearch() */
	private Query query; 
	
	/**  The returned documents, used in search() and relevanceFeedbackSearch() */
	private PostingsList results; 
	
    /**  Directories that should be indexed. */
    LinkedList<String> dirNames = new LinkedList<String>();

    /**  Indices to be retrieved from disk. */
    LinkedList<String> indexFiles = new LinkedList<String>();

    /** Maximum number of indices we can read from disk. */
    public static final int MAX_NUMBER_OF_INDEX_FILES = 10;

    /**  The query type (either intersection, phrase, or ranked). */
    int queryType = Index.INTERSECTION_QUERY;

    /**  The index type (either hashed or mega). */
    int indexType = Index.HASHED_INDEX;

	/**  The ranking type (either tf-idf, pagerank, or combination). */
    int rankingType = Index.TF_IDF;
		
    /**  Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /**  Directory from which the code is compiled and run. */
    /* on csc Compuers */
    // public static final String homeDir = "/misc/info/DD2476/ir13/lab";
    /* On laptop. */
    public static final String homeDir = "~/Code/ir/";

    /*
     *   The nice logotype
     */
    static final String IPIC = homeDir + "/pics/i.jpg";
    static final String RPIC = homeDir + "/pics/r.jpg";
    static final String TPIC = homeDir + "/pics/t.jpg";
    static final String HPIC = homeDir + "/pics/h.jpg";
    static final String EPIC = homeDir + "/pics/e.jpg";
    static final String NPIC = homeDir + "/pics/n.jpg";
    static final String BLANKPIC = homeDir + "/pics/blank.jpg";
	

    /*  
     *   Common GUI resources
     */
    public JTextField queryWindow = new JTextField( "", 28 );
    public JTextArea resultWindow = new JTextArea( "", 23, 28 );
    private JScrollPane resultPane = new JScrollPane( resultWindow );
    private Font queryFont = new Font( "Arial", Font.BOLD, 24 );
    private Font resultFont = new Font( "Arial", Font.BOLD, 16 );
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu( "File" );
    JMenu optionsMenu = new JMenu( "Search options" );
    JMenu rankingMenu = new JMenu( "Ranking score" ); 
    JMenuItem saveItem = new JMenuItem( "Save index and exit" );
    JMenuItem quitItem = new JMenuItem( "Quit" );
    JRadioButtonMenuItem intersectionItem = new JRadioButtonMenuItem( "Intersection query" );
    JRadioButtonMenuItem phraseItem = new JRadioButtonMenuItem( "Phrase query" );
    JRadioButtonMenuItem rankedItem = new JRadioButtonMenuItem( "Ranked retrieval" );
    JRadioButtonMenuItem tfidfItem = new JRadioButtonMenuItem( "tf-idf" );
    JRadioButtonMenuItem pagerankItem = new JRadioButtonMenuItem( "PageRank" );
    JRadioButtonMenuItem combinationItem = new JRadioButtonMenuItem( "Combination" );
    ButtonGroup queries = new ButtonGroup();
	ButtonGroup ranking = new ButtonGroup(); 
	public JPanel feedbackBar = new JPanel(); 
	JCheckBox[] feedbackButton = { new JCheckBox ( "1" ), new JCheckBox ( "2" ), new JCheckBox ( "3" ), 
	new JCheckBox ( "4" ), new JCheckBox ( "5" ), new JCheckBox ( "6" ), new JCheckBox ( "7" ), 
	new JCheckBox ( "8" ), new JCheckBox ( "9" ), new JCheckBox ( "10" ) }; 
	JToggleButton feedbackExecutor = new JToggleButton("New search"); 


    /* ----------------------------------------------- */


    /*
     *   Create the GUI.
     */
    private void createGUI() {
	// GUI definition
	setSize( 600, 650 );
	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	getContentPane().add(p, BorderLayout.CENTER);
	// Top menu
	menuBar.add( fileMenu );
	menuBar.add( optionsMenu );
	menuBar.add( rankingMenu );
	fileMenu.add( saveItem );
	fileMenu.add( quitItem );
	optionsMenu.add( intersectionItem );
	optionsMenu.add( phraseItem );
	optionsMenu.add( rankedItem );
	rankingMenu.add( tfidfItem ); 
	rankingMenu.add( pagerankItem ); 
	rankingMenu.add( combinationItem ); 
	queries.add( intersectionItem );
	queries.add( phraseItem );
	queries.add( rankedItem );
	ranking.add( tfidfItem ); 
	ranking.add( pagerankItem );
	ranking.add( combinationItem ); 
	intersectionItem.setSelected( true );
	tfidfItem.setSelected( true );
	p.add( menuBar );
	// Logo
	JPanel p1 = new JPanel();
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	p1.add( new JLabel( new ImageIcon( IPIC ))); 
	p1.add( new JLabel( new ImageIcon( RPIC )));
	p1.add( new JLabel( new ImageIcon( BLANKPIC )));
	p1.add( new JLabel( new ImageIcon( TPIC )));
	p1.add( new JLabel( new ImageIcon( HPIC )));
	p1.add( new JLabel( new ImageIcon( IPIC )));
	p1.add( new JLabel( new ImageIcon( RPIC )));
	p1.add( new JLabel( new ImageIcon( TPIC )));
	p1.add( new JLabel( new ImageIcon( EPIC )));
	p1.add( new JLabel( new ImageIcon( EPIC )));
	p1.add( new JLabel( new ImageIcon( NPIC )));
	p.add( p1 );
	JPanel p3 = new JPanel();
	// Search box
	p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
	p3.add( new JLabel( new ImageIcon( BLANKPIC )));
	p3.add( queryWindow );
	queryWindow.setFont( queryFont );
	p3.add( new JLabel( new ImageIcon( BLANKPIC )));
	p.add( p3 );
	// Display area for search results
	p.add( resultPane );
	resultWindow.setFont( resultFont );
	// Relevance feedback
	for ( int i = 0; i<10; i++ ) {
		feedbackBar.add( feedbackButton[i] ); 
	}
	feedbackBar.add( feedbackExecutor );
	p.add( feedbackBar ); 
	// Show the interface
	setVisible( true );
		
	Action search = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    // Normalize the search string and turn it into a Query
		    String queryString = SimpleTokenizer.normalize( queryWindow.getText() );
		    query = new Query( queryString );
		    // Search and print results. Access to the index is synchronized since
		    // we don't want to search at the same time we're indexing new files
		    // (this might corrupt the index).
		    synchronized ( indexLock ) {
			results = indexer.index.search( query, queryType, rankingType ); 
		    }
		    StringBuffer buf = new StringBuffer();
		    if ( results != null ) {
			buf.append( "\nFound " + results.size() + " matching document(s)\n\n" );
			for ( int i=0; i<results.size(); i++ ) {
				buf.append( " " + i + ". " );
				String filename = indexer.index.docIDs.get( "" + results.get(i).docID );
				if ( filename == null ) {
				buf.append( "" + results.get(i).docID );
				}
				else {
				buf.append( filename );
				}
				if ( queryType == Index.RANKED_QUERY ) {
				buf.append( "   " + String.format( "%.5f", results.get(i).score )); 
				}
				buf.append( "\n" );
			}
		    }
		    else {
			buf.append( "\nFound 0 matching document(s)\n\n" );
		    }
		    resultWindow.setText( buf.toString() );
		    resultWindow.setCaretPosition( 0 );
		}
	    };
	queryWindow.registerKeyboardAction( search,
					    "",
					    KeyStroke.getKeyStroke( "ENTER" ),
					    JComponent.WHEN_FOCUSED );
	
	Action relevanceFeedbackSearch = new AbstractAction() { 
		public void actionPerformed( ActionEvent e ) {
			// Check that a ranked search has been made prior to the relevance feedback
			StringBuffer buf = new StringBuffer();
			if (( results != null ) && ( queryType == Index.RANKED_QUERY )) {
			// Read user relevance feedback selections
			boolean[] docIsRelevant = { false, false, false, false, false, false, false, false, false, false }; 
			for ( int i = 0; i<10; i++ ) {
				docIsRelevant[i] = feedbackButton[i].isSelected(); 
			}
			// Expand the current search query with the documents marked as relevant 
			query.relevanceFeedback( results, docIsRelevant, indexer );
					
			// Perform a new search with the weighted and expanded query. Access to the index is 
			// synchronized since we don't want to search at the same time we're indexing new files
			// (this might corrupt the index).
			synchronized ( indexLock ) {
			results = indexer.index.search( query, queryType, rankingType );
			}
			buf.append( "\nSearch after relevance feedback:\n" );
			buf.append( "\nFound " + results.size() + " matching document(s)\n\n" );
			for ( int i=0; i<results.size(); i++ ) {
				buf.append( " " + i + ". " );
				String filename = indexer.index.docIDs.get( "" + results.get(i).docID );
				if ( filename == null ) {
				buf.append( "" + results.get(i).docID );
				}
				else {
				buf.append( filename );
				}
				buf.append( "   " + String.format( "%.5f", results.get(i).score ) + "\n" );
			}
			}
			else {
			buf.append( "\nThere was no returned ranked list to give feedback on.\n\n" );
			}
			resultWindow.setText( buf.toString() );
			resultWindow.setCaretPosition( 0 );
		}
		};
	feedbackExecutor.addActionListener( relevanceFeedbackSearch ); 	
		
	Action saveAndQuit = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    resultWindow.setText( "\n  Saving index..." );
		    indexer.index.cleanup();
		    System.exit( 0 );
		}
	    };
	saveItem.addActionListener( saveAndQuit );
	
	
	Action quit = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    System.exit( 0 );
		}
	    };
	quitItem.addActionListener( quit );

	
	Action setIntersectionQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.INTERSECTION_QUERY;
		}
	    };
	intersectionItem.addActionListener( setIntersectionQuery );
		
	Action setPhraseQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.PHRASE_QUERY;
		}
	    };
	phraseItem.addActionListener( setPhraseQuery );
		
	Action setRankedQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.RANKED_QUERY;
		}
	    };
	rankedItem.addActionListener( setRankedQuery );

	Action setTfidfRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.TF_IDF;
		}
		};
	tfidfItem.addActionListener( setTfidfRanking );
		
	Action setPagerankRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.PAGERANK;
		}
		};
	pagerankItem.addActionListener( setPagerankRanking );
		
	Action setCombinationRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.COMBINATION;
		}
		};
	combinationItem.addActionListener( setCombinationRanking );
		
	}
	
 
    /* ----------------------------------------------- */
   

    /**
     *   Calls the indexer to index the chosen directory structure.
     *   Access to the index is synchronized since we don't want to 
     *   search at the same time we're indexing new files (this might 
     *   corrupt the index).
     */
    private void index() {
	synchronized ( indexLock ) {
	    resultWindow.setText( "\n  Indexing, please wait..." );
	    for ( int i=0; i<dirNames.size(); i++ ) {
		File dokDir = new File( dirNames.get( i ));
		indexer.processFiles( dokDir );
	    }
	    resultWindow.setText( "\n  Done!" );
	}
    };


    /* ----------------------------------------------- */


    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
	int i=0, j=0;
	while ( i < args.length ) {
	    if ( "-i".equals( args[i] )) {
		i++;
		if ( j++ >= MAX_NUMBER_OF_INDEX_FILES ) {
		    System.err.println( "Too many index files specified" );
		    break;
		}
		if ( i < args.length ) {
		    indexFiles.add( args[i++] );
		}
	    } 
	    else if ( "-d".equals( args[i] )) {
		i++;
		if ( i < args.length ) {
		    dirNames.add( args[i++] );
		}
	    }
	    else if ( "-m".equals( args[i] )) {
		i++;
		indexType = Index.MEGA_INDEX;
	    }
	    else {
		System.err.println( "Unknown option: " + args[i] );
		break;
	    }
	}
	//  It might take a long time to create a MegaIndex. Meanwhile no searches
	//  should be carried out (it would result in a NullPointerException).
	//  Therefore the access to the index must be synchronized.
	synchronized ( indexLock ) {
	    if ( indexType == Index.HASHED_INDEX ) {
		indexer = new Indexer();
	    }
	    else {
		resultWindow.setText( "\n  Creating MegaIndex, please wait... " );
		indexer = new Indexer( indexFiles );
		resultWindow.setText( "\n  Done!" );
	    }
	}
    }				    


    /* ----------------------------------------------- */


    public static void main( String[] args ) {
	SearchGUI s = new SearchGUI();
	s.createGUI();
	s.decodeArgs( args );
	s.index();
    }

}
