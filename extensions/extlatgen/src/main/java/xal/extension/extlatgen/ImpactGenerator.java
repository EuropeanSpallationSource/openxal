package xal.extension.extlatgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.sim.slg.Element;
import xal.sim.slg.Lattice;
import xal.sim.slg.LatticeError;
import xal.sim.slg.LatticeFactory;
import xal.sim.slg.LatticeIterator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.*;
import xal.tools.beam.RelativisticParameterConverter;

public class ImpactGenerator {

    private static final Logger LOGGER = Logger.getLogger(ImpactGenerator.class.getName());

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
    protected double q = -1.;

    /**
     * beam initial condition
     */
    protected double[] beamci = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

    // device types
    /**
     * drift
     */
    static final int DRIFT = 0;
    /**
     * quadrupole
     */
    static final int QUAD = 1;
    /**
     * constant focusing
     */
    static final int CF = 2;
    /**
     * solenoid
     */
    static final int SOLENOID = 3;
    /**
     * dipole
     */
    static final int DIPOLE = 4;
    /**
     * DTL
     */
    static final int DTL = 101;
    /**
     * CCDTL
     */
    static final int CCDTL = 102;
    /**
     * CCL
     */
    static final int CCL = 103;
    /**
     * RF cavity
     */
    static final int SC = 104;
    /**
     * Solenoid with RF cavity
     */
    static final int SOLRF = 105;
    /**
     * user defined RF cavity
     */
    static final int EMFLD = 110;
    /**
     * default aperture size = 0.014 m
     */
    static final double APER = 0.014;

    // static initializer
    static {
        NUMBER_FORMAT = NumberFormat.getNumberInstance();
        ((DecimalFormat) NUMBER_FORMAT).setMaximumFractionDigits(8);
    }

    /**
     * Constructor
     */
    public ImpactGenerator(List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
        this(null, sequenceChain, envProbe);
    }

    /**
     * Constructor
     */
    public ImpactGenerator(String latticeName, List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
        this(latticeName, sequenceChain, (Probe) envProbe);
    }

    /**
     * Constructor
     */
    public ImpactGenerator(final String latticeName, final List<AcceleratorSeq> sequenceChain, final Probe<?> envProbe) {
        myLatticeName = latticeName;
        myProbe = envProbe;
        this.sequenceChain = sequenceChain;
    }

    /**
     * Set whether to use the design bend angles independent of the specified
     * data source
     */
    public void setUseDesignBendAngles(final boolean useDesignBendAngles) {
        // Do nothing
    }

    /**
     * set the beam initial condition
     */
    public void setBeamCI(double[] newBeamCI) {
        beamci = newBeamCI;
    }

    /**
     * generate the IMPACT input file
     *
     * @param deviceDataSource data source for the device's fields
     */
    public void createImpactInput(final AbstractDeviceDataSource deviceDataSource) throws IOException {
        createImpactInput(deviceDataSource, null);
    }

    /**
     * generate the IMPACT input file
     *
     * @param deviceDataSource data source for the device's fields
     */
    public void createImpactInput(final AbstractDeviceDataSource deviceDataSource, final File outputFile) throws IOException {
        if (myLatticeName == null) {
            myLatticeName = sequenceChain.get(0).getId() + "-" + sequenceChain.get(sequenceChain.size() - 1).getId();
        }

        File impactFile = outputFile != null ? outputFile : new File("test.in");
        LOGGER.log(Level.INFO, "Exporting IMPACT optics to file: {0}", impactFile.getAbsolutePath());
        try (FileWriter impactWriter = new FileWriter(impactFile)) {
            double momentum = RelativisticParameterConverter.computeMomentumFromEnergies(myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy()) / 1.e9;
            LOGGER.log(Level.INFO, "momentum = {0}", momentum);

            q = myProbe.getSpeciesCharge();

            // single CPU, single core
            impactWriter.write("1 1\n");
            // total of 10000 particles
            impactWriter.write("6 10000 2 0 2\n");

            impactWriter.write("64 64 64 1 0.14 0.14 0.1025446\n");
            // 6D Waterbag, 2 charge states
            impactWriter.write("3 0 0 2\n");
            // 5000 for each charge state
            impactWriter.write("5000 5000\n");
            // beam current for each charge state
            impactWriter.write("0.0 0.0\n");
            // q_i/m_i for each charge state
            impactWriter.write("1.48852718947e-10 1.533634074e-10\n");
            // sigmax, lambdax, mux, mismatchx, mismatchpx, offsetX, offsetPx
            impactWriter.write("\n");
            impactWriter.write("\n");
            impactWriter.write("\n");
            // 
            impactWriter.write("\n");

            for (int i = 0; i < sequenceChain.size(); i++) {
                Lattice myLattice = createLattice(sequenceChain.get(i));
                LatticeIterator ilat = myLattice.latticeIterator();

                while (ilat.hasNext()) {
                    final Element element = ilat.next();
                    final String elementType = element.getType();
                    final double elementLength = element.getLength();
                    final AcceleratorNode node = element.getAcceleratorNode();

                    // for regular drift space, diagnostic devices
                    if (elementType.equals("drift")) {
                        impactWriter.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20\t" + DRIFT + "\t " + APER + "\t/\n");
                        // for quads
                    } else if (elementType.equals("quadrupole") || elementType.equals("skewquadrupole")) {
                        final double field = getField(node, deviceDataSource);
                        impactWriter.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20\t" + QUAD
                                + NUMBER_FORMAT.format(field / elementLength) + "\t" + node.getAper().getAperX()
                                + "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY()
                                + "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
                        // for solenoid
                    } else if (elementType.equals("solenoid")) {
                        final double field = getField(node, deviceDataSource);
                        impactWriter.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20" + SOLENOID
                                + NUMBER_FORMAT.format(field) + "\t0\t" + node.getAper().getAperX()
                                + "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY()
                                + "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
                        // for bending dipole
                    } else if (elementType.equals("dipole")) {
                        impactWriter.write(NUMBER_FORMAT.format(elementLength) + "\t10\t20" + DIPOLE
                                + "\t" + ((Bend) node).getDfltBendAngle() + "\t0.0\t150\t" + node.getAper().getAperX()
                                + "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY()
                                + "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
                        // for RF cavity
                    } else if (elementType.equals("rfgap")) {
                        double len = node.getParent().getLength();
                        double freq = ((RfCavity) node.getParent()).getCavFreq() * 1.e6;
                        double phase = ((RfCavity) node.getParent()).getDfltCavPhase();
                        impactWriter.write(NUMBER_FORMAT.format(len) + "\t10\t20" + SC
                                + "\t" + "1.0\t" + freq + "\t" + phase + "\t" + "1.0\t" + node.getAper().getAperX()
                                + "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY()
                                + "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
                    }

                }
            }

            // output format
            final StringBuilder footerBuffer = new StringBuilder();

            impactWriter.write(footerBuffer.toString());
        }
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
            LOGGER.log(Level.INFO, null, lerr);
        }

        return lattice;

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
}
