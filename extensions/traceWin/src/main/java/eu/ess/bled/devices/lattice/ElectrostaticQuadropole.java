package eu.ess.bled.devices.lattice;

/**
 * <code>ElectrostaticQuadrupole</code> represents an electrostatic quadrupole
 * element.
 * 
 * @author <a href="mailto:luka.stopar@cosylab.com">Luka Stopar</a>
 * 
 */

public class ElectrostaticQuadropole extends BeamlineElement {

	private static final long serialVersionUID = 3443338684131520930L;

	private Double voltage;
	private Double skewAngle;
	private Double sextupoleVoltage;
	private Double octupoleVoltage;
	private Double decapoleVoltage;
	private Double dodecapoleVoltage;

	public Double getVoltage() {
		return voltage;
	}

	public void setVoltage(Double voltage) {
		this.voltage = voltage;
	}

	public Double getSkewAngle() {
		return skewAngle;
	}

	public void setSkewAngle(Double skewAngle) {
		this.skewAngle = skewAngle;
	}

	public Double getSextupoleVoltage() {
		return sextupoleVoltage;
	}

	public void setSextupoleVoltage(Double sextupoleVoltage) {
		this.sextupoleVoltage = sextupoleVoltage;
	}

	public Double getOctupoleVoltage() {
		return octupoleVoltage;
	}

	public void setOctupoleVoltage(Double octupoleVoltage) {
		this.octupoleVoltage = octupoleVoltage;
	}

	public Double getDecapoleVoltage() {
		return decapoleVoltage;
	}

	public void setDecapoleVoltage(Double decapoleVoltage) {
		this.decapoleVoltage = decapoleVoltage;
	}

	public Double getDodecapoleVoltage() {
		return dodecapoleVoltage;
	}

	public void setDodecapoleVoltage(Double dodecapoleVoltage) {
		this.dodecapoleVoltage = dodecapoleVoltage;
	}
}
