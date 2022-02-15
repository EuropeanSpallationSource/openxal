/*
 * SimpleChartDialog.java
 *
 * Created on January 17, 2003, 12:48 PM
 */
package xal.tools.apputils;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.border.*;
import java.text.DecimalFormat;

/**
 * Dialog box that can be attached to a chart to make common settings available
 * to the user. If you want the dialog to show when the popup menu item event
 * occurs, you must add this as a MouseListener of the desired target view.
 *
 * @author tap
 */
public class SimpleChartDialog extends JDialog implements MouseListener, SwingConstants {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    protected ChartPopupAdaptor chartAdaptor;
    protected Component parent;

    // visual components
    private JCheckBox xAutoScaleCheckbox;
    private JTextField xAxisMaxValueField;
    private JTextField xAxisMinValueField;
    private JTextField xAxisMinorTicksField;
    private JTextField xAxisDivisionsField;
    private JCheckBox xGridCheckbox;
    private JCheckBox yAutoScaleCheckbox;
    private JTextField yAxisMinValueField;
    private JTextField yAxisMaxValueField;
    private JTextField yAxisMinorTicksField;
    private JTextField yAxisDivisionsField;
    private JCheckBox yGridCheckbox;

    /**
     * Creates new form SimpleChartDialog
     *
     * @param parent the parent view near which to display this dialog
     * @param aChartAdaptor the chart popup adaptor to use
     */
    public SimpleChartDialog(Component parent, ChartPopupAdaptor aChartAdaptor) {
        super();
        setup(parent, aChartAdaptor);
    }

    /**
     * Creates new form SimpleChartDialog
     *
     * @param owner the window which owns this dialog
     * @param parent the parent view near which to display this dialog
     * @param aChartAdaptor the chart popup adaptor to use
     */
    public SimpleChartDialog(Frame owner, Component parent, ChartPopupAdaptor aChartAdaptor) {
        super(owner);
        setup(parent, aChartAdaptor);
    }

    /**
     * Creates new form SimpleChartDialog
     *
     * @param owner the window which owns this dialog
     * @param parent the parent view near which to display this dialog
     * @param aChartAdaptor the chart popup adaptor to use
     */
    public SimpleChartDialog(Dialog owner, Component parent, ChartPopupAdaptor aChartAdaptor) {
        super(owner);
        setup(parent, aChartAdaptor);
    }

    /**
     * Setup the dialog
     *
     * @param parent the parent view near which to display this dialog
     * @param aChartAdaptor the chart popup adaptor to use
     */
    protected void setup(Component parent, ChartPopupAdaptor aChartAdaptor) {
        setModal(true);
        initComponents();

        this.parent = parent;
        chartAdaptor = aChartAdaptor;
    }

