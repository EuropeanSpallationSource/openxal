package xal.extension.widgets.beaneditor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** base class for a editable property */
public abstract class EditableProperty {
    /** array of classes for which the property can be edited directly */
    final static protected Set<Class<?>> EDITABLE_PROPERTY_TYPES = new HashSet<>();

	/** property name */
	final protected String NAME;

	/** path to this property */
	final protected String PATH;

	/** target object which is assigned the property */
	final protected Object TARGET;

	/** property descriptor */
	final protected PropertyDescriptor PROPERTY_DESCRIPTOR;


	// static initializer
	static {
		// cache the editable properties in a set for quick comparison later
		final Class<?>[] editablePropertyTypes = { Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Long.class, Long.TYPE, Boolean.class, Boolean.TYPE, String.class };
		for ( final Class<?> type : editablePropertyTypes ) {
			EDITABLE_PROPERTY_TYPES.add( type );
		}
	}


	/** Constructor */
	protected EditableProperty( final String pathPrefix, final String name, final Object target, final PropertyDescriptor descriptor ) {
		NAME = name;
		PATH = pathPrefix != null && pathPrefix.length() > 0 ? pathPrefix + "." + name : name;
		TARGET = target;
		PROPERTY_DESCRIPTOR = descriptor;
	}


	/** Constructor */
	protected EditableProperty( final String pathPrefix, final Object target, final PropertyDescriptor descriptor ) {
		this( pathPrefix, descriptor.getName(), target, descriptor );
	}


	/** Get an instance starting at the root object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object root ) {
		return EditablePropertyContainer.getInstanceWithRoot( name, root );
	}


	/** name of the property */
	public String getName() {
		return NAME;
	}


	/** Get the path to this property */
	public String getPath() {
		return PATH;
	}


	/** Get the property type */
	public Class<?> getPropertyType() {
		return PROPERTY_DESCRIPTOR != null ? PROPERTY_DESCRIPTOR.getPropertyType() : null;
	}


	/** Get the value for this property */
	public Object getValue() {
		if ( TARGET != null && PROPERTY_DESCRIPTOR != null ) {
			final Method getter = PROPERTY_DESCRIPTOR.getReadMethod();
			try {
				return getter.invoke( TARGET );
			}
			catch( Exception exception ) {
				System.err.println( exception );
				return null;
			}
		}
		else {
			return null;
		}
	}


	/** set the value */
	abstract public void setValue( final Object value );


	/** Get the units */
	public String getUnits() {
		return null;
	}


	/** determine whether the property is a container */
	abstract public boolean isContainer();

	/** determine whether the property is a primitive */
	abstract public boolean isPrimitive();


    /*
     * Get the property descriptors for the given bean info
     * @param target object for which to get the descriptors
	 * @return the property descriptors for non-null beanInfo otherwise null
     */
    static protected PropertyDescriptor[] getPropertyDescriptors( final Object target ) {
		if ( target != null ) {
			final BeanInfo beanInfo = getBeanInfo( target );
			return getPropertyDescriptorsForBeanInfo( beanInfo );
		}
		else {
			return null;
		}
	}


    /*
     * Get the property descriptors for the given bean info
     * @param beanInfo bean info
	 * @return the property descriptors for non-null beanInfo otherwise null
     */
    static private PropertyDescriptor[] getPropertyDescriptorsForBeanInfo( final BeanInfo beanInfo ) {
		return beanInfo != null ? beanInfo.getPropertyDescriptors() : null;
	}


    /** Convenience method to get the BeanInfo for an object's class */
	static private BeanInfo getBeanInfo( final Object object ) {
		if ( object != null ) {
			return getBeanInfoForType( object.getClass() );
		}
		else {
			return null;
		}
	}


    /** Convenience method to get the BeanInfo for the given type */
	static private BeanInfo getBeanInfoForType( final Class<?> propertyType ) {
		if ( propertyType != null ) {
			try {
				return Introspector.getBeanInfo( propertyType );
			}
			catch( IntrospectionException exception ) {
				return null;
			}
		}
		else {
			return null;
		}
	}


	/** Get a string represenation of this property */
	public String toString() {
		return getPath();
	}
}