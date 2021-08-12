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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.tools.dispatch.DispatchQueue;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class AbstractBatchGetRequestTest {

    public AbstractBatchGetRequestTest() {
    }

    /**
     * Test of finalize method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testFinalize() throws Exception, Throwable {
        System.out.println("finalize");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        instance.finalize();

        Field queueField = AbstractBatchGetRequest.class.getDeclaredField("GET_REQUEST_PROCESSING_QUEUE");
        queueField.setAccessible(true);
        DispatchQueue queue = (DispatchQueue) queueField.get(instance);

        assertTrue(queue.isDisposed());
    }

    /**
     * Test of addBatchGetRequestListener method, of class
     * AbstractBatchGetRequest.
     */
    @Test
    public void testAddBatchGetRequestListener() throws IllegalArgumentException, IllegalAccessException {
        System.out.println("addBatchGetRequestListener");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        BatchGetRequestListener<ChannelRecord> listener = new BatchGetRequestListenerImpl();
        instance.addBatchGetRequestListener(listener);
    }

    /**
     * Test of removeBatchGetRequestListener method, of class
     * AbstractBatchGetRequest.
     */
    @Test
    public void testRemoveBatchGetRequestListener() {
        System.out.println("removeBatchGetRequestListener");

        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        BatchGetRequestListener<ChannelRecord> listener = new BatchGetRequestListenerImpl();
        instance.removeBatchGetRequestListener(listener);
    }

    /**
     * Test of addChannel method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testAddChannel() {
        System.out.println("addChannel");
        Channel channel = null;
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        instance.addChannel(channel);

        Collection<Channel> channels = instance.getChannels();

        assertTrue(channels.contains(null));
    }

    /**
     * Test of submitAndWait method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testSubmitAndWait() {
        System.out.println("submit");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());

        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        instance.addChannel(channel);

        boolean res = instance.submitAndWait(0.1);

        assertFalse(res);
    }

    /**
     * Test of await method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testAwait() {
        System.out.println("await");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        boolean result = instance.await(0.1);
        assertTrue(result);
    }

    /**
     * Test of processRequest method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testProcessRequest() {
        System.out.println("processRequest");
        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        instance.processRequest(channel);
        assertTrue(instance.hasExceptions());
    }

    /**
     * Test of isComplete method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testIsComplete() {
        System.out.println("isComplete");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        assertTrue(instance.isComplete());
        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        instance.addChannel(channel);
        instance.submit();
        assertFalse(instance.isComplete());
    }

    /**
     * Test of hasExceptions method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testHasExceptions() {
        System.out.println("hasExceptions");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        assertFalse(instance.hasExceptions());
    }

    /**
     * Test of getRecordCount method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetRecordCount() {
        System.out.println("getRecordCount");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        int expResult = 0;
        int result = instance.getRecordCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getExceptionCount method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetExceptionCount() {
        System.out.println("getExceptionCount");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        int expResult = 0;
        int result = instance.getExceptionCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRecord method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetRecord() {
        System.out.println("getRecord");
        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        Object expResult = null;
        Object result = instance.getRecord(channel);
        assertEquals(expResult, result);
    }

    /**
     * Test of getException method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetException() {
        System.out.println("getException");
        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        Exception expResult = null;
        Exception result = instance.getException(channel);
        assertEquals(expResult, result);
        instance.addChannel(channel);
        instance.submitAndWait(0.1);
        instance.processRequest(channel);
        result = instance.getException(channel);
        assertEquals(ConnectionException.class, result.getClass());
    }

    /**
     * Test of getFailedChannels method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetFailedChannels() {
        System.out.println("getFailedChannels");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        Set<Channel> expResult = new HashSet<>();
        Set<Channel> result = instance.getFailedChannels();
        assertEquals(expResult, result);

        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        instance.addChannel(channel);
        instance.submitAndWait(0.1);
        instance.processRequest(channel);
        result = instance.getFailedChannels();
        expResult.add(channel);
        assertEquals(expResult, result);
    }

    /**
     * Test of getResultChannels method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testGetResultChannels() {
        System.out.println("getResultChannels");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        Set<Channel> expResult = new HashSet<>();
        Set<Channel> result = instance.getResultChannels();
        assertEquals(expResult, result);
    }

    /**
     * Test of processRecordEvent method, of class AbstractBatchGetRequest.
     */
    @Test
    public void testProcessRecordEvent() {
        System.out.println("processRecordEvent");
        Channel channel = null;
        ChannelRecord channelRecord = null;
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());
        instance.processRecordEvent(channel, channelRecord);

        Set<Channel> expResult = new HashSet<>();
        expResult.add(channel);
        Set<Channel> result = instance.getResultChannels();
        assertEquals(expResult, result);
    }

    /**
     * Test of connectionExceptionInBatch method, of class
     * AbstractBatchGetRequest.
     */
    @Test
    public void testConnectionExceptionInBatch() {
        System.out.println("connectionExceptionInBatch");
        Channel channel = ChannelFactory.defaultFactory().getChannel("TEST");
        AbstractBatchGetRequest<ChannelRecord> instance = new AbstractBatchGetRequestImpl(new ArrayList<>());

        assertEquals(0, instance.getExceptionCount());

        instance.connectionExceptionInBatch(null, channel, new ConnectionException());

        assertEquals(1, instance.getExceptionCount());
    }

    private static class AbstractBatchGetRequestImpl extends AbstractBatchGetRequest<ChannelRecord> {

        public AbstractBatchGetRequestImpl(Collection<Channel> channels) {
            super(channels);
        }

        @Override
        protected void requestChannelData(Channel chnl) throws Exception {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class BatchGetRequestListenerImpl implements BatchGetRequestListener<ChannelRecord> {

        public BatchGetRequestListenerImpl() {
        }

        @Override
        public void batchRequestCompleted(AbstractBatchGetRequest<ChannelRecord> abgr, int i, int i1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void exceptionInBatch(AbstractBatchGetRequest<ChannelRecord> abgr, Channel chnl, Exception excptn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void recordReceivedInBatch(AbstractBatchGetRequest<ChannelRecord> abgr, Channel chnl, ChannelRecord rt) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
