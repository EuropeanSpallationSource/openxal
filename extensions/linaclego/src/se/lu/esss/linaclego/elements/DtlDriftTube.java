package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "dtlDriftTube")
public class DtlDriftTube extends Quad {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		super.accept(visitor);
	}
	
	public double getNoseConeUpLength()
	{
		return getParameters().getDoubleValue("noseConeUpLen");
	}
	
	public double getNoseConeDnLength()
	{
		return getParameters().getDoubleValue("noseConeDnLen");
	}
	
	@Override
	public double getLength()
	{
		return getNoseConeUpLength() + getQuadLength() + getNoseConeDnLength();
	}
	
	public double getQuadLength()
	{
		return getParameters().getDoubleValue("quadLen");
	}
	
	@Override
	public double getFieldGradient()
	{
		return getParameters().getDoubleValue("quadGrad");
	}
	
	@Override
	public double getApertureR()
	{
		return getParameters().getDoubleValue("radius");
	}	
}
