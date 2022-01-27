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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.converter.DefaultStringConverter;
import org.commonmark.Extension;
import org.json.JSONException;
import org.json.JSONObject;
import xal.extension.logbook.Attachment;
import xal.extension.logbook.LogbookException;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;

/**
 * FXML Controller class
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class OlogPostEntryController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(OlogPostEntryController.class.getName());

    @FXML
    private VBox vBoxParent;
    @FXML
    private GridPane gridPaneLogbook;
    @FXML
    private GridPane gridPaneAttributes;
    @FXML
    private TitledPane titledPaneAttachments;
    @FXML
    private HBox HBoxAttachmentsTitle;
    @FXML
    private Button addAttachmentButton;
    @FXML
    private ScrollPane thumbnailsScrollPane;
    @FXML
    private FlowPane thumbnailsPane;
    @FXML
    private VBox VBoxEditor;
    @FXML
    private ButtonBar buttonBar;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSubmit;
    @FXML
    private TitledPane titledPaneProperties;
    @FXML
    private HBox HBoxPropertiesTitle;
    @FXML
    private Button addPropertyButton;

    private static final OlogClient CLIENT = OlogClient.getClient();

    private Long logId = 0L;

    private ObservableList<Attachment> attachments = FXCollections.observableList(new ArrayList());
    private ObservableList<Property> properties = FXCollections.observableList(new ArrayList());

    private List<String> logbooks;
    private List<String> tags;
    private Map<String, List<String>> availableProperties;
    @FXML
    private Label userLabel;
    @FXML
    private FlowPane logbooksFP;
    @FXML
    private HBox entryTypeHBox;
    @FXML
    private ComboBox<String> entryTypeCB;
    @FXML
    private FlowPane tagsFP;
    @FXML
    private TextField subjectTF;
    @FXML
    private VBox VBoxPropertyEditor;
    @FXML
    private TreeTableView<Property> propertiesTTV;
    @FXML
    private TreeTableColumn<Property, String> propertyNameTTC;
    @FXML
    private TreeTableColumn<Property, String> propertyValueTTC;
    @FXML
    private ToggleButton markdownB;
    @FXML
    private ToggleButton splitB;
    @FXML
    private ToggleButton previewB;

    private TextArea body = new TextArea();
    private WebView previewWV = new WebView();
    
    private SplitPane editorSplitPane = new SplitPane();
    private WebEngine engine = previewWV.getEngine();
    @FXML
    private Button logoutB;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        HBoxAttachmentsTitle.minWidthProperty().bind(titledPaneAttachments.widthProperty());
        HBoxPropertiesTitle.minWidthProperty().bind(titledPaneProperties.widthProperty());

        if (CLIENT.isLoggedIn()) {
            userLabel.setText(CLIENT.getUserName());
            logoutB.setDisable(false);
        }

        // Populating Olog attributes
        try {
            logbooks = CLIENT.getLogbooks();
            tags = CLIENT.getTags();
            availableProperties = CLIENT.getProperties();
        } catch (LogbookException ex) {
            String errMsg = "Could not retrieve logbook configuration.";
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(errMsg);
            alert.setContentText("Check configuration and try again");
            alert.showAndWait();

            throw new RuntimeException(errMsg);
        }

        // Creating checkboxes for logbooks
        List<CheckBox> checkBoxLogbooksList = new ArrayList<>();
        for (String logbook : logbooks) {
            CheckBox checkBox = new CheckBox(logbook);
            checkBoxLogbooksList.add(checkBox);
        }
        logbooksFP.getChildren().addAll(checkBoxLogbooksList);

        // Creating checkboxes for tags
        List<CheckBox> checkBoxTagsList = new ArrayList<>();
        for (String logbook : tags) {
            CheckBox checkBox = new CheckBox(logbook);
            checkBoxTagsList.add(checkBox);
        }
        tagsFP.getChildren().addAll(checkBoxTagsList);

        // Filling entry types
        entryTypeCB.getItems().addAll(CLIENT.getEntryTypeList());
        entryTypeCB.getSelectionModel().selectFirst();

        // Hook a listener to update attachments panel
        attachments.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change ch) {
                updateAttachments();
            }
        });
        // Hook a listener to update attachments panel
        properties.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change ch) {
                updateProperties();
            }
        });

        propertyNameTTC.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        propertyValueTTC.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

        propertyValueTTC.setCellFactory(c -> {
            TreeTableCell<Property, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    // Make only leaf nodes editable
                    if (getTableRow().getItem() != null && getTableRow().getItem().getAttributes().isEmpty()) {
                        getTableRow().setEditable(true);
                    } else {
                        getTableRow().setEditable(false);
                    }
                }

            };
            return cell;
        });

        // Preparing the editor panel
        VBox.setVgrow(body, Priority.ALWAYS);
        VBox.setVgrow(previewWV, Priority.ALWAYS);
        VBox.setVgrow(editorSplitPane, Priority.ALWAYS);
        
        previewWV.prefHeightProperty().bind(VBoxEditor.heightProperty());
        editorSplitPane.setStyle("-fx-padding: 0;");
        
        editorSplitPane.getItems().addAll(body, previewWV);
        editorSplitPane.setDividerPositions(0.5);

        VBoxEditor.getChildren().add(body);
    }

    @FXML
    private void addAttachmentButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add attachment");
        File file = fileChooser.showOpenDialog(addAttachmentButton.getScene().getWindow());
        if (file != null) {
            try {
                attachments.add(new Attachment(file));

            } catch (IOException ex) {
                Logger.getLogger(OlogPostEntryController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateAttachments() {
        thumbnailsPane.getChildren().clear();
        if (!attachments.isEmpty()) {
            titledPaneAttachments.setExpanded(true);
            for (Attachment attachment : attachments) {
                AttachmentPane stackPane = new AttachmentPane(attachment, attachments);
                thumbnailsPane.getChildren().addAll(stackPane);
            }
        } else {
            titledPaneAttachments.setExpanded(false);
        }
    }

    // TODO: option to remove properties
    private void updateProperties() {
        if (!properties.isEmpty()) {
            titledPaneProperties.setExpanded(true);

            TreeItem<Property> propertyItems = new TreeItem(new Property("root"));
            for (Property property : properties) {
                TreeItem item = new TreeItem(property);
                propertyItems.getChildren().add(item);
                for (Property attribute : property.getAttributes()) {
                    TreeItem childItem = new TreeItem(attribute);
                    item.getChildren().add(childItem);
                }
                item.setExpanded(true);
            }
            propertiesTTV.setRoot(propertyItems);
        } else {
            titledPaneProperties.setExpanded(false);
        }
    }

    @FXML
    private void handleButtonCancel() {
        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleButtonSubmit(ActionEvent event) {
        String title = subjectTF.getText();
        String bodyText = body.getText();
        String level = entryTypeCB.getSelectionModel().getSelectedItem();

        List<String> logbooks = new ArrayList<>();
        for (Node logbookCB : logbooksFP.getChildren()) {
            if (((CheckBox) logbookCB).isSelected()) {
                logbooks.add(((CheckBox) logbookCB).getText());
            }
        }

        // Check required metadata
        if (title.isBlank() || logbooks.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing required data");
            String content = "";
            if (title.isBlank()) {
                content += "Subject";
                if (logbooks.isEmpty()) {
                    content += ", ";
                }
            }
            if (logbooks.isEmpty()) {
                content += "Logbooks";
            }
            alert.setContentText(content);
            alert.showAndWait();

            return;
        }

        LogEntry log = new LogEntry(title, bodyText, level, logbooks);

        // Adding tags
        List<String> tags = new ArrayList<>();
        for (Node tagCB : tagsFP.getChildren()) {
            if (((CheckBox) tagCB).isSelected()) {
                tags.add(((CheckBox) tagCB).getText());
            }
        }
        log.addTags(tags);

        for (Attachment att : attachments) {
            log.addAttachment(att);
        }

        for (Property prop : properties) {
            log.addProperty(prop);
        }

        if (!CLIENT.isLoggedIn()) {
            AuthenticationPaneFX authenticationPaneFX = new AuthenticationPaneFX();
            Optional<Pair<String, char[]>> credentials = authenticationPaneFX.showAndWait();
            if (credentials.isPresent()) {
                CLIENT.setCredentials(credentials.get().getKey(), credentials.get().getValue());
            } else {
                return;
            }
        }

        try {
            String jsonEntry = CLIENT.submitEntry(log);
            logId = new JSONObject(jsonEntry).getLong("id");
        } catch (JSONException | LogbookException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Exception in submitting entry");
            alert.showAndWait();
            return;
        } catch (OlogUnauthorizedException e) {
            CLIENT.forgetCredentials();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Wrong credentials");
            alert.showAndWait();
            return;
        }

        Stage stage = (Stage) buttonSubmit.getScene().getWindow();
        stage.close();
    }

    void setDefaultLogbooks(String[] defaultLogbooks) {
        for (Node logbook : logbooksFP.getChildren()) {
            CheckBox logbookCB = (CheckBox) logbook;
            String logbookName = logbookCB.getText();
            for (String defaultLogbook : defaultLogbooks) {
                if (logbookName.equals(defaultLogbook)) {
                    logbookCB.setSelected(true);
                }
            }
        }
    }

    public Long getLogId() {
        return logId;
    }

    /**
     * Default attributes are attributes that are set by applications using this
     * library.
     *
     * @param attributeName Name of the attribute.
     * @param options For checkboxes, separate arguments with commas ",".
     */
    public void addDefaultAttribute(String attributeName, List<String> options) {
        if (attributeName.equals(OlogProvider.SUBJECT_STR)) {
            subjectTF.setText(options.get(0));
        } else if (attributeName.equals("Text")) {
            body.setText(options.get(0));
        } else if (attributeName.equals(OlogProvider.ENTRY_TYPE_STR)) {
            entryTypeCB.getSelectionModel().select(options.get(0));
        } else if (attributeName.equals(OlogProvider.TAGS_STR)) {
            for (Node tag : tagsFP.getChildren()) {
                CheckBox tagCB = (CheckBox) tag;
                String tagName = tagCB.getText();
                for (String option : options) {
                    if (tagName.equals(option)) {
                        tagCB.setSelected(true);
                    }
                }
            }
        } else if (attributeName.equals(OlogProvider.PROPERTIES_STR)) {
            for (String property : options) {
                Pair<String, Map<String, String>> p = OlogProvider.parseProperty(property);
                Property newProp = new Property(p.getKey(), p.getValue());
                properties.add(newProp);
                availableProperties.remove(p.getKey());
            }
        }
    }

    public void addAttachments(List<Attachment> attachments) {
        this.attachments.addAll(attachments);
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        addAttachments(attachments);
    }

    @FXML
    private void addPropertyButtonAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(OlogPostEntryController.class.getResource("/fxml/AddPropertyScene.fxml"));

            Parent root = (Parent) fxmlLoader.load();

            AddPropertyController controller = fxmlLoader.<AddPropertyController>getController();
            controller.setProperties(availableProperties.keySet());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(OlogPostEntryController.class.getResource("/styles/olog.css").toExternalForm());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Property");
            stage.setScene(scene);

            stage.minHeightProperty().bind(((Region) root).minHeightProperty().add(stage.getHeight() - ((Region) root).getHeight()));

            stage.showAndWait();

            String propertyName = controller.getSelectedProperty();

            if (!propertyName.isBlank()) {
                properties.add(new Property(propertyName, (Map<String, String>) availableProperties.get(propertyName).stream().collect(Collectors.toMap(item -> item, item -> ""))));
                availableProperties.remove(propertyName);
            }
        } catch (Exception ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading the Add Property Dialog");
            alert.showAndWait();
        }
    }

    @FXML
    private void markdownBAction(ActionEvent event) {
        markdownB.setSelected(true);

        splitB.setSelected(false);
        previewB.setSelected(false);

        VBoxEditor.getChildren().clear();

        VBoxEditor.getChildren().add(body);

        body.setOnKeyTyped(null);
    }

    @FXML
    private void splitBAction(ActionEvent event) {
        splitB.setSelected(true);

        markdownB.setSelected(false);
        previewB.setSelected(false);

        VBoxEditor.getChildren().clear();
        editorSplitPane.getItems().clear();

        editorSplitPane.getItems().addAll(body, previewWV);
        editorSplitPane.setDividerPositions(0.5);

        VBoxEditor.getChildren().add(editorSplitPane);

        body.setOnKeyTyped((e) -> updatePreview());

        updatePreview();
    }

    @FXML
    private void previewBAction(ActionEvent event) {
        previewB.setSelected(true);

        markdownB.setSelected(false);
        splitB.setSelected(false);

        VBoxEditor.getChildren().clear();

        VBoxEditor.getChildren().add(previewWV);

        body.setOnKeyTyped(null);

        updatePreview();
    }

    private void updatePreview() {

        List<Extension> extensions
                = Arrays.asList(TablesExtension.create(), ImageAttributesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions).build();

        org.commonmark.node.Node document = parser.parse(body.getText());

        String html = renderer.render(document);
        engine.loadContent(html);
    }

    @FXML
    private void logoutBAction(ActionEvent event) {
        CLIENT.forgetCredentials();
        userLabel.setText("");
        logoutB.setDisable(true);
    }
}
