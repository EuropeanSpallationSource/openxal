package se.lu.esss.linaclego.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.Parameters;
import se.lu.esss.linaclego.Slot;
import se.lu.esss.linaclego.elements.BeamlineElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "var",
    "ble"
})
@XmlRootElement(name = "slotModel")
public class SlotModel {

    protected List<Var> var;
    @XmlElement(required = true)
    protected List<BeamlineElement> ble = new ArrayList<BeamlineElement>();
    @XmlID
    @XmlAttribute(name = "id", required = true)
    protected String id;

    public SlotModel()
    {
    	
    }
    
    public SlotModel(String id)
    {
    	this.id = id;
    }
    
    public List<Var> getVar() {
        if (var == null) {
            var = new ArrayList<Var>();
        }
        return this.var;
    }

    public List<BeamlineElement> getBle() {
        return this.ble;
    }

    public String getId() {
        return id;
    }

	public Slot apply(Parameters arguments) {
		Slot slotout = new Slot();
		List<BeamlineElement> bleout = slotout.getBeamlineElements();
		for (BeamlineElement blein1 : this.ble) {
			BeamlineElement bleout1 = blein1.apply(slotout, arguments);
			bleout.add(bleout1);
		}
		return slotout;
		
	}

}
