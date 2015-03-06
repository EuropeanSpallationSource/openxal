package se.lu.esss.linaclego.elements;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.Linac;
import se.lu.esss.linaclego.Parameters;
import se.lu.esss.linaclego.Section;
import se.lu.esss.linaclego.Slot;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parameters"
})
@XmlRootElement(name = "ble")
@XmlJavaTypeAdapter(BeamlineElementXmlAdaptor.class)
public class BeamlineElement {
	
    @XmlElement(name = "d", required = true)
    protected Parameters parameters = new Parameters();    
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlTransient
    protected Slot parent;
    
    public BeamlineElement()
    {
    	 type = getClass().getAnnotation(XmlType.class).name();
    }
    
    public BeamlineElement(String id, Slot parent)
    {
    	type = getClass().getAnnotation(XmlType.class).name();
    	this.id = id;
    	this.parent = parent;
    }
    
    public Parameters getParameters() {
        return this.parameters;
    }

    public String getId() {
        return id;
    }
    
    public Slot getParent()
    {
    	return parent;
    }

	public void accept(BLEVisitor visitor) {
		visitControlPoints(visitor);
	}
	
	protected void visitControlPoints(BLEVisitor visitor) {
		Slot slot = getParent();
		Cell cell = slot.getParent();
		Section section = cell.getParent();
		Linac linac = section.getParent();
		
		for (ControlPoint cp : linac.getControlPoints(section.getId(), cell.getId(), slot.getId(), getId()))
		{
			visitor.visit(cp);
		}
	}

	public BeamlineElement apply(Slot parent, Parameters arguments) {
		BeamlineElement bleout;
		try {
			bleout = this.getClass().newInstance();
			
			bleout.id = id;
			bleout.parent = parent;
			Parameters pout = bleout.getParameters();
			for (Parameters.D param : parameters) {
				Parameters.D arg = arguments.get(param.getValue());
				if (arg != null) {
					pout.add(new Parameters.D(param.getId(), arg));
				} else
					pout.add(param);
			}
			return bleout;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public double getLength()
	{
		return getParameters().getDoubleValue("l");
	}
	
	public double getApertureR()
	{
		return getParameters().getDoubleValue("r");
	}
	
	public double getApertureY()
	{
		return getParameters().getDoubleValue("ry");
	}
	
	public String getEssId()
	{
		Slot slot = getParent();
		if (slot == null) return getId();
		Cell cell = slot.getParent();
		Section section = cell.getParent();
		return section.getId()+"-"+cell.getId()+"-"+slot.getId()+"-"+getId();
	}
	
	public double getFrequency() {
		Slot slot = getParent();
		Cell cell = slot.getParent();
		Section section = cell.getParent();
		Linac linac = section.getParent();
		return linac.getBeamFrequency() * section.getRFHarmonic();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parent instanceof Slot)
			this.parent = (Slot)parent;
	}
	
}
