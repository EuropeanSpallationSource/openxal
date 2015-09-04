package eu.ess.bled.devices.lattice;

/**
 * <code>RFCavity</code> represents an rf cavity element in the lattice.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class RFCavity extends BeamlineElement {

	public enum CavityType {
		SINUS, BUNCHED
	}

	private static final long serialVersionUID = 8624484485602999821L;


	private Double phase;

	private Boolean absolutePhase;

	private Integer cellNumber;

	private Double averageField;

	private Double gapVoltage;

	private Double beta;

	private Double transitTimeFactor;

	private Double kT;

	private Double k2T;

	private Double kS;

	private Double k2S;

	private CavityType type;

	public Double getPhase() {
		return phase;
	}

	public void setPhase(Double phase) {
		this.phase = phase;
	}

	public Boolean getAbsolutePhase() {
		return absolutePhase;
	}

	public void setAbsolutePhase(Boolean absolutePhase) {
		this.absolutePhase = absolutePhase;
	}

	public Integer getCellNumber() {
		return cellNumber;
	}

	public void setCellNumber(Integer cellNumber) {
		this.cellNumber = cellNumber;
	}

	public Double getAverageField() {
		return averageField;
	}

	public void setAverageField(Double averageField) {
		this.averageField = averageField;
	}

	public Double getGapVoltage() {
		return gapVoltage;
	}

	public void setGapVoltage(Double gapVoltage) {
		this.gapVoltage = gapVoltage;
	}

	public Double getBeta() {
		return beta;
	}

	public void setBeta(Double beta) {
		this.beta = beta;
	}

	public Double getTransitTimeFactor() {
		return transitTimeFactor;
	}

	public void setTransitTimeFactor(Double transitTimeFactor) {
		this.transitTimeFactor = transitTimeFactor;
	}

	public Double getkT() {
		return kT;
	}

	public void setkT(Double kT) {
		this.kT = kT;
	}

	public Double getK2T() {
		return k2T;
	}

	public void setK2T(Double k2t) {
		k2T = k2t;
	}

	public Double getkS() {
		return kS;
	}

	public void setkS(Double kS) {
		this.kS = kS;
	}

	public Double getK2S() {
		return k2S;
	}

	public void setK2S(Double k2s) {
		k2S = k2s;
	}

	public CavityType getType() {
		return type;
	}

	public void setType(CavityType type) {
		this.type = type;
	}
}
