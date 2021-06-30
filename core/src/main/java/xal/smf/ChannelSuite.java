/*
 * ChannelSuite.java
 *
 * Created on September 18, 2002, 4:32 PM
 */
package xal.smf;

import xal.ca.*;
import xal.tools.data.*;
import xal.tools.transforms.ValueTransform;

import java.util.*;

/**
 * Manage the mapping of handles to signals and channels for a node. A signal is
 * the unique PV name used for accessing EPICS records. A handle is a high level
 * name used to access a PV in a specific context. For example a channel suite
 * instance typically represents a suite of PVs associated with a particular
 * element. Consider a BPM element. It has several PVs associated with it. The
 * handles are labels common to all BPMs such as "xAvg", "yAvg", ... The handle
 * is used to access a particular PV when applied to an element. So for example
 * "xAvg" applied to BPM 1 of the MEBT refers to the specific PV
 * "MEBT_Diag:BPM01:xAvg". Thus a handle is to a ChannelSuite instance much like
 * an instance variable is to an instance of a class.
 *
 * @author tap
 */
public class ChannelSuite implements DataListener {

    public static final String DATA_LABEL = "channelsuite";

    /**
     * map of channels keyed by handle
     */
    private final Map<String, Channel> channelHandleMap;

    /**
     * channel factory for getting channels
     */
    private ChannelFactory channelFactory;

    /**
     * Signal Suite
     */
    private final SignalSuite signalSuite;

    /**
     * Creates a new instance of ChannelSuite using the default channel factory
     */
    public ChannelSuite() {
        this(null);
    }

    /**
     * Primary constructor for creating an instance of channel suite
     *
     * @param channelFactory channel factory (or null for default factory) for
     * generating channels
     */
    public ChannelSuite(final ChannelFactory channelFactory) {
        this.channelFactory = channelFactory != null ? channelFactory : ChannelFactory.defaultFactory();
        channelHandleMap = new HashMap<>();
        signalSuite = new SignalSuite();
    }