    /**
     * display this dialog
     */
    public void showDialog() {
        revertSettings();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * convenience method for setting an int field
     */
    public static void setFieldValue(JTextField field, int value) {
        field.setText(String.valueOf(value));
    }

    /**
     * convenience method for setting a double field
     */
    public static void setFieldValue(JTextField field, double value) {
        DecimalFormat formatter = new DecimalFormat("0.0##E0");
        field.setText(formatter.format(value));
    }

    /**
     * Set the values in the panel to reflect the setting in the chart
     */
    protected void revertSettings() {
        boolean xAutoScales = chartAdaptor.isXAutoScale();
        xAutoScaleCheckbox.setSelected(xAutoScales);

        setFieldValue(xAxisMinValueField, chartAdaptor.getMinXLimit());
        setFieldValue(xAxisMaxValueField, chartAdaptor.getMaxXLimit());
        setFieldValue(xAxisMinorTicksField, chartAdaptor.getXNumMinorTicks());
        setFieldValue(xAxisDivisionsField, chartAdaptor.getXNumMajorTicks() - 1);
        xGridCheckbox.setSelected(chartAdaptor.isXGridVisible());

        xAxisMinValueField.setEnabled(!xAutoScales);
        xAxisMaxValueField.setEnabled(!xAutoScales);
        xAxisMinorTicksField.setEnabled(!xAutoScales);
        xAxisDivisionsField.setEnabled(!xAutoScales);

        setFieldValue(yAxisMinValueField, chartAdaptor.getMinYLimit());
        setFieldValue(yAxisMaxValueField, chartAdaptor.getMaxYLimit());
        setFieldValue(yAxisMinorTicksField, chartAdaptor.getYNumMinorTicks());
        setFieldValue(yAxisDivisionsField, chartAdaptor.getYNumMajorTicks() - 1);
        yGridCheckbox.setSelected(chartAdaptor.isYGridVisible());

        boolean yAutoScales = chartAdaptor.isYAutoScale();
        yAutoScaleCheckbox.setSelected(yAutoScales);

        yAxisMinValueField.setEnabled(!yAutoScales);
        yAxisMaxValueField.setEnabled(!yAutoScales);
        yAxisMinorTicksField.setEnabled(!yAutoScales);
        yAxisDivisionsField.setEnabled(!yAutoScales);
    }

    /**
     * Apply the values entered in the panel to the chart
     */
    protected void applySettings() {
        if (!chartAdaptor.isXAutoScale()) {
            applyXAxisMinValue();
            applyXAxisMaxValue();
            // repeated in case new x-min is greater than old x-max
            applyXAxisMinValue();
            applyXAxisMinorTicks();
            applyXAxisMajorTicks();
        }
        applyXAxisGridSetting();

        if (!chartAdaptor.isYAutoScale()) {
            applyYAxisMinValue();
            applyYAxisMaxValue();
            // repeated in case new y-min is greater than old y-max
            applyYAxisMinValue();
            applyYAxisMinorTicks();
            applyYAxisMajorTicks();
        }
        applyYAxisGridSetting();

        // update display with actual settings
        revertSettings();
    }

    /**
     * Apply the Minimum x-axis value
     */
    protected void applyXAxisMinValue() {
        try {
            String text = xAxisMinValueField.getText();
            double xAxisMinValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (xAxisMinValue != chartAdaptor.getMinXLimit()) {
                chartAdaptor.setMinXLimit(xAxisMinValue);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the Maximum x-axis value
     */
    protected void applyXAxisMaxValue() {
        try {
            String text = xAxisMaxValueField.getText();
            double xAxisMaxValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (xAxisMaxValue != chartAdaptor.getMaxXLimit()) {
                chartAdaptor.setMaxXLimit(xAxisMaxValue);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the X-Axis tick spacing
     */
    protected void applyXAxisMinorTicks() {
        try {
            String text = xAxisMinorTicksField.getText();
            int numTicks = Integer.parseInt(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (numTicks < 0) {
                Toolkit.getDefaultToolkit().beep();
            } else if (numTicks != chartAdaptor.getXNumMinorTicks()) {
                chartAdaptor.setXNumMinorTicks(numTicks);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the X-Axis tick spacing
     */
    protected void applyXAxisMajorTicks() {
        try {
            String text = xAxisDivisionsField.getText();
            int numTicks = Integer.parseInt(text) + 1;
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (numTicks < 2) {
                Toolkit.getDefaultToolkit().beep();
            } else if (numTicks != chartAdaptor.getXNumMajorTicks()) {
                chartAdaptor.setXNumMajorTicks(numTicks);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Set whether to show the x-axis grid
     */
    protected void applyXAxisGridSetting() {
        chartAdaptor.setXGridVisible(xGridCheckbox.isSelected());
    }

    /**
     * Apply the Minimum y-axis value
     */
    protected void applyYAxisMinValue() {
        try {
            String text = yAxisMinValueField.getText();
            double yAxisMinValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (yAxisMinValue != chartAdaptor.getMinYLimit()) {
                chartAdaptor.setMinYLimit(yAxisMinValue);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the Maximum y-axis value
     */
    protected void applyYAxisMaxValue() {
        try {
            String text = yAxisMaxValueField.getText();
            double yAxisMaxValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (yAxisMaxValue != chartAdaptor.getMaxYLimit()) {
                chartAdaptor.setMaxYLimit(yAxisMaxValue);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the Y-Axis tick spacing
     */
    protected void applyYAxisMinorTicks() {
        try {
            String text = yAxisMinorTicksField.getText();
            int numTicks = Integer.parseInt(text);
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (numTicks < 0) {
                Toolkit.getDefaultToolkit().beep();
            } else if (numTicks != chartAdaptor.getYNumMinorTicks()) {
                chartAdaptor.setYNumMinorTicks(numTicks);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Apply the Y-Axis tick spacing
     */
    protected void applyYAxisMajorTicks() {
        try {
            String text = yAxisDivisionsField.getText();
            int numTicks = Integer.parseInt(text) + 1;
            // don't change the value unless the user does to avoid inadvertantly 
            // changing from autoscale
            if (numTicks < 2) {
                Toolkit.getDefaultToolkit().beep();
            } else if (numTicks != chartAdaptor.getYNumMajorTicks()) {
                chartAdaptor.setYNumMajorTicks(numTicks);
            }
        } catch (NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Set whether to show the y-axis grid
     */
    protected void applyYAxisGridSetting() {
        chartAdaptor.setYGridVisible(yGridCheckbox.isSelected());
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        // Do nothing
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseEntered(MouseEvent event) {
        // Do nothing
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseExited(MouseEvent event) {
        // Do nothing
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            showDialog();
        }
    }

    /**
     * implement MouseListener interface
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        // Do nothing
    }

    /**
     * Create and layout the visual components.
     */
    private void initComponents() {
        setTitle("Chart Settings");
        setSize(new Dimension(450, 375));
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });

        Box mainView = new Box(VERTICAL);
        getContentPane().add(mainView);

        Box xAxisView = new Box(VERTICAL);
        xAxisView.setBorder(new TitledBorder("X Axis"));
        mainView.add(xAxisView);

        Box row = new Box(HORIZONTAL);

        xAutoScaleCheckbox = new JCheckBox("Auto Scale");
        xAutoScaleCheckbox.addActionListener(event -> xAutoScaleCheckboxActionPerformed());
        row.add(Box.createHorizontalGlue());
        row.add(xAutoScaleCheckbox);
        xAxisView.add(row);

        xAxisMinValueField = new JTextField(10);
        xAxisMinValueField.setMaximumSize(xAxisMinValueField.getPreferredSize());
        xAxisMinValueField.setHorizontalAlignment(SwingConstants.RIGHT);
        xAxisMinValueField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                xAxisMinValueField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                xAxisMinValueField.setCaretPosition(0);
                xAxisMinValueField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Min:"));
        row.add(xAxisMinValueField);
        xAxisView.add(row);

        xAxisMaxValueField = new JTextField(10);
        xAxisMaxValueField.setMaximumSize(xAxisMaxValueField.getPreferredSize());
        xAxisMaxValueField.setHorizontalAlignment(SwingConstants.RIGHT);
        xAxisMaxValueField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                xAxisMaxValueField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                xAxisMaxValueField.setCaretPosition(0);
                xAxisMaxValueField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Max:"));
        row.add(xAxisMaxValueField);
        xAxisView.add(row);

        xAxisDivisionsField = new JTextField(10);
        xAxisDivisionsField.setMaximumSize(xAxisDivisionsField.getPreferredSize());
        xAxisDivisionsField.setHorizontalAlignment(SwingConstants.RIGHT);
        xAxisDivisionsField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                xAxisDivisionsField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                xAxisDivisionsField.setCaretPosition(0);
                xAxisDivisionsField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Major Divisions:"));
        row.add(xAxisDivisionsField);
        xAxisView.add(row);

        xAxisMinorTicksField = new JTextField(10);
        xAxisMinorTicksField.setMaximumSize(xAxisMinorTicksField.getPreferredSize());
        xAxisMinorTicksField.setHorizontalAlignment(SwingConstants.RIGHT);
        xAxisMinorTicksField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                xAxisMinorTicksField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                xAxisMinorTicksField.setCaretPosition(0);
                xAxisMinorTicksField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Ticks/Division:"));
        row.add(xAxisMinorTicksField);
        xAxisView.add(row);

        xGridCheckbox = new JCheckBox("Show Grid");
        xGridCheckbox.addActionListener(event -> xGridCheckboxActionPerformed());
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(xGridCheckbox);
        xAxisView.add(row);

        Box yAxisView = new Box(VERTICAL);
        yAxisView.setBorder(new TitledBorder("Y Axis"));
        mainView.add(yAxisView);

        yAutoScaleCheckbox = new JCheckBox("Auto Scale");
        yAutoScaleCheckbox.addActionListener(event -> yAutoScaleCheckboxActionPerformed());
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(yAutoScaleCheckbox);
        yAxisView.add(row);

        yAxisMinValueField = new JTextField(10);
        yAxisMinValueField.setMaximumSize(yAxisMinValueField.getPreferredSize());
        yAxisMinValueField.setHorizontalAlignment(SwingConstants.RIGHT);
        yAxisMinValueField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                yAxisMinValueField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                yAxisMinValueField.setCaretPosition(0);
                yAxisMinValueField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Min:"));
        row.add(yAxisMinValueField);
        yAxisView.add(row);

        yAxisMaxValueField = new JTextField(10);
        yAxisMaxValueField.setMaximumSize(yAxisMaxValueField.getPreferredSize());
        yAxisMaxValueField.setHorizontalAlignment(SwingConstants.RIGHT);
        yAxisMaxValueField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                yAxisMaxValueField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                yAxisMaxValueField.setCaretPosition(0);
                yAxisMaxValueField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Max:"));
        row.add(yAxisMaxValueField);
        yAxisView.add(row);

        yAxisDivisionsField = new JTextField(10);
        yAxisDivisionsField.setMaximumSize(yAxisDivisionsField.getPreferredSize());
        yAxisDivisionsField.setHorizontalAlignment(SwingConstants.RIGHT);
        yAxisDivisionsField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                yAxisDivisionsField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                yAxisDivisionsField.setCaretPosition(0);
                yAxisDivisionsField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Major Divisions:"));
        row.add(yAxisDivisionsField);
        yAxisView.add(row);

        yAxisMinorTicksField = new JTextField(10);
        yAxisMinorTicksField.setMaximumSize(yAxisMinorTicksField.getPreferredSize());
        yAxisMinorTicksField.setHorizontalAlignment(SwingConstants.RIGHT);
        yAxisMinorTicksField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                yAxisMinorTicksField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                yAxisMinorTicksField.setCaretPosition(0);
                yAxisMinorTicksField.moveCaretPosition(0);
            }
        });
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(new JLabel("Ticks/Division:"));
        row.add(yAxisMinorTicksField);
        yAxisView.add(row);

        yGridCheckbox = new JCheckBox();
        yGridCheckbox.setText("Show Grid");
        yGridCheckbox.addActionListener(event -> yGridCheckboxActionPerformed());
        row = new Box(HORIZONTAL);
        row.add(Box.createHorizontalGlue());
        row.add(yGridCheckbox);
        yAxisView.add(row);

        Box buttonView = new Box(HORIZONTAL);
        mainView.add(buttonView);
        buttonView.add(Box.createHorizontalGlue());

        JButton revertButton = new JButton("Revert");
        revertButton.addActionListener(event -> revertButtonActionPerformed());
        buttonView.add(revertButton);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(event -> applyButtonActionPerformed());
        buttonView.add(applyButton);

        pack();
    }

    private void yGridCheckboxActionPerformed() {
        applyYAxisGridSetting();
        revertSettings();
    }

    private void xGridCheckboxActionPerformed() {
        applyXAxisGridSetting();
        revertSettings();
    }

    private void yAutoScaleCheckboxActionPerformed() {
        boolean autoScales = yAutoScaleCheckbox.isSelected();
        chartAdaptor.setYAutoScale(autoScales);
        revertSettings();
    }

    private void xAutoScaleCheckboxActionPerformed() {
        boolean autoScales = xAutoScaleCheckbox.isSelected();
        chartAdaptor.setXAutoScale(autoScales);
        revertSettings();
    }

    private void revertButtonActionPerformed() {
        revertSettings();
    }

    private void applyButtonActionPerformed() {
        applySettings();
    }

    /**
     * Closes the dialog
     */
    private void closeDialog() {
        setVisible(false);
    }
}
