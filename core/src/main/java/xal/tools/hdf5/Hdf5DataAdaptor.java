/*
 * Copyright (c) 2018, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.tools.hdf5;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import xal.smf.attr.Attribute;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.data.FileDataAdaptor;

/**
 * This class creates an HDF5 structure in memory before writing to a file,
 * since the HDF5 library only supports directly writing to a file.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class Hdf5DataAdaptor implements FileDataAdaptor {

    private H5Node document;  // root document
    private H5Node mainNode;      // could be a document or an element
    private List<H5Node> childNodes;    // child nodes as a List

    private Hdf5DataAdaptor(String name) {
        mainNode = new H5Node(name);
        document = mainNode;
        childNodes = new ArrayList<>();
    }

    private Hdf5DataAdaptor(H5Node newNode) {
        mainNode = newNode;
        document = mainNode.getOwnerDocument();
        childNodes = mainNode.getChildNodes();
    }

    /**
     * get the tag name for the specified XML node
     */
    static private String nameForNode(H5Node node) {
        return node.getNodeName();
    }

    /**
     * get the tag name for the main node
     *
     * @return
     */
    @Override
    public String name() {
        return nameForNode(mainNode);
    }

    /**
     * check whether the main node has the specified attribute
     *
     * @param attribute Name of the attribute to find.
     * @return Boolean specifying if the attribute exists.
     */
    @Override
    public boolean hasAttribute(String attribute) {
        return mainNode.hasAttribute(attribute);
    }

    @Override
    public String stringValue(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return attribute.getString();
        } else {
            return null;
        }
    }

    @Override
    public double doubleValue(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return attribute.getDouble();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public long longValue(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return attribute.getLong();
        } else {
            return 0;
        }
    }

    @Override
    public int intValue(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return attribute.getInteger();
        } else {
            return 0;
        }
    }

    @Override
    public boolean booleanValue(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return (boolean) attribute.getBoolean();
        } else {
            return false;
        }
    }

    @Override
    public double[] doubleArray(String attributeName) {
        Attribute attribute = mainNode.getAttributes().get(attributeName);
        if (attribute != null) {
            return attribute.getArrDbl();
        } else {
            return null;
        }
    }

    @Override
    public void setValue(String attributeName, String value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, double value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, long value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, int value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, boolean value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, Object value) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(value.toString());
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public void setValue(String attributeName, double[] array) {
        Attribute attribute = new Attribute(attributeName);
        attribute.set(array);
        mainNode.setAttributeNode(attributeName, attribute);
    }

    @Override
    public String[] attributes() {
        return mainNode.getAttributes().keySet().toArray(new String[mainNode.getAttributes().size()]);
    }

    /**
     * create a new adaptor for the specified node
     */
    static private Hdf5DataAdaptor newAdaptor(H5Node node) {
        Hdf5DataAdaptor adaptor = new Hdf5DataAdaptor(node);
        return adaptor;
    }

    @Override
    public List<DataAdaptor> childAdaptors() {
        List<DataAdaptor> childAdaptors = new ArrayList<>();

        for (H5Node node : childNodes) {
            DataAdaptor adaptor = newAdaptor(node);
            childAdaptors.add(adaptor);
        }

        return childAdaptors;
    }

    /**
     * Create a list of child adaptors (one adaptor for each non-null child node
     * whose tag name is equal to the specified label).
     *
     * @param label the label for which to match the node's tag
     * @return a list of child adaptors
     */
    @Override
    public List<DataAdaptor> childAdaptors(final String label) {
        List<DataAdaptor> childAdaptors = new ArrayList<>();

        for (H5Node node : childNodes) {
            if (nameForNode(node).equals(label)) {
                childAdaptors.add(newAdaptor(node));
            }
        }

        return childAdaptors;
    }

    @Override
    public DataAdaptor childAdaptor(String label) {
        final List<DataAdaptor> adaptors = childAdaptors(label);
        return adaptors.isEmpty() ? null : adaptors.get(0);
    }

    @Override
    public DataAdaptor createChild(String tagName) {
        H5Node node = document.createElement(tagName);
        Hdf5DataAdaptor childAdaptor = newAdaptor(node);

        mainNode.appendChild(node);
        childNodes.add(node);

        return childAdaptor;
    }

    @Override
    public void writeNode(DataListener listener) {
        String tagName = listener.dataLabel();

        DataAdaptor adaptor = createChild(tagName);
        listener.write(adaptor);
    }

    @Override
    public void writeNodes(Collection<? extends DataListener> nodes) {
        for (DataListener node : nodes) {
            writeNode(node);
        }
    }

    @Override
    public void writeTo(File file) throws IOException {
        Hdf5Writer.writeToFile(document, file);
    }

    @Override
    public void writeToUrl(URL url) throws IOException {
        Hdf5Writer.writeToUrl(document, url);
    }

    @Override
    public void writeToUrlSpec(String urlSpec) throws IOException {
        Hdf5Writer.writeToUrlSpec(document, urlSpec);
    }

    /**
     * Generate an Hdf5DataAdaptor from a urlPath.
     */
    static public Hdf5DataAdaptor adaptorForUrl(final String urlPath) {
        Hdf5DataAdaptor adaptor;

        adaptor = Hdf5DataAdaptor.newEmptyDocumentAdaptor();
        
        Hdf5Reader.readFromUrlSpec(adaptor, urlPath);

        return adaptor;
    }

    /**
     * Generate an Hdf5DataAdaptor from a URL.
     */
    static public Hdf5DataAdaptor adaptorForUrl(final URL url) {
        return Hdf5DataAdaptor.adaptorForUrl(url.toString());
    }

    /**
     * Generate an Hdf5DataAdaptor from a File.
     */
    static public Hdf5DataAdaptor adaptorForFile(final File file) throws MalformedURLException {
        return Hdf5DataAdaptor.adaptorForUrl(file.toURI().toURL());
    }

    /**
     * Create an empty HDF5 document
     *
     * @return DataAdaptor containing the root document node.
     */
    static public Hdf5DataAdaptor newEmptyDocumentAdaptor() {
        Hdf5DataAdaptor adaptor;

        adaptor = new Hdf5DataAdaptor("root");

        return adaptor;
    }
}
