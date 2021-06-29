//
//  KeyValueRecordSelector.java
//  xal
//
//  Created by Tom Pelaia on 2/6/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.widgets.swing;

import xal.tools.ResourceManager;
import xal.extension.bricks.WindowReference;

import java.net.URL;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * display a dialog that allows users to select records from a table
 */
public class KeyValueRecordSelector<RecordType> {

    /**
     * dialog box for channel selection
     */
    private final JDialog dialog;

    /**
     * table displaying the records from which to select
     */
    private final JTable recordTable;

    /**
     * table model for the records to display
     */
    private final KeyValueFilteredTableModel<RecordType> recordTableModel;

    /**
     * indicates whether the user confirmed the selections
     */
    private volatile boolean isConfirmed;

    /**
     * Primary Constructor
     */
    protected KeyValueRecordSelector(final KeyValueFilteredTableModel<RecordType> tableModel, final JFrame owner, final String title, final String filterPrompt) {
        final URL uiURL = ResourceManager.getResourceURL(KeyValueRecordSelector.class, "RecordSelector.bricks");
        final WindowReference windowReference = new WindowReference(uiURL, "RecordSelectorDialog", owner, title);
        dialog = (JDialog) windowReference.getWindow();
        dialog.setTitle(title);

        recordTableModel = tableModel;

        recordTable = (JTable) windowReference.getView("RecordTable");
        recordTable.setModel(tableModel);
        recordTable.setAutoCreateRowSorter(true);
        recordTable.addMouseListener(newDoubleClickHandler());

        final JTextField filterField = (JTextField) windowReference.getView("FilterField");
        tableModel.setInputFilterComponent(filterField);
        filterField.putClientProperty("JTextField.variant", "search");
        filterField.putClientProperty("JTextField.Search.Prompt", filterPrompt);

        final JButton cancelButton = (JButton) windowReference.getView("CancelButton");
        cancelButton.addActionListener(getCancelHandler());

        final JButton okayButton = (JButton) windowReference.getView("OkayButton");
        okayButton.addActionListener(getOkayHandler());

        final JButton clearFilterButton = (JButton) windowReference.getView("ClearFilterButton");
        clearFilterButton.addActionListener(getClearFilterHandler(filterField));
    }

    /**
     * Constructor with default filter prompt
     */
    protected KeyValueRecordSelector(final KeyValueFilteredTableModel<RecordType> tableModel, final JFrame owner, final String title) {
        this(tableModel, owner, title, "Record filter");
    }

    /**
     * Get an instance of a record selector for allowing users to select records
     * (of the specified type) from a list in a table.
     *
     * @param tableModel table model supplying the records to display
     * @param owner the window that owns the dialog window
     * @param title the title of the dialog window
     */
    public static <RecordType> KeyValueRecordSelector<RecordType> getInstance(final KeyValueFilteredTableModel<RecordType> tableModel, final JFrame owner, final String title) {
        return new KeyValueRecordSelector<>(tableModel, owner, title);
    }

    /**
     * Get an instance of a record selector for allowing users to select records
     * (of the specified type) from a list in a table.
     *
     * @param records the objects of the specified types to list for selection
     * @param owner the window that owns the dialog window
     * @param title the title of the dialog window
     * @param keyPaths are the key paths applied to each record to supply the
     * table's column data
     */
    public static <RecordType> KeyValueRecordSelector<RecordType> getInstance(final List<RecordType> records, final JFrame owner, final String title, final String... keyPaths) {
        return new KeyValueRecordSelector<>(new KeyValueFilteredTableModel<>(records, keyPaths), owner, title);
    }

