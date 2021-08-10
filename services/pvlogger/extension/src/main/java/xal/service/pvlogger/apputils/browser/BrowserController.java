/*
 * BrowserController.java
 *
 * Created on Thu Mar 25 09:00:11 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger.apputils.browser;

import java.sql.SQLException;
import xal.service.pvlogger.*;
import xal.tools.messaging.*;
import xal.tools.data.KeyValueRecordListener;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;

import java.util.*;

/**
 * BrowserController manages the selection state of the browser window.
 *
 * @author tap
 */
public class BrowserController implements BrowserModelListener, KeyValueRecordListener<KeyValueFilteredTableModel<PVRecord>, PVRecord> {

    /**
     * browser model
     */
    protected BrowserModel model;

    /**
     * selected Machine snapshot *
     */
    protected MachineSnapshot selectedSnapshot;

    /**
     * The message center for dispatching messages
     */
    private final MessageCenter messageCenter;

    /**
     * Proxy for forwarding messages to registered listeners
     */
    private final BrowserControllerListener eventProxy;

    /**
     * table model of PVs
     */
    private final KeyValueFilteredTableModel<PVRecord> pvTableModel;

    /**
     * map of PV Records keyed by signal
     */
    private final Map<String, PVRecord> signalRecords;

    /**
     * table model for displaying the machine snapshots
     */
    private final KeyValueTableModel<MachineSnapshot> machineSnapshotTableModel;

    /**
     * table model for displaying the channel snapshots
     */
    private final KeyValueTableModel<ChannelSnapshot> channelSnapshotTableModel;

    /**
     * Constructor
     */
    public BrowserController(final BrowserModel model) {
        this.model = model;
        model.addBrowserModelListener(this);

        pvTableModel = new KeyValueFilteredTableModel<>();
        pvTableModel.setKeyPaths("enabled", "signal");
        pvTableModel.setMatchingKeyPaths("signal");
        pvTableModel.setColumnName("enabled", "Use");
        pvTableModel.setColumnEditable("enabled", true);
        pvTableModel.setColumnClass("enabled", Boolean.class);
        pvTableModel.addKeyValueRecordListener(this);

        machineSnapshotTableModel = new KeyValueTableModel<>();
        machineSnapshotTableModel.setKeyPaths("id", "timestamp");

        channelSnapshotTableModel = new KeyValueTableModel<>();
        channelSnapshotTableModel.setKeyPaths("PV", "timestamp", "valueCount", "scalarValue", "status", "severity");

        signalRecords = new HashMap<>();

        messageCenter = new MessageCenter("Browser Controller");
        eventProxy = messageCenter.registerSource(this, BrowserControllerListener.class);

        updatePVTableModel();
    }

    /**
     * Add a listener of controller events from this controller
     *
     * @param listener the listener to add
     */
    public void addBrowserControllerListener(final BrowserControllerListener listener) {
        messageCenter.registerTarget(listener, this, BrowserControllerListener.class);
    }

    /**
     * Remove the listener from receiving controller events from this controller
     *
     * @param listener the listener to remove
     */
    public void removeBrowserControllerListener(final BrowserControllerListener listener) {
        messageCenter.removeTarget(listener, this, BrowserControllerListener.class);
    }

    /**
     * get the table model of PVs
     */
    public KeyValueFilteredTableModel<PVRecord> getPVTableModel() {
        return pvTableModel;
    }

    /**
     * get the table model of machine snapshots
     */
    public KeyValueTableModel<MachineSnapshot> getMachineSnapshotTableModel() {
        return machineSnapshotTableModel;
    }

    /**
     * get the table model of channel snapshots
     */
    public KeyValueTableModel<ChannelSnapshot> getChannelSnapshotTableModel() {
        return channelSnapshotTableModel;
    }

    /**
     * Convert the array of channel wrappers to an array of signals.
     *
     * @param wrappers the array of channel wrappers
     * @return the corresponding array of signals
     */
    protected static String[] convertToPVs(final ChannelWrapper[] wrappers) {
        String[] signals = new String[wrappers.length];
        for (int index = 0; index < wrappers.length; index++) {
            signals[index] = wrappers[index].getPV();
        }
        return signals;
    }

    /**
     * Select or deselect the collection of signals without affecting the
     * selection status of other signals.
     *
     * @param select true to select signals and false to deselect signals
     */
    public void selectSignals(final boolean select) {
        for (final PVRecord pvRecord : pvTableModel.getRowRecords()) {
            pvRecord.setEnabled(select);
        }

        pvTableModel.fireTableDataChanged();
        eventProxy.selectedSignalsChanged(this, getSelectedSignals());
    }

