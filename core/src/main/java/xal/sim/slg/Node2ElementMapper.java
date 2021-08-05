/*
 * Node2ElementMapper.java
 *
 * Created on April 14, 2003, 11:08 PM
 */
package xal.sim.slg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import xal.smf.*;

/**
 * A visitor that generates the forward dictionary [(key,value)=(node,element)].
 *
 * @author wdklotz
 */
public class Node2ElementMapper implements Visitor {

    /**
     * dictionary (key,value)=(node,element)
     */
    private Map<AcceleratorNode, Element> node2ElementMap;
    /**
     * dictionary (key,value)=(node ID,node)
     */
    private Map<String, AcceleratorNode> id2NodeMap;

    /**
     * Creates a new instance of Node2ElementMapper
     */
    Node2ElementMapper() {
        node2ElementMap = new HashMap<>();
        id2NodeMap = new HashMap<>();
    }

    /**
     * Returns a set view of the mappings contained in this map.
     */
    Set<Map.Entry<AcceleratorNode, Element>> entrySet() {
        return node2ElementMap.entrySet();
    }

    /**
     * Getter for the map property.
     */
    Map<AcceleratorNode, Element> getMap() {
        return node2ElementMap;
    }

    public String nodeId2ElementId(String nodeId) throws LatticeError {
        try {
            AcceleratorNode node = id2NodeMap.get(nodeId);
            Element element = node2ElementMap.get(node);
            return element.getName();
        } catch (NullPointerException e) {
            throw new LatticeError(nodeId + ": not a lattice element!");
        }
    }

    public Element nodeId2Element(String nodeId) {
        AcceleratorNode node = id2NodeMap.get(nodeId);
        return node2ElementMap.get(node);
    }

    public String node2ElementId(AcceleratorNode node) {
        Element element = node2ElementMap.get(node);
        return element.getName();
    }

    public Element node2Element(AcceleratorNode node) {
        return node2ElementMap.get(node);
    }

    /**
     * visit a RFGap lattice element
     */
    @Override
    public void visit(RFGap e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a PermMarker lattice element
     */
    @Override
    public void visit(PermMarker e) {
        StringTokenizer strtok = new StringTokenizer(e.getName(), ":");
        if (strtok.nextToken().equals("ELEMENT_CENTER")) {
            node2ElementMap.put(e.getAcceleratorNode(), e);
            id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
        }
    }

    /**
     * visit a SkewSext lattice element
     */
    @Override
    public void visit(SkewSext e) {
        // Do nothing
    }

    /**
     * visit a Octupole lattice element
     */
    @Override
    public void visit(Octupole e) {
        // Do nothing
    }

    /**
     * visit a BCMonitor lattice element
     */
    @Override
    public void visit(BCMonitor e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a HSteerer lattice element
     */
    @Override
    public void visit(HSteerer e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a Dipole lattice element
     */
    @Override
    public void visit(Dipole e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a EKicker lattice element
     */
    @Override
    public void visit(final EKicker element) {
        node2ElementMap.put(element.getAcceleratorNode(), element);
        id2NodeMap.put(element.getAcceleratorNode().getId(), element.getAcceleratorNode());
    }

    /**
     * visit a VSteerer lattice element
     */
    @Override
    public void visit(VSteerer e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a Drift lattice element
     */
    @Override
    public void visit(Drift e) {
        // Do nothing
    }

    /**
     * visit a Quadrupole lattice element
     */
    @Override
    public void visit(Quadrupole e) {
        // Do nothing
    }

    /**
     * visit a Quadrupole lattice element
     */
    @Override
    public void visit(EQuad e) {
        // Do nothing
    }

    /**
     * visit a Solenoid lattice element
     */
    @Override
    public void visit(Solenoid e) {
        // Do nothing
    }

    /**
     * visit a WScanner lattice element
     */
    @Override
    public void visit(WScanner e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a BPMonitor lattice element
     */
    @Override
    public void visit(BPMonitor e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a BLMonitor lattice element
     */
    @Override
    public void visit(BLMonitor e) {
        node2ElementMap.put(e.getAcceleratorNode(), e);
        id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
    }

    /**
     * visit a BSMonitor lattice element
     */
    @Override
    public void visit(final BSMonitor element) {
        node2ElementMap.put(element.getAcceleratorNode(), element);
        id2NodeMap.put(element.getAcceleratorNode().getId(), element.getAcceleratorNode());
    }

    /**
     * visit a SkewQuad lattice element
     */
    @Override
    public void visit(SkewQuad e) {
        // Do nothing
    }

    /**
     * visit a Sextupole lattice element
     */
    @Override
    public void visit(Sextupole e) {
        // Do nothing
    }

    /**
     * visit a Marker lattice element
     */
    @Override
    public void visit(Marker e) {
        // Do nothing
    }

    @Override
    public void visit(EDipole e) {
        // Do nothing

    }
}
