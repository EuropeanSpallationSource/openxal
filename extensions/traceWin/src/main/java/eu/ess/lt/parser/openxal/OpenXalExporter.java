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
import eu.ess.lt.parser.Exporter;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.ics.jels.smf.impl.ESSRfGap;
import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import xal.model.IElement;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.attr.SequenceBucket;
import xal.smf.impl.BPM;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Magnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetPowerSupply;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.data.DataAdaptor;

/**
 * Converter from BLED to openxal
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 * @author Blaz Kranjc
 */
public class OpenXalExporter implements Exporter {
	// Those are the constants used during export and depend on the initial beam
	// parameters.
	public static final double InitialFrequency = 352.21 * 1e6;
	public static final double beta_gamma_Er_by_e0_c = -0.08980392292066133;

	private static List<String> expSections = Arrays.asList("lebt", "rfq", "mebt", "dtl", "spoke", "mb", "sbx", "hb",
			"hebt");

	private List<LatticeCommand> latticeCommands; // a list of all lattice
													// comands
	private OnLeafComparator leafComparator = new OnLeafComparator();

	private List<MagnetPowerSupply> magnetPowerSupplies;
	private Corrector lastCorrector;

	// variables to help exporting lattice points
	private int latticeElements = 0, latticeCount = 0, periodicLatticeId = 0, latticePoint = 1;

	/**
	 * Main exporting entry point
	 */
	@Override
	public Accelerator exportToOpenxal(Subsystem parentSystem, Collection<Subsystem> systems) {

		// Getting lattice commands
		latticeCommands = new ArrayList<LatticeCommand>();
		for (Subsystem component : systems) {
			if (component instanceof LatticeCommand) {
				latticeCommands.add((LatticeCommand) component);
			}
		}
		Collections.sort(latticeCommands, leafComparator);

		magnetPowerSupplies = new ArrayList<MagnetPowerSupply>();
		AcceleratorSeq seq = export(parentSystem, systems, 0., leafComparator);
		Accelerator acc = new Accelerator("ESS") {
			@Override
			public void write(DataAdaptor adaptor) {
				super.write(adaptor);
				// write out power supplies
				DataAdaptor powerSuppliesAdaptor = adaptor.createChild("powersupplies");
				for (MagnetPowerSupply mps : magnetPowerSupplies) {
					mps.write(powerSuppliesAdaptor.createChild("ps"));
				}
			}
		};
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
		xal.smf.impl.Marker marker = new xal.smf.impl.Marker(id);
		marker.setPosition(currentPosition);
		return marker;
	}

	private static class BPMChannelSuite extends ChannelSuite {
		private String name;

		public BPMChannelSuite(String name) {
			this.name = name.replace('_', ':');
		}

		/**
		 * Write data to the data adaptor for storage.
		 * 
		 * @param adaptor
		 *            The adaptor to which the receiver's data is written
		 */
		@Override
		public void write(final DataAdaptor adaptor) {
			writeChannel(adaptor, BPM.X_AVG_HANDLE, name + ":XAvg");
			writeChannel(adaptor, BPM.Y_AVG_HANDLE, name + ":YAvg");
			writeChannel(adaptor, BPM.X_TBT_HANDLE, name + ":XTBT");
			writeChannel(adaptor, BPM.Y_TBT_HANDLE, name + ":YTBT");
			writeChannel(adaptor, BPM.PHASE_AVG_HANDLE, name + ":PhsAvg");
			writeChannel(adaptor, BPM.AMP_AVG_HANDLE, name + ":AmpAvg");
			writeChannel(adaptor, BPM.AMP_TBT_HANDLE, name + ":AmpTBT");
			writeChannel(adaptor, BPM.PHASE_TBT_HANDLE, name + ":PhsTBT");
		}

		public void writeChannel(final DataAdaptor adaptor, String handle, String signal) {

			final DataAdaptor channelAdaptor = adaptor.createChild("channel");
			channelAdaptor.setValue("handle", handle);
			channelAdaptor.setValue("signal", signal);
			channelAdaptor.setValue("settable", false);
		}
	}