    /**
     * get the list of selected signals
     */
    public List<String> getSelectedSignals() {
        final List<String> selectedSignals = new ArrayList<>();

        for (final PVRecord pvRecord : pvTableModel.getRowRecords()) {
            if (pvRecord.getEnabled()) {
                selectedSignals.add(pvRecord.getSignal());
            }
        }
        return selectedSignals;
    }

    /**
     * determine whether the signal is selected
     */
    private boolean isSignalSelected(final String signal) {
        final PVRecord pvRecord = signalRecords.get(signal);
        return pvRecord != null && pvRecord.getEnabled();
    }

    /**
     * Filter each channel snapshot based on whether its signal is selected
     *
     * @param snapshots The snapshots to filter
     * @return the array of filtered snapshots corresponding to selected signals
     */
    public ChannelSnapshot[] filterSnapshots(final ChannelSnapshot[] snapshots) {
        final List<ChannelSnapshot> filteredSnapshots = new ArrayList<>(snapshots.length);

        for (int index = 0; index < snapshots.length; index++) {
            final ChannelSnapshot snapshot = snapshots[index];
            if (isSignalSelected(snapshot.getPV())) {
                filteredSnapshots.add(snapshot);
            }
        }
        ChannelSnapshot[] result = new ChannelSnapshot[filteredSnapshots.size()];
        filteredSnapshots.toArray(result);

        return result;
    }

    /**
     * Get the main model
     *
     * @return the main model
     */
    public BrowserModel getModel() {
        return model;
    }

    /**
     * update the PV table model
     */
    private void updatePVTableModel() {
        signalRecords.clear();

        final ChannelGroup group = model.getSelectedGroup();
        if (group != null) {
            final ChannelWrapper[] wrappers = group.getChannelWrappers();
            for (final ChannelWrapper wrapper : wrappers) {
                final String signal = wrapper.getPV();
                final PVRecord aRecord = new PVRecord(signal);
                this.signalRecords.put(signal, aRecord);
            }
        }

        pvTableModel.setRecords((List<PVRecord>) signalRecords.values());
    }

    /**
     * update the detail for the selected machine snapshot
     */
    private void updateSelectedMachineSnapshotDetail() {
        final MachineSnapshot snapshot = selectedSnapshot;

        final ChannelSnapshot[] channelSnapshots = (snapshot != null) ? filterSnapshots(snapshot.getChannelSnapshots()) : null;
        final List<ChannelSnapshot> channelSnapshotRecords = new ArrayList<>();
        if (channelSnapshots != null) {
            for (final ChannelSnapshot channelSnapshot : channelSnapshots) {
                channelSnapshotRecords.add(channelSnapshot);
            }
        }
        channelSnapshotTableModel.setRecords(channelSnapshotRecords);
    }

    /**
     * Set the snapshot which is selected by the user
     *
     * @param snapshot the machine snapshot to select
     */
    public void setSelectedSnapshot(final MachineSnapshot snapshot) {
        if (snapshot != null) {
            try {
                model.populateSnapshot(snapshot);
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }
        selectedSnapshot = snapshot;
        updateSelectedMachineSnapshotDetail();

        eventProxy.snapshotSelected(this, snapshot);
    }

    /**
     * The model's connection has changed
     *
     * @param model The model whose connection changed
     */
    @Override
    public void connectionChanged(final BrowserModel model) {
        // Do nothing
    }

    /**
     * Update the channel wrappers for the newly selected channel group and
     * forward this event to the browser controller listeners.
     *
     * @param model the source of the event
     * @param newGroup the newly selected channel group
     */
    @Override
    public void selectedChannelGroupChanged(final BrowserModel model, final ChannelGroup newGroup) {
        updatePVTableModel();
        eventProxy.selectedChannelGroupChanged(this, newGroup);
    }

    /**
     * Handle the "machine snapshot fetched" event. Does nothing.
     *
     * @param model the model providing the event
     * @param snapshots the new snapshots that have been fetched
     */
    @Override
    public void machineSnapshotsFetched(final BrowserModel model, final MachineSnapshot[] snapshots) {
        final List<MachineSnapshot> machineSnapshots = new ArrayList<>();
        for (final MachineSnapshot snapshot : snapshots) {
            machineSnapshots.add(snapshot);
        }
        machineSnapshotTableModel.setRecords(machineSnapshots);
    }

    /**
     * forward message that table record changed
     */
    @Override
    public void recordModified(final KeyValueFilteredTableModel<PVRecord> tableModel, final PVRecord pvRecord, final String keyPath, final Object value) {
        updateSelectedMachineSnapshotDetail();
        eventProxy.selectedSignalsChanged(this, getSelectedSignals());
    }
}
