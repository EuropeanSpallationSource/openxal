package se.lu.esss.linaclego;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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

    @XmlTransient
    protected URL source;
    
    @XmlTransient
    protected Map<String, FieldProfile> fieldProfiles = new HashMap<>();
    
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class LinacDesc {
	    @XmlElement(required = true)
	    protected Linac.LinacData linacData = new Linac.LinacData();
	   
	    @XmlElement(required = true)
	    protected List<Section> section = new ArrayList<Section>();
	    
	    @XmlTransient
	    protected Linac linac;
	    
	    public void beforeUnmarshal(Unmarshaller u, Object parent) {
			this.linac = (Linac)parent;
		}
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

	public List<ControlPoint> getControlPoints(String section, String cell, String slot, String ble) {
		// TODO lame implementation
		List<ControlPoint> ret = new ArrayList<>();
		for (ControlPointsSets cps : getControlPointSets())
			for (ControlPoint cp : cps.getControlPoints())
			{
				if (section.equals(cp.getSection()) && cell.equals(cp.getCell()) && slot.equals(cp.getSlot()) && ble.equals(cp.getBle()))
					ret.add(cp);
			}
		return ret;
	}

	public void setSource(URL source) {
		this.source = source;
	}
	
	public URL getSource() {
		return source;
	}

	public FieldProfile getFieldProfile(String fieldmapFile) {
		if (fieldProfiles.containsKey(fieldmapFile)) return fieldProfiles.get(fieldmapFile);
		
		try {
			JAXBContext context = JAXBContext.newInstance(FieldProfile.class);
			Unmarshaller um = context.createUnmarshaller();
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setXIncludeAware(true);
			spf.setNamespaceAware(true);
			spf.setValidating(true);

			XMLReader xr = spf.newSAXParser().getXMLReader();
			
			SAXSource fmsource = new SAXSource(xr, new InputSource(new URL(getSource(), fieldmapFile+".xml").toString()));
			FieldProfile fp = um.unmarshal(fmsource, FieldProfile.class).getValue();

			fieldProfiles.put(fieldmapFile, fp);
			return fp;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