	private AcceleratorNode exportBPM(final Subsystem element, double currentPosition) {
		xal.smf.impl.BPM bpm = new BPM(element.getName()) {
			{
				channelSuite = new BPMChannelSuite(element.getName());
			}
		};
		bpm.getBPMBucket().setFrequency(getFrequency(element));
		bpm.getBPMBucket().setLength(1.0);
		bpm.getBPMBucket().setOrientation(1);
		bpm.setPosition(currentPosition);
		return bpm;
	}

	private static class ElectromagnetChannelSuite extends ChannelSuite {
		private String name, signal = "B";

		public ElectromagnetChannelSuite(String name) {
			this.name = name.replace('_', ':');
		}

		public ElectromagnetChannelSuite(String name, String signal) {
			this.name = name.replace('_', ':');
			this.signal = signal;
		}

		/**
		 * Write data to the data adaptor for storage.
		 * 
		 * @param adaptor
		 *            The adaptor to which the receiver's data is written
		 */
		@Override
		public void write(final DataAdaptor adaptor) {
			final DataAdaptor channelAdaptor = adaptor.createChild("channel");

			channelAdaptor.setValue("handle", Electromagnet.FIELD_RB_HANDLE);
			channelAdaptor.setValue("signal", name + ":" + signal);
			channelAdaptor.setValue("settable", false);
		}
	}

	private static class RFCavityChannelSuite extends ChannelSuite {
		private String name;

		public RFCavityChannelSuite(String name) {
			this.name = name.replace('_', ':');
		}

		/**
		 * Write data to the data adaptor for storage.
		 * 
		 * @param adaptor
		 *            The adaptor to which the receiver's data is written
		 */
		@Override
		public void write(final DataAdaptor adaptor) {
			writeChannel(adaptor, RfCavity.CAV_AMP_SET_HANDLE, name + ":AmpCtl", true);
			writeChannel(adaptor, RfCavity.CAV_PHASE_SET_HANDLE, name + ":PhsCtl", true);
			writeChannel(adaptor, RfCavity.CAV_AMP_AVG_HANDLE, name + ":AmpAvg", false);
			writeChannel(adaptor, RfCavity.CAV_PHASE_AVG_HANDLE, name + ":PhsAvg", false);
		}

		public void writeChannel(final DataAdaptor adaptor, String handle, String signal, boolean settable) {

			final DataAdaptor channelAdaptor = adaptor.createChild("channel");
			channelAdaptor.setValue("handle", handle);
			channelAdaptor.setValue("signal", signal);
			channelAdaptor.setValue("settable", settable);
		}
	}

	private static class MagnetChannelSuite extends ChannelSuite {
		private String name;

		public MagnetChannelSuite(String name) {
			this.name = name.replace('_', ':');
		}

		/**
		 * Write data to the data adaptor for storage.
		 * 
		 * @param adaptor
		 *            The adaptor to which the receiver's data is written
		 */
		@Override
		public void write(final DataAdaptor adaptor) {
			writeChannel(adaptor, MagnetPowerSupply.CURRENT_RB_HANDLE, name + ":CurRB", false);
			writeChannel(adaptor, MagnetPowerSupply.CURRENT_SET_HANDLE, name + ":CurSet", true);
			writeChannel(adaptor, MagnetMainSupply.FIELD_RB_HANDLE, name + ":FldRB", false);
			writeChannel(adaptor, MagnetMainSupply.FIELD_SET_HANDLE, name + ":FldSet", true);
			writeChannel(adaptor, MagnetPowerSupply.CYCLE_STATE_HANDLE, name + ":CycSt", false);
			writeChannel(adaptor, MagnetMainSupply.CYCLE_ENABLE_HANDLE, name + ":CycEn", true);
		}

		public void writeChannel(final DataAdaptor adaptor, String handle, String signal, boolean settable) {

			final DataAdaptor channelAdaptor = adaptor.createChild("channel");
			channelAdaptor.setValue("handle", handle);
			channelAdaptor.setValue("signal", signal);
		}
	}

	private static class MagnetSupply extends MagnetMainSupply {
		public MagnetSupply(String name) {
			super(null);
			strId = name + "-PS";
			channelSuite = new MagnetChannelSuite(name);
		}
	}

