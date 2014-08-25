package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.elements.ControlPoint;
import se.lu.esss.linaclego.models.CellModel;
import se.lu.esss.linaclego.models.SlotModel;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "header",
    "linac"
})
@XmlRootElement(name = "linacLego")
public class Linac {

    @XmlAttribute(name = "revNo", required = true)
    protected String revNo;
    @XmlAttribute(name = "title", required = true)
    protected String title;
    @XmlAttribute(name = "comment", required = true)
    protected String comment;

    @XmlElement(required = true)
	protected Header header = new Header();

    @XmlElement(required = true)
	protected LinacDesc linac = new LinacDesc();

    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class LinacDesc {
	    @XmlElement(required = true)
	    protected Linac.LinacData linacData = new Linac.LinacData();
	   
	    @XmlElement(required = true)
	    protected List<Section> section = new ArrayList<Section>();
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class Header {
	    @XmlElement(name="slotModels", required = true)
	    protected List<Linac.SlotModelsSets> slotModelsSets = new ArrayList<Linac.SlotModelsSets>();
	    
	    @XmlElement(name = "cellModels", required = true)
	    protected List<Linac.CellModelsSet> cellModelsSets = new ArrayList<Linac.CellModelsSet>();
	        
	    @XmlElement(name = "controlPoints", required = true)
	    protected List<Linac.ControlPointsSets> controlPointsSets = new ArrayList<Linac.ControlPointsSets>();
    }
    
    public String getRevNo() {
        return revNo;
    }

    public void setRevNo(String value) {
        this.revNo = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        this.comment = value;
    }

    public List<Linac.SlotModelsSets> getSlotModelSets() {
        return this.header.slotModelsSets;
    }

    public List<Linac.CellModelsSet> getCellModelSets() {
        return this.header.cellModelsSets;
    }

    public List<Linac.ControlPointsSets> getControlPointSets() {
        return this.header.controlPointsSets;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cellModel"
    })
    public static class CellModelsSet {

        protected List<CellModel> cellModel = new ArrayList<CellModel>();
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<CellModel> getCellModels() {
            return this.cellModel;
        }

        public String getId() {
            return id;
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cnpt"
    })
    public static class ControlPointsSets {

        protected List<ControlPoint> cnpt = new ArrayList<ControlPoint>();
        
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<ControlPoint> getControlPoints() {
            return this.cnpt;
        }

        public String getId() {
            return id;
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "slotModel"
    })
    public static class SlotModelsSets {

        protected List<SlotModel> slotModel = new ArrayList<SlotModel>();
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<SlotModel> getSlotModels() {
            return this.slotModel;
        }

        public String getId() {
            return id;
        }
    }

    public Parameters getLinacData() {
        return linac.linacData.d;
    }

    public List<Section> getSections() {
        return this.linac.section;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "d"
    })
    public static class LinacData {

        @XmlElement(required = true)
        protected Parameters d = new Parameters();
    }
    
    public void accept(BLEVisitor visitor)
    {
    	for (Section s: getSections()) s.accept(visitor);
    }

	public double getBeamFrequency() {
		return getLinacData().getDoubleValue("beamFrequency");
	}
}
