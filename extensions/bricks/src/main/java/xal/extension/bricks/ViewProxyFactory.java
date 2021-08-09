//
//  ViewProxyFactory.java
//  xal
//
//  Created by Thomas Pelaia on 7/3/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.bricks;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.beans.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.text.*;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import xal.extension.widgets.plot.*;

/**
 * factory for making view proxies
 */
public class ViewProxyFactory {

    /**
     * table of proxies keyed by type
     */
    protected static Map<String, ViewProxy<?>> proxyTable;

    // static initializer
    static {
        proxyTable = new HashMap<>();

        register(getFrameProxy("Window"));
        register(getDialogProxy("Dialog"));

        register(getScrollPaneProxy());
        register(getSplitPaneProxy());
        register(getTabbedPaneProxy());
        register(getContainerProxy(JPanel.class, "Panel"));
        register(getContainerProxy(JToolBar.class, "ToolBar"));
        register(getHorizontalBoxProxy());
        register(getVerticalBoxProxy());
        register(getHorizontalGlueProxy());
        register(getVerticalGlueProxy());

        register(getListProxy());
        register(getTableProxy());
        register(getTreeProxy());
        register(getFunctionGraphsJPanelProxy());

        register(getComponentProxy(JLabel.class, "Label", "label text", false));
        register(getComponentProxy(JTextField.class, "Text Field", "text field", false));
        register(getComponentProxy(JPasswordField.class, "Password Field", "text field", false));
        register(getComponentProxy(JFormattedTextField.class, "Formatted Text Field", false));
        register(getComponentProxy(JTextArea.class, "Text Area", "Hello, World!", false));
        register(getComponentProxy(JTextPane.class, "Text Pane", false));
        register(getComponentProxy(JEditorPane.class, "Editor Pane", false));

        register(getComponentProxy(JButton.class, "Button", "Button", true));
        register(getComponentProxy(JToggleButton.class, "Toggle Button", "Toggle", true));
        register(getComponentProxy(JCheckBox.class, "Check Box", "item", true));
        register(getComponentProxy(JRadioButton.class, "Radio Button", "item", true));
        register(getComponentProxy(JSpinner.class, "Spinner", false));
        register(getComponentProxy(JProgressBar.class, "Progress Bar", false));
        register(getComponentProxy(JSlider.class, "Slider"));
        register(getComboBoxProxy());
    }

    private ViewProxyFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * register the proxy in the proxy table
     */
    protected static void register(final ViewProxy<?> proxy) {
        proxyTable.put(proxy.getType(), proxy);
    }

    /**
     * get a border proxy with the specified type
     */
    public static ViewProxy<?> getViewProxy(final String type) {
        if (proxyTable.containsKey(type)) {
            return proxyTable.get(type);
        } else {
            final String swingType = "javax.swing." + type;
            return proxyTable.get(swingType);
        }
    }

