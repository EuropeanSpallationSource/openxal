package edu.stanford.slac.util.zplot.model;

public class Beamline {
	private final String name;
	private final double startZ;
	private final double endZ;

	public Beamline(String name, double startZ, double endZ) {
		this.name = name;
		this.startZ = startZ;
		this.endZ = endZ;
	}

	public String getName() {
		return name;
	}

	public double getStartZ() {
		return startZ;
	}

	public double getEndZ() {
		return endZ;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Beamline) {
			Beamline other = (Beamline) obj;
			return this.name.equals(other.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}

}
