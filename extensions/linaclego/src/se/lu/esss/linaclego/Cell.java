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

import se.lu.esss.linaclego.Parameters.D;
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

    @XmlTransient
    private Section parent;
    
    public Cell()
    {
    	
    }
    
    public Cell(String id, Section parent) {
    	this.id = id;
    	this.parent = parent;
	}

    public Section getParent()
    {
    	return parent;
    }
    
	public List<Slot> getSlots() {
    	if (model != null) {
    		if (cellInstance == null) {
    			cellInstance = model.apply(d);
    			cellInstance.id = id;
    			cellInstance.parent = parent;
    		}
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
		visitor.visit(this);
		for (Slot s : getSlots()) s.accept(visitor);
	}

	public int getNumBeamlineElements() {
		int n = 0;
		for (Slot s : getSlots())
			n += s.getBeamlineElements().size();
		return n;
	}

	public void beforeUnmarshal(Unmarshaller u, Object parent) {
		this.parent = (Section)parent;
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		Section section = (Section)parent;
		Linac linac = section.getParent();
		
		//load lego sets
		for (LegoSet s : linac.getLegoSets(section, this)) {
			String dataId = s.getDataId();
			D data = d.get(dataId);
			if (data == null) System.err.println("Warning: there is no dataId = " + dataId + " in " + section.getId() + "." + getId());
			else
				data.legoSet = s;
		}
		
	}
}
