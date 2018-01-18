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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import xal.extension.tracewinimporter.ImportLogger;

/**
 * Class extending ImportLogger to display the import log in a dialog window
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class JavaFXLogger extends ImportLogger {

    private TextArea textArea = null;
    private Alert alert = null;

    public Alert getAlert() {
        return alert;
    }

    public JavaFXLogger() {
        alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("TraceWin Importer");
        alert.setHeaderText("Importing from TraceWin...");
        alert.setContentText("Progress and report:");

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        // To prevent window from closing
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
            event.consume();
        });
        alert.show();
    }

    /**
     * This method appends a new String to the current output. It can be run
     * from another thread, as the update is done from the JavaFX thread
     *
     * @param string
     */
    @Override
    public void log(String string) {
        Platform.runLater(() -> {
            textArea.setText(textArea.getText().concat(string).concat("\n"));
        });
    }

    /**
     * After the import, close() must be called to enable the OK button to be
     * able to close the dialog
     */
    @Override
    public void close() {
        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {});
    }
}
