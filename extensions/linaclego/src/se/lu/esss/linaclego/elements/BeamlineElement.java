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
    protected Parameters parameters;    
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;
    @XmlAttribute(name = "type")
    protected String type;
    
    public Parameters getParameters() {
        if (parameters == null) {
            parameters = new se.lu.esss.linaclego.Parameters();
        }
        return this.parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

}
