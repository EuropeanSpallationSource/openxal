package org.openepics.discs.exporters;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.Linac;
import se.lu.esss.linaclego.LinacLego;
import se.lu.esss.linaclego.Section;
import se.lu.esss.linaclego.Slot;
import se.lu.esss.linaclego.elements.BeamlineElement;
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

public class TraceWinExporter implements BLEVisitor {
	private String fileName;
	private PrintWriter pw;
	private boolean printIdInTraceWin = false;
	private boolean printControlPoints = true;
	
	private Linac linac;
	private Section section;
	
	public static final String space = "\t";
	public static final String newline = System.getProperty("line.separator");
	public static final DecimalFormat zeroPlaces = new DecimalFormat("###", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public static final DecimalFormat twoPlaces = new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public static final DecimalFormat fourPlaces = new DecimalFormat("###.####", DecimalFormatSymbols.getInstance(Locale.ROOT));
	
	public TraceWinExporter(String fileName) {
		this.fileName = fileName;
	}
	
	public void export(Linac linac) throws FileNotFoundException {
		pw = new PrintWriter(fileName);
		pw.println(";" + linac.getTitle());
		//printIdInTraceWin = linacLego.isPrintIdInTraceWin();
		//printControlPoints = linacLego.isPrintControlPoints();

		this.linac = linac;
		linac.accept(this);
		pw.println("END");
		pw.close();
		pw = null;
	}		
	
	@Override
	public void visit(Section section) {
		if (!section.isPeriodicLatticeSection() && this.section != null && this.section.isPeriodicLatticeSection())
		{
			println(null, "LATTICE_END");
		}
		println(null, "FREQ", linac.getBeamFrequency() * section.getRFHarmonic());
		if (section.isPeriodicLatticeSection())
		{
			println(null, "LATTICE", section.getCells().get(0).getNumBeamlineElements(), 0);
		}
		this.section = section;
	}
	
	
	public void visit(Cell cell)
	{
	}
	
	public void visit(Slot slot)
	{
	}
	
	public void println(BeamlineElement ble, String command, Object... params) 
	{
		printCommand(ble, command);
		printParams(params);
		pw.println();
	}
	
	public void printCommand(BeamlineElement ble, String command) 
	{
		StringBuilder sb = new StringBuilder();
		if (printIdInTraceWin && ble != null && ble.getId() != null) { 
			Slot slot = ble.getParent();
			Cell cell = slot.getParent();
			Section section = cell.getParent();
		
			sb.append(section.getId()).append('-').append(cell.getId()).append('-')
				.append(slot.getId()).append('-').append(ble.getId())
				.append(":").append(space);
		}
		sb.append(command);
		pw.print(sb);
	}
	
	public void printParams(Object... params) 
	{
		StringBuilder sb = new StringBuilder();
		for (Object param : params) {
			sb.append(space).append(param.toString());
		}
		pw.print(sb);
	}

	@Override
	public void visit(Drift drift) {
		println(drift, "DRIFT",
				fourPlaces.format(drift.getLength()),
				fourPlaces.format(drift.getApertureR()),
				fourPlaces.format(drift.getApertureY()));
	}



	@Override
	public void visit(Quad quad) {
		println(quad, "QUAD",
				quad.getLength(),quad.getFieldGradient(), quad.getApertureR());
	}

	@Override
	public void visit(RfGap rfGap) {
		println(rfGap,"GAP",
				rfGap.getVoltage(),
				rfGap.getRFPhase(),
				rfGap.getApertureR(),
				rfGap.getPhaseFlag(),
				rfGap.getBetaS(),
				rfGap.getTTF().getTs(),
				rfGap.getTTF().getKTs(),
				rfGap.getTTF().getK2Ts(),
				rfGap.getTTF().getKS(),
				rfGap.getTTF().getK2S());
	}

	@Override
	public void visit(Bend bend) {
		println(bend, "BEND", 
				bend.getBendAngle(),
				bend.getCurvatureRadius(),
				bend.getFieldIndex(),
				bend.getApertureR(),
				bend.getHVFlag());
	}

	@Override
	public void visit(Edge edge) {
		println(edge, "EDGE",
				edge.getPoleFaceRotationAngle(),
				edge.getCurvatureRadius(),
				edge.getGap(),
				edge.getK1(),
				edge.getK2(),
				edge.getApertureR(),
				edge.getHVFlag());
	}
	
	@Override
	public void visit(ThinSteering thinSteering) {
		println(thinSteering, "THIN_STEERING",
				thinSteering.getXKick(),
				thinSteering.getYKick(),
				thinSteering.getApertureR(),
				thinSteering.getKickType());
	}
/*
	@Override
	public void visit(Ncells ncells) {
		printCommand(ncells.getEssId(), "NCELLS");
		printParams(ncells.getMode(),
				ncells.getNcells(),
				fourPlaces.format(ncells.getBetag()),
				zeroPlaces.format(ncells.getE0tRef()),
				twoPlaces.format(ncells.getPhiTWdeg()),
				twoPlaces.format(ncells.getRadius()),
				ncells.getPhaseFlag(),
				ncells.getKe0t()[0],
				ncells.getKe0t()[2],
				ncells.getDz()[0] * 1000.0,
				ncells.getDz()[2] * 1000.0);
		if (ncells.isTtInfo())
		{
			printParams(
				fourPlaces.format(ncells.getBetaRef()),
				fourPlaces.format(ncells.getTtRef()[1]),
				fourPlaces.format(-ncells.getKttRef()[1]),
				fourPlaces.format(-ncells.getK2ttRef()[1]),
				fourPlaces.format(ncells.getTtRef()[0]),
				fourPlaces.format(-ncells.getKttRef()[0]),
				fourPlaces.format(-ncells.getK2ttRef()[0]),
				fourPlaces.format(ncells.getTtRef()[2]),
				fourPlaces.format(-ncells.getKttRef()[2]),
				fourPlaces.format(-ncells.getK2ttRef()[2]));
		}
		pw.println();
	}
*/
	@Override
	public void visit(FieldMap fieldMap) {
		println(fieldMap, "FIELD_MAP",
				100,
				fourPlaces.format(fieldMap.getLength()),
				fourPlaces.format(fieldMap.getRFPhase()),
				fourPlaces.format(fieldMap.getApertureR()),
				0,
				fieldMap.getElectricFieldFactor(),
				0,
				0,
				fieldMap.getFieldmapFile().split("\\.")[0]);
	}

	@Override
	public void visit(DtlCell dtlCell) {
		println(dtlCell, "DTL_CEL",
				dtlCell.getLength(),
				dtlCell.getQ1Length(),
				dtlCell.getQ2Length(),
				dtlCell.getCellCenter(),
				dtlCell.getQ1FieldGradient(),
				dtlCell.getQ2FieldGradient(),
				dtlCell.getVoltage() * dtlCell.getVoltageMult(),
				dtlCell.getRFPhase() + dtlCell.getRFPhaseAdd(),
				dtlCell.getApertureR(),
				dtlCell.getPhaseFlag(),
				dtlCell.getBetaS(),
				dtlCell.getTTF().getTs(),
				dtlCell.getTTF().getKTs(),
				dtlCell.getTTF().getK2Ts());
	}


	@Override
	public void visit(DtlDriftTube dtlDriftTube) {
		println(dtlDriftTube, "DRIFT",
				fourPlaces.format(dtlDriftTube.getNoseConeUpLength()),
				fourPlaces.format(dtlDriftTube.getApertureR()),
				0.0);
		println(null, "QUAD",
				dtlDriftTube.getQuadLength(),
				dtlDriftTube.getFieldGradient(),
				dtlDriftTube.getApertureR());
		println(null, "DRIFT",
				fourPlaces.format(dtlDriftTube.getNoseConeDnLength()),
				fourPlaces.format(dtlDriftTube.getApertureR()),
				0.0);
	}

	@Override
	public void visit(DtlRfGap dtlRfGap) {
		println(dtlRfGap, "DRIFT",
				fourPlaces.format(dtlRfGap.getLength() / 2.0),
				fourPlaces.format(dtlRfGap.getApertureR()),
				0.0);
		println(null, "GAP", 
				dtlRfGap.getVoltage(),
				dtlRfGap.getRFPhase(),
				dtlRfGap.getApertureR(),
				dtlRfGap.getPhaseFlag(),
				dtlRfGap.getBetaS(),
				dtlRfGap.getTTF().getTs(),
				dtlRfGap.getTTF().getKTs(),
				dtlRfGap.getTTF().getK2Ts(),
				0.0,
				0.0);
		println(null, "DRIFT",
				fourPlaces.format(dtlRfGap.getLength() / 2.0),
				fourPlaces.format(dtlRfGap.getApertureR()),
				0.0);
	}

	@Override
	public void visit(ControlPoint controlPoint) {
		if (printControlPoints) {
			println(null, ";" + controlPoint.getDevName().replace(":", "-"),
				 "dxmm=" + Double.toString(controlPoint.getPosition()[0] * 1000.0),
				 "dymm=" + Double.toString(controlPoint.getPosition()[1] * 1000.0),
				 "dzmm=" + Double.toString(controlPoint.getPosition()[2] * 1000.0));
		}
	}
	
	@Override
	public void visit(Monitor monitor) {
		//println(null, ";" + monitor.getMonitorType());
		println(monitor, "DRIFT",
				fourPlaces.format(monitor.getLength()),
				fourPlaces.format(monitor.getApertureR()),
				fourPlaces.format(monitor.getApertureY()));
	}
	
	
	public static void main(String[] args) throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException, MalformedURLException {
		if (args.length < 2) {
			System.out.println("Usage: TraceWinExporter <linaclego.xml> <tracewin.dat>");
			System.exit(-1);
		}
		
		Linac ll = LinacLego.load(args[0]);
		
		TraceWinExporter twe = new TraceWinExporter(args[1]);
		twe.export(ll);
	}
}
