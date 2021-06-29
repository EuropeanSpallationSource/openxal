//
//  BeanNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.data.*;

/**
 * brick which represents a Java Bean
 */
public abstract class BeanNode<T> extends Brick implements DataListener {

    private static final Logger LOGGER = Logger.getLogger(BeanNode.class.getName());

    /**
     * data label from bean properties
     */
    protected static final String BEAN_DATA_LABEL = "BeanProperty";

    /**
     * bean object
     */
    protected final T beanObject;

    /**
     * the bean proxy
     */
    protected final BeanProxy<T> beanProxy;

    /**
     * bean settings
     */
    protected final Map<String, Object> beanSettings;

    /**
     * tag for identifying this node
     */
    protected String tag;

    /**
     * custom bean class name
     */
    protected String customBeanClassName;

    /**
     * Primary Constructor
     */
    public BeanNode(final BeanProxy<T> beanProxy, final Map<String, Object> beanSettings, final String tag) {
        this.beanProxy = beanProxy;
        beanObject = getPrototypeBean(beanProxy);
        this.beanSettings = new HashMap<>();

        this.tag = tag;
        customBeanClassName = null;

        if (beanSettings != null) {
            this.beanSettings.putAll(beanSettings);
            applyBeanSettings();
        }
    }

    /**
     * Constructor
     */
    public BeanNode(final BeanNode<T> node) {
        this(node.beanProxy, node.beanSettings, node.getTag());

        setCustomBeanClassName(node.getCustomBeanClassName());
    }

    /**
     * Constructor
     */
    public BeanNode(final BeanProxy<T> beanProxy) {
        this(beanProxy, null, beanProxy.getName());
    }

    /**
     * Get the view proxy
     *
     * @return the view proxy
     */
    public BeanProxy<T> getBeanProxy() {
        return beanProxy;
    }

    /**
     * get the bean instance
     */
    protected T getPrototypeBean(final BeanProxy<T> beanProxy) {
        return beanProxy.getPrototype();
    }

    /**
     * Get the bean object
     */
    public T getBeanObject() {
        return beanObject;
    }

    /**
     * Get the bean info of the view. Get the view's bean info
     */
    public BeanInfo getBeanObjectBeanInfo() {
        try {
            return Introspector.getBeanInfo(beanObject.getClass());
        } catch (IntrospectionException exception) {
            return null;
        }
    }

