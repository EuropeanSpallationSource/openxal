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
package xal.app.modelbrowser;


import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ChannelSuite;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss;


public class FXMLController implements Initializable {

    private static final String CELL_DEFAULT_STYLE = "-fx-font-style:normal; -fx-font-weight:normal;";
    private static final String CELL_EVEN_INVALID_STYLE = "-fx-text-fill: white; -fx-background-color: derive(magenta, 25%); -fx-font-style:italic; -fx-font-weight:normal;";
    private static final String CELL_EVEN_MAJOR_STYLE = "-fx-text-fill: white; -fx-background-color: derive(red, 25%); -fx-font-style:normal; -fx-font-weight:bold;";
    private static final String CELL_EVEN_MINOR_STYLE = "-fx-text-fill: white; -fx-background-color: derive(orange, 25%); -fx-font-style:normal; -fx-font-weight:normal;";
    private static final String CELL_ODD_INVALID_STYLE = "-fx-text-fill: white; -fx-background-color: derive(magenta, 40%); -fx-font-style:italic; -fx-font-weight:normal;";
    private static final String CELL_ODD_MAJOR_STYLE = "-fx-text-fill: white; -fx-background-color: derive(red, 40%); -fx-font-style:normal; -fx-font-weight:bold;";
    private static final String CELL_ODD_MINOR_STYLE = "-fx-text-fill: white; -fx-background-color: derive(orange, 40%); -fx-font-style:normal; -fx-font-weight:normal;";
    private static final Logger LOGGER = Logger.getLogger(FXMLController.class.getName());
    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_ALPHABETICALLY = (n1, n2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(n1.getId()), StringUtils.defaultString(n2.getId()));
    private static final Comparator<? super AcceleratorNode> NODE_COMPARATOR_BY_POSITION = (n1, n2) -> (int) ( n1.getPosition() - n2.getPosition() );
    private static final String NO_SELECTION = "<no-selection>";
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_ALPHABETICALLY = (s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(StringUtils.defaultString(s1.getId()), StringUtils.defaultString(s2.getId()));
    private static final Comparator<? super AcceleratorSeq> SEQUENCE_COMPARATOR_BY_POSITION = (s1, s2) -> (int) ( s1.getPosition() - s2.getPosition() );

    private final ObservableList<AttributeWrapper> attributes = FXCollections.observableArrayList();
    @FXML private TableColumn<AttributeWrapper, String> attributesNameColumn;
    @FXML private TableView<AttributeWrapper> attributesTable;
    @FXML private TitledPane attributesTitledPane;
    @FXML private TableColumn<AttributeWrapper, String> attributesValueColumn;
    private final ObservableList<ChannelWrapper> channels = FXCollections.observableArrayList();
    @FXML private LineChart<Double, Double> chart;
    @FXML private TableColumn<ChannelWrapper, String> epicsNameColumn;
    @FXML private TableView<ChannelWrapper> epicsTable;
    @FXML private TitledPane epicsTitledPane;
    @FXML private TableColumn<ChannelWrapper, String> epicsValueColumn;
    @FXML private Accordion inspectorAccordion;
    @FXML private TextField inspectorSearchField;
    @FXML private StackPane inspectorSearchIcon;
    @FXML private CheckMenuItem inspectorShowTableHeaders;
    @FXML private TextField modelSearchField;
    @FXML private StackPane modelSearchIcon;
    @FXML private MenuButton modelSearchMenuButton;
    @FXML private RadioMenuItem modelSortAlphabetically;
    @FXML private TreeView<AcceleratorNode> modelTree;
    @FXML private TableColumn<?, ?> probeNameColumn;
    private final ObservableList<PropertyWrapper> probeProperties = FXCollections.observableArrayList();
    @FXML private TableView<PropertyWrapper> probeTable;
    @FXML private TitledPane probeTitledPane;
    @FXML private TableColumn<?, ?> probeValueColumn;
    private final ObservableList<PropertyWrapper> properties = FXCollections.observableArrayList();
    @FXML private TableColumn<PropertyWrapper, String> propertiesNameColumn;
    @FXML private TableView<PropertyWrapper> propertiesTable;
    @FXML private TitledPane propertiesTitledPane;
    @FXML private TableColumn<PropertyWrapper, String> propertiesValueColumn;
    @FXML private ResourceBundle resources;
    @FXML private ProgressIndicator runProgress;
    @FXML private Button runSimulationButton;
    @FXML private ContextMenu runSimulationMenu;
    private final XYChart.Series<Double, Double> sigmaXSeries = new XYChart.Series<>("x", FXCollections.observableArrayList());
    private final XYChart.Series<Double, Double> sigmaYSeries = new XYChart.Series<>("y", FXCollections.observableArrayList());
    private final XYChart.Series<Double, Double> sigmaZSeries = new XYChart.Series<>("z", FXCollections.observableArrayList());
    private final RunSimulationService simulationWorker = new RunSimulationService(this);
    private final Map<String, CheckMenuItem> typeMap = new TreeMap<>();

    public LineChart<Double, Double> getChart() {
        return chart;
    }

    public TreeView<AcceleratorNode> getModelTree() {
        return modelTree;
    }

    public XYChart.Series<Double, Double> getSigmaXSeries() {
        return sigmaXSeries;
    }

    public XYChart.Series<Double, Double> getSigmaYSeries() {
        return sigmaYSeries;
    }

    public XYChart.Series<Double, Double> getSigmaZSeries() {
        return sigmaZSeries;
    }

    @Override
    public void initialize( URL url, ResourceBundle rb ) {

        runProgress.visibleProperty().bind(simulationWorker.runningProperty());
        runProgress.progressProperty().bind(simulationWorker.progressProperty());
        simulationWorker.runningProperty().addListener((observable, oldValue, newValue) -> updateRunButton(modelTree.getSelectionModel().getSelectedItem()));

        ObservableList<XYChart.Series<Double, Double>> chartData = FXCollections.observableArrayList();

        chartData.add(sigmaXSeries);
        chartData.add(sigmaYSeries);
        chartData.add(sigmaZSeries);
        
        chart.setData(chartData);
        chart.setCreateSymbols(false);

        if ( modelSearchField.getLength() == 0 ) {
            modelSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
        } else {
            modelSearchIcon.getStyleClass().add("search-clear"); //NOI18N
        }

        modelSearchField.textProperty().addListener(( observable, oldValue, newValue ) -> {

            if ( newValue.isEmpty() ) {
                modelSearchIcon.getStyleClass().clear();
                modelSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
            } else {
                modelSearchIcon.getStyleClass().clear();
                modelSearchIcon.getStyleClass().add("search-clear"); //NOI18N
            }

            updateTree(newValue);

        });

        Model.getInstance().getAccelerator().getRoot().getAllNodes().stream().map(n -> n.getType()).distinct().sorted().forEachOrdered(t -> addTypeMenuItem(t));
        Model.getInstance().getAccelerator().getComboSequences().stream().map(n -> n.getType()).distinct().sorted().forEachOrdered(t -> addTypeMenuItem(t));

        modelTree.setCellFactory(p -> new NodeTreeCell());
        modelTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateTables(newValue, inspectorSearchField.getText()));
        modelTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateRunButton(newValue));

