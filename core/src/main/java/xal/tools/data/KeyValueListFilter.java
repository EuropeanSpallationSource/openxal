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

/**
 * filter a list of objects according to the values associated with the
 * specified keys
 */
public class KeyValueListFilter<T> {

    /**
     * Key Value adaptor for getting keyed values from an object
     */
    private final KeyValueAdaptor keyValueAdaptor;

    /**
     * index of keyed values as strings for all records
     */
    private final List<RecordIndex<T>> recordIndexes;

    /**
     * keys corresponding to an object's keyed values to use for matching
     */
    private String[] matchingKeyPaths;

    /**
     * list of records to filter
     */
    private List<T> allRecords;

    /**
     * Primary Constructor
     *
     * @param adaptor key value adaptor to use to get the keyed values for the
     * objects
     * @param allRecords all of the objects to filter
     * @param matchingKeyPaths the key paths corresponding to an object's keyed
     * values to use for matching
     */
    public KeyValueListFilter(final KeyValueAdaptor adaptor, final List<T> allRecords, final String... matchingKeyPaths) {
        keyValueAdaptor = adaptor;
        recordIndexes = new ArrayList<>(allRecords.size());
        setMatchingKeyPaths(matchingKeyPaths);
        setAllRecords(allRecords);
    }

    /**
     * Set the list of all objects to filter
     */
    public void setAllRecords(final List<T> allRecords) {
        this.allRecords = allRecords;
        indexRecords();
    }

    /**
     * Set the matching key paths
     */
    public void setMatchingKeyPaths(final String... matchingKeyPaths) {
        this.matchingKeyPaths = matchingKeyPaths;
        indexRecords();
    }

    /**
     * index all records by the keyed values in the list
     */
    public void indexRecords() {
        recordIndexes.clear();
        if (allRecords != null) {
            for (final T recordType : allRecords) {
                recordIndexes.add(RecordIndex.getInstance(recordType, keyValueAdaptor, matchingKeyPaths));
            }
        }
    }

    /**
     * re-index the specified record (e.g. if a value in the record has changed
     * )
     */
    public void reIndexRecord(final T recordType) {
        int count = recordIndexes.size();
        for (int index = 0; index < count; index++) {
            final RecordIndex<T> recordIndex = recordIndexes.get(index);
            if (recordType == recordIndex.getRecord()) {
                final RecordIndex<T> newRecordIndex = RecordIndex.getInstance(recordType, keyValueAdaptor, matchingKeyPaths);
                recordIndexes.set(index, newRecordIndex);
                return;
            }
        }
    }

    /**
     * Filter the records for those that match (case insensitive) every word in
     * the specified text.
     *
     * @param text the text whose every word is matched against each record
     * @param matchingRecords the container (first gets cleared) into which the
     * matching records are placed preserving order
     */
    public void filterRecordsTo(final String text, final List<T> matchingRecords) {
        matchingRecords.clear();

        final String lowerText = text != null ? text.toLowerCase() : "";
        final String[] words = lowerText.split("\\s");

        for (final RecordIndex<T> recordIndex : recordIndexes) {
            if (recordIndex.matchesAllWords(words)) {
                matchingRecords.add(recordIndex.getRecord());
            }
        }
    }

    /**
     * Filter the records for those that match (case insensitive) every word in
     * the specified text.
     *
     * @param text the text whose every word is matched against each record
     * @return the list of matching records preserving order
     */
    public List<T> filterRecords(final String text) {
        final List<T> matchingRecords = new ArrayList<>();
        filterRecordsTo(text, matchingRecords);
        return matchingRecords;
    }
}

/**
 * index of an object's values (as lower case strings) for the specified key
 * paths
 */
class RecordIndex<T> {

    /**
     * record which is indexed
     */
    private final T recordType;

    /**
     * string of indexed words
     */
    private final String indexedWords;

    /**
     * Constructor
     */
    private RecordIndex(final T recordType, final String indexedWords) {
        this.recordType = recordType;
        this.indexedWords = indexedWords;
    }

    /**
     * index values of the specified record corresponding to the specified keys
     */
    public static <T> RecordIndex<T> getInstance(final T recordType, final KeyValueAdaptor adaptor, final String[] keyPaths) {
        // store words using a space to separate them from each other
        final StringJoiner buffer = new StringJoiner(" ");
        for (final String keyPath : keyPaths) {
            final Object value = adaptor.valueForKeyPath(recordType, keyPath);
            final String stringValue = value != null ? value.toString().toLowerCase() : null;
            if (stringValue != null) {
                buffer.append(stringValue);
            }
        }
        return new RecordIndex<>(recordType, buffer.toString());
    }

    /**
     * get the record
     */
    public T getRecord() {
        return recordType;
    }

    /**
     * determine whether the record matches all of the specified words
     */
    public boolean matchesAllWords(final String[] words) {
        for (final String word : words) {
            if (!matchesWord(word)) {
                return false;
            }
        }
        return true;
    }

    /**
     * determine whether the record matches the specified word
     */
    public boolean matchesWord(final String word) {
        return word != null ? indexedWords.contains(word) : false;
    }
}
