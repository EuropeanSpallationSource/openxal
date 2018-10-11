/**
 * TestChannelWrapper.java
 *
 * Author  : Yngve Levinsen
 * Since   : Jan 10, 2018
 */
package openxal.apps.scanner.test;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.app.scanner.ChannelWrapper;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.BPM;
import xal.smf.impl.Quadrupole;

/**
 * Test cases for class <code>DeviceData</code> and its derived classes.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Mar 31, 2014
 */
public class TestChannelWrapper {


    /*
     * Global Constants
     */

    /** EPICS ID of test device */
    public static final String  MAGNET_FLDSET_HANDLE = "fieldSet";
    public static final String  MAGNET_FLDSET_CHANNEL = "ST3-VC-PS:FldSet";
    public static final String  BPM_AVG_HANDLE = "yAvg";
    public static final String  BPM_AVG_CHANNEL = "BPM2:YAvg";
    public static final long WAIT_TIME = 50; // ms
    public static final long THREAD_WAIT_TIME = 10; // ms

    final static private ChannelFactory CHANNEL_SERVER_FACTORY = ChannelFactory.newServerFactory();

    private static ChannelWrapper CHAN_READ_WRAP;
    private static ChannelWrapper CHAN_SET_WRAP;
    private static SimpleVAThread vaThread;
    /*
     * Global Variables
     */

    /** The device under test */
    public static AcceleratorNode       SMF_DEV;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // Create the Channel to connect to the PV.
        BPM bpm = new BPM("BPM2", CHANNEL_SERVER_FACTORY);
        bpm.channelSuite().putChannel(BPM_AVG_HANDLE, BPM_AVG_CHANNEL, false);


        Quadrupole quad = new Quadrupole("Q1",CHANNEL_SERVER_FACTORY);
        quad.channelSuite().putChannel(MAGNET_FLDSET_HANDLE, MAGNET_FLDSET_CHANNEL, true);

        Channel CHAN_READ = bpm.getChannel(BPM_AVG_HANDLE);
        Channel CHAN_SET  = quad.getChannel(MAGNET_FLDSET_HANDLE);

        CHAN_SET.connectAndWait(1);
        CHAN_READ.connectAndWait(1);

        vaThread = new SimpleVAThread(THREAD_WAIT_TIME,CHAN_READ,CHAN_SET);
        vaThread.start();

        CHAN_READ_WRAP = new ChannelWrapper(CHAN_READ);
        CHAN_SET_WRAP = new ChannelWrapper(CHAN_SET);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        CHAN_READ_WRAP = null;
        CHAN_SET_WRAP = null;
        vaThread.interrupt();
        vaThread = null;
    }

    /**
     * Test method for {@link openxal.apps.scanner}.
     */
    @Test
    public void testChannelWorks() {

        assertTrue( "Set channel is not valid", CHAN_SET_WRAP.getChannel().isValid());
        assertTrue("Read channel is not valid", CHAN_READ_WRAP.getChannel().isValid());
        assertTrue( "Set channel is not connected", CHAN_SET_WRAP.getChannel().isConnected());
        assertTrue("Read channel is not connected", CHAN_READ_WRAP.getChannel().isConnected());
    }

    /**
     * Test method for {@link openxal.apps.scanner}.
     */
    @Test
    public void testChannelReadWrite() throws ConnectionException, GetException, InterruptedException, PutException {
        CHAN_SET_WRAP.getChannel().connectAndWait(0.5);
        CHAN_READ_WRAP.getChannel().connectAndWait(0.5);

        CHAN_SET_WRAP.getChannel().putVal(0.5); // read should end up 2.0375
        TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
        assertTrue("Read channel value is "+CHAN_READ_WRAP.getChannel().getValDbl()+", should be 2.0375", CHAN_READ_WRAP.getChannel().getValDbl()>2.0374);
        assertTrue("Read channel value is "+CHAN_READ_WRAP.getChannel().getValDbl()+", should be 2.0375", CHAN_READ_WRAP.getChannel().getValDbl()<2.0376);
    }
}

/**
 * A very simple VA taking in two channels.
 * The read channel is set to 1.35*x^2-1.2*x+2.3 where x is assumed the set channel
 * Both channels assumed doubles.
 *
 * @author yngvelevinsen
 */
class SimpleVAThread extends Thread {
    long ms;
    double setVal;
    Channel readChan;
    Channel setChan;
    SimpleVAThread(long msec, Channel readChannel, Channel setChannel) throws ConnectionException, GetException {
        this.ms = msec;

        this.readChan = readChannel;
        this.setChan = setChannel;
        this.readChan.connectAndWait();
        this.setChan.connectAndWait();

        this.setVal = setChannel.getValDbl();
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // do a simple edit of the readable channel
                TimeUnit.MILLISECONDS.sleep(this.ms);

                if (setVal != setChan.getValDbl()) {
                    setVal = setChan.getValDbl();
                    readChan.putVal(1.35*setVal*setVal-1.2*setVal+2.3);
                }
            } catch (ConnectionException | GetException | PutException ex) {
                Logger.getLogger(SimpleVAThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
