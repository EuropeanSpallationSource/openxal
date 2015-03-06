package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;


@XmlType(name = "quad")
public class Quad extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitControlPoints(visitor);
	}
	
	public double getFieldGradient()
	{
		return getParameters().getDoubleValue("g");
	}
}
