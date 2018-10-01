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
    
    public void disconnectAndClearAll(){
        inputChannels.keySet().forEach(channel->{
            channel.removeConnectionListener(this);
            monitor.clear();
        });
        
        inputChannels.clear();
                
    }            

    @Override
    public void eventValue(ChannelRecord record, Channel chan) {
        if (chan.isConnected()) {
            if(inputChannels.get(chan) instanceof Label){
                if(chan.channelName().contains("PolR")){
                    Platform.runLater(
                    () -> {
                        if(record.stringValue().equals("0")){
                            ((Label) inputChannels.get(chan)).setText("NEG");
                            ((Label) inputChannels.get(chan)).setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);");       
                            //((Label) inputChannels.get(chan)).setId("negative");             
                        } else if (record.stringValue().equals("1")){
                            ((Label) inputChannels.get(chan)).setText("POS");
                            ((Label) inputChannels.get(chan)).setStyle("-fx-background-color: linear-gradient(#5ee825, #206802);");
                            //((Label) inputChannels.get(chan)).setId("positive");              
                        } else {
                            ((Label) inputChannels.get(chan)).setText("-");
                            ((Label) inputChannels.get(chan)).setStyle("-fx-background-color: magenta;");
                            //((Label) inputChannels.get(chan)).setId("disconected");             
                        }                                                    
                    });               
                } else {
                    Platform.runLater(
                    () -> {
                        ((Label) inputChannels.get(chan)).setText(String.format("%.4f",record.doubleValue()));
                        //((Label) inputChannels.get(chan)).setStyle("-fx-background-color: white;");                                         
                        ((Label) inputChannels.get(chan)).setId("connected");
                    });
                }
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
                    ((TextField) inputChannels.get(chan)).setText(String.format("%.4f",record.doubleValue()));
                    //((TextField) inputChannels.get(chan)).setStyle("-fx-background-color: white;");                     
                    ((TextField) inputChannels.get(chan)).setId("connected");
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
            //((Label) inputChannels.get(chan)).setStyle("-fx-background-color: magenta;");
            ((Label) inputChannels.get(chan)).setId("disconected");
        } else if (inputChannels.get(chan) instanceof Circle){
            ((Circle) inputChannels.get(chan)).setFill(Color.GRAY);
        } if(inputChannels.get(chan) instanceof TextField){
            //((TextField) inputChannels.get(chan)).setStyle("-fx-background-color: magenta;");
            ((TextField) inputChannels.get(chan)).setId("disconected");
        }
        monitor.clear();
    }

}