/*
 * PopUPPlotController.java
 *
 * Created by Natalia Milas on 07.07.2017
 *
 * Copyright (C) 2018 European Spallation Source ERIC
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
package xal.app.trajectorycorrection;

/**
 * Secondary Controller that holds a plot that displays an orbit
 *
 * @author nataliamilas 06-2017
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import xal.smf.impl.BPM;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class PopUpPlotController {

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    //Setup the Plot
    final XYSeriesCollection Data = new XYSeriesCollection();
    XYSeries horizontalSeries = new XYSeries("horizontal");
    XYSeries verticalSeries = new XYSeries("vertical");

    @FXML
    private Button buttonClose;
    @FXML
    private AnchorPane panePlot;

    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    }

    @FXML
    public void handleButtonClose() {
        setLoggedIn(true);

    }

    public void updatePlot(TrajectoryArray Traj) {
        double valMaxY = 0;
        double valMinY = 0;
        double valMaxX = 0;
        double valMinX = 1000;

        //clear the initial values
        horizontalSeries.clear();
        verticalSeries.clear();

        for (BPM item : Traj.Pos.keySet()) {
            horizontalSeries.add(Traj.Pos.get(item), Traj.XRef.get(item));
            valMaxY = max(valMaxY, Traj.XRef.get(item));
            valMinY = min(valMinY, Traj.XRef.get(item));
            verticalSeries.add(Traj.Pos.get(item), Traj.YRef.get(item));
            valMaxY = max(valMaxY, Traj.YRef.get(item));
            valMinY = min(valMinY, Traj.YRef.get(item));
            valMaxX = max(valMaxX, Traj.Pos.get(item));
            valMinX = min(valMinX, Traj.Pos.get(item));
        }

        Data.addSeries(horizontalSeries);
        Data.addSeries(verticalSeries);

        JFreeChart xyPlot = ChartFactory.createXYLineChart(null, "Position (m)", "Ref. Trajectory (mm)", Data);
        xyPlot.getLegend().setVisible(true);
        xyPlot.setBackgroundPaint(java.awt.Color.decode("#f4f4f4"));
        xyPlot.getXYPlot().getDomainAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getDomainAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        xyPlot.getXYPlot().getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        xyPlot.getLegend().setBackgroundPaint(java.awt.Color.decode("#f4f4f4"));

        ChartViewer viewerPlot = new ChartViewer(xyPlot, true);
        panePlot.getChildren().add(viewerPlot);
        viewerPlot.setPrefSize(591, 352);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        final XYPlot plot = xyPlot.getXYPlot();
        plot.setRenderer(renderer);
        plot.getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        plot.setBackgroundPaint(Color.WHITE);
        BasicStroke gridstroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{1, 2}, 10);
        plot.setDomainGridlineStroke(gridstroke);
        plot.setRangeGridlineStroke(gridstroke);

        //BPM annotation
        XYTextAnnotation annotation = new XYTextAnnotation("", 0.0, 0.0);
        plot.addAnnotation(annotation);

        ChartCanvas canvasPlot = viewerPlot.getCanvas();

        canvasPlot.addChartMouseListener(new ChartMouseListenerFX() {
            //viewerCombinedPlot.addChartMouseListener(new ChartMouseListenerFX(){
            @Override
            public void chartMouseClicked(ChartMouseEventFX cmefx) {
                Rectangle2D dataArea = canvasPlot.getRenderingInfo().getPlotInfo().getDataArea();
                double plotXvalue = plot.getDomainAxis().java2DToValue(cmefx.getTrigger().getX(), dataArea, plot.getDomainAxisEdge());

                //crosshair markers visible
                plot.setDomainCrosshairVisible(true);
                plot.setRangeCrosshairVisible(false);
                plot.setDomainCrosshairPaint(Color.BLACK);
                plot.setDomainCrosshairStroke(new BasicStroke(1.0f));

                //Find the closest data point
                List<BPM> BPMList = new ArrayList<>();
                Traj.Pos.keySet().forEach(bpm -> BPMList.add(bpm));

                double dist = Math.abs(plotXvalue - (BPMList.get(0).getSDisplay()));
                BPM closestBPM = BPMList.get(0);
                int index = 0;

                for (BPM bpm : BPMList) {
                    if (dist > Math.abs(plotXvalue - (bpm.getSDisplay()))) {
                        dist = Math.abs(plotXvalue - (bpm.getSDisplay()));
                        closestBPM = bpm;
                        index++;
                    }
                }
                plot.setDomainCrosshairValue(closestBPM.getSDisplay());
                double annotationX = 0.9 * plot.getDomainAxis().getUpperBound();
                double annotationY = 0.9 * plot.getRangeAxis().getUpperBound();
                annotation.setText(closestBPM.toString());
                annotation.setX(annotationX);
                annotation.setY(annotationY);
                annotation.setOutlineVisible(false);
                annotation.setFont(new Font("System", Font.BOLD, 12));
                annotation.setPaint(Color.black);
                annotation.setBackgroundPaint(Color.WHITE);

            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX cmefx) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        });

        final MenuItem plotProperties = new MenuItem("Plot Properties");
        viewerPlot.getContextMenu().getItems().add(new SeparatorMenuItem());
        viewerPlot.getContextMenu().getItems().add(plotProperties);

        plotProperties.setOnAction((ActionEvent event1) -> {
            Stage stage;
            Parent root;
            URL url = null;
            String sceneFile = "/fxml/PlotProperties.fxml";
            try {
                stage = new Stage();
                url = getClass().getResource(sceneFile);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getResource(sceneFile));
                root = loader.load();
                root.getStylesheets().add("/styles/Styles.css");
                stage.setScene(new Scene(root));
                stage.setTitle("Plot Properties");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(buttonClose.getScene().getWindow());
                PlotPropertiesController loginController = loader.getController();
                loginController.setPlotProperties(xyPlot.getXYPlot().getDomainAxis().getUpperBound(), xyPlot.getXYPlot().getDomainAxis().getLowerBound(), xyPlot.getXYPlot().getDomainAxis().isAutoRange(), xyPlot.getXYPlot().getRangeAxis().getUpperBound(), xyPlot.getXYPlot().getRangeAxis().getLowerBound(), xyPlot.getXYPlot().getRangeAxis().isAutoRange());
                loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                    if (isNowLoggedIn) {
                        xyPlot.getXYPlot().getDomainAxis().setUpperBound(loginController.getxMax());
                        xyPlot.getXYPlot().getDomainAxis().setLowerBound(loginController.getxMin());
                        xyPlot.getXYPlot().getDomainAxis().setAutoRange(loginController.getxAutoscale());
                        xyPlot.getXYPlot().getRangeAxis().setUpperBound(loginController.getyMax());
                        xyPlot.getXYPlot().getRangeAxis().setLowerBound(loginController.getyMin());
                        xyPlot.getXYPlot().getRangeAxis().setAutoRange(loginController.getyAutoscale());
                        if (xyPlot.getXYPlot().getDomainCrosshairValue() < xyPlot.getXYPlot().getDomainAxis().getUpperBound() && xyPlot.getXYPlot().getDomainCrosshairValue() > xyPlot.getXYPlot().getDomainAxis().getLowerBound()) {
                            annotation.setX(0.9 * xyPlot.getXYPlot().getDomainAxis().getUpperBound());
                            annotation.setY(0.9 * xyPlot.getXYPlot().getRangeAxis().getUpperBound());
                        } else {
                            annotation.setText(" ");
                        }
                        stage.close();
                    }
                });
                stage.showAndWait();
            } catch (IOException ex) {
                System.out.println("Exception on FXMLLoader.load()");
                System.out.println("  * url: " + url);
                System.out.println("  * " + ex);
                System.out.println("    ----------------------------------------\n");
            }
        });

    }

}
