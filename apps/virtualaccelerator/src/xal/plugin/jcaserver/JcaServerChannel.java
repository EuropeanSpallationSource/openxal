package xal.plugin.jcaserver;

import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;

import java.math.BigDecimal;

import com.cosylab.epics.caj.cas.ProcessVariableEventDispatcher;

import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.ca.TimeAdaptor;
import xal.ca.ValueAdaptor;
import xal.tools.ArrayValue;

/**
 * JcaServerChannel using rawProcessVariables instead of ChanneServerPVs. Used on local machine with server.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */

public class JcaServerChannel extends Channel {


    private ServerMemoryProcessVariable pv;
    private ProcessVariableEventDispatcher pved;;

    /** size for array PVs */
    public static final int DEFAULT_ARRAY_SIZE = 1024;

    private final int size;

    JcaServerChannel(String signal, JcaServer channelServer) {
        super(signal);
        m_strId = signal;
        size = signal.matches(".*(TBT|A)") ? DEFAULT_ARRAY_SIZE : 1;
        pv = channelServer.registerRawPV(signal, new double[size]);
        pv.setUnits("units");
        
        pved = new ProcessVariableEventDispatcher(pv);
        pv.setEventCallback(pved);
        
        connectionFlag = true;

        if (size == 1) {
            final String[] warningPVs = getWarningLimitPVs();
            channelServer.registerRawPV(warningPVs[0], 0); // TODO is this really needed? 
            channelServer.registerRawPV(warningPVs[1], 0);

            final String[] alarmPVs = getAlarmLimitPVs();
            channelServer.registerRawPV(alarmPVs[0], 0);
            channelServer.registerRawPV(alarmPVs[1], 0);

            final String[] operationLimitPVs = getOperationLimitPVs();
            channelServer.registerRawPV(operationLimitPVs[0], pv.getLowerDispLimit().doubleValue());
            channelServer.registerRawPV(operationLimitPVs[1], pv.getUpperDispLimit().doubleValue());

            final String[] driveLimitPVs = getDriveLimitPVs();
            channelServer.registerRawPV(driveLimitPVs[0], pv.getLowerCtrlLimit().doubleValue());
            channelServer.registerRawPV(driveLimitPVs[1], pv.getUpperCtrlLimit().doubleValue());
        }

    }

    @Override
    public boolean connectAndWait(double timeout) {
        // We are locally connected
        return isConnected();
    }

    public void requestConnection() {
        connectionFlag = true;        
        if (connectionProxy != null) {
            connectionProxy.connectionMade(this);
        }
    }

    @Override
    public void disconnect() {
        // Nothing to do here
    }

    @Override
    public Class<?> elementType() throws ConnectionException {
        return pv.getValue().getClass().getComponentType();
    }

    @Override
    public int elementCount() throws ConnectionException {
        return size;
    }

    @Override
    public boolean readAccess() throws ConnectionException {
        return true;
    }

    @Override
    public boolean writeAccess() throws ConnectionException {
        return true;
    }

    @Override
    public String getUnits() throws ConnectionException, GetException {
        return "units";
    }

    /**
     * Get the lower and upper operation limit PVs
     * 
     * @return two element array of PVs with the lower and upper limit PVs
     */
    public String[] getOperationLimitPVs() {
        return constructLimitPVs("LOPR", "HOPR");
    }

    /**
     * Get the lower and upper warning limit PVs
     * 
     * @return two element array of PVs with the lower and upper limit PVs
     */
    public String[] getWarningLimitPVs() {
        return constructLimitPVs("LOW", "HIGH");
    }

    /**
     * Get the lower and upper alarm limit PVs
     * 
     * @return two element array of PVs with the lower and upper limit PVs
     */
    public String[] getAlarmLimitPVs() {
        return constructLimitPVs("LOLO", "HIHI");
    }

    /**
     * Get the lower and upper drive limit PVs
     * 
     * @return two element array of PVs with the lower and upper limit PVs
     */
    public String[] getDriveLimitPVs() {
        return constructLimitPVs("DRVL", "DRVH");
    }

