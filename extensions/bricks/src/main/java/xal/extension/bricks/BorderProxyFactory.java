//
//  BorderProxyFactory.java
//  xal
//
//  Created by Thomas Pelaia on 7/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import javax.swing.border.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Factory for generating border proxies
 */
public class BorderProxyFactory {

    /**
     * table of proxies keyed by type
     */
    protected static Map<String, BorderProxy<Border>> proxyTable;

    // static initializer
    static {
        proxyTable = new HashMap<>();

        register(getBorderProxy(EtchedBorder.class, "Etched Border"));
        register(getLoweredBevelBorderProxy("Lowered Bevel"));
        register(getRaisedBevelBorderProxy("Raised Bevel"));
        register(getTitledBorderProxy("Titled Border"));
    }

    private BorderProxyFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * register the proxy in the proxy table
     */
    @SuppressWarnings("unchecked")    // must convert border proxy subtypes to the subtype of Border
    protected static void register(final BorderProxy<? extends Border> proxy) {
        proxyTable.put(proxy.getType(), (BorderProxy<Border>) proxy);
    }

    /**
     * get a border proxy with the specified type
     */
    public static BorderProxy<Border> getBorderProxy(final String type) {
        if (proxyTable.containsKey(type)) {
            return proxyTable.get(type);
        } else {
            final String swingType = "javax.swing.border." + type;
            return proxyTable.get(swingType);
        }
    }

    /**
     * Create a border proxy for a border with an empty constructor
     */
    public static <T extends Border> BorderProxy<T> getBorderProxy(final Class<T> borderClass, final String name) {
        return new BorderProxy<T>(borderClass) {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Create a title border proxy
     */
    public static BorderProxy<TitledBorder> getTitledBorderProxy(final String name) {
        return new BorderProxy<TitledBorder>(TitledBorder.class) {
            /**
             * Get the array of constructor arguments
             */
            @Override
            public Class<?>[] getConstructorParameterTypes() {
                return new Class<?>[]{String.class};
            }

            /**
             * Get the array of constructor arguments
             */
            @Override
            public Object[] getConstructorParameters() {
                return new Object[]{"title"};
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Create a title border proxy
     */
    public static BorderProxy<BevelBorder> getLoweredBevelBorderProxy(final String name) {
        return new BorderProxy<BevelBorder>(BevelBorder.class) {
            /**
             * Get the array of constructor arguments
             */
            @Override
            public Class<?>[] getConstructorParameterTypes() {
                return new Class<?>[]{Integer.TYPE};
            }

            /**
             * Get the array of constructor arguments
             */
            @Override
            public Object[] getConstructorParameters() {
                return new Object[]{BevelBorder.LOWERED};
            }

            @Override
            public String getName() {
                return name;
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.border.BevelBorder_Lowered";
            }
        };
    }

    /**
     * Create a title border proxy
     */
    public static BorderProxy<BevelBorder> getRaisedBevelBorderProxy(final String name) {
        return new BorderProxy<BevelBorder>(BevelBorder.class) {
            /**
             * Get the array of constructor arguments
             */
            @Override
            public Class<?>[] getConstructorParameterTypes() {
                return new Class<?>[]{Integer.TYPE};
            }

            /**
             * Get the array of constructor arguments
             */
            @Override
            public Object[] getConstructorParameters() {
                return new Object[]{BevelBorder.RAISED};
            }

            @Override
            public String getName() {
                return name;
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.border.BevelBorder_Raised";
            }
        };
    }
}
