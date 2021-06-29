//
//  NumericCellRenderer.java
//  xal
//
//  Created by Thomas Pelaia on 6/17/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.apputils;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.*;
import java.awt.Component;
import java.awt.Rectangle;

/**
 * Defines a cell renderer capable of displaying numeric data.
 */
public class NumericCellRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    /**
     * keeps track of whether this cell should be drawn opaque or not
     */
    private boolean isOpaque;

    /**
     * Constructor
     */
    public NumericCellRenderer(final JTable table) {
        super("", SwingConstants.RIGHT);

        isOpaque = false;
        setBackground(table.getSelectionBackground());
    }

    /**
     * Get the table component.
     */
    @Override
    public final Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        setText((value != null) ? value.toString() : "");

        isOpaque = isSelected;

        return this;
    }

    /**
     * override for performance reasons
     */
    @Override
    public final boolean isOpaque() {
        return isOpaque;
    }

    /**
     * do nothing
     */
    @Override
    public final void validate() {
    }

    /**
     * do nothing
     */
    @Override
    public final void revalidate() {
    }

    /**
     * do nothing
     */
    @Override
    public final void repaint(final Rectangle rectangle) {
    }

    /**
     * do nothing
     */
    @Override
    public final void repaint(final long tm, final int x, final int y, final int width, final int height) {
    }

    /**
     * do nothing
     */
    @Override
    protected final void firePropertyChange(final String name, final Object oldValue, final Object newValue) {
    }

    /**
     * do nothing
     */
    @Override
    public final void firePropertyChange(final String name, final boolean oldValue, final boolean newValue) {
    }
}
