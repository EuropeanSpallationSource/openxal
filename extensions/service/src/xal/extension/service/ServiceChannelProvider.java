package xal.extension.service;


import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

class ServiceChannelProvider {

    static void initialize() {
        org.epics.pvaccess.ClientFactory.start(); 
    }

    static void destroy() {
        org.epics.pvaccess.ClientFactory.stop();
    }

    static Channel createChannel(String protocolType, ServiceListener listener) {
        return ChannelProviderRegistryFactory.getChannelProviderRegistry().
                createProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME).
                createChannel(protocolType, new ChannelRequesterImpl(protocolType, listener), ChannelProvider.PRIORITY_DEFAULT);
    }

    private static class ChannelRequesterImpl implements ChannelRequester, ChannelGetRequester {
        private static final Logger LOGGER = Logger.getLogger(ChannelRequesterImpl.class.getName());
        private static final PVStructure PV_REQUEST = CreateRequest.create().createRequest("field()");

        private final String protocol;
        private final ServiceListener listener;

        private String serviceName;

        ChannelRequesterImpl(String protocol, ServiceListener listener) {
            this.listener = listener;
            this.protocol = protocol;
        }

        @Override
        public String getRequesterName() {
            return getClass().getName();
        }

        @Override
        public void message(String message, MessageType messageType) {
            LOGGER.log(LoggingUtils.toLevel(messageType), message);
        }

        @Override
        public void channelCreated(Status status, Channel channel) {
            message("Service channel " + channel.getChannelName() + " created.", MessageType.info);
        }

        @Override
        public void channelStateChange(Channel channel, ConnectionState connectionState) {
            message("Service channel " + channel.getChannelName() + " state changed to " + 
                    connectionState, MessageType.info);
            if (connectionState == ConnectionState.CONNECTED) {
                channel.createChannelGet(this, PV_REQUEST);
            } else if (connectionState == ConnectionState.DISCONNECTED && serviceName != null) {
                // If serviceName is null the service was never connected and there is nothing to do.
                listener.serviceRemoved(ServiceDirectory.defaultDirectory(), protocol, serviceName);
            }
        }

        @Override
        public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
            if (status.isSuccess()) {
                channelGet.lastRequest();
                channelGet.get();
            } else {
                LOGGER.warning("Error while connecting to channel " + channelGet.getChannel().getChannelName()
                        + " for get operation.");
            }
        }

        @Override
        public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
            if (status.isSuccess()) {

                serviceName = pvStructure.getStringField(ServiceDirectory.SERVICE_NAME_FIELD_NAME).get();

                int port = pvStructure.getIntField(ServiceDirectory.PORT_FIELD_NAME).get();

                String hostAddress = parseIpAddress(channelGet.getChannel().getRemoteAddress());
                if (hostAddress.isEmpty()) {
                    // PVAccess might return address that is different from ip:port.
                    // The current implementation cannot deal with this.
                    LOGGER.warning("Could not get host address for service " + serviceName + ".");
                    return;
                }

                ServiceRef ref = new ServiceRef(serviceName, hostAddress, port);
                listener.serviceAdded(ServiceDirectory.defaultDirectory(), ref);
                
            } else 
                LOGGER.warning("Error while getting the value from " + channelGet.getChannel().getChannelName() + ".");
        }

        /**
         * Returns IP from string in format"/IP:PORT".
         * @param remoteAddress Address received from the PVAccess
         * @return IP address on which the PV is exposed
         */
        private static String parseIpAddress(String remoteAddress) {
            Pattern pattern = Pattern.compile("^/((?:[0-9]{1,3}\\.){3}[0-9]{1,3}):[0-9]+$");
            Matcher matcher = pattern.matcher(remoteAddress);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return "";
        }
        
    }
    
}
