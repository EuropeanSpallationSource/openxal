package eu.ess.bled.devices.lattice;

/**
 * <code>ElectrostaticBend</code> represents an electrostatic bend element.
 * 
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovic</a>
 * 
 */
// TODO Check persistence annotations
public class ElectrostaticBend extends BeamlineElement {

	private static final long serialVersionUID = -8428134678904715521L;

	/**
	 * <code>BendType</code> describes a type of electrostatic bend.
	 * 
	 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovic</a>
	 */
	public enum BendType {
		CYLINDRICAL(1), SPHERICAL(2), TOROIDAL(3);

		private final int val;

		BendType(int val) {
			this.val = val;
		}

		/**
		 * Returns the integer representation of this type.
		 * 
		 * @return the integer value
		 */
		public int getIntegerValue() {
			return val;
		}

		/**
		 * Maps integer value to enum value.
		 * 
		 * @param val <code>int</code> 1, 2 or 3
		 * @return the <code>enum</code> constant associated with the value or
		 *         <code>null</code> if such a value does not exist.
		 */
		public static BendType toEnum(int val) {
			try {
				return values()[val - 1];
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
	}

	private Double bendAngle;
	private Double curvatureRadius;
	private BendType bendType;
	private Boolean isVertical;

	public Double getBendAngle() {
		return bendAngle;
	}

	public void setBendAngle(Double bendAngle) {
		this.bendAngle = bendAngle;
	}

	public Double getCurvatureRadius() {
		return curvatureRadius;
	}

	public void setCurvatureRadius(Double curvatureRadius) {
		this.curvatureRadius = curvatureRadius;
	}

	public BendType getBendType() {
		return bendType;
	}

	public void setBendType(BendType bendType) {
		this.bendType = bendType;
	}

	/**
	 * Returns <code>true</code> if the element is vertical. <code>false</code>
	 * if the element is horizontal.
	 * 
	 * @return the orientation of the element.
	 */
	public Boolean isVertical() {
		return isVertical;
	}

	/**
	 * Set to <code>true</code> if the element is vertical. <code>false</code>
	 * if the element is horizontal.
	 *  
	 * @param isVertical is element vertical?
	 */
	public void setVertical(Boolean isVertical) {
		this.isVertical = isVertical;
	}
}
