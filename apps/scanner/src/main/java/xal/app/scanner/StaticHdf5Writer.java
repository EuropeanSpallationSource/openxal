/*
 * Copyright (c) 2017, Open XAL Collaboration
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
package xal.app.scanner;

import hdf.object.Datatype;
import hdf.object.FileFormat;
import hdf.object.HObject;
import hdf.object.h5.H5Datatype;
import hdf.object.h5.H5File;
import hdf.object.h5.H5Group;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an early template of what we need in terms of
 * static HDF5 writer.
 *
 * Should be refactored and put in a core i/o class
 *
 * @author yngvelevinsen
 */
public class StaticHdf5Writer {
    public static void writeArrayAsHDF5DataSet(String filePath, String path, String tStamp, double[] data) throws Exception {
        // retrieve an instance of H5File.

        int gzip = 0;
        int chunks = 1024;

        if (data.length<chunks) {
            chunks = data.length;
        }

        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            Logger.getLogger(StaticHdf5Writer.class.getName()).log(Level.SEVERE, "Can't find HDF5 FileFormat. Check the java library path.");
            throw new RuntimeException();
        }

        File file = new File(getMeasurementsFileName(filePath));

        // create a new file with a given file name.
        H5File h5File = new H5File(file.getAbsolutePath(), FileFormat.FILE_CREATE_OPEN);

        h5File.open();

        if (h5File == null) {
            Logger.getLogger(StaticHdf5Writer.class.getName()).log(Level.SEVERE, "Failed to create file: {0}", file.getName());
            throw new RuntimeException();
        }


        // We only need to do this once at start of measurement!
        H5Group group = (H5Group) h5File.getRootObject();
        for ( String name : path.split("/")) {
            if (name.length()>0) {
                group = getOrCreateGroup(name, group);
            }
        }

        Logger.getLogger(StaticHdf5Writer.class.getName()).log(Level.FINEST, "Writing dataset {0} to {1}", new Object[]{tStamp, group.getFullName()});
        H5Datatype dtype = new H5Datatype(Datatype.CLASS_FLOAT, Double.BYTES, Datatype.NATIVE, Datatype.NATIVE);

        // Deal with potentially trying to write same timestamp several times
        int i = 0;
        for (HObject object : group.getMemberList()) {
            if (object.getName().equals(tStamp) || object.getName().equals(tStamp+"-"+i))
                i+=1;
        }
        if (i>0)
            tStamp+="-"+i;

        h5File.createScalarDS(tStamp, group, dtype, new long[]{ data.length }, null, new long[]{chunks}, gzip, data);

        h5File.close();
    }

    /**
     * If the name already exist in group, return it cast as a H5Group,
     * otherwise create it and return the object.
     *
     * @param name The name of the child group object
     * @param group The name of the parent group
     * @return The child group
     * @throws Exception
     */
    private static H5Group getOrCreateGroup(String name, H5Group group) throws Exception {
        for (HObject childGroup : group.getMemberList())
            if ( childGroup.getName().equals(name) )
                return (H5Group) childGroup;

        return H5Group.create( name, group);
    }

    /**
     *
     * @return The string that should be used as file
     */
    private static String getMeasurementsFileName(String source) {
        // If the user is writing the normal data to "Data.scanner.h5", we will create a new file Data.measurements.scanner.h5
        String parent = source.substring(0, source.lastIndexOf("/")+1);
        String fileName = source.substring( source.lastIndexOf('/')+1, source.length() );
        String newFileName;
        if (fileName.endsWith(".scan.h5"))
            return parent + fileName.substring(0,fileName.length()-8)+".measurements.scan.h5";
        else if (fileName.endsWith(".scan.hdf5"))
            return parent + fileName.substring(0,fileName.length()-10)+".measurements.scan.hdf5";
        else if (fileName.endsWith(".h5"))
            return parent + fileName.substring(0,fileName.length()-3)+".measurements.h5";
        else if (fileName.endsWith(".hdf5"))
            return parent + fileName.substring(0,fileName.length()-5)+".measurements.hdf5";
        return parent + fileName + ".measurements.h5";
    }
}