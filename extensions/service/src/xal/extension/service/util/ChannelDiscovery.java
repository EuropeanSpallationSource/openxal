package xal.extension.service.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.impl.ConfigurationFactory;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

public class ChannelDiscovery {

    private static final Logger LOGGER = Logger.getLogger(ChannelDiscovery.class.getName());
    private static final long DEFAULT_TIMEOUT = 200; // timeout in milliseconds
    
    /* This class should not be instanced */
    private ChannelDiscovery() {};
    
    private static boolean send(DatagramChannel channel, 
            InetSocketAddress[] sendAddresses, ByteBuffer buffer) 
    {
        // noop check
        if (sendAddresses == null)
            return false;
            
        for (int i = 0; i < sendAddresses.length; i++)
        {
            try
            {
                // prepare buffer
                buffer.flip();
                channel.send(buffer, sendAddresses[i]);
            }
            catch (NoRouteToHostException nrthe)
            {
                System.err.println("No route to host exception caught when sending to: " + sendAddresses[i] + ".");
                continue;
            }
            catch (Throwable ex) 
            {
                ex.printStackTrace();
                return false;
            }
        }
        
        return true;
    }
    
    private final static void processSearchResponse(InetSocketAddress responseFrom, ByteBuffer socketBuffer,
            Set<Server> serverSet) throws IOException
    {
        // magic code
        final byte magicCode = socketBuffer.get();
        
        /* byte version = */ socketBuffer.get();

        // flags
        byte flags = socketBuffer.get();
        if ((flags & 0x80) == 0x80)
            socketBuffer.order(ByteOrder.BIG_ENDIAN);
        else
            socketBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // command
        byte command = socketBuffer.get();
        if (command != 0x04) 
            return;
        
        // read payload size
        int payloadSize = socketBuffer.getInt();
        if (payloadSize < (12+4+16+2))
            return;
        
        // check magic code
        if (magicCode != PVAConstants.PVA_MAGIC)
            return;
        
        // 12-byte GUID
        byte[] guid = new byte[12]; 
        socketBuffer.get(guid);

        socketBuffer.getInt();

        // 128-bit IPv6 address
        byte[] byteAddress = new byte[16]; 
        socketBuffer.get(byteAddress);
    
        final int port = socketBuffer.getShort() & 0xFFFF;
        
        // NOTE: Java knows how to compare IPv4/IPv6 :)
        
        InetAddress addr;
        try {
            addr = InetAddress.getByAddress(byteAddress);
        } catch (UnknownHostException e) {
            return;
        }

        // accept given address if explicitly specified by sender
        if (!addr.isAnyLocalAddress())
            responseFrom = new InetSocketAddress(addr, port);
        else
            responseFrom = new InetSocketAddress(responseFrom.getAddress(), port);
        
        socketBuffer.get();

        serverSet.add(new Server(responseFrom));
    }

    private static Set<Server> getServers() throws IOException {
        final ConfigurationProvider configurationProvider = ConfigurationFactory.getProvider();
        Configuration config = configurationProvider.getConfiguration("pvAccess-client");
        if (config == null)
            config = configurationProvider.getConfiguration("system");

        String addressList = config.getPropertyAsString("EPICS_PVA_ADDR_LIST", "");
        boolean autoAddressList = config.getPropertyAsBoolean("EPICS_PVA_AUTO_ADDR_LIST", true);
        int broadcastPort = config.getPropertyAsInteger("EPICS_PVA_BROADCAST_PORT", PVAConstants.PVA_BROADCAST_PORT);
        
        // where to send address
        InetSocketAddress[] broadcastAddresses = InetAddressUtil.getBroadcastAddresses(broadcastPort);
        
        // set broadcast address list
        if (addressList != null && addressList.length() > 0)
        {
            // if auto is true, add it to specified list
            InetSocketAddress[] appendList = null;
            if (autoAddressList == true)
                appendList = broadcastAddresses;
            
            broadcastAddresses = InetAddressUtil.getSocketAddressList(addressList, broadcastPort, appendList);
        }
        
        LOGGER.info("Searching for servers");

        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(true);
        datagramChannel.socket().setBroadcast(true);
        datagramChannel.socket().setSoTimeout(1000);    // 1 sec
        datagramChannel.bind(new InetSocketAddress(0));

        
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        
        sendBuffer.put(PVAConstants.PVA_MAGIC);
        sendBuffer.put(PVAConstants.PVA_VERSION);
        sendBuffer.put((byte)0x80); // big endian
        sendBuffer.put((byte)0x03); // search
        sendBuffer.putInt(4+1+3+16+2+1+2);      // payload size
        
        sendBuffer.putInt(0);       // sequenceId
        sendBuffer.put((byte)0x01); // reply required & broadcast
        sendBuffer.put((byte)0);        // reserved
        sendBuffer.putShort((short)0);  // reserved

        // NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
        InetSocketAddress address = (InetSocketAddress)datagramChannel.getLocalAddress();
        InetAddressUtil.encodeAsIPv6Address(sendBuffer, address.getAddress());
        sendBuffer.putShort((short)address.getPort());
        
        sendBuffer.put((byte)0x00); // no restriction on protocol
        sendBuffer.putShort((byte)0x00);    // count

        
        send(datagramChannel, broadcastAddresses, sendBuffer);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
        
        DatagramPacket dp = new DatagramPacket(receiveBuffer.array(), receiveBuffer.capacity());
        
        Set<Server> serverSet = new HashSet<>();
        while (true)
        {
            SocketAddress responseFrom;
            try
            {
                datagramChannel.socket().receive(dp);
                responseFrom = dp.getSocketAddress();
                receiveBuffer.position(dp.getLength());
            }
            catch (SocketTimeoutException ste) {
                break;
            }
            
            receiveBuffer.flip();
            processSearchResponse((InetSocketAddress)responseFrom, receiveBuffer, serverSet);
        }
        
        return serverSet;
    }

