//
//  ViewInspectorTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 7/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.beans.*;
import javax.swing.table.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.apputils.ApplicationSupport;

/**
 * Inspector table model
 */
class ViewInspectorTableModel extends AbstractTableModel implements PropertyTableModel {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ViewInspectorTableModel.class.getName());

    public static final int NAME_COLUMN = 0;
    public static final int VALUE_COLUMN = 1;

    protected final PropertyDescriptor[] propertyDescriptors;
    protected final BeanNode<?> beanNode;
    protected final PropertyValueEditorManager propertyValueEditorManager;

    /**
     * Constructor
     */
    public ViewInspectorTableModel(final BeanNode<?> node, final PropertyValueEditorManager propertyEditorManager) {
        beanNode = node;
        propertyValueEditorManager = propertyEditorManager;

        if (node != null) {
            final BeanInfo beanInfo = node.getBeanObjectBeanInfo();
            final PropertyDescriptor[] descriptors = beanInfo != null ? beanInfo.getPropertyDescriptors() : new PropertyDescriptor[0];
            final List<PropertyDescriptor> editableDescriptors = new ArrayList<>(descriptors.length);
            for (final PropertyDescriptor descriptor : descriptors) {
                if (isPropertyEditable(descriptor) && isPropertyReadable(descriptor)) {
                    editableDescriptors.add(descriptor);
                }
            }
            propertyDescriptors = new PropertyDescriptor[editableDescriptors.size()];
            editableDescriptors.toArray(propertyDescriptors);
        } else {
            propertyDescriptors = new PropertyDescriptor[0];
        }
    }

    /**
     * get the property descriptor for the specified row
     */
    public PropertyDescriptor getPropertyDescriptor(final int row) {
        return propertyDescriptors[row];
    }

    /**
     * get the property descriptor for the specified row
     */
    @Override
    public Class<?> getPropertyClass(final int row) {
        return getPropertyDescriptor(row).getPropertyType();
    }

    /**
     * determine if the property descriptor is editable
     */
    protected boolean isPropertyEditable(final PropertyDescriptor descriptor) {
        if (descriptor.getWriteMethod() != null) {
            //LOGGER.log(Level.INFO,  descriptor.getPropertyType() );
            return propertyValueEditorManager.hasEditor(descriptor.getPropertyType());
        } else {
            return false;
        }
    }

    /**
     * determine if the property descriptor is readable
     */
    protected boolean isPropertyReadable(final PropertyDescriptor descriptor) {
        if (descriptor.getReadMethod() != null) {
            return propertyValueEditorManager.hasEditor(descriptor.getPropertyType());
        } else {
            return false;
        }
    }

    /**
     * Get the name of the specified column.
     *
     * @param column the index of the column for which to get the name.
     * @return the name of the specified column
     */
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case NAME_COLUMN:
                return "Parameter";
            case VALUE_COLUMN:
                return "Value";
            default:
                return "?";
        }
    }

    /**
     * Get the data class for the specified column.
     */
    @Override
    public Class<?> getColumnClass(final int column) {
        switch (column) {
            default:
                return String.class;
        }
    }

    /**
     * Determine if the specified cell is editable.
     */
    @Override
    public boolean isCellEditable(final int row, final int column) {
        switch (column) {
            case VALUE_COLUMN:
                final PropertyDescriptor propertyDescriptor = propertyDescriptors[row];
                return isPropertyEditable(propertyDescriptor);
            default:
                return false;
        }
    }

    /**
     * Get the number of rows to display.
     *
     * @return the number of rows to display.
     */
    @Override
    public int getRowCount() {
        return propertyDescriptors.length;
    }

    /**
     * Get the number of columns to display.
     *
     * @return the number of columns to display.
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * Get the value for the specified cell.
     *
     * @param row the row of the cell to update.
     * @param column the column of the cell to update.
     * @return the value to display in the specified cell.
     */
    @Override
    public Object getValueAt(final int row, final int column) {
        switch (column) {
            case NAME_COLUMN:
                return propertyDescriptors[row].getName();
            case VALUE_COLUMN:
				try {
                final PropertyDescriptor propertyDescriptor = propertyDescriptors[row];
                return beanNode.getPropertyValue(propertyDescriptor);
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, null, exception);
                return "None";
            }
            default:
                return "?";
        }
    }

    /**
     * set the cell value
     */
    @Override
    public void setValueAt(final Object value, final int row, final int column) {
        switch (column) {
            case VALUE_COLUMN:
				try {
                final PropertyDescriptor propertyDescriptor = propertyDescriptors[row];
                beanNode.setPropertyValue(propertyDescriptor, value);
                break;
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Error Setting Value", exception);
                ApplicationSupport.displayWarning("Error Setting Value", "Property Setting Exception:", exception);
                return;
            }
            default:
                return;
        }
    }
}
