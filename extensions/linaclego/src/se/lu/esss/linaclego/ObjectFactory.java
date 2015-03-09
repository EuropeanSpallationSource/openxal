//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.22 at 09:29:49 AM CEST 
//


package se.lu.esss.linaclego;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;

import se.lu.esss.linaclego.elements.BeamlineElement;
import se.lu.esss.linaclego.elements.LegoMonitor;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.Quad;
import se.lu.esss.linaclego.models.CellModel;
import se.lu.esss.linaclego.models.SlotModel;
import se.lu.esss.linaclego.models.Var;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.lu.esss.linaclego package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.lu.esss.linaclego
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Linac }
     * 
     */
    public Linac createLinacLego() {
        return new Linac();
    }

    /**
     * Create an instance of {@link LegoMonitor }
     * 
     */
    public LegoMonitor createLegoMonitor() {
        return new LegoMonitor();
    }

    /**
     * Create an instance of {@link D }
     * 
     */
    public Parameters.D createD() {
        return new Parameters.D();
    }

    /**
     * Create an instance of {@link BeamlineElement }
     * 
     */
    /*public BeamlineElement createBle() {
        return new BeamlineElement();
    }*/

    public Drift createDrift()
    {
    	return new Drift();
    }
    
    public Quad createQuad()
    {
    	return new Quad();
    }
    
    /**
     * Create an instance of {@link SlotModel }
     * 
     */
    public SlotModel createSlotModel() {
        return new SlotModel();
    }

    /**
     * Create an instance of {@link Var }
     * 
     */
    public Var createVar() {
        return new Var();
    }

    /**
     * Create an instance of {@link CellModel }
     * 
     */
    public CellModel createCellModel() {
        return new CellModel();
    }

    /**
     * Create an instance of {@link Slot }
     * 
     */
    public Slot createSlot() {
        return new Slot();
    }

    /**
     * Create an instance of {@link Cell }
     * 
     */
    public Cell createCell() {
        return new Cell();
    }

    /**
     * Create an instance of {@link Section }
     * 
     */
    public Section createSection() {
        return new Section();
    }
    
    /**
     * Create an instance of {@link LegoSet}
     * 
     */
    public LegoSet createLegoSet() {
        return new LegoSet();
    }
    
    @XmlElementDecl(name="legoSet")
    public JAXBElement<LegoSet> createLegoSet(LegoSet value) {
        return new JAXBElement<LegoSet>(null, LegoSet.class, value);
    }
}
