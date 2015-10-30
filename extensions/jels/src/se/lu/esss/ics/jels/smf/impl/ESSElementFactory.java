package se.lu.esss.ics.jels.smf.impl;

import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.ElementFactory;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.RfCavity;
import xal.smf.impl.qualify.MagnetType;

/**
 * Factory class for creation of ESS specific AcceleratorNode elements, primarly designed to be used by
 * importers from external accelerator formats to openxal.
 * @author Blaz Kranjc
 */
public final class ESSElementFactory {
	
	/* This class should not be instanced. */
	private ESSElementFactory() {};
	
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
	public static ESSBend createESSBend(String name, double alpha, double k, double rho, double entry_angle, 
			double exit_angle, ApertureBucket aper, Accelerator acc, int orientation, double gap, double position) {
		double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;

		// calculations
		double len = Math.abs(rho * alpha * Math.PI / 180.0);

		double B0 = k / rho * Math.signum(alpha);

		final MagnetMainSupply ps = ElementFactory.createMainSupply(name + "-PS", acc);
		ESSBend bend = new ESSBend(name, (orientation == MagnetType.HORIZONTAL) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL);
		bend.setMainSupplyId(ps.getId());
		addElectromagnetChannels(name, "B", bend.channelSuite());				
		bend.setPosition(position);
		bend.setLength(len);
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle);
		bend.getMagBucket().setBendAngle(alpha);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle);
		bend.setDfltField(B0);
		bend.getMagBucket().setDipoleQuadComponent(0);

		bend.setGap(gap);
		bend.setEntrK1(entrK1);
		bend.setEntrK2(entrK2);
		bend.setExitK1(exitK1);
		bend.setExitK2(exitK2);

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
	public static ESSRfGap createESSRfGap(String name, boolean isFirst, double ampFactor, 
			ApertureBucket aper, double length, double position) {
		final ESSRfGap gap = new ESSRfGap(name);
		gap.setFirstGap(isFirst);
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0);
		gap.getRfGap().setAmpFactor(ampFactor);
		gap.getRfGap().setTTF(1.0);
		gap.getRfGap().setLength(length);
		gap.setPosition(position);
		gap.setAper(aper);
		return gap;
	}

	/**
	 * Creates the field map node with specified properties.
	 * @param name Name of the field map.
	 * @param length Length of the fieldmap.
	 * @param frequency TODO
	 * @param xelmax TODO
	 * @param rfphase TODO
	 * @param fieldFile File containing the filed profile.
	 * @param fieldProfile Field profile.
	 * @param aper Aperture details.
	 * @param position Position of the field map.
	 * @return ESSFieldMap object.
	 */
	public static ESSFieldMap createESSFieldMap(String name, double length, double frequency, double xelmax,
			double rfphase, String fieldFile, FieldProfile fieldProfile, ApertureBucket aper, double position) {
		ESSFieldMap fm = new ESSFieldMap(name);
		fm.setLength(length);
		fm.setPosition(position);
		fm.setFrequency(frequency);
		fm.setXelmax(xelmax);
		fm.setPhase(rfphase);
		fm.setFieldMapFile(fieldFile);
		fm.setFieldProfile(fieldProfile);
		fm.setAper(aper);

		return fm;
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
	public static ESSRfCavity createESSRfCavity(String name, double length, AcceleratorNode node, double Phis, double amplitude, 
			double frequency, double position) {
		return createESSRfCavity(name, length, new AcceleratorNode[] { node }, Phis, amplitude, frequency, position);
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
	public static ESSRfCavity createESSRfCavity(String name, double length, AcceleratorNode[] nodes, double Phis, double amplitude, 
			double frequency, double position) {

		ESSRfCavity cavity = new ESSRfCavity(name);
		addRFCavityChannels(name + ":Amp", name + ":Phs", cavity.channelSuite());
		for (AcceleratorNode gap : nodes)
			cavity.addNode(gap);

		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(amplitude);
		cavity.getRfField().setFrequency(frequency);
		cavity.setPosition(position);
		cavity.setLength(length);

		return cavity;
	}

}
