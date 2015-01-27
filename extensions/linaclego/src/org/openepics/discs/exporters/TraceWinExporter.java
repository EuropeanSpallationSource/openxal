package org.openepics.discs.exporters;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.Linac;
import se.lu.esss.linaclego.Section;
import se.lu.esss.linaclego.Slot;
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
	private boolean printIdInTraceWin = true;
	private boolean printControlPoints = true;
	
	private Linac linac;
	private Section section;
	private Cell cell;
	private Slot slot;
	
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
		this.cell = cell;
	}
	
	public void visit(Slot slot)
	{
		this.slot = slot;
	}
	
	public void println(String id, String command, Object... params) 
	{
		printCommand(id, command);
		printParams(params);
		pw.println();
	}
	
	public void printCommand(String id, String command) 
	{
		StringBuilder sb = new StringBuilder();
		if (printIdInTraceWin && id != null) 
			sb.append(section.getId()).append('-').append(cell.getId()).append('-')
				.append(slot.getId()).append('-').append(id)
				.append(":").append(space);
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
		println(drift.getId(), "DRIFT",
				fourPlaces.format(drift.getLength()),
				fourPlaces.format(drift.getApertureR()),
				fourPlaces.format(drift.getApertureY()));
		visitControlPoints(drift.getId());
	}



	@Override
	public void visit(Quad quad) {
		println(quad.getId(), "QUAD",
				quad.getLength(),quad.getFieldGradient(), quad.getApertureR());
		visitControlPoints(quad.getId());
	}

	@Override
	public void visit(RfGap rfGap) {
		println(rfGap.getId(),"GAP",
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
		visitControlPoints(rfGap.getId());
	}

	@Override
	public void visit(Bend bend) {
		println(bend.getId(), "BEND", 
				bend.getBendAngle(),
				bend.getCurvatureRadius(),
				bend.getFieldIndex(),
				bend.getApertureR(),
				bend.getHVFlag());
		visitControlPoints(bend.getId());
	}

	@Override
	public void visit(Edge edge) {
		println(edge.getId(), "EDGE",
				edge.getPoleFaceRotationAngle(),
				edge.getCurvatureRadius(),
				edge.getGap(),
				edge.getK1(),
				edge.getK2(),
				edge.getApertureR(),
				edge.getHVFlag());
		visitControlPoints(edge.getId());
	}
	
	@Override
	public void visit(ThinSteering thinSteering) {
		println(thinSteering.getId(), "THIN_STEERING",
				thinSteering.getXKick(),
				thinSteering.getYKick(),
				thinSteering.getApertureR(),
				thinSteering.getKickType());
		visitControlPoints(thinSteering.getId());
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
		println(fieldMap.getId(), "FIELD_MAP",
				100,
				fourPlaces.format(fieldMap.getLength()),
				fourPlaces.format(fieldMap.getRFPhase()),
				fourPlaces.format(fieldMap.getApertureR()),
				0,
				fieldMap.getElectricFieldFactor(),
				0,
				0,
				fieldMap.getFieldmapFile().split("\\.")[0]);
		visitControlPoints(fieldMap.getId());
	}

	@Override
	public void visit(DtlCell dtlCell) {
		println(dtlCell.getId(), "DTL_CEL",
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
		visitControlPoints(dtlCell.getId());
	}


	@Override
	public void visit(DtlDriftTube dtlDriftTube) {
		println(dtlDriftTube.getId(), "DRIFT",
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
		visitControlPoints(dtlDriftTube.getId());
	}

	@Override
	public void visit(DtlRfGap dtlRfGap) {
		println(dtlRfGap.getId(), "DRIFT",
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
		visitControlPoints(dtlRfGap.getId());
	}

	
	private void visitControlPoints(String id) {
		for (ControlPoint cp : linac.getControlPoints(section.getId(), cell.getId(), slot.getId(), id))
		{
			visit(cp);
		}
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
		println(monitor.getId(), "DRIFT",
				fourPlaces.format(monitor.getLength()),
				fourPlaces.format(monitor.getApertureR()),
				fourPlaces.format(monitor.getApertureY()));
	}
	
	
	public static void main(String[] args) throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {
		if (args.length < 2) {
			System.out.println("Usage: TraceWinExporter <linaclego.xml> <tracewin.dat>");
			System.exit(-1);
		}
		
		JAXBContext context = JAXBContext.newInstance(Linac.class, Drift.class, Quad.class);
		Unmarshaller um = context.createUnmarshaller();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setXIncludeAware(true);
		spf.setNamespaceAware(true);
		spf.setValidating(true);

		XMLReader xr = spf.newSAXParser().getXMLReader();
		SAXSource source = new SAXSource(xr, new InputSource(args[0]));
		Linac ll = um.unmarshal(source, Linac.class).getValue();
		
		TraceWinExporter twe = new TraceWinExporter(args[1]);
		twe.export(ll);
	}
}
