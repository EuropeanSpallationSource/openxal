/*
 * BrowserModel.java
 *
 * Created on Thu Mar 25 08:56:54 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger.apputils.browser;

import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.tools.messaging.MessageCenter;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * BrowserModel is the main document model.
 *
 * @author tap
 */
public class BrowserModel {

    private static final Logger LOGGER = Logger.getLogger(BrowserModel.class.getName());

    protected final MessageCenter messageCenter;
    protected final BrowserModelListener eventProxy;

    protected boolean hasConnected = false;

    protected PVLogger pvLogger;
    protected String[] loggerTypes;

    protected MachineSnapshot[] snapshots;
    protected ChannelGroup group;

    /**
     * Constructor
     */
    public BrowserModel() {
        messageCenter = new MessageCenter("Browser Model");
        eventProxy = messageCenter.registerSource(this, BrowserModelListener.class);

        snapshots = new MachineSnapshot[0];
        group = null;
    }

    /**
     * Add a listener of model events from this model.
     *
     * @param listener the listener to add for receiving model events.
     */
    public void addBrowserModelListener(final BrowserModelListener listener) {
        messageCenter.registerTarget(listener, this, BrowserModelListener.class);
    }

    /**
     * Remove the listener from receiving model events from this model.
     *
     * @param listener the listener to remove from receiving model events.
     */
    public void removeBrowserModelListener(final BrowserModelListener listener) {
        messageCenter.removeTarget(listener, this, BrowserModelListener.class);
    }

    /**
     * Set the database connection to the one specified.
     *
     * @param connection the new database connection
     */
    public void setDatabaseConnection(final Connection connection, final ConnectionDictionary dictionary) {
        hasConnected = false;

        group = null;
        snapshots = new MachineSnapshot[0];
        loggerTypes = null;
        pvLogger = new PVLogger(dictionary);
        hasConnected = true;
        eventProxy.connectionChanged(this);
    }

    /**
     * Connect to the database with the default connection dictionary
     *
     * @throws DatabaseException if the connection or schema fetch fails
     */
    public void connect() throws DatabaseException {
        connect(PVLogger.newBrowsingConnectionDictionary());
    }

    /**
     * Connect to the database with the specified connection dictionary
     *
     * @param dictionary The connection dictionary
     * @throws DatabaseException if the connection or schema fetch fails
     */
    public void connect(final ConnectionDictionary dictionary) throws DatabaseException {
        final Connection connection = dictionary.getDatabaseAdaptor().getConnection(dictionary);
        setDatabaseConnection(connection, dictionary);
    }

    /**
     * Determine if we have successfully connected to the database. Note that
     * this does not mean that the database connection is still valid.
     *
     * @return true if we have successfully connected to the database and false
     * if not
     */
    public boolean hasConnected() {
        return hasConnected;
    }

    /**
     * Fetch the available logger types from the data store.
     *
     * @return an array of available logger types.
     */
    protected String[] fetchLoggerTypes() throws SQLException {
        loggerTypes = pvLogger.fetchTypes();
        return loggerTypes;
    }

    /**
     * Get the array of available logger types.
     *
     * @return the array of available logger types.
     */
    public String[] getLoggerTypes() throws SQLException {
        return (hasConnected && loggerTypes == null) ? fetchLoggerTypes() : loggerTypes;
    }

    /**
     * Select the specified channel group corresponding to the logger type.
     *
     * @param type the logger type identifying the channel group
     * @return the channel group
     */
    public ChannelGroup selectGroup(final String type) throws SQLException {
        if (type == null) {
            group = null;
            eventProxy.selectedChannelGroupChanged(this, group);
        } else if (group == null || !group.getLabel().equals(type)) {
            group = pvLogger.getChannelGroup(type);
            eventProxy.selectedChannelGroupChanged(this, group);
        }
        return group;
    }

    /**
     * Get the selected channel group
     *
     * @return the selected channel group
     */
    public ChannelGroup getSelectedGroup() {
        return group;
    }

    /**
     * Get the array of machine snapshots that had been fetched.
     *
     * @return the array of machine snapshots
     */
    public MachineSnapshot[] getSnapshots() {
        return snapshots;
    }

    /**
     * Fetch the machine snapshots that were taken between the selected times.
     * Only identifier data is fetched into the machine snapshot.
     *
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     */
    public void fetchMachineSnapshots(final java.util.Date startTime, final java.util.Date endTime) throws SQLException {
        snapshots = pvLogger.fetchMachineSnapshotsInRange(group.getLabel(), startTime, endTime);
        LOGGER.log(Level.INFO, "Found {0} snapshots...", snapshots.length);
        eventProxy.machineSnapshotsFetched(this, snapshots);
    }

    /**
     * Populate all fetched machine snapshots with all of their data.
     */
    public void populateSnapshots() throws SQLException {
        for (int index = 0; index < snapshots.length; index++) {
            populateSnapshot(snapshots[index]);
        }
    }

    /**
     * Populate the machine snapshot with all of its data.
     *
     * @param snapshot the machine snapshot to populate
     * @return the machine snapshot that was populated (same object as the
     * parameter)
     */
    public MachineSnapshot populateSnapshot(final MachineSnapshot snapshot) throws SQLException {
        return snapshot.getChannelCount() == 0 ? pvLogger.loadChannelSnapshotsInto(snapshot) : snapshot;
    }
}
