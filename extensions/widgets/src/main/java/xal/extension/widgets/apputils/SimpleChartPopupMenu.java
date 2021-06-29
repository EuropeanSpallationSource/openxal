/*
 * SimpleChartPopupMenu.java
 *
 * Created on January 21, 2003, 9:08 AM
 */
package xal.extension.widgets.apputils;

import java.awt.AWTException;
import xal.tools.apputils.*;
import xal.tools.apputils.ImageCaptureManager;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.event.*;
import java.awt.Window;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.FunctionGraphsPopupAdaptor;

/**
 * Popup menu that can be attached to a Chart to provide common actions for the
 * user. Supported chart types must have a ChartPopupAdaptor and an associated
 * constructor in this class. If you want the menu to appear with a popup event,
 * then you must add it as a mouse listener of the target view. Alternatively
 * you can use one of the static convenience methods:
 * <code>addPopupMenuTo()</code> to both create the popup menu and add it as a
 * mouse listener to the chart.
 *
 * @author tap
 */
public class SimpleChartPopupMenu extends JPopupMenu implements MouseListener {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(SimpleChartPopupMenu.class.getName());

    // action ID constants
    public static final String SCALE_ONCE_ID = "scale-once";
    public static final String X_AUTOSCALE_ID = "x-autoscale";
    public static final String Y_AUTOSCALE_ID = "y-autoscale";
    public static final String X_GRID_ID = "x-grid-toggle";
    public static final String Y_GRID_ID = "y-grid-toggle";
    public static final String OPTIONS_DIALOG_ID = "options-dialog";
    public static final String IMAGE_CAPTURE_ID = "save-image";

    // Chart references
    protected ChartPopupAdaptor chartAdaptor;

    // Menu action table keyed by action ID
    protected Map<String, Action> actionTable;

    // Menu actions
    protected Action scaleOnceAction;
    protected Action xAutoScaleAction;
    protected Action yAutoScaleAction;
    protected Action xGridAction;
    protected Action yGridAction;
    protected Action optionsAction;
    protected Action imageCaptureAction;

    // Other components
    protected Component chart;
    protected SimpleChartDialog chartDialog;
    protected JFileChooser fileChooser;

    /**
     * Primary constructor
     */
    public SimpleChartPopupMenu(Component aChart, ChartPopupAdaptor anAdaptor) {
        chart = aChart;
        chartAdaptor = anAdaptor;
        setup();
    }

    /**
     * Create a simple chart popup menu for a FunctionGraphsJPanel chart
     */
    public SimpleChartPopupMenu(FunctionGraphsJPanel aChart) {
        this(aChart, new FunctionGraphsPopupAdaptor(aChart));
    }

    /**
     * Convenience method for creating a SimpleChartPopupMenu and adding it as a
     * menu listener to the chart.
     *
     * @param aChart The chart to manage
     * @param anAdaptor The chart popup adaptor to use
     * @return The popup menu instance
     */
    public static SimpleChartPopupMenu addPopupMenuTo(Component aChart, ChartPopupAdaptor anAdaptor) {
        SimpleChartPopupMenu menu = new SimpleChartPopupMenu(aChart, anAdaptor);
        aChart.addMouseListener(menu);
        return menu;
    }

    /**
     * Convenience method for creating a SimpleChartPopupMenu and adding it as a
     * menu listener to the chart.
     *
     * @param aChart The chart to manage
     * @return The popup menu instance
     */
    public static SimpleChartPopupMenu addPopupMenuTo(FunctionGraphsJPanel aChart) {
        return addPopupMenuTo(aChart, new FunctionGraphsPopupAdaptor(aChart));
    }

    /**
     * Initialize the popup menu.
     */
    protected void setup() {
        actionTable = new HashMap<>(8);
        initComponents();
    }

    /**
     * Get the chart dialog and make it if it does not already exist
     *
     * @return the chart dialog
     */
    private SimpleChartDialog getChartDialog() {
        if (chartDialog != null) {
            return chartDialog;
        }

        Window owner = SwingUtilities.windowForComponent(chart);

        if (owner instanceof Frame) {
            chartDialog = new SimpleChartDialog((Frame) owner, chart, chartAdaptor);
        } else if (owner instanceof Dialog) {
            chartDialog = new SimpleChartDialog((Dialog) owner, chart, chartAdaptor);
        } else {
            chartDialog = new SimpleChartDialog(chart, chartAdaptor);
        }

        return chartDialog;
    }