    /**
     * get the channel factory
     */
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    /**
     * set a new channel factory and clears the old channel map
     */
    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        channelHandleMap.clear();
    }

    /**
     * dataLabel() provides the name used to identify the class in an external
     * data source.
     *
     * @return a tag that identifies the receiver's type
     */
    @Override
    public String dataLabel() {
        return DATA_LABEL;
    }

    /**
     * Update the data based on the information provided by the data provider.
     *
     * @param adaptor The adaptor from which to update the data
     */
    @Override
    public void update(final DataAdaptor adaptor) {
        signalSuite.update(adaptor);
    }

    /**
     * Write data to the data adaptor for storage.
     *
     * @param adaptor The adaptor to which the receiver's data is written
     */
    @Override
    public void write(final DataAdaptor adaptor) {
        signalSuite.write(adaptor);
    }

    /**
     * Programmatically add or replace a channel corresponding to the specified
     * handle
     *
     * @param handle The handle referring to the signal
     * @param signal PV signal associated with the handle
     * @param settable indicates whether the channel is settable
     * @param transformKey Key of the signal's transformation
     * @param valid specifies whether the channel is marked valid
     */
    public void putChannel(final String handle, final String signal, final boolean settable, final String transformKey, final boolean valid) {
        signalSuite.putChannel(handle, signal, settable, transformKey, valid);
    }

    /**
     * Convenience method to programmatically add or replace a channel
     * corresponding to the specified handle with valid set to true
     *
     * @param handle The handle referring to the signal
     * @param signal PV signal associated with the handle
     * @param settable indicates whether the channel is settable
     * @param transformKey Key of the signal's transformation
     */
    public void putChannel(final String handle, final String signal, final boolean settable, final String transformKey) {
        putChannel(handle, signal, settable, transformKey, true);
    }

    /**
     * Convenience method to programmatically add or replace a channel
     * corresponding to the specified handle with valid set to true and no
     * transform
     *
     * @param handle The handle referring to the signal
     * @param signal PV signal associated with the handle
     * @param settable indicates whether the channel is settable
     */
    public void putChannel(final String handle, final String signal, final boolean settable) {
        putChannel(handle, signal, settable, null);
    }

    /**
     * Programmatically assign a transform for the specified name
     *
     * @param name key for associating the transform
     * @param transform the value transform
     */
    public void putTransform(final String name, final ValueTransform transform) {
        signalSuite.putTransform(name, transform);
    }

    /**
     * See if this channel suite manages the specified signal.
     *
     * @param signal The PV signal to check for availability.
     * @return true if the PV signal is available and false if not.
     */
    protected boolean hasSignal(final String signal) {
        return signalSuite.hasSignal(signal);
    }

    /**
     * See if this channel suite manages the specified handle.
     *
     * @param handle The handle to check for availability.
     * @return true if the handle is available and false if not.
     */
    public final boolean hasHandle(final String handle) {
        return signalSuite.hasHandle(handle);
    }

    /**
     * Get all of the handles managed by the is channel suite.
     *
     * @return The handles managed by this channel suite.
     */
    public final Collection<String> getHandles() {
        return signalSuite.getHandles();
    }

    /**
     * Get the channel signal corresponding to the handle.
     *
     * @param handle The handle for which to get the PV signal name.
     * @return Get the PV signal name associated with the specified handle or
     * null if it is not found.
     */
    public final String getSignal(final String handle) {
        return signalSuite.getSignal(handle);
    }

    /**
     * Get the transform associated with the specified handle.
     *
     * @param handle The handle for which to get the transform.
     * @return The transform for the specified handle.
     */
    public final ValueTransform getTransform(final String handle) {
        return signalSuite.getTransform(handle);
    }

    /**
     * Determine whether the handle's corresponding PV is valid.
     *
     * @param handle The handle for which to get the validity.
     * @return validity state of the PV or false if there is no entry for the
     * handle
     */
    public final boolean isValid(final String handle) {
        return signalSuite.isValid(handle);
    }

    /**
     * Determine whether the handle's corresponding PV is settable.
     *
     * @param handle The handle for which to get the attribute.
     * @return set parameter of the PV or false if there is no entry for the
     * handle
     */
    public boolean isSettable(final String handle) {
        return signalSuite.isSettable(handle);
    }

    /**
     * Set settable property for the PV associated with the handle.
     *
     * @param handle The handle for which to set the settable property.
     * @param settable Set parameter.
     */
    public void setSettable(final String handle, final boolean settable) {
        signalSuite.setSettable(handle, settable);
    }

    /**
     * Get the channel corresponding to the specified handle.
     *
     * @param handle The handle for which to get the associated Channel.
     * @return The channel associated with the specified handle.
     */
    public Channel getChannel(final String handle) {
        // first see if we have ever cached the channel
        Channel channel = channelHandleMap.get(handle);

        // if the channel was never cached ...
        if (channel == null) {
            // lookup the signal
            final String signal = getSignal(handle);
            // get the channel from the channel factory
            if (signal != null && !signal.equals("")) {
                final ValueTransform transform = getTransform(handle);
                if (transform != null) {
                    channel = channelFactory.getChannel(signal, transform);
                } else {
                    channel = channelFactory.getChannel(signal);
                }

                channel.setValid(isValid(handle));

                if (channel instanceof IServerChannel) {
                    ((IServerChannel) channel).setSettable(signalSuite.isSettable(handle));
                }

            }

            // if we have a channel, cache it for future access
            if (channel != null) {
                channelHandleMap.put(handle, channel);
            }
        }

        return channel;
    }
}

/**
 * SignalSuite represents the map of handle/signal pairs that identifies a
 * channel and associates it with a node via the handle.
 *
 * @author tap
 */
class SignalSuite {

    /**
     * hash set of handles that are settable by default
     */
    private static final Set<String> SETTABLE_CHANNEL_HANDLES = new HashSet<>();

    // assign the settable handles
    static {
        final String[] HANDLES = {"I_Set", "fieldSet", "cycleEnable", "cavAmpSet", "cavPhaseSet", "deltaTRFStart", "deltaTRFEnd", "tDelay", "blankBeam"};

        for (final String handle : HANDLES) {
            SETTABLE_CHANNEL_HANDLES.add(handle);
        }
    }

    /**
     * map of signal entries keyed by handle
     */
    private final Map<String, SignalEntry> signalMap;

    /**
     * map of transforms keyed by name
     */
    private final Map<String, ValueTransform> transformMap;

