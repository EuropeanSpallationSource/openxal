//
//  PropertyValueNumberEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.*;

/**
 * property value editor
 */
public abstract class PropertyValueNumberEditor<T> extends PropertyValueTextEditor<T> {

    /**
     * Constructor
     */
    protected PropertyValueNumberEditor() {
        ((JTextField) editorComponent).setHorizontalAlignment(SwingConstants.RIGHT);
        ((JLabel) renderingComponent).setHorizontalAlignment(SwingConstants.RIGHT);
    }
}
