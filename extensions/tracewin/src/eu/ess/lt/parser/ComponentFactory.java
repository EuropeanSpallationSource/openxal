package eu.ess.lt.parser;

import java.io.File;

import eu.ess.bled.Subsystem;
import eu.ess.bled.devices.lattice.Aperture;
import eu.ess.bled.devices.lattice.BPM;
import eu.ess.bled.devices.lattice.BeamlineElement.ApertureType;
import eu.ess.bled.devices.lattice.Bend;
import eu.ess.bled.devices.lattice.Bend.Orientation;
import eu.ess.bled.devices.lattice.Corrector;
import eu.ess.bled.devices.lattice.DTLCell;
import eu.ess.bled.devices.lattice.Drift;
import eu.ess.bled.devices.lattice.ElectrostaticAcceleration;
import eu.ess.bled.devices.lattice.ElectrostaticBend;
import eu.ess.bled.devices.lattice.ElectrostaticQuadropole;
import eu.ess.bled.devices.lattice.FieldMap;
import eu.ess.bled.devices.lattice.LatticeCommand;
import eu.ess.bled.devices.lattice.Marker;
import eu.ess.bled.devices.lattice.MultipoleMagnet;
import eu.ess.bled.devices.lattice.MultipoleMagnet.MagnetType;
import eu.ess.bled.devices.lattice.NCell;
import eu.ess.bled.devices.lattice.Quadrupole;
import eu.ess.bled.devices.lattice.RFCavity;
import eu.ess.bled.devices.lattice.RFCavity.CavityType;
import eu.ess.bled.devices.lattice.RFQCell;
import eu.ess.bled.devices.lattice.Solenoid;
import eu.ess.bled.devices.lattice.SpaceChargeCompensation;
import eu.ess.bled.devices.lattice.ThinLens;

/**
 * A factory that generates BLED entities. If the factory can connect to the
 * BLED database, it will first attempt to retrieve the entity instance from the
 * database. This will prevent problems when saving the changes, since there
 * will be no attempts to duplicate entities.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
 *         Prelog</a>
 */
public class ComponentFactory {
	private String fieldmapPath = "";
	private String basePath = "";
	
	
	/**
	 * Returns an existing {@link Aperture} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the aperture.
	 * @param dx
	 *            <code>double</code> aperture's X position.
	 * @param dy
	 *            <code>double</code> aperture's Y position.
	 * @param apertureType
	 *            {@link ApertureType} type of the aperture.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Aperture} with the specified field values.
	 */
	public Aperture getAperture(String name, double dx, double dy, ApertureType apertureType,
			Integer previousSubsystem) {
		Aperture aperture = new Aperture();
		aperture.setName(name);
		aperture.setApertureX(dx);
		aperture.setApertureY(dy);
		aperture.setApertureType(apertureType);
		aperture.setPreviousSubsystem(previousSubsystem);
		// CHECK aperture.setSubsystemType(defaultType());
		return aperture;
	}

	/**
	 * Returns an existing {@link Bend} identified by its name, or creates a new
	 * one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the bending magnet.
	 * @param bendAngle
	 *            <code>double</code> angle of bending.
	 * @param curvatureRadius
	 *            <code>double</code> radius of the trajectory curve.
	 * @param gradientIndex
	 *            <code>int</code> gradient index.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param orientation
	 *            {@link Orientation} orientation of the bending magnet.
	 * @param gap
	 *            <code>double</code> total gap of the magnet.
	 * @param entranceAngle
	 *            <code>double</code> angle of the incoming beam.
	 * @param entranceCurvature
	 *            <code>double</code> curvature of the incoming beam.
	 * @param exitAngle
	 *            <code>double</code> angle of the outgoing beam.
	 * @param exitCurvature
	 *            <code>double</code> curvature of the outgoing beam.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Bend} with the specified field values.
	 */
	public Bend getBend(String name, String entranceEdgeName, String exitEdgeName, double bendAngle,
			double curvatureRadius, int gradientIndex, double aperture, Orientation orientation, double gap,
			double entranceAngle, double entranceCurvature, double exitAngle, double exitCurvature,
			Integer previousSubsystem) {
		Bend bend = new Bend();
		// Bend parameters
		bend.setName(name);
		bend.setBendAngle(bendAngle);
		bend.setCurvatureRadius(curvatureRadius);
		bend.setApertureX(aperture);
		bend.setOrientation(orientation);
		bend.setGap(gap);
		// Entrance edge
		bend.setEntranceAngle(entranceAngle);
		bend.setEntranceCurvature(entranceCurvature);
		bend.setEntranceName(entranceEdgeName);
		// Exit edge
		bend.setExitAngle(exitAngle);
		bend.setExitCurvature(exitCurvature);
		bend.setPreviousSubsystem(previousSubsystem);
		bend.setExitName(exitEdgeName);
		// CHECK bend.setSubsystemType(defaultType());
		return bend;
	}

