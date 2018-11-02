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

import hdf.object.Dataset;
import java.util.logging.Level;
import java.util.logging.Logger;
import hdf.object.Datatype;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5File;
import java.net.URL;
import javax.swing.tree.DefaultMutableTreeNode;
import xal.tools.data.DataAdaptor;

/**
 * Class that handles the reading of HDF5 files to FileDataAdaptors.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class Hdf5Reader {

    private Hdf5DataAdaptor adaptor;
    private String fileUrl;

    private H5File h5File;

    private Hdf5Reader(Hdf5DataAdaptor adaptor, String fileUrl) {
        this.adaptor = adaptor;
        this.fileUrl = fileUrl;
    }

    /**
     * Read an HDF5 defined by an UrlSpec to a DataAdaptor.
     */
    protected static void readFromUrlSpec(Hdf5DataAdaptor adaptor, String urlSpec) {
        Hdf5Reader hdf5Reader = new Hdf5Reader(adaptor, urlSpec);
        try {
            hdf5Reader.read();
        } catch (Exception ex) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void read() throws Exception {
        // retrieve an instance of H5File
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, "Can't find HDF5 FileFormat. Check the java library path.");
            throw new RuntimeException();
        }

        // Open the HDF5 file with a given file name.
        h5File = new H5File(new URL(fileUrl).getFile(), FileFormat.READ);

        if (h5File == null) {
            Logger.getLogger(Hdf5Writer.class.getName()).log(Level.SEVERE, "Failed to create file:{0}", fileUrl);
            throw new RuntimeException();
        }

        // open the file and retrieve the root group
        h5File.open();

        Group root = (Group) h5File.getRootObject();

        readNode(adaptor, root);

        h5File.close();
    }

    /**
     * Read a node and store the data into a DataAdaptor and recursively read
     * all the children nodes.
     *
     * @throws Exception
     */
    private void readNode(DataAdaptor adaptor, Group group) throws Exception {
        for (HObject object : group.getMemberList()) {
            if (object instanceof Dataset) {
                readAttribute(adaptor, (Dataset) object);
            } else if (object instanceof Group) {
                String tagName = ((String[]) ((hdf.object.Attribute) ((Group) object).getMetadata().get(0)).read())[0];
                DataAdaptor childAdaptor = adaptor.createChild(tagName);
                readNode(childAdaptor, (Group) object);
            }
        }

    }

    /**
     * Read all DataSet in the Group and store them as attributes of the
     * DataAdaptor. It only implements the Datatypes supported by
     * {@link xal.tools.data.DataAdaptor}.
     *
     * @throws Exception
     */
    private void readAttribute(DataAdaptor adaptor, Dataset dataset) throws Exception {
        dataset.init();
        
        switch (dataset.getDatatype().getDatatypeClass()) {
            case Datatype.CLASS_INTEGER:
                switch ((int) dataset.getDatatype().getDatatypeSize()) {
                    case Byte.BYTES:
                        adaptor.setValue(dataset.getName(), (((byte[]) dataset.getData())[0] != 0));
                        break;
                    case Integer.BYTES:
                        adaptor.setValue(dataset.getName(), ((int[]) dataset.getData())[0]);
                        break;
                    case Long.BYTES:
                        adaptor.setValue(dataset.getName(), ((long[]) dataset.getData())[0]);
                        break;
                }
                break;
            case Datatype.CLASS_FLOAT:
                if (dataset.getDims()[0] == 1) {
                    adaptor.setValue(dataset.getName(), ((double[]) dataset.getData())[0]);
                } else {
                    adaptor.setValue(dataset.getName(), (double[]) dataset.getData());
                }
                break;
            case Datatype.CLASS_STRING:
                adaptor.setValue(dataset.getName(), ((String[]) dataset.getData())[0]);
                break;
        }
    }
}
