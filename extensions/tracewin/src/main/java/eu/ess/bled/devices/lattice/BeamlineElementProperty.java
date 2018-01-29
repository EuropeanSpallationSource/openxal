package eu.ess.bled.devices.lattice;

import eu.ess.bled.BLEDEntity;

/**
 * 
 * <code>BeamlineElementProperty</code> represents an additional property that
 * can be defined for one beamline element. The property represents an abstract
 * entity, and does not have any relation to any element in the system. It
 * servers merely as a description of the entity. The property is realized
 * through the {@link BeamlineElementPropertyValue}, which represents a concrete
 * property and value of one {@link BeamlineElement}.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */
public class BeamlineElementProperty implements BLEDEntity {

	private static final long serialVersionUID = 683422333721779368L;

	/**
	 * 
	 * <code>PropertyType</code> defines the type of the property value. The
	 * value is always stored as a string, but based on the property type it can
	 * be parsed into appropriate type.
	 * 
	 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
	 * 
	 */
	public static enum PropertyType {
		TEXT(0), DECIMAL(1), INTEGER(2);

		private int val;

		PropertyType(int val) {
			this.val = val;
		}

		/**
		 * Returns the type that corresponds the given int value.
		 * 
		 * @param val
		 *            the requested value
		 * @return the enum type
		 */
		public static PropertyType toEnum(int val) {
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
	private String name;
	private String description;
	private PropertyType propertyType;

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

	public PropertyType getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}
}
