package org.openepics.discs.exporters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.lu.esss.ics.jels.smf.impl.ESSBend;
import se.lu.esss.ics.jels.smf.impl.ESSElementFactory;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.FieldProfile;
import se.lu.esss.linaclego.Linac;
import se.lu.esss.linaclego.LinacLego;
import se.lu.esss.linaclego.Parameters;
import se.lu.esss.linaclego.Section;
import se.lu.esss.linaclego.Slot;
import se.lu.esss.linaclego.elements.BeamlineElement;
import se.lu.esss.linaclego.elements.Bend;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.DtlCell;
import se.lu.esss.linaclego.elements.DtlDriftTube;
import se.lu.esss.linaclego.elements.DtlRfGap;
import se.lu.esss.linaclego.elements.Edge;
import se.lu.esss.linaclego.elements.FieldMap;
import se.lu.esss.linaclego.elements.LegoMonitor;
import se.lu.esss.linaclego.elements.Monitor;
import se.lu.esss.linaclego.elements.Quad;
import se.lu.esss.linaclego.elements.RfGap;
import se.lu.esss.linaclego.elements.ThinSteering;
import xal.model.probe.EnvelopeProbe;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.attr.SequenceBucket;
import xal.smf.impl.DipoleCorr;
import xal.smf.impl.ElementFactory;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Magnet;
import xal.smf.impl.MagnetPowerSupply;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlWriter;

public class OpenXALExporter implements BLEVisitor {	
	private double acceleratorPosition;
	private double sectionPosition;
	public static final double beta_gamma_Er_by_e0_c = -9.302773635653585;
	
	private Linac linac;
	private Section section;
	
	private Accelerator accelerator;
	private AcceleratorSeq currentSequence;

	private Map<String, se.lu.esss.ics.jels.smf.impl.FieldProfile> fieldProfiles = new HashMap<>();
	
	protected OpenXALExporter() 
	{
		
	}
	
	public static Accelerator convert(Linac linac) {
		return convertFull(linac).accelerator;		
	}
		
	
	public static OpenXALExporter convertFull(Linac linac) {
		final OpenXALExporter exporter = new OpenXALExporter();
		
		exporter.acceleratorPosition = 0.0;
		exporter.sectionPosition = 0.0;
		
		exporter.linac = linac;
		
		exporter.accelerator = new Accelerator("ESS") {			
			public void write(DataAdaptor adaptor) {
				super.write(adaptor);
				// write out power supplies
				DataAdaptor powerSuppliesAdaptor = adaptor.createChild("powersupplies");				 
				for ( MagnetPowerSupply mps : exporter.accelerator.getMagnetMainSupplies()) {
					mps.write( powerSuppliesAdaptor.createChild("ps"));
				}			 
			 }
		};
		linac.accept(exporter);
		exporter.accelerator.setLength(exporter.acceleratorPosition + exporter.sectionPosition);

		return  exporter;
	}
	
	public static void export(Linac linac, String fileName) throws IOException, URISyntaxException {
		System.out.println("Converting linac lego to openxal.");
		OpenXALExporter exporter = convertFull(linac);
		Accelerator accelerator = exporter.accelerator;

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();

		Document document = da.document();
		document.setDocumentURI(new File(fileName).toURI().toString());
	
		da.writeNode(accelerator);
		cleanup(document);
	
		Element root = document.getDocumentElement();
		
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
					    "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/xdxf.xsd?format=raw");

		System.out.printf("Writing output to: %s\n", fileName);
		XmlWriter.writeToFile(document, new File(fileName));
		
