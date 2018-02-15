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

package xal.app.scanner;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

public class PVTree extends SplitPane {

    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_ALPHABETICALLY = (n1, n2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(n1.getId()), StringUtils.defaultString(n2.getId()));
    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_BY_POSITION = (n1, n2) -> (int) ( n1.getPosition() - n2.getPosition() );
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_ALPHABETICALLY = (s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(s1.getId()), StringUtils.defaultString(s2.getId()));
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_BY_POSITION = (s1, s2) -> (int) ( s1.getPosition() - s2.getPosition() );


    private final Map<String, CheckMenuItem> typeMap = new TreeMap<>();
    private final ObservableList<HandleWrapper> epicsChannels = FXCollections.observableArrayList();

    @FXML // fx:id="elementSearch"
    private TextField elementSearch; // Value injected by FXMLLoader

    @FXML // fx:id="elementSearchIcon"
    private StackPane elementSearchIcon; // Value injected by FXMLLoader

    @FXML // fx:id="elementSearchMenuButton"
    private MenuButton elementSearchMenuButton; // Value injected by FXMLLoader

    @FXML // fx:id="elementSortAlphabetically"
    private RadioMenuItem elementSortAlphabetically; // Value injected by FXMLLoader

    @FXML // fx:id="elementTree"
    private TreeView<AcceleratorNode> elementTree; // Value injected by FXMLLoader

    @FXML // fx:id="manualTextField"
    private TextField manualTextField; // Value injected by FXMLLoader
    private Channel manualChannel;

    @FXML // fx:id="pvCheckManualButton"
    private Button pvCheckManualButton; // Value injected by FXMLLoader


    @FXML // fx:id="pvTree"
    private TreeView<?> pvTree; // Value injected by FXMLLoader

    @FXML // fx:id="epicsTable"
    private TableView<HandleWrapper> epicsTable; // Value injected by FXMLLoader

    @FXML // fx:id="epicsNameColumn"
    private TableColumn<HandleWrapper, String> epicsNameColumn; // Value injected by FXMLLoader

    @FXML // fx:id="epicsTypeColumn"
    private TableColumn<HandleWrapper, String> epicsTypeColumn; // Value injected by FXMLLoader

    @FXML // fx:id="pvAddButton"
    private Button pvAddButton; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert elementSearch != null : "fx:id=\"elementSearch\" was not injected: check your FXML file 'PVTree.fxml'.";
        assert elementTree != null : "fx:id=\"elementTree\" was not injected: check your FXML file 'PVTree.fxml'.";
        assert pvTree != null : "fx:id=\"pvTree\" was not injected: check your FXML file 'PVTree.fxml'.";
        assert pvAddButton != null : "fx:id=\"pvAddButton\" was not injected: check your FXML file 'PVTree.fxml'.";

        Model.getInstance().getAccelerator().getRoot().getAllNodes().stream().map(n -> n.getType()).distinct().sorted().forEachOrdered(t -> addTypeMenuItem(t));

