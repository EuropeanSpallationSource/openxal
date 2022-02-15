/*
 *  LoggerWindow.java
 *
 *  Created on Tue Sep 14 12:51:14 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.application;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.util.*;
import java.util.logging.*;

/**
 * LoggerWindow
 *
 * @author tap
 * @since Sep 14, 2004
 */
class LoggerWindow extends JFrame {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * default logger window
     */
    protected static LoggerWindow defaultWindow;

    /**
     * logger handler
     */
    protected LoggerBuffer loggerHandler;

    /**
     * indicates whether this window has ever been shown
     */
    protected boolean neverShown;

    /**
     * logger table model
     */
    protected LogTableModel logTableModel;

    /**
     * the selected log record
     */
    protected LogRecord selectedRecord;

    /**
     * text view for displaying the selected log record's message
     */
    protected JTextArea selectedRecordMessageView;

    /**
     * text view for displaying the selected log record's exception if any
     */
    protected JTextArea selectedRecordExceptionView;

    /**
     * Primary constructor
     *
     * @param handler the handler for which to display the logged events
     * @param title the window's title
     */
    public LoggerWindow(final String title, final LoggerBuffer handler) {
        super(title);

        logTableModel = new LogTableModel();

        setLoggerHandler(handler);
        neverShown = true;

        makeView();
    }

    /**
     * Constructor
     */
    public LoggerWindow() {
        this("Event Log", LoggerBuffer.getRootHandler());
    }

    /**
     * Make the main view.
     */
    protected void makeView() {
        setSize(500, 400);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        Box mainView = new Box(BoxLayout.Y_AXIS);
        getContentPane().add(mainView);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, makeTable(), makeRecordInspector());
        mainSplit.setResizeWeight(0.5);

        mainView.add(makeTopButtonBar());
        mainView.add(mainSplit);
    }

    /**
     * Make a button bar at the top of the window.
     *
     * @return the button bar
     */
    protected Component makeTopButtonBar() {
        Box bar = new Box(BoxLayout.X_AXIS);
        bar.setBorder(BorderFactory.createEtchedBorder());

        bar.add(Box.createHorizontalGlue());

        JButton clearButton = new JButton("Clear");
        bar.add(clearButton);
        clearButton.addActionListener(event -> loggerHandler.clear());

        return bar;
    }

    /**
     * Make the table view.
     *
     * @return the table view
     */
    protected Component makeTable() {
        Box view = new Box(BoxLayout.Y_AXIS);
        // force the table to fill horizontally
        view.add(Box.createHorizontalStrut(10000));
        final JTable table = new JTable(logTableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        view.add(scrollPane);

        table.getSelectionModel().addListSelectionListener(event -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                LogTableModel tableModel = (LogTableModel) table.getModel();
                setSelectedRecord(tableModel.getRecord(selectedRow));
            } else {
                setSelectedRecord(null);
            }
        });

        return view;
    }

    /**
     * Make an inspector to display the log record's message and associated
     * exception if any.
     *
     * @return a log record inspector
     */
    protected Component makeRecordInspector() {
        selectedRecordMessageView = new JTextArea();
        selectedRecordExceptionView = new JTextArea();

        JTabbedPane tabbedView = new JTabbedPane();
        tabbedView.addTab("Message", new JScrollPane(selectedRecordMessageView));
        tabbedView.addTab("Exception", new JScrollPane(selectedRecordExceptionView));

        return tabbedView;
    }

    /**
     * Get the default logger window.
     *
     * @return The default logger window
     */
    public static LoggerWindow getDefault() {
        if (defaultWindow == null) {
            defaultWindow = new LoggerWindow();
        }

        return defaultWindow;
    }

    /**
     * Show the logger window. If the window has never been shown before, place
     * it relative to the sender, otherwise simply show it where it was last
     * placed by the user.
     *
     * @param sender The component relative to which the logger should be
     * positioned
     */
    public void showFirstTimeNear(final Component sender) {
        if (neverShown) {
            setLocationRelativeTo(sender);
            neverShown = false;
        }
        setVisible(true);
    }

    /**
     * Set the logger handler to the one specified.
     *
     * @param handler The new logger handler
     */
    public void setLoggerHandler(final LoggerBuffer handler) {
        if (loggerHandler != null) {
            loggerHandler.removeLoggerBufferListener(logTableModel);
        }

        loggerHandler = handler;

        if (handler != null) {
            handler.addLoggerBufferListener(logTableModel);
        }
    }

    /**
     * Set the selected record to the value specified.
     *
     * @param logRecord the new selected record
     */
    public void setSelectedRecord(final LogRecord logRecord) {
        if (logRecord != selectedRecord) {
            selectedRecord = logRecord;

            if (logRecord != null) {
                selectedRecordMessageView.setText(logRecord.getMessage());
                Throwable exception = logRecord.getThrown();
                String exceptionText = exception != null ? exception.toString() : "";
                selectedRecordExceptionView.setText(exceptionText);
            } else {
                selectedRecordMessageView.setText("");
                selectedRecordExceptionView.setText("");
            }
        }
    }
}

