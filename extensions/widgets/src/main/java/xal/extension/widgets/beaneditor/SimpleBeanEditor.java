/*
 * SimpleBeanEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Tom Pelaia
 * @author Patrick Scruggs
 */
package xal.extension.widgets.beaneditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.table.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.widgets.swing.*;
import xal.tools.data.*;

/**
 * SimpleBeanEditor
 */
public class SimpleBeanEditor<T> extends JDialog {

    /**
     * Private serializable version ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Table model of property records
     */
    private final KeyValueFilteredTableModel<PropertyRecord> propertyTableModel;

    /**
     * List of properties that appear in the properties table
     */
    private final List<PropertyRecord> beanPropertyRecords;

    /**
     * Bean that is being edited
     */
    private final T bean;

    /**
     * model column for the value in the property table
     */
    private final int propertyTableValueColumn;

    private JTable propertyTable;
    private Box controlPanel;
    private static final Logger LOGGER = Logger.getLogger(SimpleBeanEditor.class.getName());

    public SimpleBeanEditor(final Frame owner, final String dialogTitle, final String beanName, final T bean) {
        this(owner, dialogTitle, beanName, bean, true, true);
    }

    /* Constructor that takes a window parent
     * and a bean to fetch properties from
     */
    public SimpleBeanEditor(final Frame owner, final String dialogTitle, final String beanName, final T bean, boolean bottomButtons, boolean visible) {
        //Set JDialog's owner, title, and modality
        super(owner, dialogTitle, true);

        // Set the bean to edit
        this.bean = bean;

        // generate the bean property tree
        final EditablePropertyContainer probePropertyTree = EditableProperty.getInstanceWithRoot(beanName, bean);

        beanPropertyRecords = PropertyRecord.toRecords(probePropertyTree);

        propertyTableModel = new KeyValueFilteredTableModel<>(beanPropertyRecords, "displayLabel", "value", "units");
        // match on the path
        propertyTableModel.setMatchingKeyPaths("path");
        propertyTableModel.setColumnName("displayLabel", "Property");
        // the value is editable if the record is editable
        propertyTableModel.setColumnEditKeyPath("value", "editable");
        // store the column for the "value" key path
        propertyTableValueColumn = propertyTableModel.getColumnForKeyPath("value");

        // Set the window size
        setSize(600, 600);
        // Set up each component in the editor
        initializeComponents(bottomButtons);
        // Center the editor in relation to the frame that constructed the editor
        setLocationRelativeTo(owner);
        // Make the window visible
        setVisible(visible);
    }

    /**
     * Get the probe to edit
     *
     * @return probe associated with this editor
     */
    public T getBean() {
        return bean;
    }

    /**
     * publish record values to the bean
     */
    protected void publishToBean() {
        for (final PropertyRecord aRecord : beanPropertyRecords) {
            aRecord.publishIfNeeded();
        }
        propertyTableModel.fireTableDataChanged();
    }

    /**
     * revert the record values from the bean (if changed by the user)
     */
    protected void revertFromBean() {
        for (final PropertyRecord aRecord : beanPropertyRecords) {
            aRecord.revertIfNeeded();
        }
        propertyTableModel.fireTableDataChanged();
    }

    /**
     * reload all the record values from the bean (changed by external code)
     */
    protected void reloadBean() {
        final EditablePropertyContainer probePropertyTree = EditableProperty.getInstanceWithRoot("", bean);
        beanPropertyRecords.clear();
        beanPropertyRecords.addAll(PropertyRecord.toRecords(probePropertyTree));
        propertyTableModel.setRecords(beanPropertyRecords);
    }