        elementTree.setCellFactory(p -> new NodeTreeCell());
        elementTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        elementTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateEPICSTable(oldValue, newValue));

        epicsTable.itemsProperty().setValue(epicsChannels);
        epicsNameColumn.setCellValueFactory(new PropertyValueFactory<>("typeHandle"));
        epicsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        epicsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        manualTextField.textProperty().addListener((obs, oldText, newText) -> {
            enteringManualPV(oldText, newText);
            // ...
        });

        if ( elementSearch.getLength() == 0 ) {
            //elementSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
        } else {
            //elementSearchIcon.getStyleClass().add("search-clear"); //NOI18N
        }

        elementSearch.textProperty().addListener(( observable, oldValue, newValue ) -> {

            if ( newValue.isEmpty() ) {
                elementSearchIcon.getStyleClass().clear();
                elementSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
            } else {
                elementSearchIcon.getStyleClass().clear();
                elementSearchIcon.getStyleClass().add("search-clear"); //NOI18N
            }

            updateTree(newValue);

        });

        updateTree(elementSearch.getText());
        Logger.getLogger(PVTree.class.getName()).log(Level.FINER, "PV Tree Widget initialized");
    }

    private void addOneChannel(Channel channel) {
        ChannelWrapper newChannelWrapper = new ChannelWrapper(channel);
        try {
            Logger.getLogger(PVTree.class.getName()).log(Level.FINER, "Channel {0} has write access {1}", new Object[]{channel.getId(), channel.writeAccess()});
            if (channel.writeAccess())
            {
                if (!FXMLController.pvScannablelist.contains(newChannelWrapper)) {
                    Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "Adding channel {0} to scannable list", new Object[]{channel.getId()});
                    FXMLController.pvScannablelist.add(newChannelWrapper);
                } else
                    Logger.getLogger(PVTree.class.getName()).log(Level.INFO, "Channel {0} has already been added", channel.getId());
            }
            else
            {
                if (!FXMLController.pvReadablelist.contains(newChannelWrapper)) {
                    Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "Adding channel {0} to readable list", new Object[]{channel.getId()});
                    FXMLController.pvReadablelist.add(newChannelWrapper);
                } else
                    Logger.getLogger(PVTree.class.getName()).log(Level.INFO, "Channel {0} has already been added", channel.getId());
            }
        } catch (ConnectionException ex) {
            Logger.getLogger(PVTree.class.getName()).log(Level.SEVERE, "Failed to check channel access", ex);
        }
    }

    @FXML
    void addSelectedPV(ActionEvent event) {
        Logger.getLogger(PVTree.class.getName()).log(Level.INFO, "Adding selected channels");
        int count = FXMLController.pvScannablelist.size() + FXMLController.pvReadablelist.size();

        if (epicsTable.getSelectionModel().getSelectedItems().size()>0) {
            epicsTable.getSelectionModel().getSelectedItems().forEach((HandleWrapper hw) -> {
                elementTree.getSelectionModel().getSelectedItems().forEach( value -> {
                    AcceleratorNode node = value.getValue();
                    if (node.getType().equals(hw.getElementClass())) {
                        Channel chan = node.findChannel(hw.getHandle());
                        if (chan!=null) {
                            chan.connectAndWait();
                            if (chan.isConnected()) {
                                addOneChannel(chan);
                            } else {
                                Logger.getLogger(PVTree.class.getName()).log(Level.WARNING, "Could not connect to {0}, not adding", chan.getId());
                            }
                        }
                    }
                    });
                });
        } else if (manualChannel.isConnected()) {
            addOneChannel(manualChannel);
        } else {
            Logger.getLogger(PVTree.class.getName()).log(Level.WARNING, "Nothing to add");
        }
        Logger.getLogger(PVTree.class.getName()).log(Level.INFO, "Added {0} channels.", FXMLController.pvScannablelist.size() + FXMLController.pvReadablelist.size() - count);

    }


    @FXML
    void elementClearSearchField(MouseEvent event) {
        elementSearch.clear();
        Logger.getLogger(PVTree.class.getName()).log(Level.FINER, "Search field cleared");
    }

    @FXML
    void elementSortChanged(ActionEvent event) {
        throw new UnsupportedOperationException("Changing sorting type not implemented yet)");
    }

    public PVTree() {
        FXMLLoader fxmlLoader = new FXMLLoader(

        getClass().getResource("/widgets/PVTree.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
           fxmlLoader.load();
        } catch (IOException exception) {
           throw new RuntimeException(exception);
        }
    }

    @FXML
    void checkManualPV(ActionEvent event) {
        // This function is called when button "Check" is clicked under the manual PV entry
        // the Add button should be enabled if the PV is found
        // Ideally we want an area where information about the button is added (units, current value, r or r/w etc)
        Logger.getLogger(PVTree.class.getName()).log(Level.FINER, "Trying to connect to channel {0}", manualTextField.getText());
        ChannelFactory cFact = ChannelFactory.defaultFactory();
        manualChannel = cFact.getChannel(manualTextField.getText());
        try {
            manualChannel.connectAndWait();
            if (manualChannel.isConnected()) {
                pvAddButton.setDisable(false);
                Logger.getLogger(PVTree.class.getName()).log(Level.FINER, "Connected to channel {0}", manualTextField.getText());
            } else {
                Logger.getLogger(PVTree.class.getName()).log(Level.WARNING, "Did not manage to connect to channel {0}", manualTextField.getText());
            }

        } catch (Exception exception) {
            Logger.getLogger(PVTree.class.getName()).log(Level.WARNING, "Exception raised when trying to connect to channel {0}", manualTextField.getText());
        }
    }

    void enteringManualPV(String oldText, String newText) {
        // This function is called when something is written in the text field for manual entry.
        // Any selection in the element tree should be deselected, and the "Add" button should be disabled
        if (newText.length()>0)
        {
            epicsTable.getSelectionModel().clearSelection();
            epicsChannels.clear();
            elementTree.getSelectionModel().clearSelection();
        }
        pvAddButton.setDisable(true);

    }

    private void addTypeMenuItem( String type ) {
        final CheckMenuItem menuItem = new CheckMenuItem(type);

        if (!Arrays.asList("marker").stream().anyMatch(str -> str.trim().equals(type)))
            menuItem.setSelected(true);
        else
            menuItem.setSelected(false);

        if (!Arrays.asList("marker", "sequence").stream().anyMatch(str -> str.trim().equals(type)))
            elementSearchMenuButton.getItems().add(menuItem);

        typeMap.put(type, menuItem);
    }

    /**
     * Check if the list of EPICS parameters already contain this typeHandle
     * @param typeHandle The handle to compare against
     * @return true if typeHandle is found in the table
     */
    private boolean epicsTableContains(String typeHandle) {
        return epicsTable.getItems().stream().anyMatch((item) -> (item.getTypeHandle().equals(typeHandle)));
    }

    /**
     * Adds the Handle to the list if it is not already there..
     *
     * @param hw
     */
    private void addHandleToList(HandleWrapper hw) {
        if (! epicsTableContains(hw.getTypeHandle())) {
            epicsChannels.add(hw);
        }
    }

    private void updateEPICSTable( TreeItem<AcceleratorNode> oldValue, TreeItem<AcceleratorNode> newValue) {


        if (oldValue == newValue)
            return;
        if ( newValue != null ) {
            // Clear manual text entry (we add EITHER manual entry OR selected entries)
            manualTextField.clear();

            Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "Epics table updating...");
            epicsChannels.clear();

            // First we initiate the updated list
            elementTree.getSelectionModel().getSelectedItems().forEach(value -> {
                AcceleratorNode node = value.getValue();
                node.getHandles()
                    .stream()
                    .sorted(( h1, h2 ) -> String.CASE_INSENSITIVE_ORDER.compare(h1, h2))
                    .forEach(h -> addHandleToList(new HandleWrapper(node, h)));
            });

            Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "Connecting handles...");
            // Then we make connections in parallell (otherwise it will be slow when we don't find PV's on the network)
            epicsChannels.parallelStream().forEach(h -> h.initConnection());

            Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "Epics table updated");
        }
    }

    private void updateTree( String searchPattern ) {

        Accelerator accelerator = Model.getInstance().getAccelerator();
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(accelerator.getRoot(), new ImageView(getClass().getResource("/icons/A.png").toExternalForm()));

        rootNode.setExpanded(true);
        populateTreeWithSequences(rootNode, accelerator, searchPattern);

        elementTree.setRoot(rootNode);

        Logger.getLogger(PVTree.class.getName()).log(Level.FINEST, "PV Tree widget updated");

    }

    /**
     * @param node          The node to be tested for search pattern and type.
     * @param searchPattern The search string.
     * @return {@code true} if the node ID contains the search string and its
     *         type is one of the admitted ones.
     */
    private boolean filterNode( AcceleratorNode node, String searchPattern ) {

        boolean idSearch = StringUtils.isEmpty(searchPattern) || StringUtils.contains(StringUtils.upperCase(node.getId()), StringUtils.upperCase(searchPattern));
        boolean typeSearch = typeMap.get(node.getType()).isSelected();

        return typeSearch && idSearch;

    }

    /**
     * Populate the {@code parent} node.
     *
     * @param parent        The node to be populated.
     * @param sequence      The accelerator sequence corresponding to the given
     *                      node to be populated.
     * @param searchPattern The search string. If not {@code null} and not empty,
     *                      will filter out nodes whose ID doesn't contain the
     *                      search string.
     */
    private void populateTreeWithSequences( TreeItem<AcceleratorNode> parent, AcceleratorSeq sequence, String searchPattern ) {

        final Comparator<? super AcceleratorNode> nComparator = elementSortAlphabetically.isSelected() ? NODE_COMPARATOR_ALPHABETICALLY : NODE_COMPARATOR_BY_POSITION;
        final Comparator<? super AcceleratorSeq> sComparator = elementSortAlphabetically.isSelected() ? SEQUENCE_COMPARATOR_ALPHABETICALLY : SEQUENCE_COMPARATOR_BY_POSITION;

        sequence.getSequences().stream().sorted(sComparator).forEach(s -> {
                TreeItem<AcceleratorNode> sequenceNode = new TreeItem<>(s, new ImageView(getClass().getResource("/icons/S.png").toExternalForm()));

                populateTreeWithSequences(sequenceNode, s, searchPattern);

                s.getNodes().stream().sorted(nComparator).filter(n -> filterNode(n, searchPattern)).forEach(n -> {
                    sequenceNode.getChildren().add(new TreeItem<>(n, new ImageView(getClass().getResource("/icons/N.png").toExternalForm())));
                    sequenceNode.setExpanded(!StringUtils.isEmpty(searchPattern));
                });

                if ( !sequenceNode.isLeaf() || filterNode(s, searchPattern) ) {

                        parent.getChildren().add(sequenceNode);

                    if ( sequenceNode.isExpanded() ) {
                        parent.setExpanded(true);
                    }

                }
        });
    }
}
