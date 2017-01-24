package eu.ess.bled.devices.lattice;

/**
 * <code>Corrector</code> is a definition of the beam correction element in the
 * lattice.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class Corrector extends BeamlineElement {

	private static final long serialVersionUID = 1199546869001413203L;

	private Boolean insideNext;
	private Double horizontalField;
	private Double verticalField;
	private Double maximumField;
	private Boolean electric;
	private Double coef1;
	private Double coef2;
	
	public Double getHorizontalField() {
		return horizontalField;
	}

	public void setHorizontalField(Double horizontalField) {
		this.horizontalField = horizontalField;
	}

	public Double getVerticalField() {
		return verticalField;
	}

	public void setVerticalField(Double verticalField) {
		this.verticalField = verticalField;
	}

	public Boolean getInsideNext() {
		return insideNext;
	}

	public void setInsideNext(Boolean insideNext) {
		this.insideNext = insideNext;
	}

	public Double getMaximumField() {
		return maximumField;
	}

	public void setMaximumField(Double maximumField) {
		this.maximumField = maximumField;
	}

	public Boolean getElectric() {
		return electric;
	}

	public void setElectric(Boolean electric) {
		this.electric = electric;
	}

	public Double getCoef1() {
		return coef1;
	}

	public void setCoef1(Double coef1) {
		this.coef1 = coef1;
	}

	public Double getCoef2() {
		return coef2;
	}

	public void setCoef2(Double coef2) {
		this.coef2 = coef2;
	}
}
