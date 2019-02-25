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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import xal.smf.attr.Attribute;

/**
 * Node class to store the data of Hdf5DataAdaptor. Inspired by the XML Element
 * class to make Hdf5DataAdaptor implementation similar to XmlDataAdaptor.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
class H5Node {

    private final String nodeName;

    private H5Node ownerDocument;

    // Using smf Attribute class for convenience
    private HashMap<String, Attribute> attributes;

    private List<H5Node> childNodes;

    public H5Node(String tagName) {
        nodeName = tagName;
        childNodes = new ArrayList<>();
        attributes = new HashMap<>();
    }

    public HashMap<String, Attribute> getAttributes() {
        return attributes;
    }

    public String getNodeName() {
        return nodeName;
    }

    public boolean hasAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    public void setAttributeNode(String attributeName, Attribute attribute) {
        attributes.put(attributeName, attribute);
    }

    public H5Node getOwnerDocument() {
        return ownerDocument;
    }

    private void setOwnerDocument(H5Node owner) {
        ownerDocument = owner;
    }

    public List<H5Node> getChildNodes() {
        return childNodes;
    }

    public void appendChild(H5Node node) {
        childNodes.add(node);
    }

    H5Node createElement(String tagName) {
        H5Node node = new H5Node(tagName);
        node.setOwnerDocument(this);
        return node;
    }
}