/**
 * LogTableModel is a table model for displaying the log records in a table.
 */
class LogTableModel extends AbstractTableModel implements LoggerBufferListener {

    private static final Logger LOGGER = Logger.getLogger(LogTableModel.class.getName());

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    static final int LEVEL_COLUMN = 0;
    static final int TIMESTAMP_COLUMN = 1;
    static final int CLASS_COLUMN = 2;
    static final int METHOD_COLUMN = 3;
    static final int MESSAGE_COLUMN = 4;
    static final int EXCEPTION_COLUMN = 5;

    /**
     * log records
     */
    protected final transient List<LogRecord> logRecords;

    /**
     * Map of level colors keyed by level
     */
    protected static Map<Level, String> levelColors;

    /**
     * static initializer
     */
    static {
        populateLevelColors();
    }

    /**
     * Constructor
     */
    public LogTableModel() {
        logRecords = new ArrayList<>();
    }

    /**
     * Populate the map of HTML colors corresponding to each log level.
     */
    protected static void populateLevelColors() {
        levelColors = new HashMap<>();

        levelColors.put(Level.CONFIG, "purple");
        levelColors.put(Level.FINE, "blue");
        levelColors.put(Level.FINER, "aqua");
        levelColors.put(Level.FINEST, "lime");
        levelColors.put(Level.INFO, "black");
        levelColors.put(Level.WARNING, "ff8800");
        levelColors.put(Level.SEVERE, "red");
        levelColors.put(null, "black");
    }

    /**
     * Get the table row count
     *
     * @return the table row count
     */
    @Override
    public int getRowCount() {
        synchronized (logRecords) {
            return logRecords.size();
        }
    }

    /**
     * Get the table column count
     *
     * @return the table column count
     */
    @Override
    public int getColumnCount() {
        return 6;
    }

    /**
     * Get the name for the specified column
     *
     * @param column the column for which to get the name
     * @return the name for the column
     */
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case LEVEL_COLUMN:
                return "Level";
            case TIMESTAMP_COLUMN:
                return "Timestamp";
            case CLASS_COLUMN:
                return "Source Class";
            case METHOD_COLUMN:
                return "Source Method";
            case MESSAGE_COLUMN:
                return "Message";
            case EXCEPTION_COLUMN:
                return "Exception";
            default:
                return "";
        }
    }

    /**
     * Get the value to display in the cell at the specified row and column.
     *
     * @param row the cell's row
     * @param column the cell's column
     * @return the value to display in the cell
     */
    @Override
    public Object getValueAt(final int row, final int column) {
        final LogRecord logRecord = getRecord(row);
        if (logRecord == null) {
            return null;
        }

        final Level level = logRecord.getLevel();
        final String color = getColor(level);
        Object value;

        switch (column) {
            case LEVEL_COLUMN:
                value = level;
                break;
            case TIMESTAMP_COLUMN:
                value = new Date(logRecord.getMillis());
                break;
            case CLASS_COLUMN:
                value = logRecord.getSourceClassName();
                break;
            case METHOD_COLUMN:
                value = logRecord.getSourceMethodName();
                break;
            case MESSAGE_COLUMN:
                value = logRecord.getMessage();
                break;
            case EXCEPTION_COLUMN:
                value = logRecord.getThrown();
                break;
            default:
                value = "";
                break;
        }

        value = value != null ? value : "";
        return getCellCode(color, value);
    }

    /**
     * Get the HTML code for the table cell which sets the font color of the
     * text describing the value for the cell.
     *
     * @param color HTML color
     * @param value value of the cell
     * @return The HTML describing the cell's value with the proper font color
     */
    protected static String getCellCode(final String color, final Object value) {
        return "<html><body><font color=" + color + ">" + value + "</font></body></html>";
    }

    /**
     * Get the HTML color for the specified log level.
     *
     * @param level the level for which to get the color
     * @return The HTML color to use for the specified level
     */
    protected static String getColor(final Level level) {
        return levelColors.get(level);
    }

    /**
     * Get the log record at the specified index.
     *
     * @param index the index of the record to get
     * @return the log record at the specified index
     */
    public LogRecord getRecord(final int index) {
        synchronized (logRecords) {
            try {
                return logRecords.get(index);
            } catch (ArrayIndexOutOfBoundsException exception) {
                LOGGER.log(Level.WARNING, null, exception);
                return null;
            }
        }
    }

    /**
     * Event indicating that the records in the logger buffer have changed.
     *
     * @param buffer the buffer whose records have changed
     * @param logRecords the new records in the buffer
     */
    @Override
    public void recordsChanged(LoggerBuffer buffer, List<LogRecord> logRecords) {
        synchronized (this.logRecords) {
            this.logRecords.clear();
            this.logRecords.addAll(logRecords);
            fireTableDataChanged();
        }
    }
}
