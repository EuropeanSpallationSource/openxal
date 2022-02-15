//
//  BeanProxy.java
//  xal
//
//  Created by Thomas Pelaia on 7/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.lang.reflect.*;
import javax.swing.*;

import xal.tools.data.*;

/**
 * proxy for generating a Java Bean object
 */
public abstract class BeanProxy<T> implements DataListener {

    /**
     * prototype class
     */
    protected final Class<T> prototypeClass;

    /**
     * Constructor
     */
    protected BeanProxy(final Class<T> prototypeClass) {
        this.prototypeClass = prototypeClass;
    }

    /**
     * Create an instance of the specified view
     */
    public T getBeanInstance(final Class<T> theClass) {
        try {
            final Constructor<T> constructor = theClass.getConstructor(getConstructorParameterTypes());
            final Object[] parameters = getConstructorParameters();
            return getBeanInstance(theClass, constructor, parameters);
        } catch (NoSuchMethodException | SecurityException exception) {
            throw new RuntimeException("Can't instantiate class:  " + theClass.toString(), exception);
        }
    }

    /**
     * Create an instance of the specified view
     */
    public T getBeanInstance(final Class<T> theClass, final Constructor<T> constructor, Object... parameters) {
        try {
            constructor.setAccessible(true);
            final T object = constructor.newInstance(parameters);
            setup(object);
            return object;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException exception) {
            throw new RuntimeException("Can't instantiate class:  " + theClass.toString(), exception);
        }
    }

    /**
     * setup the instance after construction
     */
    public void setup(final T object) {
    }

    /**
     * setup the instance after construction with prototype data
     */
    public void setupPrototype(final T object) {
    }

    /**
     * Get the class of the view
     */
    public final Class<T> getPrototypeClass() {
        return prototypeClass;
    }

    /**
     * Get the prototype view
     *
     * @return the prototype view
     */
    public final T getPrototype() {
        final T object = getBeanInstance(prototypeClass);
        setupPrototype(object);
        return object;
    }

    /**
     * Get the array of constructor arguments
     *
     * @return the constructor arguments
     */
    // generics don't mix with arrays
    @SuppressWarnings("rawtypes")
    public Class[] getConstructorParameterTypes() {
        return new Class[0];
    }

    /**
     * Get the array of constructor arguments
     *
     * @return the constructor arguments
     */
    public Object[] getConstructorParameters() {
        return new Object[0];
    }

    /**
     * Get an icon representation for the view
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * get the name of the prototype
     */
    public String getType() {
        return prototypeClass.getName();
    }

    /**
     * get the name of the prototype
     */
    public String getName() {
        return prototypeClass.getName();
    }

    /**
     * get the short name of the prototype
     */
    public String getShortName() {
        final String[] words = getName().split("\\W");
        return words[words.length - 1];
    }

    /**
     * Get a textual representation of the view
     */
    public String getText() {
        return prototypeClass.getName();
    }

    /**
     * get the Jython reference snippet
     */
    public String getJythonReferenceSnippet(final BeanNode<?> node) {
        // lower the case of the tag and replace spaces with underscores
        final String symbol = node.getTag().toLowerCase().replaceAll(" ", "_");

        final StringBuilder buffer = new StringBuilder();
        buffer.append(symbol);
        buffer.append(" = ");
        buffer.append("window_reference.");
        buffer.append(getReferenceSnippetFetchMethodName());
        buffer.append("(");
        buffer.append(getReferenceSnippetFetchMethodArgumentsString(node));
        buffer.append(")");

        return buffer.toString();
    }

    /**
     * get the java reference snippet
     */
    public String getJavaReferenceSnippet(final BeanNode<?> node) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("final ");
        buffer.append(node.getShortClassName());
        buffer.append(" ");
        buffer.append(generateJavaReferenceSymbol(node));
        buffer.append(" = (");
        buffer.append(node.getShortClassName());
        buffer.append(")windowReference.");
        buffer.append(getReferenceSnippetFetchMethodName());
        buffer.append("(");
        buffer.append(getReferenceSnippetFetchMethodArgumentsString(node));
        buffer.append(");");

        return buffer.toString();
    }

    /**
     * Generate the Java symbol for the specified node by lowering the case of
     * the first character, stripping white space and capitalizing the first
     * word character
     */
    private static String generateJavaReferenceSymbol(final BeanNode<?> node) {
        final String tag = node.getTag();
        final int tagLength = tag.length();
        final StringBuilder buffer = new StringBuilder();

        // lower the case of the first character of the symbol
        buffer.append(Character.toLowerCase(tag.charAt(0)));

        // indicates whether the next character begins a word
        boolean nextCharBeginsWord = false;
        for (int index = 1; index < tagLength; index++) {
            final char theCharacter = tag.charAt(index);
            // whitespace separates words
            if (Character.isWhitespace(theCharacter)) {
                // space indicates that next character begins a new word
                nextCharBeginsWord = true;
            } else {
                if (nextCharBeginsWord) {
                    // capitalize the first character of the word
                    buffer.append(Character.toUpperCase(theCharacter));
                } else {
                    // simply append the character if it is part of word and not the first character
                    buffer.append(theCharacter);
                }
                nextCharBeginsWord = false;
            }
        }

        return buffer.toString();
    }

    /**
     * get the java reference snippet
     */
    public String getXALReferenceSnippet(final BeanNode<T> node) {
        return getJavaReferenceSnippet(node);
    }

    /**
     * Get the java declaration snippet
     *
     * @return the java declaration snippet
     */
    public String getJavaDeclarationSnippet(final BeanNode<T> node) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(node.getShortClassName());
        buffer.append(" ");
        buffer.append(generateJavaReferenceSymbol(node));
        buffer.append(";");

        return buffer.toString();
    }

    /**
     * Get the reference snippet method name
     *
     * @return the method name
     */
    protected String getReferenceSnippetFetchMethodName() {
        return "getView";
    }

    /**
     * Get the reference snippet method arguments
     *
     * @return the method arguments
     */
    protected String getReferenceSnippetFetchMethodArgumentsString(final BeanNode<?> node) {
        return " \"" + node.getTag() + "\" ";
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
    }

    /**
     * Write data to the data adaptor for storage.
     *
     * @param adaptor The adaptor to which the receiver's data is written
     */
    @Override
    public void write(final DataAdaptor adaptor) {
        adaptor.setValue("type", getType());
    }

    /**
     * get string representation
     */
    @Override
    public String toString() {
        return getName();
    }
}
