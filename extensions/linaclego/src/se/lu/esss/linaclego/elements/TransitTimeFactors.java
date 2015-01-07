package se.lu.esss.linaclego.elements;

import se.lu.esss.linaclego.Parameters;

public class TransitTimeFactors {
	protected Parameters p;
	
	public TransitTimeFactors(Parameters p)
	{
		this.p = p;
	}
	
	public double getBetaS() {
		return p.getDoubleValue("betaS");
	}
	
	public double getTs() {
		return p.getDoubleValue("tts");
	}
	
	public double getKTs() {
		return p.getDoubleValue("ktts");
	}

	public double getK2Ts() {
		return p.getDoubleValue("k2tts");
	}
	
	public double getKS() {
		return p.getDoubleValue("ks");
	}
		
	public double getK2S() {
		return p.getDoubleValue("k2s");
	}
}