        updateTree(modelSearchField.getText());

        if ( inspectorSearchField.getLength() == 0 ) {
            inspectorSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
        } else {
            inspectorSearchIcon.getStyleClass().add("search-clear"); //NOI18N
        }

        inspectorSearchField.textProperty().addListener(( observable, oldValue, newValue ) -> {

            if ( newValue.isEmpty() ) {
                inspectorSearchIcon.getStyleClass().clear();
                inspectorSearchIcon.getStyleClass().add("search-magnifying-glass"); //NOI18N
            } else {
                inspectorSearchIcon.getStyleClass().clear();
                inspectorSearchIcon.getStyleClass().add("search-clear"); //NOI18N
            }

            updateTables(modelTree.getSelectionModel().getSelectedItem(), newValue);

        });

        inspectorAccordion.setExpandedPane(inspectorAccordion.getPanes().get(0));

        attributesTable.itemsProperty().setValue(attributes);
        attributesNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        attributesValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        attributesTable.getColumns().forEach(this::decorateTableCells);

        epicsTable.itemsProperty().setValue(channels);
        epicsNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        epicsValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        epicsTable.getColumns().forEach(this::decorateTableCells);

        propertiesTable.itemsProperty().setValue(properties);
        propertiesNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        propertiesValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        propertiesTable.getColumns().forEach(this::decorateTableCells);

