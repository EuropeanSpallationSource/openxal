package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.elements.BeamlineElement;
import se.lu.esss.linaclego.models.SlotModel;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ble",
    "d"
})
@XmlRootElement(name = "slot")
public class Slot {
	protected List<BeamlineElement> ble = new ArrayList<BeamlineElement>();
    protected Parameters d = new Parameters();
    @XmlAttribute(name = "id", required = true)
    protected String id;
    
    @XmlIDREF
    @XmlAttribute(name = "model")
    protected SlotModel model;
    
    @XmlTransient
    protected Slot slotInstance;

    public Slot()
    {
    	
    }
    
    public Slot(String id)
    {
    	this.id = id;
    }
    
    public List<BeamlineElement> getBeamlineElements() {
    	if (model != null) {
    		if (slotInstance == null)
    			slotInstance =  model.apply(id, getParameters());    		 
    		return slotInstance.getBeamlineElements();
    	} else {
    		return this.ble;
    	}
    }

    public Parameters getParameters() {
        return this.d;
    }

    public String getId() {
        return id;
    }

    public SlotModel getModel() {
        return model;
    }

	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		for (BeamlineElement ble : getBeamlineElements()) ble.accept(visitor);
	}

	public Slot apply(Parameters arguments) {
		if (model != null) {
			Parameters pout = new Parameters();
			for (Parameters.D param : getParameters()) {
				Parameters.D arg = arguments.get(param.getValue());
				if (arg != null) {
					pout.add(new Parameters.D(param.getId(), arg));
				} else
					pout.add(param);
			}
			return model.apply(id, pout); 
		}
		else {
			Slot s = new Slot();
			s.id = id;
			List<BeamlineElement> bleout = s.getBeamlineElements();
			for (BeamlineElement blein : ble)
				bleout.add(blein.apply(arguments));
			return s;
		}
	}
}
