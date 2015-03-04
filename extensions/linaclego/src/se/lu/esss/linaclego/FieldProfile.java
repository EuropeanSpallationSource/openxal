package se.lu.esss.linaclego;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fieldProfile")
public class FieldProfile {
    @XmlElement(name = "d", required = true)
    protected Parameters parameters = new Parameters();
    @XmlAttribute(name = "title", required = true)
    protected String title;
    @XmlAttribute(name = "storedEnergy", required = true)
    protected double storedEnergy;
    @XmlAttribute(name = "length", required = true)
    protected double length;
    @XmlAttribute(name = "lengthUnit", required = true)
    protected String lengthUnit;
    @XmlAttribute(name = "storedEnergyUnit", required = true)
    protected String storedEnergyUnit;
    @XmlAttribute(name = "fieldUnit", required = true)
    protected String fieldUnit;
    
    
	@XmlTransient
	double[] field;
	
	public double[] getField() {
		if (field != null) return field;
		double[] field;
		int N = 0;
		for (Parameters.D d : parameters) {
			try {
				int i = Integer.parseInt(d.getId());
				if (i > N) N = i;
			} catch (NumberFormatException e) {
			}
		}
		N++;
		
		field = new double[N];
		for (int i = 0; i<N; i++)
			field[i] = parameters.getDoubleValue(Integer.toString(i));
	
		this.field = field;
		return field;
	}
	
	public double getLength() {
		return length;
	}
}
