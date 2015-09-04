package eu.ess.bled.infrastructure;

import eu.ess.bled.BLEDEntity;

/**
 * <code>Room</code> is a physical location within the building. Each room is a
 * part of the {@link Building}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class Room implements BLEDEntity {

	private static final long serialVersionUID = -8452277078077699121L;

	private Integer id;
	private String name;
	private Building building;

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

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}
}
