//
//  ViewNodeTransferable.java
//  xal
//
//  Created by Thomas Pelaia on 6/30/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * transferable for transferring view nodes
 */
public class ViewNodeTransferable implements Transferable {

    /**
     * define the view node flavor
     */
    public static final DataFlavor VIEW_NODE_FLAVOR;

    /**
     * the list of flavors associated with view node transfer
     */
    public static final DataFlavor[] FLAVORS;

    /**
     * The view nodes being transferred
     */
    protected final List<BeanNode<?>> viewNodes;

    // static initializer
    static {
        VIEW_NODE_FLAVOR = new DataFlavor(ViewNode.class, "View Node");
        FLAVORS = new DataFlavor[]{VIEW_NODE_FLAVOR};
    }

    /**
     * Primary Constructor
     *
     * @param nodes The nodes being transferred
     */
    public ViewNodeTransferable(final List<BeanNode<?>> nodes) {
        viewNodes = new ArrayList<>(nodes);
    }

    /**
     * Constructor
     *
     * @param node The node to transfer
     */
    public ViewNodeTransferable(final BeanNode<?> node) {
        this(Collections.<BeanNode<?>>singletonList(node));
    }

    /**
     * Get the data being transferred which in this case is simply the list of
     * view nodes
     *
     * @param flavor The flavor of the transfer
     * @return The nodes to transfer
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) {
        return viewNodes;
    }

    /**
     * The flavors handled by this transferable which is presently just
     * VIEW_FLAVOR
     *
     * @return the array of flavors handled
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return FLAVORS;
    }

    /**
     * Test if the specified flavor is supported by this instance. Only
     * VIEW_FLAVOR is currently supported.
     *
     * @param flavor The flavor to test.
     * @return true if the flavor is among the supported flavors and false
     * otherwise.
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        for (int index = 0; index < FLAVORS.length; index++) {
            if (FLAVORS[index].equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
