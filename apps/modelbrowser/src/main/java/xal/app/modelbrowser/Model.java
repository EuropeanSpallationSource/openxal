/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.app.modelbrowser;


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
