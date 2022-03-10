package xal.smf;

import java.lang.reflect.Field;
import xal.ca.*;
import xal.tools.data.*;
import xal.smf.attr.*;
import xal.smf.data.BucketParser;
import xal.smf.impl.qualify.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The base class in the hierarchy of different accelerator node types.
 *
 * @author Nikolay Malitsky, Christopher K. Allen, Nick D. Pattengale
 */
public abstract class AcceleratorNode implements ElementType, DataListener {

    private static final Logger LOGGER = Logger.getLogger(AcceleratorNode.class.getName());

    private static final String ID_ATTR = "id";
    private static final String LEN_ATTR = "len";
    private static final String S_ATTR = "s";
    private static final String POS_ATTR = "pos";
    private static final String STATUS_ATTR = "status";
    private static final String EID_ATTR = "eid";
    private static final String PID_ATTR = "pid";

    /*
     *  Local Attributes
     */
    /**
     * node identifier
     */
    protected String strId;

    /**
     * physics identifier
     */
    protected String strPId;

    /**
     * engineering identifier
     */
    protected String strEId;

    /**
     * position of node
     */
    protected double dblPos;

    /**
     * length of node
     */
    protected double dblLen;

    /**
     * parent sequence object
     */
    protected AcceleratorSeq seqParent;

    /**
     * the associated Accelerator object
     */
    protected Accelerator objAccel;

    /**
     * all attribute buckets for node
     */
    protected Map<String, AttributeBucket> mapAttrs;

    /**
     * alignment attribute bucket for node
     */
    protected AlignmentBucket bucAlign;

    /**
     * twiss parameter bucket for node
     */
    protected TwissBucket bucTwiss;

    /**
     * aperture parameters for node
     */
    protected ApertureBucket bucAper;

    /**
     * Indicator as to whether the Accelerator Node is functional
     */
    protected boolean bolStatus;

    /**
     * Indicator as to whether accelerator node is valid
     */
    protected boolean bolValid;

    /**
     * "s" position for global display
     */
    protected double dblS;

    /**
     * Indicator if this node is a "softNode" copy
     */
    protected boolean bolIsSoft = false;

    /**
     * channel suite associated with this node
     */
    protected ChannelSuite channelSuite;

    /**
     * Derived class must furnish a unique type id
     */
    public abstract String getType();

    /**
     * Derived class may furnish a unique software type
     */
    public String getSoftType() {
        return null;
    }

    /**
     * Designated constructor
     *
     * @param strId the string ID for this node
     * @param channelFactory channel factory (null for default) for generating
     * this node's channels
     */
    protected AcceleratorNode(final String strId, final ChannelFactory channelFactory) {
        this.strId = strId;

        bolStatus = true;
        bolValid = true;

        mapAttrs = new HashMap<>();

        channelSuite = new ChannelSuite(channelFactory);
    }

    /**
     * Convenience constructor using the default channel factory
     *
     * @param strId the string ID for this node
     */
    protected AcceleratorNode(final String strId) {
        this(strId, ChannelFactory.defaultFactory());
    }

    // DataListener interface -tap
    /**
     * implement DataListener interface
     */
    @Override
    public String dataLabel() {
        return "node";
    }

    /**
     * implement DataListener interface
     */
    @Override
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        // set the id only the first time
        if (strId == null) {
            strId = adaptor.stringValue(ID_ATTR);
        }

        // update physics id
        if (adaptor.hasAttribute(PID_ATTR)) {
            strPId = adaptor.stringValue(PID_ATTR);
        }

        // update engineering id
        if (adaptor.hasAttribute(EID_ATTR)) {
            strEId = adaptor.stringValue(EID_ATTR);
        }

        // get the status of the node which identifies whether the node is operational
        if (adaptor.hasAttribute(STATUS_ATTR)) {
            bolStatus = adaptor.booleanValue(STATUS_ATTR);
        }

        // update length attribute if the adaptor supplies it
        if (adaptor.hasAttribute(LEN_ATTR)) {
            double newLength;
            try {
                newLength = adaptor.doubleValue(LEN_ATTR);
            } catch (NumberFormatException exception) {
                final String message = "Error reading node: " + strId;
                LOGGER.log(Level.SEVERE, message, exception);
                newLength = Double.NaN;
            }

            setLength(newLength);
        }

