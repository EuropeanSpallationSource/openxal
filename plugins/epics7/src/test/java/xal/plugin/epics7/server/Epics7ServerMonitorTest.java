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

import gov.aps.jca.dbr.DBR_CTRL_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_CTRL_String;
import gov.aps.jca.dbr.DBR_Enum;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.pva.MonitorFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.ca.PutException;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;
import static xal.plugin.epics7.Epics7Channel.VALUE_ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.VALUE_REQUEST;
import xal.plugin.epics7.TestMonitor;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerMonitorTest {

    private boolean methodCalled = false;

    /**
     * Test of begin method, of class Epics7ServerMonitor.
     */
    @Test
    public void testBegin() throws ConnectionException, MonitorException {
        System.out.println("begin");
        Epics7ServerChannelFactory channelFactory = new Epics7ServerChannelFactory();
        Channel channel = channelFactory.newChannel("TestChannel");

        Epics7ServerMonitor instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);

        assertEquals(true, methodCalled);

        methodCalled = false;
        instance.clear();
        assertEquals(false, methodCalled);
        instance.begin();

        assertEquals(true, methodCalled);
    }

    /**
     * Test of monitorConnect method, of class Epics7ServerMonitor.
     */
    @Test
    public void testMonitorConnect() throws ConnectionException, MonitorException {
        System.out.println("monitorConnect");
        Epics7ServerChannelFactory channelFactory = new Epics7ServerChannelFactory();
        Channel channel = channelFactory.newChannel("TestChannel");

        Epics7ServerMonitor instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);

        methodCalled = false;

        instance.monitorConnect(null, MonitorFactory.create(instance.record, instance, CreateRequest.create().createRequest(VALUE_REQUEST)), null);

        assertEquals(true, methodCalled);

    }

    /**
     * Test of monitorEvent method, of class Epics7ServerMonitor.
     */
    @Test
    public void testMonitorEvent() throws ConnectionException, MonitorException, PutException {
        System.out.println("monitorEvent");
        Epics7ServerChannelFactory channelFactory = new Epics7ServerChannelFactory();
        Channel channel = channelFactory.newChannel("TestChannel");

        Epics7ServerMonitor instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);

        methodCalled = false;

        instance.monitorEvent(new TestMonitor());

        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback((long) 1, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.monitorEvent(new TestMonitor() {
            boolean poll = true;

            @Override
            public MonitorElement poll() {
                if (poll) {
                    poll = false;
                    return new MonitorElement() {
                        @Override
                        public PVStructure getPVStructure() {
                            Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvLong, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                                    + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                            PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                            PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                            return pvStructure;
                        }

                        @Override
                        public BitSet getChangedBitSet() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public BitSet getOverrunBitSet() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    };
                } else {
                    return null;
                }
            }
        });
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new long[]{1, 2}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.monitorEvent(new TestMonitor() {
            boolean poll = true;

            @Override
            public MonitorElement poll() {
                if (poll) {
                    poll = false;
                    return new MonitorElement() {
                        @Override
                        public PVStructure getPVStructure() {
                            Structure structure = StandardFieldFactory.getStandardField().scalarArray(ScalarType.pvLong, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                                    + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                            PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                            PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                            return pvStructure;
                        }

                        @Override
                        public BitSet getChangedBitSet() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public BitSet getOverrunBitSet() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    };
                } else {
                    return null;
                }
            }
        });
        assertEquals(true, methodCalled);
    }

    /**
     * Test of postEvent method, of class Epics7ServerMonitor.
     */
    @Test

    public void testPostEvent() throws ConnectionException, MonitorException, PutException {
        System.out.println("postEvent");
        Epics7ServerChannelFactory channelFactory = new Epics7ServerChannelFactory();
        Channel channel = channelFactory.newChannel("TestChannel");

        Epics7ServerMonitor instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);

        methodCalled = false;
        instance.postEvent(0, new DBR_Enum());
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new double[]{1, 2}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Double(new double[]{1, 2}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback((byte) 1, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Byte(new byte[]{1}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new byte[]{1, 2}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Byte(new byte[]{1, 2}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(1.0F, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Float(new float[]{1.0F}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new float[]{1.0F, 2.0F}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Float(new float[]{1.0F, 2.0F}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback((int) 1, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Int(new int[]{1}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new int[]{1, 2}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Int(new int[]{1, 2}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback((short) 1, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Short(new short[]{1}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new short[]{1, 2}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_Short(new short[]{1, 2}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback("Test", (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_String(new String[]{"Test"}));
        assertEquals(true, methodCalled);

        methodCalled = false;
        channel.putRawValCallback(new String[]{"Test1", "Test2"}, (e) -> {
        });
        instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
            methodCalled = true;
        }, 0);
        instance.postEvent(0, new DBR_CTRL_String(new String[]{"Test1", "Test2"}));
        assertEquals(true, methodCalled);
    }

    /**
     * Test of canceled method, of class Epics7ServerMonitor.
     */
    @Test
    public void testCanceled() throws ConnectionException, MonitorException {
        System.out.println("canceled");
        Epics7ServerChannelFactory channelFactory = new Epics7ServerChannelFactory();
        Channel channel = channelFactory.newChannel("TestChannel");

        Epics7ServerMonitor instance = (Epics7ServerMonitor) channel.addMonitorValue((c, r) -> {
        }, 0);
        instance.canceled();
    }

}
