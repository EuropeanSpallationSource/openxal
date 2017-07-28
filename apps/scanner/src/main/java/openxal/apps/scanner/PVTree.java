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

package openxal.apps.scanner;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
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
import org.apache.commons.lang3.StringUtils;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ChannelSuite;

public class PVTree extends SplitPane {

    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_ALPHABETICALLY = (n1, n2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(n1.getId()), StringUtils.defaultString(n2.getId()));
    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_BY_POSITION = (n1, n2) -> (int) ( n1.getPosition() - n2.getPosition() );
    private static final String NO_SELECTION = "<no-selection>";
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_ALPHABETICALLY = (s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(s1.getId()), StringUtils.defaultString(s2.getId()));
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_BY_POSITION = (s1, s2) -> (int) ( s1.getPosition() - s2.getPosition() );


    private final Map<String, CheckMenuItem> typeMap = new TreeMap<>();
    private final ObservableList<ChannelWrapper> epicsChannels = FXCollections.observableArrayList();

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="elementSearch"
    private TextField elementSearch; // Value injected by FXMLLoader

    @FXML // fx:id="elementSearchMenuButton"
    private MenuButton elementSearchMenuButton; // Value injected by FXMLLoader

    @FXML // fx:id="elementSortAlphabetically"
    private RadioMenuItem elementSortAlphabetically; // Value injected by FXMLLoader

    @FXML // fx:id="elementTree"
    private TreeView<AcceleratorNode> elementTree; // Value injected by FXMLLoader

    @FXML // fx:id="pvTree"
    private TreeView<?> pvTree; // Value injected by FXMLLoader

    @FXML // fx:id="epicsTable"
    private TableView<ChannelWrapper> epicsTable; // Value injected by FXMLLoader

    @FXML // fx:id="epicsNameColumn"
    private TableColumn<ChannelWrapper, String> epicsNameColumn; // Value injected by FXMLLoader

    @FXML // fx:id="epicsTypeColumn"
    private TableColumn<ChannelWrapper, String> epicsTypeColumn; // Value injected by FXMLLoader

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
        epicsNameColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        epicsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        epicsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // TODO: the nodes which have getStatus() false should be grey in the list (to mark them as "problematic")
        //Model.getInstance().getAccelerator().getRoot().getSequence("MEBT").getAllNodes().stream().forEach(n -> System.out.println("Node " + n.getId() + " is valid? "+ n.getStatus()));

        updateTree(elementSearch.getText());
    }

    @FXML
    void addSelectedPV(ActionEvent event) {
        epicsTable.getSelectionModel().getSelectedItems().forEach((ChannelWrapper c) -> {
            System.out.println("Adding PV "+c.idProperty().getValue());
            FXMLController.PVlist.add(c);
            });

    }

    @FXML
    void elementSortChanged(ActionEvent event) {
        System.out.println("DBG Changing sorting type (not implemented yet!)");
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

    private void updateEPICSTable( TreeItem<AcceleratorNode> oldValue, TreeItem<AcceleratorNode> newValue) {

        if (oldValue == newValue)
            return;

        if ( newValue != null ) {
            AcceleratorNode node = newValue.getValue();

            ChannelSuite channelSuite = node.channelSuite();

            epicsChannels.clear();

            channelSuite.getHandles()
                .stream()
                .sorted(( h1, h2 ) -> String.CASE_INSENSITIVE_ORDER.compare(h1, h2))
                .forEach(h -> epicsChannels.add(new ChannelWrapper(channelSuite.getChannel(h))));
        }
    }

    private void updateTree( String searchPattern ) {

        Accelerator accelerator = Model.getInstance().getAccelerator();
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(accelerator.getRoot(), new ImageView(getClass().getResource("/icons/A.png").toExternalForm()));

        rootNode.setExpanded(true);
        populateTreeWithSequences(rootNode, accelerator, searchPattern);

        elementTree.setRoot(rootNode);

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
