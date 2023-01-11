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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
import netscape.javascript.JSObject;
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

    public class JavaBridge {

        public void log(String text) {
            LOGGER.log(Level.INFO, "JS console: {0}", text);
        }
    }
    private final JavaBridge bridge = new JavaBridge();
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
    @FXML
    private ButtonBar editorPreviewBB;
    @FXML
    private Button loginB;

    private static final OlogClient CLIENT = OlogClient.getClient();

    private Long logId = 0L;

    private ObservableList<Attachment> attachments = FXCollections.observableList(new ArrayList());
    private ObservableList<Property> properties = FXCollections.observableList(new ArrayList());

    private List<String> logbooks;
    private List<String> tags;
    private List<String> availableProperties;
    private Map<String, List<String>> serverProperties;
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

    private static final double EDITOR_MIN_HEIGHT = 330;
    private WebView editorWebView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        HBoxAttachmentsTitle.minWidthProperty().bind(titledPaneAttachments.widthProperty());
        HBoxPropertiesTitle.minWidthProperty().bind(titledPaneProperties.widthProperty());

        String loggedUser = CLIENT.getUserLogedIn();
        if (loggedUser == null) {
            userLabel.setText("");
            loginB.setText("Log in");
            loginB.setOnAction((e) -> {
                try {
                    login();
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, "Error while logging in.", ex);
                }
            });
        } else {
            userLabel.setText(loggedUser);
            loginB.setText("Log out");
            loginB.setOnAction((e) -> logout());
        }

        // Populating Olog attributes
        try {
            logbooks = CLIENT.getLogbooks();
            tags = CLIENT.getTags();
            serverProperties = CLIENT.getProperties();
            // To keep track of properties not yet included in the log entry.
            availableProperties = new ArrayList<>(serverProperties.keySet());
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

        propertyNameTTC.setCellFactory(c -> {
            TreeTableCell<Property, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setGraphic(null);
                    getTableRow().setEditable(false);
                    // Make only leaf nodes editable
                    if (getTableRow().getItem() != null && !getTableRow().getItem().getAttributes().isEmpty()) {
                        GridPane pane = new GridPane();
                        Label label = new Label(item);
                        Button removeButton = new Button("-");
                        removeButton.setStyle("-fx-background-insets: 0; -fx-padding: 0; -fx-border-color: #000000; -fx-border-radius: 3; -fx-font-size: 14; -fx-font-weight: bold");
                        removeButton.setMinWidth(20);
                        removeButton.setOnAction((e) -> removeProperty(item));
                        pane.add(label, 0, 0);
                        pane.add(removeButton, 1, 0);
                        GridPane.setMargin(label, new Insets(0, 0, 0, 20));
                        GridPane.setHgrow(removeButton, Priority.ALWAYS);
                        GridPane.setHalignment(removeButton, HPos.RIGHT);
                        setGraphic(pane);
                        setText(null);
                    }
                }

            };
            return cell;
        });

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
        InputStream editorJs = OlogPostEntryController.class.getResourceAsStream("/ckeditor5-md/build/ckeditor.js");
        InputStream editorHtmlTop = OlogPostEntryController.class.getResourceAsStream("/ckeditor5-md/ckeditor_top.html");
        InputStream editorHtmlBottom = OlogPostEntryController.class.getResourceAsStream("/ckeditor5-md/ckeditor_bottom.html");

        if (editorJs != null && editorHtmlTop != null && editorHtmlBottom != null) {
            // Markdown WYSIWYG editor
            LOGGER.log(Level.INFO, "CKEditor found. Loading it...");
            editorWebView = new WebView();
            editorWebView.setMinHeight(EDITOR_MIN_HEIGHT);
            VBoxEditor.setPrefHeight(EDITOR_MIN_HEIGHT);
            editorWebView.setPickOnBounds(true);
            VBoxEditor.getChildren().add(editorWebView);
            editorPreviewBB.setVisible(false);

            // Forward console.log() messages from JS to the Java logger.
            editorWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue)
                    -> {
                JSObject window = (JSObject) editorWebView.getEngine().executeScript("window");
                window.setMember("java", bridge);
                editorWebView.getEngine().executeScript("console.log = function(message)\n"
                        + "{\n"
                        + "    java.log(message);\n"
                        + "};");
            });

            editorWebView.getEngine().loadContent(loadCKEditor(editorJs, editorHtmlTop, editorHtmlBottom));
        } else {
            LOGGER.log(Level.INFO, "CKEditor missing. Loading alternative editor...");
            VBox.setVgrow(body, Priority.ALWAYS);
            VBox.setVgrow(previewWV, Priority.ALWAYS);
            VBox.setVgrow(editorSplitPane, Priority.ALWAYS);

            previewWV.prefHeightProperty().bind(VBoxEditor.heightProperty());
            editorSplitPane.setStyle("-fx-padding: 0;");

            editorSplitPane.getItems().addAll(body, previewWV);
            editorSplitPane.setDividerPositions(0.5);

            VBoxEditor.getChildren().add(body);
        }
    }

    private void login() {
        while (true) {
            AuthenticationPaneFX authenticationPaneFX = new AuthenticationPaneFX();
            Optional<Pair<String, char[]>> credentials = authenticationPaneFX.showAndWait();
            if (credentials.isPresent()) {
                try {
                    boolean loginSuccessful = CLIENT.login(credentials.get().getKey(), credentials.get().getValue());
                    if (loginSuccessful) {
                        userLabel.setText(credentials.get().getKey());

                        loginB.setText("Log out");
                        loginB.setOnAction((e) -> logout());

                        return;
                    }
                } catch (OlogUnauthorizedException | LogbookException ex) {
                    Logger.getLogger(OlogPostEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                return;
            }

            ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(AlertType.WARNING,
                    "Incorrect username or password. Would you like to try again?",
                    ok, cancel);

            alert.setTitle("Invalid credentials");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(cancel) == cancel) {
                return;
            }

        }
    }

    private void logout() {
        CLIENT.logout();
        CookieManager manager = (CookieManager) CookieHandler.getDefault();
        manager.getCookieStore().removeAll();
        userLabel.setText("");

        loginB.setText("Log in");
        loginB.setOnAction((e) -> login());
    }

    /**
     * Workaround to load CKEditor. Generate an HTML document in memory from
     * resources files.
     *
     * @param editorJs
     * @param editorHtmlTop
     * @param editorHtmlBottom
     * @return
     */
    private String loadCKEditor(InputStream editorJs, InputStream editorHtmlTop, InputStream editorHtmlBottom) {
        StringBuilder content = new StringBuilder();

        for (InputStream stream : new InputStream[]{editorHtmlTop, editorJs, editorHtmlBottom}) {
            try {
                BufferedReader bf = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = bf.readLine()) != null) {
                    content.append(line);

                }
            } catch (IOException ex) {
                Logger.getLogger(OlogPostEntryController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return content.toString();
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

    private void removeProperty(String propertyName) {
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property property = iterator.next();
            if (property.getName().equals(propertyName)) {
                iterator.remove();
                availableProperties.add(propertyName);
            }
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

        String bodyText;
        if (editorWebView == null) {
            bodyText = body.getText();
        } else {
            bodyText = (String) editorWebView.getEngine().executeScript("window.CKEDITOR.getData()");
        }

        bodyText = parseCleanBody(bodyText);

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

        // Log in if not yet logged
        if (!CLIENT.isLoggedIn()) {
            login();
            // Go back to the editor if the client cancels log in.
            if (!CLIENT.isLoggedIn()) {
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
            CLIENT.logout();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Wrong credentials");
            alert.showAndWait();
            return;
        }

        Stage stage = (Stage) buttonSubmit.getScene().getWindow();
        stage.close();
    }

    /**
     * This method takes the markdown generated manually or by CKEditor and
     * deals with some unsupported cases.
     *
     * @param bodyText
     * @return
     */
    private String parseCleanBody(String bodyText) {
        // TODO: add header to tables missing it.
        return bodyText;
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
     * @param options For check boxes, separate arguments with commas ",".
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
                if (availableProperties.contains(p.getKey())) {
                    List<String> propertiesMissing = new ArrayList<>(serverProperties.get(p.getKey()));
                    for (Property prop : newProp.getAttributes()) {
                        propertiesMissing.remove(prop.getName());
                    }
                    if (!propertiesMissing.isEmpty()) {
                        for (String attribute : propertiesMissing) {
                            newProp.addAttribute(attribute, "");
                        }
                    }
                }
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
        if (availableProperties.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("There are no (more) properties available.");
            alert.showAndWait();
            return;

        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(OlogPostEntryController.class
                    .getResource("/fxml/AddPropertyScene.fxml"));

            Parent root = (Parent) fxmlLoader.load();

            AddPropertyController controller = fxmlLoader.<AddPropertyController>getController();
            controller.setProperties(serverProperties.keySet());

            Scene scene = new Scene(root);
            scene
                    .getStylesheets().add(OlogPostEntryController.class
                            .getResource("/styles/olog.css").toExternalForm());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Property");
            stage.setScene(scene);

            stage.minHeightProperty().bind(((Region) root).minHeightProperty().add(stage.getHeight() - ((Region) root).getHeight()));

            stage.showAndWait();

            String propertyName = controller.getSelectedProperty();

            if (!propertyName.isBlank()) {
                properties.add(new Property(propertyName, (Map<String, String>) serverProperties.get(propertyName).stream().collect(Collectors.toMap(item -> item, item -> ""))));
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
}
