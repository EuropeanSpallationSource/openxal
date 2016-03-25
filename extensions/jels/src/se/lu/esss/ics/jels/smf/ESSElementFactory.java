package se.lu.esss.ics.jels.smf;

import se.lu.esss.ics.jels.smf.impl.ESSBend;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.ics.jels.smf.impl.ESSRfGap;
import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import xal.smf.AcceleratorNode;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Electromagnet;
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
	 * Default values for upstream and downstream edge face Fringe-field factor are used.
	 * @param name Name of the bend magnets.
	 * @param alpha Bend angle in degrees.
	 * @param k beta*gamma*Er/(e0*c).
	 * @param rho Curvature radius in meter.
	 * @param entry_angle Entry angle in degrees.
	 * @param exit_angle Exit angle in degrees.
	 * @param quadComp Quadrupole component error of the dipole.
	 * @param aper Aperture details.
	 * @param ps Power supply for magnet. Can be null.
	 * @param position Position of the magnet in the accelerator.
	 * @return Bend object.
	 */
	public static ESSBend createESSBend(String name, double alpha, double k, double rho, double entry_angle, 
			double exit_angle, double quadComp, ApertureBucket aper, MagnetMainSupply ps, int orientation, double gap, double position) {

		return createESSBend(name, alpha, k, rho, entry_angle, exit_angle, 0.45, 2.8, 0.45, 2.8, quadComp, aper, ps, orientation, 
				gap, position);
	}

	/**
	 * Creates the Bend node with specified properties.
	 * @param name Name of the bend magnets.
	 * @param alpha Bend angle in degrees.
	 * @param k beta*gamma*Er/(e0*c).
	 * @param rho Curvature radius in meter.
	 * @param entry_angle Entry angle in degrees.
	 * @param exit_angle Exit angle in degrees.
	 * @param enterK1 First upstream edge face Fringe-field factor.
	 * @param enterK2 Second upstream edge face Fringe-field factor.
	 * @param exitK1 First downstream edge face Fringe-field factor.
	 * @param exitK2 Second downstream edge face Fringe-field factor.
	 * @param quadComp Quadrupole component error of the dipole.
	 * @param aper Aperture details.
	 * @param ps Power supply for magnet. Can be null.
	 * @param position Position of the magnet in the accelerator.
	 * @return Bend object.
	 */
	public static ESSBend createESSBend(String name, double alpha, double k, double rho, double entry_angle, 
			double exit_angle, double enterK1, double enterK2, double exitK1, double exitK2, double quadComp,
			ApertureBucket aper, MagnetMainSupply ps, int orientation, double gap, double position) {

		// calculations
		double len = Math.abs(rho * alpha * Math.PI / 180.0);

		double B0 = k / rho * Math.signum(alpha);

		ESSBend bend = new ESSBend(name, (orientation == MagnetType.HORIZONTAL) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL);
		if (ps != null) {
			bend.setMainSupplyId(ps.getId());
		}
		addElectromagnetChannels(name, "B", bend.channelSuite());				
		bend.setPosition(position);
		bend.setLength(len);
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle);
		bend.getMagBucket().setBendAngle(alpha);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle);
		bend.setDfltField(B0);
		bend.getMagBucket().setDipoleQuadComponent(quadComp);

		bend.setGap(gap);
		bend.setEntrK1(enterK1);
		bend.setEntrK2(enterK2);
		bend.setExitK1(exitK1);
		bend.setExitK2(exitK2);

		bend.setAper(aper);

		return bend;
	}
	
	/**
	 * Creates the RfGap node with specified properties.
	 * @param name Name of the RF gap.
	 * @param isFirst Is this the first gap in cavity.
	 * @param ampFactor Amplification factor.
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
	 * @param frequency Frequency at the start of the element.
	 * @param xelmax Electric field intensity factor.
	 * @param rfphase RF phase.
	 * @param fieldFile File containing the filed profile.
	 * @param fieldProfile Field profile.
	 * @param aper Aperture details.
	 * @param position Position of the field map.
	 * @return ESSFieldMap object.
	 */
	public static ESSRfCavity createESSFieldMap(String name, double length, double frequency, double xelmax,
			double rfphase, String fieldFile, FieldProfile fieldProfile, ApertureBucket aper, double position) {
		ESSFieldMap fm = new ESSFieldMap(name+":FM");
		fm.setLength(length);
		fm.setPosition(length/2.);
		fm.setFieldMapFile(fieldFile);
		fm.setFieldProfile(fieldProfile);
		fm.setXelmax(xelmax);
		fm.setAper(aper);
		fm.setFrequency(frequency);
		fm.setPhase(rfphase);

		double amplitude =  (fieldProfile.getE0L(frequency)/fieldProfile.getLength()) * 1e-6;

		ESSRfCavity cavity = createESSRfCavity(name, length, new AcceleratorNode[] { fm }, rfphase, amplitude, frequency, position - length/2.);
		return cavity;
	}
	
	/**
	 * Creates the RfCavity node with specified properties. TTF and STF coefficients are not overwritten.
	 * @param name Name of the RF cavity.
	 * @param length Length of the cavity in meters.
	 * @param node Node to include in the cavity.
	 * @param Phis Phase.
	 * @param amplitude Amplitude.
	 * @param frequency Frequency at the start of the element.
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
	 * @param Phis Phase.
	 * @param amplitude Amplitude.
	 * @param frequency Frequency at the start of the element.
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