        // update position attribute if the adaptor supplies it
        if (adaptor.hasAttribute(POS_ATTR)) {
            double newPosition = adaptor.doubleValue(POS_ATTR);
            setPosition(newPosition);
        }

        // update s display coordinate if there is one
        if (adaptor.hasAttribute(S_ATTR)) {
            double newSDisplay = adaptor.doubleValue(S_ATTR);
            setSDisplay(newSDisplay);
        }

        // read the channel suites
        DataAdaptor suiteAdaptor = adaptor.childAdaptor("channelsuite");
        if (suiteAdaptor != null) {
            channelSuite.update(suiteAdaptor);
        }

        // read the attribute buckets
        final List<DataAdaptor> parserAdaptors = adaptor.childAdaptors("attributes");
        for (final DataAdaptor parserAdaptor : parserAdaptors) {
            final Collection<AttributeBucket> buckets = getBuckets();
            final BucketParser parser = new BucketParser(buckets);
            parser.update(parserAdaptor);

            // get the attribute buckets from the parser
            final Collection<AttributeBucket> bucketList = parser.getBuckets();
            for (final AttributeBucket bucket : bucketList) {
                // add the bucket only if it already hasn't been added
                if (!hasBucket(bucket)) {
                    addBucket(bucket);
                }
            }
        }
    }

    /**
     * implement DataListener interface
     */
    @Override
    public void write(DataAdaptor adaptor) {
        writeAttributes(adaptor);

        Collection<AttributeBucket> buckets = getBuckets();
        if (!buckets.isEmpty()) {
            adaptor.writeNode(new BucketParser(buckets));
        }
        if (!channelSuite.getHandles().isEmpty()) {
            adaptor.writeNode(channelSuite);
        }
    }

    /**
     * method to write status of the node into a separate file
     */
    public void writeStatus(DataAdaptor adaptor) {
        if (!bolStatus && getAccelerator().hasStatusFile()) {
            DataAdaptor childAdaptor = adaptor.createChild(dataLabel());
            childAdaptor.setValue(ID_ATTR, strId);
            childAdaptor.setValue(STATUS_ATTR, bolStatus);
        }
    }

    /**
     * write the attributes of the Node. Subclasses can be override this method
     * to write a different set of attributes
     *
     * @param adaptor
     */
    protected void writeAttributes(DataAdaptor adaptor) {
        adaptor.setValue(ID_ATTR, strId);
        adaptor.setValue("len", dblLen);
        adaptor.setValue("pos", dblPos);
        adaptor.setValue("type", getType());
        if (strPId != null) {
            adaptor.setValue(PID_ATTR, strPId);
        }
        if (strEId != null) {
            adaptor.setValue(EID_ATTR, strEId);
        }
        if (getSoftType() != null) {
            adaptor.setValue("softType", getSoftType());
        }
        if (!bolStatus && getAccelerator().hasStatusFile()) {
            adaptor.setValue(STATUS_ATTR, bolStatus);
        }
        if (dblS != 0) {
            adaptor.setValue("s", dblS);
        }
    }
    // end DataListener interface -tap

    /**
     * Attempt to find a channel for the given handle.
     *
     * @param handle the handle for which to find an associated channel
     * @return channel for the given handle or null if none could be found
     */
    public Channel findChannel(final String handle) {
        return channelSuite.getChannel(handle);
    }

    /**
     * Returns a collection of all channels
     *
     * @return channels (corresponding to all handles)
     */
    public List<Channel> getAllChannels() {
        ArrayList<Channel> channels = new ArrayList<>();
        Channel channel;
        for (String handle : getHandles()) {
            channel = findChannel(handle);
            if (channel != null && !channels.contains(channel)) {
                channels.add(channel);
            }
        }
        return channels;
    }

    /**
     * Do a batch connection of all handles for this node
     *
     * @return the BatchConnectionRequest object
     */
    public BatchConnectionRequest batchConnectAllHandles() {
        final BatchConnectionRequest request = new BatchConnectionRequest(getAllChannels());
        request.submit();
        return request;
    }

    /**
     * Do a batch connection of all handles for this node and wait for
     * completion
     *
     * @param timeout the maximum time in seconds to wait for completion
     * @return true if all channels successfully connected
     */
    public boolean batchConnectAllHandlesAndWait(double timeout) {
        final BatchConnectionRequest request = new BatchConnectionRequest(getAllChannels());
        return request.submitAndWait(timeout);
    }

    // added by nickp 2/8/2002
    /**
     * this method returns the Channel object of this node, associated with a
     * prescribed PV name. Note - xal interacts with EPICS via Channel objects.
     *
     * @param chanHandle The handle to the epics channel in stored in the
     * channel suite
     */
    public Channel getChannel(final String chanHandle) throws NoSuchChannelException {
        final Channel channel = findChannel(chanHandle);

        if (channel == null) {
            throw new NoSuchChannelException(this, chanHandle);
        }

        return channel;
    }

    public boolean isChannelSettable(final String handle) {
        return channelSuite.isSettable(handle);
    }

    /**
     * Get the channel corresponding to the specified handle and connect it.
     *
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or
     * null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by
     * the handle is associated with this node.
     */
    public Channel getAndConnectChannel(final String handle) throws NoSuchChannelException {
        final Channel channel = getChannel(handle);
        channel.connectAndWait();

        return channel;
    }

    /**
     * A method to make an EPICS ca connection for a given PV name The channel
     * connection is initiated, and no extra work is done, if the channel
     * connection already exists
     */
    public Channel lazilyGetAndConnect(String chanHandle, Channel channel) throws NoSuchChannelException {
        Channel tmpChan;

        if (channel == null) {
            tmpChan = getChannel(chanHandle);
            if (tmpChan == null) {
                throw new NoSuchChannelException(this, chanHandle);
            }
        } else {
            tmpChan = channel;
        }

        tmpChan.connectAndWait();

        return tmpChan;
    }

    /**
     * Get a list of properties that can be accessed through EPICS.
     *
     * @return properties that can be accessed via EPICS.
     */
    public List<AccessibleProperty> getAccessibleProperties() {
        return getAccessibleProperties(null, this.getClass());
    }

    protected List<AccessibleProperty> getAccessibleProperties(List<AccessibleProperty> properties, Class<?> cls) {
        if (properties == null) {
            properties = new ArrayList<>();
        }

        List<Field> fieldList = Arrays.asList(cls.getDeclaredFields());
        for (Field field : fieldList) {
            if (field.getType().equals(AccessibleProperty.class)) {
                try {
                    properties.add((AccessibleProperty) field.get(this));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(AcceleratorNode.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (cls.getSuperclass() != null) {
            getAccessibleProperties(properties, cls.getSuperclass());
        }

        return properties;
    }

    /**
     *
     * @return a list with expected channel handles by default.
     */
    public Collection<String> getDefaultHandles() {
        List<String> defaultHandles = new ArrayList<>();
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty property : properties) {
            if (!defaultHandles.contains(property.getSetHandle())) {
                defaultHandles.add(property.getSetHandle());
            }
            for (String readbackHandle : property.getReadbackHandles()) {
                if (!defaultHandles.contains(readbackHandle)) {
                    defaultHandles.add(readbackHandle);
                }
            }
        }
        return defaultHandles;
    }

    /**
     * Get the channels corresponding to the specified handle and connect to
     * them. This method is useful when setting a value and checking that the
     * value was set.
     *
     * @param setHandle The set handle for the channel to set.
     * @return A list containing the set (first) and readback channels
     * (subsequent) associated with this node and the specified set handle or
     * null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by
     * the handle is associated with this node.
     */
    public List<Channel> getAndConnectChannelSetAndReadback(String setHandle) {
        List<Channel> list = new ArrayList<>();
        try {
            Channel setChannel = getChannel(setHandle);
            setChannel.connectAndWait();
            list.add(setChannel);
        } catch (NoSuchChannelException ex) {
            Logger.getLogger(AcceleratorNode.class.getName()).log(Level.INFO, "Set channel not found for handle" + setHandle, ex);
            return list;
        }

        for (String handle : getReadbackHandles(setHandle)) {
            try {
                Channel readBackChannel = getChannel(handle);
                readBackChannel.connectAndWait();
                list.add(readBackChannel);
            } catch (NoSuchChannelException ex) {
                Logger.getLogger(AcceleratorNode.class.getName()).log(Level.INFO, "Readback channel not found for handle" + handle, ex);
            }
        }

        return list;
    }

    /**
     * Get the readback handle corresponding to a set channel.
     *
     * @param setHandle The set handle.
     * @return The corresponding readback handle.
     */
    public String[] getReadbackHandles(String setHandle) {
        List<String> readbackHandles = new ArrayList<>();
        for (AccessibleProperty prop : getAccessibleProperties()) {
            if (prop.getSetHandle().equals(setHandle)) {
                readbackHandles.addAll(Arrays.asList(prop.getReadbackHandles()));
            }
        }

        // Remove possible duplicates
        readbackHandles = readbackHandles.stream().distinct().collect(Collectors.toList());

        return (String[]) readbackHandles.toArray(new String[0]);
    }

    /**
     * Get the set handle corresponding to a readback channel.
     *
     * @param readbackHandle The readback handle.
     * @return The corresponding set handle.
     */
    public String getSetHandle(String readbackHandle) {
        for (AccessibleProperty prop : getAccessibleProperties()) {
            for (String readback : prop.getReadbackHandles()) {
                if (readback.equals(readbackHandle)) {
                    return prop.getSetHandle();
                }
            }
        }
        return null;
    }

    /**
     * Set a value to the set channel corresponding to the set handle, and then
     * check on the readback channel that the value is within an interval around
     * the set value.
     *
     * @param setHandle Handle corresponding to the set channel.
     * @param value Value to be set.
     * @param tolerance Defines an interval around the value (absolute value).
     * @param timeout Timeout in seconds for the readback to reach the set value
     * before failing. Failure is indicated by returning false.
     * @return true if the value is set correctly, otherwise false.
     * @throws PutException
     * @throws MonitorException
     */
    public boolean setValueAndVerify(String setHandle, Number value, Number tolerance, double timeout) throws PutException, MonitorException {
        List<Channel> channels = getAndConnectChannelSetAndReadback(setHandle);

        if (channels.size() >= 2 && channels.get(0) != null && channels.get(1) != null) {
            CountDownLatch latch = new CountDownLatch(1);

            // First channel is the set channel
            Channel setChannel = channels.get(0);

            List<Monitor> monitors = new ArrayList<>();

            // Try all readback channels, return after the first event that is within tolerance.
            for (Channel rbChannel : channels.subList(1, channels.size())) {
                Monitor monitor = null;

                if (value instanceof Byte) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.byteValue() - value.byteValue()) <= tolerance.byteValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.byteValue());
                } else if (value instanceof Float) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.floatValue() - value.floatValue()) <= tolerance.floatValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.floatValue());
                } else if (value instanceof Double) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.doubleValue() - value.doubleValue()) <= tolerance.doubleValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.doubleValue());
                } else if (value instanceof Short) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.shortValue() - value.shortValue()) <= tolerance.shortValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.shortValue());
                } else if (value instanceof Integer) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.intValue() - value.intValue()) <= tolerance.intValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.intValue());
                } else if (value instanceof Long) {
                    monitor = rbChannel.addMonitorValue((channelRecord, chan) -> {
                        if (Math.abs(channelRecord.longValue() - value.longValue()) <= tolerance.longValue()) {
                            latch.countDown();
                        }
                    }, 0);
                    setChannel.putVal(value.longValue());
                }

                if (monitor == null) {
                    return false;
                } else {
                    monitors.add(monitor);
                }
            }

            // Wait for the delay
            try {
                latch.await((long) (timeout * 1000), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(AcceleratorNode.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (Monitor monitor : monitors) {
                monitor.clear();
            }

            if (latch.getCount() == 0) {
                return true;
            }
        } else if (channels.size() == 1 && channels.get(0) != null) {
            // Only set channel found, put Value and return false  
            Logger.getLogger(AcceleratorNode.class.getName()).log(Level.INFO, "Only set channel found for handle {0}", setHandle);

            Channel setChannel = channels.get(0);

            if (value instanceof Byte) {
                setChannel.putVal(value.byteValue());
            } else if (value instanceof Float) {
                setChannel.putVal(value.floatValue());
            } else if (value instanceof Double) {
                setChannel.putVal(value.doubleValue());
            } else if (value instanceof Short) {
                setChannel.putVal(value.shortValue());
            } else if (value instanceof Integer) {
                setChannel.putVal(value.intValue());
            } else if (value instanceof Long) {
                setChannel.putVal(value.longValue());
            }
        } else {
            Logger.getLogger(AcceleratorNode.class.getName()).log(Level.INFO, "No channels found for handle {0}", setHandle);
        }

        return false;
    }

    /**
     * Get a list with the names of properties that can be accessed through
     * EPICS and that are used by the model.
     *
     * @return properties that can be accessed via EPICS.
     */
    public List<String> getProperties() {
        List<AccessibleProperty> accessibleProperties = getAccessibleProperties();
        List<String> properties = new ArrayList<>();
        for (AccessibleProperty prop : accessibleProperties) {
            if (prop.hasDesignValues()) {
                properties.add(prop.getName());
            }
        }
        return properties;
    }

    /**
     * Get the design value for the specified property
     */
    public double getDesignPropertyValue(final String propertyName) {
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName) && prop.hasDesignValues()) {
                return prop.getDesign();
            }
        }
        throw new IllegalArgumentException("Unsupported AcceleratorNode design value property: " + propertyName);
    }

    /**
     * Get the design value for the specified property
     */
    public void setDesignPropertyValue(String propertyName, double value) {
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName) && prop.hasDesignValues()) {
                prop.setDesign(value);
            }
        }
        throw new IllegalArgumentException("Unsupported AcceleratorNode design value property: " + propertyName);
    }

    /**
     * Get the live property value for the corresponding array of channel values
     * in the order given by getLivePropertyChannels()
     */
    public double getLivePropertyValue(final String propertyName, final double[] channelValues) {
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName)) {
                return prop.getLive(channelValues);
            }
        }
        throw new IllegalArgumentException("Unsupported AcceleratorNode live value property: " + propertyName);
    }

    /**
     * Set the live property value for the corresponding array of channel values
     * in the order given by getLivePropertyChannels()
     */
    public void setLivePropertyValue(String propertyName, double channelValue) throws PutException {
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName)) {
                double setterValue = prop.setLive(channelValue);
                Channel setChannel = findChannel(prop.getSetHandle());
                setChannel.putVal(setterValue);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported AcceleratorNode live value property: " + propertyName);
    }

    /**
     * Get the array of channels for the specified property
     */
    public Channel[] getLivePropertyChannels(final String propertyName) {
        List<Channel> channels = new ArrayList<>();
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName) && prop.hasDesignValues()) {
                for (String readback : prop.getReadbackHandles()) {
                    channels.add(findChannel(readback));
                }
                return channels.toArray(new Channel[0]);
            }
        }

        throw new IllegalArgumentException("Unsupported AcceleratorNode live channels property: " + propertyName);
    }

    /*
     *  User Interface
     */
    /**
     * return the ID of this node
     */
    public String getId() {
        return strId;
    }

    /**
     * return the engineering ID of this node
     */
    public String getEId() {
        return strEId;
    }

    /**
     * return the physics ID of this node
     */
    public String getPId() {
        return strPId;
    }

    /**
     * return the physical length of this node (m)
     */
    public double getLength() {
        return dblLen;
    }

    /**
     * return the position of this node, along the reference orbit within its
     * sequence (m)
     */
    public double getPosition() {
        return dblPos;
    }

    /**
     * return global "s" display coordinate
     *
     * @return s coordinate
     */
    public double getSDisplay() {
        return dblS;
    }

    /**
     * return the top level accelerator that this node belongs to
     */
    public Accelerator getAccelerator() {
        return objAccel;
    }

    /**
     * return the parent sequence that this node belongs to
     */
    public AcceleratorSeq getParent() {
        return seqParent;
    }

    /**
     * get the primary ancestor sequence that is a direct child of the
     * accelerator
     */
    public AcceleratorSeq getPrimaryAncestor() {
        return getParent().getPrimaryAncestor();
    }

    /**
     * Indicates if the node has a parent set
     */
    public boolean hasParent() {
        return (seqParent != null);
    }

    /**
     * Runtime indication of accelerator component operation
     *
     * @return true(up and running) false(down)
     */
    public boolean getStatus() {
        return bolStatus;
    }

    /**
     * Runtime indication of the validity of component operation
     *
     * @return true(valid operation) false(questionable operation)
     */
    public boolean getValid() {
        return bolValid;
    }

    void setPId(String value) {
        strPId = value;
    }

    void setEId(String value) {
        strEId = value;
    }

    /**
     * set the position of this accelerator node within its parent sequence
     */
    public void setPosition(final double position) {
        dblPos = position;
    }

    /**
     * set the length of this accelerator node
     */
    public void setLength(final double length) {
        dblLen = length;
    }

    /**
     * set "s" coordinate
     *
     * @param dblS s coordinate
     */
    public void setSDisplay(double dblS) {
        this.dblS = dblS;
    }

    /**
     * Runtime indication of accelerator operation
     *
     * @param bolStatus true(up and running) false(down)
     */
    public void setStatus(boolean bolStatus) {
        this.bolStatus = bolStatus;
    }

    /**
     * Runtime indication of the validity of component operation
     *
     * @param bolValid true(valid operation) false(questionable operation)
     */
    public void setValid(boolean bolValid) {
        this.bolValid = bolValid;
    }


    /*
     *  SMF Attribute Buckets Support
     */
    /**
     * General attribute buckets support
     */
    public void addBucket(AttributeBucket buc) {

        if (buc.getClass().equals(TwissBucket.class)) {
            setTwiss((TwissBucket) buc);
        }
        if (buc.getClass().equals(AlignmentBucket.class)) {
            setAlign((AlignmentBucket) buc);
        }
        if (buc.getClass().equals(ApertureBucket.class)) {
            setAper((ApertureBucket) buc);
        }

        // List of all attribute buckets
        mapAttrs.put(buc.getType(), buc);
    }

    public Collection<AttributeBucket> getBuckets() {
        return mapAttrs.values();
    }

    public AttributeBucket getBucket(String type) {
        return mapAttrs.get(type);
    }

    public boolean hasBucket(AttributeBucket bucket) {
        String bucketType = bucket.getType();
        return bucket == getBucket(bucketType);
    }

    // Specific Buckets
    /**
     * returns the bucket containing the Twiss parameters - see attr.TwissBucket
     */
    public TwissBucket getTwiss() {
        return bucTwiss;
    }

    /**
     * returns the bucket containing the alignment parameters - see
     * attr.AlignBucket
     */
    public AlignmentBucket getAlign() {
        if (bucAlign == null) {
            setAlign(new AlignmentBucket());
        }
        return bucAlign;
    }

    /**
     * returns device pitch angle in degrees
     *
     * @return pitch angle
     */
    public double getPitchAngle() {
        if (bucAlign != null) {
            return getAlign().getPitch();
        } else {
            return new AlignmentBucket().getPitch();
        }
    }

    /**
     * returns device yaw angle in degrees
     *
     * @return yaw angle
     */
    public double getYawAngle() {
        if (bucAlign != null) {
            return getAlign().getYaw();
        } else {
            return new AlignmentBucket().getYaw();
        }
    }

    /**
     * returns device roll angle in degrees
     *
     * @return roll angle
     */
    public double getRollAngle() {
        if (bucAlign != null) {
            return getAlign().getRoll();
        } else {
            return new AlignmentBucket().getRoll();
        }
    }

    /**
     * returns device x offset
     *
     * @return x offset
     */
    public double getXOffset() {
        if (bucAlign != null) {
            return getAlign().getX();
        } else {
            return new AlignmentBucket().getX();
        }
    }

    /**
     * returns device y offset
     *
     * @return y offset
     */
    public double getYOffset() {
        if (bucAlign != null) {
            return getAlign().getY();
        } else {
            return new AlignmentBucket().getY();
        }
    }

    /**
     * returns device z offset
     *
     * @return z offset
     */
    public double getZOffset() {
        if (bucAlign != null) {
            return bucAlign.getZ();
        } else {
            return new AlignmentBucket().getZ();
        }
    }

    /**
     * returns the bucket containing the Aperture parameters - see
     * attr.ApertureBucket
     */
    public ApertureBucket getAper() {
        if (bucAper == null) {
            setAper(new ApertureBucket());
        }
        return bucAper;
    }

    /**
     * sets the bucket containing the twiss parameters - see attr.TwissBucket
     */
    public void setAlign(AlignmentBucket buc) {
        bucAlign = buc;
        mapAttrs.put(buc.getType(), buc);
    }

    /**
     * sets the bucket containing the alignment parameters - see
     * attr.AlignBucket
     */
    public void setTwiss(TwissBucket buc) {
        bucTwiss = buc;
        mapAttrs.put(buc.getType(), buc);
    }

    /**
     * sets the bucket containing the Aperture parameters - see
     * attr.ApertureBucket
     */
    public void setAper(ApertureBucket buc) {
        bucAper = buc;
        mapAttrs.put(buc.getType(), buc);
    }

    /**
     * set device pitch angle
     *
     * @param angle pitch angle in degree
     */
    public void setPitchAngle(double angle) {
        getAlign().setPitch(angle);
    }

    /**
     * set device yaw angle
     *
     * @param angle yaw angle in degree
     */
    public void setYawAngle(double angle) {
        getAlign().setYaw(angle);
    }

    /**
     * set device roll angle
     *
     * @param angle roll angle in degree
     */
    public void setRollAngle(double angle) {
        getAlign().setRoll(angle);
    }

    /**
     * set device x offset
     *
     * @param offset x offset
     */
    public void setXOffset(double offset) {
        getAlign().setX(offset);
    }

    /**
     * set device y offset
     *
     * @param offset y offset
     */
    public void setYOffset(double offset) {
        getAlign().setY(offset);
    }

    /**
     * set device z offset
     *
     * @param offset z offset
     */
    public void setZOffset(double offset) {
        getAlign().setZ(offset);
    }

    /*
     *  SMF Data Structure Methods
     */
    /**
     * remove this node from the accelerator hieracrhcy
     */
    public void clear() {
        removeFromParent();
    }

    /**
     * remove this node from its immediate parent sequence
     */
    protected void removeFromParent() {
        if (seqParent == null) {
            return;
        }
        seqParent.removeNode(this);
    }

    /**
     * define the parent sequence for this node
     */
    protected void setParent(AcceleratorSeq parent) {
        removeFromParent();
        seqParent = parent;
    }

    /**
     * set the top level accelerator for this node
     */
    protected void setAccelerator(Accelerator accel) {
        if (objAccel != null) {
            objAccel.nodeRemoved(this);
        }

        objAccel = accel;

        if (accel != null) {
            accel.nodeAdded(this);
        }
    }

    /**
     * channel suite accessor
     */
    public ChannelSuite channelSuite() {
        return channelSuite;
    }

    /**
     * accessor to channel suite handles
     */
    public Collection<String> getHandles() {
        return channelSuite.getHandles();
    }

    //------- ElementType interface --------------------------------
    /**
     * Determine if a node is of the specified type. The comparison is based
     * upon the node's class and the element type manager handles checking for
     * inherited classes to types get inherited. Subclasses can override this
     * method if the types comparison is more complicated (e.g. if more than one
     * type can be associated with the same node class).
     *
     * @param compType The type against which to compare.
     * @return true if the node is of the specified type; false otherwise.
     */
    @Override
    public boolean isKindOf(String compType) {
        return ElementTypeManager.defaultManager().match(this.getClass(), compType);
    }

    /**
     * Determine if the node is a magnet.
     *
     * @return true if the node is a magnet; false other.
     */
    @Override
    public boolean isMagnet() {
        // by default, a node is not an magnet
        return false;
    }

    //------------------ Object Overrides ------------------------------\\
    /**
     * Returns the identifier string of the node.
     *
     * @return the physical hardware identifier
     *
     * @since Aug 20, 2009
     * @author Christopher K. Allen
     *
     * @see java.lang.Object#toString()
     * @see AcceleratorNode#getId()
     *
     */
    @Override
    public String toString() {
        return this.getId();
    }
}