		for (Entry<String, se.lu.esss.ics.jels.smf.impl.FieldProfile> e : exporter.fieldProfiles.entrySet()) {
			String destinationFile = new URL(new URL(document.getDocumentURI()), e.getKey()+".edz").toString();
			System.out.printf("Saving fieldmap %s to: %s\n", e.getKey(), destinationFile);
			e.getValue().saveFile(destinationFile);
		}
		
	}
	
	public static void export(String sourceFileName, String destinationFilename) throws IOException, URISyntaxException, SAXException, ParserConfigurationException, JAXBException {
		System.out.printf("Loading linac lego: %s\n", sourceFileName);
		Linac ll = LinacLego.load(sourceFileName);		
		export(ll, destinationFilename);
	}
	
	private void add(AcceleratorNode node)
	{
		currentSequence.addNode(node);
		double length = node.getLength();					
		if (node instanceof Magnet) {
			if (node instanceof xal.smf.impl.Bend)
				length = ((xal.smf.impl.Bend) node).getDfltPathLength();
			else if (!(node instanceof HDipoleCorr) && !(node instanceof VDipoleCorr))
				length = ((Magnet) node).getEffLength();
		} 
		sectionPosition += length;
	}
	
	/**
	 * Cleans up XML OpenXal produces
	 * @param parent node to clean
	 */
	private static void cleanup(Node parent) {			
		NodeList children = parent.getChildNodes();
		NamedNodeMap attrs = parent.getAttributes();
		if (attrs != null) {
			// unneeded attributes 
			if (attrs.getNamedItem("s") != null) attrs.removeNamedItem("s");
			if (attrs.getNamedItem("pid") != null) attrs.removeNamedItem("pid");
			if (attrs.getNamedItem("status") != null) attrs.removeNamedItem("status");
			if (attrs.getNamedItem("eid") != null) attrs.removeNamedItem("eid");

			// remove type="sequence" on sequences - import doesn't work otherwise
			if ("sequence".equals(parent.getNodeName()) && attrs.getNamedItem("type") != null && "sequence".equals(attrs.getNamedItem("type").getNodeValue())) 
				attrs.removeNamedItem("type");
			
			if ("xdxf".equals(parent.getNodeName())) {
				attrs.removeNamedItem("id");
				attrs.removeNamedItem("len");
				attrs.removeNamedItem("pos");
				attrs.removeNamedItem("type");
			}
		}
		
		for (int i = 0; i<children.getLength(); )
		{
			Node child = children.item(i);			
			attrs = child.getAttributes();
			
			if ("align".equals(child.getNodeName()) || "twiss".equals(child.getNodeName())) 
				// remove twiss and align - not needed
				parent.removeChild(child);
			else if ("channelsuite".equals(child.getNodeName()) && !child.hasChildNodes()) {
				parent.removeChild(child);
			}
			else if ("aperture".equals(child.getNodeName()) && "0.0".equals(attrs.getNamedItem("x").getNodeValue())) 
				// remove empty apertures
				parent.removeChild(child);
			else {			
				cleanup(child);				
				// remove empty attributes
				if ("attributes".equals(child.getNodeName()) && child.getChildNodes().getLength()==0)
				{
					parent.removeChild(child);
				} else
					i++;
			}
		}	
	}
	
	private static ApertureBucket generateApertureBucket(BeamlineElement element) {
		ApertureBucket aper = new ApertureBucket();
		aper.setAperX(element.getApertureR());
		if (element.getApertureY() != 0.) aper.setAperY(element.getApertureY());		
		/*int apertureCode = element.getApertureType() == null ? 2 : element.getApertureType().getIntegerValue();
		aper.setShape(toOpenXALApertureCode(apertureCode));		*/
		return aper;
	}
	
	
	@Override
	public void visit(Drift drift) {
		sectionPosition += drift.getLength()*1e-3;
	}

	@Override
	public void visit(final Quad iquad) {
		ApertureBucket aper = generateApertureBucket(iquad);
		double L = iquad.getLength()*1e-3;		
		double G = iquad.getFieldGradient();
		
		add(ElementFactory.createQuadrupole(iquad.getEssId(), L, G, aper, accelerator, sectionPosition+L/2));
	}

	@Override
	public void visit(final RfGap rfGap) {
		double E0TL = rfGap.getVoltage();
		double Phis = rfGap.getRFPhase();	
		double betas = rfGap.getBetaS();
		double Ts = rfGap.getTTF().getTs();
		double kTs = rfGap.getTTF().getKTs();
		double k2Ts = rfGap.getTTF().getK2Ts();
		double kS = rfGap.getTTF().getKS();
		double k2S = rfGap.getTTF().getK2S();		

		ApertureBucket aper = generateApertureBucket(rfGap);

		xal.smf.impl.RfGap gap = ElementFactory.createRfGap(rfGap.getEssId()+":G", true, 1, aper, 1, 0);
		
		ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(rfGap.getEssId(), 0, gap, Phis, E0TL*1e-6,
				rfGap.getFrequency(), sectionPosition);
		
		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_startCoefs(new double[] {});
		} else {				
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		
		
		add(cavity);
	}

	@Override
	public void visit(final Bend ibend) {
		double alpha_deg = ibend.getBendAngle();
		double rho = ibend.getCurvatureRadius() * 1e-3;
		double entry_angle_deg = Math.abs(alpha_deg / 2.); //ibend.getEntranceAngle();
		double exit_angle_deg = Math.abs(alpha_deg / 2.); //ibend.getExitAngle();
		double G = 0. * 1e-3;//ibend.getGap();
		
		// TODO put those values into the database
		//double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;
						
		// calculations		
		double len = Math.abs(rho*alpha_deg * Math.PI/180.0);
		
	    double k = beta_gamma_Er_by_e0_c;
	    
	    ApertureBucket aper = generateApertureBucket(ibend);
	    
	    ESSBend bend = ESSElementFactory.createESSBend(ibend.getEssId(), alpha_deg, k, rho, entry_angle_deg,
	    		exit_angle_deg, aper, accelerator, MagnetType.VERTICAL, G, sectionPosition + len*0.5);
		
		add(bend);		
	}

	@Override
	public void visit(final ThinSteering thinSteering) {
		double L = thinSteering.getLength()*1e-3;
		
		final String essId = thinSteering.getEssId();
		ApertureBucket aper = generateApertureBucket(thinSteering);
		
		DipoleCorr vcorr = ElementFactory.createCorrector(essId+"-VC", MagnetType.VERTICAL, L, aper, accelerator,
				sectionPosition + L/2);
		add(vcorr);
		DipoleCorr hcorr = ElementFactory.createCorrector(essId+"-HC", MagnetType.HORIZONTAL, L, aper, accelerator,
				sectionPosition + L/2);
		add(hcorr);
	}
