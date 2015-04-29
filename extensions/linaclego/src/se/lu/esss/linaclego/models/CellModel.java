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

import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.Parameters;
import se.lu.esss.linaclego.Slot;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "var",
    "slot"
})
@XmlRootElement(name = "cellModel")
public class CellModel {

    @XmlElement(required = true)
    protected List<Var> var;
    @XmlElement(required = true)
    protected List<Slot> slot= new ArrayList<Slot>();
    @XmlID
    @XmlAttribute(name = "id", required = true)
    protected String id;

    public List<Var> getVar() {
        if (var == null) {
            var = new ArrayList<Var>();
        }
        return this.var;
    }

    public List<Slot> getSlots() {
        return this.slot;
    }
    
    public String getId() {
        return id;
    }    
    
    public Cell apply(Parameters arguments)
    {
    	Cell cell = new Cell();
    	List<Slot> slotsout = cell.getSlots();
    	for (Slot s : slot) {
    		slotsout.add(s.apply(cell, arguments));
    	}
    	return cell;
    }
}
