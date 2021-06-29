//
//  PropertyValueEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.awt.Component;

import xal.tools.data.*;


/** property value editor */
public abstract class PropertyValueEditor<ValueType> {
	protected final Component editorComponent;
	protected final Component renderingComponent;
	
	protected PropertyValueCellEditor currentCellEditor;
	
	
	/** Constructor */
	public PropertyValueEditor() {
		editorComponent = getEditorComponentInstance();
		renderingComponent = getRenderingComponentInstance();
	}
	
	
	/** write to a data adaptor */
	public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
		adaptor.setValue( "name", name );
	}
	
	
	/** write to a data adaptor */
	public abstract ValueType readValue( final DataAdaptor adaptor );
	
	
	/** Determine if the component supports editing */
	public boolean isEditable() {
		return editorComponent != null;
	}
	
	
	/** get the component */
	public Component getEditorComponent() {
		return editorComponent;
	}
	
	
	/** get the component */
	public Component getRenderingComponent() {
		return renderingComponent;
	}
	
	/** instantiate a component */
	public Component getRenderingComponentInstance() {
		return getEditorComponentInstance();
	}
	
	
	/** instantiate a component */
	public abstract Component getEditorComponentInstance();	
	
	
	/** get the cell editor value */
	public abstract ValueType getEditorValue( final BricksContext context );
	
	
	/** set the editor value */
	public abstract void setEditorValue( final Object value );
	
	
	/** set the rendering value */
	public abstract void setRenderingValue( final Object value );
	
	
	/** set the current cell editor */
	public void setCurrentCellEditor( final PropertyValueCellEditor cellEditor ) {
		currentCellEditor = cellEditor;
	}
}
