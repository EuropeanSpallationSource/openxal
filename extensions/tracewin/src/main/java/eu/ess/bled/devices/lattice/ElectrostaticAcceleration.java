package eu.ess.bled.devices.lattice;

/**
 * <code>ElectrostaticAcceleration</code> represents an electrostatic
 * acceleration element.
 * 
 * @author <a href="mailto:luka.stopar@cosylab.com">Luka Stopar</a>
 * 
 */
public class ElectrostaticAcceleration extends BeamlineElement {

	private static final long serialVersionUID = -8713323826041131036L;

	private Double voltage;
	private Double defocal;

	public Double getVoltage() {
		return voltage;
	}

	public void setVoltage(Double voltage) {
		this.voltage = voltage;
	}

	public Double getDefocal() {
		return defocal;
	}

	public void setDefocal(Double defocal) {
		this.defocal = defocal;
	}
}
