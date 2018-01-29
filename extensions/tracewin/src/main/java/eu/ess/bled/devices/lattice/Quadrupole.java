package eu.ess.bled.devices.lattice;

public class Quadrupole extends MultipoleMagnet {

	private static final long serialVersionUID = -6119164258245692863L;

	@Override
	public MagnetType getMagnetType() {
		return MagnetType.QUADRUPOLE;
	}

	@Override
	public void setMagnetType(MagnetType magnetType) {
		super.setMagnetType(MagnetType.QUADRUPOLE);
	}
}
