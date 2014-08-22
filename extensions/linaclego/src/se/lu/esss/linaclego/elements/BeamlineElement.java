package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import se.lu.esss.linaclego.Parameters;

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
    
    public BeamlineElement()
    {
    	 type = getClass().getAnnotation(XmlType.class).name();
    }
    
    public BeamlineElement(String id)
    {
    	type = getClass().getAnnotation(XmlType.class).name();
    	this.id = id;
    }
    
    public Parameters getParameters() {
        return this.parameters;
    }

    public String getId() {
        return id;
    }
}