	private AcceleratorNode exportQuadrupole(final Quadrupole element, double currentPosition) {
		double L = element.getLength();
		double G = element.getQuadrupoleGradient();

		final MagnetSupply ps = new MagnetSupply(element.getName());
		magnetPowerSupplies.add(ps);
		xal.smf.impl.Quadrupole quad = new xal.smf.impl.Quadrupole(element.getName()) { // there's
																						// no
																						// setter
																						// for
																						// type
																						// (you
																						// need
																						// to
																						// extend
																						// class)
			{
				_type = "Q";
				channelSuite = new ElectromagnetChannelSuite(element.getName());
				mainSupplyId = ps.getId();
			}
		};

		quad.setPosition(currentPosition + L * 0.5); // always position on
														// center!
		quad.setLength(L); // effLength below is actually the only one read
		quad.getMagBucket().setEffLength(L);

		quad.setDfltField(G);
		quad.getMagBucket().setPolarity(1);
		updateApertureBucket(element, quad.getAper());

		return quad;
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
		final MagnetSupply vcps = new MagnetSupply(element.getName() + "-VC");
		magnetPowerSupplies.add(vcps);

		VDipoleCorr vcorr = new VDipoleCorr(element.getName() + "-VC") {
			{
				channelSuite = new ElectromagnetChannelSuite(element.getName() + "-VC");
				mainSupplyId = vcps.getId();
			}
		};
		vcorr.setPosition(currentPosition + L / 2.);
		vcorr.setLength(L);
		vcorr.getMagBucket().setEffLength(L);
		updateApertureBucket(element, vcorr.getAper());
		seq.addNode(vcorr);

		final MagnetSupply hcps = new MagnetSupply(element.getName() + "-HC");
		magnetPowerSupplies.add(hcps);

		HDipoleCorr hcorr = new HDipoleCorr(element.getName() + "-HC") {
			{
				channelSuite = new ElectromagnetChannelSuite(element.getName() + "-HC");
				mainSupplyId = hcps.getId();
			}
		};
		hcorr.setPosition(currentPosition + L / 2.);
		hcorr.setLength(L);
		hcorr.getMagBucket().setEffLength(L);
		updateApertureBucket(element, hcorr.getAper());
		seq.addNode(hcorr);

		return seq;
	}

