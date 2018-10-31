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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import hdf.object.Datatype;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.h5.H5File;
import xal.smf.attr.Attribute;

/**
 * Class that handles the writing of FileDataAdaptors to HDF5 files.
 * 
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class Hdf5Writer {

    private H5Node document;
    private File file;

    private H5File h5File;

    private Hdf5Writer(H5Node document, File file) {
        this.document = document;
        this.file = file;
    }

    /**
     * Writes a DataAdaptor to an HDF5 file defined by an UrlSpec.
     */
    static void writeToUrlSpec(H5Node document, String urlSpec) throws MalformedURLException, IOException {
        URL url = new URL(urlSpec);
        writeToUrl(document, url);
    }

    /**
     * Writes a DataAdaptor to an HDF5 file defined by an URL.
     */
    static void writeToUrl(H5Node document, URL url) throws IOException {
        try {
            final File file = new File(url.toURI());
            writeToFile(document, file);
        } catch (URISyntaxException exception) {
            throw new RuntimeException("URI Syntax Exception", exception);
        }
    }

    /**
     * Writes a DataAdaptor to an HDF5 file specified by a File object.
     */
    static void writeToFile(H5Node document, final File file) throws IOException {
        Hdf5Writer hdf5Writer = new Hdf5Writer(document, file);
        try {
            hdf5Writer.write();
        } catch (Exception ex) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void write() throws Exception {
        // retrieve an instance of H5File.
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, "Can't find HDF5 FileFormat. Check the java library path.");
            throw new RuntimeException();
        }

        // create a new file with a given file name.
        h5File = (H5File) fileFormat.createFile(file.getAbsolutePath(), FileFormat.FILE_CREATE_DELETE);

        if (h5File == null) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, "Failed to create file:{0}", file.getName());
            throw new RuntimeException();
        }

        // open the file.
        h5File.open();

        writeNode(document, null);

        h5File.close();
    }

    /**
     * Writes a node from a DataAdaptor and recursively writes all the children
     * nodes.
     *
     * @param node Node that needs to be written.
     * @param group Group where the node belongs to.
     * @throws Exception
     */
    private void writeNode(H5Node node, Group group) throws Exception {
        // First writes all attributes as HDF5 DataSets
        for (Map.Entry<String, Attribute> entry : node.getAttributes().entrySet()) {
            createAttribute(entry.getKey(), entry.getValue(), group);
        }

        // Then recursively writes the children nodes, creating new groups.
        for (H5Node childNode : node.getChildNodes()) {
            Group childGroup = h5File.createGroup(childNode.getNodeName(), group);
            writeNode(childNode, childGroup);
        }

    }

    /**
     * Creates a new DataSet to store an attribute from a DataAdaptor. It only
     * implements the Datatypes supported by {@link xal.tools.data.DataAdaptor}.
     *
     * @param attributeName The name of the new attribute.
     * @param attribute Attribute object.
     * @param group Group where the new DataSet will be stored.
     * @throws Exception
     */
    private void createAttribute(String attributeName, Attribute attribute, Group group) throws Exception {
        Datatype dtype;

        switch (attribute.getType()) {
            case Attribute.iBoolean:
                dtype = h5File.createDatatype(Datatype.CLASS_CHAR, Byte.BYTES, Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{1}, null, null, 0, new int[]{(attribute.getBoolean() ? 1 : 0)});
                break;
            case Attribute.iInteger:
                dtype = h5File.createDatatype(Datatype.CLASS_INTEGER, Integer.BYTES, Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{1}, null, null, 0, new long[]{attribute.getInteger()});
                break;
            case Attribute.iLong:
                dtype = h5File.createDatatype(Datatype.CLASS_INTEGER, Long.BYTES, Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{1}, null, null, 0, new long[]{attribute.getLong()});
                break;
            case Attribute.iDouble:
                dtype = h5File.createDatatype(Datatype.CLASS_FLOAT, Double.BYTES, Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{1}, null, null, 0, new double[]{attribute.getDouble()});
                break;
            case Attribute.iString:
                dtype = h5File.createDatatype(Datatype.CLASS_STRING, attribute.getString().length(), Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{1}, null, null, 0, new String[]{attribute.getString()});
                break;
            case Attribute.iArrDbl:
                dtype = h5File.createDatatype(Datatype.CLASS_FLOAT, Double.BYTES, Datatype.NATIVE, Datatype.NATIVE);
                h5File.createScalarDS(attributeName, group, dtype, new long[]{attribute.getArrDbl().length}, null, null, 0, attribute.getArrDbl());
                break;
        }
    }
}