    /**
     * Construct the lower and upper limit PVs from the lower and upper suffixes
     * 
     * @return two element array of PVs with the lower and upper limit PVs
     */
    private String[] constructLimitPVs(final String lowerSuffix, final String upperSuffix) {
        final String[] rangePVs = new String[2];
        rangePVs[0] = channelName() + "." + lowerSuffix;
        rangePVs[1] = channelName() + "." + upperSuffix;
        return rangePVs;
    }

    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        return pv.getUpperDispLimit();
    }

    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        return pv.getLowerDispLimit();
    }

    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        return pv.getUpperAlarmLimit();
    }

    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        return pv.getLowerAlarmLimit();
    }

    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        return pv.getUpperWarningLimit();
    }

    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        return pv.getLowerWarningLimit();
    }

    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        return pv.getUpperCtrlLimit();
    }

    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        return pv.getLowerCtrlLimit();
    }

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() {
        return new ChannelTimeRecord(new TimeAdaptor() {
        	final BigDecimal EPOCH_SECONDS_OFFSET = new BigDecimal( 7305*24*3600 );     // offset from standard Java epoch

        	private ArrayValue value = ArrayValue.arrayValueFromArray(pv.getValue());
        	private BigDecimal timestamp = pv.getTimestamp().asBigDecimal().add( EPOCH_SECONDS_OFFSET ).setScale( 9, BigDecimal.ROUND_HALF_UP );
        	
			@Override
			public int status() {					
				return Status.NO_ALARM.getValue();
			}

			@Override
			public int severity() {
				return Severity.NO_ALARM.getValue();
			}

			@Override
			public ArrayValue getStore() {
				return value;
			}

			@Override
			public BigDecimal getTimestamp() {					
				return timestamp;
			}            	
        });
    }

    @Override
    public void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        getRawValueCallback(listener, true);
    }

    @Override
    public void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException,
            GetException {
        if (listener != null) {
            listener.eventValue(new ChannelRecord(new ValueAdaptor() {				
				@Override
				public ArrayValue getStore() {
					return ArrayValue.arrayValueFromArray(pv.getValue());
				}
			}), this);
        }
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection)
            throws ConnectionException, GetException {        
        if (listener != null) {
            listener.eventValue(getRawTimeRecord(), this);
        }

    }

    
    @Override
    public Monitor addMonitorValTime(final IEventSinkValTime listener, final int intMaskFire) throws ConnectionException,
            MonitorException {
    	return new JcaServerMonitor(listener, intMaskFire);
    }

    @Override
    public Monitor addMonitorValStatus(final IEventSinkValStatus listener, int intMaskFire) throws ConnectionException,
            MonitorException {
    	return new JcaServerMonitor(new IEventSinkValTime() {
			@Override
			public void eventValue(ChannelTimeRecord record, Channel chan) {
				listener.eventValue(record, chan);
			}
    	}, intMaskFire);
    }

    @Override
    public Monitor addMonitorValue(final IEventSinkValue listener, int intMaskFire) throws ConnectionException,
            MonitorException {
    	return new JcaServerMonitor(new IEventSinkValTime() {
			@Override
			public void eventValue(ChannelTimeRecord record, Channel chan) {
				listener.eventValue(record, chan);
			}
    	}, intMaskFire);
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new String[] { newVal }, listener);
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new byte[] { newVal }, listener);
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new short[] { newVal }, listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new int[] { newVal }, listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new float[] { newVal }, listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(new double[] { newVal }, listener);

    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        listener.putCompleted(this);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        if (listener != null) {
            listener.putCompleted(this);
        }
    }

    public void putRawValCallback(String[] newVal, PutListener listener) throws ConnectionException, PutException {
        pv.setValue(newVal);
        if (listener != null) {
            listener.putCompleted(this);
        }
    }
    
    private class JcaServerMonitor extends Monitor implements ProcessVariableEventCallback {
    	private int maskEvent;
    	private IEventSinkValTime listener;
		
    	protected JcaServerMonitor(IEventSinkValTime listener, int intMaskEvent)
				throws ConnectionException {
			super(JcaServerChannel.this, intMaskEvent);
			this.listener = listener;
			this.maskEvent = intMaskEvent;
		}
		
    	@Override
		public void postEvent(int select, DBR event) {
			if ((select & maskEvent) != 0)
				listener.eventValue(getRawTimeRecord(), JcaServerChannel.this);
		}
	
		@Override
		public void canceled() {
		}
		
		@Override
		protected void begin() throws MonitorException {
			pved.registerEventListener(this);
		}

		@Override
		public void clear() {
			pved.unregisterEventListener(this);			
		}
    }
}
