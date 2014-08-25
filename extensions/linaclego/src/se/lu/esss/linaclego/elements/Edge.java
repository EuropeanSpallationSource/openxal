package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "edge")
public class Edge extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
	}
}