	/**
	 * Returns an existing {@link RFCavity} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the RF cavity.
	 * @param gapVoltage
	 *            <code>double</code> effective gap voltage.
	 * @param rfPhase
	 *            <code>double</code> RF phase.
	 * @param apertureRadius
	 *            <code>double</code> aperture size.
	 * @param isPhaseAbsolute
	 *            <code>boolean</code> 1 if phase is absolute, 0 if it's
	 *            relative.
	 * @param beta
	 *            <code>double</code>
	 * @param transitTimeFactor
	 *            <code>double</code> transit time factor.
	 * @param kT
	 *            <code>double</code> transit time factor coefficient.
	 * @param kkT
	 *            <code>double</code> transit time factor coefficient.
	 * @param kS
	 *            <code>double</code> transit time factor coefficient.
	 * @param kkS
	 *            <code>double</code> transit time factor coefficient.
	 * @param cavityType
	 *            {@link CavityType} type of the cavity.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link RFCavity} with the specified field values.
	 */
	public RFCavity getRFCavity(String name, double gapVoltage, double rfPhase, double apertureRadius,
			boolean isPhaseAbsolute, double beta, double transitTimeFactor, double kT, double kkT, double kS,
			double kkS, CavityType cavityType, Integer previousSubsystem) {
		RFCavity rfCavity = new RFCavity();
		rfCavity.setName(name);
		rfCavity.setGapVoltage(gapVoltage);
		rfCavity.setPhase(rfPhase);
		rfCavity.setApertureX(apertureRadius);
		rfCavity.setAbsolutePhase(isPhaseAbsolute);
		rfCavity.setBeta(beta);
		rfCavity.setTransitTimeFactor(transitTimeFactor);
		rfCavity.setkT(kT);
		rfCavity.setK2T(kkT);
		rfCavity.setkS(kS);
		rfCavity.setK2S(kkS);
		rfCavity.setType(cavityType);
		rfCavity.setPreviousSubsystem(previousSubsystem);
		// CHECK rfCavity.setSubsystemType(defaultType());
		return rfCavity;
	}

	/**
	 * Returns an existing {@link Drift} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the drift element.
	 * @param length
	 *            <code>double</code> length of the drift.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param apertureY
	 *            <code>double</code> aperture position.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Drift} with the specified field values.
	 */
	public Drift getDrift(String name, double length, double aperture, double apertureY, Integer previousSubsystem) {
		Drift drift = new Drift();
		drift.setName(name);
		drift.setLength(length);
		drift.setApertureX(aperture);
		drift.setApertureY(apertureY);
		drift.setPreviousSubsystem(previousSubsystem);
		// CHECK drift.setSubsystemType(defaultType());
		return drift;
	}

