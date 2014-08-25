package org.openepics.discs.exporters;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Linac;
import se.lu.esss.linaclego.Section;
import se.lu.esss.linaclego.elements.Bend;
import se.lu.esss.linaclego.elements.ControlPoint;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.DtlCell;
import se.lu.esss.linaclego.elements.Edge;
import se.lu.esss.linaclego.elements.FieldMap;
import se.lu.esss.linaclego.elements.Quad;
import se.lu.esss.linaclego.elements.RfGap;
import se.lu.esss.linaclego.elements.ThinSteering;

public class TraceWinExporter implements BLEVisitor {
	private String fileName;
	private PrintWriter pw;
	private boolean printIdInTraceWin;
	private boolean printControlPoints;
	private boolean insidePeriodicLattice;
	private Linac linac;
	
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
		insidePeriodicLattice = false;
		this.linac = linac;
		linac.accept(this);
		pw.println("END");
		pw.close();
		pw = null;
	}		
	
	@Override
	public void visit(Section section) {
		if (!section.isPeriodicLatticeSection() && insidePeriodicLattice)
		{
			println(null, "LATTICE_END");
		}
		println(null, "FREQ", linac.getBeamFrequency() * section.getRFHarmonic());
		if (section.isPeriodicLatticeSection())
		{
			println(null, "LATTICE", section.getCells().get(0).getNumBeamlineElements(), 0);
		}
		insidePeriodicLattice = section.isPeriodicLatticeSection();
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
			sb.append(id).append(":").append(space);
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
		println(drift.getEssId(), "DRIFT",
				fourPlaces.format(drift.getLength()),
				fourPlaces.format(drift.getApertureR()),
				fourPlaces.format(drift.getApertureY()));
	}

	@Override
	public void visit(Quad quad) {
		println(quad.getEssId(), "QUAD",
				quad.getLength(),quad.getFieldGradient(), quad.getApertureR());
	}

	@Override
	public void visit(RfGap rfGap) {
		println(rfGap.getEssId(),"GAP",
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
		println(bend.getEssId(), "BEND");/*, 
				bend.getTWBendAngleDeg(),
				bend.getRadOfCurvmm(),
				bend.getFieldIndex(),
				bend.getAperRadmm(),
				bend.getHVflag());*/
	}

	@Override
	public void visit(Edge edge) {
		println(edge.getEssId(), "EDGE");/*,
				edge.getPoleFaceAngleDeg(),
				edge.getRadOfCurvmm(),
				edge.getGapmm(),
				edge.getK1(),
				edge.getK2(),
				edge.getAperRadmm(),
				edge.getHVflag());	*/
	}
	
	@Override
	public void visit(ThinSteering thinSteering) {
		println(thinSteering.getEssId(), "THIN_STEERING");/*,
				thinSteering.getXkick(),
				thinSteering.getYkick(),
				thinSteering.getRmm(),
				thinSteering.getKickType());*/
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
		println(fieldMap.getEssId(), "FIELD_MAP",
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
		println(dtlCell.getEssId(), "DTL_CEL",
				dtlCell.getLength(),
				dtlCell.getQ1Lenght(),
				dtlCell.getQ2Lenght(),
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

/*
	@Override
	public void visit(DtlDriftTube dtlDriftTube) {
		println(dtlDriftTube.getEssId(), "DRIFT",
				fourPlaces.format(dtlDriftTube.getNoseConeUpLen()),
				fourPlaces.format(dtlDriftTube.getRadius()),
				0.0);
		println(null, "QUAD",
				dtlDriftTube.getQuadLen(),
				dtlDriftTube.getQuadGrad(),
				dtlDriftTube.getRadius());
		println(null, "DRIFT",
				fourPlaces.format(dtlDriftTube.getNoseConeDnLen()),
				fourPlaces.format(dtlDriftTube.getRadius()),
				0.0);
	}

	@Override
	public void visit(DtlRfGap dtlRfGap) {
		println(dtlRfGap.getEssId(), "DRIFT",
				fourPlaces.format(dtlRfGap.getLength() / 2.0),
				fourPlaces.format(dtlRfGap.getRadApermm()),
				0.0);
		println(null, "GAP", 
				dtlRfGap.getVoltsT(),
				dtlRfGap.getRfPhaseDeg(),
				dtlRfGap.getRadApermm(),
				dtlRfGap.getPhaseFlag(),
				dtlRfGap.getBetaS(),
				dtlRfGap.getTts(),
				dtlRfGap.getKtts(),
				dtlRfGap.getK2tts(),
				0.0,
				0.0);
		println(null, "DRIFT",
				fourPlaces.format(dtlRfGap.getLength() / 2.0),
				fourPlaces.format(dtlRfGap.getRadApermm()),
				0.0);
	}
*/
	@Override
	public void visit(ControlPoint controlPoint) {
		/*if (printControlPoints) {
			println(null, ";" + controlPoint.getName().replace(":", "-"),
				 "dxmm=" + Double.toString(controlPoint.getEndLocalPosVec()[0] * 1000.0),
				 "dymm=" + Double.toString(controlPoint.getEndLocalPosVec()[1] * 1000.0),
				 "dzmm=" + Double.toString(controlPoint.getEndLocalPosVec()[2] * 1000.0));
		}*/
	}
}
