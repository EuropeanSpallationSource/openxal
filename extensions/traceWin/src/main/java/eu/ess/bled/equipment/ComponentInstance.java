package eu.ess.bled.equipment;

import java.sql.Timestamp;

import eu.ess.bled.BLEDEntity;
import eu.ess.bled.Subsystem;
import eu.ess.bled.infrastructure.Room;

/**
 * <code>ComponentInstance</code> represents a single physical component or a
 * device in the system. A {@link ComponentInstance} is associated with a
 * {@link eu.ess.bled.Subsystem} (optional) as to describe which subsystem it
 * represents or which subsystem it belongs to.
 * <p>
 * Each {@link ComponentInstance} has a serial version number and a link to
 * {@link ComponentDefinition} which define this instance.
 * </p>
 * <p>
 * Every {@link ComponentInstance} also has a single status value, which defines
 * the current status of the {@link ComponentInstance}, such as for example
 * component is ordered, or component is delivered or installed. When the
 * component is removed from the system it is made deprecated and later
 * destroyed when removed from the inventory.
 * </p>
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class ComponentInstance implements BLEDEntity {

	private static final long serialVersionUID = 5293294349649295597L;

	/**
	 * <code>ComponentStatus</code> represents the status of this component. Is
	 * the component planned ordered, delivered etc.
	 * 
	 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
	 */
	public static enum ComponentStatus {
		UNKNOWN(0), PLANNED(1), OREDERED(2), DELIVERED(3), INSTALLED(4), BROKEN(5), DEPRECATED(6), DESTROYED(7);

		private int val;

		ComponentStatus(int val) {
			this.val = val;
		}

		/**
		 * Returns the type that corresponds the given int value.
		 * 
		 * @param val
		 *            the requested value
		 * @return the enum type
		 */
		public static ComponentStatus toEnum(int val) {
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

	private Integer id;
	private Subsystem subsystem;
	private String name;
	private String serialNumber;
	private ComponentStatus status;
	private Timestamp orderDate;
	private Timestamp deliverDate;
	private Timestamp installDate;
	private ComponentDefinition componentDefinition;
	private Room room;

	public Integer getId() {
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

	public Subsystem getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(Subsystem subsystem) {
		this.subsystem = subsystem;
	}

	public ComponentStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public ComponentDefinition getComponentDefinition() {
		return componentDefinition;
	}

	public void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Timestamp getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}

	public Timestamp getDeliverDate() {
		return deliverDate;
	}

	public void setDeliverDate(Timestamp deliverDate) {
		this.deliverDate = deliverDate;
	}

	public Timestamp getInstallDate() {
		return installDate;
	}

	public void setInstallDate(Timestamp installDate) {
		this.installDate = installDate;
	}
}
