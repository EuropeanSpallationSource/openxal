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

public final class ESSElementFactory {
	
	private ESSElementFactory() {};
	
	private static void addElectromagnetChannels(String name, String signal, ChannelSuite channelSuite)
	{		
		channelSuite.putChannel(Electromagnet.FIELD_RB_HANDLE, name.replace('_', ':')+":"+signal, false);
	}	
	
	private static void addRFCavityChannels(String ampChannel, String phaseChannel, ChannelSuite channelSuite)
	{
    	channelSuite.putChannel(RfCavity.CAV_AMP_SET_HANDLE, ampChannel+"Ctl", true);
    	channelSuite.putChannel(RfCavity.CAV_PHASE_SET_HANDLE, phaseChannel+"Ctl", true);
    	channelSuite.putChannel(RfCavity.CAV_AMP_AVG_HANDLE, ampChannel, false);
    	channelSuite.putChannel(RfCavity.CAV_PHASE_AVG_HANDLE, phaseChannel, false);
	}

	public static ESSBend createESSBend(String name, double alpha_deg, double k, double rho, double entry_angle, 
			double exit_angle, ApertureBucket aper, Accelerator acc, int orientation, double gap, double position) {
		double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;

		// calculations
		double len = Math.abs(rho * alpha_deg * Math.PI / 180.0);

		double B0 = k / rho * Math.signum(alpha_deg);

		final MagnetMainSupply ps = ElementFactory.createMainSupply(name + "-PS", acc);
		ESSBend bend = new ESSBend(name, (orientation == MagnetType.HORIZONTAL) ? MagnetType.HORIZONTAL : MagnetType.VERTICAL);
		bend.setMainSupplyId(ps.getId());
		addElectromagnetChannels(name, "B", bend.channelSuite());				
		bend.setPosition(position);
		bend.setLength(len);
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle);
		bend.getMagBucket().setBendAngle(alpha_deg);
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

	public static ESSFieldMap createESSFieldMap(String name, double length, double frequency, double xelmax,
			double rfphase, String fieldFile, FieldProfile fieldProfile, ApertureBucket aper, double position) {
		ESSFieldMap fm = new ESSFieldMap(name);
		fm.setPosition(position);
		fm.setFrequency(frequency);
		fm.setXelmax(xelmax);
		fm.setPhase(rfphase);
		fm.setFieldMapFile(fieldFile);
		fm.setFieldProfile(fieldProfile);
		fm.setAper(aper);

		return fm;
	}
	
	public static ESSRfCavity createESSRfCavity(String name, double length, AcceleratorNode gap, double Phis, double amplitude, 
			double betas, double frequency, double position) {
		return createESSRfCavity(name, length, new AcceleratorNode[] { gap }, Phis, amplitude, betas, frequency, position);
	}

	public static ESSRfCavity createESSRfCavity(String name, double length, AcceleratorNode[] gaps, double Phis, double amplitude, 
			double betas, double frequency, double position) {

		ESSRfCavity cavity = new ESSRfCavity(name);
		addRFCavityChannels(name + ":Amp", name + ":Phs", cavity.channelSuite());
		for (AcceleratorNode gap : gaps)
			cavity.addNode(gap);

		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(amplitude);
		cavity.getRfField().setFrequency(frequency);
		cavity.setPosition(position);
		cavity.setLength(length);

		return cavity;
	}

}
