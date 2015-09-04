package eu.ess.bled.devices.lattice;

/**
 * <code>DTLCell</code> represents a Drift Tube Linac element as used in the
 * TraceWin.
 * 
 * @see for more info on DTLCell see TraceWin documentation
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class DTLCell extends BeamlineElement {

	private static final long serialVersionUID = 1249482597879153482L;

	private Double lq1;
	private Double lq2;
	private Double cellCenter;
	private Double b1p;
	private Double b2p;
	private Double e0TL;
	private Double rfPhase;
	private Boolean absolutePhase;
	private Double betas;
	private Double transitTime;
	private Double kTsp;
	private Double k2Tsp;

	public Double getLq1() {
		return lq1;
	}

	public void setLq1(Double lq1) {
		this.lq1 = lq1;
	}

	public Double getLq2() {
		return lq2;
	}

	public void setLq2(Double lq2) {
		this.lq2 = lq2;
	}

	public Double getCellCenter() {
		return cellCenter;
	}

	public void setCellCenter(Double cellCenter) {
		this.cellCenter = cellCenter;
	}

	public Double getB1p() {
		return b1p;
	}

	public void setB1p(Double b1p) {
		this.b1p = b1p;
	}

	public Double getB2p() {
		return b2p;
	}

	public void setB2p(Double b2p) {
		this.b2p = b2p;
	}

	public Double getE0TL() {
		return e0TL;
	}

	public void setE0TL(Double e0tl) {
		e0TL = e0tl;
	}

	public Double getRfPhase() {
		return rfPhase;
	}

	public void setRfPhase(Double rfPhase) {
		this.rfPhase = rfPhase;
	}

	public Boolean getAbsolutePhase() {
		return absolutePhase;
	}

	public void setAbsolutePhase(Boolean absolutePhase) {
		this.absolutePhase = absolutePhase;
	}

	public Double getBetas() {
		return betas;
	}

	public void setBetas(Double betas) {
		this.betas = betas;
	}

	public Double getTransitTime() {
		return transitTime;
	}

	public void setTransitTime(Double transitTime) {
		this.transitTime = transitTime;
	}

	public Double getkTsp() {
		return kTsp;
	}

	public void setkTsp(Double kTsp) {
		this.kTsp = kTsp;
	}

	public Double getK2Tsp() {
		return k2Tsp;
	}

	public void setK2Tsp(Double k2Tsp) {
		this.k2Tsp = k2Tsp;
	}
}
