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

package openxal.apps.scanner;


import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;


/**
 * Singleton class containing the {@link Accelerator} model.
 *
 * @author claudio.rosati@esss.se
 */
public class Model {

    private static final Logger LOGGER     = Logger.getLogger(Model.class.getName());
    private static String modelFilePath = null;

    public static Model getInstance() {
        return AcceleratorModelHolder.INSTANCE;
    }

    public static void setModelFile( String filePath ) {
        modelFilePath = filePath;
    }

    private final Accelerator accelerator;

    private Model() {

        String dataFilePath = null;

        if ( StringUtils.isBlank(dataFilePath) && StringUtils.isNotBlank(modelFilePath) ) {

            Path path = FileSystems.getDefault().getPath(modelFilePath);

            if ( Files.exists(path) && Files.isReadable(path) && !Files.isDirectory(path) ) {
                dataFilePath = path.toString();
            }

        }

        if ( StringUtils.isBlank(dataFilePath) ) {

            Path path = FileSystems.getDefault().getPath(XMLDataManager.defaultPath());

            if ( Files.exists(path) && Files.isReadable(path) && !Files.isDirectory(path) ) {
                dataFilePath = path.toString();
            }

        }

        if ( StringUtils.isBlank(dataFilePath) ) {

            FileChooser chooser = new FileChooser();

            chooser.setTitle("Select Accelerator Model File");
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OpenXAL Model Files", "*.xal"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File choice = chooser.showOpenDialog(null);

            if ( choice != null && choice.exists() && choice.isFile() && choice.canRead() ) {
                dataFilePath = choice.getPath();
            }

        }

        accelerator = XMLDataManager.acceleratorWithPath(dataFilePath);

    }

    public Accelerator getAccelerator() {
        return accelerator;
    }

    @SuppressWarnings( "UtilityClassWithoutPrivateConstructor" )
    private static class AcceleratorModelHolder {

        private static final Model INSTANCE = new Model();

    }

}
