package eu.ess.bled.equipment;

import eu.ess.bled.BLEDEntity;

/**
 * <code>ComponentPropertyValue</code> defines a value of one
 * {@link ComponentProperty}. There can be several different
 * {@link ComponentPropertyValue} for each {@link ComponentProperty}, however,
 * each such value has to be linked to a different {@link ComponentDefinition}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class ComponentPropertyValue implements BLEDEntity {

	private static final long serialVersionUID = 7773935329435422970L;
	private Integer id;

	private String value;
	private ComponentDefinition componentDefinition;
	private ComponentProperty componentProperty;

	public Integer getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ComponentDefinition getComponentDefinition() {
		return componentDefinition;
	}

	public void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}

	public ComponentProperty getComponentProperty() {
		return componentProperty;
	}

	public void setComponentProperty(ComponentProperty componentProperty) {
		this.componentProperty = componentProperty;
	}
}