    /**
     * Create a view proxy for a component with an empty constructor
     */
    public static <T extends Component> ViewProxy<T> getViewProxy(final Class<T> viewClass, final String name, final boolean isContainer, final boolean makeIcon) {
        return new ViewProxy<T>(viewClass, isContainer, makeIcon) {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Create a view proxy for a component with a constructor that takes a
     * string argument
     */
    public static <T extends Component> ViewProxy<T> getViewProxy(final Class<T> viewClass, final String name, final String text, final boolean isContainer, final boolean makeIcon) {
        return new ViewProxy<T>(viewClass, isContainer, makeIcon) {
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
                return new Object[]{text};
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getContainerProxy(final Class<T> viewClass, final String name, final String text) {
        return getViewProxy(viewClass, name, text, true, false);
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getContainerProxy(final Class<T> viewClass, final String name) {
        return getViewProxy(viewClass, name, true, false);
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getComponentProxy(final Class<T> viewClass, final String name, final String text, final boolean makeIcon) {
        return getViewProxy(viewClass, name, text, false, makeIcon);
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getComponentProxy(final Class<T> viewClass, final String name, final String text) {
        return getComponentProxy(viewClass, name, text, false);
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getComponentProxy(final Class<T> viewClass, final String name, final boolean makeIcon) {
        return getViewProxy(viewClass, name, false, makeIcon);
    }

    /**
     * Create a view proxy for a component
     */
    public static <T extends Component> ViewProxy<T> getComponentProxy(final Class<T> viewClass, final String name) {
        return getComponentProxy(viewClass, name, false);
    }

    /**
     * Generate a view proxy for a combo box view
     */
    public static ViewProxy<JTabbedPane> getTabbedPaneProxy() {
        return new ViewProxy<JTabbedPane>(JTabbedPane.class, true, false) {
            /**
             * handle child node property change
             */
            @Override
            public void handleChildNodePropertyChange(final ViewNode node, final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value) {
                if (beanNode instanceof ViewNode && propertyDescriptor.getName().equals("name")) {
                    final int viewIndex = node.getViewIndex((ViewNode) beanNode);
                    if (viewIndex >= 0) {
                        ((JTabbedPane) node.getView()).setTitleAt(viewIndex, value.toString());
                        node.refreshDisplay();
                    }
                }
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Tabbed Pane";
            }
        };
    }

    /**
     * Generate a view proxy for a combo box view
     */
    // TODO: JComboBox is typed in Java 7 but not earlier
    @SuppressWarnings({"unchecked", "rawtypes"})    
    public static ViewProxy<JComboBox> getComboBoxProxy() {
        return new ViewProxy<JComboBox>(JComboBox.class, false, false) {
            @Override
            public void setupPrototype(final JComboBox comboBox) {
                comboBox.addItem("Oak Ridge National Lab");
                comboBox.addItem("Argonne National Lab");
                comboBox.addItem("Los Alamos National Lab");
                comboBox.addItem("Fermi National Lab");
                comboBox.addItem("Lawrence Livermore National Lab");
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Combo Box";
            }
        };
    }

    /**
     * Generate a view proxy for a list view
     */
    // TODO: JList is typed in Java 7 but not earlier
    @SuppressWarnings({"rawtypes", "unchecked"})    
    public static ViewProxy<JList> getListProxy() {
        return new ViewProxy<JList>(JList.class, false, false) {
            /**
             * setup the list data
             */
            @Override
            public void setupPrototype(final JList list) {
                final Object[] data = {"Oak Ridge", "Knoxville", "Nashville", "Chattanooga", "Memphis", "Pigeon Forge", "Gatlinburg", "Kingston", "Kingsport", "Johnson City", "Sweetwater", "Crossville", "Jefferson City", "Cleveland", "Alcoa", "Maryville"};
                list.setListData(data);
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "List";
            }
        };
    }

    /**
     * Generate a view proxy for creating a table
     */
    public static ViewProxy<JTable> getTableProxy() {
        return new ViewProxy<JTable>(JTable.class, false, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public void setupPrototype(final JTable table) {
                table.setModel(new AbstractTableModel() {
                    /**
                     * serialization ID
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public int getRowCount() {
                        return 50;
                    }

                    @Override
                    public String getColumnName(final int column) {
                        switch (column) {
                            case 0:
                                return "Value";
                            case 1:
                                return "Square";
                            case 2:
                                return "Cube";
                            default:
                                return "";
                        }
                    }

                    @Override
                    public Class<?> getColumnClass(final int column) {
                        return Number.class;
                    }

                    @Override
                    public int getColumnCount() {
                        return 3;
                    }

                    @Override
                    public Object getValueAt(final int row, final int column) {
                        switch (column) {
                            case 0:
                                return row;
                            case 1:
                                return row * row;
                            case 2:
                                return row * row * row;
                            default:
                                return null;
                        }
                    }
                });
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Table";
            }
        };
    }

    /**
     * Generate a view proxy for creating a table
     */
    public static ViewProxy<JTree> getTreeProxy() {
        return new ViewProxy<JTree>(JTree.class, false, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public void setupPrototype(final JTree tree) {
                final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Stuff");
                tree.setModel(new DefaultTreeModel(rootNode));
                final DefaultMutableTreeNode redNode = new DefaultMutableTreeNode("Red");
                rootNode.add(redNode);
                redNode.add(new DefaultMutableTreeNode("x"));
                redNode.add(new DefaultMutableTreeNode("y"));
                redNode.add(new DefaultMutableTreeNode("z"));
                final DefaultMutableTreeNode whiteNode = new DefaultMutableTreeNode("White");
                rootNode.add(whiteNode);
                whiteNode.add(new DefaultMutableTreeNode("This is a test"));
                whiteNode.add(new DefaultMutableTreeNode("Hello, World!"));
                whiteNode.add(new DefaultMutableTreeNode("Testing 1, 2, 3"));
                whiteNode.add(new DefaultMutableTreeNode("Enough with all the testing!"));
                final DefaultMutableTreeNode blueNode = new DefaultMutableTreeNode("Blue");
                rootNode.add(blueNode);
                blueNode.add(new DefaultMutableTreeNode("Beef"));
                blueNode.add(new DefaultMutableTreeNode("Tomato"));
                blueNode.add(new DefaultMutableTreeNode("Cucumber"));
                blueNode.add(new DefaultMutableTreeNode("Chicken"));
                blueNode.add(new DefaultMutableTreeNode("Salmon"));
                blueNode.add(new DefaultMutableTreeNode("Flounder"));
                blueNode.add(new DefaultMutableTreeNode("Shrimp"));
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Tree";
            }
        };
    }

    /**
     * Generate a view proxy for creating check boxes
     */
    public static ViewProxy<JScrollPane> getScrollPaneProxy() {
        return new ViewProxy<JScrollPane>(JScrollPane.class, true, false) {
            /**
             * Get the view's container to which to add sub components
             */
            @Override
            public Container getContainer(final JScrollPane view) {
                return view.getViewport();
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Scroll Pane";
            }
        };
    }

    /**
     * Generate a view proxy for creating a split pane
     */
    public static ViewProxy<JSplitPane> getSplitPaneProxy() {
        return new ViewProxy<JSplitPane>(JSplitPane.class, true, false) {
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
                return new Object[]{JSplitPane.HORIZONTAL_SPLIT};
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Split Pane";
            }
        };
    }

    /**
     * Generate a view proxy for creating horizontal boxes
     */
    public static ViewProxy<Box> getHorizontalBoxProxy() {
        return new ViewProxy<Box>(Box.class, true, false) {
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
                return new Object[]{BoxLayout.X_AXIS};
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Horizontal Box";
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.Box_Horizontal";
            }
        };
    }

    /**
     * Generate a view proxy for creating vertical boxes
     */
    public static ViewProxy<Box> getVerticalBoxProxy() {
        return new ViewProxy<Box>(Box.class, true, false) {
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
                return new Object[]{BoxLayout.Y_AXIS};
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Vertical Box";
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.Box_Vertical";
            }
        };
    }

    /**
     * Generate a view proxy for creating horizontal glue
     */
    public static ViewProxy<Component> getHorizontalGlueProxy() {
        return new ViewProxy<Component>(Component.class, false, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public Component getBeanInstance(final Class<Component> theClass) {
                return Box.createHorizontalGlue();
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Horizontal Glue";
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.Box_HorizontalGlue";
            }
        };
    }

    /**
     * Generate a view proxy for creating horizontal glue
     */
    public static ViewProxy<Component> getVerticalGlueProxy() {
        return new ViewProxy<Component>(Component.class, false, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public Component getBeanInstance(final Class<Component> theClass) {
                return Box.createVerticalGlue();
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Vertical Glue";
            }

            /**
             * get the type of the prototype
             */
            @Override
            public String getType() {
                return "javax.swing.Box_VerticalGlue";
            }
        };
    }

    /**
     * Generate a view proxy for creating a function graph panel
     */
    public static ViewProxy<FunctionGraphsJPanel> getFunctionGraphsJPanelProxy() {
        return new ViewProxy<FunctionGraphsJPanel>(FunctionGraphsJPanel.class, false, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public void setup(final FunctionGraphsJPanel plot) {
                plot.setName("Demo");
                plot.setAxisNameX("x");
                plot.setAxisNameY("y");
                plot.setNumberFormatX(new DecimalFormat("0.00E0"));
                plot.setNumberFormatY(new DecimalFormat("0.00E0"));
                plot.setLegendPosition(FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY);
                plot.setLegendKeyString("Legend");
                plot.setLegendBackground(Color.lightGray);
                plot.setLegendColor(Color.BLUE);
                plot.setLegendVisible(true);
            }

            /**
             * Create an instance of the specified view
             */
            @Override
            public void setupPrototype(final FunctionGraphsJPanel plot) {
                final BasicGraphData graphData = new BasicGraphData();
                graphData.setGraphColor(Color.BLUE);
                graphData.setGraphProperty(plot.getLegendKeyString(), "Trend");
                for (double x = 0.0; x < 20.0; x++) {
                    graphData.addPoint(x, x * x);
                }
                final Vector<BasicGraphData> series = new Vector<>(1);
                series.add(graphData);
                plot.addGraphData(series);
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "Function Graph Panel";
            }
        };
    }

    /**
     * Generate a view proxy for creating horizontal glue
     */
    public static ViewProxy<JFrame> getFrameProxy(final String title) {
        return new ViewProxy<JFrame>(JFrame.class, true, false) {
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
                return new Object[]{title};
            }

            /**
             * Create an instance of the specified view
             */
            @Override
            public void setup(final JFrame frame) {
                frame.setSize(500, 400);
                frame.setResizable(true);
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "JFrame";
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getJavaReferenceSnippet(final BeanNode<?> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("WindowReference windowReference = new WindowReference( url, ");
                buffer.append("\"").append(node.getTag()).append("\", arg1, arg2 );");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJavaReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getXALReferenceSnippet(final BeanNode<JFrame> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("WindowReference windowReference = Application.getAdaptor().getDefaultWindowReference( ");
                buffer.append("\"").append(node.getTag()).append("\", arg1, arg2 );");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJavaReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getJythonReferenceSnippet(final BeanNode<?> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("window_reference = WindowReference( url, ");
                buffer.append("\"").append(node.getTag()).append("\", [arg1, arg2] )");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJythonReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * Get the reference snippet method name
             *
             * @return the method name
             */
            @Override
            protected String getReferenceSnippetFetchMethodName() {
                return "getWindow";
            }

            /**
             * Get the reference snippet method arguments
             *
             * @return the method arguments
             */
            @Override
            protected String getReferenceSnippetFetchMethodArgumentsString(final BeanNode<?> node) {
                return "";
            }
        };
    }

    /**
     * Generate a view proxy for creating horizontal glue
     */
    public static ViewProxy<JDialog> getDialogProxy(final String title) {
        return new ViewProxy<JDialog>(JDialog.class, true, false) {
            /**
             * Create an instance of the specified view
             */
            @Override
            public void setup(final JDialog dialog) {
                dialog.setTitle(title);
                dialog.setSize(500, 400);
                dialog.setResizable(true);
            }

            /**
             * get the name of the prototype
             */
            @Override
            public String getName() {
                return "JDialog";
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getJavaReferenceSnippet(final BeanNode<?> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("WindowReference windowReference = new WindowReference( url, ");
                buffer.append("\"").append(node.getTag()).append("\", arg1, arg2 );");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJavaReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getXALReferenceSnippet(final BeanNode<JDialog> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("WindowReference windowReference = Application.getAdaptor().getDefaultWindowReference( ");
                buffer.append("\"").append(node.getTag()).append("\", arg1, arg2 );");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJavaReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * get the java reference snippet
             */
            @Override
            public String getJythonReferenceSnippet(final BeanNode<?> node) {
                final StringBuilder buffer = new StringBuilder();
                buffer.append("window_reference = WindowReference( url, ");
                buffer.append("\"").append(node.getTag()).append("\", [arg1, arg2] )");
                buffer.append(System.getProperty("line.separator"));
                buffer.append(super.getJythonReferenceSnippet(node));
                return buffer.toString();
            }

            /**
             * Get the reference snippet method name
             *
             * @return the method name
             */
            @Override
            protected String getReferenceSnippetFetchMethodName() {
                return "getWindow";
            }

            /**
             * Get the reference snippet method arguments
             *
             * @return the method arguments
             */
            @Override
            protected String getReferenceSnippetFetchMethodArgumentsString(final BeanNode<?> node) {
                return "";
            }
        };
    }
}
