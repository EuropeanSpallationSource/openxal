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
package xal.plugin.epics7;

import gov.aps.jca.dbr.CTRL;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.GR;
import gov.aps.jca.dbr.PRECISION;
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.TIME;
import gov.aps.jca.dbr.TimeStamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Converts Channel Access data into the {@link PVAStructure} form used throughout this plugin.
 *
 * pvAccessJava used to provide a "ca" {@code ChannelProvider} that presented Channel Access through the same API as PV
 * Access, normalising DBR values into normative-type structures. The PV Access library used by Phoebus implements PV
 * Access only, so that normalisation is done here instead: the resulting structures mimic NTScalar / NTScalarArray so
 * that {@link Epics7ChannelRecord} and its subclasses can read either protocol.
 *
 * Which optional fields are present depends on what the requested DBR type carries:
 * <ul>
 * <li>plain DBR: value only</li>
 * <li>STS: value, alarm</li>
 * <li>TIME: value, alarm, timeStamp</li>
 * <li>CTRL: value, alarm, display, control, valueAlarm</li>
 * </ul>
 * Channel Access cannot return timestamps and control limits in a single request, which is why the two are never
 * present together.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public final class CaDbrConverter {

    /**
     * Seconds between the UNIX epoch (1970-01-01) and the EPICS epoch (1990-01-01). Channel Access timestamps count
     * from the EPICS epoch; PV Access timestamps count from the UNIX epoch.
     */
    public static final long EPICS_EPOCH = 631152000L;

    private static final String VALUE = "value";
    private static final String SCALAR_TYPE = PVAScalar.SCALAR_STRUCT_NAME_STRING;
    private static final String ARRAY_TYPE = PVAScalar.ARRAY_STRUCT_NAME_STRING;

    private CaDbrConverter() {
    }

    /**
     * Convert a DBR into a structure holding a "value" field plus whatever metadata the DBR carries.
     */
    public static PVAStructure toStructure(DBR dbr) {
        List<PVAData> fields = new ArrayList<>();
        fields.add(toValue(dbr));

        if (dbr.isSTS() && dbr instanceof STS) {
            STS sts = (STS) dbr;
            int severity = sts.getSeverity() == null ? 0 : sts.getSeverity().getValue();
            int status = sts.getStatus() == null ? 0 : sts.getStatus().getValue();
            String message = sts.getStatus() == null ? "" : sts.getStatus().getName();
            fields.add(new PVAAlarm(new PVAInt("severity", severity),
                    new PVAInt("status", status),
                    new PVAString("message", message)));
        }

        if (dbr.isTIME() && dbr instanceof TIME) {
            fields.add(new PVATimeStamp(toInstant(((TIME) dbr).getTimeStamp())));
        }

        // DBR_CTRL_Enum carries labels rather than limits, so it is not a CTRL/GR.
        if (dbr.isCTRL() && dbr instanceof CTRL) {
            CTRL ctrl = (CTRL) dbr;
            int precision = dbr instanceof PRECISION ? ((PRECISION) dbr).getPrecision() : 0;

            fields.add(new PVADisplay(toDouble(ctrl.getLowerDispLimit()), toDouble(ctrl.getUpperDispLimit()),
                    "", nullToEmpty(ctrl.getUnits()), precision, PVADisplay.Form.DEFAULT));
            fields.add(new PVAControl(toDouble(ctrl.getLowerCtrlLimit()), toDouble(ctrl.getUpperCtrlLimit()), 0.0));
            fields.add(valueAlarm(ctrl));
        }

        boolean array = dbr.getCount() > 1;
        return new PVAStructure("", array ? ARRAY_TYPE : SCALAR_TYPE, fields);
    }

    /**
     * Build the "value" field, a scalar when the DBR holds a single element and an array otherwise.
     */
    private static PVAData toValue(DBR dbr) {
        Object value = dbr.getValue();
        boolean scalar = dbr.getCount() <= 1;

        if (value instanceof double[]) {
            double[] v = (double[]) value;
            return scalar ? new PVADouble(VALUE, v[0]) : new PVADoubleArray(VALUE, v);
        }
        if (value instanceof float[]) {
            float[] v = (float[]) value;
            return scalar ? new PVAFloat(VALUE, v[0]) : new PVAFloatArray(VALUE, v);
        }
        if (value instanceof int[]) {
            int[] v = (int[]) value;
            return scalar ? new PVAInt(VALUE, v[0]) : new PVAIntArray(VALUE, false, v);
        }
        if (value instanceof short[]) {
            // Covers DBR_Short as well as DBR_Enum, whose value is the enumeration index.
            short[] v = (short[]) value;
            return scalar ? new PVAShort(VALUE, false, v[0]) : new PVAShortArray(VALUE, false, v);
        }
        if (value instanceof byte[]) {
            byte[] v = (byte[]) value;
            return scalar ? new PVAByte(VALUE, false, v[0]) : new PVAByteArray(VALUE, false, v);
        }
        if (value instanceof String[]) {
            String[] v = (String[]) value;
            return scalar ? new PVAString(VALUE, v[0]) : new PVAStringArray(VALUE, v);
        }
        throw new IllegalArgumentException("Unsupported DBR value type: " + dbr.getType());
    }

    /**
     * The pvData "valueAlarm_t" structure, filled from the DBR's alarm and warning limits.
     */
    private static PVAStructure valueAlarm(GR gr) {
        return new PVAStructure("valueAlarm", "epics:nt/valueAlarm_t:1.0",
                new PVABool("active", false),
                new PVADouble("lowAlarmLimit", toDouble(gr.getLowerAlarmLimit())),
                new PVADouble("lowWarningLimit", toDouble(gr.getLowerWarningLimit())),
                new PVADouble("highWarningLimit", toDouble(gr.getUpperWarningLimit())),
                new PVADouble("highAlarmLimit", toDouble(gr.getUpperAlarmLimit())),
                new PVAInt("lowAlarmSeverity", 0),
                new PVAInt("lowWarningSeverity", 0),
                new PVAInt("highWarningSeverity", 0),
                new PVAInt("highAlarmSeverity", 0),
                new PVAByte("hysteresis", false, (byte) 0));
    }

    /**
     * Convert a Channel Access timestamp to an {@link Instant}, shifting from the EPICS to the UNIX epoch.
     */
    public static Instant toInstant(TimeStamp timeStamp) {
        if (timeStamp == null) {
            return Instant.now();
        }
        return Instant.ofEpochSecond(timeStamp.secPastEpoch() + EPICS_EPOCH, timeStamp.nsec());
    }

    /**
     * The DBR type to request so that the returned DBR carries every field named in the request.
     *
     * @param fieldType the channel's native field type, from {@code gov.aps.jca.Channel#getFieldType()}
     * @param request comma separated field names, e.g. "value,alarm"
     */
    public static DBRType requestType(DBRType fieldType, String request) {
        // Control limits and timestamps cannot be requested together; metadata wins,
        // because a caller asking for "display" has no use for a timestamp.
        if (request.contains(Epics7Channel.DISPLAY_FIELD)
                || request.contains(Epics7Channel.CONTROL_FIELD)
                || request.contains(Epics7Channel.VALUE_ALARM_FIELD)) {
            return controlType(fieldType);
        }
        if (request.contains(Epics7Channel.TIMESTAMP_FIELD)) {
            return timeType(fieldType);
        }
        if (request.contains(Epics7Channel.ALARM_FIELD)) {
            return statusType(fieldType);
        }
        return fieldType;
    }

    private static DBRType controlType(DBRType fieldType) {
        if (fieldType.isBYTE()) {
            return DBRType.CTRL_BYTE;
        }
        if (fieldType.isSHORT()) {
            return DBRType.CTRL_SHORT;
        }
        if (fieldType.isINT()) {
            return DBRType.CTRL_INT;
        }
        if (fieldType.isFLOAT()) {
            return DBRType.CTRL_FLOAT;
        }
        if (fieldType.isDOUBLE()) {
            return DBRType.CTRL_DOUBLE;
        }
        if (fieldType.isENUM()) {
            return DBRType.CTRL_ENUM;
        }
        return DBRType.CTRL_STRING;
    }

    private static DBRType timeType(DBRType fieldType) {
        if (fieldType.isBYTE()) {
            return DBRType.TIME_BYTE;
        }
        if (fieldType.isSHORT()) {
            return DBRType.TIME_SHORT;
        }
        if (fieldType.isINT()) {
            return DBRType.TIME_INT;
        }
        if (fieldType.isFLOAT()) {
            return DBRType.TIME_FLOAT;
        }
        if (fieldType.isDOUBLE()) {
            return DBRType.TIME_DOUBLE;
        }
        if (fieldType.isENUM()) {
            return DBRType.TIME_ENUM;
        }
        return DBRType.TIME_STRING;
    }

    private static DBRType statusType(DBRType fieldType) {
        if (fieldType.isBYTE()) {
            return DBRType.STS_BYTE;
        }
        if (fieldType.isSHORT()) {
            return DBRType.STS_SHORT;
        }
        if (fieldType.isINT()) {
            return DBRType.STS_INT;
        }
        if (fieldType.isFLOAT()) {
            return DBRType.STS_FLOAT;
        }
        if (fieldType.isDOUBLE()) {
            return DBRType.STS_DOUBLE;
        }
        if (fieldType.isENUM()) {
            return DBRType.STS_ENUM;
        }
        return DBRType.STS_STRING;
    }

    /**
     * Convert a "value" field back into the DBR that a Channel Access server serves.
     *
     * Channel Access has no 64 bit integer or boolean type, so longs are narrowed to int and booleans are served as
     * bytes, matching what the previous implementation did.
     */
    public static DBR toDBR(PVAData valueField) {
        if (valueField instanceof PVADouble) {
            return new DBR_Double(new double[]{((PVADouble) valueField).get()});
        }
        if (valueField instanceof PVAFloat) {
            return new DBR_Float(new float[]{((PVAFloat) valueField).get()});
        }
        if (valueField instanceof PVAInt) {
            return new DBR_Int(new int[]{((PVAInt) valueField).get()});
        }
        if (valueField instanceof PVALong) {
            return new DBR_Int(new int[]{(int) ((PVALong) valueField).get()});
        }
        if (valueField instanceof PVAShort) {
            return new DBR_Short(new short[]{((PVAShort) valueField).get()});
        }
        if (valueField instanceof PVAByte) {
            return new DBR_Byte(new byte[]{((PVAByte) valueField).get()});
        }
        if (valueField instanceof PVABool) {
            return new DBR_Byte(new byte[]{(byte) (((PVABool) valueField).get() ? 1 : 0)});
        }
        if (valueField instanceof PVAString) {
            return new DBR_String(new String[]{((PVAString) valueField).get()});
        }
        if (valueField instanceof PVADoubleArray) {
            return new DBR_Double(((PVADoubleArray) valueField).get());
        }
        if (valueField instanceof PVAFloatArray) {
            return new DBR_Float(((PVAFloatArray) valueField).get());
        }
        if (valueField instanceof PVAIntArray) {
            return new DBR_Int(((PVAIntArray) valueField).get());
        }
        if (valueField instanceof PVALongArray) {
            long[] longs = ((PVALongArray) valueField).get();
            int[] ints = new int[longs.length];
            for (int i = 0; i < longs.length; i++) {
                ints[i] = (int) longs[i];
            }
            return new DBR_Int(ints);
        }
        if (valueField instanceof PVAShortArray) {
            return new DBR_Short(((PVAShortArray) valueField).get());
        }
        if (valueField instanceof PVAByteArray) {
            return new DBR_Byte(((PVAByteArray) valueField).get());
        }
        if (valueField instanceof PVAStringArray) {
            return new DBR_String(((PVAStringArray) valueField).get());
        }
        throw new IllegalArgumentException("Cannot serve " + valueField + " over Channel Access");
    }

    /**
     * Extract a "value" field as a plain Java scalar or array.
     */
    public static Object toJava(PVAData valueField) {
        if (valueField instanceof PVADouble) {
            return ((PVADouble) valueField).get();
        }
        if (valueField instanceof PVAFloat) {
            return ((PVAFloat) valueField).get();
        }
        if (valueField instanceof PVAInt) {
            return ((PVAInt) valueField).get();
        }
        if (valueField instanceof PVALong) {
            return ((PVALong) valueField).get();
        }
        if (valueField instanceof PVAShort) {
            return ((PVAShort) valueField).get();
        }
        if (valueField instanceof PVAByte) {
            return ((PVAByte) valueField).get();
        }
        if (valueField instanceof PVABool) {
            return ((PVABool) valueField).get();
        }
        if (valueField instanceof PVAString) {
            return ((PVAString) valueField).get();
        }
        if (valueField instanceof PVADoubleArray) {
            return ((PVADoubleArray) valueField).get();
        }
        if (valueField instanceof PVAFloatArray) {
            return ((PVAFloatArray) valueField).get();
        }
        if (valueField instanceof PVAIntArray) {
            return ((PVAIntArray) valueField).get();
        }
        if (valueField instanceof PVALongArray) {
            return ((PVALongArray) valueField).get();
        }
        if (valueField instanceof PVAShortArray) {
            return ((PVAShortArray) valueField).get();
        }
        if (valueField instanceof PVAByteArray) {
            return ((PVAByteArray) valueField).get();
        }
        if (valueField instanceof PVAStringArray) {
            return ((PVAStringArray) valueField).get();
        }
        throw new IllegalArgumentException("Unsupported value field " + valueField);
    }

    private static double toDouble(Number number) {
        return number == null ? 0.0 : number.doubleValue();
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
