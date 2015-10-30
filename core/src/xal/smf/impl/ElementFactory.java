package xal.smf.impl;

import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.qualify.MagnetType;

/**
 * Factory class for creation of AcceleratorNode elements, primarly designed to be used by
 * importers from external accelerator formats to openxal.
 * 
 * @author Blaz Kranjc
 */
public class ElementFactory {
	
	/* This class should not be instanced. */
	private ElementFactory() {};

	/**
	 * Add channels for a BPM to the channelSuite.
	 * @param name BPM name.
	 * @param channelSuite Target channelSuite.
	 */
	private static void addBPMChannels(String name, ChannelSuite channelSuite)
	{
		name = name.replace('_', ':');
		channelSuite.putChannel(BPM.X_AVG_HANDLE, name+":XAvg", false);
    	channelSuite.putChannel(BPM.Y_AVG_HANDLE, name+":YAvg", false);
    	channelSuite.putChannel(BPM.X_TBT_HANDLE, name+":XTBT", false);
    	channelSuite.putChannel(BPM.Y_TBT_HANDLE, name+":YTBT", false);
    	channelSuite.putChannel(BPM.PHASE_AVG_HANDLE, name+":PhsAvg", false);
    	channelSuite.putChannel(BPM.AMP_AVG_HANDLE, name+":AmpAvg", false);
    	channelSuite.putChannel(BPM.AMP_TBT_HANDLE, name+":AmpTBT", false);
    	channelSuite.putChannel(BPM.PHASE_TBT_HANDLE, name+":PhsTBT", false);
	}

	/**
	 * Add channels for electromagnet to the channelSuite.
	 * @param name Name of the magnet.
	 * @param signal Signal name added to magnet name.
	 * @param channelSuite Target channelSuite.
	 */
	private static void addElectromagnetChannels(String name, String signal, ChannelSuite channelSuite)
	{		
		channelSuite.putChannel(Electromagnet.FIELD_RB_HANDLE, name.replace('_', ':')+":"+signal, false);
	}	
	
	/**
	 * Add channels for a RF cavity to the channelSuite.
	 * @param ampChannel Name of the amplitude channel.
	 * @param phaseChannel Name of the phase channel.
	 * @param channelSuite Target channelSuite.
	 */
	private static void addRFCavityChannels(String ampChannel, String phaseChannel, ChannelSuite channelSuite)
	{
    	channelSuite.putChannel(RfCavity.CAV_AMP_SET_HANDLE, ampChannel+"Ctl", true);
    	channelSuite.putChannel(RfCavity.CAV_PHASE_SET_HANDLE, phaseChannel+"Ctl", true);
    	channelSuite.putChannel(RfCavity.CAV_AMP_AVG_HANDLE, ampChannel, false);
    	channelSuite.putChannel(RfCavity.CAV_PHASE_AVG_HANDLE, phaseChannel, false);
	}

	/**
	 * Add channels for the power supply to the channelSuite.
	 * @param name Name of the power supply.
	 * @param channelSuite Target channelSuite.
	 */
	private static void addPowersupplyChannels(String name, ChannelSuite channelSuite) {
		channelSuite.putChannel(MagnetPowerSupply.CURRENT_RB_HANDLE, name + ":CurRB", false);
		channelSuite.putChannel(MagnetPowerSupply.CURRENT_SET_HANDLE, name + ":CurSet", true);
		channelSuite.putChannel(MagnetMainSupply.FIELD_RB_HANDLE, name + ":FldRB", false);
		channelSuite.putChannel(MagnetMainSupply.FIELD_SET_HANDLE, name + ":FldSet", true);
		channelSuite.putChannel(MagnetPowerSupply.CYCLE_STATE_HANDLE, name + ":CycSt", false);
		channelSuite.putChannel(MagnetMainSupply.CYCLE_ENABLE_HANDLE, name + ":CycEn", true);			
	}
	
	/**
	 * Creates a power supply and adds it to the accelerator.
	 * @param name Name of the power supply.
	 * @param acc Accelerator that will contains the power supply.
	 * @return Power supply object.
	 */
	public static MagnetMainSupply createMainSupply(String name, Accelerator acc) {
		MagnetMainSupply supply = new MagnetMainSupply(acc);
		supply.strId = name;
		addPowersupplyChannels(name, supply.getChannelSuite());
		acc.putMagnetMainSupply(supply);
		return supply;
	}

	/**
	 * Creates the Marker node with specified properties.
	 * @param name Name of the marker.
	 * @param position Position of the marker in the accelerator.
	 * @return Marker object.
	 */
	public static Marker createMarker(String name, double position) {
		Marker marker = new Marker(name);
		marker.setPosition(position);
		return marker;
	}

	/**
	 * Creates the BPM node with specified properties.
	 * @param name Name of the BPM.
	 * @param frequency TODO.
	 * @param length Length of the BPM in meters. 
	 * @param position Position of BPM.
	 * @return BPM object.
	 */
	public static BPM createBPM(String name, double frequency, double length, double position) {
		BPM bpm = new BPM(name);
		addBPMChannels(bpm.getId(), bpm.channelSuite());
		bpm.getBPMBucket().setFrequency(frequency);
		bpm.getBPMBucket().setLength(length);
		bpm.getBPMBucket().setOrientation(1);
		bpm.setPosition(position);
		return bpm;
	}

