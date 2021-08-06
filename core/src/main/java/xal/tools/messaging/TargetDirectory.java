/*
 * TargetDirectory.java
 *
 * Created on February 6, 2002, 8:20 AM
 */
package xal.tools.messaging;

import java.io.Serializable;
import java.util.*;

/**
 * TargetDirectory is a utility class for convenient storage and retrieval of
 * targets keyed by source and protocol.
 *
 * @author tap
 */
class TargetDirectory implements Serializable {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Map of target sets keyed by source which are keyed by protocol name
     */
    private final transient Map<String, Map<Object, Set<Object>>> protocolTable;

    /**
     * Creates new TargetDirectory
     */
    public TargetDirectory() {
        protocolTable = new HashMap<>();
    }

    /**
     * get an unmodifiable set of targets
     */
    synchronized <T> Set<T> targets(final Object source, final Class<T> protocol) {
        final Set<T> targetSet = targetSet(source, protocol);
        return Collections.unmodifiableSet(new HashSet<>(targetSet));
    }

    /**
     * get the set of targets keyed by source and protocol
     */
    @SuppressWarnings("unchecked")
    private synchronized <T> Set<T> targetSet(final Object source, final Class<T> protocol) {
        Map<Object, Set<Object>> sourceTable;
        final String protocolKey = protocolKey(protocol);

        if (protocolTable.containsKey(protocolKey)) {
            sourceTable = protocolTable.get(protocolKey);
        } else {
            return emptySet();
        }

        if (sourceTable.containsKey(source)) {
            return (Set<T>) sourceTable.get(source);
        } else {
            return emptySet();
        }
    }

    /**
     * an emptySet
     */
    private <T> Set<T> emptySet() {
        return new HashSet<>();
    }

    /**
     * Register a target to listen to protocol messages from source
     */
    public synchronized <T> void registerTarget(final Object target, final Object source, final Class<T> protocol) {
        Map<Object, Set<Object>> sourceTable;
        final String protocolKey = protocolKey(protocol);

        if (protocolTable.containsKey(protocolKey)) {
            sourceTable = protocolTable.get(protocolKey);
        } else {
            sourceTable = new HashMap<>();
            protocolTable.put(protocolKey, sourceTable);
        }

        Set<Object> targetSet;
        if (sourceTable.containsKey(source)) {
            targetSet = sourceTable.get(source);
        } else {
            targetSet = new HashSet<>();
            sourceTable.put(source, targetSet);
        }
        targetSet.add(target);
    }

    /**
     * remove the target as a listener of protocol messages from source
     */
    public synchronized <T> void removeTarget(final Object target, final Object source, final Class<T> protocol) {
        targetSet(source, protocol).remove(target);
    }

    /**
     * remove the target as a listener of protocol messages
     */
    public synchronized <T> void removeTarget(final Object target, final Class<T> protocol) {
        targetSet(null, protocol).remove(target);
    }

    /**
     * Remove the target from all sources that message to the specified protocol
     */
    public synchronized <T> void removeTargetFromAllSources(final Object target, final Class<T> protocol) {
        Map<Object, Set<Object>> sourceTable;
        final String protocolKey = protocolKey(protocol);

        if (protocolTable.containsKey(protocolKey)) {
            sourceTable = protocolTable.get(protocolKey);
        } else {
            return;
        }

        // get the collection of target sets associated with every source in the table
        final Collection<Set<Object>> targetSets = sourceTable.values();

        // loop through the target sets and remove the target from each set
        for (final Set<Object> targetSet : targetSets) {
            targetSet.remove(target);
        }
    }

    private <T> String protocolKey(final Class<T> protocol) {
        return protocol.getName();
    }
}
