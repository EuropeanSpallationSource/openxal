//
//  KeyValueFilteredTableModel.java
//  xal
//
//  Created by Tom Pelaia on 2/5/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.widgets.swing;

import java.util.ArrayList;
import xal.tools.FreshProcessor;
import xal.tools.data.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import javax.swing.event.*;

/**
 * Key Value Table Model with built in support for filtering through a text
 * input document
 */
public class KeyValueFilteredTableModel<T> extends KeyValueTableModel<T> {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(KeyValueFilteredTableModel.class.getName());

    /**
     * handles the input events and filters the table records accordingly
     */
    private final InputFilterHandler inputFilterHandler;

    /**
     * record filter
     */
    private final KeyValueListFilter<T> recordFilter;

    /**
     * input document used to specify filtering text
     */
    private Document inputFilterDocument;

    /**
     * list of all records to filter
     */
    private List<T> allRecords;

    /**
     * Primary Constructor
     *
     * @param records the list of objects (one record for each table row)
     * @param keyPaths specifies the array of key paths to get the data to
     * display (one key path for each column)
     */
    public KeyValueFilteredTableModel(final List<T> records, final String... keyPaths) {
        super(records, keyPaths);

        recordFilter = new KeyValueListFilter<>(keyValueAdaptor, records, keyPaths);

        inputFilterHandler = new InputFilterHandler();
        setInputFilterDocument(null);

        filterRecords();
    }

    /**
     * Empty Constructor
     */
    public KeyValueFilteredTableModel() {
        this(new ArrayList<>(), "toString");
    }

    /**
     * Set the value of the specified cell
     */
    @Override
    public void setValueAt(final Object value, final int row, final int column) {
        super.setValueAt(value, row, column);
        final T aRecord = getRecordAtRow(row);
        if (aRecord != null) {
            recordFilter.reIndexRecord(aRecord);
            filterRecords();
        }
    }

    /**
     * set the specified text input document as the text source for filtering
     * table records
     */
    public void setInputFilterDocument(final Document document) {
        if (inputFilterDocument != null) {
            inputFilterDocument.removeDocumentListener(inputFilterHandler);
            // clear pending requests if any
            inputFilterHandler.clear();
        }

        inputFilterDocument = document;

        if (document != null) {
            document.addDocumentListener(inputFilterHandler);
        }
    }

    /**
     * set the specified text component as the text source for filtering table
     * records
     */
    public void setInputFilterComponent(final JTextComponent component) {
        setInputFilterDocument(component != null ? component.getDocument() : null);
    }

    /**
     * set the key paths to use for matching
     */
    public void setMatchingKeyPaths(final String... keyPaths) {
        recordFilter.setMatchingKeyPaths(keyPaths);
        filterRecords();
    }

    /**
     * Overrides the inherited method to set all of the records (before
     * filtering)
     *
     * @param records the list of objects
     */
    @Override
    public void setRecords(final List<T> records) {
        setAllRecords(records);
    }

    /**
     * Set all of the records (before filtering)
     *
     * @param records the list of objects
     */
    private void setAllRecords(final List<T> records) {
        allRecords = records;
        if (recordFilter != null) {
            recordFilter.setAllRecords(records);
            filterRecords();
        }
        fireTableDataChanged();
    }

    /**
     * set the filtered records to display in the table
     */
    private void setFilteredRecords(final List<T> records) {
        super.setRecords(records);
    }

    /**
     * apply the filter to all records
     */
    private void filterRecords() {
        final Document document = inputFilterDocument;
        if (document != null) {
            final String text = getText(document);
            filterRecords(text);
        } else {
            setFilteredRecords(allRecords);
        }
    }

    /**
     * get the text from the specified document
     */
    private String getText(final Document document) {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return "";
        }
    }

    /**
     * apply the filter to all records
     */
    private void filterRecords(final String text) {
        if (recordFilter != null) {
            setFilteredRecords(recordFilter.filterRecords(text));
        } else {
            setFilteredRecords(allRecords);
        }
    }

    /**
     * listens for text input changes and filters the table records accordingly
     */
    private class InputFilterHandler implements DocumentListener {

        /**
         * processor for filtering records
         */
        private final FreshProcessor filterProcessor;

        /**
         * Constructor
         */
        public InputFilterHandler() {
            filterProcessor = new FreshProcessor();
        }

        /**
         * clear pending requests
         */
        public void clear() {
            filterProcessor.clear();
        }

        /**
         * handle the input change event
         */
        @Override
        public void changedUpdate(final DocumentEvent event) {
            recordsNeedsFiltering(event);
        }

        /**
         * handle the input insert event
         */
        @Override
        public void insertUpdate(final DocumentEvent event) {
            recordsNeedsFiltering(event);
        }

        /**
         * handle the input remove event
         */
        @Override
        public void removeUpdate(final DocumentEvent event) {
            recordsNeedsFiltering(event);
        }

        /**
         * filter the records based upon the latest input text
         */
        private void recordsNeedsFiltering(final DocumentEvent event) {
            final Document document = event.getDocument();
            final String text = getText(document);
            filterProcessor.post(new FilterRecordsRequest(text));
        }
    }

    /**
     * request to filter records in the table model according to the specified
     * text
     */
    private class FilterRecordsRequest implements Runnable {

        /**
         * text with which to filter the records
         */
        private final String filterText;

        /**
         * Constructor
         */
        public FilterRecordsRequest(final String text) {
            filterText = text;
        }

        /**
         * filter the records in the table
         */
        @Override
        public void run() {
            filterRecords();
        }
    }
}
