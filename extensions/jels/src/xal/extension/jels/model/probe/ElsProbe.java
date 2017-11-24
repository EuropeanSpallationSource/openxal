package xal.extension.jels.model.probe;

import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss;
import xal.tools.math.GenericMatrix;


/**
 * This is a probe ported from ELS implementation.
 * State of the beam is kept in 9-vector called envelope. Given by (beta_x,alpha_x,gamma_x, .._y, .._z).
 * Normalized emmitance is also kept in 3-vector.
 * 
 * @author Emanuele Laface, Ivo List <ivo.list@cosylab.com>
 */
public class ElsProbe extends EnvelopeProbe {
	private GenericMatrix envelope = new GenericMatrix(9,1);
	private GenericMatrix normalized_emmitance = new GenericMatrix(3,1);	
	
	private GenericMatrix envelope0;
	private GenericMatrix normalized_emmitance0;
	
    @Override
	public EnvelopeProbeState createProbeState() {
		EnvelopeProbeState ps = new EnvelopeProbeState(this);
		ps.setCovariance(getCovariance());
		return ps;
	}

	public void initFromTwiss(Twiss[] twiss)
	{
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta()/Math.pow(getGamma(), 2), twiss[2].getEmittance());
		for (int i = 0; i<3; i++) {
			envelope.setElem(3*i+0,0,twiss[i].getBeta());
			envelope.setElem(3*i+1,0,twiss[i].getAlpha());
			envelope.setElem(3*i+2,0,twiss[i].getGamma());
			normalized_emmitance.setElem(i,0,twiss[i].getEmittance()*getBeta()*getGamma());
		}		
		envelope0 = envelope.copy();
		normalized_emmitance0 = normalized_emmitance.copy();
	}
	
	public GenericMatrix getEnvelope() {
		return envelope;
	}

	public void setEnvelope(GenericMatrix envelope) {
		this.envelope = envelope;
	}
	
	public Twiss[] getTwiss()
	{
		Twiss[] twiss = new Twiss[3];
		for (int i=0; i<3; i++)
			twiss[i] = new Twiss(envelope.getElem(3*i+1,0), envelope.getElem(3*i,0), normalized_emmitance.getElem(i,0)/(getBeta()*getGamma()));
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta()*Math.pow(getGamma(), 2), twiss[2].getEmittance());
		return twiss;
	}

	@Override
	public CovarianceMatrix getCovariance() {
		Twiss[] twiss = getTwiss();
		return CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2]);		
	}
	
	public void setCorrelation(CovarianceMatrix matTau) {
		initFromTwiss(matTau.computeTwiss());
	};

	
	@Override
	public void reset()
	{
		super.reset();
		envelope = envelope0.copy();
		normalized_emmitance = normalized_emmitance0.copy();
	}
}