        probeTable.itemsProperty().setValue(probeProperties);
        probeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        probeValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        probeTable.getColumns().forEach(this::decorateTableCells);

        updateTableHeaders(null);

    }

    private void addTypeMenuItem( String type ) {

        final CheckMenuItem menuItem = new CheckMenuItem(type);

        menuItem.setSelected(true);
        menuItem.selectedProperty().addListener(( observable, oldValue, newValue ) -> {
            updateTree(modelSearchField.getText());
        });

        typeMap.put(type, menuItem);
        modelSearchMenuButton.getItems().add(menuItem);

    }

    @FXML
    private void contextMenuRequested( ContextMenuEvent event ) {

    }

    private <T extends BaseWrapper, C> void decorateTableCells( TableColumn<T, C> column ) {

        Callback<TableColumn<T, C>, TableCell<T, C>> existingCellFactory = column.getCellFactory();

        column.setCellFactory(( TableColumn<T, C> c ) -> {

            boolean isValueColumn = StringUtils.endsWith(column.getId(), "ValueColumn");
            Tooltip tooltip = new Tooltip();
            TableCell<T, C> cell = new TableCell<T, C>() {
                @Override
                protected void updateItem( C item, boolean empty ) {

                    super.updateItem(item, empty);

                    if ( item == null ) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if ( item instanceof Node ) {
                        super.setText(null);
                        super.setGraphic((Node) item);
                    } else {
                        super.setText(item.toString());
                        super.setGraphic(null);
                    }

                    setStyle(CELL_DEFAULT_STYLE);
                    getTooltip().setText(getText());

                    if ( isValueColumn ) {

                        int index = getIndex();

                        if ( index >= 0 ) {

                            ObservableList<T> items = getTableView().getItems();

                            if ( index < items.size() ) {

                                boolean isEven = ( index % 2 ) == 0;
                                T wrapper = items.get(index);

                                getTooltip().setText(wrapper.getTooltip());

                                switch ( wrapper.getAlarm() ) {
                                    case INVALID:
                                        setStyle(isEven ? CELL_EVEN_INVALID_STYLE : CELL_ODD_INVALID_STYLE);
                                        break;
                                    case MAJOR:
                                        setStyle(isEven ? CELL_EVEN_MAJOR_STYLE : CELL_ODD_MAJOR_STYLE);
                                        break;
                                    case MINOR:
                                        setStyle(isEven ? CELL_EVEN_MINOR_STYLE : CELL_ODD_MINOR_STYLE);
                                        break;
                                }

                            }

                        }

                    }

                }
            };

            cell.setTooltip(tooltip);

            return cell;

        });

    }

    /**
     * @param name          The name to be tested for search pattern.
     * @param searchPattern The search string.
     * @return {@code true} if the name contains the search string.
     */
    private boolean filterName( String name, String searchPattern ) {
        return StringUtils.isEmpty(searchPattern) || StringUtils.contains(StringUtils.upperCase(name), StringUtils.upperCase(searchPattern));
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

    @FXML
    private void inspectorClearSearchField( MouseEvent event ) {
        inspectorSearchField.clear();
    }

    @FXML
    private void inspectorDetectClearSearchField( KeyEvent event ) {
        if ( event.getCode() == KeyCode.ESCAPE ) {
            inspectorSearchField.clear();
        }
    }

    private boolean isInvaludNodeForRun ( TreeItem<AcceleratorNode> item ) {
        return ( item == null
            || item.getValue() == null
            || !( item.getValue() instanceof AcceleratorSeq )
            || item.getParent() != modelTree.getRoot()
            || simulationWorker.isRunning() );
    }

    @FXML
    private void modelClearSearchField( MouseEvent event ) {
        modelSearchField.clear();
    }

    @FXML
    private void modelDetectClearSearchField( KeyEvent event ) {
        if ( event.getCode() == KeyCode.ESCAPE ) {
            modelSearchField.clear();
        }
    }

    @FXML
    private void modelSelectAllTypes( ActionEvent event ) {
        typeMap.values().stream().forEach(m -> m.setSelected(true));
    }

    @FXML
    private void modelSortChanged( ActionEvent event ) {
        updateTree(modelSearchField.getText());
    }

    @FXML
    private void modelUnselectAllTypes( ActionEvent event ) {
        typeMap.values().stream().forEach(m -> m.setSelected(false));
    }

    /**
     * Populate the {@code parent} node.
     *
     * @param parent        The node to be populated.
     * @param accelerator      The accelerator sequence corresponding to the given
     *                      node to be populated.
     * @param searchPattern The search string. If not {@code null} and not empty,
     *                      will filter out nodes whose ID doesn't contain the
     *                      search string.
     */
    private void populateTreeWithComboSequences( TreeItem<AcceleratorNode> parent, Accelerator accelerator, String searchPattern ) {

        final Comparator<? super AcceleratorSeq> sComparator = modelSortAlphabetically.isSelected() ? SEQUENCE_COMPARATOR_ALPHABETICALLY : SEQUENCE_COMPARATOR_BY_POSITION;

        accelerator.getComboSequences().stream().sorted(sComparator).forEach(s -> {

            TreeItem<AcceleratorNode> sequenceNode = new TreeItem<>(s, new ImageView(getClass().getResource("/icons/C.png").toExternalForm()));

            if ( !sequenceNode.isLeaf() || filterNode(s, searchPattern) ) {

                parent.getChildren().add(sequenceNode);

                if ( sequenceNode.isExpanded() ) {
                    parent.setExpanded(true);
                }

            }

        });

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

        final Comparator<? super AcceleratorNode> nComparator = modelSortAlphabetically.isSelected() ? NODE_COMPARATOR_ALPHABETICALLY : NODE_COMPARATOR_BY_POSITION;
        final Comparator<? super AcceleratorSeq> sComparator = modelSortAlphabetically.isSelected() ? SEQUENCE_COMPARATOR_ALPHABETICALLY : SEQUENCE_COMPARATOR_BY_POSITION;

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

    @FXML
    private void reloadSelectedNode( ActionEvent event ) {

        TreeItem<AcceleratorNode> selectedNode = modelTree.getSelectionModel().getSelectedItem();
        String searchPattern = inspectorSearchField.getText();

        updateAttributesTable(selectedNode, searchPattern);
        //  EPICS values are updated automatically thank to change listeners.
        //updateEPICSTable(selectedNode, searchPattern);
        updateIntrospectionTable(selectedNode, searchPattern);
        updateProbeTable(isInvaludNodeForRun(selectedNode) ? null : selectedNode);

    }

    private void runSimulation( String mode ) {

        runSimulationButton.setDisable(true);

        sigmaXSeries.setData(FXCollections.observableArrayList());
        sigmaYSeries.setData(FXCollections.observableArrayList());
        sigmaZSeries.setData(FXCollections.observableArrayList());

        simulationWorker.setSynchronizationMode(mode);
        simulationWorker.reset();
        simulationWorker.start();

    }

    @FXML
    private void runSimulation( ActionEvent event ) {
        runSimulationMenu.show(runSimulationButton, Side.BOTTOM, 0, 0);
    }

    @FXML
    private void runSimulationDesign( ActionEvent event ) {
        runSimulation(Scenario.SYNC_MODE_DESIGN);
    }

    @FXML
    private void runSimulationLive( ActionEvent event ) {
        runSimulation(Scenario.SYNC_MODE_LIVE);
    }

    private void updateAttributesTable( TreeItem<AcceleratorNode> item, String searchPattern ) {

        attributes.forEach(a -> a.dispose());
        attributes.clear();

        if ( item != null ) {

            AcceleratorNode node = item.getValue();
            ImageView imageView = new ImageView(((ImageView) item.getGraphic()).getImage());

            imageView.setScaleX(.7);
            imageView.setScaleY(.7);

            ((Labeled) attributesTitledPane.getGraphic()).setText(node.getId());
            ((Labeled) attributesTitledPane.getGraphic()).setGraphic(imageView);

            node.getBuckets()
                .stream()
                .sorted(( b1, b2 ) -> String.CASE_INSENSITIVE_ORDER.compare(b1.getType(), b2.getType()))
                .forEach(b -> {
                    Arrays.asList(b.getAttrNames()).stream()
                        .filter(n -> filterName(MessageFormat.format("{0}: {1}", b.getType(), n), searchPattern))
                        .sorted(( n1, n2 ) -> String.CASE_INSENSITIVE_ORDER.compare(n1, n2))
                        .forEach(n -> attributes.add(new AttributeWrapper(b.getAttr(n), MessageFormat.format("{0}: {1}", b.getType(), n))));
                });

        } else {
            ((Labeled) attributesTitledPane.getGraphic()).setText(NO_SELECTION);
            ((Labeled) attributesTitledPane.getGraphic()).setGraphic(null);
        }

    }

    private void updateEPICSTable( TreeItem<AcceleratorNode> item, String searchPattern ) {

        channels.forEach(c -> c.dispose());
        channels.clear();

        if ( item != null ) {

            AcceleratorNode node = item.getValue();
            ImageView imageView = new ImageView(((ImageView) item.getGraphic()).getImage());

            imageView.setScaleX(.7);
            imageView.setScaleY(.7);

            ((Labeled) epicsTitledPane.getGraphic()).setText(node.getId());
            ((Labeled) epicsTitledPane.getGraphic()).setGraphic(imageView);

            ChannelSuite channelSuite = node.channelSuite();

            channelSuite.getHandles()
                .stream()
                .filter(h -> filterName(MessageFormat.format("{0}: {1}", h, channelSuite.getSignal(h)), searchPattern))
                .sorted(( h1, h2 ) -> String.CASE_INSENSITIVE_ORDER.compare(h1, h2))
                .forEach(h -> channels.add(new ChannelWrapper(channelSuite.getChannel(h), MessageFormat.format("{0}: {1}", h, channelSuite.getSignal(h)))));

        } else {
            ((Labeled) epicsTitledPane.getGraphic()).setText(NO_SELECTION);
            ((Labeled) epicsTitledPane.getGraphic()).setGraphic(null);
        }

    }

    private void updateIntrospectionTable( TreeItem<AcceleratorNode> item, String searchPattern ) {

        properties.forEach(p -> p.dispose());
        properties.clear();

        if ( item != null ) {

            AcceleratorNode node = item.getValue();
            ImageView imageView = new ImageView(((ImageView) item.getGraphic()).getImage());

            imageView.setScaleX(.7);
            imageView.setScaleY(.7);

            ((Labeled) propertiesTitledPane.getGraphic()).setText(node.getId());
            ((Labeled) propertiesTitledPane.getGraphic()).setGraphic(imageView);

            try {

                BeanInfo beanInfo = Introspector.getBeanInfo(node.getClass());

                Arrays.asList(beanInfo.getPropertyDescriptors())
                    .stream()
                    .filter(p -> {

                        Class<?> type = ( p instanceof IndexedPropertyDescriptor ) ? ((IndexedPropertyDescriptor) p).getIndexedPropertyType() : p.getPropertyType();

                        if ( !ClassUtils.isPrimitiveOrWrapper(type) && !String.class.isAssignableFrom(type) ) {
                            return false;
                        }

                        try {
                            return p.getReadMethod() != null;
                        } catch ( Exception ex ) {
                            return false;
                        }

                    })
                    .filter(p -> filterName(p.getDisplayName(), searchPattern))
                    .sorted(( p1, p2 ) -> String.CASE_INSENSITIVE_ORDER.compare(p1.getDisplayName(), p2.getDisplayName()))
                    .forEach(p -> { 
                        try {
                            properties.add(new PropertyWrapper(node, p.getDisplayName()));
                        } catch ( NoSuchMethodException ex ) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    });

            } catch ( IntrospectionException ex ) {
                LOGGER.log(Level.WARNING, null, ex);
            }

        } else {
            ((Labeled) propertiesTitledPane.getGraphic()).setText(NO_SELECTION);
            ((Labeled) propertiesTitledPane.getGraphic()).setGraphic(null);
        }

    }

    private void updateProbeTable( TreeItem<AcceleratorNode> item ) {

        probeProperties.forEach(p -> p.dispose());
        probeProperties.clear();

        if ( item != null ) {

            AcceleratorSeq sequence = (AcceleratorSeq) item.getValue();
            ImageView imageView = new ImageView(((ImageView) item.getGraphic()).getImage());

            imageView.setScaleX(.7);
            imageView.setScaleY(.7);

            ((Labeled) probeTitledPane.getGraphic()).setText(sequence.getId());
            ((Labeled) probeTitledPane.getGraphic()).setGraphic(imageView);

            try {

                EnvTrackerAdapt envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);

                envelopeTracker.setMaxIterations(1000);
                envelopeTracker.setAccuracyOrder(1);
                envelopeTracker.setErrorTolerance(0.001);

                EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);

                probeProperties.add(new PropertyWrapper(probe, "beamCurrent"));
                probeProperties.add(new PropertyWrapper(probe, "beamPerveance", "beamPerveance"));
                probeProperties.add(new PropertyWrapper(probe, "beta"));
                probeProperties.add(new PropertyWrapper(probe, "bunchCharge", "bunchCharge"));
                probeProperties.add(new PropertyWrapper(probe, "bunchFrequency"));
                probeProperties.add(new PropertyWrapper(probe, "gamma"));
                probeProperties.add(new PropertyWrapper(probe, "kineticEnergy"));

                CovarianceMatrix covariance = probe.getCovariance();
                Twiss[] twisses = covariance.computeTwiss();

                for ( int i = 0; i < twisses.length; i++ ) {
                    probeProperties.add(new PropertyWrapper(MessageFormat.format("twiss {0,number,###0}: ", i), twisses[i], "alpha"));
                    probeProperties.add(new PropertyWrapper(MessageFormat.format("twiss {0,number,###0}: ", i), twisses[i], "beta"));
                    probeProperties.add(new PropertyWrapper(MessageFormat.format("twiss {0,number,###0}: ", i), twisses[i], "envelopeRadius"));
                    probeProperties.add(new PropertyWrapper(MessageFormat.format("twiss {0,number,###0}: ", i), twisses[i], "envelopeSlope"));
                    probeProperties.add(new PropertyWrapper(MessageFormat.format("twiss {0,number,###0}: ", i), twisses[i], "gamma"));
                }

            } catch ( InstantiationException | NoSuchMethodException ex ) {
                LOGGER.log(Level.WARNING, null, ex);
            }

        } else {
            ((Labeled) probeTitledPane.getGraphic()).setText(NO_SELECTION);
            ((Labeled) probeTitledPane.getGraphic()).setGraphic(null);
        }

    }

    private void updateRunButton( TreeItem<AcceleratorNode> item ) {

        boolean invalidNode = isInvaludNodeForRun(item);

        runSimulationButton.setDisable(invalidNode);
        updateProbeTable(invalidNode ? null : item);

    }

    @FXML
    private void updateTableHeaders( ActionEvent event ) {
        if ( inspectorShowTableHeaders.isSelected() ) {
            attributesTable.getStyleClass().remove("noheader");
            epicsTable.getStyleClass().remove("noheader");
            propertiesTable.getStyleClass().remove("noheader");
            probeTable.getStyleClass().remove("noheader");
        } else {
            attributesTable.getStyleClass().add("noheader");
            epicsTable.getStyleClass().add("noheader");
            propertiesTable.getStyleClass().add("noheader");
            probeTable.getStyleClass().add("noheader");
        }
    }

    private void updateTables( TreeItem<AcceleratorNode> newValue, String searchPattern ) {
        updateAttributesTable(newValue, searchPattern);
        updateEPICSTable(newValue, searchPattern);
        updateIntrospectionTable(newValue, searchPattern);
    }

    private void updateTree( String searchPattern ) {

        Accelerator accelerator = Model.getInstance().getAccelerator();
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(accelerator.getRoot(), new ImageView(getClass().getResource("/icons/A.png").toExternalForm()));

        rootNode.setExpanded(true);
        populateTreeWithSequences(rootNode, accelerator, searchPattern);
        populateTreeWithComboSequences(rootNode, accelerator, searchPattern);

        modelTree.setRoot(rootNode);

    }

}
