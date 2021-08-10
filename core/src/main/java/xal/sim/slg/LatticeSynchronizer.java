/*
 * LatticeSynchronizer.java
 *
 * Created on June 2, 2003, 3:55 PM
 */
package xal.sim.slg;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlWriter;
import xal.tools.xml.XmlDataAdaptor.WriteException;
import xal.model.elem.sync.IElectromagnet;
import xal.sim.mpx.ModelProxy;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;
import xal.smf.impl.Electrostatic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.w3c.dom.Document;
import xal.ca.GetException;

/**
 * A visitor generating an XML document for the lattice. This document is not
 * compatible with the on-line model. To make the document compatible with the
 * on-line model it has to be transformed with an XSL stylesheet.
 *
 * @author wdklotz
 */
public class LatticeSynchronizer implements Visitor {

    private static final Logger LOGGER = Logger.getLogger(LatticeSynchronizer.class.getName());

    /**
     * root adaptor for xml-document
     */
    private XmlDataAdaptor docAdptr;
    /**
     * adaptor for lattice tag
     */
    private DataAdaptor latAdptr;
    /**
     * adaptor for sequence tag
     */
    private DataAdaptor seqAdptr;
    /**
     * adaptor for element tag
     */
    private DataAdaptor elmAdptr;
    /**
     * adaptor for parameter tag
     */
    private DataAdaptor parAdptr;
    /**
     * adaptor for comment tag
     */
    private DataAdaptor comAdptr;
    private String paramSrc;
    /**
     * number formater
     */
    private static final NumberFormat FMT = Lattice.FMT;

    private static final String DOC_TYPE;
    private static final String ELM_TAG;
    private static final String COM_TAG;
    private static final String DTD;

    static {
        DOC_TYPE = "Lattice";
        ELM_TAG = "Element";
        COM_TAG = "comment";
        DTD = "Lattice.mod.xal.dtd";
    }

    /**
     * Creates a new instance of LatticeSynchronizer
     */
    public LatticeSynchronizer(Lattice lattice) {
        this(lattice, ModelProxy.PARAMSRC_DESIGN);
    }

    /**
     * Creates a new instance of LatticeSynchronizer
     */
    public LatticeSynchronizer(Lattice lattice, String paramSrc) {
        // use CA ?
        this.paramSrc = paramSrc;

        //the xml-document-adaptor: creates the <!DOCTYPE ...> declaration
        docAdptr = XmlDataAdaptor.newEmptyDocumentAdaptor(DOC_TYPE, DTD);

        // the Lattice tag: creates the <Lattice .../> root tag
        latAdptr = docAdptr.createChild(DOC_TYPE);
        latAdptr.setValue("id", lattice.getName());
        latAdptr.setValue("ver", " ");
        latAdptr.setValue("author", "W.-D. Klotz");

        // the comment tag: creates a <comment ..../> tag
        comAdptr = latAdptr.createChild(COM_TAG);
        comAdptr.setValue("text", "document generated from " + Lattice.version());

        // the Sequence tag: creates the <Sequence id="xxx" ..../> tag
        seqAdptr = latAdptr.createChild("Sequence");
        seqAdptr.setValue("id", lattice.getName());

        // go and visit all lattice elments ...
        LatticeIterator liter = lattice.latticeIterator();
        while (liter.hasNext()) {
            liter.next().accept(this);
        }
    }

