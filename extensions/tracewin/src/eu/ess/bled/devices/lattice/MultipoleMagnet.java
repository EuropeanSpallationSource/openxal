package eu.ess.bled.devices.lattice;

/**
 * 
 * <code>MultipoleMagnet</code> represents multi pole magnets in the beamline.
 * It contains the strength and multipole gradients of a single magnet.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */

public class MultipoleMagnet extends Magnet {

	private static final long serialVersionUID = -2303390614527808908L;

	/**
	 * 
	 * <code>MagnetType</code> describes different types of multipoles.
	 * 
	 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
	 * 
	 */
	public static enum MagnetType {
		SOLENOID(0), DIPOLE(1), QUADRUPOLE(2), SEXTUPOLE(3), OCTUPOLE(4), DECAPOLE(5), DODECAPOLE(6);

		private int val;

		MagnetType(int val) {
			this.val = val;
		}

		/**
		 * Returns the type that corresponds to the given int value.
		 * 
		 * @param val
		 *            the requested value
		 * @return the enum type
		 */
		public static MagnetType toEnum(int val) {
			return values()[val];
		}

		/**
		 * Returns the integer representation of this type.
		 * 
		 * @return the integer value
		 */
		public int getIntegerValue() {
			return val;
		}
	}


	private Double dipoleStrength;

	private Double quadrupoleGradient;

	private Double sextupoleGradient;

	private Double octupoleGradient;

	private Double decapoleGradient;

	private Double dodecapoleGradient;

	private MagnetType magnetType;

	private Integer steps;

	private Double solenoidLength;

	private Integer solenoidSteps;

	public Double getDipoleStrength() {
		return dipoleStrength;
	}

	public void setDipoleStrength(Double dipoleStrength) {
		this.dipoleStrength = dipoleStrength;
	}

	public Double getQuadrupoleGradient() {
		return quadrupoleGradient;
	}

	public void setQuadrupoleGradient(Double quadrupoleGradient) {
		this.quadrupoleGradient = quadrupoleGradient;
	}

	public Double getSextupoleGradient() {
		return sextupoleGradient;
	}

	public void setSextupoleGradient(Double sextupoleGradient) {
		this.sextupoleGradient = sextupoleGradient;
	}

	public Double getOctupoleGradient() {
		return octupoleGradient;
	}

	public void setOctupoleGradient(Double octupoleGradient) {
		this.octupoleGradient = octupoleGradient;
	}

	public Double getDecapoleGradient() {
		return decapoleGradient;
	}

	public void setDecapoleGradient(Double decapoleGradient) {
		this.decapoleGradient = decapoleGradient;
	}

	public Double getDodecapoleGradient() {
		return dodecapoleGradient;
	}

	public void setDodecapoleGradient(Double dodecapoleGradient) {
		this.dodecapoleGradient = dodecapoleGradient;
	}

	public MagnetType getMagnetType() {
		return magnetType;
	}

	public void setMagnetType(MagnetType magnetType) {
		this.magnetType = magnetType;
	}

	public Integer getSteps() {
		return steps;
	}

	public void setSteps(Integer steps) {
		this.steps = steps;
	}

	public Double getSolenoidLength() {
		return solenoidLength;
	}

	public void setSolenoidLength(Double solenoidLength) {
		this.solenoidLength = solenoidLength;
	}

	public Integer getSolenoidSteps() {
		return solenoidSteps;
	}

	public void setSolenoidSteps(Integer solenoidSteps) {
		this.solenoidSteps = solenoidSteps;
	}

}