    /**
     * Create and initialize the GUI components
     */
    protected void initComponents() {
        defineActions();
        storeActions();
        buildMenu();
    }

    /**
     * Define the actions for the popup menu
     */
    protected void defineActions() {
        // scale the X and Y axes once
        scaleOnceAction = new AbstractAction("Scale X and Y Once") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                chartAdaptor.scaleXandY();
            }
        };

        // toggle x auto scale
        xAutoScaleAction = new AbstractAction("Autoscale X") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                chartAdaptor.setXAutoScale(!chartAdaptor.isXAutoScale());
            }
        };

        // toggle y auto scale
        yAutoScaleAction = new AbstractAction("Autoscale Y") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                chartAdaptor.setYAutoScale(!chartAdaptor.isYAutoScale());
            }
        };

        // define x grid menu item action
        xGridAction = new AbstractAction("Show X Grid") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                chartAdaptor.setXGridVisible(!chartAdaptor.isXGridVisible());
            }
        };

        // define y grid menu item action
        yGridAction = new AbstractAction("Show Y Grid") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                chartAdaptor.setYGridVisible(!chartAdaptor.isYGridVisible());
            }
        };

        // define options menu item action
        optionsAction = new AbstractAction("Options...") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                getChartDialog().showDialog();
            }
        };

        // define options menu item action
        imageCaptureAction = new AbstractAction("Save as PNG") {
            /**
             * serialization ID
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    ImageCaptureManager.defaultManager().saveSnapshot(chartAdaptor.getChartComponent());
                } catch (AWTException | IOException exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                    JOptionPane.showMessageDialog(chartAdaptor.getChartComponent(), exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
                }
            }
        };
    }

    /**
     * Put the actions in the table.
     */
    protected void storeActions() {
        actionTable.put(SCALE_ONCE_ID, scaleOnceAction);
        actionTable.put(X_AUTOSCALE_ID, xAutoScaleAction);
        actionTable.put(Y_AUTOSCALE_ID, yAutoScaleAction);
        actionTable.put(X_GRID_ID, xGridAction);
        actionTable.put(Y_GRID_ID, yGridAction);
        actionTable.put(OPTIONS_DIALOG_ID, optionsAction);
        actionTable.put(IMAGE_CAPTURE_ID, imageCaptureAction);
    }

    /**
     * Build the popup menu by adding all of the defined actions
     */
    protected void buildMenu() {
        add(scaleOnceAction);
        add(xAutoScaleAction);
        add(yAutoScaleAction);
        addSeparator();
        add(xGridAction);
        add(yGridAction);
        addSeparator();
        add(optionsAction);
        addSeparator();
        add(imageCaptureAction);
    }

    /**
     * Update the components to reflect the state of the chart
     */
    protected void update() {
        xAutoScaleAction.putValue(Action.NAME, (chartAdaptor.isXAutoScale()) ? "Freeze X Scale" : "Autoscale X");
        yAutoScaleAction.putValue(Action.NAME, (chartAdaptor.isYAutoScale()) ? "Freeze Y Scale" : "Autoscale Y");

        xGridAction.putValue(Action.NAME, (chartAdaptor.isXGridVisible()) ? "Hide X Grid" : "Show X Grid");
        yGridAction.putValue(Action.NAME, (chartAdaptor.isYGridVisible()) ? "Hide Y Grid" : "Show Y Grid");

        pack();
    }

    /**
     * Enable/Disable the action specified by the actionID.
     *
     * @param actionID The id of the action to enable/disable.
     * @param enableState The desired enable/disable state.
     */
    public void setActionEnabled(final String actionID, final boolean enableState) {
        Action action = actionTable.get(actionID);
        action.setEnabled(enableState);
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseClicked(MouseEvent event) {
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mousePressed(MouseEvent event) {
        handleMouseEvent(event);
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        handleMouseEvent(event);
    }

    /**
     * handle the mouse event
     */
    public void handleMouseEvent(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            update();
            show(chartAdaptor.getChartComponent(), event.getX(), event.getY());
        }
    }
}
