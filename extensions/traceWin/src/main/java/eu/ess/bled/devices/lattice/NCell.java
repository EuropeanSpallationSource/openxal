package eu.ess.bled.devices.lattice;

/**
 * <code>NCell</code> describes a multiple cavitity cell as used and defined in
 * the TraceWin.
 * 
 * @see for more info see TraceWin documentation
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class NCell extends BeamlineElement {

	private static final long serialVersionUID = 5022912837590247759L;


	private Integer mode;

	private Integer cellNumber;

	private Double betag;

	private Double e0T;

	private Double rfPhase;

	private Boolean absolutePhase;

	private Double kE0Ti;

	private Double kE0To;

	private Double dzi;

	private Double dzo;

	private Double betas;

	private Double transitTime;

	private Double kTsp;

	private Double k2Tspp;

	private Double transitTimeIn;

	private Double kTip;

	private Double k2Tipp;

	private Double transitTimeOut;

	private Double kTop;

	private Double k2Topp;

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public Integer getCellNumber() {
		return cellNumber;
	}

	public void setCellNumber(Integer cellNumber) {
		this.cellNumber = cellNumber;
	}

	public Double getBetag() {
		return betag;
	}

	public void setBetag(Double betag) {
		this.betag = betag;
	}

	public Double getE0T() {
		return e0T;
	}

	public void setE0T(Double e0t) {
		e0T = e0t;
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

	public Double getkE0Ti() {
		return kE0Ti;
	}

	public void setkE0Ti(Double kE0Ti) {
		this.kE0Ti = kE0Ti;
	}

	public Double getkE0To() {
		return kE0To;
	}

	public void setkE0To(Double kE0To) {
		this.kE0To = kE0To;
	}

	public Double getDzi() {
		return dzi;
	}

	public void setDzi(Double dzi) {
		this.dzi = dzi;
	}

	public Double getDzo() {
		return dzo;
	}

	public void setDzo(Double dzo) {
		this.dzo = dzo;
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

	public Double getK2Tspp() {
		return k2Tspp;
	}

	public void setK2Tspp(Double k2Tspp) {
		this.k2Tspp = k2Tspp;
	}

	public Double getTransitTimeIn() {
		return transitTimeIn;
	}

	public void setTransitTimeIn(Double transitTimeIn) {
		this.transitTimeIn = transitTimeIn;
	}

	public Double getkTip() {
		return kTip;
	}

	public void setkTip(Double kTip) {
		this.kTip = kTip;
	}

	public Double getK2Tipp() {
		return k2Tipp;
	}

	public void setK2Tipp(Double k2Tipp) {
		this.k2Tipp = k2Tipp;
	}

	public Double getTransitTimeOut() {
		return transitTimeOut;
	}

	public void setTransitTimeOut(Double transitTimeOut) {
		this.transitTimeOut = transitTimeOut;
	}

	public Double getkTop() {
		return kTop;
	}

	public void setkTop(Double kTop) {
		this.kTop = kTop;
	}

	public Double getK2Topp() {
		return k2Topp;
	}

	public void setK2Topp(Double k2Topp) {
		this.k2Topp = k2Topp;
	}
}
