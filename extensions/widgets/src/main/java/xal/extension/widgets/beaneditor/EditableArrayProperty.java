package xal.extension.widgets.beaneditor;

import java.beans.PropertyDescriptor;
import java.util.Set;

/** container for an editable property that is an array */
class EditableArrayProperty extends EditablePropertyContainer {
	/** Constructor */
	protected EditableArrayProperty( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Set<Object> ancestors ) {
		super( pathPrefix, target, descriptor, ancestors );
	}

	// TODO: complete implementation of array property
}