	/**
	 * Returns an existing {@link DTLCell} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the DTL cell.
	 * @param length
	 *            <code>double</code> length of the DTL cell.
	 * @param quadLength1
	 *            <code>double</code> first 1/2 quadrupole length.
	 * @param quadLength2
	 *            <code>double</code> second 1/2 quadrupole length.
	 * @param cellCenter
	 *            <code>double</code> cell center.
	 * @param fieldGradient1
	 *            <code>double</code> first magnetic field gradient.
	 * @param fieldGradient2
	 *            <code>double</code> second magnetic field gradient.
	 * @param gapVoltage
	 *            <code>double</code> effective gap voltage.
	 * @param rfPhase
	 *            <code>double</code> RF phase.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param isPhaseAbsolute
	 *            <code>boolean</code> 1 if phase is absolute, 0 if it's
	 *            relative.
	 * @param beta
	 *            <code>double</code> particle reduced velocity.
	 * @param transitTimeFactor
	 *            <code>double</code> transit time factor.
	 * @param kT
	 *            <code>double</code> transit time factor coefficient.
	 * @param kkT
	 *            <code>double</code> transit time factor coefficient.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link DTLCell} with the specified field values.
	 */
	public DTLCell getDTLCell(String name, double length, double quadLength1, double quadLength2, double cellCenter,
			double fieldGradient1, double fieldGradient2, double gapVoltage, double rfPhase, double aperture,
			boolean isPhaseAbsolute, double beta, double transitTimeFactor, double kT, double kkT,
			Integer previousSubsystem) {
		DTLCell dtlCell = new DTLCell();
		dtlCell.setName(name);
		dtlCell.setLength(length);
		dtlCell.setLq1(quadLength1);
		dtlCell.setLq2(quadLength2);
		dtlCell.setCellCenter(cellCenter);
		dtlCell.setB1p(fieldGradient1);
		dtlCell.setB2p(fieldGradient2);
		dtlCell.setE0TL(gapVoltage);
		dtlCell.setRfPhase(rfPhase);
		dtlCell.setApertureX(aperture);
		dtlCell.setAbsolutePhase(isPhaseAbsolute);
		dtlCell.setBetas(beta);
		dtlCell.setTransitTime(transitTimeFactor);
		dtlCell.setkTsp(kT);
		dtlCell.setK2Tsp(kkT);
		dtlCell.setPreviousSubsystem(previousSubsystem);
		// CHECK dtlCell.setSubsystemType(defaultType());
		return dtlCell;
	}

	/**
	 * Returns an existing {@link ElectrostaticAcceleration} identified by its
	 * name, or creates a new one. The returned entity has its fields set to the
	 * specified values.
	 *
	 * @param name
	 *            {@link String} name of the electrostatic acceleration element.
	 * @param length
	 *            <code>double</code> length of the electrostatic acceleration
	 *            element.
	 * @param voltage
	 *            <code>double</code> voltage.
	 * @param defocal
	 *            <code>double</code> transverse defocal.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link ElectrostaticAcceleration} with the specified field
	 *         values.
	 */
	public ElectrostaticAcceleration getElectrostaticAcceleration(String name, double length, double voltage,
			double defocal, double aperture, Integer previousSubsystem) {
		ElectrostaticAcceleration acceleration = new ElectrostaticAcceleration();
		acceleration.setName(name);
		acceleration.setLength(length);
		acceleration.setVoltage(voltage);
		acceleration.setDefocal(defocal);
		acceleration.setApertureX(aperture);
		acceleration.setPreviousSubsystem(previousSubsystem);
		// CHECK acceleration.setSubsystemType(defaultType());
		return acceleration;
	}

	/**
	 * Returns an existing {@link ElectrostaticQuadropole} identified by its
	 * name, or creates a new one. The returned entity has its fields set to the
	 * specified values.
	 *
	 * @param name
	 *            {@link String} name of the electrostatic quadrupole.
	 * @param length
	 *            <code>double</code> length of the electrostatic quadrupole.
	 * @param voltage
	 *            <code>double</code> voltage between electrodes.
	 * @param apertureRadius
	 *            <code>double</code> aperture size.
	 * @param skewAngle
	 *            <code>double</code> skew angle.
	 * @param sextupoleVoltage
	 *            <code>double</code> sextupole voltage component.
	 * @param octupoleVoltage
	 *            <code>double</code> octupole voltage component.
	 * @param decapoleVoltage
	 *            <code>double</code> decapole voltage component.
	 * @param dodecapoleVoltage
	 *            <code>double</code> dodecapole voltage component.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link ElectrostaticQuadropole} with the specified field values.
	 */
	public ElectrostaticQuadropole getElectrostaticQuadrupole(String name, double length, double voltage,
			double apertureRadius, double skewAngle, double sextupoleVoltage, double octupoleVoltage,
			double decapoleVoltage, double dodecapoleVoltage, Integer previousSubsystem) {
		ElectrostaticQuadropole quadrupole = new ElectrostaticQuadropole();
		quadrupole.setName(name);
		quadrupole.setLength(length);
		quadrupole.setVoltage(voltage);
		quadrupole.setApertureX(apertureRadius);
		quadrupole.setSkewAngle(skewAngle);
		quadrupole.setSextupoleVoltage(sextupoleVoltage);
		quadrupole.setOctupoleVoltage(octupoleVoltage);
		quadrupole.setDecapoleVoltage(decapoleVoltage);
		quadrupole.setDodecapoleVoltage(dodecapoleVoltage);
		quadrupole.setPreviousSubsystem(previousSubsystem);
		// CHECK quadrupole.setSubsystemType(defaultType());
		return quadrupole;
	}

