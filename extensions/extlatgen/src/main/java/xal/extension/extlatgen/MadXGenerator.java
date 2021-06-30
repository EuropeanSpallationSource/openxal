package xal.extension.extlatgen;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import xal.smf.*;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;
import xal.smf.impl.RfCavity;
// for lattice generation
import xal.sim.slg.*;
// Probe for Mad header
import xal.model.probe.*;
import xal.tools.beam.Twiss;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.CovarianceMatrix;

public class MadXGenerator {

    private static final Logger LOGGER = Logger.getLogger(MadXGenerator.class.getName());

    /**
     * speed of light constant in 10^9 m/s
     */
    static final double LIGHT_SPEED = 0.2997925;

    /**
     * default number format
     */
    static final NumberFormat NUMBER_FORMAT;

    /**
     * Probe for initial condition
     */
    protected Probe<?> myProbe;

    protected List<AcceleratorSeq> sequenceChain = null;

    /**
     * for design values
     */
    public static final int PARAMSRC_DESIGN = 2;

    /**
     * for live data from the machine
     */
    public static final int PARAMSRC_LIVE = 3;

    protected String myLatticeName = null;

    /**
     * sign of particle charge
     */
    protected double Q = -1.;

    /**
     * list of MAD elements
     */
    private List<MadXElement> MADX_ELEMENTS;

