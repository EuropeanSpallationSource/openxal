package se.lu.esss.linaclego;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


public class Parameters extends AbstractList<Parameters.D> {
	protected Map<String, D> hashedData = new HashMap<>();
	protected List<D> data = new ArrayList<>();
	
	public Parameters() 
	{
	}

	public D get(String value) {
		return hashedData.get(value);
	}
	
	public double getDoubleValue(String id)
	{
		Parameters.D p = hashedData.get(id);
		return p == null ? 0 : Double.parseDouble(p.getValue());
	}
	

	public int getIntValue(String id) {
		Parameters.D p = hashedData.get(id);
		return p == null ? 0 : Integer.parseInt(p.getValue());
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
	    
	    @XmlTransient
	    protected LegoSet legoSet;
	
	    public D()
	    {
	    	
	    }
	    
	    public D(String id, D value)
	    {
	    	this.id = id;
	    	this.value = value.value;
	    	this.legoSet = value.legoSet;
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
	    
		
		public LegoSet getLegoSet() {
			return legoSet;
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
	public D get(int index) {
		return data.get(index);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public void add(int i,D e) {
		hashedData.put(e.id, e);
		data.add(i, e);
	}
	
	@Override
	public D remove(int i)
	{
		D d = data.remove(i);
		hashedData.remove(d.id);
		return d;
	}

}