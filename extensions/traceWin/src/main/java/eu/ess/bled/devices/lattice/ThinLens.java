package eu.ess.bled.devices.lattice;

/**
 * <code>ThinLens</code> represents a thin lens element, which has a 0 length.
 * 
 * @author <a href="mailto:luka.stopar@cosylab.com">Luka Stopar</a>
 */

public class ThinLens extends BeamlineElement {

	private static final long serialVersionUID = 6427542882989817706L;

	private Double focalLengthX;

	private Double focalLengthY;

	public Double getFocalLengthX() {
		return focalLengthX;
	}

	public void setFocalLengthX(Double focalLengthX) {
		this.focalLengthX = focalLengthX;
	}

	public Double getFocalLengthY() {
		return focalLengthY;
	}

	public void setFocalLengthY(Double focalLengthY) {
		this.focalLengthY = focalLengthY;
	}
}
