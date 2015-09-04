package eu.ess.bled;

/**
 * <code>SubsystemType</code> defines the possible types of {@link Subsystem}s,
 * such as for example vacuum, or cooling. Each type is defined by a human
 * readable name <code>name</code> and its naming convention complient name
 * <code>nc_name</code>.
 * <p>
 * Each {@link Subsystem} has a link to a particular {@link SubsystemType},
 * which defines the naming convention name of that {@link Subsystem}. More than
 * one {@link Subsystem} can have a link to each {@link SubsystemType}.
 * </p>
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class SubsystemType implements BLEDEntity {

	private static final long serialVersionUID = -6701569406227532216L;

	private Integer id;
	private String name;
	private String ncName;
	private String description;

	public Integer getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	public String getNCName() {
		return ncName;
	}

	public void setNCName(String name) {
		this.ncName = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name=");
		sb.append(name);
		sb.append(", nc_name=");
		sb.append(ncName);
		sb.append(", description=");
		sb.append(description);
		return "SubsystemType [" + sb.toString() + "]";
	}
}
