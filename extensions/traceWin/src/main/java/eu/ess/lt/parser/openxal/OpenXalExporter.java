package eu.ess.lt.parser.openxal;

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
import se.lu.esss.ics.jels.smf.impl.ESSElementFactory;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.ics.jels.smf.impl.ESSRfGap;
import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import xal.model.IElement;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.attr.SequenceBucket;
import xal.smf.impl.ElementFactory;
import xal.smf.impl.Magnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetPowerSupply;
import xal.smf.impl.RfGap;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.data.DataAdaptor;

/**
 * Converter from BLED to openxal
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 * @author Blaz Kranjc
 */
public class OpenXalExporter {
	// Those are the constants used during export and depend on the initial beam
	// parameters.
	public static final double InitialFrequency = 352.21 * 1e6;
	public static final double beta_gamma_Er_by_e0_c = -0.08980392292066133;

	private static List<String> expSections = Arrays.asList("lebt", "rfq", "mebt", "dtl", "spoke", "mb", "sbx", "hb",
			"hebt");

	private List<LatticeCommand> latticeCommands; // a list of all lattice
													// comands
	private OnLeafComparator leafComparator = new OnLeafComparator();

	private Accelerator acc;
	private Corrector lastCorrector;

	// variables to help exporting lattice points
	private int latticeElements = 0, latticeCount = 0, periodicLatticeId = 0, latticePoint = 1;

	/**
	 * Main exporting entry point
	 */	
	public Accelerator exportToOpenxal(Subsystem parentSystem, List<Subsystem> systems) {		
		leafComparator.init(systems);
		
		// Getting lattice commands
		latticeCommands = new ArrayList<LatticeCommand>();
		for (Subsystem component : systems) {
			if (component instanceof LatticeCommand) {
				latticeCommands.add((LatticeCommand) component);
			}
		}
		Collections.sort(latticeCommands, leafComparator);
		
		acc = new Accelerator("ESS") {
			@Override
			public void write(DataAdaptor adaptor) {
				super.write(adaptor);
				// write out power supplies
				DataAdaptor powerSuppliesAdaptor = adaptor.createChild("powersupplies");
				for (MagnetPowerSupply mps : getMagnetMainSupplies()) {
					mps.write(powerSuppliesAdaptor.createChild("ps"));
				}
			}
		};
		AcceleratorSeq seq = export(parentSystem, systems, 0., leafComparator);		
		AcceleratorSeq previousSeq = null;
		for (AcceleratorSeq s : new ArrayList<AcceleratorSeq>(seq.getSequences())) {
			acc.addNode(s);
			if (previousSeq != null) {
				SequenceBucket sequenceBucket = new SequenceBucket();
				sequenceBucket.setPredecessors(new String[] { previousSeq.getId() });
				s.setSequence(sequenceBucket);
			}
			previousSeq = s;
		}
		for (AcceleratorNode n : new ArrayList<AcceleratorNode>(seq.getNodes())) {
			acc.addNode(n);
		}

		acc.setLength(seq.getLength());
		return acc;
	}

