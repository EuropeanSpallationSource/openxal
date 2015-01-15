package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "fieldMap")
public class FieldMap extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
	}
	
	public double getRFPhase()
	{
		return getParameters().getDoubleValue("rfpdeg");
	}
	
	@Override
	public double getLength()
	{
		return getParameters().getDoubleValue("lengthmm");
	}
	
	@Override
	public double getApertureR()
	{
		return getParameters().getDoubleValue("radiusmm");
	}
	
	public double getElectricFieldFactor()
	{
		return getParameters().getDoubleValue("xelmax");
	}
	
	public String getFieldmapFile()
	{
		return getParameters().get("file").getValue();
	}
	
}
