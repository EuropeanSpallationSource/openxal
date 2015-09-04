package eu.ess.bled.devices.lattice;

/**
 * <code>BPM</code> is a definition of the Beam Position Monitor.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class BPM extends BeamlineElement {

	private static final long serialVersionUID = -7334163691096857895L;

	private Double frequency;

	public Double getFrequency() {
		return frequency;
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}
}
