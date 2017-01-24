package eu.ess.lt.parser.tracewin;

import java.util.ArrayList;

import eu.ess.bled.Subsystem;

/**
 * 
 * <code>Section</code> defines a single section within the lattice file. The
 * section is composed of varoius components (elements and commands) as well as
 * slots and beamlines.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
 *         Prelog</a>
 *
 */
public class Section {

	public final int sectionNumber;
	public final int periodType;
	public final double syncPhaseInput;
	public final double syncPhaseOutput;
	public final double syncPhaseMax;
	public final double syncPhaseStepMax;
	public final boolean syncPhaseContinuity;
	public final boolean syncPhaseConstantAcceptance;

	public final double phaseAdvanceMax;
	public final double phaseAdvanceStep;
	public final boolean phaseAdvanceContinuity;

	public final double cavityBetaG;
	public final double cavityFrequency;
	public final double cavityMaxPower;
	public final double cavityAccElectricField;

	public final boolean analyticModel;
	public final String cavityFile;

	public final String radiusData;

	private ArrayList<Subsystem> components = new ArrayList<Subsystem>();
	private ArrayList<Subsystem> slots = new ArrayList<Subsystem>();
	private ArrayList<Subsystem> beamlines = new ArrayList<Subsystem>();

	/**
	 * Constructs a new Section containing the lattice data. The arguments for
	 * the constructor are the actual section header data.
	 * 
	 * @param sectionNumber
	 *            the number of the section in the file
	 * @param periodType
	 *            ??
	 * @param syncPhaseInput
	 *            phase input in degrees
	 * @param syncPhaseOutput
	 *            phase output in degrees
	 * @param syncPhaseMax
	 *            phase maximum in degrees
	 * @param syncPhaseStepMax
	 *            maximum phase step in degrees
	 * @param syncPhaseContinuity
	 *            ??
	 * @param syncPhaseConstantAcceptance
	 *            ??
	 * @param phaseAdvanceMax
	 *            maximum phase advance in degrees
	 * @param phaseAdvanceStep
	 *            step size degrees per meter
	 * @param phaseAdvanceContinuity
	 *            ??
	 * @param cavityBetaG
	 *            reduced speed
	 * @param cavityFrequency
	 *            the frequency in Hz
	 * @param cavityMaxPower
	 *            maximal power in W
	 * @param cavityAccElectricField
	 *            accelerating electric field in V/m
	 * @param analyticModel
	 *            ??
	 * @param cavityFile
	 *            ??
	 * @param radiusData
	 *            ??
	 */
	public Section(int sectionNumber, int periodType, double syncPhaseInput, double syncPhaseOutput,
			double syncPhaseMax, double syncPhaseStepMax, boolean syncPhaseContinuity,
			boolean syncPhaseConstantAcceptance, double phaseAdvanceMax, double phaseAdvanceStep,
			boolean phaseAdvanceContinuity, double cavityBetaG, double cavityFrequency, double cavityMaxPower,
			double cavityAccElectricField, boolean analyticModel, String cavityFile, String radiusData) {

		this.sectionNumber = sectionNumber;
		this.periodType = periodType;
		this.syncPhaseInput = syncPhaseInput;
		this.syncPhaseOutput = syncPhaseOutput;
		this.syncPhaseMax = syncPhaseMax;
		this.syncPhaseStepMax = syncPhaseStepMax;
		this.syncPhaseContinuity = syncPhaseContinuity;
		this.syncPhaseConstantAcceptance = syncPhaseConstantAcceptance;
		this.phaseAdvanceMax = phaseAdvanceMax;
		this.phaseAdvanceStep = phaseAdvanceStep;
		this.phaseAdvanceContinuity = phaseAdvanceContinuity;
		this.cavityBetaG = cavityBetaG;
		this.cavityFrequency = cavityFrequency;
		this.cavityMaxPower = cavityMaxPower;
		this.cavityAccElectricField = cavityAccElectricField;
		this.analyticModel = analyticModel;
		this.cavityFile = cavityFile;
		this.radiusData = radiusData;
	}

	/**
	 * Constructs a new Section with null or NaN header values and the given
	 * section id number.
	 * 
	 * @param sectionNumber
	 *            the section id number
	 */
	public Section(int sectionNumber) {
		this(sectionNumber, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false, Double.NaN, Double.NaN,
				false, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, null, null);
	}

	/**
	 * Adds the component to the stack of component as the last component in the
	 * current lattice.
	 * 
	 * @param component
	 *            the component to add
	 */
	public void addComponent(Subsystem component) {
		if (component == null)
			return;
		components.add(component);
	}

	/**
	 * Returns the array of all components in the section.
	 * 
	 * @return the array of components
	 */
	public Subsystem[] getComponents() {
		return components.toArray(new Subsystem[components.size()]);
	}

	/**
	 * Adds the slot to the stack of slots. If slots are arranged so that there
	 * exists a master slot which contains the other slots, then the order at
	 * which the slots are inserted is not important.
	 * 
	 * @param slot
	 *            the slot to add
	 */
	public void addSlot(Subsystem slot) {
		if (slot == null)
			return;
		slots.add(slot);
	}

	/**
	 * Returns the array of all slots in this section.
	 * 
	 * @return the array of slots
	 */
	public Subsystem[] getSlots() {
		return slots.toArray(new Subsystem[slots.size()]);
	}

	/**
	 * Adds the beamline to the stack of beamlines.
	 * 
	 * @param beamline
	 *            the beamline to add
	 */
	public void addBeamline(Subsystem beamline) {
		if (beamline == null)
			return;
		beamlines.add(beamline);
	}

	/**
	 * Returns the array of all beamlines in this section.
	 * 
	 * @return the array of beamlines
	 */
	public Subsystem[] getBeamlines() {
		return beamlines.toArray(new Subsystem[beamlines.size()]);
	}
}
