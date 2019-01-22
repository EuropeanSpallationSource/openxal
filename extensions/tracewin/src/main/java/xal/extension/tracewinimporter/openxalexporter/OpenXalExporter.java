package xal.extension.tracewinimporter.openxalexporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.ess.bled.Subsystem;
import eu.ess.bled.devices.lattice.BeamlineElement;
import eu.ess.bled.devices.lattice.Bend;
import eu.ess.bled.devices.lattice.Bend.Orientation;
import eu.ess.bled.devices.lattice.Corrector;
import eu.ess.bled.devices.lattice.DTLCell;
import eu.ess.bled.devices.lattice.Drift;
import eu.ess.bled.devices.lattice.FieldMap;
import eu.ess.bled.devices.lattice.LatticeCommand;
import eu.ess.bled.devices.lattice.Marker;
import eu.ess.bled.devices.lattice.NCell;
import eu.ess.bled.devices.lattice.Quadrupole;
import eu.ess.bled.devices.lattice.RFCavity;
import xal.extension.jels.smf.ESSAccelerator;
import xal.extension.jels.smf.ESSElementFactory;
import xal.extension.jels.smf.impl.ESSDTLTank;
import xal.extension.jels.smf.impl.ESSRfCavity;
import xal.extension.jels.smf.impl.ESSRfGap;
import xal.model.IElement;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ElementFactory;
import xal.smf.attr.ApertureBucket;
import xal.smf.attr.SequenceBucket;
import xal.smf.impl.Magnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.PermQuadrupole;
import xal.smf.impl.qualify.MagnetType;

