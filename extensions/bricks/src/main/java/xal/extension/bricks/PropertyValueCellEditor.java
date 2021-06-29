//
//  PropertyValueCellEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;

/**
 * cell editor for property values
 */
/**
 * cell editor for property values
 */
/**
 * cell editor for property values
 */
/**
 * cell editor for property values
 */
public class PropertyValueCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * context in which bricks is run
     */
    private final BricksContext context;

    protected final PropertyValueEditorManager propertyEditorManager;
    protected final PropertyTableModel tableModel;
    protected PropertyValueEditor<?> currentEditor;

    /**
     * Constructor
     */
    public PropertyValueCellEditor(final BricksContext context, final JTable table, final PropertyValueEditorManager propertyEditorManager, final PropertyTableModel tableModel) {
        this.context = context;
        this.propertyEditorManager = propertyEditorManager;
        this.tableModel = tableModel;
    }

    /**
     * get the editor for the property corresponding to the specified table row
     */
    protected PropertyValueEditor<?> getEditor(final int row) {
        final Class<?> propertyClass = tableModel.getPropertyClass(row);
        PropertyValueEditor<?> editor;

        if (propertyClass != null && propertyEditorManager.hasEditor(propertyClass)) {
            editor = propertyEditorManager.getEditor(propertyClass);
        } else {
            editor = propertyEditorManager.getEditor(String.class);
        }

        return editor;
    }

    /**
     * get the component
     */
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        currentEditor = getEditor(row);
        currentEditor.setCurrentCellEditor(this);
        currentEditor.setEditorValue(value);
        return currentEditor.getEditorComponent();
    }

    /**
     * get the cell editor value
     */
    @Override
    public Object getCellEditorValue() {
        return currentEditor.getEditorValue(context);
    }

    /**
     * Get the table component.
     */
    @Override
    public final Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final PropertyValueEditor<?> editor = getEditor(row);
        final JComponent component = (JComponent) editor.getRenderingComponent();
        editor.setRenderingValue(value);
        component.setOpaque(isSelected);
        return component;
    }

    /**
     * Make this method public
     */
    @Override
    public void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
