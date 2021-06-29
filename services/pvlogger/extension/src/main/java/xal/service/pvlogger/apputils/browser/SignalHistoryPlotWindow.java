/*
 * SignalHistoryPlotWindow.java
 *
 * Created on Fri May 21 09:05:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger.apputils.browser;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.service.pvlogger.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.*;

/**
 * SignalHistoryPlotWindow displays a plot history of the selected signals.
 *
 * @author tap
 */
public class SignalHistoryPlotWindow extends JFrame implements SwingConstants {

    /**
     * specify the serializable version as required
     */
    private static final long serialVersionUID = 1;

    protected FunctionGraphsJPanel chart;
    protected BrowserController controller;
    protected BrowserModel model;

    /**
     * Primary constructor.
     *
     * @param controller the browser controller
     */
    public SignalHistoryPlotWindow(final BrowserController controller) {
        super("Signal History Plot");
        this.controller = controller;
        model = controller.getModel();

        makeContent();
    }

    /**
     * Show this window near the specified neighbor
     *
     * @param neighbor the view near which we wish to display this window
     */
    public void showNear(Component neighbor) {
        updateChart();
        setLocationRelativeTo(neighbor);
        setVisible(true);
    }

    /**
     * Build the component contents of the window.
     */
    protected void makeContent() {
        setSize(900, 500);
        Box mainView = new Box(BoxLayout.X_AXIS);
        getContentPane().add(mainView);

        mainView.add(buildChartView());
    }

    /**
     * Build the chart view.
     *
     * @return the chart view
     */
    protected Container buildChartView() {
        chart = new FunctionGraphsJPanel();
        chart.setSmartGL(false);
        chart.addMouseListener(new SimpleChartPopupMenu(chart));

        //chart.setBackground(Color.black);
        chart.setNumberFormatX(new DateGraphFormat("MMM dd, yyyy HH:mm"));

        // add legend support
        chart.setLegendPosition(FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY);
        chart.setLegendKeyString("Legend");
        chart.setLegendBackground(Color.lightGray);
        chart.setLegendColor(Color.black);
        chart.setLegendVisible(true);

        return chart;
    }

    /**
     * Update the chart with the latest data.
     */
    public void updateChart() {
        final List<String> signals = new ArrayList<>(controller.getSelectedSignals());
        final int numSignals = signals.size();
        final MachineSnapshot[] machineSnapshots = model.getSnapshots();

        final Map<String, List<ChannelSnapshot>> signalMap = new HashMap<String, List<ChannelSnapshot>>(numSignals);
        for (int signalIndex = 0; signalIndex < numSignals; signalIndex++) {
            final String signal = signals.get(signalIndex);
            signalMap.put(signal, new ArrayList<>());
        }

        try {
            model.populateSnapshots();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        for (int machineSnapshotIndex = 0; machineSnapshotIndex < machineSnapshots.length; machineSnapshotIndex++) {
            final MachineSnapshot machineSnapshot = machineSnapshots[machineSnapshotIndex];
            final ChannelSnapshot[] channelSnapshots = machineSnapshot.getChannelSnapshots();
            for (int index = 0; index < channelSnapshots.length; index++) {
                final ChannelSnapshot channelSnapshot = channelSnapshots[index];
                final String signal = channelSnapshot.getPV();
                final List<ChannelSnapshot> dataList = signalMap.get(signal);
                if (dataList != null) {
                    dataList.add(channelSnapshot);
                }
            }
        }

        final Vector<BasicGraphData> seriesData = new Vector<>();
        for (int signalIndex = 0; signalIndex < numSignals; signalIndex++) {
            final String signal = signals.get(signalIndex);
            final List<ChannelSnapshot> snapshots = signalMap.get(signal);
            final int numPoints = snapshots.size();
            double[] values = new double[numPoints];
            double[] timestamps = new double[numPoints];
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                final ChannelSnapshot snapshot = snapshots.get(pointIndex);
                values[pointIndex] = snapshot.getValue()[0];
                timestamps[pointIndex] = snapshot.getTimestamp().getSeconds();
            }
            if (numPoints > 0) {
                Color color = IncrementalColors.getColor(signalIndex);
                BasicGraphData graphData = new BasicGraphData();
                graphData.addPoint(timestamps, values);
                graphData.setGraphColor(color);
                graphData.setGraphProperty(chart.getLegendKeyString(), signal);
                graphData.setGraphName(signal);
                seriesData.add(graphData);
            }
        }
        chart.removeAllGraphData();
        chart.addGraphData(seriesData);

        // set the plot X range to only the selected snapshot range (exclude PV values with timestamps outside this range).
        double stepSize = (machineSnapshots[machineSnapshots.length - 1].getTimestamp().getTime() / 1000.
                - machineSnapshots[0].getTimestamp().getTime() / 1000.) / 3.;
        chart.setLimitsAndTicksX(machineSnapshots[0].getTimestamp().getTime() / 1000.,
                machineSnapshots[machineSnapshots.length - 1].getTimestamp().getTime() / 1000.,
                stepSize);
    }

    public FunctionGraphsJPanel getChart() {
        return chart;
    }
}
