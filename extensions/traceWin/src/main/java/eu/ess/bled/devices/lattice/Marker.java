package eu.ess.bled.devices.lattice;

/**
 * 
 * <code>Marker</code> is an abstract beamline element of null length and
 * aperture, which defines a particular location in the lattice.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */

public class Marker extends BeamlineElement {

	private static final long serialVersionUID = 4987660751185142290L;

	@Override
	public Double getLength() {
		return null;
	}

	@Override
	public Double getOpticalLength() {
		return null;
	}

	@Override
	public Double getApertureX() {
		return null;
	}

	@Override
	public Double getApertureY() {
		return null;
	}

	@Override
	public ApertureType getApertureType() {
		return null;
	}

}