    public static Collection<String> getChannels(Function<String, Boolean> f) {
        return getChannels(DEFAULT_TIMEOUT, f);
    }

    public static Collection<String> getChannels(long timeout, Function<String, Boolean> f) {

        Set<Server> servers;

        try {
            servers = getServers();
        } catch (IOException e) {
            LOGGER.severe("Could not retrieve servers from pvaccess.");
            return new HashSet<>();
        }

        CountDownLatch serversLatch = new CountDownLatch(servers.size());
        Collection<String> channels = Collections.synchronizedCollection(new HashSet<>());

        servers.forEach(srv -> srv.getChannels(serversLatch, channels, f));

        try {
            if (serversLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                LOGGER.info("Recieved channel list from channels.");
            } else {
                LOGGER.warning("Only recieved channels from " + (servers.size()-serversLatch.getCount()) + " out of " +
                        servers.size() + " servers.");
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Channel discovery interrupted with: \n" + e.getMessage());
        }
        
        return channels;
    }

    private static class Server {
        private final String authority;
        
        public Server(InetSocketAddress addr) {
           this.authority = addr.getHostName() + ":" + addr.getPort(); 
        }
        
        private PVStructure getRequest() {
            final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
            final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

            Structure structure = fieldCreate.createFieldBuilder().
                                add("scheme", ScalarType.pvString).
                                add("authority", ScalarType.pvString).
                                add("path", ScalarType.pvString).
                                addNestedStructure("query").
                                    add("op", ScalarType.pvString).endNested().
                                createStructure();
            
            Structure structureWithHeader = fieldCreate.createStructure("epics:nt/NTURI:1.0", 
                    structure.getFieldNames(), structure.getFields());
            
            PVStructure pvs = pvDataCreate.createPVStructure(structureWithHeader);
            pvs.getStringField("scheme").put("pva");
            pvs.getStringField("authority").put(authority);
            pvs.getStringField("path").put("server");
            pvs.getStructureField("query").getStringField("op").put("channels");

            return pvs;
        }
        
        public void getChannels(CountDownLatch serversLatch, Collection<String> channels, Function<String, Boolean> f) {

            ChannelProvider channelProvider = ChannelProviderRegistryFactory.getChannelProviderRegistry().
                    createProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);
            
            ChannelRequesterImpl requester = new ChannelRequesterImpl();
            Channel c = channelProvider.createChannel("server", requester, ChannelProvider.PRIORITY_DEFAULT, authority);

            if (requester.isConnected(DEFAULT_TIMEOUT)) {
                ChannelRPCRequesterImpl rpcRequester = new ChannelRPCRequesterImpl(serversLatch, channels, f);
                ChannelRPC channelRpc = c.createChannelRPC(rpcRequester, CreateRequest.create().createRequest("field()")); 
                if (rpcRequester.isConnected(DEFAULT_TIMEOUT)) {
                    channelRpc.lastRequest();
                    channelRpc.request(getRequest());
                } else {
                    channelRpc.cancel();
                }
            }
        }
    }
    
    private static class ChannelRPCRequesterImpl implements ChannelRPCRequester {
        
        private final CountDownLatch connectionLatch = new CountDownLatch(1);
        private final CountDownLatch rpcLatch;
        private final Collection<String> channels;
        private final Function<String, Boolean> filterFunction;
        
        public ChannelRPCRequesterImpl(CountDownLatch latch, Collection<String> channels, Function<String, Boolean> f) {
            this.rpcLatch = latch;
            this.channels = channels;
            this.filterFunction = f;
        }

        @Override
        public String getRequesterName() {
            return ChannelRPCRequesterImpl.class.getName();
        }

        @Override
        public void message(String message, MessageType messageType) {
        }

        @Override
        public void channelRPCConnect(Status status, ChannelRPC channelRPC) {
            if (status.isSuccess()) {
                connectionLatch.countDown();
            }
        }

        @Override
        public void requestDone(Status status, ChannelRPC channelRPC, PVStructure pvResponse) {
            PVStringArray valueField = (PVStringArray) pvResponse.getScalarArrayField("value", ScalarType.pvString);
            Convert convert = ConvertFactory.getConvert();
            String[] recievedChannels = new String[valueField.getLength()];
            convert.toStringArray(valueField, 0, valueField.getLength(), recievedChannels, 0);
            
            channels.addAll(Arrays.stream(recievedChannels).filter(x -> filterFunction.apply(x)).collect(Collectors.toSet()));
            rpcLatch.countDown();
            
            channelRPC.getChannel().destroy();
        }
        
        public boolean isConnected(long timeout) {
            try {
                if (connectionLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                    return true;
                }
            } catch (InterruptedException e) {
                /* fallthrough */
            }
            return false;
        }
    }
    
    private static class ChannelRequesterImpl implements ChannelRequester {
        
        CountDownLatch latch = new CountDownLatch(1);

        @Override
        public String getRequesterName() {
            return ChannelRPCRequesterImpl.class.getName();
        }

        @Override
        public void message(String message, MessageType messageType) {
        }

        @Override
        public void channelCreated(Status status, Channel channel) {
        }

        @Override
        public void channelStateChange(Channel channel, ConnectionState connectionState) {
            if (connectionState == ConnectionState.CONNECTED) {
                latch.countDown();
            }
        }
        
        public boolean isConnected(long timeout) {
            try {
                if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
                    return true;
                }
            } catch (InterruptedException e) {
                /* fallthrough */
            }
            return false;
        }
        
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println(getChannels(x -> true));
    }
    
}
