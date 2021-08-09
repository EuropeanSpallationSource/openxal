//
//  ViewInspector.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

/**
 * Inspector for setting view properties
 */
public class ViewInspector extends Box {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * table of property settings
     */
    protected final JTable parameterTable;

    /**
     * field for editing the node's tag
     */
    protected final JTextField tagField;

    /**
     * field for editing the node's custom class
     */
    protected final JTextField customClassField;

    /**
     * check box for enabling/disabling custom classes
     */
    protected final JCheckBox customClassEnable;

    /**
     * property editor manager
     */
    protected final PropertyValueEditorManager propertyEditorManager;

    /**
     * indicates whether this palette has ever been positioned
     */
    protected boolean hasBeenPositioned;

    /**
     * view node to inspect
     */
    protected BeanNode<?> node;

    /**
     * Constructor
     */
    public ViewInspector() {
        super(BoxLayout.Y_AXIS);

        setSize(400, 600);

        parameterTable = new JTable();
        parameterTable.setRowHeight((int) (1.2 * parameterTable.getRowHeight()));

        propertyEditorManager = PropertyValueEditorManager.getDefaultManager();
        hasBeenPositioned = false;

        tagField = new JTextField();
        customClassField = new JTextField();
        customClassEnable = new JCheckBox("");

        makeContent();
    }

    /**
     * Inspect the specified view node
     */
    public void inspect(final BricksContext context, final BeanNode<?> node) {
        setViewNode(context, node);
    }

    /**
     * Set the view node
     */
    public void setViewNode(final BricksContext context, final BeanNode<?> node) {
        this.node = node;

        final ViewInspectorTableModel tableModel = new ViewInspectorTableModel(node, propertyEditorManager);
        parameterTable.setModel(tableModel);
        final PropertyValueCellEditor editor = new PropertyValueCellEditor(context, parameterTable, propertyEditorManager, tableModel);
        parameterTable.getColumnModel().getColumn(ViewInspectorTableModel.VALUE_COLUMN).setCellEditor(editor);
        parameterTable.getColumnModel().getColumn(ViewInspectorTableModel.VALUE_COLUMN).setCellRenderer(editor);

        if (node != null) {
            tagField.setText(node.getTag());
            tagField.setEnabled(true);

            refreshCustomClassView();
        } else {
            tagField.setText("");
            tagField.setEnabled(false);
            customClassField.setText("");
            customClassField.setEnabled(false);
            customClassEnable.setSelected(false);
        }
    }

    /**
     * refresh the custom class field to reflect the custom class field if any
     */
    private void refreshCustomClassView() {
        final boolean hasCustomBeanClass = node.hasCustomBeanClass();
        customClassField.setText(hasCustomBeanClass ? node.getCustomBeanClassName() : node.getClassName());
        customClassField.setForeground(hasCustomBeanClass ? Color.BLACK : Color.GRAY);
        customClassField.setEnabled(hasCustomBeanClass);
        customClassEnable.setSelected(hasCustomBeanClass);
    }

    /**
     * Get the view node
     */
    public BeanNode<?> getViewNode() {
        return node;
    }

    /**
     * Make the content for the window.
     */
    protected void makeContent() {
        this.add(makeSettingsView());
        this.add(makeBeanPropertiesView());
    }

    /**
     * make the bean properties view
     */
    protected Component makeBeanPropertiesView() {
        final JComponent view = new JScrollPane(parameterTable);

        view.setBorder(BorderFactory.createTitledBorder("Bean Parameters"));

        return view;
    }

    /**
     * make the settings view
     */
    protected Component makeSettingsView() {
        final Box view = new Box(BoxLayout.Y_AXIS);

        view.add(makeTagView());
        view.add(makeCustomClassView());

        return view;
    }

    /**
     * make the tag view
     */
    protected Component makeTagView() {
        final Box view = new Box(BoxLayout.X_AXIS);

        view.setBorder(BorderFactory.createTitledBorder("Tag"));

        tagField.setMaximumSize(new Dimension(10000, tagField.getPreferredSize().height));

        tagField.addActionListener(event -> {
            if (node != null) {
                node.setTag(tagField.getText());
            }
        });

        view.add(tagField);

        return view;
    }

    /**
     * make the custom class view
     */
    protected Component makeCustomClassView() {
        final Box view = new Box(BoxLayout.X_AXIS);
        view.setBorder(BorderFactory.createTitledBorder("Custom Class"));

        customClassField.setMaximumSize(new Dimension(10000, tagField.getPreferredSize().height));

        customClassField.addActionListener(event -> {
            if (node != null) {
                node.setCustomBeanClassName(customClassField.getText());
            }
        });

        customClassEnable.addActionListener(event -> {
            if (node != null) {
                final boolean shouldEnable = customClassEnable.isSelected();
                node.setCustomBeanClassName(shouldEnable ? node.getClassName() : null);
                refreshCustomClassView();
            }
        });

        view.add(customClassEnable);
        view.add(customClassField);

        return view;
    }
}
