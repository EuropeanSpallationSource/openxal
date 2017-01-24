package eu.ess.bled.devices.lattice;

/**
 * A <code>Solenoid</code> <code>Magnet</code>. Basically a <code>Magnet</code>
 * with field magneticField.
 * 
 * @author <a href="mailto:luka.stopar@cosylab.com">Luka Stopar</a>
 * 
 */

public class Solenoid extends Magnet {

	private static final long serialVersionUID = 4682619669847022420L;


	private Double magneticField;

	public Double getMagneticField() {
		return magneticField;
	}

	public void setMagneticField(Double magneticField) {
		this.magneticField = magneticField;
	}
}