/**
 * Converter from BLED to OpenXAL
 *
 * @author Ivo List <ivo.list@cosylab.com>
 * @author Blaz Kranjc
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class OpenXalExporter {
    // Those are the constants used during export and depend on the initial beam
    // parameters.

    public static final double InitialFrequency = 352.21 * 1e6;
    public static final double beta_gamma_Er_by_e0_c = -0.08980392292066133;

    private static List<String> expSections = Arrays.asList("lebt", "rfq",
            "mebt", "dtl", "spk", "mbl", "hbl", "hebt", "a2t");

    private List<LatticeCommand> latticeCommands; // a list of all lattice
    // comands
    private OnLeafComparator leafComparator = new OnLeafComparator();

    private ESSAccelerator acc;
    private Corrector lastCorrector;

    // variables to help exporting lattice points
    private int latticeElements = 0, latticeCount = 0, periodicLatticeId = 0, latticePoint = 1;

    /**
     * Main exporting entry point
     */
    public ESSAccelerator exportToOpenxal(Subsystem parentSystem, List<Subsystem> systems) {
        leafComparator.init(systems);

        // Getting lattice commands
        latticeCommands = new ArrayList<LatticeCommand>();
        for (Subsystem component : systems) {
            if (component instanceof LatticeCommand) {
                latticeCommands.add((LatticeCommand) component);
            }
        }
        Collections.sort(latticeCommands, leafComparator);

        acc = new ESSAccelerator("ESS");
        AcceleratorSeq seq = export(parentSystem, systems, 0., leafComparator);
        AcceleratorSeq previousSeq = null;
        for (AcceleratorSeq s : new ArrayList<AcceleratorSeq>(seq.getSequences())) {
            acc.addNode(s);
            if (previousSeq != null) {
                SequenceBucket sequenceBucket = new SequenceBucket();
                sequenceBucket.setPredecessors(new String[]{previousSeq.getId()});
                s.setSequence(sequenceBucket);
            }
            previousSeq = s;
        }
        for (AcceleratorNode n : new ArrayList<AcceleratorNode>(seq.getNodes())) {
            acc.addNode(n);
        }

        acc.setLength(seq.getLength());

        addSDisplayPositions(acc);

        return acc;
    }

    /**
     * This method adds the SDisplay property that tells the absolute position
     * of a node inside the accelerator.
     *
     * @param accelerator
     */
    private void addSDisplayPositions(ESSAccelerator accelerator) {
        AcceleratorNode node = null;
        for (AcceleratorNode element : accelerator.getAllInclusiveNodes()) {
            double position = 0;
            node = element;
            while (node.hasParent()) {
                position += node.getPosition();
                node = node.getParent();
            }
            element.setSDisplay(position);
        }
    }

    /**
     * Searches for last frequency command before the given subsystem
     *
     * @param subsystem frequency before this subsystem
     * @return frequency
     */
    private double getFrequency(Subsystem subsystem) {
        LatticeCommand lastBefore = null;
        for (LatticeCommand lc : latticeCommands) {
            if (leafComparator.compare(lc, subsystem) < 0) { // lc < subsystem
                if (!lc.getValue().contains("FREQ")) {
                    continue;
                }
                lastBefore = lc;
            } else {
                break;
            }
        }

        if (lastBefore == null) {
            return InitialFrequency; // initial frequency
        }
        // parse the lattice command
        String command = lastBefore.getValue();
        if (command.contains(": ")) {
            command = command.substring(command.indexOf(": ") + 1).trim();
        }
        // F-704-42: FREQ 704.42
        String[] parts = command.split(" ", 2);
        if (parts.length >= 2 && "FREQ".equals(parts[0])) {
            return new Double(parts[1]) * 1e6;
        }

        throw new RuntimeException("Cannot get correct frequency");
    }

    /**
     * Main recursive exporting/conversion procedure to SMF.
     *
     * @param parentSystem system for which data will be exported
     * @param systems all BLED systems
     * @param parentsCurrentPosition position of parent system
     * @return AcceleratorSeq exported openxal accelerator sequence
     */
    private AcceleratorSeq export(Subsystem parentSystem, Collection<Subsystem> systems, double parentsCurrentPosition,
            Comparator<Subsystem> comparator) {
        String parentSystemName = parentSystem != null ? parentSystem.getName().toUpperCase() : null;
        AcceleratorSeq seq = new AcceleratorSeq(parentSystemName);

        double currentPosition = 0.;
        List<Subsystem> sortedSubsystemList = new ArrayList<Subsystem>();
        for (Subsystem component : systems) {
            if (component.getParentSubsystem() == parentSystem) {
                sortedSubsystemList.add(component);
            }
        }
        Collections.sort(sortedSubsystemList, comparator);

        AcceleratorSeq old_seq = null;
        Integer dtlTankNumber = 1;
        double dtl_currentLength = 0;
        AcceleratorSeq dtlTank = null;
        AcceleratorNode prev_node = null;
        // Export according to class.
        for (Subsystem subsystem : sortedSubsystemList) {
            AcceleratorNode node = null;
            Corrector nextCorrector = null;
            if (subsystem instanceof Quadrupole) {
                node = exportQuadrupole((Quadrupole) subsystem, currentPosition);
                latticeCount++;
            } else if (subsystem instanceof Bend) {
                node = exportBend((Bend) subsystem, currentPosition);
                latticeCount++;
            } else if (subsystem instanceof RFCavity) {
                node = exportRFCavity((RFCavity) subsystem, currentPosition);
                latticeCount++;
            } else if (subsystem instanceof NCell) {
                node = exportNCell((NCell) subsystem, currentPosition);
                latticeCount++;
            } else if (subsystem instanceof FieldMap) {
                node = exportFieldMap((FieldMap) subsystem, currentPosition);
                if (node != null) {
                    latticeCount++;
                } else {
                    currentPosition += ((FieldMap) subsystem).getLength();
                }
            } else if (subsystem instanceof DTLCell) {
                // First cell: create cavity 
                if (((DTLCell) subsystem).getRfPhase() != 0) {
                    dtlTank = exportDTLTank((DTLCell) subsystem, currentPosition, "DTLTank" + dtlTankNumber.toString());
                    dtlTank.setPosition(currentPosition);
                    dtlTankNumber++;
                    dtl_currentLength = 0;
                    node = dtlTank;
                    latticeCount++;
                } else {
                    node = null;

                    // Find the dtl tank
                    int i = 1;
                    boolean dtlTankFound = false;
                    while (!dtlTankFound) {
                        if (seq.getNodeAt(seq.getNodeCount() - i).isKindOf("DTLTank")) {
                            dtlTankFound = true;
                            dtlTank = (AcceleratorSeq) seq.getNodeAt(seq.getNodeCount() - i);
                        } else {
                            i++;
                        }
                    }
                    // Adding to the dtl tank any element found in between
                    while (i > 1) {
                        i--;
                        prev_node = seq.getNodeAt(seq.getNodeCount() - i);
                        seq.removeNode(prev_node);
                        prev_node.setPosition(prev_node.getPosition() - dtlTank.getPosition());
                        dtlTank.addNode(prev_node);
                        dtl_currentLength += prev_node.getLength();
                        dtlTank.setLength(dtl_currentLength);
                    }
                }

                AcceleratorNode[] nodes = exportDTLCell((DTLCell) subsystem, dtl_currentLength, ((ESSDTLTank) dtlTank).getDfltCavAmp());
                // Extend the previous quadrupole if exists
                if (dtlTank.getNodeCount() != 0 && dtlTank.getNodeAt(dtlTank.getNodeCount() - 1).getType().equals("PQ")) {
                    PermQuadrupole previous_PQ = (PermQuadrupole) dtlTank.getNodeAt(dtlTank.getNodeCount() - 1);
                    if (nodes[0] != null && nodes[0].getType().equals("PQ")
                            && ((PermQuadrupole) nodes[0]).getDesignField() == previous_PQ.getDesignField()) {
                        previous_PQ.setLength(previous_PQ.getLength() + nodes[0].getLength());
                        previous_PQ.getMagBucket().setEffLength(previous_PQ.getEffLength() + nodes[0].getLength());
                        previous_PQ.setPosition(previous_PQ.getPosition() + nodes[0].getLength() / 2);
                        nodes[0] = null;
                    }
                }
                for (AcceleratorNode node_i : nodes) {
                    if (node_i != null) {
                        dtlTank.addNode(node_i);
                    }
                }

                dtl_currentLength += ((DTLCell) subsystem).getLength();
                dtlTank.setLength(dtl_currentLength);
                currentPosition = dtlTank.getPosition() + dtl_currentLength;
            } else if (subsystem instanceof Corrector) {
                if (((Corrector) subsystem).getInsideNext()) {
                    nextCorrector = (Corrector) subsystem;
                } else {
                    node = exportCorrector((Corrector) subsystem, currentPosition);
                }
            } else if (subsystem instanceof LatticeCommand) {
                LatticeCommand latticeCommand = (LatticeCommand) subsystem;
                if (latticeCommand.getValue().toUpperCase().startsWith("LATTICE ")) {
                    if (latticeElements == 0) { // this if is here because of
                        // TraceWin behaviour
                        latticeCount = 0;
                        seq.addNode(exportMarker("LATTICE-POINT-" + (latticePoint++), currentPosition));
                    }

                    latticeElements = Integer.parseInt(latticeCommand.getValue().split(" ")[1]);

                    periodicLatticeId++;
                    node = exportMarker("LATTICE-START-" + periodicLatticeId, currentPosition);
                }
                if (latticeCommand.getValue().toUpperCase().startsWith("LATTICE_END")) {
                    latticeElements = 0;
                    node = exportMarker("LATTICE-END-" + periodicLatticeId, currentPosition);
                }

            } else if (subsystem instanceof eu.ess.bled.devices.lattice.BPM) {
                node = exportBPM(subsystem, currentPosition);
            } else if (subsystem instanceof Marker) {
                if (subsystem.getName().contains("BPM")) {
                    node = exportBPM(subsystem, currentPosition);
                } else {
                    node = exportMarker((Marker) subsystem, currentPosition);
                }
            } else if (subsystem instanceof BeamlineElement) {
                if (((BeamlineElement) subsystem).getLength() != null) {
                    currentPosition += ((BeamlineElement) subsystem).getLength();
                }
                if (!(subsystem instanceof Drift)) {
                    latticeCount++;
                }
            } else { // Subsystem
                node = export(subsystem, systems, currentPosition, comparator);
            }

            if (node != null) {
                if (node.getClass().equals(AcceleratorSeq.class) && !expSections.contains(node.getId().toLowerCase())) {
                    double offset = node.getPosition();
                    for (AcceleratorNode n : new ArrayList<AcceleratorSeq>(((AcceleratorSeq) node).getSequences())) {
                        n.setPosition(n.getPosition() + offset);
                        seq.addNode(n);
                    }
                    for (AcceleratorNode n : new ArrayList<AcceleratorNode>(((AcceleratorSeq) node).getNodes())) {
                        n.setPosition(n.getPosition() + offset);
                        seq.addNode(n);
                    }
                } else {
                    seq.addNode(node);
                }

                double length = node.getLength();
                if (node instanceof Magnet) {
                    if (node instanceof xal.smf.impl.Bend) {
                        length = ((xal.smf.impl.Bend) node).getDfltPathLength();
                    } else {
                        length = ((Magnet) node).getEffLength();
                    }
                }

                if (lastCorrector != null) {
                    exportCorrector(lastCorrector, seq, currentPosition, length);
                    lastCorrector = null;
                }

                currentPosition += length;
            }
            if (nextCorrector != null) {
                lastCorrector = nextCorrector;
                nextCorrector = null;
            }

            if (latticeElements != 0 && latticeCount >= latticeElements) {
                seq.addNode(exportMarker("LATTICE-POINT-" + (latticePoint++), currentPosition));
                latticeCount = 0;
            }

        }

        seq.setLength(currentPosition);
        seq.setPosition(parentsCurrentPosition);
        return seq;
    }

    /**
     * ************* Exporting each element ******************************
     */
    private AcceleratorNode exportMarker(Marker element, double currentPosition) {
        return exportMarker(element.getName(), currentPosition);
    }

    private AcceleratorNode exportMarker(String id, double currentPosition) {
        return ElementFactory.createMarker(id, currentPosition);
    }

    private AcceleratorNode exportBPM(final Subsystem element, double currentPosition) {
        return ElementFactory.createBPM(element.getName(), getFrequency(element), 1.0, currentPosition);
    }

    private AcceleratorNode exportQuadrupole(final Quadrupole element, double currentPosition) {
        ApertureBucket aper = generateApertureBucket(element);
        MagnetMainSupply ps = ElementFactory.createMainSupply(element.getName() + "-PS", acc);
        return ElementFactory.createQuadrupole(element.getName(), element.getLength(), element.getQuadrupoleGradient(),
                aper, ps, currentPosition + element.getLength() / 2);
    }

    private AcceleratorNode exportCorrector(final Corrector element, double currentPosition) {
        AcceleratorSeq seq = new AcceleratorSeq(element.getName() + "-S");
        exportCorrector(element, seq, 0., 0.);
        seq.setPosition(currentPosition);
        seq.setLength(0.);
        return seq;
    }

    private AcceleratorNode exportCorrector(final Corrector element, AcceleratorSeq seq, double currentPosition,
            double L) {

        ApertureBucket vAper = generateApertureBucket(element);
        MagnetMainSupply vps = ElementFactory.createMainSupply(element.getName() + "-VC-PS", acc);
        AcceleratorNode vcorr = ElementFactory.createCorrector(element.getName() + "-VC", MagnetType.VERTICAL,
                L, vAper, vps, currentPosition + L / 2);
        seq.addNode(vcorr);

        MagnetMainSupply hps = ElementFactory.createMainSupply(element.getName() + "-HC-PS", acc);
        ApertureBucket hAper = generateApertureBucket(element);
        AcceleratorNode hcorr = ElementFactory.createCorrector(element.getName() + "-HC", MagnetType.HORIZONTAL,
                L, hAper, hps, currentPosition + L / 2);
        seq.addNode(hcorr);
        return seq;
    }

    private AcceleratorNode exportBend(final Bend element, double currentPosition) {
        double alpha_deg = element.getBendAngle();
        double rho = element.getCurvatureRadius() * 1e-3;
        double entry_angle_deg = element.getEntranceAngle();
        double exit_angle_deg = element.getExitAngle();
        double k = beta_gamma_Er_by_e0_c;
        double G = element.getGap() * 1e-3;
        int orientation = Orientation.HORIZONTAL.equals(element.getOrientation()) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL;
        double len = Math.abs(rho * alpha_deg * Math.PI / 180.);

        ApertureBucket aper = generateApertureBucket(element);

        MagnetMainSupply ps = ElementFactory.createMainSupply(element.getName() + "-PS", acc);
        return ESSElementFactory.createESSBend(element.getName(), alpha_deg, k, rho, entry_angle_deg, exit_angle_deg,
                0, aper, ps, orientation, G, currentPosition + len / 2);
    }

    private AcceleratorNode exportRFCavity(final RFCavity element, double currentPosition) {
        double E0TL = element.getGapVoltage();
        double Phis = element.getPhase();
        double betas = element.getBeta();
        double amplitude = E0TL * 1e-6;
        double Ts = element.getTransitTimeFactor();
        double kTs = element.getkT();
        double k2Ts = element.getK2T();
        double kS = element.getkS();
        double k2S = element.getK2S();

        ApertureBucket aper = generateApertureBucket(element);

        final ESSRfGap gap = ESSElementFactory.createESSRfGap(element.getName() + ":G", true, 1.0, aper, 1.0, 0);
        ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(element.getName(), 0, gap, Phis, amplitude,
                getFrequency(element) * 1e-6, currentPosition);
        if (betas == 0.0) {
            cavity.getRfField().setTTFCoefs(new double[]{});
            cavity.getRfField().setTTF_endCoefs(new double[]{});
        } else {
            cavity.getRfField().setTTFCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_startCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_endCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setSTFCoefs(new double[]{betas, 0, kS, k2S});
            cavity.getRfField().setSTF_startCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setSTF_endCoefs(new double[]{betas, 0, kS, k2S});
        }
        return cavity;
    }

    private AcceleratorNode exportFieldMap(final FieldMap element, double currentPosition) {
        ApertureBucket aper = generateApertureBucket(element);

        if (element.getGeom() == 100) {
            return ESSElementFactory.createRfFieldMap("R" + element.getName(), element.getLength(),
                    getFrequency(element) * 1e-6, element.getElectricIntensityFactor(), element.getRfPhase(),
                    element.getFileName(), element.getBasePath(), aper, currentPosition, 0);
        } else if (element.getGeom() == 50) {
            MagnetMainSupply ps = ElementFactory.createMainSupply("M" + element.getName() + "-PS", acc);

            return ESSElementFactory.createMagFieldMap("M" + element.getName(), element.getLength(), element.getMagneticIntensityFactor(),
                    element.getBasePath(), element.getFileName(), aper, ps, currentPosition, 2, 0);
        }

        return null;
    }

    private AcceleratorNode exportNCell(final NCell element, double currentPosition) {
        double frequency = getFrequency(element);

        double Phis = element.getRfPhase();
        double E0T = element.getE0T();
        double betas = element.getBetas();

        double Ts = element.getTransitTime();
        double kTs = element.getkTsp();
        double k2Ts = element.getK2Tspp();

        double Ti = element.getTransitTimeIn();
        double kTi = element.getkTip();
        double k2Ti = element.getK2Tipp();

        double To = element.getTransitTimeOut();
        double kTo = element.getkTop();
        double k2To = element.getK2Topp();

        double betag = element.getBetag();
        double kE0Ti = element.getkE0Ti();
        double kE0To = element.getkE0To();
        double dzi = element.getDzi();
        double dzo = element.getDzo();

        int n = element.getCellNumber();
        int m = element.getMode();

        double lambda = IElement.LightSpeed / frequency;
        double Lc0, Lc, Lcn;
        double amp0 = 1 + kE0Ti;
        double ampn = 1 + kE0To;
        double pos0, posn;

        ApertureBucket apertureBucket = generateApertureBucket(element);

        if (betas != 0.0) {
            amp0 *= Ti / Ts;
            ampn *= To / Ts;
        }
        switch (m) {
            case 0:
                Lc = Lc0 = Lcn = betag * lambda;
                pos0 = 0.5 * Lc0 + dzi;
                posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo;
                break;
            case 1:
                Lc = Lc0 = Lcn = 0.5 * betag * lambda;
                pos0 = 0.5 * Lc0 + dzi;
                posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo;
                break;
            default:
                // m==2
                Lc0 = Lcn = 0.75 * betag * lambda;
                Lc = betag * lambda;
                pos0 = 0.25 * betag * lambda + dzi;
                posn = Lc0 + (n - 2) * Lc + 0.5 * betag * lambda + dzo;
                break;
        }

        AcceleratorNode[] nodes = new AcceleratorNode[n];

        // setup
        nodes[0] = ESSElementFactory.createESSRfGap(element.getName() + ":G0", true, amp0, apertureBucket,
                Lc0, pos0);

        for (int i = 1; i < n - 1; i++) {
            nodes[i] = ESSElementFactory.createESSRfGap(element.getName() + "G" + i, false, 1, apertureBucket,
                    Lc, Lc0 + (i - 0.5) * Lc);
        }

        ESSRfGap lastGap = ESSElementFactory.createESSRfGap(element.getName() + ":G" + (n - 1), false, ampn, apertureBucket,
                Lcn, posn);
        lastGap.getRfGap().setEndCell(1);

        nodes[n - 1] = lastGap;

        ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(element.getName(), Lc0 + (n - 2) * Lc + Lcn, nodes, Phis, E0T * 1e-6,
                frequency * 1e-6, currentPosition);

        if (betas == 0.0) {
            cavity.getRfField().setTTF_startCoefs(new double[]{});
            cavity.getRfField().setTTFCoefs(new double[]{});
            cavity.getRfField().setTTF_endCoefs(new double[]{});
        } else {
            cavity.getRfField().setTTF_startCoefs(new double[]{betas, Ti, kTi, k2Ti});
            cavity.getRfField().setTTFCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_endCoefs(new double[]{betas, To, kTo, k2To});
        }

        if (m == 1) {
            cavity.getRfField().setStructureMode(1);
        }

        return cavity;
    }

    private AcceleratorSeq exportDTLTank(final DTLCell element,
            double currentPosition, String name) {
        double L = element.getLength();
        double Lq1 = element.getLq1();
        double Lq2 = element.getLq2();
        double Phis = element.getRfPhase();
        double E0TL = element.getE0TL();

        double length = L - Lq1 - Lq2;

        ESSDTLTank dtlTank = ESSElementFactory.createESSDTLTank(name, L,
                new AcceleratorNode[0], Phis, E0TL / length * 1e-6,
                getFrequency(element) * 1e-6, currentPosition);

        return dtlTank;
    }

    private AcceleratorNode[] exportDTLCell(final DTLCell element,
            double currentPosition, double amplitude) {
        double L = element.getLength();
        double Lq1 = element.getLq1();
        double Lq2 = element.getLq2();
        double g = element.getCellCenter();
        double Phis = element.getRfPhase();

        double betas = element.getBetas();
        double Ts = element.getTransitTime();
        double kTs = element.getkTsp();
        double k2Ts = element.getK2Tsp();
        double kS = 0;
        double k2S = 0;
        double E0TL = element.getE0TL();

        double B1 = element.getB1p();
        double B2 = element.getB2p();

        double length = L - Lq1 - Lq2;

        ApertureBucket apertureBucket = generateApertureBucket(element);

        double ampFactor = E0TL / length * 1e-6 / amplitude;

        boolean isFirst = Phis != 0;
        ESSRfGap gap = ESSElementFactory.createESSRfGap(element.getName() + ":G", isFirst, ampFactor, apertureBucket, length, currentPosition + L / 2 - g);

        if (betas == 0.0) {
            gap.getRfGap().setTCoefficients(new double[]{});
        } else {
            gap.getRfGap().setTCoefficients(new double[]{betas, Ts, kTs, k2Ts});
            gap.getRfGap().setSCoefficients(new double[]{betas, 0, kS, k2S});
        }

        PermQuadrupole quad1 = ElementFactory.createPermQuadrupole(element.getName() + ":Q1", Lq1, B1, apertureBucket,
                currentPosition + Lq1 / 2);
        PermQuadrupole quad2 = ElementFactory.createPermQuadrupole(element.getName() + ":Q2", Lq2, B2, apertureBucket,
                currentPosition + L - Lq2 / 2);

        AcceleratorNode[] cell = {null, null, null};
        if (B1 != 0.0) {
            cell[0] = quad1;
        }
        if (betas != 0.0) {
            cell[1] = gap;
        }
        if (B2 != 0) {
            cell[2] = quad2;
        }

        return cell;
    }

    private ApertureBucket generateApertureBucket(BeamlineElement element) {
        ApertureBucket aper = new ApertureBucket();
        if (element.getApertureX() != null) {
            aper.setAperX(element.getApertureX());
        }
        if (element.getApertureY() != null) {
            aper.setAperY(element.getApertureY());
        }
        int apertureCode = element.getApertureType() == null ? 2 : element.getApertureType().getIntegerValue();
        aper.setShape(toOpenXALApertureCode(apertureCode));
        return aper;
    }

    public static final int VAL_APERTURE_UNKNOWN = 0;
    public static final int VAL_APERTURE_ELLIPSE = 1;
    public static final int VAL_APERTURE_RECTANGLE = 2;
    public static final int VAL_APERTURE_DIAMOND = 3;
    public static final int VAL_APERTURE_IRREGULAR = 11;

    private static int toOpenXALApertureCode(int traceWinApertureCode) {
        switch (traceWinApertureCode) {
            case 0:
                return VAL_APERTURE_RECTANGLE;
            case 1:
                return VAL_APERTURE_ELLIPSE;
            case 2:
                return VAL_APERTURE_UNKNOWN;
            case 3:
            case 4:
            case 5:
            case 6:
                return VAL_APERTURE_IRREGULAR;
            default:
                return VAL_APERTURE_UNKNOWN;
        }
    }
}
