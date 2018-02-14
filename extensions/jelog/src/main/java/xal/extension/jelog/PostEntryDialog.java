/*
 * Copyright (C) 2018 European Spallation Source ERIC.
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
package xal.extension.jelog;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.esss.jelog.Attachment;
import se.esss.jelog.PostEntryController;

/**
 * Class to submit new entries using a dialog. Useful for logging data together
 * with some files or images attached directly from an Open XAL application.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PostEntryDialog {

    public static void post() throws IOException, Exception {
        post(null, null, null);
    }

    public static void post(String defaultLogbook) throws IOException, Exception {
        post(null, null, defaultLogbook);
    }

    public static void post(WritableImage[] snapshots) throws IOException, Exception {
        post(snapshots, null, null);
    }

    public static void post(Attachment[] attachments) throws IOException, Exception {
        post(null, attachments, null);
    }

    public static void post(WritableImage[] snapshots, String defaultLogbook) throws IOException, Exception {
        post(snapshots, null, defaultLogbook);
    }

    public static void post(Attachment[] attachments, String defaultLogbook) throws IOException, Exception {
        post(null, attachments, defaultLogbook);
    }

    public static void post(WritableImage[] snapshots, Attachment[] attachments, String defaultLogbook) throws IOException, Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(PostEntryController.class.getResource("/fxml/PostEntryScene.fxml"));

        Parent root = (Parent) fxmlLoader.load();

        PostEntryController controller = fxmlLoader.<PostEntryController>getController();

        controller.setElogServer(ElogServer.getElogURL());
        
        if (defaultLogbook != null) {
            controller.setDefaultLogbook(defaultLogbook);
        }

        if (controller.login()) {
            controller.setSnapshots(snapshots);
            controller.setAttachments(attachments);

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/Styles.css");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("elog");
            stage.setScene(scene);
            stage.show();
        }
    }
}