	private AcceleratorNode exportBend(final Bend element, double currentPosition) {
		double alpha_deg = element.getBendAngle();
		double rho = element.getCurvatureRadius();
		double entry_angle_deg = element.getEntranceAngle();
		double exit_angle_deg = element.getExitAngle();
		double G = element.getGap();
		double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;
		double N = 0;

		// mm -> m
		rho *= 1e-3;
		G *= 1e-3;

		// calculations
		double len = Math.abs(rho * alpha_deg * Math.PI / 180.0);
		double quadComp = -N / (rho * rho);

		double k = beta_gamma_Er_by_e0_c;
		double B0 = k / rho * Math.signum(alpha_deg);

		final MagnetSupply ps = new MagnetSupply(element.getName());
		magnetPowerSupplies.add(ps);
		se.lu.esss.ics.jels.smf.impl.ESSBend bend = new se.lu.esss.ics.jels.smf.impl.ESSBend(element.getName(),
				Orientation.HORIZONTAL.equals(element.getOrientation()) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL) {
			{
				channelSuite = new ElectromagnetChannelSuite(element.getName());
				mainSupplyId = ps.getId();
			}
		};
		bend.setPosition(currentPosition + len * 0.5); // always position on
														// center!
		bend.setLength(len); // both paths are used in calculation
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle_deg);
		bend.getMagBucket().setBendAngle(alpha_deg);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle_deg);
		bend.setDfltField(B0);
		bend.getMagBucket().setDipoleQuadComponent(quadComp);

		bend.setGap(G);
		bend.setEntrK1(entrK1);
		bend.setEntrK2(entrK2);
		bend.setExitK1(exitK1);
		bend.setExitK2(exitK2);

		updateApertureBucket(element, bend.getAper());

		return bend;
	}

	private AcceleratorNode exportRFCavity(final RFCavity element, double currentPosition) {
		double E0TL = element.getGapVoltage();
		double Phis = element.getPhase();
		double betas = element.getBeta();
		double Ts = element.getTransitTimeFactor();
		double kTs = element.getkT();
		double k2Ts = element.getK2T();
		double kS = element.getkS();
		double k2S = element.getK2S();

		// setup
		final RfGap gap = new RfGap(element.getName() + ":G");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning

		// following are used to calculate E0TL
		double length = 1.0; // length is not given in TraceWin, but is used
								// only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length);
		gap.getRfGap().setAmpFactor(1.0);

		ESSRfCavity cavity = new ESSRfCavity(element.getName()) {
			{
				channelSuite = new RFCavityChannelSuite(element.getName());
			}
		};
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(E0TL * 1e-6 / length);
		cavity.getRfField().setFrequency(getFrequency(element) * 1e-6);
		gap.getRfGap().setTTF(1.0);

		// TTF
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		} else {
			cavity.getRfField().setTTFCoefs(new double[] { betas, Ts, kTs, k2Ts });
			cavity.getRfField().setTTF_endCoefs(new double[] { betas, Ts, kTs, k2Ts });
			cavity.getRfField().setSTFCoefs(new double[] { betas, 0., kS, k2S });
			cavity.getRfField().setSTF_endCoefs(new double[] { betas, 0., kS, k2S });
		}

		updateApertureBucket(element, gap.getAper());

		cavity.setPosition(currentPosition);
		cavity.setLength(0.0);
		return cavity;
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

		ESSRfCavity cavity = new ESSRfCavity(element.getName()) {
			{
				channelSuite = new RFCavityChannelSuite(element.getName());
			}
		};
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(E0T * 1e-6);
		cavity.getRfField().setFrequency(frequency * 1e-6);

		// TTF
		if (betas == 0.0) {
			cavity.getRfField().setTTF_startCoefs(new double[] {});
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		} else {
			cavity.getRfField().setTTF_startCoefs(new double[] { betas, Ti, kTi, k2Ti });
			cavity.getRfField().setTTFCoefs(new double[] { betas, Ts, kTs, k2Ts });
			cavity.getRfField().setTTF_endCoefs(new double[] { betas, To, kTo, k2To });
		}

		// setup
		ESSRfGap firstgap = new ESSRfGap(element.getName() + ":G0");

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
			cavity.getRfField().setStructureMode(1);
		} else { // m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;
			pos0 = 0.25 * betag * lambda + dzi;
			posn = Lc0 + (n - 2) * Lc + 0.5 * betag * lambda + dzo;
		}

		firstgap.setFirstGap(true); // this uses only phase for calculations
		firstgap.getRfGap().setEndCell(0);
		firstgap.setLength(0); // used only for positioning
		firstgap.setPosition(pos0);

		// following are used to calculate E0TL
		firstgap.getRfGap().setLength(Lc0);
		firstgap.getRfGap().setAmpFactor(amp0);
		firstgap.getRfGap().setTTF(1);
		cavity.addNode(firstgap);

		for (int i = 1; i < n - 1; i++) {
			ESSRfGap gap = new ESSRfGap(element.getName() + ":G" + i);
			gap.getRfGap().setTTF(1);
			gap.setPosition(Lc0 + (i - 0.5) * Lc);
			gap.setLength(0);
			gap.getRfGap().setLength(Lc);
			gap.getRfGap().setAmpFactor(1.0);
			cavity.addNode(gap);
		}

		ESSRfGap lastgap = new ESSRfGap(element.getName() + ":G" + (n - 1));
		lastgap.getRfGap().setEndCell(1);
		lastgap.setLength(0); // used only for positioning
		lastgap.setPosition(posn);

		// following are used to calculate E0TL
		lastgap.getRfGap().setLength(Lcn);
		lastgap.getRfGap().setAmpFactor(ampn);
		lastgap.getRfGap().setTTF(1);
		cavity.addNode(lastgap);

		cavity.setLength(Lc0 + (n - 2) * Lc + Lcn);
		cavity.setPosition(currentPosition);
		return cavity;
	}

	private AcceleratorNode exportFieldMap(final FieldMap element, double currentPosition) {
		ESSFieldMap fm = new ESSFieldMap(element.getName());
		fm.setPosition(currentPosition + element.getLength() * 0.5);
		fm.setLength(element.getLength());
		fm.setFrequency(getFrequency(element) * 1e-6);
		fm.setXelmax(element.getElectricIntensityFactor());
		fm.setPhase(element.getRfPhase());
		fm.setFieldMapFile(element.getFileName());
		fm.setFieldProfile(FieldProfile
				.getInstance(OpenXalExporter.class.getResource("/" + element.getFileName() + ".edz").toString()));
		updateApertureBucket(element, fm.getAper());

		return fm;
	}

	private AcceleratorNode exportDTLCell(final DTLCell element, double currentPosition) {
		// mm -> m
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

		// setup
		// QUAD1,2
		final MagnetSupply ps1 = new MagnetSupply(element.getName() + "A");
		magnetPowerSupplies.add(ps1);

		xal.smf.impl.Quadrupole quad1 = new xal.smf.impl.Quadrupole(element.getName() + ":Q1") { // there's
																									// no
																									// setter
																									// for
																									// type
																									// (you
																									// need
																									// to
																									// extend
																									// class)
			{
				_type = "Q";
				channelSuite = new ElectromagnetChannelSuite(element.getName(), "B1");
				mainSupplyId = ps1.getId();
			}
		};
		quad1.setPosition(0.5 * Lq1); // always position on center!
		quad1.setLength(Lq1); // effLength below is actually the only one read
		quad1.getMagBucket().setEffLength(Lq1);
		quad1.setDfltField(B1);
		quad1.getMagBucket().setPolarity(1);

		final MagnetSupply ps2 = new MagnetSupply(element.getName() + "B");
		magnetPowerSupplies.add(ps2);

		xal.smf.impl.Quadrupole quad2 = new xal.smf.impl.Quadrupole(element.getName() + ":Q2") { // there's
																									// no
																									// setter
																									// for
																									// type
																									// (you
																									// need
																									// to
																									// extend
																									// class)
			{
				_type = "Q";
				channelSuite = new ElectromagnetChannelSuite(element.getName(), "B2");
				mainSupplyId = ps2.getId();
			}
		};
		quad2.setPosition(L - 0.5 * Lq2); // always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2);
		quad2.getMagBucket().setPolarity(1);

		// GAP
		RfGap gap = new RfGap(element.getName() + ":G");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		gap.setPosition(0.5 * L - g);
		// following are used to calculate E0TL
		double length = L - Lq1 - Lq2; // length is not given in TraceWin, but
										// is used only as a factor in E0TL in
										// OpenXal
		gap.getRfGap().setLength(length);
		gap.getRfGap().setAmpFactor(1.0);
		gap.getRfGap().setTTF(1.0);

		ESSRfCavity dtlTank = new ESSRfCavity(element.getName()) {
			{
				channelSuite = new RFCavityChannelSuite(element.getName());
			}
		};
		;
		dtlTank.addNode(quad1);
		dtlTank.addNode(gap);
		dtlTank.addNode(quad2);
		dtlTank.getRfField().setPhase(Phis);
		dtlTank.getRfField().setAmplitude(E0TL * 1e-6 / length);
		dtlTank.getRfField().setFrequency(getFrequency(element) * 1e-6);

		// TTF
		if (betas == 0.0) {
			dtlTank.getRfField().setTTFCoefs(new double[] { 0.0 });
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] { betas, Ts, kTs, k2Ts });
			dtlTank.getRfField().setTTF_endCoefs(new double[] { betas, Ts, kTs, k2Ts });
			dtlTank.getRfField().setSTFCoefs(new double[] { betas, 0., kS, k2S });
			dtlTank.getRfField().setSTF_endCoefs(new double[] { betas, 0., kS, k2S });
		}
		dtlTank.setLength(L);
		dtlTank.setPosition(currentPosition);

		return dtlTank;
	}

	private static void updateApertureBucket(BeamlineElement element, ApertureBucket aper) {
		if (element.getApertureX() != null)
			aper.setAperX(element.getApertureX());
		if (element.getApertureY() != null)
			aper.setAperY(element.getApertureY());
		int apertureCode = element.getApertureType() == null ? 2 : element.getApertureType().getIntegerValue();
		aper.setShape(toOpenXALApertureCode(apertureCode));
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