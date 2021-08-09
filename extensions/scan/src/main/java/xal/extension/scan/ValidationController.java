package xal.extension.scan;

import xal.extension.widgets.swing.*;

import java.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

public class ValidationController {

    private DoubleInputTextField lowLimText = new DoubleInputTextField(6);
    private DoubleInputTextField uppLimText = new DoubleInputTextField(6);

    private DecimalFormat limFormat = new DecimalFormat("###.###");

    private JRadioButton validationButton = new JRadioButton("Validation:");
    private JLabel lowLimLabel = new JLabel(" low=", SwingConstants.CENTER);
    private JLabel uppLimLabel = new JLabel(" upp=", SwingConstants.CENTER);

    private JPanel validatorLimitsPanel = new JPanel();

    private double lowLim = 0.;
    private double uppLim = 100.;

    private boolean isOn = false;

    private Vector<ChangeListener> changeListenerV = new Vector<>();
    private ChangeEvent changeEvent = null;

    public ValidationController() {
        init(lowLim, uppLim);
    }

    public ValidationController(double lowLimIn, double uppLimIn) {
        init(lowLimIn, uppLimIn);
    }

    public void init(double lowLimIn, double uppLimIn) {
        lowLim = lowLimIn;
        uppLim = uppLimIn;
        lowLimText.setHorizontalAlignment(SwingConstants.CENTER);
        uppLimText.setHorizontalAlignment(SwingConstants.CENTER);
        lowLimText.setNormalBackground(Color.white);
        uppLimText.setNormalBackground(Color.white);

        lowLimText.setNumberFormat(limFormat);
        uppLimText.setNumberFormat(limFormat);

        lowLimText.setEditable(false);
        uppLimText.setEditable(false);
        setLowLim(lowLim);
        setUppLim(uppLim);

        validationButton.setSelected(false);

        changeEvent = new ChangeEvent(this);

        lowLimText.addActionListener(e -> {
            lowLim = lowLimText.getValue();
            notifyChanges();
        });

        uppLimText.addActionListener(e -> {
            uppLim = uppLimText.getValue();
            notifyChanges();
        });

        validationButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setOnOff(true);
                lowLimText.setEditable(true);
                uppLimText.setEditable(true);
            } else {
                setOnOff(false);
                lowLimText.setEditable(false);
                uppLimText.setEditable(false);
            }
        });

        setFontForAll(new Font("Monospaced", Font.PLAIN, 10));
    }

    public void setFontForAll(Font fnt) {
        lowLimText.setFont(fnt);
        uppLimText.setFont(fnt);
        validationButton.setFont(fnt);
        lowLimLabel.setFont(fnt);
        uppLimLabel.setFont(fnt);
    }

    public JPanel getJPanel() {
        return getJPanel(0);
    }

    public JPanel getJPanel(int index) {
        validatorLimitsPanel.removeAll();

        if (index == 0) {
            JPanel tmp = new JPanel();
            tmp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            tmp.add(validationButton);
            tmp.add(lowLimLabel);
            tmp.add(lowLimText);
            tmp.add(uppLimLabel);
            tmp.add(uppLimText);

            JPanel tmp1 = new JPanel();
            tmp1.setLayout(new BorderLayout());
            tmp1.add(tmp, BorderLayout.NORTH);

            validatorLimitsPanel.setLayout(new BorderLayout());
            validatorLimitsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
            validatorLimitsPanel.setBackground(validatorLimitsPanel.getBackground().darker());
            validatorLimitsPanel.add(tmp1, BorderLayout.NORTH);

            notifyChanges();
        }

        if (index == 1) {
            JPanel tmp = new JPanel();
            tmp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            validationButton.setText("Threshold for re-measuring = ");
            tmp.add(validationButton);
            tmp.add(lowLimText);

            JPanel tmp1 = new JPanel();
            tmp1.setLayout(new BorderLayout());
            tmp1.add(tmp, BorderLayout.NORTH);

            validatorLimitsPanel.setLayout(new BorderLayout());
            validatorLimitsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
            validatorLimitsPanel.setBackground(validatorLimitsPanel.getBackground().darker());
            validatorLimitsPanel.add(tmp1, BorderLayout.NORTH);

            uppLim = Double.MAX_VALUE;
            notifyChanges();
        }

        return validatorLimitsPanel;
    }

    public void setLowLim(double lowLimIn) {
        lowLimText.setValue(lowLimIn);
        notifyChanges();
    }

    public void setUppLim(double uppLimIn) {
        uppLimText.setValue(uppLimIn);
        notifyChanges();
    }

    public double getLowLim() {
        if (isOn) {
            return lowLim;
        }
        return (-Double.MAX_VALUE);
    }

    public double getUppLim() {
        if (isOn) {
            return uppLim;
        }
        return Double.MAX_VALUE;
    }

    public double getInnerLowLim() {
        return lowLim;
    }

    public double getInnerUppLim() {
        return uppLim;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOnOff(boolean isOnIn) {
        isOn = isOnIn;
        validationButton.setSelected(isOn);
        notifyChanges();
    }

    public void addChangeListener(ChangeListener chgL) {
        changeListenerV.add(chgL);
    }

    public void removeChangeListener(ChangeListener chgL) {
        changeListenerV.remove(chgL);
    }

    public void removeAllChangeListeners() {
        changeListenerV.clear();
    }

    private void notifyChanges() {
        for (int i = 0, n = changeListenerV.size(); i < n; i++) {
            changeListenerV.get(i).stateChanged(changeEvent);
        }
    }

    //------------------------------------
    //MAIN for debugging
    //------------------------------------
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Valuator Limits Manager Class");
        mainFrame.addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        mainFrame.getContentPane().setLayout(new BorderLayout());

        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        mainFrame.getContentPane().add(tmpP, BorderLayout.WEST);

        ValidationController vm = new ValidationController(0.0, 20.0);
        JPanel vmPanel = vm.getJPanel();
        vmPanel.setBackground(Color.getHSBColor(0.9f, 0.9f, 0.9f));
        tmpP.add(vmPanel, BorderLayout.NORTH);

        mainFrame.pack();
        mainFrame.setSize(new Dimension(300, 430));
        mainFrame.setVisible(true);
    }
}
