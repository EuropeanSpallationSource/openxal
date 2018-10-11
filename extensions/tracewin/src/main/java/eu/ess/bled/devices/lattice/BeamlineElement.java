package eu.ess.bled.devices.lattice;

import java.util.Collection;

import eu.ess.bled.Subsystem;
import eu.ess.bled.devices.lattice.installation.CorrectionElement;

/**
 * <code>BeamlineElement</code> is the base element of the lattice. Every
 * element that is part of the lattice is an extension of this element, which is
 * derived from the {@link Subsystem}. It defines the length, optical length,
 * aperture size and position of the element in the ideal case. The length and
 * position are the theoretical values of the two attributes as calculated
 * during lattice design. In reality the attributes of the element might not be
 * ideal, therefore they are corrected with a {@link CorrectionElement}. This
 * element does not depend to a {@link CorrectionElement} and it can resides on
 * its own, but if there is a {@link CorrectionElement} that depends on this
 * {@link BeamlineElement}, those corrections need to be taken into account.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class BeamlineElement extends Subsystem {

	/**
	 * <code>ApertureType</code> describes different types of apertures.
	 * 
	 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
	 */
	public static enum ApertureType {
		RECTANGULAR(0), CIRCULAR(1), PERPERPOT_MODE(2), RECTANGULAR_TO_BEAM_FRACTION(3), HORIZONTAL_FINGER(4), VERTICAL_FINGER(
				5);

		private int val;

		ApertureType(int val) {
			this.val = val;
		}

		/**
		 * Returns the type that corresponds the given int value.
		 * 
		 * @param val
		 *            the requested value
		 * @return the enum type
		 */
		public static ApertureType toEnum(int val) {
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

	private static final long serialVersionUID = 2130649385503728900L;
	private Double length;
	private Double opticalLength;
	private Double position;
	private Double apertureY;
	private Double apertureX;
	private ApertureType apertureType;
	private CorrectionElement correctionElement;
	private Collection<BeamlineElementPropertyValue> properties;

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public Double getPosition() {
		return position;
	}

	public void setPosition(Double position) {
		this.position = position;
	}

	public Double getApertureY() {
		return apertureY;
	}

	public void setApertureY(Double apertureY) {
		this.apertureY = apertureY;
	}

	public Double getApertureX() {
		return apertureX;
	}

	public void setApertureX(Double apertureX) {
		this.apertureX = apertureX;
	}

	public void setOpticalLength(Double opticalLength) {
		this.opticalLength = opticalLength;
	}

	public Double getOpticalLength() {
		return opticalLength;
	}

	public ApertureType getApertureType() {
		return apertureType;
	}

	public void setApertureType(ApertureType apertureType) {
		this.apertureType = apertureType;
	}

	public CorrectionElement getCorrectionElement() {
		return correctionElement;
	}

	public void setCorrectionElement(CorrectionElement correctionElement) {
		this.correctionElement = correctionElement;
	}

	public Collection<BeamlineElementPropertyValue> getProperties() {
		return properties;
	}

	public void setProperties(Collection<BeamlineElementPropertyValue> properties) {
		this.properties = properties;
	}
}
