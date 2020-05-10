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

import java.util.logging.Logger;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
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
 * Server channel implementation. Only PVAccess for the moment.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannel extends Epics7Channel implements IServerChannel {

    // Record associated with this channel.    
    private PVRecord pvRecord;
    private final Epics7ServerChannelSystem CHANNEL_SYSTEM;

    private static final String PROPERTIES = ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
            + DISPLAY_FIELD + "," + CONTROL_FIELD;

    public Epics7ServerChannel(String signalName, Epics7ServerChannelSystem CHANNEL_SYSTEM) {
        super(signalName, CHANNEL_SYSTEM);

        // Removing protocol in case it is defined.
        if (m_strId.startsWith("ca://") || m_strId.startsWith("pva://")) {
            m_strId = m_strId.substring(m_strId.indexOf("://") + 3);
        }

        this.CHANNEL_SYSTEM = CHANNEL_SYSTEM;

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
            addRecord(ScalarType.pvDouble, false);
            connectionFlag = true;
        }
    }

    @Override
    public void disconnect() {
        removeRecord();
        connectionFlag = false;
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

        CHANNEL_SYSTEM.addRecord(pvRecord);
    }

    private void removeRecord() {
        if (pvRecord != null) {
            CHANNEL_SYSTEM.removeRecord(pvRecord);
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

    private PVStructure getDisplay() {
        if (pvRecord != null) {
            return pvRecord.getPVStructure().getStructureField(DISPLAY_FIELD);
        }
        return null;
    }

    private PVStructure getVAlueAlarm() {
        if (pvRecord != null) {
            return pvRecord.getPVStructure().getStructureField(VALUE_ALARM_FIELD);
        }
        return null;
    }

    private PVStructure getControl() {
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

        return Epics7ServerMonitor.createNewMonitor(pvRecord, Epics7Channel.TIME_REQUEST, (pvStructure) -> {
            ChannelTimeRecord record = new Epics7ChannelTimeRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws ConnectionException, MonitorException {
        checkConnection("addMonitorValStatus");

        return Epics7ServerMonitor.createNewMonitor(pvRecord, Epics7Channel.STATUS_REQUEST, (pvStructure) -> {
            ChannelStatusRecord record = new Epics7ChannelStatusRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws ConnectionException, MonitorException {
        checkConnection("addMonitorValue");

        return Epics7ServerMonitor.createNewMonitor(pvRecord, Epics7Channel.VALUE_REQUEST, (pvStructure) -> {
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
            addRecord(ScalarType.pvString, false);
        }

        pvRecord.getPVStructure().getStringField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != byte.class) {
            addRecord(ScalarType.pvByte, false);
        }

        pvRecord.getPVStructure().getByteField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != short.class) {
            addRecord(ScalarType.pvShort, false);
        }

        pvRecord.getPVStructure().getShortField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != int.class) {
            addRecord(ScalarType.pvInt, false);
        }

        pvRecord.getPVStructure().getIntField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != float.class) {
            addRecord(ScalarType.pvFloat, false);
        }

        pvRecord.getPVStructure().getFloatField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != double.class) {
            addRecord(ScalarType.pvDouble, false);
        }

        pvRecord.getPVStructure().getDoubleField(VALUE_FIELD).put(newVal);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != String[].class) {
            addRecord(ScalarType.pvString, true);
        }

        pvRecord.getPVStructure().getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != byte[].class) {
            addRecord(ScalarType.pvByte, true);
        }

        pvRecord.getPVStructure().getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != short[].class) {
            addRecord(ScalarType.pvShort, true);
        }

        pvRecord.getPVStructure().getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != int[].class) {
            addRecord(ScalarType.pvInt, true);
        }

        pvRecord.getPVStructure().getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != float[].class) {
            addRecord(ScalarType.pvFloat, true);
        }

        pvRecord.getPVStructure().getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

        updateTimeStampAlarmsAndTriggerListener(listener);
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (elementType() != double[].class) {
            addRecord(ScalarType.pvDouble, true);
        }

        pvRecord.getPVStructure().getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST).put(0, newVal.length, newVal, 0);

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
    }

    @Override
    public void setLowerDispLimit(Number lowerLimit) {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            displayStructure.getDoubleField("limitLow").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find \"display\" field.");
        }
    }

    @Override
    public void setUpperDispLimit(Number upperLimit) {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            displayStructure.getDoubleField("limitHigh").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"display\" field.");
        }
    }

    @Override
    public void setLowerAlarmLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("lowAlarmLimit").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }
    }

    @Override
    public void setUpperAlarmLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("highAlarmLimit").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }
    }

    @Override
    public void setLowerWarningLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("lowWarningLimit").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }
    }

    @Override
    public void setUpperWarningLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("highWarningLimit").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"valueAlarm\" field.");
        }
    }

    @Override
    public void setLowerCtrlLimit(Number lowerLimit) {
        PVStructure alarmValueStructure = getControl();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("limitLow").put(lowerLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"control\" field.");
        }
    }

    @Override
    public void setUpperCtrlLimit(Number upperLimit) {
        PVStructure alarmValueStructure = getControl();
        if (alarmValueStructure != null) {
            alarmValueStructure.getDoubleField("limitHigh").put(upperLimit.doubleValue());
        } else {
            Logger.getLogger(Epics7ServerChannel.class.getName()).severe("Couldn't find  \"control\" field.");
        }
    }

    @Override
    public void setSettable(boolean settable) {
        // Does nothing.
    }
}