    /**
     * Creates a new instance of SignalSuite
     */
    public SignalSuite() {
        signalMap = new HashMap<>();
        transformMap = new HashMap<>();
    }

    /**
     * determine if the handle is a settable handle (fixed time lookup since
     * using a Hash Set)
     */
    private boolean isHandleSettable(final String handle) {
        return SETTABLE_CHANNEL_HANDLES.contains(handle);
    }

    /**
     * Update the data based on the information provided by the data provider.
     *
     * @param adaptor The adaptor from which to update the data
     */
    public void update(final DataAdaptor adaptor) {
        final List<DataAdaptor> channelAdaptors = adaptor.childAdaptors("channel");
        if (channelAdaptors == null) {
            return;
        }
        for (final DataAdaptor channelAdaptor : channelAdaptors) {
            final String handle = channelAdaptor.stringValue("handle");

            if (!hasHandle(handle)) {
                signalMap.put(handle, new SignalEntry());
            }
            final SignalEntry signalEntry = signalMap.get(handle);

            final String signal = channelAdaptor.stringValue("signal");
            if (signal != null) {
                signalEntry.setSignal(signal);
            }

            // if the settable attribute is specified, then use its value otherwise fallback to the default settable handles lookup
            if (channelAdaptor.hasAttribute("settable")) {
                final boolean settable = channelAdaptor.booleanValue("settable");
                signalEntry.setSettable(settable);
            } else if (isHandleSettable(handle)) {		// if settable is not explicitly specified, determine if the handle is settable by default
                signalEntry.setSettable(true);
            }

            if (channelAdaptor.hasAttribute("valid")) {
                final boolean valid = channelAdaptor.booleanValue("valid");
                signalEntry.setValid(valid);
            }

            if (channelAdaptor.hasAttribute("transform")) {
                final String transformKey = channelAdaptor.stringValue("transform");
                signalEntry.setTransformKey(transformKey);
            }
        }

        final List<DataAdaptor> transformAdaptors = adaptor.childAdaptors("transform");
        if (transformAdaptors == null) {
            return;
        }
        for (final DataAdaptor transformAdaptor : transformAdaptors) {
            final String name = transformAdaptor.stringValue("name");
            final ValueTransform transform = TransformFactory.getTransform(transformAdaptor);
            putTransform(name, transform);
        }
    }

    /**
     * Write data to the data adaptor for storage.
     *
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write(final DataAdaptor adaptor) {
        final Collection<Map.Entry<String, SignalEntry>> signalMapEntries = signalMap.entrySet();
        for (final Map.Entry<String, SignalEntry> entry : signalMapEntries) {
            final DataAdaptor channelAdaptor = adaptor.createChild("channel");
            final SignalEntry signalEntry = entry.getValue();

            channelAdaptor.setValue("handle", entry.getKey());
            channelAdaptor.setValue("signal", signalEntry.signal());
            channelAdaptor.setValue("settable", signalEntry.settable());
            if (!signalEntry.isValid()) {
                channelAdaptor.setValue("valid", signalEntry.isValid());
            }
            if (signalEntry.getTransformKey() != null) {
                channelAdaptor.setValue("transform", signalEntry.getTransformKey());
            }
        }
    }

    /**
     * Programmatically add or replace a signal entry corresponding to the
     * specified handle
     *
     * @param handle The handle referring to the signal entry
     * @param signal PV signal associated with the handle
     * @param settable indicates whether the channel is settable
     * @param transformKey Key of the signal's transformation
     * @param valid specifies whether the channel is marked valid
     */
    public void putChannel(final String handle, final String signal, final boolean settable, final String transformKey, final boolean valid) {
        final SignalEntry signalEntry = new SignalEntry(signal, settable, transformKey);
        signalEntry.setValid(valid);
        signalMap.put(handle, signalEntry);
    }

    /**
     * Programmatically assign a transform for the specified name
     *
     * @param name key for associating the transform
     * @param transform the value transform
     */
    public void putTransform(final String name, final ValueTransform transform) {
        transformMap.put(name, transform);
    }

