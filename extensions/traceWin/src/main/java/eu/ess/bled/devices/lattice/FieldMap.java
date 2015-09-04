package eu.ess.bled.devices.lattice;

/**
 * <code>FieldMap</code> represents a field map element.
 * 
 * @author <a href="mailto:luka.stopar@cosylab.com">Luka Stopar</a>
 */

public class FieldMap extends BeamlineElement {

	private static final long serialVersionUID = 7093145695308828230L;
	private Integer geom;
	private Double rfPhase;
	private Double magneticIntensityFactor;
	private Double electricIntensityFactor;
	private Double spaceChargeCompensationFactor;
	private Integer apertureFlag;
	private String fileName;

	public Integer getGeom() {
		return geom;
	}

	public void setGeom(Integer geom) {
		this.geom = geom;
	}

	public Double getRfPhase() {
		return rfPhase;
	}

	public void setRfPhase(Double rfPhase) {
		this.rfPhase = rfPhase;
	}

	public Double getMagneticIntensityFactor() {
		return magneticIntensityFactor;
	}

	public void setMagneticIntensityFactor(Double magneticIntensityFactor) {
		this.magneticIntensityFactor = magneticIntensityFactor;
	}

	public Double getElectricIntensityFactor() {
		return electricIntensityFactor;
	}

	public void setElectricIntensityFactor(Double electricIntensityFactor) {
		this.electricIntensityFactor = electricIntensityFactor;
	}

	public Double getSpaceChargeCompensationFactor() {
		return spaceChargeCompensationFactor;
	}

	public void setSpaceChargeCompensationFactor(Double spaceChargeCompensationFactor) {
		this.spaceChargeCompensationFactor = spaceChargeCompensationFactor;
	}

	public Integer getApertureFlag() {
		return apertureFlag;
	}

	public void setApertureFlag(Integer apertureFlag) {
		this.apertureFlag = apertureFlag;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
