/*
 * Copyright (C) 2017 European Spallation Source ERIC
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
package xal.app.modelbrowser;


import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import java.text.MessageFormat;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorController;
import xal.ca.MonitorEventListener;
import xal.ca.Timestamp;


/**
 * Class wrapping EPICS channel name and value to be displayed into a JavaFX table.
 *
 * @author claudiorosati
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class ChannelWrapper extends BaseWrapper {

    private final MonitorController monitorController;
    private final MonitorEventListener monitorListener = new MonitorEventListener() {
        @Override
        public void connectionChanged( Channel channel, boolean connected ) {
        }
        @Override
        public void valueChanged( final Channel channel, final ChannelTimeRecord record ) {
            Platform.runLater(() -> {
                setAlarm(Alarm.values()[record.severity()]);
                setTooltip(tooltipString(channel, record));
                setValue(valueString(channel, record));
            });
        }
    };

    public ChannelWrapper( Channel channel, String name ) {

        setName(name);

        monitorController = new MonitorController(channel);

        monitorController.addMonitorEventListener(monitorListener);
        monitorController.requestMonitor();

    }

    @Override
    public void dispose() {
        monitorController.removeMonitorEventListener(monitorListener);
        monitorController.dispose();
    }

    private String tooltipString( final Channel channel, final ChannelTimeRecord record ) {

        String units = "–";

        try {
            units = channel.getUnits();
        } catch ( ConnectionException | GetException ex ) {
        }

        Timestamp ts = record.getTimestamp();
        String timestamp = StringUtils.defaultIfEmpty(ts.toString(), "–");

        return MessageFormat.format(
            "Value: {0}\nUnit: {1}\nStatus: {2}\nSeverity: {3}\nTimestamp: {4}]",
            record.stringValue(),
            units,
            Status.forValue(record.status()).getName(),
            Severity.forValue(record.severity()).getName(),
            timestamp
        );

    }

    private String valueString( final Channel channel, final ChannelTimeRecord record ) {

        String units = "–";

        try {
            units = channel.getUnits();
        } catch ( ConnectionException | GetException ex ) {
        }

        return MessageFormat.format("{0} [{1}]", record.stringValue(), units);
        
    }

}
