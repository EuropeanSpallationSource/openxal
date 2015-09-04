package eu.ess.bled.devices.lattice;

import eu.ess.bled.BLEDEntity;

/**
 * 
 * <code>BeamlineElementPropertyValue</code> represents a concrete value of one
 * property of a {@link BeamlineElement}. The value has a direct relation to one
 * {@link BeamlineElementProperty}, which describes this value. More than one
 * value can exist for a single {@link BeamlineElementProperty}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */
public class BeamlineElementPropertyValue implements BLEDEntity {

	private static final long serialVersionUID = -1008840422581055871L;

	private Integer id;
	private BeamlineElementProperty property;
	private String value;
	private BeamlineElement beamlineElement;

	public Integer getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	public BeamlineElementProperty getProperty() {
		return property;
	}

	public void setProperty(BeamlineElementProperty property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public BeamlineElement getBeamlineElement() {
		return beamlineElement;
	}

	public void setBeamlineElement(BeamlineElement beamlineElement) {
		this.beamlineElement = beamlineElement;
	}
}
