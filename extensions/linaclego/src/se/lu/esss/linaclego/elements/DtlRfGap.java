package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "dtlRfGap")
public class DtlRfGap extends RfGap {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public double getLength()
	{
		return getParameters().getDoubleValue("length");
	}	
}
