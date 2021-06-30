/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 //www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 * http:
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package xal.extension.widgets.smf;

import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * <code>XALSynopticPanel</code> is simple panel, that shows synoptic layout of
 * selected XAL sequence. Synoptic is drawn in horizontal direction. To define
 * right or left distance of drawing from the edge of panel, use margins. To
 * define which part of sequence should be drawn, use start and end position.
 *
 * @author <a href="mailto:igor.kriznar@cosylab.com">Igor Kriznar</a>
 * @since Aug 29, 2003.
 *
 * TODO Fire events for mouse selection of elements TODO Add element selection
 * dialog for choosing single from multiple selection TODO Implement SNS
 * official color code
 */
public class XALSynopticPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(XALSynopticPanel.class.getName());

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    private double startPosition;
    private double endPosition;
    private AcceleratorSeq acceleratorSequence;
    private ArrayList<AcceleratorNode> thick = new ArrayList<>();
    private ArrayList<AcceleratorNode> thin = new ArrayList<>();
    private Insets margin;
    private String[] labels = new String[0];
    /**
     * relevant for rings; it specifies the shift in wrap location where
     * negative numbers start
     */
    private double wrapShift;

    /**
     * Default constructor.
     */
    public XALSynopticPanel() {
        super();
        wrapShift = 0.0;

        setBackground(Color.white);
        margin = new Insets(30, 20, 30, 20);
        setOpaque(true);
        setToolTipText("XAL Synoptics");
    }

    /**
     * This runs simple test applet.
     *
     * @param args CMD args
     */
    public static void main(String[] args) {
        try {
            XALSynopticPanel pane = new XALSynopticPanel();
            Accelerator acc = XMLDataManager.loadDefaultAccelerator();

            pane.setAcceleratorSequence(acc.getSequence("MEBT"));

            JFrame frame = new JFrame();
            frame.setSize(300, 200);
            frame.getContentPane().add(pane);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.setVisible(true);
        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Get the accelerator sequence, which is displayed from first to last by
     * synoptic defined element.
     *
     * @return the accelerator sequence, which is displayed from first to last
     * by synoptic defined element
     */
    public AcceleratorSeq getAcceleratorSequence() {
        return acceleratorSequence;
    }

    /**
     * Sets accelerator sequence. Note that this method might take some time to
     * finish, if sequence contains a lot of elements. Uses beginning and end of
     * this sequence for initial start and end position.
     *
     * @param seq
     */
    public void setAcceleratorSequence(AcceleratorSeq seq) {
        if (seq != acceleratorSequence) {		// need to check to avoid side effects of resetting the start end breaking synchronization with plot
            setAcceleratorSequence(seq, 0.0, 0.0);
        }
    }

    /**
     * Sets accelerator sequence and initial start and end position in sequence.
     * Note that this method might take some time to finish, if sequence
     * contains a lot of elements.
     *
     * @param seq
     */
    public void setAcceleratorSequence(AcceleratorSeq seq, double start, double end) {
        acceleratorSequence = seq;

        if (start < end) {
            startPosition = start;
            endPosition = end;
        } else {
            startPosition = end;
            endPosition = start;
        }

        updateSequence();
    }

    /**
     * Set the shift in the location where the wrapping occurs (relevant only
     * for rings)
     *
     * @param shift the shift (meters) in location along the ring where
     * positions are measured in positive versus negative numbers relative to
     * the origin
     */
    public void setWrapShift(final double shift) {
        wrapShift = shift;
        repaint();
    }

    private void updateSequence() {
        thick.clear();
        thin.clear();

        if (acceleratorSequence == null) {
            repaint();
            return;
        }

        final List<AcceleratorNode> list = acceleratorSequence.getAllNodes();

        // set initial size from sequence
        if (list.size() > 0 && startPosition == endPosition) {
            startPosition = acceleratorSequence.getPosition(list.get(0)) - list.get(0).getLength() / 2.0;
            endPosition = acceleratorSequence.getPosition(list.get(list.size() - 1)) + list.get(list.size() - 1).getLength() / 2.0;
        }

        final ArrayList<AcceleratorNode> newThick = new ArrayList<>(list.size());
        final ArrayList<AcceleratorNode> newThin = new ArrayList<>(list.size());

        for (final AcceleratorNode el : list) {
            double pos = acceleratorSequence.getPosition(el);

            if (pos >= startPosition && pos <= endPosition) {
                if (el instanceof Bend || el instanceof Quadrupole || el instanceof PermQuadrupole || el instanceof RfGap || el instanceof RfCavity) {
                    newThick.add(el);
                } else {
                    newThin.add(el);
                }
            }
        }

        thick = newThick;
        thin = newThin;
        repaint();
    }

    /**
     * Get the position in sequence up to which elements are drawn.
     *
     * @return the position in sequence up to which elements are drawn
     */
    public double getEndPosition() {
        return endPosition;
    }

    /**
     * Get the position in sequence from which elements are drawn.
     *
     * @return the position in the sequence from which elements are drawn
     */
    public double getStartPosition() {
        return startPosition;
    }

    /**
     * Sets position in sequence up to which elements are drawn. Must be more
     * than start position.
     *
     * @param d
     *
     * @throws IllegalArgumentException if new end is less then start position
     */
    public void setEndPosition(double d) {
        if (d < startPosition) {
            throw new IllegalArgumentException("New end position (" + d
                    + ") is less than start position (" + startPosition + ").");
        }

        endPosition = d;
        repaint();
    }

    /**
     * Sets position in sequence from which elements are drawn. Must be less
     * than end position.
     *
     * @param d
     *
     * @throws IllegalArgumentException if new start is more than end position
     */
    public void setStartPosition(double d) {
        if (d > endPosition) {
            throw new IllegalArgumentException("New start position (" + d
                    + ") is more than end position (" + endPosition + ").");
        }

        startPosition = d;
        repaint();
    }

    /* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        double scale = endPosition - startPosition;

        int width = getWidth() - margin.right - margin.left;
        int height = getHeight() - margin.top - margin.bottom;

        if (width <= 0 || height <= 0) {
            return;
        }

        if (width != labels.length) {
            labels = new String[width];
        }

        Arrays.fill(labels, null);

        int x = margin.left;
        int y = margin.top + (int) (height / 2.0);

        // draw line
        g.setColor(Color.black);

        g.drawLine(x, y, x + width - 1, y);
        g.drawLine(x, y - 3, x, y + 3);
        g.drawLine(x + width - 1, y - 3, x + width - 1, y + 3);

        if (thin == null || thick == null) {
            return;
        }

        final double sequenceLength = acceleratorSequence != null ? acceleratorSequence.getLength() : 0.0;
        final double wrapLocation = sequenceLength - wrapShift;

        // first draw thick elements
        for (final AcceleratorNode el : thick) {
            String name = el.getId();
            double pos = acceleratorSequence.getPosition(el);

            final double wrappedPosition = pos <= wrapLocation ? pos : pos - sequenceLength;
            if (wrappedPosition + el.getLength() < startPosition || wrappedPosition - el.getLength() > endPosition) {
                continue;
            }

            int ex = x + (int) ((wrappedPosition - el.getLength() / 2.0 - startPosition) / scale * (width - 1));
            int l = (int) (el.getLength() / scale * (width - 1));

            if (l < 2) {
                l = 2;
            }

            if (el instanceof Dipole) {
                g.setColor(Color.yellow);
                g.fillRect(ex, margin.top, l, height);

                for (int i = ex - x; i < ex - x + l; i++) {
                    addLabel(i, name);
                }
            } else if (el instanceof Quadrupole || el instanceof PermQuadrupole) {
                g.setColor(Color.red);
                g.fillRect(ex, margin.top, l, height);

                for (int i = ex - x; i < ex - x + l; i++) {
                    addLabel(i, name);
                }
            } else if (el instanceof Solenoid) {
                g.setColor(Color.green);
                g.fillRect(ex, margin.top, l, height);

                for (int i = ex - x; i < ex - x + l; i++) {
                    addLabel(i, name);
                }
            } else if (el instanceof RfGap) {
                g.setColor(Color.gray);

                if (l < 3) {
                    g.fillRect(ex, margin.top, l, height);
                } else {
                    g.fillRoundRect(ex, margin.top, l, height, height / 5,
                            height / 5);
                }

                for (int i = ex - x; i < ex - x + l; i++) {
                    addLabel(i, name);
                }
            } else if (el instanceof RfCavity) {
                g.setColor(Color.gray);

                if (l < 3) {
                    g.drawRect(ex, margin.top - 1, l, height + 1);
                } else {
                    g.drawRoundRect(ex, margin.top - 1, l, height + 1,
                            height / 10, height / 10);
                }

                for (int i = ex - x; i < ex - x + l; i++) {
                    addLabel(i, name);
                }
            }
        }

        // Draw thin elements
        for (final AcceleratorNode el : thin) {
            String name = el.getId();
            double pos = acceleratorSequence.getPosition(el);

            final double wrappedPosition = pos <= wrapLocation ? pos : pos - sequenceLength;
            if (wrappedPosition + el.getLength() < startPosition || wrappedPosition - el.getLength() > endPosition) {
                continue;
            }

            if (el instanceof HDipoleCorr || el instanceof VDipoleCorr) {
                int ex = x + (int) ((wrappedPosition - startPosition) / scale * (width - 1));
                g.setColor(Color.blue);
                g.fillRect(ex, margin.top, 2, height);

                for (int i = ex - x; i < ex - x + 2; i++) {
                    addLabel(i, name);
                }
            } else if (el instanceof BPM || el instanceof ProfileMonitor || el instanceof Marker) {
                int ex = x + (int) ((wrappedPosition - startPosition) / scale * (width - 1));
                if (el instanceof BPM) {
                    g.setColor(Color.cyan);
                } else if (el instanceof ProfileMonitor) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(Color.GRAY);
                }

                g.fillRect(ex, margin.top, 1, height);

                for (int i = ex - x; i < ex - x + 1; i++) {
                    addLabel(i, name);
                }
            }
        }
    }

    /**
     * Get the margin around drawing.
     *
     * @return the margin around drawing
     */
    public Insets getMargin() {
        return margin;
    }

    /**
     * Sets the margin around drawing, takes effect regardless the border
     * margins.
     *
     * @param insets
     */
    public void setMargin(Insets insets) {

        margin = insets;
        repaint();
    }

    private void addLabel(int index, String label) {
        if (index >= labels.length || index < 0) {
            return;
        }

        if (labels[index] == null) {
            labels[index] = label;
        } else {
            labels[index] = labels[index] + ", " + label;
        }
    }

    /* (non-Javadoc)
	 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        int i = event.getPoint().x - margin.left;

        if (i >= 0 && i < labels.length) {
            int i_upper = i;
            int i_down = i;
            while (i_upper < (labels.length - 1) && labels[i_upper] == null) {
                i_upper += 1;
            }
            while (i_down > 0 && labels[i_down] == null) {
                i_down -= 1;
            }
            if (Math.abs(i - i_down) < Math.abs(i_upper - i)) {
                return labels[i_down];
            } else {
                return labels[i_upper];
            }

        } else {
            return super.getToolTipText(event);
        }
    }
}
