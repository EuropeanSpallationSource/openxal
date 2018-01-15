package eu.ess.bled.devices.lattice;

/**
 * <code>Bend</code> is an extension of the {@link Magnet}, which describes a
 * single bending magnet in the accelerator.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class Bend extends Magnet {

	private static final long serialVersionUID = 6280045004498060060L;

	/**
	 * 
	 * <code>Orientation</code> describes different magnet orientations.
	 * Orientation can be either horizontal or vertical.
	 * 
	 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
	 *         Prelog</a>
	 * 
	 */
	public static enum Orientation {
		HORIZONTAL(0), VERTICAL(1);

		private int val;

		Orientation(int val) {
			this.val = val;
		}

		/**
		 * Returns the type that corresponds to the given int value.
		 * 
		 * @param val
		 *            the requested value
		 * @return the enum type
		 */
		public static Orientation toEnum(int val) {
			return values()[val];
		}

		/**
		 * Returns the integer representation of this type.
		 * 
		 * @return the integer value
		 */
		public int getIntegerValue() {
			return val;
		}
	}

	private Double bendAngle;
	private Double entranceAngle;
	private Double exitAngle;
	private Double entranceCurvature;
	private Double exitCurvature;
	private String entranceName;
	private String exitName;
	private Double gap;
	private Double curvatureRadius;
	private Orientation orientation;

	public Double getBendAngle() {
		return bendAngle;
	}

	public void setBendAngle(Double bendAngle) {
		this.bendAngle = bendAngle;
	}

	public Double getEntranceAngle() {
		return entranceAngle;
	}

	public void setEntranceAngle(Double entranceAngle) {
		this.entranceAngle = entranceAngle;
	}

	public Double getExitAngle() {
		return exitAngle;
	}

	public void setExitAngle(Double exitAngle) {
		this.exitAngle = exitAngle;
	}

	public Double getEntranceCurvature() {
		return entranceCurvature;
	}

	public void setEntranceCurvature(Double entranceCurvature) {
		this.entranceCurvature = entranceCurvature;
	}

	public Double getExitCurvature() {
		return exitCurvature;
	}

	public void setExitCurvature(Double exitCurvature) {
		this.exitCurvature = exitCurvature;
	}

	public String getEntranceName() {
		return entranceName;
	}

	public void setEntranceName(String entranceName) {
		this.entranceName = entranceName;
	}

	public String getExitName() {
		return exitName;
	}

	public void setExitName(String exitName) {
		this.exitName = exitName;
	}

	public Double getGap() {
		return gap;
	}

	public void setGap(Double gap) {
		this.gap = gap;
	}

	public Double getCurvatureRadius() {
		return curvatureRadius;
	}

	public void setCurvatureRadius(Double curvatureRadius) {
		this.curvatureRadius = curvatureRadius;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
}
