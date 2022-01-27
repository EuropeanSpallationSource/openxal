/*
 * Copyright (C) 2022 European Spallation Source ERIC.
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
package xal.plugin.olog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 *
 * @author nataliamilas
 */
public class AuthenticationPaneFX extends Dialog<Pair<String, char[]>> {

    private TextField tfUsername;
    private PasswordField pfPassword;

    /**
     * Constructs a new authentication pane.
     */
    public AuthenticationPaneFX() {
        this(null);
    }

    /**
     * Creates a dialog that contains this pane.
     *
     * @param parent the parent of this dialog
     */
    public AuthenticationPaneFX(Parent parent) {
        super();

        setTitle("ESS Logbook Login Dialog");
        setHeaderText("Authenticate user:");
        // Set the icon (must be included in the project).
        setGraphic(new ImageView(this.getClass().getResource("/pictures/login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        tfUsername = new TextField();
        tfUsername.setPromptText("Username");
        pfPassword = new PasswordField();
        pfPassword.setPromptText("Password");

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(tfUsername, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(pfPassword, 1, 1);

        getDialogPane().setContent(grid);

        // Do some validation (using the Java 8 lambda syntax).
        tfUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        // Request focus on the username field by default.
        Platform.runLater(() -> tfUsername.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(tfUsername.getText(), pfPassword.getText().toCharArray());
            }
            return null;
        });
    }
}
