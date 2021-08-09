//
//  WindowGroupNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.beans.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import xal.tools.data.*;

/**
 * root brick to which windows are added
 */
public class RootBrick extends Brick implements ViewNodeContainer, DataListener {

    /**
     * data label
     */
    public static final String DATA_LABEL = "RootBrick";

    /**
     * list of window nodes
     */
    final List<ViewNode> windowNodes;

    /**
     * Constructor
     */
    public RootBrick() {
        windowNodes = new ArrayList<>();
    }

    /**
     * dispose of all windows
     */
    public void disposeAllWindows() {
        for (final ViewNode node : windowNodes) {
            final Component window = node.getView();
            if (window instanceof Window) {
                ((Window) window).dispose();
            }
        }
    }

    /**
     * Determine if the brick can add the specified view
     *
     * @return true if it can add the specified view and false if not
     */
    @Override
    public boolean canAdd(final BeanProxy<?> beanProxy) {
        if (beanProxy instanceof ViewProxy) {
            return ((ViewProxy) beanProxy).isWindow();
        } else {
            return false;
        }
    }

    /**
     * Get the label
     *
     * @return the label for this brick
     */
    @Override
    public String toString() {
        return "windows";
    }

    /**
     * Add the views to this node
     *
     * @param beanProxies the views to add to this node
     */
    // must cast bean proxy to view proxy
    @SuppressWarnings("unchecked")    
    @Override
    public void add(final List<BeanProxy<?>> beanProxies) {
        final List<BeanNode<?>> nodes = new ArrayList<>(beanProxies.size());
        for (final BeanProxy<?> beanProxy : beanProxies) {
            if (beanProxy instanceof ViewProxy) {
                final ViewNode node = new ViewNode((ViewProxy<Component>) beanProxy);
                windowNodes.add(node);
                node.addBrickListener(this);
                nodes.add(node);
                treeNode.add(node.getTreeNode());
                ((Window) node.getView()).setVisible(true);
            }
        }
        eventProxy.nodesAdded(this, this, nodes);
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * Get the tree index offset from the view index
     *
     * @return the tree index offset
     */
    @Override
    public int getTreeIndexOffsetFromViewIndex() {
        return 0;
    }

    /**
     * move the specified nodes down
     *
     * @param nodes
     */
    @Override
    public void moveDownNodes(final List<BeanNode<?>> nodes) {
    }

    /**
     * move the specified nodes up
     *
     * @param nodes
     */
    @Override
    public void moveUpNodes(final List<BeanNode<?>> nodes) {
    }

    /**
     * Insert the bean node in this node beginning at the specified index
     *
     * @param node the node to insert in this node
     * @param viewIndex the initial index at which to begin inserting the nodes
     */
    @Override
    public void insertViewNode(final ViewNode node, final int viewIndex) {
        windowNodes.add(viewIndex, node);
        node.addBrickListener(this);
        treeNode.insert(node.getTreeNode(), viewIndex);
        ((Window) node.getView()).setVisible(true);
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * Insert the views in this node beginning at the specified index
     *
     * @param viewProxies the views to add to this node
     */
    @Override
    public void insertSiblings(final List<BeanProxy<?>> viewProxies) {
    }

    /**
     * Add the views nodes to this node
     *
     * @param originalNodes the nodes to add to this node
     */
    @Override
    public void addNodes(final List<BeanNode<?>> originalNodes) {
        final List<BeanNode<?>> nodes = new ArrayList<>(originalNodes.size());
        for (final BeanNode<?> originalNode : originalNodes) {
            if (originalNode instanceof ViewNode) {
                final ViewNode node = new ViewNode((ViewNode) originalNode);
                windowNodes.add(node);
                node.addBrickListener(this);
                nodes.add(node);
                treeNode.add(node.getTreeNode());
            }
        }
        eventProxy.nodesAdded(this, this, nodes);
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * Insert the view nodes in this node beginning at the specified index
     *
     * @param originalNodes the nodes to add to this node
     */
    @Override
    public void insertSiblingNodes(final List<BeanNode<?>> originalNodes) {
    }

    /**
     * Remove the view node from this container
     *
     * @param node the node to remove
     */
    @Override
    public void removeNode(final BeanNode<?> node) {
        final List<BeanNode<?>> nodes = Collections.<BeanNode<?>>singletonList(node);
        removeNodes(nodes);
    }

    /**
     * Remove the view nodes from this container
     *
     * @param nodes the nodes to remove
     */
    @Override
    public void removeNodes(final List<BeanNode<?>> nodes) {
        for (final BeanNode<?> node : nodes) {
            if (node instanceof ViewNode) {
                final ViewNode viewNode = (ViewNode) node;
                viewNode.removeBrickListener(this);
                windowNodes.remove(viewNode);
                treeNode.remove(viewNode.getTreeNode());
                final Window window = (Window) viewNode.getView();
                window.dispose();
            }
        }
        eventProxy.nodesRemoved(this, this, nodes);
        eventProxy.treeNeedsRefresh(this, this);
    }

    /**
     * Remove this brick from its parent
     */
    @Override
    public void removeFromParent() {
    }

    /**
     * Provides the name used to identify the class in an external data source.
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
        final List<DataAdaptor> nodeAdaptors = adaptor.childAdaptors(ViewNode.DATA_LABEL);
        final List<BeanNode<?>> nodes = new ArrayList<>(nodeAdaptors.size());
        for (final DataAdaptor nodeAdaptor : nodeAdaptors) {
            nodeAdaptor.setValue("contextURL", adaptor.stringValue("contextURL"));
            nodes.add(ViewNode.getInstance(nodeAdaptor));
        }
        addNodes(nodes);
    }

    /**
     * Write data to the data adaptor for storage.
     *
     * @param adaptor The adaptor to which the receiver's data is written
     */
    @Override
    public void write(final DataAdaptor adaptor) {
        adaptor.writeNodes(windowNodes);
    }

    /**
     * Handle the event in which nodes have been added to a container
     *
     * @param source the source of the event
     * @param container the node to which nodes have been added
     * @param nodes the nodes which have been added
     */
    @Override
    public void nodesAdded(final Object source, final Brick container, final List<BeanNode<?>> nodes) {
        eventProxy.nodesAdded(this, container, nodes);
    }

    /**
     * Handle the event in which nodes have been removed from a container
     *
     * @param source the source of the event
     * @param container the node from which nodes have been removed
     * @param nodes the nodes which have been removed
     */
    @Override
    public void nodesRemoved(final Object source, final Brick container, final List<BeanNode<?>> nodes) {
        eventProxy.nodesRemoved(this, container, nodes);
    }

    /**
     * Handle the event in which a bean's property has been changed
     *
     * @param node the node whose property has changed
     * @param propertyDescriptor the property which has changed
     * @param value the new value
     */
    @Override
    public void propertyChanged(final BeanNode<?> node, final PropertyDescriptor propertyDescriptor, final Object value) {
        eventProxy.propertyChanged(node, propertyDescriptor, value);
    }

    /**
     * Handle the event in which a brick's tree path needs refresh
     *
     * @param source the source of the event
     * @param brick the brick at which the refresh needs to be done
     */
    @Override
    public void treeNeedsRefresh(final Object source, final Brick brick) {
        eventProxy.treeNeedsRefresh(this, brick);
    }
}