    /**
     * Writes the <Element .../> tag to the xml document.
     */
    private void writeElementTag(Element e) {
        elmAdptr = seqAdptr.createChild(ELM_TAG);
        elmAdptr.setValue("fam", e.getFam());
        elmAdptr.setValue("type", e.getType());
        elmAdptr.setValue("id", e.getName());
        elmAdptr.setValue("length", FMT.format(e.getLength()));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "StartPosition");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", FMT.format(e.getStartPosition()));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Position");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", FMT.format(e.getPosition()));
    }

    /**
     * Returns the whole lattice document as a string.
     */
    @Override
    public String toString() {
        StringWriter sout = new StringWriter();
        docAdptr.writeTo(sout);
        return sout.toString();
    }

    /**
     * Returns the whole lattice document as a DOM object.
     */
    public Document getDocument() {
        return docAdptr.document();
    }

    /**
     * Write XML to the specified url
     */
    public void writeTo(java.io.Writer writer) {
        XmlWriter.writeToWriter(docAdptr.document(), writer);
    }

    /**
     * Convenience method for writing an XML file
     */
    public void writeTo(File file) throws IOException {
        writeTo(new FileWriter(file));
    }

    /**
     * Write XML to the specified url
     */
    public void writeToUrlSpec(String urlSpec) throws WriteException {
        try {
            XmlWriter.writeToUrlSpec(docAdptr.document(), urlSpec);
        } catch (IOException excpt) {
            throw new WriteException(excpt);
        }
    }

    /**
     * Write XML to the specified url
     */
    public void writeToUrl(java.net.URL url) throws WriteException {
        try {
            XmlWriter.writeToUrl(docAdptr.document(), url);
        } catch (IOException excpt) {
            throw new WriteException(excpt);
        }
    }

    /**
     * Writes the parameters of a RFGap lattice element
     */
    @Override
    public void visit(RFGap e) {
        writeElementTag(e);
        RfGap rfgap = (RfGap) e.getAcceleratorNode();
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Frequency");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getGapFrequencyWrapper(rfgap)));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Phase");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getRfGapPhaseAvgWrapper(rfgap)));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "ETL");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getRfGapE0TLWrapper(rfgap)));
    }

    /**
     * Writes the element- and parameter-tags of a PermMarker lattice element
     */
    @Override
    public void visit(PermMarker e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a SkewSext lattice element
     */
    @Override
    public void visit(SkewSext e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of an Octupole lattice element
     */
    @Override
    public void visit(Octupole e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a BCMonitor lattice element
     */
    @Override
    public void visit(BCMonitor e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a BSMonitor lattice element
     */
    @Override
    public void visit(final BSMonitor element) {
        writeElementTag(element);
    }

    /**
     * Writes the element- and parameter-tags of a HSteerer lattice element
     */
    @Override
    public void visit(HSteerer e) {
        writeElementTag(e);
        Magnet magnet = (Magnet) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a Dipole lattice element
     */
    @Override
    public void visit(Dipole e) {
        writeElementTag(e);
        Magnet magnet = (Magnet) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "magField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength() * e.getLength() / magnet.getLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    @Override
    public void visit(EDipole e) {
        // TODO Auto-generated method stub
        writeElementTag(e);
        xal.smf.impl.EDipole edipole = (xal.smf.impl.EDipole) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "magField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(edipole)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = edipole.getEffLength() * e.getLength() / edipole.getLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");
        int orientation = edipole.getOrientation();

        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a VSteerer lattice element
     */
    @Override
    public void visit(VSteerer e) {
        writeElementTag(e);
        Magnet magnet = (Magnet) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a EKicker lattice element
     */
    @Override
    public void visit(final EKicker element) {
        writeElementTag(element);
        final Magnet magnet = (Magnet) element.getAcceleratorNode();
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a Drift lattice element
     */
    @Override
    public void visit(Drift e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a Quadrupole lattice element
     */
    @Override
    public void visit(Quadrupole e) {
        writeElementTag(e);
        Magnet magnet = (Magnet) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength() * e.getLength() / magnet.getLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a Quadrupole lattice element
     */
    @Override
    public void visit(EQuad e) {
        writeElementTag(e);
        xal.smf.impl.EQuad magnet = (xal.smf.impl.EQuad) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength() * e.getLength() / magnet.getLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "Orientation");
        parAdptr.setValue("type", "int");

        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnet.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        }
        if (magnet.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        parAdptr.setValue("value", Integer.toString(orientation));
    }

    /**
     * Writes the element- and parameter-tags of a Quadrupole lattice element
     */
    @Override
    public void visit(Solenoid e) {
        writeElementTag(e);
        Magnet magnet = (Magnet) e.getAcceleratorNode();
        //parameter 
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "MagField");
        parAdptr.setValue("type", "double");
        parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
        parAdptr.setValue("name", "EffLength");
        parAdptr.setValue("type", "double");
        double effLen = magnet.getEffLength() * e.getLength() / magnet.getLength();
        parAdptr.setValue("value", Double.toString(effLen));
        //parameter         
        parAdptr = elmAdptr.createChild("Parameter");
    }

    /**
     * Writes the element- and parameter-tags of a WScanner lattice element
     */
    @Override
    public void visit(WScanner e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a BPMonitor lattice element
     */
    @Override
    public void visit(BPMonitor e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a BLMonitor lattice element
     */
    @Override
    public void visit(BLMonitor e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a SkewQuad lattice element
     */
    @Override
    public void visit(SkewQuad e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a Sextupole lattice element
     */
    @Override
    public void visit(Sextupole e) {
        writeElementTag(e);
    }

    /**
     * Writes the element- and parameter-tags of a Marker lattice element
     */
    @Override
    public void visit(Marker e) {
        writeElementTag(e);
    }

    /**
     * A wrapper to read the RfGap frequency.
     */
    private double getGapFrequencyWrapper(RfGap rfgap) {
        return rfgap.getGapDfltFrequency() * 1.e6;
    }

    /**
     * A wrapper to read the RfGap average phase.
     */
    private double getRfGapPhaseAvgWrapper(RfGap rfgap) {
        // for design values
        if (ModelProxy.PARAMSRC_DESIGN.equals(paramSrc)) {
            return rfgap.getGapDfltPhase() * Math.PI / 180.;
            // for live values
        } else if (ModelProxy.PARAMSRC_LIVE.equals(paramSrc)) {
            try {
                return rfgap.getGapPhaseAvg() * Math.PI / 180.;
            } catch (GetException e) {
                if (e.getMessage() != null) {
                    LOGGER.log(Level.INFO, e.getMessage());
                } else {
                    LOGGER.log(Level.INFO, "RfGap.getGapPhaseAvg(): channel access failed: {0}", rfgap.getId());
                }
                // 90 degrees
                return Math.PI * 0.5;
            }
        } else if (ModelProxy.PARAMSRC_RF_DESIGN.equals(paramSrc)) {
            return rfgap.getGapDfltPhase() * Math.PI / 180.;
        } else {
            return rfgap.getGapDfltPhase() * Math.PI / 180.;
        }
    }

    /**
     * A wrapper to read the RfGap E0TL.
     */
    private double getRfGapE0TLWrapper(RfGap rfgap) {
        // for design values
        if (ModelProxy.PARAMSRC_DESIGN.equals(paramSrc)) {
            return rfgap.getGapDfltE0TL() * 1.e6;
            // for live values
        } else if (ModelProxy.PARAMSRC_LIVE.equals(paramSrc)) {
            try {
                return rfgap.getGapE0TL() * 1.e6;
            } catch (GetException e) {
                if (e.getMessage() != null) {
                    LOGGER.log(Level.INFO, e.getMessage());
                } else {
                    LOGGER.log(Level.INFO, "RfGap.getGapE0TL(): channel access failed: {0}", rfgap.getId());
                }
                return rfgap.getGapDfltE0TL() * 1.e6;
            }
        } else if (ModelProxy.PARAMSRC_RF_DESIGN.equals(paramSrc)) {
            return rfgap.getGapDfltPhase() * Math.PI / 180.;
        } else {
            return rfgap.getGapDfltE0TL() * 1.e6;
        }
    }

    /**
     * A wrapper to read the magnet field strength.
     */
    private double getFieldWrapper(Magnet magnet) {
        // for design values
        if (ModelProxy.PARAMSRC_DESIGN.equals(paramSrc)) {
            return magnet.getDesignField();
            // for live values
        } else if (ModelProxy.PARAMSRC_LIVE.equals(paramSrc)
                || ModelProxy.PARAMSRC_RF_DESIGN.equals(paramSrc)) {
            try {
                if (magnet instanceof Electromagnet) {
                    return ((Electromagnet) magnet).getField();
                } else {
                    return magnet.getDesignField();
                }
            } catch (GetException e) {
                if (e.getMessage() != null) {
                    LOGGER.log(Level.INFO, e.getMessage());
                } else {
                    LOGGER.log(Level.INFO, "Electromagnet.getField(): channel access failed: " + magnet.getId());
                }
                return 0.d;
            }
        } else {
            return magnet.getDesignField();
        }
    }

    /**
     * A wrapper to read the magnet field strength.
     */
    private double getFieldWrapper(Electrostatic magnet) {
        // for design values
        if (ModelProxy.PARAMSRC_DESIGN.equals(paramSrc)) {
            return magnet.getDesignField();
            // for live values
        } else if (ModelProxy.PARAMSRC_LIVE.equals(paramSrc)
                || ModelProxy.PARAMSRC_RF_DESIGN.equals(paramSrc)) {
            try {
                if (magnet instanceof Electrostatic) {
                    return magnet.getField();
                } else {
                    return magnet.getDesignField();
                }
            } catch (GetException e) {
                if (e.getMessage() != null) {
                    LOGGER.log(Level.INFO, null, e);
                } else {
                    LOGGER.log(Level.INFO, "Electromagnet.getField(): channel access failed: " + magnet.getId(), e);
                }
                return 0.d;
            }
        } else {
            return magnet.getDesignField();
        }
    }
}
