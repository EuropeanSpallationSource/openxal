/*
 * VerticalLayout.java
 *
 * Created on June 10, 2004, 2:44 PM
 */
package xal.tools.apputils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * The custom layout manager. This layout manager store components vertically
 * and provides width that is the maximal one among components.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public class VerticalLayout implements LayoutManager {

    private int vgap;
    private int minWidth = 0;
    private int minHeight = 0;
    private int preferredWidth = 0;
    private int preferredHeight = 0;
    private boolean sizeUnknown = true;

    /* Constructor with vgap = 2. */
    public VerticalLayout() {
        this(2);
    }

    /* Constructor with the vertical gap value parameter. */
    public VerticalLayout(int v) {
        vgap = v;
    }

    /* Required by LayoutManager. */
    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    /* Required by LayoutManager. */
    @Override
    public void removeLayoutComponent(Component comp) {
    }

    private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;

        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;

        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();

                if (i > 0) {
                    preferredHeight += vgap;
                }
                preferredHeight += d.height;

                minWidth = Math.max(c.getPreferredSize().width, minWidth);
                preferredWidth = minWidth;
                minHeight = preferredHeight;
            }
        }
    }


    /* Required by LayoutManager. */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth
                + insets.left + insets.right;
        dim.height = preferredHeight
                + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

    /* Required by LayoutManager. */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = minWidth
                + insets.left + insets.right;
        dim.height = minHeight
                + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

    /* Required by LayoutManager. */
 /*
     * This is called when the panel is first displayed,
     * and every time its size changes.
     * Note: You CAN'T assume preferredLayoutSize or
     * minimumLayoutSize will be called -- in the case
     * of applets, at least, they probably won't be.
     */
    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width
                - (insets.left + insets.right);
        int maxHeight = parent.getSize().height
                - (insets.top + insets.bottom);
        int nComps = parent.getComponentCount();
        int previousHeight = 0;
        int x = insets.left, y = insets.top;

        // Go through the components' sizes, if neither
        // preferredLayoutSize nor minimumLayoutSize has
        // been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();

                // increase x and y, if appropriate
                if (i > 0) {
                    y += previousHeight + vgap;
                }

                // Set the component's size and position.
                //all component have the same width - maximal
                c.setBounds(x, y, minWidth, d.height);

                //old variant - all components have minimal width
                previousHeight = d.height;
            }
        }
    }

    /**
     * Returns the string that describes an instance.
     */
    @Override
    public String toString() {
        String str = "";
        return getClass().getName() + "[vgap=" + vgap + str + "]";
    }

    /**
     * The main method of the application.
     */
    public static void main(String[] args) {

        JFrame mainFrame = new JFrame("Test of VerticalLayout class");
        mainFrame.addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        }
        );

        mainFrame.getContentPane().setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new VerticalLayout(0));
        mainFrame.getContentPane().add(panel, BorderLayout.WEST);

        JTextField txt1 = new JTextField("aaa");
        JTextField txt2 = new JTextField(8);
        txt2.setText("=========aaa===========");
        txt2.setFont(new Font(txt1.getFont().getFamily(), Font.BOLD, 50));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(0, 1, 0, 0));

        JLabel label1 = new JLabel("Label 1 ", SwingConstants.CENTER);
        JLabel label2 = new JLabel("Label 2 ", SwingConstants.CENTER);
        panel1.add(label1);
        panel1.add(label2);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        JLabel label3 = new JLabel("Text 3: ", SwingConstants.CENTER);
        JTextField txt3 = new JTextField(" ===========text field 3====");
        panel2.add(label3);
        panel2.add(txt3);
        panel2.setBackground(Color.red);

        panel.add(txt1);
        panel.add(txt2);
        panel.add(panel1);
        panel.add(panel2);

        mainFrame.pack();
        mainFrame.setVisible(true);

    }

}
