//
//  KeyValueListFilter.java
//  xal
//
//  Created by Tom Pelaia on 2/3/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import xal.tools.StringJoiner;

import java.util.ArrayList;
import java.util.List;


/** filter a list of objects according to the values associated with the specified keys */
public class KeyValueListFilter<RecordType> {
	/** Key Value adaptor for getting keyed values from an object */
	private final KeyValueAdaptor keyValueAdaptor;
	
	/** index of keyed values as strings for all records */
	private final List<RecordIndex<RecordType>> recordIndexes;
	
	/** keys corresponding to an object's keyed values to use for matching */
	private String[] matchingKeyPaths;
	
	/** list of records to filter */
	private List<RecordType> allRecords;
	
	
	/** 
	 * Primary Constructor 
	 * @param adaptor key value adaptor to use to get the keyed values for the objects
	 * @param allRecords all of the objects to filter
	 * @param matchingKeyPaths the key paths corresponding to an object's keyed values to use for matching
	 */
	public KeyValueListFilter( final KeyValueAdaptor adaptor, final List<RecordType> allRecords, final String ... matchingKeyPaths ) {
		keyValueAdaptor = adaptor;
		recordIndexes = new ArrayList<>( allRecords.size() );
		setMatchingKeyPaths( matchingKeyPaths );
		setAllRecords( allRecords );
	}
	
	
	/** Set the list of all objects to filter */
	public void setAllRecords( final List<RecordType> allRecords ) {
		this.allRecords = allRecords;
		indexRecords();
	}
	
	
	/** Set the matching key paths */
	public void setMatchingKeyPaths( final String ... matchingKeyPaths ) {
		this.matchingKeyPaths = matchingKeyPaths;
		indexRecords();
	}
	
	
	/** index all records by the keyed values in the list */
	public void indexRecords() {
		recordIndexes.clear();
		final List<RecordType> records = allRecords;
		final String[] matchingKeyPaths = this.matchingKeyPaths;
		if ( records != null ) {
			for ( final RecordType record : records ) {
				recordIndexes.add( RecordIndex.getInstance( record, keyValueAdaptor, matchingKeyPaths ) );
			}
		}
	}
	
	
	/** re-index the specified record (e.g. if a value in the record has changed ) */
	public void reIndexRecord( final RecordType record ) {
		final String[] matchingKeyPaths = this.matchingKeyPaths;
		final int count = recordIndexes.size();
		for ( int index = 0 ; index < count ; index++ ) {
			final RecordIndex<RecordType> recordIndex = recordIndexes.get( index );
			if ( record == recordIndex.getRecord() ) {
				final RecordIndex<RecordType> newRecordIndex = RecordIndex.getInstance( record, keyValueAdaptor, matchingKeyPaths );
				recordIndexes.remove( index );
				recordIndexes.add( index, newRecordIndex );
				return;
			}
		}
	}
	
	
	/** 
	 * Filter the records for those that match (case insensitive) every word in the specified text.
	 * @param text the text whose every word is matched against each record
	 * @param matchingRecords the container (first gets cleared) into which the matching records are placed preserving order
	 */
	public void filterRecordsTo( final String text, final List<RecordType> matchingRecords ) {
		matchingRecords.clear();
		
		final String lowerText = text != null ? text.toLowerCase() : "";
		final String[] words = lowerText.split( "\\s" );
		
		for ( final RecordIndex<RecordType> recordIndex : recordIndexes ) {
			if ( recordIndex.matchesAllWords( words ) ) {
				matchingRecords.add( recordIndex.getRecord() );
			}
		}
	}
	
	
	/** 
	 * Filter the records for those that match (case insensitive) every word in the specified text.
	 * @param text the text whose every word is matched against each record
	 * @return the list of matching records preserving order
	 */
	public List<RecordType> filterRecords( final String text ) {
		final List<RecordType> matchingRecords = new ArrayList<>();
		filterRecordsTo( text, matchingRecords );
		return matchingRecords;
	}
}



/** index of an object's values (as lower case strings) for the specified key paths */
class RecordIndex<RecordType> {
	/** record which is indexed */
	private final RecordType record;
	
	/** string of indexed words */
	private final String indexedWords;
	
	
	/** Constructor */
	private RecordIndex( final RecordType record, final String indexedWords ) {
		this.record = record;
		this.indexedWords = indexedWords;
	}
	
	
	/** index values of the specified record corresponding to the specified keys */
	public static <RecordType> RecordIndex<RecordType> getInstance( final RecordType record, final KeyValueAdaptor adaptor, final String[] keyPaths ) {
		final StringJoiner buffer = new StringJoiner( " " );	// store words using a space to separate them from each other
		for ( final String keyPath : keyPaths ) {
			final Object value = adaptor.valueForKeyPath( record, keyPath );
			final String stringValue = value != null ? value.toString().toLowerCase() : null;
			if ( stringValue != null ) buffer.append( stringValue );
		}
		return new RecordIndex<>( record, buffer.toString() );
	}
	
	
	/** get the record */
	public RecordType getRecord() {
		return record;
	}
	
	
	/** determine whether the record matches all of the specified words */
	public boolean matchesAllWords( final String[] words ) {
		for ( final String word : words ) {
			if ( !matchesWord( word ) )  return false;
		}
		return true;
	}
	
	
	/** determine whether the record matches the specified word */
	public boolean matchesWord( final String word ) {
		return word != null ? indexedWords.contains( word ) : false;
	}
}
