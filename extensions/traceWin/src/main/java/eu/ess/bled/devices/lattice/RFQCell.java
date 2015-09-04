package eu.ess.bled.devices.lattice;

/**
 * <code>RFQCell</code> describes a radio frequency quadrupole cell as defined
 * and used in TraceWin.
 * 
 * @see for details see TraceWin documentation
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class RFQCell extends BeamlineElement {

	private static final long serialVersionUID = -5718858021140018364L;


	private Double gapVoltage;

	private Double vaneRadius;

	private Double accelParam;

	private Double modulation;

	private Double rfPhase;

	private String cellType;

	private Double transCurv;

	private Double transFocus;

	public Double getGapVoltage() {
		return gapVoltage;
	}

	public void setGapVoltage(Double gapVoltage) {
		this.gapVoltage = gapVoltage;
	}

	public Double getVaneRadius() {
		return vaneRadius;
	}

	public void setVaneRadius(Double vaneRadius) {
		this.vaneRadius = vaneRadius;
	}

	public Double getAccelParam() {
		return accelParam;
	}

	public void setAccelParam(Double accelParam) {
		this.accelParam = accelParam;
	}

	public Double getModulation() {
		return modulation;
	}

	public void setModulation(Double modulation) {
		this.modulation = modulation;
	}

	public Double getRfPhase() {
		return rfPhase;
	}

	public void setRfPhase(Double rfPhase) {
		this.rfPhase = rfPhase;
	}

	public String getCellType() {
		return cellType;
	}

	public void setCellType(String cellType) {
		this.cellType = cellType;
	}

	public Double getTransCurv() {
		return transCurv;
	}

	public void setTransCurv(Double transCurv) {
		this.transCurv = transCurv;
	}

	public Double getTransFocus() {
		return transFocus;
	}

	public void setTransFocus(Double transFocus) {
		this.transFocus = transFocus;
	}
}