    /**
     * Check if this suite manages the specified PV signal.
     *
     * @param signal The PV signal name for which to check.
     * @return true if this suite manages the specified signal and false
     * otherwise.
     */
    boolean hasSignal(final String signal) {
        for (final SignalEntry entry : signalMap.values()) {
            if (entry.signal().equals(signal)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the PV signal associated with the handle.
     *
     * @param handle The handle for which to get the associated PV signal.
     * @return The signal associated with the specified handle.
     */
    public String getSignal(final String handle) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        return signalEntry != null ? signalEntry.signal() : null;
    }

    /**
     * Get all of the handles within this suite.
     *
     * @return The handles managed by this suite.
     */
    public Collection<String> getHandles() {
        return signalMap.keySet();
    }

    /**
     * Check if the signal suite manages the handle.
     *
     * @param handle The handle for which to check availability.
     * @return true if the handle is available and false otherwise.
     */
    public boolean hasHandle(final String handle) {
        return signalMap.containsKey(handle);
    }

    /**
     * Check if the signal entry associated with the specified handle has an
     * associated value transform.
     *
     * @param handle The handle to check for an associated transform.
     * @return true if the handle has an associated value transform and false
     * otherwise.
     */
    public boolean hasTransform(final String handle) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        return signalEntry != null ? (signalEntry.getTransformKey() != null) : false;
    }

    /**
     * Get the transform associated with the specified handle.
     *
     * @param handle The handle for which to get the transform.
     * @return The transform for the specified handle.
     */
    public ValueTransform getTransform(String handle) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        return signalEntry != null ? transformMap.get(signalEntry.getTransformKey()) : null;
    }

    /**
     * Determine whether the handle's corresponding PV is valid.
     *
     * @param handle The handle for which to get the validity.
     * @return validity state of the PV or false if there is no entry for the
     * handle
     */
    public boolean isValid(final String handle) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        return signalEntry != null ? signalEntry.isValid() : false;
    }

    /**
     * Determine whether the handle's corresponding PV is settable.
     *
     * @param handle The handle for which to get the attribute.
     * @return set parameter of the PV or false if there is no entry for the
     * handle
     */
    public boolean isSettable(final String handle) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        return signalEntry != null ? signalEntry.settable() : false;
    }

    /**
     * Set settable property for the PV associated with the handle.
     *
     * @param handle The handle for which to set the settable property.
     * @param settable Set parameter.
     */
    public void setSettable(final String handle, final boolean settable) {
        final SignalEntry signalEntry = getSignalEntry(handle);
        if (signalEntry != null) {
            signalEntry.setSettable(settable);
        }
    }

    /**
     * Get the signal entry for the handle.
     *
     * @param handle The handle for which to get the entry.
     * @return signal entry for the handle or null if there is none
     */
    private SignalEntry getSignalEntry(final String handle) {
        return signalMap.get(handle);
    }
}

/**
 * Entry in the signal map corresponding to a handle. In the signal map the key
 * is a handle and the value is an instance of SignalEntry.
 */
class SignalEntry {

    /**
     * the PV signal name
     */
    private String signal;
    /**
     * whether the PV is settable
     */
    private boolean settable;
    /**
     * whether the channel is marked valid
     */
    private boolean valid;
    /**
     * Name of the transform if any
     */
    private String transformKey;

    /**
     * Primary Constructor
     */
    public SignalEntry(final String signal, final boolean settable, final String transformKey) {
        this.signal = signal;
        this.settable = settable;
        this.transformKey = transformKey;
        valid = true;
    }

    /**
     * Constructor
     */
    public SignalEntry() {
        this(null, false, null);
    }

    /**
     * Get whether the PV is settable or not.
     *
     * @return true if the PV is settable and false otherwise.
     */
    public boolean settable() {
        return settable;
    }

    /**
     * set the settable property
     */
    public void setSettable(final boolean isSettable) {
        settable = isSettable;
    }

    /**
     * get the valid status of the PV
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * mark the valid status of the PV
     */
    public void setValid(final boolean isValid) {
        valid = isValid;
    }

    /**
     * Get the PV signal name.
     *
     * @return The PV signal name.
     */
    public String signal() {
        return signal;
    }

    /**
     * set the signal
     */
    public void setSignal(final String signal) {
        this.signal = signal;
    }

    /**
     * Get the name of the associated transform used
     *
     * @return the name of the associated transform
     */
    public String getTransformKey() {
        return transformKey;
    }

    /**
     * set the transform key
     */
    public void setTransformKey(final String transformKey) {
        this.transformKey = transformKey;
    }
}