	/**
	 * Returns an existing {@link FieldMap} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the field map.
	 * @param length
	 *            <code>double</code> length of the field map element.
	 * @param geom
	 *            <code>int</code> field map type.
	 * @param rfPhase
	 *            <code>double</code> RF phase.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param magneticIntensityFactor
	 *            <code>double</code> magnetic field intensity factor.
	 * @param electricIntensityFactor
	 *            <code>double</code> electric field intensity factor.
	 * @param spaceChargeCompensationFactor
	 *            <code>double</code> space charge compensation factor.
	 * @param apertureFlag
	 *            <code>int</code> aperture flag.
	 * @param fileName
	 *            {@link String} file name without extension.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link FieldMap} with the specified field values.
	 */
	public FieldMap getFieldMap(String name, double length, int geom, double rfPhase, double aperture,
			double magneticIntensityFactor, double electricIntensityFactor, double spaceChargeCompensationFactor,
			int apertureFlag, String fileName, Integer previousSubsystem) {
		FieldMap fieldMap = new FieldMap();
		fieldMap.setName(name);
		fieldMap.setLength(length);
		fieldMap.setGeom(geom);
		fieldMap.setRfPhase(rfPhase);
		fieldMap.setApertureX(aperture);
		fieldMap.setMagneticIntensityFactor(magneticIntensityFactor);
		fieldMap.setElectricIntensityFactor(electricIntensityFactor);
		fieldMap.setSpaceChargeCompensationFactor(spaceChargeCompensationFactor);
		fieldMap.setApertureFlag(apertureFlag);
		fieldMap.setFileName(fieldmapPath + fileName);
		fieldMap.setBasePath(basePath);
		fieldMap.setPreviousSubsystem(previousSubsystem);
		// CHECK fieldMap.setSubsystemType(defaultType());
		return fieldMap;
	}

	/**
	 * Returns an existing {@link MultipoleMagnet} identified by its name, or
	 * creates a new one. The returned entity has its fields set to the
	 * specified values.
	 *
	 * @param name
	 *            {@link String} name of the multipole magnet.
	 * @param length
	 *            <code>double</code> length of the multipole magnet.
	 * @param magnetType
	 *            {@link MagnetType} magnet type.
	 * @param numberOfSteps
	 *            <code>int</code> number of steps along x and y directions.
	 * @param magneticField
	 *            <code>double</code> magnetic field on pole.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param solenoidLength
	 *            <code>double</code> physical length of solenoid.
	 * @param solenoidStepNumber
	 *            <code>int</code> step number of solenoid case.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link MultipoleMagnet} with the specified field values.
	 * @see MagnetType
	 */
	public MultipoleMagnet getMultipoleFieldMap(String name, double length, MagnetType magnetType, int numberOfSteps,
			double magneticField, double aperture, double solenoidLength, int solenoidStepNumber,
			Integer previousSubsystem) {
		MultipoleMagnet multipoleMagnet = new MultipoleMagnet();
		multipoleMagnet.setName(name);
		multipoleMagnet.setLength(length);
		multipoleMagnet.setMagnetType(magnetType);
		multipoleMagnet.setSteps(numberOfSteps);
		multipoleMagnet.setDipoleStrength(magneticField);
		multipoleMagnet.setApertureX(aperture);
		multipoleMagnet.setSolenoidLength(solenoidLength);
		multipoleMagnet.setSolenoidSteps(solenoidStepNumber);
		multipoleMagnet.setPreviousSubsystem(previousSubsystem);
		// CHECH multipoleMagnet.setSubsystemType(defaultType());
		return multipoleMagnet;
	}

