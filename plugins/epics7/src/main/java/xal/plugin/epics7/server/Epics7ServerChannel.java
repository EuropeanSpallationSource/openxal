/*
 * Copyright (C) 2020 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.plugin.epics7.server;

import com.cosylab.epics.caj.cas.ProcessVariableEventDispatcher;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pva.data.PVAArray;
import org.epics.pva.data.PVABool;
import org.epics.pva.data.PVAByte;
import org.epics.pva.data.PVAByteArray;
import org.epics.pva.data.PVAData;
import org.epics.pva.data.PVADouble;
import org.epics.pva.data.PVADoubleArray;
import org.epics.pva.data.PVAFloat;
import org.epics.pva.data.PVAFloatArray;
import org.epics.pva.data.PVAInt;
import org.epics.pva.data.PVAIntArray;
import org.epics.pva.data.PVALong;
import org.epics.pva.data.PVALongArray;
import org.epics.pva.data.PVAShort;
import org.epics.pva.data.PVAShortArray;
import org.epics.pva.data.PVAString;
import org.epics.pva.data.PVAStringArray;
import org.epics.pva.data.PVAStructure;
import org.epics.pva.data.nt.PVAAlarm;
import org.epics.pva.data.nt.PVAControl;
import org.epics.pva.data.nt.PVADisplay;
import org.epics.pva.data.nt.PVAScalar;
import org.epics.pva.data.nt.PVATimeStamp;
import org.epics.pva.server.ServerPV;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;
import xal.ca.IServerChannel;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.plugin.epics7.CaDbrConverter;
import xal.plugin.epics7.Epics7Channel;
import xal.plugin.epics7.Epics7ChannelRecord;
import xal.plugin.epics7.Epics7ChannelStatusRecord;
import xal.plugin.epics7.Epics7ChannelTimeRecord;

/**
 * Server channel implementation. It serves the same value over PV Access and Channel Access, independently of the
 * signal prefix. This is done to ensure backwards compatibility.
 *
 * The channel owns the record: a {@link PVAStructure} held in memory. Writes arriving from either protocol, and local
 * puts, all funnel through {@link #updateValue}, which mirrors the new value to the other protocol and notifies
 * monitors. The previous implementation kept a pvDatabase record and a Channel Access process variable in sync with a
 * monitor on each; the PV Access library used by Phoebus offers a write callback instead, so no monitor is needed.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannel extends Epics7Channel implements IServerChannel, ProcessVariableEventCallback {

    private MemoryProcessVariable memoryProcessVariable;
    private ServerPV serverPV;

    /**
     * The served value plus its metadata. Guarded by {@link #updateLock}.
     */
    private PVAStructure record;

    private final Epics7ServerChannelSystem epics7ServerChannelSystem;

    private static final Logger LOGGER = Logger.getLogger(Epics7ServerChannel.class.getName());

    private static final String DISPLAY_FIELD_ERR = "Couldn't find \"display\" field.";
    private static final String VALUE_ALARM_FIELD_ERR = "Couldn't find \"valueAlarm\" field.";
    private static final String CONTROL_FIELD_ERR = "Couldn't find \"control\" field.";

    private final List<Epics7ServerMonitor> monitors = new CopyOnWriteArrayList<>();

    /**
     * Serialises mutations of {@link #record}.
     *
     * It must never be held while writing to {@link #memoryProcessVariable}: MemoryProcessVariable.write is
     * synchronized and calls back into {@link #postEvent} while holding its own monitor, so holding this lock across
     * that call would invert the lock order against an incoming Channel Access write and deadlock.
     */
    private final ReentrantLock updateLock = new ReentrantLock();

    /**
     * Set while this thread mirrors a value into the Channel Access process variable, so that the resulting
     * {@link #postEvent} callback is recognised as our own echo rather than a client write.
     */
    private final ThreadLocal<Boolean> mirroringToCa = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public Epics7ServerChannel(String signalName, Epics7ServerChannelSystem channelSystem) {
        super(signalName, channelSystem);

        // Removing protocol in case it is defined.
        if (strId.startsWith("ca://") || strId.startsWith("pva://")) {
            strId = strId.substring(strId.indexOf("://") + 3);
        }

        this.epics7ServerChannelSystem = channelSystem;

        requestConnection();
    }

    // Always return true because there is no connection to be made.
    @Override
    public boolean connectAndWait(double timeout) {
        requestConnection();
        return isConnected();
    }

    // No connection to be made, just create the record. By default its type is double.
    @Override
    public final void requestConnection() {
        if (record == null) {
            createPVs(DBRType.DOUBLE, new PVADouble(VALUE_FIELD, 0.0));
            connectionFlag = true;
        }
    }

    @Override
    public void disconnect() {
        updateLock.lock();
        try {
            removeCAPV();
            removePvaPV();
            record = null;
            connectionFlag = false;
        } finally {
            updateLock.unlock();
        }
    }

    void addMonitor(Epics7ServerMonitor monitor) {
        monitors.add(monitor);
    }

    void removeMonitor(Epics7ServerMonitor monitor) {
        monitors.remove(monitor);
    }

    // ---------------- Record construction ----------------
    /**
     * The pvData "valueAlarm_t" structure. Strings have no alarm limits, so they get no valueAlarm field, matching the
     * previous implementation.
     */
    private static PVAStructure newValueAlarm() {
        return new PVAStructure(VALUE_ALARM_FIELD, "epics:nt/valueAlarm_t:1.0",
                new PVABool("active", false),
                new PVADouble("lowAlarmLimit", 0.0),
                new PVADouble("lowWarningLimit", 0.0),
                new PVADouble("highWarningLimit", 0.0),
                new PVADouble("highAlarmLimit", 0.0),
                new PVAInt("lowAlarmSeverity", 0),
                new PVAInt("lowWarningSeverity", 0),
                new PVAInt("highWarningSeverity", 0),
                new PVAInt("highAlarmSeverity", 0),
                new PVAByte("hysteresis", false, (byte) 0));
    }

    private static PVAStructure newRecord(PVAData valueField) {
        boolean isString = valueField instanceof PVAString || valueField instanceof PVAStringArray;
        boolean isArray = valueField instanceof PVAArray;

        String structName = isArray ? PVAScalar.ARRAY_STRUCT_NAME_STRING : PVAScalar.SCALAR_STRUCT_NAME_STRING;

        if (isString) {
            return new PVAStructure("", structName,
                    valueField,
                    new PVAAlarm(),
                    new PVATimeStamp(),
                    new PVADisplay(0.0, 0.0, "", "", 0, PVADisplay.Form.DEFAULT),
                    new PVAControl(0.0, 0.0, 0.0));
        }
        return new PVAStructure("", structName,
                valueField,
                new PVAAlarm(),
                new PVATimeStamp(),
                new PVADisplay(0.0, 0.0, "", "", 0, PVADisplay.Form.DEFAULT),
                new PVAControl(0.0, 0.0, 0.0),
                newValueAlarm());
    }

    /**
     * An initial value for the Channel Access process variable: a single element array of the right primitive type.
     */
    private static Object initialCaValue(DBRType type) {
        if (type == DBRType.STRING) {
            return new String[]{""};
        }
        if (type == DBRType.BYTE) {
            return new byte[]{0};
        }
        if (type == DBRType.SHORT) {
            return new short[]{0};
        }
        if (type == DBRType.INT) {
            return new int[]{0};
        }
        if (type == DBRType.FLOAT) {
            return new float[]{0.0f};
        }
        return new double[]{0.0};
    }

    /**
     * Replace both process variables with ones of the given type, preserving nothing. Must hold {@link #updateLock}.
     */
    private void createPVs(DBRType dbrType, PVAData valueField) {
        removeCAPV();
        removePvaPV();

        // TODO: copy metadata from the old record to the new one.
        record = newRecord(valueField);

        memoryProcessVariable = new MemoryProcessVariable(strId, null, dbrType, initialCaValue(dbrType));
        epics7ServerChannelSystem.addMemPV(memoryProcessVariable);
        ((ProcessVariableEventDispatcher) memoryProcessVariable.getEventCallback()).registerEventListener(this);

        // Passing a write handler is what makes the PV writable.
        serverPV = epics7ServerChannelSystem.getPvaServer().createPV(strId, record, this::handlePvaWrite);
    }

    private void removeCAPV() {
        if (memoryProcessVariable != null) {
            ProcessVariableEventCallback callback = memoryProcessVariable.getEventCallback();
            if (callback instanceof ProcessVariableEventDispatcher) {
                ((ProcessVariableEventDispatcher) callback).unregisterEventListener(this);
            }
            epics7ServerChannelSystem.removeMemPV(memoryProcessVariable);
            memoryProcessVariable = null;
        }
    }

    private void removePvaPV() {
        if (serverPV != null) {
            serverPV.close();
            serverPV = null;
        }
    }

    // ---------------- Value propagation ----------------
    /**
     * A value mutation to apply to the record while {@link #updateLock} is held.
     */
    @FunctionalInterface
    private interface Mutation {

        void apply() throws Exception;
    }

    /**
     * Apply a change to the record, then mirror it to the protocol that did not originate it and notify monitors.
     *
     * The record is mutated under {@link #updateLock}, but the mirroring happens outside it; see the note on that
     * field for why.
     *
     * @param toCa whether the Channel Access process variable needs updating
     */
    private void updateRecord(Mutation mutation, boolean toCa) throws Exception {
        DBR dbr;
        PVAStructure snapshot;

        updateLock.lock();
        try {
            if (record == null) {
                return;
            }
            mutation.apply();
            PVATimeStamp.set(record, Instant.now());

            dbr = toCa ? CaDbrConverter.toDBR(record.get(VALUE_FIELD)) : null;
            snapshot = record.cloneData();
        } finally {
            updateLock.unlock();
        }

        if (dbr != null && memoryProcessVariable != null) {
            mirroringToCa.set(Boolean.TRUE);
            try {
                memoryProcessVariable.write(dbr, null);
            } catch (CAException ex) {
                LOGGER.log(Level.SEVERE, "Could not update Channel Access value of " + strId, ex);
            } finally {
                mirroringToCa.set(Boolean.FALSE);
            }
        }

        if (serverPV != null) {
            try {
                serverPV.update(snapshot);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not update PV Access value of " + strId, ex);
            }
        }

        for (Epics7ServerMonitor monitor : monitors) {
            monitor.post(snapshot);
        }
    }

    /**
     * A Channel Access client wrote to the process variable.
     */
    @Override
    public void postEvent(int select, DBR event) {
        // Ignore the echo of a value this channel just pushed into the CA process variable.
        if (Boolean.TRUE.equals(mirroringToCa.get())) {
            return;
        }
        try {
            updateRecord(() -> setValueFromDbr(event), false);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not apply Channel Access write to " + strId, ex);
        }
    }

    @Override
    public void canceled() {
        // Nothing to do.
    }

    /**
     * A PV Access client wrote to the served PV. The library hands over a copy of the record with the client's changes
     * already applied.
     */
    private void handlePvaWrite(org.epics.pva.common.TCPHandler tcp, ServerPV pv, java.util.BitSet changes,
            PVAStructure written) throws Exception {
        updateRecord(() -> record.get(VALUE_FIELD).setValue(written.get(VALUE_FIELD)), true);
    }

    /**
     * Copy a Channel Access value into the record's value field.
     */
    private void setValueFromDbr(DBR dbr) throws Exception {
        PVAData valueField = record.get(VALUE_FIELD);
        Object value = dbr.getValue();

        if (!(valueField instanceof PVAArray)) {
            valueField.setValue(Array.get(value, 0));
            return;
        }
        // Channel Access has no 64 bit integer type, so a long array arrives as int[].
        if (valueField instanceof PVALongArray && value instanceof int[]) {
            int[] ints = (int[]) value;
            long[] longs = new long[ints.length];
            for (int i = 0; i < ints.length; i++) {
                longs[i] = ints[i];
            }
            valueField.setValue(longs);
            return;
        }
        valueField.setValue(value);
    }

    // ---------------- Reads ----------------
    @Override
    public int elementCount() throws ConnectionException {
        updateLock.lock();
        try {
            return record == null ? 0 : Epics7ChannelRecord.getCountArray(record.get(VALUE_FIELD));
        } finally {
            updateLock.unlock();
        }
    }

    @Override
    protected PVAStructure getDisplay() {
        return record == null ? null : record.get(DISPLAY_FIELD);
    }

    @Override
    protected PVAStructure getVAlueAlarm() {
        return record == null ? null : record.get(VALUE_ALARM_FIELD);
    }

    @Override
    protected PVAStructure getControl() {
        return record == null ? null : record.get(CONTROL_FIELD);
    }

    @Override
    public String getUnits() {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            PVAString units = displayStructure.get("units");
            return units == null ? "" : units.get();
        }

        return "";
    }

    /**
     * A snapshot of the record, so that callers are not exposed to later updates.
     */
    private PVAStructure snapshot() {
        updateLock.lock();
        try {
            return record.cloneData();
        } finally {
            updateLock.unlock();
        }
    }

    @Override
    public ChannelRecord getRawValueRecord() throws GetException {
        return new Epics7ChannelRecord(snapshot());
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws GetException {
        return new Epics7ChannelStatusRecord(snapshot());
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws GetException {
        return new Epics7ChannelTimeRecord(snapshot());
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws GetException {
        listener.eventValue(getRawValueRecord(), this);
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws GetException {
        listener.eventValue(getRawValueRecord(), this);
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws GetException {
        listener.eventValue(getRawTimeRecord(), this);
    }

    // ---------------- Monitors ----------------
    @Override
    public Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValTime");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7ServerMonitor.createNewMonitor(this, pvStructure -> {
            ChannelTimeRecord channelRecord = new Epics7ChannelTimeRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValStatus");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7ServerMonitor.createNewMonitor(this, pvStructure -> {
            ChannelStatusRecord channelRecord = new Epics7ChannelStatusRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValue");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7ServerMonitor.createNewMonitor(this, pvStructure -> {
            ChannelRecord channelRecord = new Epics7ChannelRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    // ---------------- Writes ----------------
    /**
     * Set a new value, recreating the process variables first if the type changed.
     *
     * @param typeClass the Open XAL element type the new value implies
     * @param dbrType the Channel Access type to serve
     * @param template a fresh, empty value field of the right PV Access type
     * @param newValue the value to store
     */
    private void updateValue(Class<?> typeClass, DBRType dbrType, PVAData template, Object newValue,
            PutListener listener) throws PutException {
        try {
            updateLock.lock();
            try {
                if (record == null || elementType() != typeClass) {
                    createPVs(dbrType, template);
                }
            } finally {
                updateLock.unlock();
            }

            updateRecord(() -> record.get(VALUE_FIELD).setValue(newValue), true);
        } catch (Exception ex) {
            throw new PutException("Could not write to " + strId + ": " + ex.getMessage());
        }

        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws PutException {
        updateValue(String.class, DBRType.STRING, new PVAString(VALUE_FIELD), newVal, listener);
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws PutException {
        updateValue(byte.class, DBRType.BYTE, new PVAByte(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws PutException {
        updateValue(short.class, DBRType.SHORT, new PVAShort(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws PutException {
        updateValue(int.class, DBRType.INT, new PVAInt(VALUE_FIELD), newVal, listener);
    }

    /**
     * Long is not supported in EPICS3, so they are cast to int for CA.
     */
    @Override
    public void putRawValCallback(long newVal, PutListener listener) throws PutException {
        updateValue(long.class, DBRType.INT, new PVALong(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws PutException {
        updateValue(float.class, DBRType.FLOAT, new PVAFloat(VALUE_FIELD, 0.0f), newVal, listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws PutException {
        updateValue(double.class, DBRType.DOUBLE, new PVADouble(VALUE_FIELD, 0.0), newVal, listener);
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws PutException {
        updateValue(String[].class, DBRType.STRING, new PVAStringArray(VALUE_FIELD), newVal, listener);
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws PutException {
        updateValue(byte[].class, DBRType.BYTE, new PVAByteArray(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws PutException {
        updateValue(short[].class, DBRType.SHORT, new PVAShortArray(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws PutException {
        updateValue(int[].class, DBRType.INT, new PVAIntArray(VALUE_FIELD, false), newVal, listener);
    }

    /**
     * Long is not supported in EPICS3, so they are cast to int for CA.
     */
    @Override
    public void putRawValCallback(long[] newVal, PutListener listener) throws PutException {
        updateValue(long[].class, DBRType.INT, new PVALongArray(VALUE_FIELD, false), newVal, listener);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws PutException {
        updateValue(float[].class, DBRType.FLOAT, new PVAFloatArray(VALUE_FIELD), newVal, listener);
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws PutException {
        updateValue(double[].class, DBRType.DOUBLE, new PVADoubleArray(VALUE_FIELD), newVal, listener);
    }

    // ---------------- Metadata ----------------
    private void setDisplayField(String name, Number value, String error) {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            PVADouble field = displayStructure.get(name);
            if (field != null) {
                field.set(value.doubleValue());
                return;
            }
        }
        LOGGER.severe(error);
    }

    private void setValueAlarmField(String name, Number value) {
        PVAStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            PVADouble field = alarmValueStructure.get(name);
            if (field != null) {
                field.set(value.doubleValue());
                return;
            }
        }
        LOGGER.severe(VALUE_ALARM_FIELD_ERR);
    }

    private void setControlField(String name, Number value) {
        PVAStructure controlStructure = getControl();
        if (controlStructure != null) {
            PVADouble field = controlStructure.get(name);
            if (field != null) {
                field.set(value.doubleValue());
                return;
            }
        }
        LOGGER.severe(CONTROL_FIELD_ERR);
    }

    @Override
    public void setUnits(String units) {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            PVAString field = displayStructure.get("units");
            if (field != null) {
                field.set(units);
            }
        } else {
            LOGGER.severe(DISPLAY_FIELD_ERR);
        }

        memoryProcessVariable.setUnits(units);
    }

    @Override
    public void setLowerDispLimit(Number lowerLimit) {
        setDisplayField("limitLow", lowerLimit, DISPLAY_FIELD_ERR);
        memoryProcessVariable.setLowerDispLimit(lowerLimit);
    }

    @Override
    public void setUpperDispLimit(Number upperLimit) {
        setDisplayField("limitHigh", upperLimit, DISPLAY_FIELD_ERR);
        memoryProcessVariable.setUpperDispLimit(upperLimit);
    }

    @Override
    public void setLowerAlarmLimit(Number lowerLimit) {
        setValueAlarmField("lowAlarmLimit", lowerLimit);
        memoryProcessVariable.setLowerAlarmLimit(lowerLimit);
    }

    @Override
    public void setUpperAlarmLimit(Number upperLimit) {
        setValueAlarmField("highAlarmLimit", upperLimit);
        memoryProcessVariable.setUpperAlarmLimit(upperLimit);
    }

    @Override
    public void setLowerWarningLimit(Number lowerLimit) {
        setValueAlarmField("lowWarningLimit", lowerLimit);
        memoryProcessVariable.setLowerWarningLimit(lowerLimit);
    }

    @Override
    public void setUpperWarningLimit(Number upperLimit) {
        setValueAlarmField("highWarningLimit", upperLimit);
        memoryProcessVariable.setUpperWarningLimit(upperLimit);
    }

    @Override
    public void setLowerCtrlLimit(Number lowerLimit) {
        setControlField("limitLow", lowerLimit);
        memoryProcessVariable.setLowerCtrlLimit(lowerLimit);
    }

    @Override
    public void setUpperCtrlLimit(Number upperLimit) {
        setControlField("limitHigh", upperLimit);
        memoryProcessVariable.setUpperCtrlLimit(upperLimit);
    }

    @Override
    public void setSettable(boolean settable) {
        // Does nothing.
    }
}
