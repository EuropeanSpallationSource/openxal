package xal.smf.impl;

import xal.smf.Accelerator;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.qualify.MagnetType;

public class ElementFactory {
	
	private ElementFactory() {};

	public static Marker createMarker(String name, double position) {
		Marker marker = new Marker(name);
		marker.setPosition(position);
		return marker;
	}

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

	public static BPM createBPM(String name, double frequency, double position) {
		BPM bpm = new BPM(name);
		addBPMChannels(bpm.getId(), bpm.channelSuite());
		bpm.getBPMBucket().setFrequency(frequency);
		bpm.getBPMBucket().setLength(1.0);
		bpm.getBPMBucket().setOrientation(1);
		bpm.setPosition(position);
		return bpm;
	}
	
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

	private static void addPowersupplyChannels(String name, ChannelSuite channelSuite) {
		channelSuite.putChannel(MagnetPowerSupply.CURRENT_RB_HANDLE, name + ":CurRB", false);
		channelSuite.putChannel(MagnetPowerSupply.CURRENT_SET_HANDLE, name + ":CurSet", true);
		channelSuite.putChannel(MagnetMainSupply.FIELD_RB_HANDLE, name + ":FldRB", false);
		channelSuite.putChannel(MagnetMainSupply.FIELD_SET_HANDLE, name + ":FldSet", true);
		channelSuite.putChannel(MagnetPowerSupply.CYCLE_STATE_HANDLE, name + ":CycSt", false);
		channelSuite.putChannel(MagnetMainSupply.CYCLE_ENABLE_HANDLE, name + ":CycEn", true);			
	}
	
	public static MagnetMainSupply createMainSupply(String name, Accelerator acc) {
		MagnetMainSupply supply = new MagnetMainSupply(acc);
		supply.strId = name;
		addPowersupplyChannels(name, supply.getChannelSuite());
		acc.putMagnetMainSupply(supply);
		return supply;
	}

	public static Quadrupole createQuadrupole(String name, double length, double gradient,
			ApertureBucket aper, Accelerator acc, double position) {
		Quadrupole quad = new Quadrupole(name);
		quad._type = "Q";
		addElectromagnetChannels(name, "B", quad.channelSuite());				
		MagnetMainSupply ps = createMainSupply(name + "-PS", acc);
		quad.setMainSupplyId(ps.getId());

		quad.setPosition(position + length * 0.5); // always position on center!
		quad.setLength(length);
		quad.getMagBucket().setEffLength(length);

		quad.setDfltField(gradient);
		quad.getMagBucket().setPolarity(1);
		quad.setAper(aper);

		return quad;
	}

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

	public static Bend createBend(String name, double alpha_deg, double k, double rho, double entry_angle, 
			double exit_angle, ApertureBucket aper, Accelerator acc, double position) {

		double len = Math.abs(rho * alpha_deg * Math.PI / 180.0);

		double B0 = k / rho * Math.signum(alpha_deg);

		Bend bend = new Bend(name);
		addElectromagnetChannels(name, "B", bend.channelSuite());				
		MagnetMainSupply ps = createMainSupply(name + "-PS", acc);
		bend.setMainSupplyId(ps.getId());
		bend.setPosition(position + len * 0.5); // always position on center!
		bend.setLength(len);
		bend.getMagBucket().setPathLength(len);

		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle);
		bend.getMagBucket().setBendAngle(alpha_deg);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle);
		bend.setDfltField(B0);
		bend.getMagBucket().setDipoleQuadComponent(0);
		bend.setAper(aper);

		return bend;
	}
	
	public static RfGap createRfGap(String name, boolean isFirst, 
			double ampFactor, ApertureBucket aper, double length) {
		final RfGap gap = new RfGap(name);
		gap.setFirstGap(isFirst);
		gap.getRfGap().setEndCell(0);
		gap.setLength(length);
		gap.getRfGap().setAmpFactor(ampFactor);
		gap.getRfGap().setTTF(1.0);
		gap.setAper(aper);
		return gap;
	}
	
	public static RfCavity createRfCavity(String name, RfGap gap, double Phis, double amplitude, double betas,
			double frequency, double[] TTFCoefs, double[] STFCoefs, double position) {
		RfCavity cavity = new RfCavity(name);
		addRFCavityChannels(name + ":Amp", name + ":Phs", cavity.channelSuite());
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(amplitude);
		cavity.getRfField().setFrequency(frequency);
		cavity.setPosition(position);
		cavity.setLength(0.0);
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		} else {
			cavity.getRfField().setTTFCoefs(TTFCoefs);
			cavity.getRfField().setTTF_endCoefs(TTFCoefs);
			cavity.getRfField().setSTFCoefs(STFCoefs);
			cavity.getRfField().setSTF_endCoefs(STFCoefs);
		}
		return cavity;
	}
}