	/**
	 * Returns an existing {@link MultipoleMagnet} of quadruple type identified
	 * by its name, or creates a new one. The returned entity has its fields set
	 * to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the quadrupole magnet.
	 * @param length
	 *            <code>double</code> length of the quadrupole magnet.
	 * @param quadrupoleGradient
	 *            <code>double</code> magnetic field gradient (T/m)
	 * @param aperture
	 *            <code>double</code> aperture
	 * @param skewAngle
	 *            <code>double</code> skew angle (degrees)
	 * @param sextupoleGradient
	 *            <code>double</code> sextupole gradient (T/m2)
	 * @param octupoleGradient
	 *            <code>double</code> octupole gradient (T/m3)
	 * @param decapoleGradient
	 *            <code>double</code> decapole gradient (T/m4)
	 * @param dodecapoleGradient
	 *            <code>double</code> dodecapole gradient (T/m5)
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link MultipoleMagnet} with the specified field values.
	 */
	public Quadrupole getQuadrupole(String name, double length, double quadrupoleGradient, double aperture,
			double skewAngle, double sextupoleGradient, double octupoleGradient, double decapoleGradient,
			double dodecapoleGradient, Integer previousSubsystem) {
		Quadrupole quadrupole = new Quadrupole();
		quadrupole.setName(name);
		quadrupole.setSteps(0);
		quadrupole.setMagnetType(MagnetType.QUADRUPOLE);
		quadrupole.setLength(length);
		quadrupole.setQuadrupoleGradient(quadrupoleGradient);
		quadrupole.setApertureX(aperture);
		quadrupole.setSkewAngle(skewAngle);
		quadrupole.setSextupoleGradient(sextupoleGradient);
		quadrupole.setOctupoleGradient(octupoleGradient);
		quadrupole.setDecapoleGradient(decapoleGradient);
		quadrupole.setDodecapoleGradient(dodecapoleGradient);
		quadrupole.setPreviousSubsystem(previousSubsystem);
		// CHECHKquadrupole.setSubsystemType(defaultType());
		return quadrupole;
	}

	/**
	 * Returns an existing {@link NCell} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the NCell element.
	 * @param length
	 *            {@link String} length of the NCell element.
	 * @param mode
	 *            <code>int</code> mode.
	 * @param numberOfCells
	 *            <code>int</code> number of cells.
	 * @param geometricBeta
	 *            <code>double</code> geometric beta.
	 * @param gapVoltage
	 *            <code>double</code> effective gap voltage.
	 * @param rfPhase
	 *            <code>double</code> RF phase.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param isPhaseAbsolute
	 *            <code>boolean</code> 1 if phase is absolute, 0 if it's
	 *            relative.
	 * @param inputFieldCorrection
	 *            <code>double</code> input field correction.
	 * @param outputFieldCorrection
	 *            <code>double</code> output field correction.
	 * @param gapDisplacement1
	 *            <code>double</code> first gap displacement.
	 * @param gapDisplacement2
	 *            <code>double</code> second gap displacement.
	 * @param beta
	 *            <code>double</code> particle reduced velocity.
	 * @param ttfMiddleGaps
	 *            <code>double</code> transit time factor for the middle gaps.
	 * @param kTMiddleGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            middle gaps.
	 * @param kktMiddleGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            middle gaps.
	 * @param ttfInputGaps
	 *            <code>double</code> transit time factor for the input gaps.
	 * @param kTInputGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            input gaps.
	 * @param kkTInputGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            input gaps.
	 * @param ttfOutputGaps
	 *            <code>double</code> transit time factor for the output gaps.
	 * @param kTOutputGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            output gaps.
	 * @param kkTOutputGaps
	 *            <code>double</code> transit time factor coefficient for the
	 *            output gaps.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link NCell} with the specified field values.
	 */
	public NCell getNCell(String name, double length, int mode, int numberOfCells, double geometricBeta,
			double gapVoltage, double rfPhase, double aperture, boolean isPhaseAbsolute, double inputFieldCorrection,
			double outputFieldCorrection, double gapDisplacement1, double gapDisplacement2, double beta,
			double ttfMiddleGaps, double kTMiddleGaps, double kktMiddleGaps, double ttfInputGaps, double kTInputGaps,
			double kkTInputGaps, double ttfOutputGaps, double kTOutputGaps, double kkTOutputGaps,
			Integer previousSubsystem) {
		NCell nCell = new NCell();
		nCell.setName(name);
		nCell.setLength(length);
		nCell.setMode(mode);
		nCell.setCellNumber(numberOfCells);
		nCell.setBetag(geometricBeta);
		nCell.setE0T(gapVoltage);
		nCell.setRfPhase(rfPhase);
		nCell.setApertureX(aperture);
		nCell.setAbsolutePhase(isPhaseAbsolute);
		nCell.setkE0Ti(inputFieldCorrection);
		nCell.setkE0To(outputFieldCorrection);
		nCell.setDzi(gapDisplacement1);
		nCell.setDzo(gapDisplacement2);
		nCell.setBetas(beta);
		nCell.setTransitTime(ttfMiddleGaps);
		nCell.setkTsp(kTMiddleGaps);
		nCell.setK2Tspp(kktMiddleGaps);
		nCell.setTransitTimeIn(ttfInputGaps);
		nCell.setkTip(kTInputGaps);
		nCell.setK2Tipp(kkTInputGaps);
		nCell.setTransitTimeOut(ttfOutputGaps);
		nCell.setkTop(kTOutputGaps);
		nCell.setK2Topp(kkTOutputGaps);
		nCell.setPreviousSubsystem(previousSubsystem);
		// CHECK nCell.setSubsystemType(defaultType());
		return nCell;
	}

