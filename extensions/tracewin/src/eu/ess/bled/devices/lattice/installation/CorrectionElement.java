package eu.ess.bled.devices.lattice.installation;

import eu.ess.bled.BLEDEntity;
import eu.ess.bled.devices.lattice.BeamlineElement;

/**
 * <code>CorrectionElement</code> represents a real life correction of the
 * beamline element. When devices are installed in the system, their
 * characteristics might differ from the ideal designed system. In such cases
 * the element needs to be corrected. The corrections include its rotation,
 * alignment along the <code>z</code> (beam axis), the real length of the
 * element and the real optical lengths could also be different.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */


public class CorrectionElement implements BLEDEntity {

	private static final long serialVersionUID = -2803049537409813350L;
	private Integer id;

	private BeamlineElement beamlineElement;

	private Double alignX;

	private Double alignY;

	private Double alignZ;

	private Double alignPitch;

	private Double alignYaw;

	private Double alignRoll;

	private Double realLength;

	private Double realOpticalLength;

	private Double realApertureX;

	private Double realApertureY;

	public Integer getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	public Double getAlignX() {
		return alignX;
	}

	public void setAlignX(Double alignX) {
		this.alignX = alignX;
	}

	public Double getAlignY() {
		return alignY;
	}

	public void setAlignY(Double alignY) {
		this.alignY = alignY;
	}

	public Double getAlignZ() {
		return alignZ;
	}

	public void setAlignZ(Double alignZ) {
		this.alignZ = alignZ;
	}

	public Double getAlignPitch() {
		return alignPitch;
	}

	public void setAlignPitch(Double alignPitch) {
		this.alignPitch = alignPitch;
	}

	public Double getAlignYaw() {
		return alignYaw;
	}

	public void setAlignYaw(Double alignYaw) {
		this.alignYaw = alignYaw;
	}

	public Double getAlignRoll() {
		return alignRoll;
	}

	public void setAlignRoll(Double alignRoll) {
		this.alignRoll = alignRoll;
	}

	public void setRealLength(Double realLength) {
		this.realLength = realLength;
	}

	public Double getRealLength() {
		return realLength;
	}

	public void setRealOpticalLength(Double realOpticalLength) {
		this.realOpticalLength = realOpticalLength;
	}

	public Double getRealOpticalLength() {
		return realOpticalLength;
	}

	public BeamlineElement getBeamlineElement() {
		return beamlineElement;
	}

	public void setBeamlineElement(BeamlineElement beamlineElement) {
		this.beamlineElement = beamlineElement;
	}

	public Double getRealApertureX() {
		return realApertureX;
	}

	public void setRealApertureX(Double realApertureX) {
		this.realApertureX = realApertureX;
	}

	public Double getRealApertureY() {
		return realApertureY;
	}

	public void setRealApertureY(Double realApertureY) {
		this.realApertureY = realApertureY;
	}
}