    /**
     * Get an instance of a record selector for allowing users to select records
     * (of the specified type) from a list in a table.
     *
     * @param tableModel table model supplying the records to display
     * @param owner the window that owns the dialog window
     * @param title the title of the dialog window
     * @param filterPrompt the prompt to appear as a placeholder in the filter
     * field
     */
    public static <RecordType> KeyValueRecordSelector<RecordType> getInstanceWithFilterPrompt(final KeyValueFilteredTableModel<RecordType> tableModel, final JFrame owner, final String title, final String filterPrompt) {
        return new KeyValueRecordSelector<>(tableModel, owner, title, filterPrompt);
    }

    /**
     * Get an instance of a record selector for allowing users to select records
     * (of the specified type) from a list in a table.
     *
     * @param records the objects of the specified types to list for selection
     * @param owner the window that owns the dialog window
     * @param title the title of the dialog window
     * @param filterPrompt the prompt to appear as a placeholder in the filter
     * field
     * @param keyPaths are the key paths applied to each record to supply the
     * table's column data
     */
    public static <RecordType> KeyValueRecordSelector<RecordType> getInstanceWithFilterPrompt(final List<RecordType> records, final JFrame owner, final String title, final String filterPrompt, final String... keyPaths) {
        return new KeyValueRecordSelector<>(new KeyValueFilteredTableModel<>(records, keyPaths), owner, title, filterPrompt);
    }

    /**
     * get the table which displays the records from which to select
     */
    public JTable getRecordTable() {
        return recordTable;
    }

    /**
     * get the table model for displaying the records
     */
    public KeyValueFilteredTableModel<RecordType> getRecordTableModel() {
        return recordTableModel;
    }

    /**
     * Show the dialog with single record selection allowed and return the
     * selected record if any.
     *
     * @return the selected record or null if no record was selected or the
     * dialog was canceled
     */
    public RecordType showSingleSelectionDialog() {
        final List<RecordType> records = showDialog(ListSelectionModel.SINGLE_SELECTION);
        return records != null && records.size() > 0 ? records.get(0) : null;
    }

    /**
     * Show the dialog with arbitrary record selection allowed and return a list
     * of selected records.
     *
     * @return the selected records or null if the dialog was canceled
     */
    public List<RecordType> showDialog() {
        return showDialog(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    /**
     * Show the dialog with the specified record selection mode and return a
     * list of selected records.
     *
     * @param selectionMode record selection mode:
     * ListSelectionModel.SINGLE_SELECTION,
     * ListSelectionModel.SINGLE_INTERVAL_SELECTION or
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
     * @return the selected records or null if the dialog was canceled
     */
    public List<RecordType> showDialog(final int selectionMode) {
        isConfirmed = false;

        recordTable.setSelectionMode(selectionMode);

        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);

        return isConfirmed ? getSelectedRecords() : null;
    }

    /**
     * perform the Okay action
     */
    private void performOkay() {
        isConfirmed = true;
        dialog.setVisible(false);
    }

    /**
     * get the list of selected records
     */
    protected List<RecordType> getSelectedRecords() {
        final RowSorter<?> sorter = recordTable.getRowSorter();
        final int[] selectedRows = recordTable.getSelectedRows();
        final List<RecordType> records = new ArrayList<>(selectedRows.length);
        for (final int row : selectedRows) {
            final int modelRow = sorter.convertRowIndexToModel(row);
            records.add(recordTableModel.getRecordAtRow(modelRow));
        }
        return records;
    }

    /**
     * get the handler for the cancel button
     */
    private ActionListener getCancelHandler() {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                dialog.setVisible(false);
                isConfirmed = false;
            }
        };
    }

    /**
     * get the handler for the okay button
     */
    private ActionListener getOkayHandler() {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                performOkay();
            }
        };
    }

    /**
     * get the handler for clearing the filter field
     */
    private ActionListener getClearFilterHandler(final JTextField filterField) {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                filterField.setText("");
            }
        };
    }

    /**
     * handle the double click event on the table for selecting a row
     */
    private MouseListener newDoubleClickHandler() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if (event.getClickCount() == 2) {		// double click
                    performOkay();
                }
            }
        };
    }
}
