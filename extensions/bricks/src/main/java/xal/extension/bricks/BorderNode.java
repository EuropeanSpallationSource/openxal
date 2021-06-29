//
//  BorderNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.border.*;
import java.util.Map;

import xal.tools.data.*;

/**
 * brick which represents a view
 */
public class BorderNode extends BeanNode<Border> {

    /**
     * data label
     */
    public static final String DATA_LABEL = "BorderNode";

    /**
     * Primary Constructor
     */
    @SuppressWarnings("unchecked")    // nothing we can do to type BorderNode any tighter without introducing a type on BorderNode
    public BorderNode(final BorderProxy<Border> borderProxy, final Map<String, Object> beanSettings, final String tag) {
        super(borderProxy, beanSettings, tag);
    }

    /**
     * Primary Constructor
     */
    public BorderNode(final BorderProxy<Border> borderProxy) {
        this(borderProxy, null, borderProxy.getName());
    }

    /**
     * Constructor
     */
    public BorderNode(final BorderNode node) {
        this(node.getBorderProxy(), node.beanSettings, node.getTag());

        setCustomBeanClassName(node.getCustomBeanClassName());
    }

    /**
     * get the bean instance
     */
    @Override
    protected Border getPrototypeBean(final BeanProxy<Border> beanProxy) {
        return beanProxy.getPrototype();
    }

    /**
     * generator
     */
    public static BorderNode getInstance(final DataAdaptor adaptor) {
        final DataAdaptor proxyAdaptor = adaptor.childAdaptor(BorderProxy.DATA_LABEL);
        final BorderProxy<Border> borderProxy = BorderProxy.getInstance(proxyAdaptor);
        final String tag = adaptor.stringValue("tag");
        final BorderNode node = new BorderNode(borderProxy, null, tag);

        node.update(adaptor);

        return node;
    }

    /**
     * Get the border.
     *
     * @return the border
     */
    public Border getBorder() {
        return beanObject;
    }

    /**
     * Get the border proxy
     *
     * @return the border proxy
     */
    public BorderProxy<Border> getBorderProxy() {
        return (BorderProxy<Border>) beanProxy;
    }

    /**
     * Determine if the brick can add the specified view
     *
     * @return true if it can add the specified view and false if not
     */
    @Override
    public boolean canAdd(final BeanProxy<?> beanProxy) {
        return false;
    }

    /**
     * refresh display
     */
    @Override
    public void refreshDisplay() {
        final BeanNode<?> node = (BeanNode<?>) getContainingBrick();
        if (node != null) {
            node.refreshDisplay();
        }
    }

    /**
     * Remove this brick from its parent
     */
    @Override
    public void removeFromParent() {
        final ViewNode parent = (ViewNode) getContainingBrick();
        if (parent.getBorderNode() == this) {
            parent.setBorderNode(null);
        }
    }

    /**
     * Display the bean's window
     */
    @Override
    public void display() {
        final ViewNode parent = (ViewNode) getContainingBrick();
        if (parent.getBorderNode() == this) {
            parent.display();
        }
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
}
