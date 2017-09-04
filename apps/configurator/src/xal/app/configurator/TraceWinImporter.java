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
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import xal.extension.tracewinimporter.TraceWin;

/**
 * Class to import a TraceWin input file to Open XAL .xdxf format.
 *
 * @author <juanf.estebanmuller@esss.se>
 */
public class TraceWinImporter {

    // TODO: add option to select directory and from repository.
    /**
     * Open file dialogs to select the TraceWin input file and Open XAL output
     * and performs the conversion using the {@link TraceWin} extension.
     *
     * @param scene JavaFX scene object
     */
    public static void importTraceWinFile(Scene scene) {
        String tracewinFilePath;
        String openXALDirPath;

        if (scene != null) {
            Window window = scene.getWindow();
            if (window != null) {

                tracewinFilePath = openFileDialog(window);

                if (tracewinFilePath != null) {
                    OpticsSwitcher opticsSwitcher = OpticsSwitcher.getInstance();

                    openXALDirPath = dirDialog(window, "Select Open XAL SMF output dir", opticsSwitcher.getOpticsLibraryPath());

                    if (openXALDirPath != null) {
                        TraceWin.main(new String[]{tracewinFilePath, openXALDirPath, "main"});
                    }

                    System.out.print("Exported");
                }
            }
        }
    }

    /**
     * Open file dialogs to select the TraceWin input directory and Open XAL
     * output and performs the conversion using the {@link TraceWin} extension.
     *
     * @param scene JavaFX scene object
     */
    public static void importTraceWinDir(Scene scene) {
        String tracewinDirPath;
        String openXALDirPath;

        if (scene != null) {
            Window window = scene.getWindow();
            if (window != null) {

                tracewinDirPath = dirDialog(window, "Select TraceWin input dir", null);

                if (tracewinDirPath != null) {
                    OpticsSwitcher opticsSwitcher = OpticsSwitcher.getInstance();

                    openXALDirPath = dirDialog(window, "Select Open XAL SMF output dir", opticsSwitcher.getOpticsLibraryPath());

                    if (openXALDirPath != null) {
                        TraceWin.main(new String[]{tracewinDirPath, openXALDirPath, "main"});
                    }

                    System.out.print("Exported");
                }
            }
        }
    }

    /**
     * Open file dialogs to select the repository containing the TraceWin files
     * and the directory to save the Open XAL output and then it performs the
     * conversion using the {@link TraceWin} extension.
     *
     * @param scene JavaFX scene object
     */
    public static void importTraceWinGit(Scene scene) {
        TextInputDialog dialog = new TextInputDialog("https://api.bitbucket.org/2.0/repositories/europeanspallationsource/ess-lattice/src/");
        dialog.getDialogPane().setMinWidth(600);
        dialog.setTitle("Git importer");
        dialog.setHeaderText("Importing TraceWin files from Git");
        dialog.setContentText("Repository address:");
        dialog.setGraphic(null);

        Optional<String> gitRepositoryOpt = dialog.showAndWait();

        gitRepositoryOpt.ifPresent(gitRepository -> {
            if (scene != null) {
                Window window = scene.getWindow();
                if (window != null) {
                    OpticsSwitcher opticsSwitcher = OpticsSwitcher.getInstance();

                    String openXALDirPath = dirDialog(window, "Select Open XAL SMF output dir", opticsSwitcher.getOpticsLibraryPath());
                    if (openXALDirPath != null) {
                        TraceWin.main(new String[]{gitRepository, openXALDirPath, "main"});
                    }
                }
            }
        }
        );
    }

    /**
     * Open a file dialog to select the TraceWin input file.
     *
     * @param window Owner window object, null if none
     * @return TraceWin file path
     */
    private static String openFileDialog(Window window) {
        String tracewinFilePath = null;

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select TraceWin input File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TraceWin Input Files", "*.dat"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File choice = chooser.showOpenDialog(window);

        if (choice != null && choice.exists() && choice.isFile() && choice.canRead()) {
            tracewinFilePath = choice.getPath();
        }

        return tracewinFilePath;

    }

    /**
     * Open a file dialog to select the Open XAL SMF output file.
     *
     * @param window Owner window object, null if none
     * @return Open XAL SMF file path
     */
    private static String dirDialog(Window window, String title, String path) {
        String openXALFilePath = null;

        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle(title);

        if (path != null) {
            chooser.setInitialDirectory(new File(path));
        }

        File choice = chooser.showDialog(window);

        if (choice != null) {
            openXALFilePath = choice.getPath();
        }

        return openXALFilePath;
    }
//    /**
//     * Open a file dialog to select the Open XAL SMF output file.
//     *
//     * @param window Owner window object, null if none
//     * @return Open XAL SMF file path
//     */
//    private static String saveFileDialog(Window window, String tracewinDirPath) {
//        String openXALDirPath = null;
//
//        FileChooser chooser = new FileChooser();
//
//        chooser.setTitle("Select Open XAL SMF File");
//        chooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Open XAL SMF Files", "*.xdxf"),
//                new FileChooser.ExtensionFilter("All Files", "*.*")
//        );
//
//        // The default new filename is the same as the input file name but with
//        // .xdxf extension
//        int pathIdx = tracewinDirPath.lastIndexOf(File.separator);
//        int extIdx = tracewinDirPath.lastIndexOf('.');
//        String newDirPath = tracewinDirPath.substring(0, pathIdx);
//        String newFileName = tracewinDirPath.substring(pathIdx + 1, extIdx);
//        newFileName = newFileName.concat(".xdxf");
//
//        chooser.setInitialDirectory(new File(newDirPath));
//        chooser.setInitialFileName(newFileName);
//
//        File choice = chooser.showSaveDialog(window);
//
//        if (choice != null) {
//            openXALDirPath = choice.getPath();
//        }
//
//        return openXALDirPath;
//    }
}
