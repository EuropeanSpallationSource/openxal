package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


public class Parameters extends ArrayList<Parameters.D> {
	private static final long serialVersionUID = 1L;
	protected Map<String, D> hashed = new HashMap<>();
	
	
	public double getDoubleValue(String id)
	{
		return Double.parseDouble(hashed.get(id).getValue());
	}
	
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
	
	    public D()
	    {
	    	
	    }
	    
	    public D(String id, String value)
	    {
	    	this.id = id;
	    	this.value = value;
	    }
	    	
	    public D(String id, String type, String unit, String value)
	    {
	    	this.id = id;
	    	this.type = type;
	    	this.unit = unit;
	    	this.value = value;
	    }
	    
	    public String getValue() {
	        return value;
	    }
	
	    public String getId() {
	        return id;
	    }
	
	    public String getType() {
	        return type;
	    }
	
	    public String getUnit() {
	        return unit;
	    }
	}

	@Override
	public boolean add(D e) {
		hashed.put(e.getId(), e);
		return super.add(e);
	}
}