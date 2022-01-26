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

import java.io.ByteArrayInputStream;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xal.extension.logbook.Attachment;

/**
 * Pane showing a thumbnail of an attachment image or the filename for binaries
 * and a cross icon on the top right to remove the attachment.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class AttachmentPane extends StackPane {

    // Reference to the attachement list.
    private final List<Attachment> attachments;
    private Image image;

    /**
     * Create a new attachment pane.
     *
     * @param attachment The attachment object.
     * @param attachments The list of attachments, required for the remove
     * callback.
     */
    public AttachmentPane(Attachment attachment, List<Attachment> attachments) {
        super();

        this.attachments = attachments;

        Label label = new Label(attachment.getFileName());
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: rgba(255,255,255,0.9);");
        StackPane innerStackPane = new StackPane();

        // Try to load the image, if possible
        image = new Image(new ByteArrayInputStream(attachment.getContent().toByteArray()));
        // Show a preview for images or a square for binary attachments.
        if (image.isError()) {
            Rectangle rect = new Rectangle();
            rect.setHeight(90);
            rect.setWidth(150);
            rect.setFill(Color.WHITE);
            rect.setEffect(new DropShadow(5, Color.BLACK));
            label.setMaxWidth(0.9 * rect.getLayoutBounds().getWidth());
            innerStackPane.getChildren().addAll(rect, label);
        } else {
            ImageView imageView = createImageView(image);
            label.setMaxWidth(0.9 * imageView.getLayoutBounds().getWidth());
            innerStackPane.getChildren().addAll(imageView, label);
        }

        // Adds a remove icon to delete attachments.
        ImageView removeIconIW = removeIcon(attachment);

        this.setAlignment(Pos.TOP_RIGHT);
        this.getChildren().addAll(innerStackPane, removeIconIW);

        // Clicking opens a larger preview for images.
        if (!image.isError()) {
            innerStackPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {

                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        if (mouseEvent.getClickCount() == 1) {
                            BorderPane borderPane = new BorderPane();
                            ImageView imageView = new ImageView();
                            imageView.setImage(image);
                            imageView.setPreserveRatio(true);
                            imageView.setSmooth(true);
                            imageView.setCache(true);
                            borderPane.setCenter(imageView);

                            Stage newStage = new Stage();
                            Scene scene = new Scene(borderPane, Color.BLACK);
                            newStage.setTitle(attachment.getFileName());
                            newStage.initModality(Modality.APPLICATION_MODAL);
                            newStage.setScene(scene);
                            // Preview can be close by clicking on it.
                            imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                        newStage.close();
                                    }
                                }
                            });
                            newStage.show();
                        }
                    }
                }
            });
        }

    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(90);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        // apply a shadow effect.
        imageView.setEffect(new DropShadow(5, Color.BLACK));

        return imageView;
    }

    private ImageView removeIcon(Attachment attachment) {
        Image removeIconI = new Image("pictures/remove_icon_25x25.png");
        ImageView removeIconIW = new ImageView(removeIconI);
        removeIconIW.setFitHeight(25);
        removeIconIW.setFitWidth(25);
        removeIconIW.setPreserveRatio(true);

        removeIconIW.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 1) {
                        attachments.remove(attachment);
                    }
                }
            }
        });

        return removeIconIW;
    }
}
