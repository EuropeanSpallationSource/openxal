package eu.ess.bled;

/**
 * <p>
 * <code>Subsystem</code> is the base element for all elements in the control
 * system, which has a particular CS name attached to it. </p>
 * <p>
 * All subsystems are organized in a hierarchical way, which means that each
 * subsystem has a parent (only one), however more elements can have the same
 * parent. Parent Subsystem is the element which is directly above this element
 * or this element is a part of.
 * </p>
 * <p>
 * e.g. If this element represents a <code>subsystem</code> in the machine, than
 * its parent is the <code>system</code> {@link Subsystem}; if this
 * {@link Subsystem} is a <code>device</code>, its parent {@link Subsystem} is a
 * <code>subsystem</code>.
 * </p>
 * <p>
 * If the {@link Subsystem} has no parent it means that it is either the top
 * element in the machine or is a standalone part of the machine unrelated to
 * the accelerator lattice.
 * </p>
 * <p>
 * Additionally, each {@link Subsystem} has a predecessor or so called
 * <code>previousSubsystem</code>, which is also of a {@link Subsystem} type.
 * The predecessor defines the element that is physically located in the machine
 * before this {@link Subsystem}. This property is used to define the lattice of
 * the accelerator or just to functionally order the elements. If an element
 * does not have a <code>previousSubsystem</code>, that element is the first in
 * some group of {@link Subsystem}s, or may also not be a part of any group of
 * {@link Subsystem}s. If a {@link Subsystem} is also not a
 * <code>previousSubsystem</code> to any other {@link Subsystem} the element is
 * the last element in some group, or does not belong to any group if other
 * requirements are also met.
 * </p>
 * <p>
 * Each {@link Subsystem} also has a {@link SubsystemType} which further
 * describes it. The type defines the group of {@link Subsystem}s that this
 * {@link Subsystem} belongs to, such as for instance vacuum or pump. The
 * {@link SubsystemType} defines also a part of the naming convention complient
 * name of this {@link Subsystem}. The {@link ProcessVariable} name that is
 * associated with this {@link Subsystem} is composed of the hierarchical
 * composition of the naming convetion complient name of the type and the
 * instance name of this {@link Subsystem}.
 * </p>
 * <p>
 * Physical location of the {@link Subsystem} is defined by the Room
 * that this {@link Subsystem} is located in.
 * </p>
 * <p>
 * Furthermore, {@link Subsystem}s can also be used to group devices or other
 * subsystem into a logical component, which has a meaning in real situation,
 * but in terms of control system hierarchy it does not represents any of the
 * levels. Such {@link Subsystem} is marked as being virtual (
 * {@link #isVirtual()}). Virtual {@link Subsystem} can be used as any other
 * {@link Subsystem}, however one should be aware that this is an abstract
 * feature. All children of a virtual {@link Subsystem} are in the control
 * system actually children of the first non virtual parent in the hierarchy.
 * Virtual {@link Subsystem}s, which are on different hierarchy levels may still
 * be flattened into the same level in the control system hierarchy.
 * </p>
 * <p>
 * Each {@link Subsystem} can also be active or inactive ({@link #isActive()}).
 * Active {@link Subsystem}s are in use, while the inactive ones are the
 * {@link Subsystem}s that have been disabled in the machine. Obsolete
 * {@link Subsystem}s may also be marked as inactive if one doesn't wish to
 * remove them from the system completely.
 * </p>
 * <p>
 * {@link Subsystem} can have a link to a {@link ComponentInstance}, which
 * describes the actual physical component in the system, such as a physical
 * device installed in the accelerator.
 * 
 * @see For details on the name composition see ESS Naming Convention document.
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class Subsystem implements BLEDEntity {

	private static final long serialVersionUID = 3656417365837106137L;
	private Integer id;
	private String name;
	private Subsystem parentSubsystem;
	private Integer previousSubsystem;
	private SubsystemType subsystemType;
	private String description;
	private Boolean active = true;
	private String instanceName;
	private Boolean virtual = false;

	private static int nextid = 0;
	
	public Integer getId() {
		if (id == null) id = nextid++;
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Subsystem getParentSubsystem() {
		return parentSubsystem;
	}

	public void setParentSubsystem(Subsystem parentSubsystem) {
		this.parentSubsystem = parentSubsystem;
	}

	public Integer getPreviousSubsystem() {
		return previousSubsystem;
	}

	public void setPreviousSubsystem(Integer previousSubsystem) {
		this.previousSubsystem = previousSubsystem;
	}

	public SubsystemType getSubsystemType() {
		return subsystemType;
	}

	public void setSubsystemType(SubsystemType subsystemType) {
		this.subsystemType = subsystemType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public Boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(Boolean virtual) {
		this.virtual = virtual;
	}

	protected StringBuilder getValuesAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name=");
		sb.append(name);
		sb.append(", instance_name=");
		sb.append(instanceName);
		sb.append(", description=");
		sb.append(description);
		return sb;
	}

	@Override
	public String toString() {
		return "Subsystem: [" + getValuesAsString().toString() + "]";
	}
}