    /**
     * beam initial condition
     */
    protected double beamci[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

    /**
     * indicates whether to use design bend angles regardless of the specified
     * data source
     */
    private boolean useDesignBendAngles;

    // static initializer
    static {
        NUMBER_FORMAT = NumberFormat.getNumberInstance();
        ((DecimalFormat) NUMBER_FORMAT).setMaximumFractionDigits(8);
    }

    /**
     * Constructor
     *
     * @param sequenceChain sequence list
     * @param envProbe envelope probe
     */
    public MadXGenerator(List<AcceleratorSeq> sequenceChain, TransferMapProbe envProbe) {
        this(null, sequenceChain, envProbe);
    }

    /**
     * Constructor
     */
    public MadXGenerator(List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
        this(null, sequenceChain, envProbe);
    }

    /**
     * Constructor
     *
     * @param latticeName lattice name (if there is one)
     * @param sequenceChain sequence list
     * @param envProbe envelope probe
     */
    public MadXGenerator(String latticeName, List<AcceleratorSeq> sequenceChain, TransferMapProbe envProbe) {
        this(latticeName, sequenceChain, (Probe) envProbe);
    }

    /**
     * Constructor
     */
    public MadXGenerator(String latticeName, List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
        this(latticeName, sequenceChain, (Probe) envProbe);
    }

    /**
     * Constructor
     */
    public MadXGenerator(final String latticeName, final List<AcceleratorSeq> sequenceChain, final Probe<?> envProbe) {
        myLatticeName = latticeName;
        myProbe = envProbe;
        this.sequenceChain = sequenceChain;
        useDesignBendAngles = true;
    }

    /**
     * Set whether to use the design bend angles independent of the specified
     * data source
     */
    public void setUseDesignBendAngles(final boolean useDesignBendAngles) {
        this.useDesignBendAngles = useDesignBendAngles;
    }

    /**
     * set the beam initial condition
     */
    public void setBeamCI(double[] newBeamCI) {
        beamci = newBeamCI;
    }

    /**
     * generate the MAD input file
     *
     * @param deviceDataSource data source for the device's fields
     */
    public void createMadInput(final AbstractDeviceDataSource deviceDataSource) throws IOException {
        createMadInput(deviceDataSource, null);
    }

    /**
     * add a new MAD element with the specified element name and definition
     */
    private void addElement(final String elementName, final String definition) {
        MADX_ELEMENTS.add(new MadXElement(elementName, definition));
    }

    /**
     * Get the magnet's field
     */
    private double getField(final AcceleratorNode node, final AbstractDeviceDataSource deviceDataSource) {
        if (node instanceof Magnet) {
            final Magnet magnet = (Magnet) node;
            return deviceDataSource.getField(magnet);
        } else {
            return 0.0;
        }
    }

    /**
     * generate the MAD input file
     *
     * @param deviceDataSource data source for the device's fields
     */
    public void createMadInput(final AbstractDeviceDataSource deviceDataSource, final File outputFile) throws IOException {
        // select the data source for bends depending on whether the flag has been set to use design bend angles
        final AbstractDeviceDataSource bendDataSource = useDesignBendAngles ? AbstractDeviceDataSource.getDesignDataSourceInstance() : deviceDataSource;

        if (myLatticeName == null) {
            myLatticeName = sequenceChain.get(0).getId() + "-" + sequenceChain.get(sequenceChain.size() - 1).getId();
        }

        File madFile = outputFile != null ? outputFile : new File(myLatticeName + ".mad");
        LOGGER.log(Level.INFO, "Exporting MAD optics to file: {0}", madFile.getAbsolutePath());
        final FileWriter madxWritter = new FileWriter(madFile);
        final Date today = new Date();

        double momentum = RelativisticParameterConverter.computeMomentumFromEnergies(myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy()) / 1.e9;
        LOGGER.log(Level.INFO, "momentum = {0}", momentum);

        Q = myProbe.getSpeciesCharge();

        final String sourceLabel = deviceDataSource.getLabel();
        madxWritter.write("TITLE, \"" + sourceLabel + ": " + formatName(myLatticeName) + "  Date created: " + today.toString() + "\";\n\n");

        // no need to put drift spaces in the file
        MADX_ELEMENTS = new ArrayList<>();

        ArrayList<Double> lat_lengths = new ArrayList<>();
        HashMap<String, Double> elem_pos_map = new HashMap<>();
        ArrayList<String> allNames = new ArrayList<>();

        for (int i = 0; i < sequenceChain.size(); i++) {
            Lattice myLattice = createLattice(sequenceChain.get(i));
            lat_lengths.add(myLattice.getLength());
            LatticeIterator ilat = myLattice.latticeIterator();
            int counter = 1;
            int devTypeInd = 1;
            String devStr = "";
            AcceleratorNode currentThickNode = null;	// there can at most be one thick node at any location
            double currentThickNodePath = 0.0;			// total current path taken through the thick node (only bends modify and use this variable)

            while (ilat.hasNext()) {
                final Element element = ilat.next();
                final String elementName = element.getName();
                String formattedName = formatName(elementName);
                if (!allNames.contains(formattedName)) {
                    allNames.add(formattedName);
                } else {
                    formattedName = formattedName.concat("_A");
                    allNames.add(formattedName);
                }
                final String elementType = element.getType();
                final double elementLength = element.getLength();
                final AcceleratorNode node = element.getAcceleratorNode();

                if (elementType.equals("rfgap")) {
                    elem_pos_map.put(formatName(node.getParent().getId()), element.getPosition());
                } else {
                    elem_pos_map.put(formattedName, element.getPosition());
                }

                if (element != null && element.isThick()) {
                    if (node != currentThickNode) {
                        currentThickNode = node;
                        currentThickNodePath = 0.0;
                    }
                }

                // for marker
                if (elementType.equals("pmarker") || elementType.equals("foil")) {
                    if (!element.getName().contains("CENTER")) {
                        addElement(formattedName, "MARKER");
                    }
                    // for diagnostic devices (monitors)
                } else if (elementType.equals("beampositionmonitor") || elementType.equals("beamlossmonitor") || elementType.equals("beamcurrentmonitor") || elementType.equals("wirescanner")) {
                    addElement(formattedName, "MONITOR");
                    // for quads
                } else if (elementType.equals("quadrupole") || elementType.equals("skewquadrupole")) {
                    final double rollAngle = node.getAlign().getRoll() * Math.PI / 180.0;	// get the roll angle in radians
                    final double field = getField(node, deviceDataSource);

                    String definition = "QUADRUPOLE, L=" + NUMBER_FORMAT.format(elementLength) + ", K1=" + NUMBER_FORMAT.format(Q * field * LIGHT_SPEED / momentum);
                    if (rollAngle != 0.0) {
                        definition += ", TILT=" + rollAngle;
                    }
                    addElement(formattedName, definition);
                    // for bending dipole
                } else if (elementType.equals("dipole")) {
                    final xal.smf.impl.Bend bendNode = (xal.smf.impl.Bend) node;
                    final double bendMagneticLength = bendNode.getEffLength();

                    final double bendAngle = elementLength * bendDataSource.getBendAnglePerLength(bendNode, Q, momentum);

                    // if the element is the first for the bend magnet then we apply the entrance angle for this element
                    // an element is determined to be the first element of a bend if the current path through the bend is at the beginning (i.e. zero).
                    final double entranceAngle = currentThickNodePath == 0.0 ? bendDataSource.getBendEntranceAngle(bendNode, Q, momentum) : 0.0;
                    currentThickNodePath += elementLength;		// advance the path through the bend magnet
                    // if the element is the last for the bend magnet then we apply the exit angle for this element
                    // an element is determined to be the last element of a bend if the path after having passed through the element equals the magnetic length of the whole bend
                    final double lengthThreshold = 0.99999;	// ideally this should be 1.0, but we must allow for numerical precision errors
                    final double exitAngle = currentThickNodePath > lengthThreshold * bendMagneticLength ? bendDataSource.getBendExitAngle(bendNode, Q, momentum) : 0.0;

                    final double k1 = bendNode.getQuadComponent();

                    addElement(formattedName, "SBEND, L=" + NUMBER_FORMAT.format(elementLength) + ", ANGLE=" + NUMBER_FORMAT.format(bendAngle) + ", K1=" + NUMBER_FORMAT.format(k1) + ", " + "E1=" + NUMBER_FORMAT.format(entranceAngle) + ", " + "E2=" + NUMBER_FORMAT.format(exitAngle));
                    // for solenoid
                } else if (elementType.equals("solenoid")) {
                    final double field = getField(node, deviceDataSource);
                    addElement(formattedName, "SOLENOID, L=" + NUMBER_FORMAT.format(elementLength) + ", KS=" + NUMBER_FORMAT.format(field * LIGHT_SPEED / momentum));
                    // for horizontal dipole correctors
                } else if (elementType.equals("hsteerer")) {
                    final xal.smf.impl.HDipoleCorr corrector = (xal.smf.impl.HDipoleCorr) node;
                    final double field = getField(node, deviceDataSource);
                    final double kick = -field * LIGHT_SPEED * corrector.getEffLength() / momentum;
                    addElement(formattedName, "HKICKER, KICK=" + NUMBER_FORMAT.format(kick));
                    // for vertical dipole correctors
                } else if (elementType.equals("vsteerer")) {
                    final xal.smf.impl.VDipoleCorr corrector = (xal.smf.impl.VDipoleCorr) node;
                    final double field = getField(node, deviceDataSource);
                    final double kick = -field * LIGHT_SPEED * corrector.getEffLength() / momentum;
                    addElement(formattedName, "VKICKER, KICK=" + NUMBER_FORMAT.format(kick));
                    // for sextupoles
                } else if (elementType.equals("sextupole")) {
                    final double field = getField(node, deviceDataSource);
                    final double k2 = Q * field * LIGHT_SPEED / momentum;
                    addElement(formattedName, "SEXTUPOLE, L=" + NUMBER_FORMAT.format(elementLength) + ", K2=" + NUMBER_FORMAT.format(k2));
                    // RF Cavities are not handled properly, so comment out the RF Cavity code
                    // for rf gaps
                } else if (elementType.equals("rfgap")) {
                    double field = 0.;
                    field = ((RfCavity) node.getParent()).getDfltCavAmp();
                    final double phase = ((RfGap) node).getGapDfltPhase();
                    addElement(formatName(node.getParent().getId()), "RFCAVITY, L=" + NUMBER_FORMAT.format(node.getParent().getLength()) + ", VOLT=" + NUMBER_FORMAT.format(field) + ", LAG=" + NUMBER_FORMAT.format(phase) + ", HARMON =1, FREQ=" + ((RfCavity) node.getParent()).getCavFreq());
                } else {
                    if (node != null) {
                        LOGGER.log(Level.INFO, "Ignored element type: {0}, node: {1}, length: {2}", new Object[]{elementType, node.getId(), node.getLength()});
                    } else {
                        LOGGER.log(Level.INFO, "Ignored element type: {0}", elementType);
                    }
                    continue;
                }
                counter++;
            }
        }

        // write the MAD element definitions
        for (final MadXElement element : MADX_ELEMENTS) {
            madxWritter.write(element.NAME + ": " + element.DEFINITION + ";\n");
        }

        // construct the MAD lines
        final int MAX_LINE_LENGTH = 1250;
        int lineIndex = MAX_LINE_LENGTH;
        final List<List<MadXElement>> lines = new ArrayList<>();
        List<MadXElement> line = null;	// current line
        for (final MadXElement element : MADX_ELEMENTS) {
            if (lineIndex >= MAX_LINE_LENGTH) {
                line = new ArrayList<>(MAX_LINE_LENGTH);
                lines.add(line);
                lineIndex = 1;
            }
            line.add(element);
            ++lineIndex;
        }

        // write the MAD lines
        final int lineCount = lines.size();
        for (lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            final List<MadXElement> theLine = lines.get(lineIndex);
            madxWritter.write(formatName(myLatticeName) + ": SEQUENCE, REFER=CENTER, L=" + lat_lengths.get(lineIndex) + ";\n");
            final int numELements = theLine.size();
            for (int index = 0; index < numELements; index++) {
                final MadXElement element = theLine.get(index);
                madxWritter.write("    " + element.NAME + ", AT=" + elem_pos_map.get(element.NAME) + ";\n");
            }
            madxWritter.write("ENDSEQUENCE;\n");
        }

        final StringBuilder footerBuffer = new StringBuilder();
        footerBuffer.append("BEAM, PARTICLE=").append(myProbe.getSpeciesName()).append(", MASS=").append(NUMBER_FORMAT.format(myProbe.getSpeciesRestEnergy() / 1.e9));
        footerBuffer.append(", CHARGE=").append(NUMBER_FORMAT.format(myProbe.getSpeciesCharge()));
        footerBuffer.append(", ENERGY=").append(NUMBER_FORMAT.format((RelativisticParameterConverter.computeGammaFromEnergies(myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy()) * myProbe.getSpeciesRestEnergy()) / 1.e9)).append(";\n");
        footerBuffer.append("USE, sequence=").append(formatName(myLatticeName)).append(";\n");
        if (myProbe instanceof EnvelopeProbe) {
            CovarianceMatrix covarianceMatrix = ((EnvelopeProbe) myProbe).createProbeState().getCovarianceMatrix();

            Twiss[] inputTwiss = covarianceMatrix.computeTwiss();

            footerBuffer.append("   SELECT, flag=twiss, COLUMN = NAME,KEYWORD,S,L,K1,x,y,BETX,ALFX,DX,BETY,ALFY,DY,ENERGY;\n");
            footerBuffer.append("   TWISS");
            footerBuffer.append(",BETX=").append(inputTwiss[0].getBeta());
            footerBuffer.append(",ALFX=").append(inputTwiss[0].getAlpha());
            footerBuffer.append(",BETY=").append(inputTwiss[1].getBeta());
            footerBuffer.append(",ALFY=").append(inputTwiss[1].getAlpha());
            footerBuffer.append(",DX=").append(0.0);
            footerBuffer.append(",DPX=").append(0.0);
            footerBuffer.append(", file='twiss.out';\n");
        } else {
            footerBuffer.append("   SELECT, Flag=twiss, Class=MONITOR, PATTERN=\"BPM.*\", RANGE=#S/#E, COLUMN=name,s,x,y,betx,bety,alfx,alfy,mux,muy,Dx,Dy;\n");
            footerBuffer.append("   twiss, file=twiss.out;\n");
        }
        footerBuffer.append("   setplot, post=1, font=-1;\n");
        footerBuffer.append("   plot, haxis=s, vaxis1=betx,bety, range=#s/#e, style=100, colour=100, notitle=true;\n");
        footerBuffer.append("   plot, haxis=s, vaxis1=x,y, range=#s/#e, style=100, colour=100, notitle=true;\n");
        footerBuffer.append("STOP;\n");

        madxWritter.write(footerBuffer.toString());

        madxWritter.close();

    }

    /**
     * strip the leading sequence and device category identifier (i.e.
     * Ring_Mag:), replace any "-" or ":" with "_" in the device name or
     * beamline name
     */
    public String formatName(final String name) {
        final String formattedName = name.replaceFirst(".*_.*:", "").replace('-', '_').replace(':', '_').replace(' ', '_');
        return formattedName;
    }

    /**
     * create an XAL intermediate lattice
     *
     * @param accSeq accelerator sequence for the lattice
     * @return XAL intermediate lattice
     */
    public Lattice createLattice(AcceleratorSeq accSeq) {
        // create lattice using the (combo) sequence
        LatticeFactory factory = new LatticeFactory();
        factory.setDebug(false);
        factory.setVerbose(false);
        factory.setHalfMag(true);
        Lattice lattice = new Lattice(myLatticeName);
        try {
            lattice = factory.getLattice(accSeq);
            lattice.clearMarkers();
            lattice.joinDrifts();
        } catch (LatticeError lerr) {
            LOGGER.log(Level.INFO, lerr.getMessage());
        }

        return lattice;

    }
}

/**
 * MAD element name and definition
 */
class MadXElement {

    /**
     * name of the MAD element
     */
    public final String NAME;

    /**
     * definition of the MAD element
     */
    public final String DEFINITION;

    /**
     * Constructor
     */
    public MadXElement(final String name, final String definition) {
        NAME = name;
        DEFINITION = definition;
    }

}
