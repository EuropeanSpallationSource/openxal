package se.lu.esss.linaclego;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


public class Parameters extends ArrayList<Parameters.D> {
	private static final long serialVersionUID = 1L;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "d", propOrder = {
	    "value"
	})
	public static class D {
	
	    @XmlValue
	    protected String value;
	    @XmlAttribute(name = "id", required = true)
	    protected String id;
	    @XmlAttribute(name = "type")
	    protected String type;
	    @XmlAttribute(name = "unit")
	    protected String unit;
	
	    public String getValue() {
	        return value;
	    }
	
	    public void setValue(String value) {
	        this.value = value;
	    }
	
	    public String getId() {
	        return id;
	    }
	
	    public void setId(String value) {
	        this.id = value;
	    }
	
	    public String getType() {
	        return type;
	    }
	
	    public void setType(String value) {
	        this.type = value;
	    }
	
	    public String getUnit() {
	        return unit;
	    }
	
	    public void setUnit(String value) {
	        this.unit = value;
	    }
	}
}