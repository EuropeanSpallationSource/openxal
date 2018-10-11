package xal.app.scanner;

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

import com.sun.javafx.charts.Legend;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.gillius.jfxutils.chart.ChartZoomManager;
import xal.ca.Channel;

/**
 * FXML Controller class
 *
 * @author yngvelevinsen
 */
public class Plot extends SplitPane implements Initializable {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="analyseGraphPane"
    private StackPane analyseGraphPane; // Value injected by FXMLLoader

    @FXML // fx:id="pvReadbacksGraph"
    private ScannerLineChart<Number, Number> pvReadbacksGraph; // Value injected by FXMLLoader

    @FXML // fx:id="selectRect"
    private Rectangle selectRect; // Value injected by FXMLLoader

    @FXML // fx:id="pvWriteablesGraph"
    private ScannerLineChart<Number, Number> pvWriteablesGraph; // Value injected by FXMLLoader

    private static ChartZoomManager zoomManager;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert analyseGraphPane != null : "fx:id=\"analyseGraphPane\" was not injected: check your FXML file 'Plot.fxml'.";
        assert pvReadbacksGraph != null : "fx:id=\"pvReadbacksGraph\" was not injected: check your FXML file 'Plot.fxml'.";
        assert selectRect != null : "fx:id=\"selectRect\" was not injected: check your FXML file 'Plot.fxml'.";
        assert pvWriteablesGraph != null : "fx:id=\"pvWriteablesGraph\" was not injected: check your FXML file 'Plot.fxml'.";

        // Allow zooming in the chart..
        zoomManager = new ChartZoomManager( analyseGraphPane, selectRect, pvReadbacksGraph );
        zoomManager.setMouseWheelZoomAllowed(true);
        zoomManager.setZoomDurationMillis(100);
        zoomManager.start();
    }

    public Plot() {
        FXMLLoader fxmlLoader = new FXMLLoader(

        getClass().getResource("/fxml/Plot.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
           fxmlLoader.load();
        } catch (IOException exception) {
           throw new RuntimeException(exception);
        }
    }

    public void clear() {
        pvReadbacksGraph.getData().clear();
        pvWriteablesGraph.getData().clear();
        stopZoom();
    }

    public void startZoom() {
        zoomManager.start();
    }

    public void stopZoom() {
        zoomManager.stop();
    }


    // Plot measurement of name measName
    public void plotMeasurement(String measName) {
        double [][] measurement = MainFunctions.mainDocument.getDataSet(measName);
        List<Channel> pvR = MainFunctions.mainDocument.getPVrbData(measName);
        List<Channel> pvW = MainFunctions.mainDocument.getPVWriteData(measName);
        plotMeasurement(measurement, pvW, pvR);
    }

    // Plot the current (ongoing) measurement
    public void plotMeasurement() {
        List<Channel> _pvR = new ArrayList<>();
        List<Channel> _pvW = new ArrayList<>();
        MainFunctions.mainDocument.pvChannels.forEach(cWrap -> {
            if (cWrap.getIsRead()) _pvR.add(cWrap.getChannel());
            if (cWrap.getIsScanned()) _pvW.add(cWrap.getChannel());
        });
        plotMeasurement(MainFunctions.mainDocument.currentMeasurement,_pvW,_pvR);
    }

    // Manually provide list of data and list of channels for the plot
    private void plotMeasurement(double [][] measurement, List<Channel> pvWriteables, List<Channel> pvReadbacks) {
        if (measurement != null) {
            for (int i=0;i<pvWriteables.size();i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName(pvWriteables.get(i).getId());
                pvWriteablesGraph.getData().add(series);
                addLegendHideable(pvWriteablesGraph);
            }
            for (int i=pvWriteables.size();i<measurement[0].length;i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName(pvReadbacks.get(i-pvWriteables.size()).getId());
                pvReadbacksGraph.getData().add(series);
                addLegendHideable(pvReadbacksGraph);
            }
        }
    }


    private void addLegendHideable(LineChart<Number, Number> chart) {
        chart.getChildrenUnmodifiable().stream().filter(n -> n instanceof Legend).forEach( n -> {
            ((Legend)n).getItems().forEach( li -> {
                for (XYChart.Series<Number, Number> s : chart.getData()) {
                    if (s.getName().equals(li.getText())) {
                        li.getSymbol().setCursor(Cursor.HAND);
                        li.getSymbol().setOnMouseClicked(me -> {
                            if (me.getButton() == MouseButton.PRIMARY) {
                                s.getNode().setVisible(!s.getNode().isVisible());
                                if (s.getNode().isVisible()) {
                                    li.getSymbol().setOpacity(1.0);
                                } else {
                                    li.getSymbol().setOpacity(0.2);
                                }
                                for (XYChart.Data<Number, Number> d : s.getData()) {
                                    if (d.getNode() != null) {
                                        d.getNode().setVisible(s.getNode().isVisible());
                                    }
                                }
                            }
                            // This seems like a clumsy way to trigger updateAutoRange(),
                            // but it is the best I've found for now
                            chart.getYAxis().setAutoRanging(false);
                            chart.getYAxis().setAutoRanging(true);
                        });
                        break;
                    }
                }
            });
        });
    }

}
