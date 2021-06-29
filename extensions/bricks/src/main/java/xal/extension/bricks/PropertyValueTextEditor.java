//
//  PropertyValueTextFieldEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.*;
import java.awt.Component;

/**
 * property value editor
 */
public abstract class PropertyValueTextEditor<T> extends PropertyValueEditor<T> {

    /**
     * instantiate a component
     */
    @Override
    public Component getRenderingComponentInstance() {
        return new JLabel();
    }

    /**
     * instantiate a component
     */
    @Override
    public Component getEditorComponentInstance() {
        return new JTextField();
    }

    /**
     * get the cell editor value
     */
    @Override
    public abstract T getEditorValue(final BricksContext context);

    /**
     * set the editor value
     */
    @Override
    public void setEditorValue(final Object value) {
        ((JTextField) getEditorComponent()).setText(value != null ? value.toString() : "");
    }

    /**
     * set the rendering value
     */
    @Override
    public void setRenderingValue(final Object value) {
        ((JLabel) getRenderingComponent()).setText(value != null ? value.toString() : "");
    }
}
