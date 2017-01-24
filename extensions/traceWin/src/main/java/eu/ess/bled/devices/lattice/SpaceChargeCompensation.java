package eu.ess.bled.devices.lattice;

/**
 * <code>SpaceChargeCompensation</code> represents a space charge compensation.
 * 
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovic</a>
 * 
 */


public class SpaceChargeCompensation extends BeamlineElement {

	private static final long serialVersionUID = 3644917995527497340L;


	private Double factor;

	/**
	 * @return the factor
	 */
	public Double getFactor() {
		return factor;
	}

	/**
	 * @param factor
	 *            the factor to set
	 */
	public void setFactor(Double factor) {
		this.factor = factor;
	}

}
