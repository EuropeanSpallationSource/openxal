package eu.ess.bled.equipment;

/**
 * 
 * <code>ComponentProperty</code> represents a single physical property of a
 * {@link ComponentDefinition}, such as for example weight, or size etc. The
 * component property is intended to describe the component in more details.
 * {@link ComponentProperty ()} is used only to describe the property, while the
 * actual value is held by the {@link ComponentPropertyValue}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */

public class ComponentProperty {

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