/*
	@Override
	public void visit(final NCells ncells) {
		double frequency = ncells.getSection().getRfFreqMHz();
		
		double Phis = ncells.getPhiSynchCalc();
		double E0T = ncells.getE0tRef();
		double betas = ncells.getBetaRef();
		
		double Ts = ncells.getTtRef()[1];
		double kTs = ncells.getKttRef()[1];
		double k2Ts = ncells.getK2ttRef()[1];		
		
		double Ti = ncells.getTtRef()[0];
		double kTi = ncells.getKttRef()[0];
		double k2Ti = ncells.getK2ttRef()[0];
		
		double To = ncells.getTtRef()[2];
		double kTo = ncells.getKttRef()[2];
		double k2To = ncells.getK2ttRef()[2];
		
		double betag = ncells.getBetag();
		double kE0Ti = ncells.getKe0t()[0];
		double kE0To = ncells.getKe0t()[2];
		double dzi = ncells.getDz()[0];
		double dzo = ncells.getDz()[2];
		
		int n = ncells.getNcells();
		int m = ncells.getMode();
		
		ESSRfCavity cavity = new ESSRfCavity(ncells.getEssId())
		addRFCavityChannels(ncells.getEssId(), cavity.channelSuite());
			
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(E0T * 1e-6);
		cavity.getRfField().setFrequency(frequency * 1e-6);	

		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTF_startCoefs(new double[] {});
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		} else {
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ti, kTi, k2Ti});
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, To, kTo, k2To});			
		}		

		
		// setup		
		ESSRfGap firstgap = new ESSRfGap(ncells.getEssId()+":G0");
		
		double lambda = IElement.LightSpeed/frequency;
		double Lc0,Lc,Lcn;
		double amp0,ampn;
		double pos0, posn;
		
		amp0 = (1+kE0Ti)*(Ti/Ts);		
		ampn = (1+kE0To)*(To/Ts);
		if (m==0) {
			Lc = Lc0 = Lcn = betag * lambda;
			pos0 = 0.5*Lc0 + dzi;
			posn = Lc0 + (n-2)*Lc + 0.5*Lcn + dzo;			
		} else if (m==1) {
			Lc = Lc0 = Lcn = 0.5 * betag * lambda;
			pos0 = 0.5*Lc0 + dzi;
			posn = Lc0 + (n-2)*Lc + 0.5*Lcn + dzo;
			cavity.getRfField().setStructureMode(1);
		} else { //m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;			
			pos0 = 0.25 * betag * lambda + dzi;
			posn = Lc0 + (n-2)*Lc + 0.5 * betag * lambda + dzo;
		}
						
		firstgap.setFirstGap(true); // this uses only phase for calculations
		firstgap.getRfGap().setEndCell(0);
		firstgap.setLength(0); // used only for positioning
		firstgap.setPosition(pos0);
		
		// following are used to calculate E0TL		
		firstgap.getRfGap().setLength(Lc0); 		
		firstgap.getRfGap().setAmpFactor(amp0);
		firstgap.getRfGap().setTTF(1);
		
		cavity.addNode(firstgap);
				
		for (int i = 1; i<n-1; i++) {
			ESSRfGap gap = new ESSRfGap(ncells.getEssId()+":G"+i);
			gap.getRfGap().setTTF(1);
			gap.setPosition(Lc0 + (i-0.5)*Lc);
			gap.setLength(0);
			gap.getRfGap().setLength(Lc);
			gap.getRfGap().setAmpFactor(1.0);
			cavity.addNode(gap);
		}
		
		ESSRfGap lastgap = new ESSRfGap(ncells.getEssId()+":G"+(n-1));
		lastgap.getRfGap().setEndCell(1);
		lastgap.setLength(0); // used only for positioning
		lastgap.setPosition(posn);
		
		// following are used to calculate E0TL		
		lastgap.getRfGap().setLength(Lcn); 		
		lastgap.getRfGap().setAmpFactor(ampn);
		lastgap.getRfGap().setTTF(1);
		cavity.addNode(lastgap);		
				
		cavity.setLength(Lc0+(n-2)*Lc+Lcn);
		cavity.setPosition(sectionPosition);		
		add(cavity);		
	}*/

	@Override
	public void visit(FieldMap fieldMap) {
		ApertureBucket aper = generateApertureBucket(fieldMap);
		double L = fieldMap.getLength()*1e-3;
		FieldProfile fp = fieldMap.getFieldProfile();
		final se.lu.esss.ics.jels.smf.impl.FieldProfile fp2 = new se.lu.esss.ics.jels.smf.impl.FieldProfile(fp.getLength()*1e-3, fp.getField());
		fieldProfiles.put(fieldMap.getFieldmapFile(),  fp2);
		ESSFieldMap fm = ESSElementFactory.createESSFieldMap(fieldMap.getEssId(), L, fieldMap.getFrequency(),
				fieldMap.getElectricFieldFactor(), fieldMap.getRFPhase(), fieldMap.getFieldmapFile(), fp2, aper, sectionPosition + L/2);
		add(fm);
	}

	
	@Override
	public void visit(final DtlRfGap dtlRfGap) {
		double E0TL = dtlRfGap.getVoltage() * dtlRfGap.getVoltageMult();
		double Phis = dtlRfGap.getRFPhase() + dtlRfGap.getRFPhaseAdd();
		double betas = dtlRfGap.getBetaS();
		double Ts = dtlRfGap.getTTF().getTs();
		double kTs = dtlRfGap.getTTF().getKTs();
		double k2Ts = dtlRfGap.getTTF().getK2Ts();
		double kS = dtlRfGap.getTTF().getKS();
		double k2S = dtlRfGap.getTTF().getK2S();		
		double length = dtlRfGap.getLength()*1e-3; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		ApertureBucket aper = generateApertureBucket(dtlRfGap);		

		// setup		
		xal.smf.impl.RfGap gap = ElementFactory.createRfGap(dtlRfGap.getEssId()+":G", true, 1, aper, length, length/2);
		
		ESSRfCavity cavity = ESSElementFactory.createESSRfCavity(dtlRfGap.getEssId(), length, gap, Phis, E0TL*1e-6/length,
				dtlRfGap.getFrequency(), sectionPosition);
		
		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_startCoefs(new double[] {});
		} else {				
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		

		add(cavity);
	}

	@Override
	public void visit(final DtlDriftTube dtlDriftTube) {
		double L1 = dtlDriftTube.getNoseConeUpLength() * 1e-3;
		double Lq = dtlDriftTube.getQuadLength()*1e-3;
		double L2 = dtlDriftTube.getNoseConeDnLength() * 1e-3;
		double G = dtlDriftTube.getFieldGradient();
		
		final String essId = dtlDriftTube.getEssId();
		ApertureBucket aper = generateApertureBucket(dtlDriftTube);
		
		xal.smf.impl.Quadrupole quad = ElementFactory.createQuadrupole(essId+":Q", Lq, G, aper, accelerator, L1+Lq/2.);
		
		AcceleratorSeq dt = new AcceleratorSeq(essId);
		dt.addNode(quad);
		dt.setLength(L1+Lq+L2);
		dt.setPosition(sectionPosition);
		add(dt);
	}

	
	@Override
	public void visit(final DtlCell dtlCell) {
		// mm -> m
		double L = dtlCell.getLength()*1e-3;
		double Lq1 = dtlCell.getQ1Length()*1e-3;
		double Lq2 = dtlCell.getQ2Length()*1e-3;
		double g = dtlCell.getCellCenter()*1e-3;
		
		double Phis = dtlCell.getRFPhase() + dtlCell.getRFPhaseAdd();
		double betas = dtlCell.getBetaS();
		double Ts = dtlCell.getTTF().getTs();
		double kTs = dtlCell.getTTF().getKTs();
		double k2Ts = dtlCell.getTTF().getK2Ts();
		double kS = 0;
		double k2S = 0;
		double E0TL = dtlCell.getVoltage()  * dtlCell.getVoltageMult();
		
		double B1 = dtlCell.getQ1FieldGradient();
		double B2 = dtlCell.getQ2FieldGradient();
		
		final String essId = dtlCell.getEssId();
		
		xal.smf.impl.Quadrupole quad1 = ElementFactory.createQuadrupole(essId+":Q1", Lq1, B1, new ApertureBucket(),
				accelerator, Lq1/2.);
		xal.smf.impl.Quadrupole quad2 = ElementFactory.createQuadrupole(essId+":Q2", Lq2, B2, new ApertureBucket(),
				accelerator, L - Lq2/2.);
		double length = L-Lq1-Lq2; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		xal.smf.impl.RfGap gap = ElementFactory.createRfGap(essId+":G", true, 1.0, new ApertureBucket(), length, L/2.-g);
		
		ESSRfCavity dtlTank = ESSElementFactory.createESSRfCavity(essId, L, new AcceleratorNode[] {quad1, gap, quad2}, Phis,
				E0TL*1e-6/length, dtlCell.getFrequency(), sectionPosition); 
				
		// TTF		
		if (betas == 0.0) {			
			dtlTank.getRfField().setTTFCoefs(new double[] {0.0});
			dtlTank.getRfField().setTTF_startCoefs(new double[] {0.0});
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		

		add(dtlTank);
	}

	@Override
	public void visit(Section section) {
		AcceleratorSeq s;
		s = new AcceleratorSeq(section.getId());
	
		if (currentSequence != null) {
			SequenceBucket sequenceBucket = new SequenceBucket();
			sequenceBucket.setPredecessors(new String[]{currentSequence.getId()});
			s.setSequence(sequenceBucket);
			currentSequence.setLength(sectionPosition);
			acceleratorPosition += sectionPosition;
			sectionPosition = 0.0;
		}
		s.setPosition(acceleratorPosition);	
		accelerator.addNode(s);
		
		currentSequence = s;
		this.section = section;
	}
		
	public void visit(Cell cell)
	{
	}
	
	public void visit(Slot slot)
	{
	}

	@Override
	public void visit(Edge edge) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void visit(final LegoMonitor legoMonitor) {
		if ("bpm".equals(legoMonitor.getType().toLowerCase()) || "bpm".equals(legoMonitor.getModel().toLowerCase())) {
			xal.smf.impl.BPM bpm = ElementFactory.createBPM(legoMonitor.getEssId(), 
					linac.getBeamFrequency()*section.getRFHarmonic(), 1.0, sectionPosition);
			add(bpm);
		}
	}
	
	@Override
	public void visit(final Monitor monitor) {
		if ("bpm".equals(monitor.getMonitorType().toLowerCase())) {
			xal.smf.impl.BPM bpm = ElementFactory.createBPM(monitor.getEssId(), monitor.getFrequency(), monitor.getLength(),
					sectionPosition+monitor.getLength()/2.);
			add(bpm);
		}
	}
	
	
	/**
	 * Reads info about initial parameters from linac lego parameters into a probe.
	 *
	 * @param baseProbe probe over which to set inital parameters
	 */
	public void readInitialParameters(EnvelopeProbe baseProbe) {
		Parameters params = linac.getLinacData();
		
		// generate a probe
		baseProbe.setSpeciesCharge(1);
		baseProbe.setSpeciesRestEnergy(9.3827202900E8);
		baseProbe.setSpeciesName("PROTON");
		//elsbaseProbe.setSpeciesRestEnergy(9.38272013e8);	
		baseProbe.setKineticEnergy(params.getDoubleValue("ekin")*1E+6);//energy
		baseProbe.setPosition(0.0);
		baseProbe.setTime(0.0);		
		
		double beta_gamma = baseProbe.getBeta() * baseProbe.getGamma();
	
		Twiss[] twiss = new Twiss[3];
		for (int i=0; i<3; i++) {			
			String axis = new String[] {"X", "Y", "Z"}[i];
			double dblAlpha = params.getDoubleValue("alpha"+axis);
			double dblBeta = params.getDoubleValue("beta"+axis);
			double dblEmitt = params.getDoubleValue("emit"+axis) * 1e-6 / beta_gamma; // / beta_gamma
			twiss[i] = new Twiss(dblAlpha, dblBeta, dblEmitt);
		}
		
		baseProbe.initFromTwiss(twiss);
		baseProbe.setBeamCurrent(params.getDoubleValue("beamCurrent")*1E-3);
		baseProbe.setBunchFrequency(params.getDoubleValue("beamFrequency")*1E+6); 	
	}

	public static void main(String[] args) throws JAXBException, SAXException, ParserConfigurationException, IOException, URISyntaxException {
		if (args.length < 2) {
			System.out.println("Usage: OpenXALExporter <linaclego.xml> <openxal.xdxf>");
			System.exit(-1);
		}

		export(args[0], args[1]);		
	}

	public Accelerator getAccelerator() {
		return accelerator;
	}
}
