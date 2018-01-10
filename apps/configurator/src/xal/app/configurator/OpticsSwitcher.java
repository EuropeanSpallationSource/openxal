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
package xal.app.configurator;

import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import xal.smf.data.XMLDataManager;

/**
 * Singleton class to retrieve and update Open XAL default optics and update the
 * GUI elements accordingly
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public final class OpticsSwitcher {

    private static final OpticsSwitcher INSTANCE = new OpticsSwitcher();

    private ObservableList<String> opticsItems;
    private ObservableList<String> fileItems;
    private String opticsLibraryPath = null;
    private String prevOpticsPath = null;

    public static OpticsSwitcher getInstance() {
        return INSTANCE;
    }

    private OpticsSwitcher() {
    }

    private String getDefaultPath() {
        return XMLDataManager.defaultPath();
    }

    public boolean setDefaultPath(ListView<String> opticsListView) {
        // Save current path for revert function
        prevOpticsPath = getDefaultPath();

        int defaultIndex;
        defaultIndex = opticsListView.getSelectionModel().getSelectedIndex();

        if (defaultIndex != -1) {
            String newDefaultPath;
            newDefaultPath = fileItems.get(defaultIndex);

            XMLDataManager.setDefaultPath(newDefaultPath);

            refreshList(opticsListView);

            return true;
        }

        return false;
    }

    /**
     * This method allows to revert a setDefaultPath() call and update the
     * ListView tree
     *
     * @param opticsListView
     */
    public void revertDefaultPath(ListView<String> opticsListView) {
        XMLDataManager.setDefaultPath(prevOpticsPath);

        prevOpticsPath = null;

        refreshList(opticsListView);
    }

    public void setOpticsLibraryPath(String opticsPath) {
        opticsLibraryPath = opticsPath;
    }

    public String getDefaultOpticsLibraryPath() {
        String opticsPath;
        if (getDefaultPath() != null) {
            opticsPath = new File(getDefaultPath()).getParentFile().getParent();
        } else {
            opticsPath = null;
        }

        return opticsPath;
    }

    public String getOpticsLibraryPath() {
        String opticsPath;
        if (opticsLibraryPath == null) {
            opticsPath = getDefaultOpticsLibraryPath();
        } else {
            opticsPath = opticsLibraryPath;
        }
        return opticsPath;
    }

    public String setDefaultOpticsPathDialog(Scene scene, String initialDirectory) {
        String newOpticsPath = null;

        if (scene != null) {
            Window window = scene.getWindow();

            if (window != null) {

                DirectoryChooser chooser = new DirectoryChooser();

                chooser.setTitle("Select Default Optics Path");

                File initialDirectoryFile = new File(initialDirectory);
                if (initialDirectoryFile.exists()) {
                    chooser.setInitialDirectory(initialDirectoryFile);
                }

                File choice = chooser.showDialog(window);

                if (choice != null && choice.exists() && choice.canRead()) {
                    newOpticsPath = choice.getPath();
                    opticsLibraryPath = newOpticsPath;
                }
            }
        }

        return newOpticsPath;
    }

    /**
     * Refreshes the ListView element with the list of optics available,
     * indicating which one is the default accelerator.
     *
     * @param opticsListView
     */
    public void refreshList(ListView<String> opticsListView) {
        opticsItems = FXCollections.observableArrayList();
        fileItems = FXCollections.observableArrayList();

        int indexDefault = -1;

        String libraryPath = getOpticsLibraryPath();
        if (libraryPath.startsWith("~")) {
            libraryPath = libraryPath.replace("~", System.getProperty("user.home"));
        }

        File defaultOptics = new File(libraryPath);
        String defaultPath = getDefaultPath();

        if (defaultOptics.exists()) {

            File[] directoryArray = defaultOptics.listFiles();
            File[] fileArray;
            String filename;

            // Select directories containing .xal files
            for (File directory : directoryArray) {
                if (directory.isDirectory()) {
                    fileArray = directory.listFiles();
                    for (File file : fileArray) {
                        filename = file.getParentFile().getName();
                        filename = filename.concat(File.separator);
                        filename = filename.concat(file.getName());
                        if (filename.endsWith(".xal")) {
                            if (file.getPath().equals(defaultPath)) {
                                indexDefault = opticsItems.size();
                                filename = filename.concat(" (Default)");
                            }

                            opticsItems.add(filename);
                            fileItems.add(file.getPath());

                        }
                    }
                }
            }
        } else {
            opticsLibraryPath = getDefaultOpticsLibraryPath();
        }

        opticsListView.setItems(opticsItems);
        if (indexDefault != -1) {
            opticsListView.getSelectionModel().select(indexDefault);
        }
    }
}
