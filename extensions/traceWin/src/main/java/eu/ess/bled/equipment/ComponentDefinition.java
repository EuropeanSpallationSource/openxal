package eu.ess.bled.equipment;

import java.util.Collection;

import eu.ess.bled.BLEDEntity;

/**
 * <p>
 * <code>ComponentDefinition</code> defines a single piece of component. This
 * class represents only te description of the component and has no knowledge
 * about the actual instance of the component. There can be several
 * {@link ComponentInstance}s for each {@link ComponentDefinition}. For example:
 * this definition defines a <code>power supply</code>, made by <code>XYZ</code>
 * that has some certain characteristics. There is only one such definition in
 * the system for this <code>power supply</code>; however, several instances of
 * the <code>power supply</code> can be installed in the system. </p>
 * <p>
 * This component is further identified by the {@link Manufacturer} of the
 * component. In case of the <code>the power supply</code> the
 * {@link #getManufacturer()} would point to the {@link Manufacturer} entity
 * with the name <code>XYZ</code>.
 * </p>
 * <p>
 * Each {@link ComponentDefinition} can have several properties, such as input
 * voltage or maximum current etc. These properties are defined by the
 * {@link ComponentPropertyValue}s. These properties don't necessarily
 * represents physical signals or control points of the device, but can
 * represents any possible characteristic of the component.
 * </p>
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class ComponentDefinition implements BLEDEntity {

	private static final long serialVersionUID = -3140690881754657313L;
	private Integer id;
	private String name;
	private String description;
	private Manufacturer manufacturer;
	private Collection<ComponentPropertyValue> properties;

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

	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Collection<ComponentPropertyValue> getProperties() {
		return properties;
	}

	public void setProperties(Collection<ComponentPropertyValue> properties) {
		this.properties = properties;
	}
}
