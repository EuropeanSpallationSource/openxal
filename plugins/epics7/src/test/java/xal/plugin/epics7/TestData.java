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

import java.time.Instant;
import org.epics.pva.data.PVABool;
import org.epics.pva.data.PVAByte;
import org.epics.pva.data.PVAData;
import org.epics.pva.data.PVADouble;
import org.epics.pva.data.PVAInt;
import org.epics.pva.data.PVAStructure;
import org.epics.pva.data.nt.PVAAlarm;
import org.epics.pva.data.nt.PVAControl;
import org.epics.pva.data.nt.PVADisplay;
import org.epics.pva.data.nt.PVAScalar;
import org.epics.pva.data.nt.PVATimeStamp;

/**
 * Builders for the normative-type structures that the plugin passes around.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public final class TestData {

    public static final String VALUE = "value";

    private TestData() {
    }

    /**
     * A structure holding only a "value" field.
     */
    public static PVAStructure valueOnly(PVAData value) {
        return new PVAStructure("", PVAScalar.SCALAR_STRUCT_NAME_STRING, value);
    }

    public static PVAStructure doubleRecord(double value) {
        return valueOnly(new PVADouble(VALUE, value));
    }

    /**
     * A structure with "value" and "alarm".
     */
    public static PVAStructure withAlarm(PVAData value, int severity, int status) {
        return new PVAStructure("", PVAScalar.SCALAR_STRUCT_NAME_STRING,
                value,
                new PVAAlarm(new PVAInt("severity", severity),
                        new PVAInt("status", status),
                        new org.epics.pva.data.PVAString("message", "")));
    }

    /**
     * A structure with "value", "alarm" and "timeStamp".
     */
    public static PVAStructure withTime(PVAData value, int severity, int status, Instant time) {
        return new PVAStructure("", PVAScalar.SCALAR_STRUCT_NAME_STRING,
                value,
                new PVAAlarm(new PVAInt("severity", severity),
                        new PVAInt("status", status),
                        new org.epics.pva.data.PVAString("message", "")),
                new PVATimeStamp(time));
    }

    /**
     * A structure carrying the metadata fields that {@link Epics7Channel} reads limits from.
     */
    public static PVAStructure withMetadata(double displayLow, double displayHigh,
            double controlLow, double controlHigh,
            double lowAlarm, double lowWarning, double highWarning, double highAlarm,
            String units) {
        return new PVAStructure("", PVAScalar.SCALAR_STRUCT_NAME_STRING,
                new PVADouble(VALUE, 0.0),
                new PVADisplay(displayLow, displayHigh, "", units, 3, PVADisplay.Form.DEFAULT),
                new PVAControl(controlLow, controlHigh, 0.0),
                new PVAStructure("valueAlarm", "epics:nt/valueAlarm_t:1.0",
                        new PVABool("active", false),
                        new PVADouble("lowAlarmLimit", lowAlarm),
                        new PVADouble("lowWarningLimit", lowWarning),
                        new PVADouble("highWarningLimit", highWarning),
                        new PVADouble("highAlarmLimit", highAlarm),
                        new PVAInt("lowAlarmSeverity", 0),
                        new PVAInt("lowWarningSeverity", 0),
                        new PVAInt("highWarningSeverity", 0),
                        new PVAInt("highAlarmSeverity", 0),
                        new PVAByte("hysteresis", false, (byte) 0)));
    }
}
