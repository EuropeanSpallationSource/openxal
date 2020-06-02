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

import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.epics.pvdatabase.PVRecord;
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
import xal.plugin.epics7.Epics7Channel;
import xal.plugin.epics7.Epics7ChannelRecord;
import xal.plugin.epics7.Epics7ChannelStatusRecord;
import xal.plugin.epics7.Epics7ChannelTimeRecord;

/**
 * Server channel implementation. It creates PVAccess and CA channels,
 * independently of the signal prefix. This is done to ensure backwards
 * compatibility.
 *
 * Gets are done on the PVRecord, while sets are done to both PVRecord and CA
 * PV, so that they are always in sync.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannel extends Epics7Channel implements IServerChannel {

    // Record associated with this channel.    
    private MemoryProcessVariable memoryProcessVariable;
    private PVRecord pvRecord;

    private final Epics7ServerChannelSystem epics7ServerChannelSystem;

    private static final String PROPERTIES = ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
            + DISPLAY_FIELD + "," + CONTROL_FIELD;

    public Epics7ServerChannel(String signalName, Epics7ServerChannelSystem CHANNEL_SYSTEM) {
        super(signalName, CHANNEL_SYSTEM);

        // Removing protocol in case it is defined.
        if (m_strId.startsWith("ca://") || m_strId.startsWith("pva://")) {
            m_strId = m_strId.substring(m_strId.indexOf("://") + 3);
        }

        this.epics7ServerChannelSystem = CHANNEL_SYSTEM;

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
        if (pvRecord == null) {
            addCAPV(DBRType.DOUBLE);
            addRecord(ScalarType.pvDouble, false);

            // Adding a monitor to update the value on one protocol channel when the
            // other one is updated by new data received in a put.
            try {
                Epics7ServerMonitor.createNewMonitor(pvRecord, memoryProcessVariable,
                        Epics7Channel.VALUE_REQUEST, (pvS) -> {
                        }, 0);
            } catch (ConnectionException e) {
                Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Not possible to set callback to update value");
            }

            connectionFlag = true;
        }
    }

    @Override
    public void disconnect() {
        removeCAPV();
        removeRecord();
        connectionFlag = false;
    }

    private void addCAPV(DBRType type) {
        MemoryProcessVariable newMemoryProcessVariable = new MemoryProcessVariable(m_strId, null, type, new double[]{0.0});

        if (memoryProcessVariable != null) {
            // TODO: Copy metadata from old record to new record
            removeCAPV();
        }
        memoryProcessVariable = newMemoryProcessVariable;

        epics7ServerChannelSystem.addMemPV(memoryProcessVariable);
    }

    private void addRecord(ScalarType scalarType, boolean array) {
        StandardField standardField = StandardFieldFactory.getStandardField();

        String properties = PROPERTIES;
        if (scalarType != ScalarType.pvString) {
            properties += "," + VALUE_ALARM_FIELD;
        }

        Structure structure;
        if (array) {
            structure = standardField.scalarArray(scalarType, properties);
        } else {
            structure = standardField.scalar(scalarType, properties);
        }
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
        PVRecord newPVRecord = new PVRecord(m_strId, pvStructure);
        if (pvRecord != null) {
            // TODO: Copy metadata from old record to new record
            removeRecord();
        }
        pvRecord = newPVRecord;

        epics7ServerChannelSystem.addRecord(pvRecord);
    }

    private void removeCAPV() {
        if (memoryProcessVariable != null) {
            epics7ServerChannelSystem.removeMemPV(memoryProcessVariable);
            memoryProcessVariable = null;
        }
    }

    private void removeRecord() {
        if (pvRecord != null) {
            epics7ServerChannelSystem.removeRecord(pvRecord);
            pvRecord = null;
        }
    }

    @Override
    public int elementCount() throws ConnectionException {
        if (pvRecord != null) {
            PVField valueField = pvRecord.getPVStructure().getSubField(VALUE_FIELD);
            Type type = valueField.getField().getType();
            switch (type) {
                case scalar:
                    return 1;
                case scalarArray:
                    return Epics7ChannelRecord.getCountArray(pvRecord.getPVStructure(), valueField);
                default:
                    break;
            }
        }
        return 0;
    }

    protected PVStructure getDisplay() {
        if (pvRecord != null) {
            return pvRecord.getPVStructure().getStructureField(DISPLAY_FIELD);
        }
        return null;
    }

    protected PVStructure getVAlueAlarm() {
        if (pvRecord != null) {
            return pvRecord.getPVStructure().getStructureField(VALUE_ALARM_FIELD);
        }
        return null;
    }

    protected PVStructure getControl() {
        if (pvRecord != null) {
            return pvRecord.getPVStructure().getStructureField(CONTROL_FIELD);
        }
        return null;
    }

    @Override
    public String getUnits() {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return displayStructure.getStringField("units").get();
        }

        return "";
    }

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return new Epics7ChannelRecord(pvRecord.getPVStructure(), m_strId);
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return new Epics7ChannelStatusRecord(pvRecord.getPVStructure(), m_strId);
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        return new Epics7ChannelTimeRecord(pvRecord.getPVStructure(), m_strId);
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        listener.eventValue(getRawValueRecord(), this);
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException, GetException {
        listener.eventValue(getRawValueRecord(), this);
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws ConnectionException, GetException {
        listener.eventValue(getRawTimeRecord(), this);
    }

    @Override
    public Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire) throws ConnectionException, MonitorException {
        checkConnection("addMonitorValTime");

        return Epics7ServerMonitor.createNewMonitor(pvRecord, memoryProcessVariable, Epics7Channel.TIME_REQUEST, (pvStructure) -> {
            ChannelTimeRecord record = new Epics7ChannelTimeRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws ConnectionException, MonitorException {
        checkConnection("addMonitorValStatus");

        return Epics7ServerMonitor.createNewMonitor(pvRecord, memoryProcessVariable, Epics7Channel.STATUS_REQUEST, (pvStructure) -> {
            ChannelStatusRecord record = new Epics7ChannelStatusRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws ConnectionException, MonitorException {
        checkConnection("addMonitorValue");

        return Epics7ServerMonitor.createNewMonitor(pvRecord, memoryProcessVariable, Epics7Channel.VALUE_REQUEST, (pvStructure) -> {
            ChannelRecord record = new Epics7ChannelRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    private void updateTimeStampAlarmsAndTriggerListener(PutListener listener) {
        long currentTimeMillis = System.currentTimeMillis();

        int nanoSeconds = (int) (1000000 * currentTimeMillis % 1000);
        long seconds = currentTimeMillis / 1000;

        PVStructure timeStampField = pvRecord.getPVStructure().getStructureField(TIMESTAMP_FIELD);

        timeStampField.getLongField(Epics7ChannelTimeRecord.SECONDS_FIELD_NAME).put(seconds);
        timeStampField.getIntField(Epics7ChannelTimeRecord.NANOSECONDS_FIELD_NAME).put(nanoSeconds);

        //TODO: update alarms
        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != String.class) {
            addCAPV(DBRType.STRING);
            addRecord(ScalarType.pvString, false);
        }

        pvRecord.getPVStructure().getStringField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_String(new String[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != byte.class) {
            addCAPV(DBRType.BYTE);
            addRecord(ScalarType.pvByte, false);
        }

        pvRecord.getPVStructure().getByteField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Byte(new byte[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != short.class) {
            addCAPV(DBRType.SHORT);
            addRecord(ScalarType.pvShort, false);
        }

        pvRecord.getPVStructure().getShortField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Short(new short[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != int.class) {
            addCAPV(DBRType.INT);
            addRecord(ScalarType.pvInt, false);
        }

        pvRecord.getPVStructure().getIntField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Int(new int[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    /**
     * Long is not supported in EPICS3, so they are casted to int for CA.
     */
    @Override
    public void putRawValCallback(long newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != long.class) {
            addCAPV(DBRType.INT);
            addRecord(ScalarType.pvLong, false);
        }

        pvRecord.getPVStructure().getLongField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Int(new int[]{(int) newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != float.class) {
            addCAPV(DBRType.FLOAT);
            addRecord(ScalarType.pvFloat, false);
        }

        pvRecord.getPVStructure().getFloatField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Float(new float[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != double.class) {
            addCAPV(DBRType.DOUBLE);
            addRecord(ScalarType.pvDouble, false);
        }

        pvRecord.getPVStructure().getDoubleField(VALUE_FIELD).put(newVal);

        DBR dbr = new DBR_Double(new double[]{newVal});
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != String[].class) {
            addCAPV(DBRType.STRING);
            addRecord(ScalarType.pvString, true);
        }

        pvRecord.getPVStructure().getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_String(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != byte[].class) {
            addCAPV(DBRType.BYTE);
            addRecord(ScalarType.pvByte, true);
        }

        pvRecord.getPVStructure().getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_String(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != short[].class) {
            addCAPV(DBRType.SHORT);
            addRecord(ScalarType.pvShort, true);
        }

        pvRecord.getPVStructure().getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_Short(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != int[].class) {
            addCAPV(DBRType.INT);
            addRecord(ScalarType.pvInt, true);
        }

        pvRecord.getPVStructure().getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_Int(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    /**
     * Long is not supported in EPICS3, so they are casted to int for CA.
     */
    @Override
    public void putRawValCallback(long[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != long[].class) {
            addCAPV(DBRType.INT);
            addRecord(ScalarType.pvLong, true);
        }

        pvRecord.getPVStructure().getSubField(PVLongArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        int[] newInt = new int[newVal.length];
        for (int i = 0; i < newVal.length; i++) {
            newInt[i] = (int) newVal[i];
        }
        DBR dbr = new DBR_Int(newInt);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != float[].class) {
            addCAPV(DBRType.FLOAT);
            addRecord(ScalarType.pvFloat, true);
        }

        pvRecord.getPVStructure().getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_Float(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != double[].class) {
            addCAPV(DBRType.DOUBLE);
            addRecord(ScalarType.pvDouble, true);
        }

        pvRecord.getPVStructure().getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        DBR dbr = new DBR_Double(newVal);
        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void setUnits(String units) {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            displayStructure.getStringField("units").put(units);
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find \"display\" field.");
        }

        memoryProcessVariable.setUnits(units);
    }

    @Override
    public void setLowerDispLimit(Number lowerLimit) {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            displayStructure.getDoubleField("limitLow").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find \"display\" field.");
        }

        memoryProcessVariable.setLowerDispLimit(lowerLimit);
    }

    @Override
    public void setUpperDispLimit(Number upperLimit) {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            displayStructure.getDoubleField("limitHigh").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"display\" field.");
        }

        memoryProcessVariable.setUpperDispLimit(upperLimit);
    }

    @Override
    public void setLowerAlarmLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("lowAlarmLimit").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }

        memoryProcessVariable.setLowerAlarmLimit(lowerLimit);
    }

    @Override
    public void setUpperAlarmLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("highAlarmLimit").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }

        memoryProcessVariable.setUpperAlarmLimit(upperLimit);
    }

    @Override
    public void setLowerWarningLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("lowWarningLimit").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }

        memoryProcessVariable.setLowerWarningLimit(lowerLimit);
    }

    @Override
    public void setUpperWarningLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("highWarningLimit").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }

        memoryProcessVariable.setUpperWarningLimit(upperLimit);
    }

    @Override
    public void setLowerCtrlLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getControl();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("limitLow").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"control\" field.");
        }

        memoryProcessVariable.setLowerCtrlLimit(lowerLimit);
    }

    @Override
    public void setUpperCtrlLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getControl();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("limitHigh").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"control\" field.");
        }

        memoryProcessVariable.setUpperCtrlLimit(upperLimit);
    }

    @Override
    public void setSettable(boolean settable) {
        // Does nothing.
    }
}