    /**
     * Initialize the components of the bean editor
     */
    protected void initializeComponents(boolean bottomButtons) {
        //main view containing all components
        final Box mainContainer = new Box(BoxLayout.Y_AXIS);

        //Table containing the properties that can be modified
        propertyTable = new JTable() {
            /**
             * Serializable version ID
             */
            private static final long serialVersionUID = 1L;

            /**
             * renderer for a table section
             */
            private final TableCellRenderer sectionRenderer = makeSectionRenderer();

            //Get the cell editor for the table
            @Override
            public TableCellEditor getCellEditor(final int row, final int column) {
                //Value at [row, col] of the table
                final Object value = getValueAt(row, column);

                if (value == null) {
                    return super.getCellEditor(row, column);
                } else if (value instanceof Enum) {
                    return new DefaultCellEditor(new JComboBox<Object>(((Enum<?>) value).getDeclaringClass().getEnumConstants()) {
                        private static final long serialVersionUID = 1L;

                        {
                            setForeground(getSelectionForeground());
                            setBackground(getSelectionBackground());
                        }
                    });
                } else {
                    return getDefaultEditor(value.getClass());
                }
            }

            //Get the cell renderer for the table to change how values are displayed
            @Override
            public TableCellRenderer getCellRenderer(final int row, final int column) {
                // index of the record in the model
                final int recordIndex = this.convertRowIndexToModel(row);
                final PropertyRecord aRecord = propertyTableModel.getRecordAtRow(recordIndex);
                final Object value = getValueAt(row, column);

                //Set the renderer according to the property type (e.g. Boolean => checkbox display, numeric => right justified)
                if (!aRecord.isEditable()) {
                    return sectionRenderer;
                } else if (value == null) {
                    return super.getCellRenderer(row, column);
                } else if (value instanceof Enum) {
                    final JComboBox<Object> combo = new JComboBox<>(((Enum<?>) value).getDeclaringClass().getEnumConstants());
                    setRowHeight(row, (int) combo.getPreferredSize().getHeight());

                    return (table, newValue, isSelected, hasFocus, selRow, selColumn) -> {
                        if (isSelected) {
                            combo.setForeground(table.getSelectionForeground());
                            combo.setBackground(table.getSelectionBackground());
                        } else {
                            combo.setForeground(table.getForeground());
                            combo.setBackground(table.getBackground());
                        }
                        combo.setSelectedItem(newValue);
                        return combo;
                    };
                } else {
                    final TableCellRenderer renderer = getDefaultRenderer(value.getClass());
                    if (renderer instanceof DefaultTableCellRenderer) {
                        final DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer) renderer;
                        final int modelColumn = convertColumnIndexToModel(column);
                        // highlight the cell if the column corresponds to the value and it has unpublished changes
                        defaultRenderer.setForeground(modelColumn == propertyTableValueColumn && aRecord.hasChanges() ? Color.BLUE : Color.BLACK);
                    }
                    return renderer;
                }
            }

            private TableCellRenderer makeSectionRenderer() {
                final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setBackground(Color.GRAY);
                renderer.setForeground(Color.WHITE);
                return renderer;
            }
        };

        //Set the table to allow one-click edit
        ((DefaultCellEditor) propertyTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
        propertyTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public void setValue(Object value) {
                setText((value == null) ? "" : String.format(Locale.ROOT, "%10.7g", value));
            }
        });

        //Resize the last column
        propertyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        //Allow single selection only
        propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Set the model to the table
        propertyTable.setModel(propertyTableModel);

        //Configure the text field to filter the table
        final JTextField filterTextField = new JTextField();
        filterTextField.setMaximumSize(new Dimension(32000, filterTextField.getPreferredSize().height));
        filterTextField.putClientProperty("JTextField.variant", "search");
        filterTextField.putClientProperty("JTextField.Search.Prompt", "Property Filter");
        propertyTableModel.setInputFilterComponent(filterTextField);
        mainContainer.add(filterTextField, BorderLayout.NORTH);

        //Add the scrollpane to the table with a vertical scrollbar
        final JScrollPane scrollPane = new JScrollPane(propertyTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainContainer.add(scrollPane);

        //Add everything to the dialog
        add(mainContainer);

        if (bottomButtons) {
            mainContainer.add(initializeControlPanel());
        }
    }

    protected Box initializeControlPanel() {

        // button to revert changes back to last saved state
        final JButton revertButton = new JButton("Revert");
        revertButton.setToolTipText("Revert values back.");
        revertButton.setEnabled(false);

        // button to publish changes
        final JButton publishButton = new JButton("Publish");
        publishButton.setToolTipText("Publish values.");
        publishButton.setEnabled(false);

        // button to publish changes and dismiss the panel
        final JButton okayButton = new JButton("Okay");
        okayButton.setToolTipText("Publish values and dismiss the dialog.");
        okayButton.setEnabled(true);

        //Add the action listener as the ApplyButtonListener
        revertButton.addActionListener(event -> {
            revertFromBean();
            revertButton.setEnabled(false);
            publishButton.setEnabled(false);
        });

        //Add the action listener as the ApplyButtonListener
        publishButton.addActionListener(event -> {
            publishToBean();
            revertButton.setEnabled(false);
            publishButton.setEnabled(false);
        });

        //Add the action listener as the ApplyButtonListener
        okayButton.addActionListener(event -> {
            try {
                publishToBean();
                dispose();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(SimpleBeanEditor.this, exception.getMessage(), "Error Publishing", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.WARNING, "Exception publishing values: ", exception);
            }
        });

        propertyTableModel.addKeyValueRecordListener(new KeyValueRecordListener<KeyValueTableModel<PropertyRecord>, PropertyRecord>() {
            @Override
            public void recordModified(final KeyValueTableModel<PropertyRecord> source, final PropertyRecord aRecord, final String keyPath, final Object value) {
                revertButton.setEnabled(true);
                publishButton.setEnabled(true);
            }
        });

        //Add the buttons to the bottom of the dialog
        controlPanel = new Box(BoxLayout.X_AXIS);
        controlPanel.add(revertButton);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(publishButton);
        controlPanel.add(okayButton);

        return controlPanel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        propertyTable.setEnabled(enabled);
        if (controlPanel != null) {
            controlPanel.setEnabled(enabled);
        }
    }
}
