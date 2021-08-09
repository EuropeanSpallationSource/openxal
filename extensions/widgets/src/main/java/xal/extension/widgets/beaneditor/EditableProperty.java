package xal.extension.widgets.beaneditor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * base class for a editable property
 */
public abstract class EditableProperty {

    /**
     * array of classes for which the property can be edited directly
     */
    protected static final Set<Class<?>> EDITABLE_PROPERTY_TYPES = new HashSet<>();

    private static final Logger LOGGER = Logger.getLogger(EditableProperty.class.getName());

    /**
     * property name
     */
    protected final String name;

    /**
     * path to this property
     */
    protected final String path;

    /**
     * target object which is assigned the property
     */
    protected final Object target;

    /**
     * property descriptor
     */
    protected final PropertyDescriptor propertyDescriptor;

    // static initializer
    static {
        // cache the editable properties in a set for quick comparison later
        final Class<?>[] editablePropertyTypes = {Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Long.class, Long.TYPE, Boolean.class, Boolean.TYPE, String.class};
        for (final Class<?> type : editablePropertyTypes) {
            EDITABLE_PROPERTY_TYPES.add(type);
        }
    }

    /**
     * Constructor
     */
    protected EditableProperty(final String pathPrefix, final String name, final Object target, final PropertyDescriptor descriptor) {
        this.name = name;
        path = pathPrefix != null && pathPrefix.length() > 0 ? pathPrefix + "." + name : name;
        this.target = target;
        propertyDescriptor = descriptor;
    }

    /**
     * Constructor
     */
    protected EditableProperty(final String pathPrefix, final Object target, final PropertyDescriptor descriptor) {
        this(pathPrefix, descriptor.getName(), target, descriptor);
    }

    /**
     * Get an instance starting at the root object
     */
    public static EditablePropertyContainer getInstanceWithRoot(final String name, final Object root) {
        return EditablePropertyContainer.getInstanceWithRoot(name, root);
    }

    /**
     * name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Get the path to this property
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the property type
     */
    public Class<?> getPropertyType() {
        return propertyDescriptor != null ? propertyDescriptor.getPropertyType() : null;
    }

    /**
     * Get the value for this property
     */
    public Object getValue() {
        if (target != null && propertyDescriptor != null) {
            final Method getter = propertyDescriptor.getReadMethod();
            try {
                return getter.invoke(target);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                LOGGER.log(Level.WARNING, null, exception);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * set the value
     */
    public abstract void setValue(final Object value);

    /**
     * Get the units
     */
    public String getUnits() {
        return null;
    }

    /**
     * determine whether the property is a container
     */
    public abstract boolean isContainer();

    /**
     * determine whether the property is a primitive
     */
    public abstract boolean isPrimitive();


    /*
     * Get the property descriptors for the given bean info
     * @param target object for which to get the descriptors
     * @return the property descriptors for non-null beanInfo otherwise null
     */
    protected static PropertyDescriptor[] getPropertyDescriptors(final Object target) {
        if (target != null) {
            final BeanInfo beanInfo = getBeanInfo(target);
            return getPropertyDescriptorsForBeanInfo(beanInfo);
        } else {
            return new PropertyDescriptor[0];
        }
    }


    /*
     * Get the property descriptors for the given bean info
     * @param beanInfo bean info
     * @return the property descriptors for non-null beanInfo otherwise null
     */
    private static PropertyDescriptor[] getPropertyDescriptorsForBeanInfo(final BeanInfo beanInfo) {
        return beanInfo != null ? beanInfo.getPropertyDescriptors() : null;
    }

    /**
     * Convenience method to get the BeanInfo for an object's class
     */
    private static BeanInfo getBeanInfo(final Object object) {
        if (object != null) {
            return getBeanInfoForType(object.getClass());
        } else {
            return null;
        }
    }

    /**
     * Convenience method to get the BeanInfo for the given type
     */
    private static BeanInfo getBeanInfoForType(final Class<?> propertyType) {
        if (propertyType != null) {
            try {
                return Introspector.getBeanInfo(propertyType);
            } catch (IntrospectionException exception) {
                LOGGER.log(Level.WARNING, null, exception);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get a string representation of this property
     */
    @Override
    public String toString() {
        return getPath();
    }
}
