/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import xal.ca.BatchConnectionRequest;
import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.ConnectionListener;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.smf.impl.Magnet;

/**
 *
 * @author nataliamilas
 */
class ChannelMonitor implements IEventSinkValue, ConnectionListener {

    private BatchConnectionRequest request;
    private HashMap<Channel,Object> inputChannels;
    private Monitor monitor;

    public ChannelMonitor() {
       this.inputChannels= new HashMap<>();
    }        

    public void connectAndMonitor(HashMap<Channel,Object> inputChannels) {       
        this.inputChannels = inputChannels;        
        
        request = new BatchConnectionRequest(inputChannels.keySet());
        request.submitAndWait(5.0);  
        
        inputChannels.keySet().forEach(channel->{
            channel.addConnectionListener(this);
        });
    }

    @Override
    public void eventValue(ChannelRecord record, Channel chan) {
        if (chan.isConnected()) {
            if(inputChannels.get(chan) instanceof Label){
                Platform.runLater(
                () -> {
                    ((Label) inputChannels.get(chan)).setText(String.format("%.3f",record.doubleValue()));
                    ((Label) inputChannels.get(chan)).setStyle("-fx-background-color: white;");                                         
                });
            } else if (inputChannels.get(chan) instanceof Circle){
                Platform.runLater(
                () -> {
                    int val = (int) Math.round(record.doubleValue());
                    switch (val) {
                        case 1:
                            ((Circle) inputChannels.get(chan)).setFill(Color.GREEN);
                            break;
                        case 0:
                            ((Circle) inputChannels.get(chan)).setFill(Color.RED);
                            break;
                        default:
                            ((Circle) inputChannels.get(chan)).setFill(Color.GRAY);
                            break;
                    }     
                });
            } else if (inputChannels.get(chan) instanceof TextField){
                Platform.runLater(
                () -> {                    
                    ((TextField) inputChannels.get(chan)).setText(String.format("%.3f",record.doubleValue()));
                    ((TextField) inputChannels.get(chan)).setStyle("-fx-background-color: white;");                     
                });                                                          
            }      
        }
    }

    @Override
    public void connectionMade(Channel channel) {
        if (channel.isConnected()) {
            try {            
                monitor = channel.addMonitorValue(this, Monitor.VALUE);            
            } catch (ConnectionException | MonitorException ex) {
                Logger.getLogger(ChannelMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void connectionDropped(Channel chan) {
        if(inputChannels.get(chan) instanceof Label){
            ((Label) inputChannels.get(chan)).setStyle("-fx-background-color: magenta;");
        } else if (inputChannels.get(chan) instanceof Circle){
            ((Circle) inputChannels.get(chan)).setFill(Color.GRAY);
        } if(inputChannels.get(chan) instanceof TextField){
            ((TextField) inputChannels.get(chan)).setStyle("-fx-background-color: magenta;");
        }
        monitor.clear();
    }

}