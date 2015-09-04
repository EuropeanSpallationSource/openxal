package eu.ess.bled.infrastructure;

import eu.ess.bled.BLEDEntity;

/**
 * <code>Building</code> describes the building which is a part of the machine.
 * Building is an agreggation of {@link Room}s.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class Building implements BLEDEntity {

	private static final long serialVersionUID = 4794914477877629384L;
	private Integer id;
	private String name;

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
}