    /**
     * Get this node's tag
     *
     * @return this node's tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set this node's tag
     *
     * @param tag the new tag
     */
    public void setTag(final String tag) {
        this.tag = tag;
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * Determine whether this node has a custom bean class
     *
     * @return true if this node has a custom bean class and false if not
     */
    public boolean hasCustomBeanClass() {
        return customBeanClassName != null;
    }

    /**
     * Get this node's custom bean class name
     *
     * @return this node's custom bean class name
     */
    public String getCustomBeanClassName() {
        return customBeanClassName;
    }

    /**
     * Set this node's custom bean class name
     *
     * @param name the new custom bean class name
     */
    public void setCustomBeanClassName(final String name) {
        customBeanClassName = name;
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * get the fully qualified class name
     *
     * @return the custom class name if it exists or the prototype class name if
     * there is no custom class
     */
    public String getClassName() {
        return hasCustomBeanClass() ? getCustomBeanClassName() : beanProxy.getPrototypeClass().getName();
    }

    /**
     * get the short version of the class name
     *
     * @return the short version of the class name
     */
    public String getShortClassName() {
        final String[] words = getClassName().split("\\W");
        return words[words.length - 1];
    }

    /**
     * get the Jython reference snippet
     */
    public String getJythonReferenceSnippet() {
        return beanProxy.getJythonReferenceSnippet(this);
    }

    /**
     * get the java reference snippet
     */
    public String getJavaReferenceSnippet() {
        return beanProxy.getJavaReferenceSnippet(this);
    }

    /**
     * get the java reference snippet
     */
    public String getXALReferenceSnippet() {
        return beanProxy.getXALReferenceSnippet(this);
    }

    /**
     * Get the java declaration snippet
     *
     * @return the java declaration snippet
     */
    public String getJavaDeclarationSnippet() {
        return beanProxy.getJavaDeclarationSnippet(this);
    }

    /**
     * refresh display
     */
    public void refreshDisplay() {
    }

    /**
     * apply the bean settings
     */
    protected void applyBeanSettings() {
        final Map<String, PropertyDescriptor> descriptorTable = getProperyDescriptorTable();

        final Iterator<String> nameIter = beanSettings.keySet().iterator();
        while (nameIter.hasNext()) {
            final String name = nameIter.next();
            final PropertyDescriptor descriptor = descriptorTable.get(name);
            final Object value = beanSettings.get(name);
            try {
                setPropertyValue(descriptor, value);
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }
    }

    /**
     * Get the property descriptor table keyed by property name
     */
    protected Map<String, PropertyDescriptor> getProperyDescriptorTable() {
        final BeanInfo beanInfo = getBeanObjectBeanInfo();
        final PropertyDescriptor[] descriptors = beanInfo != null ? beanInfo.getPropertyDescriptors() : new PropertyDescriptor[0];
        final Map<String, PropertyDescriptor> descriptorTable = new HashMap<>(beanSettings.size());
        for (final PropertyDescriptor descriptor : descriptors) {
            descriptorTable.put(descriptor.getName(), descriptor);
        }
        return descriptorTable;
    }

    /**
     * get the property value
     */
    public Object getPropertyValue(final PropertyDescriptor propertyDescriptor) throws Exception {
        final Method method = propertyDescriptor.getReadMethod();
        return method != null ? method.invoke(beanObject) : null;
    }

    /**
     * update the property with the specified value
     */
    public void setPropertyValue(final PropertyDescriptor propertyDescriptor, final Object value) {
        final Method method = propertyDescriptor.getWriteMethod();

        try {
            method.invoke(beanObject, value);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() != null) {
                throw new RuntimeException(exception.getCause());
            } else {
                throw new RuntimeException(exception);
            }
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        final String name = propertyDescriptor.getName();
        beanSettings.put(name, value);
        eventProxy.propertyChanged(this, propertyDescriptor, value);
        refreshDisplay();
    }

    /**
     * Get the containing node
     *
     * @return the parent view node
     */
    public ViewNodeContainer getViewNodeContainer() {
        final Object parent = getParent();
        return parent instanceof ViewNodeContainer ? (ViewNodeContainer) parent : null;
    }

    /**
     * Display the bean's window
     */
    public void display() {
    }

    /**
     * Provides the name used to identify the class in an external data source.
     *
     * @return a tag that identifies the receiver's type
     */
    @Override
    public abstract String dataLabel();

    /**
     * Update the data based on the information provided by the data provider.
     *
     * @param adaptor The adaptor from which to update the data
     */
    @Override
    public void update(final DataAdaptor adaptor) {
        if (adaptor.hasAttribute("customBeanClass")) {
            setCustomBeanClassName(adaptor.stringValue("customBeanClass"));
        }

        final PropertyValueEditorManager editorManager = PropertyValueEditorManager.getDefaultManager();
        final Map<String, PropertyDescriptor> descriptorTable = getProperyDescriptorTable();
        final List<DataAdaptor> beanAdaptors = adaptor.childAdaptors(BEAN_DATA_LABEL);
        for (final DataAdaptor beanAdaptor : beanAdaptors) {
            try {
                beanAdaptor.setValue("contextURL", adaptor.stringValue("contextURL"));
                final String name = beanAdaptor.stringValue("name");
                final PropertyDescriptor propertyDescriptor = descriptorTable.get(name);
                final Class<?> propertyType = propertyDescriptor.getPropertyType();
                final PropertyValueEditor<?> propertyEditor = editorManager.getEditor(propertyType);
                final Object value = propertyEditor.readValue(beanAdaptor);
                setPropertyValue(propertyDescriptor, value);
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }
    }

    /**
     * Write data to the data adaptor for storage.
     *
     * @param adaptor The adaptor to which the receiver's data is written
     */
    @Override
    public void write(final DataAdaptor adaptor) {
        adaptor.setValue("tag", tag);

        if (customBeanClassName != null) {
            adaptor.setValue("customBeanClass", customBeanClassName);
        }

        adaptor.writeNode(beanProxy);

        final Set<Map.Entry<String, Object>> settings = beanSettings.entrySet();
        for (final Map.Entry<String, Object> setting : settings) {
            final String name = setting.getKey();
            final Object value = setting.getValue();
            adaptor.writeNode(getPropertyArchiver(name, value));
        }
    }

    /**
     * get the archiver of bean properties
     */
    public static DataListener getPropertyArchiver(final String name, final Object value) {
        return new DataListener() {
            /**
             * Provides the name used to identify the class in an external data
             * source.
             *
             * @return a tag that identifies the receiver's type
             */
            @Override
            public String dataLabel() {
                return BEAN_DATA_LABEL;
            }

            /**
             * Update the data based on the information provided by the data
             * provider.
             *
             * @param adaptor The adaptor from which to update the data
             */
            @Override
            public void update(final DataAdaptor adaptor) {
            }

            /**
             * Write data to the data adaptor for storage.
             *
             * @param adaptor The adaptor to which the receiver's data is
             * written
             */
            @Override
            public void write(final DataAdaptor adaptor) {
                final PropertyValueEditor<?> editor = PropertyValueEditorManager.getDefaultManager().getEditor(value.getClass());
                editor.writeValue(name, value, adaptor);
            }
        };
    }

    /**
     * get a label
     */
    @Override
    public String toString() {
        return tag;
    }
}
