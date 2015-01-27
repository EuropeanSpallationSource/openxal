/**
 * 
 */
package se.lu.esss.linaclego;

import se.lu.esss.linaclego.elements.Bend;
import se.lu.esss.linaclego.elements.ControlPoint;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.DtlCell;
import se.lu.esss.linaclego.elements.DtlDriftTube;
import se.lu.esss.linaclego.elements.DtlRfGap;
import se.lu.esss.linaclego.elements.Edge;
import se.lu.esss.linaclego.elements.FieldMap;
import se.lu.esss.linaclego.elements.Monitor;
import se.lu.esss.linaclego.elements.Quad;
import se.lu.esss.linaclego.elements.RfGap;
import se.lu.esss.linaclego.elements.ThinSteering;

/**
 * BeamLineElement visitor
 * 
 * @author Ivo List
 *
 */
public interface BLEVisitor {
	public void visit(Section section);
	public void visit(Cell cell);
	public void visit(Slot slot);
	
	public void visit(Drift drift);
	public void visit(Quad quad);
	public void visit(RfGap rfGap);
	public void visit(Bend bend);
	public void visit(ThinSteering thinSteering);
	//public void visit(Ncells ncells);
	public void visit(FieldMap fieldMap);
	public void visit(DtlRfGap dtlRfGap);
	public void visit(DtlDriftTube dtlDriftTube);
	public void visit(DtlCell dtlCell);
	public void visit(Edge edge);
	public void visit(ControlPoint controlPoint);
	public void visit(Monitor monitor);
}
