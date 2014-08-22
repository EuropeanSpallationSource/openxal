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
public class LinacLego {

    @XmlAttribute(name = "revNo", required = true)
    protected String revNo;
    @XmlAttribute(name = "title", required = true)
    protected String title;
    @XmlAttribute(name = "comment", required = true)
    protected String comment;

    @XmlElement(required = true)
	protected Header header = new Header();

    @XmlElement(required = true)
	protected Linac linac = new Linac();

    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class Linac {
	    @XmlElement(required = true)
	    protected LinacLego.LinacData linacData;
	   
	    @XmlElement(required = true)
	    protected List<Section> section;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class Header {
	    @XmlElement(required = true)
	    protected List<LinacLego.SlotModels> slotModels;
	    
	    @XmlElement(required = true)
	    protected List<LinacLego.CellModels> cellModels;
	        
	    @XmlElement(required = true)
	    protected List<LinacLego.ControlPoints> controlPoints;
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

    public List<LinacLego.SlotModels> getSlotModels() {
        if (header.slotModels == null) {
            header.slotModels = new ArrayList<LinacLego.SlotModels>();
        }
        return this.header.slotModels;
    }

    public List<LinacLego.CellModels> getCellModels() {
        if (header.cellModels == null) {
            header.cellModels = new ArrayList<LinacLego.CellModels>();
        }
        return this.header.cellModels;
    }

    public List<LinacLego.ControlPoints> getControlPoints() {
        if (header.controlPoints == null) {
            header.controlPoints = new ArrayList<LinacLego.ControlPoints>();
        }
        return this.header.controlPoints;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cellModel"
    })
    public static class CellModels {

        protected List<CellModel> cellModel;
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<CellModel> getCellModel() {
            if (cellModel == null) {
                cellModel = new ArrayList<CellModel>();
            }
            return this.cellModel;
        }

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cnpt"
    })
    public static class ControlPoints {

        protected List<ControlPoint> cnpt;
        
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<ControlPoint> getCnpt() {
            if (cnpt == null) {
                cnpt = new ArrayList<ControlPoint>();
            }
            return this.cnpt;
        }

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "slotModel"
    })
    public static class SlotModels {

        protected List<SlotModel> slotModel;
        @XmlAttribute(name = "id", required = true)
        protected String id;

        public List<SlotModel> getSlotModel() {
            if (slotModel == null) {
                slotModel = new ArrayList<SlotModel>();
            }
            return this.slotModel;
        }

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }

    }

    public LinacLego.LinacData getLinacData() {
        return linac.linacData;
    }

    public void setLinacData(LinacLego.LinacData value) {
        this.linac.linacData = value;
    }

    public List<Section> getSection() {
        if (linac.section == null) {
            linac.section = new ArrayList<Section>();
        }
        return this.linac.section;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "d"
    })
    public static class LinacData {

        @XmlElement(required = true)
        protected Parameters d;

        public Parameters getD() {
            if (d == null) {
                d = new Parameters();
            }
            return this.d;
        }

    }
}
