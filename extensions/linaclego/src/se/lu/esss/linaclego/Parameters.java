package se.lu.esss.linaclego;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;


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
	
	    @XmlMixed
	    @XmlElementRefs({
            @XmlElementRef(name="legoSet", type=JAXBElement.class)})
	    protected List<Object> value;
	    
	    @XmlAttribute(name = "id", required = true)
	    protected String id;
	    @XmlAttribute(name = "type")
	    protected String type;
	    @XmlAttribute(name = "unit")
	    protected String unit;
	
	    public D()
	    {
	    	
	    }
	    
	    public D(String id, D value)
	    {
	    	this.id = id;
	    	this.value = value.value;
	    }
	    
	    public D(String id, String value)
	    {
	    	this.id = id;
	    	this.value = Arrays.asList((Object)value);
	    }
	    	
	    public D(String id, String type, String unit, String value)
	    {
	    	this.id = id;
	    	this.type = type;
	    	this.unit = unit;
	    	this.value = Arrays.asList((Object)value);
	    }
	    
	    public String getValue() {
	    	StringBuffer buf = new StringBuffer();
	    	for (Object o : value) {
	    		if (o instanceof String) buf.append((String)o);
	    	}
	    	
	        return buf.toString().trim();
	    }
	    
		
		@SuppressWarnings("unchecked")
		public LegoSet getLegoSet() {
	    	for (Object o : value) {
	    		if (o instanceof JAXBElement && ((JAXBElement<?>)o).getDeclaredType().equals(LegoSet.class)) 	    			
	    			return ((JAXBElement<LegoSet>)o).getValue();
	    	}
	    	return null;
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