	/**
	 * Returns an existing {@link RFQCell} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the RFQ cell.
	 * @param length
	 *            <code>double</code> length of the RFQ cell.
	 * @param gapVoltage
	 *            <code>double</code> effective gap voltage.
	 * @param vaneRadius
	 *            <code>double</code> vane radius.
	 * @param accParameter
	 *            <code>double</code> acceleration parameter.
	 * @param modulation
	 *            <code>double</code> modulation.
	 * @param rfPhase
	 *            <code>double</code> RF phase.
	 * @param type
	 *            {@link String} type of RF cell.
	 * @param curvature
	 *            <code>double</code> transverse curvature.
	 * @param focusing
	 *            <code>double</code> transverse focusing.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link RFQCell} with the specified field values.
	 */
	public RFQCell getRFQCell(String name, double length, double gapVoltage, double vaneRadius, double accParameter,
			double modulation, double rfPhase, String type, double curvature, double focusing,
			Integer previousSubsystem) {
		RFQCell rfqCell = new RFQCell();
		rfqCell.setName(name);
		rfqCell.setLength(length);
		rfqCell.setGapVoltage(gapVoltage);
		rfqCell.setVaneRadius(vaneRadius);
		rfqCell.setAccelParam(accParameter);
		rfqCell.setModulation(modulation);
		rfqCell.setRfPhase(rfPhase);
		rfqCell.setCellType(type);
		rfqCell.setTransCurv(curvature);
		rfqCell.setTransFocus(focusing);
		rfqCell.setPreviousSubsystem(previousSubsystem);
		// CHECK rfqCell.setSubsystemType(defaultType());
		return rfqCell;
	}

	/**
	 * Returns an existing {@link RFCavity} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the RF cavity.
	 * @param length
	 *            <code>double</code> length of the RF cavity.
	 * @param cellNumber
	 *            <code>int</code> cell number.
	 * @param averageAccField
	 *            <code>double</code> average accelerating field.
	 * @param phaseAtEntrance
	 *            <code>double</code> phase of the synchronous particle at the
	 *            entrance.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param isPhaseAbsolute
	 *            <code>boolean</code> 1 if phase is absolute, 0 if it's
	 *            relative.
	 * @param cavityType
	 *            {@link CavityType} type of the cavity.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link RFCavity} with the specified field values.
	 */
	public RFCavity getRFCavity(String name, double length, int cellNumber, double averageAccField,
			double phaseAtEntrance, double aperture, boolean isPhaseAbsolute, CavityType cavityType,
			Integer previousSubsystem) {
		RFCavity rfCavity = new RFCavity();
		rfCavity.setName(name);
		rfCavity.setLength(length);
		rfCavity.setCellNumber(cellNumber);
		rfCavity.setAverageField(averageAccField);
		rfCavity.setPhase(phaseAtEntrance);
		rfCavity.setApertureX(aperture);
		rfCavity.setAbsolutePhase(isPhaseAbsolute);
		rfCavity.setType(cavityType);
		rfCavity.setPreviousSubsystem(previousSubsystem);
		// CHECK rfCavity.setSubsystemType(defaultType());
		return rfCavity;
	}

