package eu.ess.bled.devices.lattice;

import eu.ess.bled.devices.PowerSupply;

/**
 * <code>Magnet</code> represents a physical magnet in the lattice.
 * {@link Magnet} is connected to a particular {@link PowerSupply}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public abstract class Magnet extends BeamlineElement {

	private static final long serialVersionUID = -1293462035850992083L;


	private Double skewAngle;

	private PowerSupply powerSupply;

	public Double getSkewAngle() {
		return skewAngle;
	}

	public void setSkewAngle(Double skewAngle) {
		this.skewAngle = skewAngle;
	}

	public PowerSupply getPowerSupply() {
		return powerSupply;
	}

	public void setPowerSupply(PowerSupply powerSupply) {
		this.powerSupply = powerSupply;
	}
}
