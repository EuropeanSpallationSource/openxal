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
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_CTRL_String;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.plugin.epics7.*;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.MonitorFactory;
import xal.ca.ConnectionException;
import static xal.plugin.epics7.Epics7Channel.VALUE_FIELD;

/**
 * Monitor implementation for Epics7 server.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerMonitor extends Epics7Monitor implements MonitorRequester, ProcessVariableEventCallback {

    protected ProcessVariableEventDispatcher processVariableEventDispatcher;
    protected MemoryProcessVariable memoryProcessVariable;
    protected PVRecord record;

    private Epics7ServerMonitor(EventListener listener, int intMaskEvent) throws ConnectionException {
        super(null, listener, intMaskEvent);
    }

    public static Epics7ServerMonitor createNewMonitor(PVRecord pvRecord, MemoryProcessVariable memoryProcessVariable, String request, EventListener listener, int intMaskEvent) throws ConnectionException {
        Epics7ServerMonitor monitor = new Epics7ServerMonitor(listener, intMaskEvent);

        monitor.createRequest(pvRecord, memoryProcessVariable, request);

        return monitor;
    }

    private void createRequest(PVRecord record, MemoryProcessVariable memoryProcessVariable, String request) {
        this.record = record;
        this.memoryProcessVariable = memoryProcessVariable;

        PVStructure structure = CreateRequest.create().createRequest(request);
        nativeMonitor = MonitorFactory.create(record, this, structure);
        processVariableEventDispatcher = (ProcessVariableEventDispatcher) memoryProcessVariable.getEventCallback();

        processVariableEventDispatcher.registerEventListener(this);
    }

    @Override
    protected void begin() {
        nativeMonitor.start();
        processVariableEventDispatcher.registerEventListener(this);
    }

    @Override
    public void clear() {
        nativeMonitor.stop();
        processVariableEventDispatcher.unregisterEventListener(this);
    }

    // PVA monitor event    @Override
    public void monitorConnect(Status status, Monitor monitor, Structure structure) {
        monitor.start();
    }

    @Override
    public void monitorEvent(Monitor monitor) {
        MonitorElement element;
        while ((element = monitor.poll()) != null) {
            listener.event(element.getPVStructure());

            // Update the CA channel.
            updateMpv(element.getPVStructure());
            monitor.release(element);
        }
    }

    // CA monitor event
    @Override
    public void postEvent(int select, DBR event) {
        PVStructure pvStructure = updatePvRecord(event);
        listener.event(pvStructure);
    }

    @Override
    public void canceled() {
        //
    }

    private PVStructure updatePvRecord(DBR event) {
        PVStructure pvStructure = record.getPVStructure();

        if (event.getType().isBYTE()) {
            updateByteValue(event, pvStructure);
        } else if (event.getType().isDOUBLE()) {
            updateDoubleValue(event, pvStructure);
        } else if (event.getType().isFLOAT()) {
            updateFloatValue(event, pvStructure);
        } else if (event.getType().isINT()) {
            updateIntValue(event, pvStructure);
        } else if (event.getType().isSHORT()) {
            updateShortValue(event, pvStructure);
        } else if (event.getType().isSTRING()) {
            updateStringValue(event, pvStructure);
        }

        return pvStructure;
    }

    private PVStructure updateByteValue(DBR event, PVStructure pvStructure) {
        byte[] byteValue = ((DBR_CTRL_Byte) event).getByteValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getByteField(VALUE_FIELD).put(byteValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST).put(0, byteValue.length, byteValue, 0);
        }
        return pvStructure;
    }

    private PVStructure updateDoubleValue(DBR event, PVStructure pvStructure) {
        double[] doubleValue = ((DBR_CTRL_Double) event).getDoubleValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getDoubleField(VALUE_FIELD).put(doubleValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST).put(0, doubleValue.length, doubleValue, 0);
        }
        return pvStructure;
    }

    private PVStructure updateFloatValue(DBR event, PVStructure pvStructure) {
        float[] floatValue = ((DBR_CTRL_Float) event).getFloatValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getFloatField(VALUE_FIELD).put(floatValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST).put(0, floatValue.length, floatValue, 0);
        }
        return pvStructure;
    }

    private PVStructure updateIntValue(DBR event, PVStructure pvStructure) {
        int[] intValue = ((DBR_CTRL_Int) event).getIntValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getIntField(VALUE_FIELD).put(intValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST).put(0, intValue.length, intValue, 0);
        }
        return pvStructure;
    }

    private PVStructure updateShortValue(DBR event, PVStructure pvStructure) {
        short[] shortValue = ((DBR_CTRL_Short) event).getShortValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getShortField(VALUE_FIELD).put(shortValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST).put(0, shortValue.length, shortValue, 0);
        }
        return pvStructure;
    }

    private PVStructure updateStringValue(DBR event, PVStructure pvStructure) {
        String[] stringValue = ((DBR_CTRL_String) event).getStringValue();
        if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalar) {
            pvStructure.getStringField(VALUE_FIELD).put(stringValue[0]);
        } else if (pvStructure.getSubField(VALUE_FIELD).getField().getType() == Type.scalarArray) {
            pvStructure.getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST).put(0, stringValue.length, stringValue, 0);
        }
        return pvStructure;
    }

    private void updateMpv(PVStructure pvStructure) {
        DBR dbr = null;

        PVField valueField = pvStructure.getSubField(VALUE_FIELD);
        Type type = valueField.getField().getType();
        switch (type) {
            case scalar:
                dbr = getDBR(pvStructure, (PVScalar) valueField);
                break;
            case scalarArray:
                dbr = getDBR(pvStructure, (PVScalarArray) valueField);
                break;
            default:
                break;
        }

        try {
            memoryProcessVariable.write(dbr, null);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DBR getDBR(PVStructure pvStructure, PVScalar pvScalar) {
        ScalarType type = pvScalar.getScalar().getScalarType();
        switch (type) {
            case pvByte:
            case pvUByte:
                return new DBR_Byte(new byte[]{pvStructure.getByteField(VALUE_FIELD).get()});
            case pvDouble:
                return new DBR_Double(new double[]{pvStructure.getDoubleField(VALUE_FIELD).get()});
            case pvFloat:
                return new DBR_Float(new float[]{pvStructure.getFloatField(VALUE_FIELD).get()});
            case pvInt:
            case pvUInt:
                return new DBR_Int(new int[]{pvStructure.getIntField(VALUE_FIELD).get()});
            case pvLong:
            case pvULong:
                return new DBR_Int(new int[]{(int) pvStructure.getLongField(VALUE_FIELD).get()});
            case pvShort:
            case pvUShort:
                return new DBR_Short(new short[]{pvStructure.getShortField(VALUE_FIELD).get()});
            case pvString:
                return new DBR_String(new String[]{pvStructure.getStringField(VALUE_FIELD).get()});
            default:
                break;
        }
        return null;
    }

    private DBR getDBR(PVStructure pvStructure, PVScalarArray pvScalarArray) {
        ScalarType type = pvScalarArray.getScalarArray().getElementType();
        switch (type) {
            case pvByte:
            case pvUByte:
                PVByteArray byteArray = (PVByteArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvByte);
                return new DBR_Byte(byteArray.get().toArray(new byte[byteArray.getLength()]));
            case pvDouble:
                PVDoubleArray doubleArray = (PVDoubleArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvDouble);
                return new DBR_Double(doubleArray.get().toArray(new double[doubleArray.getLength()]));
            case pvFloat:
                PVFloatArray floatArray = (PVFloatArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvFloat);
                return new DBR_Float(floatArray.get().toArray(new float[floatArray.getLength()]));
            case pvInt:
            case pvUInt:
                PVIntArray intArray = (PVIntArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvInt);
                return new DBR_Int(intArray.get().toArray(new int[intArray.getLength()]));
            case pvLong:
            case pvULong:
                PVLongArray longArray = (PVLongArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvLong);
                long[] oldLongArray = longArray.get().toArray(new long[longArray.getLength()]);
                int[] newInt = new int[oldLongArray.length];
                for (int i = 0; i < oldLongArray.length; i++) {
                    newInt[i] = (int) oldLongArray[i];
                }

                return new DBR_Int(newInt);
            case pvShort:
            case pvUShort:
                PVShortArray shortArray = (PVShortArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvShort);
                return new DBR_Short(shortArray.get().toArray(new short[shortArray.getLength()]));
            case pvString:
                PVStringArray stringArray = (PVStringArray) pvStructure.getScalarArrayField(VALUE_FIELD, ScalarType.pvString);
                StringArrayData stringArrayData = new StringArrayData();
                return new DBR_String(stringArray.get(0, stringArray.getLength(), stringArrayData));
            default:
                break;
        }
        return null;
    }
}
