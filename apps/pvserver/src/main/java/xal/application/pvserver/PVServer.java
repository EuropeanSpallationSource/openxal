/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.application.pvserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.IServerChannel;
import xal.ca.IServerChannelFactory;
import xal.ca.PutException;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PVServer {

    private ObservableList<PV> pvs;

    public void setPvs(ObservableList<PV> pvs) {
        this.pvs = pvs;
    }

    private ChannelFactory CHANNEL_SERVER_FACTORY = null;

    private static PVServer INSTANCE = null;

    public static final PVServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PVServer();
        }
        return INSTANCE;
    }

    private PVServer() {
    }

    public void startServer() {
        CHANNEL_SERVER_FACTORY = ChannelFactory.newServerFactory();
        pvs.forEach(pv -> addPV(pv));
    }

    public void updatePVs() {
        for (PV pv : pvs) {
            Channel channel = pv.getChannel();
            channel.setChannelName(pv.getName());
            ((IServerChannel) channel).setSettable(pv.getWritable());
            try {
                channel.putVal(Double.parseDouble(pv.getValue()));
            } catch (ConnectionException | PutException ex) {
                Logger.getLogger(PVServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stopServer() {
        if (CHANNEL_SERVER_FACTORY != null) {
            ((IServerChannelFactory) CHANNEL_SERVER_FACTORY).dispose();
            CHANNEL_SERVER_FACTORY = null;
        }
    }

    public void addPV(PV pv) {
        Channel channel = getChannel(pv.getName());
        ((IServerChannel) channel).setSettable(pv.getWritable());
        try {
            channel.putVal(Double.parseDouble(pv.getValue()));
        } catch (ConnectionException | PutException ex) {
            Logger.getLogger(PVServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        pv.setChannel(channel);
    }

    private ChannelFactory getChannelFactory() {
        return CHANNEL_SERVER_FACTORY;
    }

    private Channel getChannel(String name) {
        return getChannelFactory().getChannel(name);
    }
}
