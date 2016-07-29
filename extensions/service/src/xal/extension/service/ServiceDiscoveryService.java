package xal.extension.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple service that periodically scans the network for new services and
 * creates connections to them.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
class ServiceDiscoveryService {
    
    /** Loop period in milliseconds */
    private static final long LOOP_PERIOD = 2000;
    
    /** Type used for filtering of the pva channels */
    private final String type;

    private final ServiceListener listener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** Names of the created channels */
    private Collection<String> currentChannelNames;
    
    /**
     * Creates channels from name in the collection.
     * @param newChannels Collection that contains channel names for all channels that need to be created.
     */
    private void createChannels(Collection<String> newChannels) {
        for (String channel : newChannels) {
           ServiceChannelProvider.createChannel(channel, listener); 
        }
    }
    
    /**
     * Constructor 
     * @param type Name filter for pva channels, this service only observes channels that start with this string
     * @param listener Service listener that is notified of new channels
     */
    ServiceDiscoveryService (String type, ServiceListener listener) {
        this.type = type;
        this.listener = listener;
        
        currentChannelNames = ServerDiscoveryUtil.getChannels(x -> x.startsWith(type));
        createChannels(currentChannelNames);

        scheduler.scheduleAtFixedRate(() -> channelDiscoveryTask(), LOOP_PERIOD,
                LOOP_PERIOD, TimeUnit.MILLISECONDS);

    }
    
    /**
     * Main task of the service.
     */
    private void channelDiscoveryTask() {
        Collection<String> channels = ServerDiscoveryUtil.getChannels(x -> x.startsWith(type));
        Collection<String> newChannels = new HashSet<>(channels);
        newChannels.removeAll(currentChannelNames);

        if (newChannels.size() > 0) {
            createChannels(newChannels);
            currentChannelNames.addAll(channels);
        }
    }

}
