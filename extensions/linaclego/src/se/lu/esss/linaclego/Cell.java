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

import se.lu.esss.linaclego.models.CellModel;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "slot",
    "d"
})
@XmlRootElement(name = "cell")
public class Cell {

    protected List<Slot> slot = new ArrayList<Slot>();
    protected Parameters d = new Parameters();
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlIDREF
    @XmlAttribute(name = "model")
    protected CellModel model;
    
    @XmlTransient
    protected Cell cellInstance;

    public Cell()
    {
    	
    }
    
    public Cell(String id) {
    	this.id = id;
	}

	public List<Slot> getSlots() {
    	if (model != null) {
    		if (cellInstance == null)
    			cellInstance = model.apply(id, d);
    		return cellInstance.getSlots();  
    	} else
    		return this.slot;
    }

    public Parameters getParameters() { 
        return this.d;
    }

    public String getId() {
        return id;
    }

    public CellModel getModel() {
        return model;
    }

	public void accept(BLEVisitor visitor) {
		for (Slot s : getSlots()) s.accept(visitor);
	}
}