	/**
	 * Searches for last frequency command before the given subsystem
	 * 
	 * @param subsystem
	 *            frequency before this subsystem
	 * @return frequency
	 */
	private double getFrequency(Subsystem subsystem) {
		LatticeCommand lastBefore = null;
		for (LatticeCommand lc : latticeCommands) {
			if (leafComparator.compare(lc, subsystem) < 0) { // lc < subsystem
				if (!lc.getValue().contains("FREQ"))
					continue;
				lastBefore = lc;
			} else
				break;
		}

		if (lastBefore == null)
			return InitialFrequency; // initial frequency

		// parse the lattice command
		String command = lastBefore.getValue();
		if (command.contains(": "))
			command = command.substring(command.indexOf(": ") + 1).trim();
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
	 * @param parentSystem
	 *            system for which data will be exported
	 * @param systems
	 *            all BLED systems
	 * @param parentsCurrentPosition
	 *            position of parent system
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
				latticeCount++;
			} else if (subsystem instanceof DTLCell) {
				node = exportDTLCell((DTLCell) subsystem, currentPosition);
				latticeCount++;
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
					;
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
				} else
					node = exportMarker((Marker) subsystem, currentPosition);
			} else if (subsystem instanceof BeamlineElement) {
				if (((BeamlineElement) subsystem).getLength() != null)
					currentPosition += ((BeamlineElement) subsystem).getLength();
				if (!(subsystem instanceof Drift))
					latticeCount++;
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
				} else
					seq.addNode(node);

				double length = node.getLength();
				if (node instanceof Magnet) {
					if (node instanceof xal.smf.impl.Bend)
						length = ((xal.smf.impl.Bend) node).getDfltPathLength();
					else
						length = ((Magnet) node).getEffLength();
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

	/*************** Exporting each element *******************************/

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
		MagnetMainSupply ps = ElementFactory.createMainSupply(element.getName()+"-PS", acc);
		return ElementFactory.createQuadrupole(element.getName(), element.getLength(), element.getQuadrupoleGradient(),
				aper, ps, currentPosition + element.getLength()/2);
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
		MagnetMainSupply vps = ElementFactory.createMainSupply(element.getName()+"-VC-PS", acc);
		AcceleratorNode vcorr = ElementFactory.createCorrector(element.getName() + "-VC", MagnetType.VERTICAL,
				L, vAper, vps, currentPosition + L/2);
		seq.addNode(vcorr);

		MagnetMainSupply hps = ElementFactory.createMainSupply(element.getName()+"-HC-PS", acc);
		ApertureBucket hAper = generateApertureBucket(element);
		AcceleratorNode hcorr = ElementFactory.createCorrector(element.getName() + "-HC", MagnetType.HORIZONTAL,
				L, hAper, hps, currentPosition + L/2);
		seq.addNode(hcorr);
		return seq;
	}

	private AcceleratorNode exportBend(final Bend element, double currentPosition) {
		double alpha_deg = element.getBendAngle();
		double rho = element.getCurvatureRadius() * 1e-3;
		double entry_angle_deg = element.getEntranceAngle();
		double exit_angle_deg = element.getExitAngle();
		double k = beta_gamma_Er_by_e0_c;
		double G = element.getGap();
		int orientation = Orientation.HORIZONTAL.equals(element.getOrientation()) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL;
		double len = Math.abs(rho * alpha_deg * Math.PI / 180.);

		ApertureBucket aper = generateApertureBucket(element);
		double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;

		MagnetMainSupply ps = ElementFactory.createMainSupply(element.getName()+"-PS", acc);
		return ESSElementFactory.createESSBend(element.getName(), alpha_deg, k, rho, entry_angle_deg, exit_angle_deg, 
				entrK1, entrK2, exitK1, exitK2, 0, aper, ps, orientation, G * 1e-3, currentPosition + len/2);
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
		
		final RfGap gap = ElementFactory.createRfGap(element.getName()+":G", true, 1.0, aper, 1.0, 0);
		ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(element.getName(), 0, gap, Phis, amplitude,
				getFrequency(element) * 1e-6, currentPosition);
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		}
		else {
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setSTFCoefs(new double[] {betas, 0, kS, k2S});
			cavity.getRfField().setSTF_endCoefs(new double[] {betas, 0, kS, k2S});
		}
		return cavity;
	}

	private AcceleratorNode exportFieldMap(final FieldMap element, double currentPosition) {
		FieldProfile profile = FieldProfile.getInstance(OpenXalExporter.class.getResource(
				"/" + element.getFileName() + ".edz").toString());
		ApertureBucket aper =  generateApertureBucket(element);
		return ESSElementFactory.createESSFieldMap(element.getName(), element.getLength(),
				getFrequency(element) * 1e-6, element.getElectricIntensityFactor(), element.getRfPhase(), 
				element.getFileName(), profile, aper, currentPosition);
	}

	private AcceleratorNode exportNCell(final NCell element, double currentPosition) {
		double frequency = getFrequency(element) * 1e-6;

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
		double amp0, ampn;
		double pos0, posn;

		amp0 = (1 + kE0Ti) * (Ti / Ts);
		ampn = (1 + kE0To) * (To / Ts);
		if (m == 0) {
			Lc = Lc0 = Lcn = betag * lambda;
			pos0 = 0.5 * Lc0 + dzi;
			posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo;
		} else if (m == 1) {
			Lc = Lc0 = Lcn = 0.5 * betag * lambda;
			pos0 = 0.5 * Lc0 + dzi;
			posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo;
		} else { // m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;
			pos0 = 0.25 * betag * lambda + dzi;
			posn = Lc0 + (n - 2) * Lc + 0.5 * betag * lambda + dzo;
		}

		AcceleratorNode[] nodes = new AcceleratorNode[n];

		// setup
		nodes[0] = ESSElementFactory.createESSRfGap(element.getName()+":G0", true, amp0, new ApertureBucket(),
				Lc0, pos0);

		for (int i = 1; i < n - 1; i++) {
			nodes[i] = ESSElementFactory.createESSRfGap(element.getName()+"G"+i, false, 1, new ApertureBucket(),
					Lc, Lc0 + (i - 0.5) * Lc);
		}
		
		ESSRfGap lastGap = ESSElementFactory.createESSRfGap(element.getName()+":G"+(n-1), false, ampn, new ApertureBucket(),
				Lcn, posn);
		lastGap.getRfGap().setEndCell(1);
		
		nodes[n-1] = lastGap;


		ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(element.getName(), Lc0+(n-2)*Lc+Lcn, nodes, Phis, E0T*1e-6, 
				frequency, currentPosition);

		if (betas == 0.0) {
			cavity.getRfField().setTTF_startCoefs(new double[] {});
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		}
		else {
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ti, kTi, k2Ti});
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, To, kTo, k2To});
		}

		if (m==1)
			cavity.getRfField().setStructureMode(1);

		return cavity;
	}


	private AcceleratorNode exportDTLCell(final DTLCell element, double currentPosition) {
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

		MagnetMainSupply ps1 = ElementFactory.createMainSupply(element.getName()+":Q1-PS", acc);
		xal.smf.impl.Quadrupole quad1 = ElementFactory.createQuadrupole(element.getName()+":Q1", Lq1, B1, new ApertureBucket(),
				ps1, Lq1/2);
		MagnetMainSupply ps2 = ElementFactory.createMainSupply(element.getName()+":Q2-PS", acc);
		xal.smf.impl.Quadrupole quad2 = ElementFactory.createQuadrupole(element.getName()+":Q2", Lq2, B2, new ApertureBucket(),
				ps2, L - Lq2/2);

		RfGap gap = ElementFactory.createRfGap(element.getName()+":G", true, 1, new ApertureBucket(), length, L/2-g);


		ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(element.getName(), L, new AcceleratorNode[] {quad1, gap, quad2},
				Phis, E0TL*1e-6/length, getFrequency(element)*1e-6, currentPosition);
		
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
		}
		else {
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setSTFCoefs(new double[] {betas, 0, kS, k2S});
			cavity.getRfField().setSTF_endCoefs(new double[] {betas, 0, kS, k2S});
		}
		
		return cavity;
	}

	private ApertureBucket generateApertureBucket(BeamlineElement element) {
		ApertureBucket aper = new ApertureBucket();
		if (element.getApertureX() != null)
			aper.setAperX(element.getApertureX());
		if (element.getApertureY() != null)
			aper.setAperY(element.getApertureY());
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