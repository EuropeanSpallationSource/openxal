package eu.ess.bled.equipment;

import eu.ess.bled.BLEDEntity;

/**
 * <code>Manufacturer</code> represents a manufacturer of goods, components etc.
 * Each device or other physical component has a known manufacturer, which is
 * used to more precisely describe the component.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class Manufacturer implements BLEDEntity {

	private static final long serialVersionUID = -6786190613174099136L;

	private Integer id;
	private String name;
	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
