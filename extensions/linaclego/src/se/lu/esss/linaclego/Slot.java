package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
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

    @XmlTransient
    private Cell parent;
    
    public Slot()
    {
    	
    }
    
    public Slot(String id, Cell parent)
    {
    	this.id = id;
    	this.parent = parent;
    }
    
    public Cell getParent()
    {
    	return parent;
    }
    
    public List<BeamlineElement> getBeamlineElements() {
    	if (model != null) {
    		if (slotInstance == null) {
    			slotInstance =  model.apply(getParameters());
    			slotInstance.id = id;
    			slotInstance.parent = parent;
    		}
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

	public Slot apply(Cell parent, Parameters arguments) {
		if (model != null) {
			Parameters pout = new Parameters();
			for (Parameters.D param : getParameters()) {
				Parameters.D arg = arguments.get(param.getValue());
				if (arg != null) {
					pout.add(new Parameters.D(param.getId(), arg));
				} else
					pout.add(param);
			}
			Slot instance = model.apply(pout);
			instance.id = id;
			instance.parent = parent;
			return instance;
		}
		else {
			Slot s = new Slot();
			s.id = id;
			s.parent = parent;
			List<BeamlineElement> bleout = s.getBeamlineElements();
			for (BeamlineElement blein : ble) {
				bleout.add(blein.apply(s, arguments));
			}
			return s;
		}
	}
	
	public void beforeUnmarshal(Unmarshaller u, Object parent) {
		if (parent instanceof Cell)
			this.parent = (Cell)parent;
	}
}