	/**
	 * Returns an existing {@link Solenoid} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the solenoid.
	 * @param length
	 *            <code>double</code> length of the solenoid.
	 * @param magneticField
	 *            <code>double</code> magnetic field.
	 * @param aperture
	 *            <code>double</code> aperture size.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Solenoid} with the specified field values.
	 */
	public Solenoid getSolenoid(String name, double length, double magneticField, double aperture,
			Integer previousSubsystem) {
		Solenoid solenoid = new Solenoid();
		solenoid.setName(name);
		solenoid.setLength(length);
		solenoid.setMagneticField(magneticField);
		solenoid.setApertureX(aperture);
		solenoid.setPreviousSubsystem(previousSubsystem);
		// CHECK solenoid.setSubsystemType(defaultType());
		return solenoid;
	}

	/**
	 * Returns an existing {@link ThinLens} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the thin lens.
	 * @param focalLengthX
	 *            <code>double</code> focal length.
	 * @param focalLengthY
	 *            <code>double</code> focal length.
	 * @param aperture
	 *            <code>double</code> size of aperture.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link ThinLens} with the specified field values.
	 */
	public ThinLens getThinLens(String name, double focalLengthX, double focalLengthY, double aperture,
			Integer previousSubsystem) {
		ThinLens thinLens = new ThinLens();
		thinLens.setName(name);
		thinLens.setFocalLengthX(focalLengthX);
		thinLens.setFocalLengthY(focalLengthY);
		thinLens.setApertureX(aperture);
		thinLens.setPreviousSubsystem(previousSubsystem);
		// CHECK thinLens.setSubsystemType(defaultType());
		return thinLens;
	}

	/**
	 * Returns an existing {@link Corrector} identified by its name, or creates
	 * a new one. The returned entity has its fields set to the specified
	 * values.
	 *
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Corrector} with the specified field values.
	 */
	public Corrector getCorrector(String name, boolean insideNext, double fx, double fy, Double fmax, boolean electric,
			Double c1, Double c2, Double aperture, Integer previousSubsystem) {
		Corrector corrector = new Corrector();
		corrector.setName(name);
		corrector.setInsideNext(insideNext);
		corrector.setHorizontalField(fx);
		corrector.setVerticalField(fy);
		corrector.setMaximumField(fmax);
		corrector.setElectric(electric);
		corrector.setCoef1(c1);
		corrector.setCoef2(c2);
		corrector.setPreviousSubsystem(previousSubsystem);
		// CHECK corrector.setSubsystemType(defaultType());
		corrector.setApertureX(aperture);
		return corrector;
	}

	/**
	 * Returns an existing {@link BPM} identified by its name, or creates a new
	 * one. The returned entity has its fields set to the specified values.
	 *
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link BPM} with the specified field values.
	 */
	public BPM getBPM(String name, Integer previousSubsystem) {
		BPM bpm = new BPM();
		bpm.setName(name);
		bpm.setPreviousSubsystem(previousSubsystem);
		// CHECK bpm.setSubsystemType(defaultType());
		return bpm;
	}

	/**
	 * Returns an existing slot identified by its name, or creates a new one.
	 * The returned entity has its fields set to the specified values. Slots are
	 * not virtual subsystems.
	 *
	 * @param name
	 *            {@link String} name of the slot.
	 * @param description
	 *            {@link String} description of the slot.
	 * @return {@link Subsystem} slot with the specified field values.
	 */
	public Subsystem getSlot(String name, String description, Integer previousSubsystem) {
		Subsystem slot = new Subsystem();
		slot.setName(name);
		slot.setPreviousSubsystem(previousSubsystem);
		slot.setDescription(description);
		slot.setVirtual(false);
		// CHECK Subsystem slotslot.setSubsystemType(defaultType());
		return slot;
	}

	/**
	 * Returns an existing beam line identified by its name, or creates a new
	 * one. The returned entity has its fields set to the specified values. Beam
	 * lines are virtual subsystems.
	 *
	 * @param name
	 *            {@link String} name of the beamline.
	 * @param description
	 *            {@link String} description of the beamline.
	 * @return {@link Subsystem} beam line with the specified field values.
	 */
	public Subsystem getBeamline(String name, String description, Integer previousSubsystem) {
		Subsystem beamline = new Subsystem();
		beamline.setName(name);
		beamline.setPreviousSubsystem(previousSubsystem);
		beamline.setDescription(description);
		beamline.setVirtual(true);
		// CHECK beamline.setSubsystemType(defaultType());
		return beamline;
	}