	/**
	 * Creates the Quadrupole node with specified properties.
	 * @param name Name of the Quadrupole.
	 * @param length Length of the Quadrupole in meters.
	 * @param gradient TODO
	 * @param aper Aperture details.
	 * @param acc Accelerator containing the Quadrupole.
	 * @param position Position of Quadrupole.
	 * @return Quadrupole object.
	 */
	public static Quadrupole createQuadrupole(String name, double length, double gradient,
			ApertureBucket aper, Accelerator acc, double position) {
		Quadrupole quad = new Quadrupole(name);
		quad._type = "Q";
		addElectromagnetChannels(name, "B", quad.channelSuite());				
		MagnetMainSupply ps = createMainSupply(name + "-PS", acc);
		quad.setMainSupplyId(ps.getId());

		quad.setPosition(position);
		quad.setLength(length);
		quad.getMagBucket().setEffLength(length);

		quad.setDfltField(gradient);
		quad.getMagBucket().setPolarity(1);
		quad.setAper(aper);

		return quad;
	}

	/**
	 * Creates the DipoleCorrector node with specified properties.
	 * @param name Name of the corrector.
	 * @param orientation Orientation of the corrector.
	 * @param length Length of the corrector in meters.
	 * @param aper Aperture details.
	 * @param acc Accelerator that contains the corrector.
	 * @param position Position of the corrector in the accelerator.
	 * @return Dipole correctors.
	 */
	public static DipoleCorr createCorrector(String name, int orientation, double length, 
			ApertureBucket aper, Accelerator acc, double position) {
		DipoleCorr corr = (orientation == MagnetType.HORIZONTAL) ? 
				new HDipoleCorr(name) : new VDipoleCorr(name);
		MagnetMainSupply ps = createMainSupply(name + "-PS", acc);
		corr.setMainSupplyId(ps.getId());
		addElectromagnetChannels(name, "B",  corr.channelSuite());
		corr.setPosition(position);
		corr.setLength(length);
		corr.getMagBucket().setEffLength(length);
		corr.setAper(aper);
		return corr;
	}

	/**
	 * Creates the Bend node with specified properties.
	 * @param name Name of the bend magnets.
	 * @param alpha Bend angle in degrees.
	 * @param k TODO
	 * @param rho TODO
	 * @param entry_angle Entry angle in degrees.
	 * @param exit_angle Exit angle in degrees.
	 * @param aper Aperture details.
	 * @param acc Accelerator that contains the magnet. 
	 * @param position Position of the magnet in the accelerator.
	 * @return Bend object.
	 */
	public static Bend createBend(String name, double alpha, double k, double rho, double entry_angle, 
			double exit_angle, ApertureBucket aper, Accelerator acc, double position) {

		double len = Math.abs(rho * alpha * Math.PI / 180.0);

		double B0 = k / rho * Math.signum(alpha);

		Bend bend = new Bend(name);
		addElectromagnetChannels(name, "B", bend.channelSuite());				
		MagnetMainSupply ps = createMainSupply(name + "-PS", acc);
		bend.setMainSupplyId(ps.getId());
		bend.setPosition(position);
		bend.setLength(len);
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle);
		bend.getMagBucket().setBendAngle(alpha);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle);
		bend.setDfltField(B0);
		bend.getMagBucket().setDipoleQuadComponent(0);
		bend.setAper(aper);

		return bend;
	}
	
	/**
	 * Creates the RfGap node with specified properties.
	 * @param name Name of the RF gap.
	 * @param isFirst Is this the first gap in cavity.
	 * @param ampFactor TODO
	 * @param aper Aperture details.
	 * @param length Length of gap in meters.
	 * @param position Position of RF gap.
	 * @return RfGap object.
	 */
	public static RfGap createRfGap(String name, boolean isFirst, double ampFactor,
			ApertureBucket aper, double length, double position) {
		final RfGap gap = new RfGap(name);
		gap.setFirstGap(isFirst);
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0);
		gap.setPosition(position);
		gap.getRfGap().setLength(length);
		gap.getRfGap().setAmpFactor(ampFactor);
		gap.getRfGap().setTTF(1.0);
		gap.setAper(aper);
		return gap;
	}
	
	/**
	 * Creates the RfCavity node with specified properties. TTF and STF coefficients are not overwritten.
	 * @param name Name of the RF cavity.
	 * @param length Length of the cavity in meters.
	 * @param node Node to include in the cavity.
	 * @param Phis TODO
	 * @param amplitude TODO
	 * @param frequency TODO
	 * @param position Position of the cavity.
	 * @return RfCavity object.
	 */
	public static RfCavity createRfCavity(String name, double length, AcceleratorNode node, double Phis, double amplitude, 
			double frequency, double position) {
		return createRfCavity(name, length, new AcceleratorNode[] {node}, Phis, amplitude, frequency, position);
	}

	/**
	 * Creates the RfCavity node with specified properties. TTF and STF coefficients are not overwritten.
	 * @param name Name of the RF cavity.
	 * @param length Length of the cavity in meters.
	 * @param nodes Nodes to include in the cavity.
	 * @param Phis TODO
	 * @param amplitude TODO
	 * @param frequency TODO
	 * @param position Position of the cavity.
	 * @return RfCavity object.
	 */
	public static RfCavity createRfCavity(String name, double length, AcceleratorNode[] nodes, double Phis, double amplitude,
			double frequency, double position) {
		RfCavity cavity = new RfCavity(name);
		addRFCavityChannels(name + ":Amp", name + ":Phs", cavity.channelSuite());
		for(AcceleratorNode gap : nodes)
			cavity.addNode(gap);

		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(amplitude);
		cavity.getRfField().setFrequency(frequency);
		cavity.setPosition(position);
		cavity.setLength(length);
		return cavity;
	}
}
