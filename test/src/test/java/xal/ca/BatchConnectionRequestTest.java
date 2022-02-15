/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal.ca;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.tools.dispatch.DispatchQueue;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class BatchConnectionRequestTest {

    private List<Channel> connectedChannels;
    private Channel connectedChannel;

    private List<Channel> disconnectedChannels;
    private Channel disconnectedChannel;

    public BatchConnectionRequestTest() {
        connectedChannels = new ArrayList<>();
        connectedChannel = new TestChannelAutoConnect("TEST");
        connectedChannels.add(connectedChannel);

        disconnectedChannels = new ArrayList<>();
        disconnectedChannel = ChannelFactory.defaultFactory().getChannel("TEST");
        disconnectedChannels.add(disconnectedChannel);
    }

    /**
     * Test of finalize method, of class BatchConnectionRequest.
     */
    @Test
    public void testFinalize() throws Exception, Throwable {
        System.out.println("finalize");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        instance.finalize();

        Field queueField = BatchConnectionRequest.class.getDeclaredField("resourceSyncQueue");
        queueField.setAccessible(true);
        DispatchQueue queue = (DispatchQueue) queueField.get(instance);

        assertTrue(queue.isDisposed());
    }

    /**
     * Test of addBatchConnectionRequestListener method, of class
     * BatchConnectionRequest.
     */
    @Test
    public void testAddBatchConnectionRequestListener() {
        System.out.println("addBatchConnectionRequestListener");
        BatchConnectionRequestListener listener = new BatchConnectionRequestListenerImpl();
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        instance.addBatchConnectionRequestListener(listener);
    }

    /**
     * Test of removeBatchConnectionRequestListener method, of class
     * BatchConnectionRequest.
     */
    @Test
    public void testRemoveBatchConnectionRequestListener() {
        System.out.println("removeBatchConnectionRequestListener");
        BatchConnectionRequestListener listener = new BatchConnectionRequestListenerImpl();
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        instance.removeBatchConnectionRequestListener(listener);
    }

    /**
     * Test of getChannels method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetChannels() {
        System.out.println("getChannels");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        Set<Channel> result = instance.getChannels();
        assertTrue(result.contains(connectedChannel));
    }

    /**
     * Test of getChannelCount method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetChannelCount() {
        System.out.println("getChannelCount");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        int expResult = 0;
        int result = instance.getChannelCount();
        assertEquals(expResult, result);

        instance = new BatchConnectionRequest(connectedChannels);
        expResult = 1;
        result = instance.getChannelCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectedChannels method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetConnectedChannels() {
        System.out.println("getConnectedChannels");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        Set<Channel> result = instance.getConnectedChannels();
        assertTrue(result.isEmpty());
    }

    /**
     * Test of getConnectedCount method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetConnectedCount() {
        System.out.println("getConnectedCount");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        int expResult = 0;
        int result = instance.getConnectedCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDisconnectedChannels method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetDisconnectedChannels() {
        System.out.println("getDisconnectedChannels");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        Set<Channel> result = instance.getDisconnectedChannels();
        assertTrue(result.isEmpty());

        instance = new BatchConnectionRequest(disconnectedChannels);
        instance.submitAndWait(0.1);
        result = instance.getPendingChannels();
        assertTrue(result.contains(disconnectedChannel));

        instance = new BatchConnectionRequest(connectedChannels);
        instance.submitAndWait(0.1);
        result = instance.getPendingChannels();
        assertTrue(result.isEmpty());
    }

    /**
     * Test of getDisconnectedCount method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetDisconnectedCount() {
        System.out.println("getDisconnectedCount");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        int expResult = 0;
        int result = instance.getDisconnectedCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPendingChannels method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetPendingChannels() {
        System.out.println("getPendingChannels");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        Set<Channel> result = instance.getPendingChannels();
        assertTrue(result.isEmpty());

        instance = new BatchConnectionRequest(disconnectedChannels);
        instance.submit();
        result = instance.getPendingChannels();
        assertTrue(result.contains(disconnectedChannel));

        instance = new BatchConnectionRequest(connectedChannels);
        instance.submit();
        result = instance.getPendingChannels();
        assertTrue(result.isEmpty());
    }

    /**
     * Test of getException method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetException() {
        System.out.println("getException");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        instance.submitAndWait(0.1);

        Exception result = instance.getException(connectedChannel);
        assertEquals(null, result);
    }

    /**
     * Test of getFailedChannels method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetFailedChannels() {
        System.out.println("getFailedChannels");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        instance.submitAndWait(0.1);

        Set<Channel> result = instance.getFailedChannels();
        assertTrue(result.isEmpty());
    }

    /**
     * Test of getExceptionCount method, of class BatchConnectionRequest.
     */
    @Test
    public void testGetExceptionCount() {
        System.out.println("getExceptionCount");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        instance.submitAndWait(0.1);
        int expResult = 0;
        int result = instance.getExceptionCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of await method, of class BatchConnectionRequest.
     */
    @Test
    public void testAwait() {
        System.out.println("await");
        double timeout = 0.1;
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        boolean expResult = true;
        boolean result = instance.await(timeout);
        assertEquals(expResult, result);
    }

    /**
     * Test of isCanceled method, of class BatchConnectionRequest.
     */
    @Test
    public void testIsCanceled() {
        System.out.println("isCanceled");
        BatchConnectionRequest instance = new BatchConnectionRequest(connectedChannels);
        boolean expResult = false;
        boolean result = instance.isCanceled();
        assertEquals(expResult, result);

        instance.cancel();
        expResult = true;
        result = instance.isCanceled();
        assertEquals(expResult, result);
    }

    /**
     * Test of isComplete method, of class BatchConnectionRequest.
     */
    @Test
    public void testIsComplete() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("isComplete");
        BatchConnectionRequest instance = new BatchConnectionRequest(new ArrayList<>());
        instance.submit();
        boolean expResult = true;
        boolean result = instance.isComplete();
        assertEquals(expResult, result);

        instance = new BatchConnectionRequest(connectedChannels);
        instance.submit();
        expResult = true;
        result = instance.isComplete();
        assertEquals(expResult, result);

        instance = new BatchConnectionRequest(disconnectedChannels);
        instance.submit();
        expResult = false;
        result = instance.isComplete();
        assertEquals(expResult, result);

    }

    private static class BatchConnectionRequestListenerImpl implements BatchConnectionRequestListener {

        public BatchConnectionRequestListenerImpl() {
        }

        @Override
        public void batchConnectionRequestCompleted(BatchConnectionRequest bcr, int i, int i1, int i2) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void connectionExceptionInBatch(BatchConnectionRequest bcr, Channel chnl, Exception excptn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void connectionChangeInBatch(BatchConnectionRequest bcr, Channel chnl, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