	/**
	 * Returns an existing {@link Marker} identified by its name, or creates a
	 * new one. The returned entity has its fields set to the specified values.
	 *
	 * @param name
	 *            {@link String} name of the marker.
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return {@link Marker} with the specified field values.
	 */
	public Marker getMarker(String name, Integer previousSubsystem) {
		Marker marker = new Marker();
		marker.setName(name);
		marker.setPreviousSubsystem(previousSubsystem);
		// CHECK marker.setSubsystemType(defaultType());
		return marker;
	}

	/**
	 * Returns an existing {@link ElectrostaticBend} identified by its name, or
	 * creates a new one. The returned entity has its fields set to the
	 * specified values.
	 *
	 * @param name
	 *            {@link String} name of the electrostatic bend.
	 * @param bendAngle
	 *            <code>double</code> bend angle in the rotation plane (deg)
	 * @param curvatureRadius
	 *            <code>double</code> curvature radius of central trajectory
	 *            (mm)
	 * @param bendType
	 *            {@link ElectrostaticBend.BendType} CYLINDRICAL, SPHERICAL or
	 *            TOROIDAL
	 * @param aperture
	 *            <code>double</code> aperture (mm)
	 * @param horizVert
	 *            <code>false</code> horizontal, <code>true</code> vertical
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return
	 */
	public ElectrostaticBend getElectrostaticBend(String name, double bendAngle, double curvatureRadius,
			ElectrostaticBend.BendType bendType, double aperture, boolean horizVert, Integer previousSubsystem) {
		ElectrostaticBend bendEle = new ElectrostaticBend();
		bendEle.setName(name);
		bendEle.setBendAngle(bendAngle);
		bendEle.setCurvatureRadius(curvatureRadius);
		bendEle.setBendType(bendType);
		bendEle.setApertureX(aperture);
		bendEle.setVertical(horizVert);
		bendEle.setPreviousSubsystem(previousSubsystem);
		// CHECK bendEle.setSubsystemType(defaultType());
		return bendEle;
	}

	/**
	 * Returns an existing {@link SpaceChargeCompensation} identified by its
	 * name, or creates a new one. The returned entity has its fields set to the
	 * specified values.
	 *
	 * @param name
	 *            {@link String} name of the space charge compensation.
	 * @param factor
	 *            <code>double</code> beam current is compensated by a factor
	 * @param previousSubsystem
	 *            {@link Subsystem} subsystem positioned previously in the
	 *            lattice.
	 * @return
	 */
	public SpaceChargeCompensation getSpaceChargeCompensation(String name, double factor, Integer previousSubsystem) {
		SpaceChargeCompensation spaceChargeComp = new SpaceChargeCompensation();
		spaceChargeComp.setName(name);
		spaceChargeComp.setFactor(factor);
		spaceChargeComp.setPreviousSubsystem(previousSubsystem);
		// CHECK spaceChargeComp.setSubsystemType(defaultType());
		return spaceChargeComp;
	}

	public LatticeCommand getLatticeCommand(String name, String value, Integer previousSubsystem) {
		// TODO how to check for appropriate existing entries for commands?
		LatticeCommand latticeCommand = new LatticeCommand();
		latticeCommand.setName(name);
		latticeCommand.setValue(value);
		latticeCommand.setPreviousSubsystem(previousSubsystem);
		// CHECK latticeCommand.setSubsystemType(defaultType());
		return latticeCommand;
	}

	public ValidationResult validateFields(Subsystem subsystem) {
		// Currently, the only known limitation is description length.
		if (subsystem.getDescription() != null && subsystem.getDescription().length() > 8192)
			return new ValidationResult(false, "Description field too long for subsystem " + subsystem.getName(),
					"description");
		return new ValidationResult(true, "Validation successful for " + subsystem.getName(), null);
	}

	public void setFieldmapPath(String fieldmapPath) {
		this.fieldmapPath = fieldmapPath + "/";		
	}
	
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}