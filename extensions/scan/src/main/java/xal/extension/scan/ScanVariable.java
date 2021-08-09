package xal.extension.scan;

import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.ca.*;

public class ScanVariable {

    private static final Logger LOGGER = Logger.getLogger(ScanVariable.class.getName());

    private MonitoredPV mpv = null;
    private MonitoredPV mpvRB = null;

    private double memValue = 0.0;

    //message text to report about problems
    private JTextField messageText = null;

    private ActionListener stopScanListener = null;
    private ActionEvent stopActionEvent = null;

    //synchronizing lock
    private Object lockObj = new Object();

    public ScanVariable(String alias, String aliasRB) {
        mpv = MonitoredPV.getMonitoredPV(alias);
        mpvRB = MonitoredPV.getMonitoredPV(aliasRB);
        stopActionEvent = new ActionEvent(this, 0, "stop");
    }

    protected void setStopScanListener(ActionListener stopScanListener) {
        this.stopScanListener = stopScanListener;
    }

    public void setMessageTextField(JTextField messageText) {
        this.messageText = messageText;
    }

    protected void setLockObject(Object lockObj) {
        this.lockObj = lockObj;
    }

    public String getChannelName() {
        return mpv.getChannelName();
    }

    public String getChannelNameRB() {
        return mpvRB.getChannelName();
    }

    public Channel getChannel() {
        return mpv.getChannel();
    }

    public Channel getChannelRB() {
        return mpvRB.getChannel();
    }

    public MonitoredPV getMonitoredPV() {
        return mpv;
    }

    public MonitoredPV getMonitoredPVRB() {
        return mpvRB;
    }

    public void setChannelName(String chanName) {
        synchronized (lockObj) {
            mpv.setChannelName(chanName);
        }
    }

    public void setChannelNameRB(String chanNameRB) {
        synchronized (lockObj) {
            mpvRB.setChannelName(chanNameRB);
        }
    }

    public void setChannel(Channel ch) {
        synchronized (lockObj) {
            mpv.setChannel(ch);
        }
    }

    public void setChannelRB(Channel chRB) {
        synchronized (lockObj) {
            mpvRB.setChannel(chRB);
        }
    }

    public void memorizeValue() {
        memValue = mpv.getValue();
    }

    public void restoreFromMemory() {
        setValue(memValue);
    }

    public void setValue(double val) {
        Channel ch = mpv.getChannel();
        String chanName = mpv.getChannelName();
        if (ch != null) {
            try {
                ch.putVal(val);
            } catch (PutException e) {
                stopScanWithMessage("Cannot put value to the channel: " + chanName);
            }
        }
    }

    public double getValue() {
        return mpv.getValue();
    }

    public double getValueRB() {
        return mpvRB.getValue();
    }

    private void stopScanWithMessage(String msg) {
        if (stopScanListener != null) {
            stopScanListener.actionPerformed(stopActionEvent);
        }
        if (messageText != null) {
            messageText.setText(msg);
        } else {
            LOGGER.log(Level.INFO, "ScanVariable class:{0}", msg);
        }
    }

}
