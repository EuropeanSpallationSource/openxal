package org.openepics.discs.exporters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.Cell;
import se.lu.esss.linaclego.FieldProfile;
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
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ChannelSuite;
import xal.smf.attr.ApertureBucket;
import xal.smf.attr.SequenceBucket;
import xal.smf.impl.BPM;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Magnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetPowerSupply;
import xal.smf.impl.RfCavity;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlWriter;

public class OpenXALExporter implements BLEVisitor {
	private Map<String, MagnetMainSupply> magnetPowerSupplies;
	private double acceleratorPosition;
	private double sectionPosition;
	public static final double beta_gamma_Er_by_e0_c = -9.302773635653585;
	
	private Linac linac;
	private Section section;
	
	private Accelerator accelerator;
	private AcceleratorSeq currentSequence;

	protected OpenXALExporter() 
	{
		
	}
	
	public static Accelerator convert(Linac linac) {
		final OpenXALExporter exporter = new OpenXALExporter();
		
		exporter.acceleratorPosition = 0.0;
		exporter.sectionPosition = 0.0;
		
		exporter.linac = linac;
		
		exporter.accelerator = new Accelerator("ESS") {
			{
				exporter.magnetPowerSupplies = magnetMainSupplies; 
			}
			public void write(DataAdaptor adaptor) {
				super.write(adaptor);
				// write out power supplies
				DataAdaptor powerSuppliesAdaptor = adaptor.createChild("powersupplies");				 
				for ( MagnetPowerSupply mps : exporter.magnetPowerSupplies.values()) {
					mps.write( powerSuppliesAdaptor.createChild("ps"));				 
				}				 
			 }
		};
		linac.accept(exporter);
		exporter.accelerator.setLength(exporter.acceleratorPosition + exporter.sectionPosition);
		return exporter.accelerator;
	}
	
	public static void export(Linac linac, String fileName) throws IOException {
		Accelerator accelerator = convert(linac);
			
		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();

		Document document = da.document();
		document.setDocumentURI(new File(fileName).toURI().toString());

		da.writeNode(accelerator);
		cleanup(document);
		XmlWriter.writeToFile(document, new File(fileName));
	}
	
	public static void export(String sourceFileName, String destinationFilename) throws IOException, URISyntaxException, SAXException, ParserConfigurationException, JAXBException {
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
	
	private static class MutableChannelSuite extends ChannelSuite
	{
		public MutableChannelSuite() {
			super();
		}
	
		protected void addChannel(String handle, String signal, boolean settable)
		{
			SIGNAL_SUITE.addChannel(handle, signal, null, settable);
		}
	}
	
	private static class RFCavityChannelSuite extends MutableChannelSuite 
	{
		public RFCavityChannelSuite(String name)
		{
			name = name.replace('_', ':');
			addChannel(RfCavity.CAV_AMP_SET_HANDLE, name+":AmpCtl", true);
	    	addChannel(RfCavity.CAV_PHASE_SET_HANDLE, name+":PhsCtl", true);
	    	addChannel(RfCavity.CAV_AMP_AVG_HANDLE, name+":AmpAvg", false);
	    	addChannel(RfCavity.CAV_PHASE_AVG_HANDLE, name+":PhsAvg", false);
		}
		
		public RFCavityChannelSuite(String name, String ampChannel, String phaseChannel)
		{
	    	addChannel(RfCavity.CAV_AMP_SET_HANDLE, ampChannel+"Ctl", true);
	    	addChannel(RfCavity.CAV_PHASE_SET_HANDLE, phaseChannel+"Ctl", true);
	    	addChannel(RfCavity.CAV_AMP_AVG_HANDLE, ampChannel, false);
	    	addChannel(RfCavity.CAV_PHASE_AVG_HANDLE, phaseChannel, false);
		}
	}
	
	private static class BPMChannelSuite extends MutableChannelSuite 
	{
		public BPMChannelSuite(String name)
		{
			name = name.replace('_', ':');
			addChannel(BPM.X_AVG_HANDLE, name+":XAvg", false);
	    	addChannel(BPM.Y_AVG_HANDLE, name+":YAvg", false);
	    	addChannel(BPM.X_TBT_HANDLE, name+":XTBT", false);
	    	addChannel(BPM.Y_TBT_HANDLE, name+":YTBT", false);
	    	addChannel(BPM.PHASE_AVG_HANDLE, name+":PhsAvg", false);
	    	addChannel(BPM.AMP_AVG_HANDLE, name+":AmpAvg", false);
	    	addChannel(BPM.AMP_TBT_HANDLE, name+":AmpTBT", false);
	    	addChannel(BPM.PHASE_TBT_HANDLE, name+":PhsTBT", false);
		}
	}
	
	private static class ElectromagnetChannelSuite extends MutableChannelSuite 
	{
		public ElectromagnetChannelSuite(String name)
		{
			name = name.replace('_', ':');
			addChannel(Electromagnet.FIELD_RB_HANDLE, name + ":B", false);
		}
		
		public ElectromagnetChannelSuite(String name, String signal)
		{
			name = name.replace('_', ':');
			addChannel(Electromagnet.FIELD_RB_HANDLE, name + ":" + signal, false);
		}
	}
	
	
	private static class MagnetChannelSuite extends MutableChannelSuite 
	{
		public MagnetChannelSuite(String name)
		{
			name = name.replace('_', ':');
			addChannel(MagnetPowerSupply.CURRENT_RB_HANDLE, name+":CurRB", false);
	    	addChannel(MagnetPowerSupply.CURRENT_SET_HANDLE, name+":CurSet", true);
	    	addChannel(MagnetMainSupply.FIELD_RB_HANDLE, name+":FldRB", false);
	    	addChannel(MagnetMainSupply.FIELD_SET_HANDLE, name+":FldSet", true);
	    	addChannel(MagnetPowerSupply.CYCLE_STATE_HANDLE, name+":CycSt", false);
	    	addChannel(MagnetMainSupply.CYCLE_ENABLE_HANDLE, name+":CycEn", true);
		}
	}
	
	
	private static class MagnetSupply extends MagnetMainSupply {
		public MagnetSupply(String name) {
			super(null);
			strId = name + "-PS";			
			channelSuite = new MagnetChannelSuite(name);
		}
	}
	
	private static void updateApertureBucket(BeamlineElement element, ApertureBucket aper) {
		aper.setAperX(element.getApertureR());
		if (element.getApertureY() != 0.) aper.setAperY(element.getApertureY());		
		/*int apertureCode = element.getApertureType() == null ? 2 : element.getApertureType().getIntegerValue();
		aper.setShape(toOpenXALApertureCode(apertureCode));		*/
	}
	
	
	@Override
	public void visit(Drift drift) {
		sectionPosition += drift.getLength()*1e-3;
	}

	@Override
	public void visit(final Quad iquad) {
		double L = iquad.getLength()*1e-3;		
		double G = iquad.getFieldGradient();
		
		final MagnetSupply ps = new MagnetSupply(iquad.getEssId());
		magnetPowerSupplies.put(ps.getId(), ps);
		xal.smf.impl.Quadrupole quad = new xal.smf.impl.Quadrupole(iquad.getEssId()) { // there's no setter for type (you need to extend class)
			{
				_type="Q"; 
				channelSuite = new ElectromagnetChannelSuite(iquad.getEssId());
				mainSupplyId = ps.getId();
			}
		};
		
		quad.setPosition(sectionPosition + L*0.5); //always position on center!
		quad.setLength(L); // effLength below is actually the only one read 
		quad.getMagBucket().setEffLength(L);
					
		quad.setDfltField(G);
		quad.getMagBucket().setPolarity(1);
		updateApertureBucket(iquad, quad.getAper());
		
		add(quad);
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

		// setup		
		xal.smf.impl.RfGap gap = new xal.smf.impl.RfGap(rfGap.getEssId()+":G");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		
		// following are used to calculate E0TL
		double length = 1.0; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		/*gap.getRfGap().setGapOffset(dblVal)*/	
		
		ESSRfCavity cavity = new ESSRfCavity(rfGap.getEssId())
		{
			{
				channelSuite = new RFCavityChannelSuite(rfGap.getEssId());//, rfGap.getVoltageDevName(), rfGap.getRFPhaseDevName());
			}
		};
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);		
		cavity.getRfField().setAmplitude(E0TL * 1e-6 / length);
		cavity.getRfField().setFrequency(rfGap.getFrequency());		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		gap.getRfGap().setTTF(1.0);		
		
		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_startCoefs(new double[] {});
		} else {				
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		
		
		updateApertureBucket(rfGap, gap.getAper());		
		
		cavity.setPosition(sectionPosition);
		cavity.setLength(0.0);
		add(cavity);
	}

	@Override
	public void visit(final Bend ibend) {
		double alpha_deg = ibend.getBendAngle();
		double rho = ibend.getCurvatureRadius();
		double entry_angle_deg = Math.abs(alpha_deg / 2.); //ibend.getEntranceAngle();
		double exit_angle_deg = Math.abs(alpha_deg / 2.); //ibend.getExitAngle();
		double G = 0.;//ibend.getGap();
		
		// TODO put those values into the database
		double entrK1 = 0.45, entrK2 = 2.8, exitK1 = 0.45, exitK2 = 2.8;
		double N = 0;
						
		// mm -> m
		rho *= 1e-3;
		G *= 1e-3;		
		
		// calculations		
		double len = Math.abs(rho*alpha_deg * Math.PI/180.0);
		double quadComp = - N / (rho*rho);
		
		// following are used to calculate field		
	    /*double c  = IConstants.LightSpeed;	      
	    double e = GlobalConst.SpeciesCharge;
	    double Er = probe.getSpeciesRestEnergy();
	    double gamma = probe.getGamma();
	    double b  = probe.getBeta();*/
	    
	    double k /* = b*gamma*Er/(e*c); */ = beta_gamma_Er_by_e0_c;
	    double B0 = k/rho*Math.signum(alpha_deg);
	    //double B0 = b*gamma*Er/(e*c*rho)*Math.signum(alpha);
		
	    final MagnetSupply ps = new MagnetSupply(ibend.getEssId());
		magnetPowerSupplies.put(ps.getId(), ps);
	    se.lu.esss.ics.jels.smf.impl.ESSBend bend = new se.lu.esss.ics.jels.smf.impl.ESSBend(ibend.getEssId(), 
				 MagnetType.VERTICAL)
	    {
	    	{				
				channelSuite = new ElectromagnetChannelSuite(ibend.getEssId());
				mainSupplyId = ps.getId();
			}
	    };
		bend.setPosition(sectionPosition+len*0.5); //always position on center!
		bend.setLength(len); // both paths are used in calculation
		bend.getMagBucket().setPathLength(len);
		
		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle_deg);
		bend.getMagBucket().setBendAngle(alpha_deg);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle_deg);		
		bend.setDfltField(B0);		
		bend.getMagBucket().setDipoleQuadComponent(quadComp);
		
		bend.setGap(G);
		bend.setEntrK1(entrK1);
		bend.setEntrK2(entrK2);
		bend.setExitK1(exitK1);
		bend.setExitK2(exitK2);
		
		updateApertureBucket(ibend, bend.getAper());		
				
		add(bend);		
	}

	@Override
	public void visit(final ThinSteering thinSteering) {
		double L = thinSteering.getLength()*1e-3;
		
		final String essId = thinSteering.getEssId();
		
		final MagnetSupply vcps = new MagnetSupply(essId+"-VC");
		magnetPowerSupplies.put(vcps.getId(), vcps);

		VDipoleCorr vcorr = new VDipoleCorr(essId+"-VC") {
			{
				channelSuite = new ElectromagnetChannelSuite(essId+"-VC");
				mainSupplyId = vcps.getId();
			}
		};
		vcorr.setPosition(sectionPosition + L/2.);
		vcorr.setLength(L);
		vcorr.getMagBucket().setEffLength(L == 0. ?  1. : L);
		updateApertureBucket(thinSteering, vcorr.getAper());
		add(vcorr);
		
		final MagnetSupply hcps = new MagnetSupply(essId+"-HC");
		magnetPowerSupplies.put(hcps.getId(), hcps);
		
		HDipoleCorr hcorr = new HDipoleCorr(essId+"-HC") {
			{
				channelSuite = new ElectromagnetChannelSuite(essId+"-HC");
				mainSupplyId = hcps.getId();
			}
		};
		hcorr.setPosition(sectionPosition + L/2.);
		hcorr.setLength(L);
		hcorr.getMagBucket().setEffLength(L == 0. ?  1. : L);
		updateApertureBucket(thinSteering, hcorr.getAper());
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
		{
			{
				channelSuite = new RFCavityChannelSuite(ncells.getEssId());
			}
		};
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
		FieldProfile fp = fieldMap.getFieldProfile();
		final se.lu.esss.ics.jels.smf.impl.FieldProfile fp2 = new se.lu.esss.ics.jels.smf.impl.FieldProfile(fp.getLength()*1e-3, fp.getField());
		ESSFieldMap fm = new ESSFieldMap(fieldMap.getEssId());		
		fm.setFieldProfile(fp2);
		fm.setPosition(sectionPosition + fieldMap.getLength()*1e-3*0.5);
		fm.setLength(fieldMap.getLength()*1e-3);
		fm.setFrequency(fieldMap.getFrequency());
		fm.setXelmax(fieldMap.getElectricFieldFactor());
		fm.setPhase(fieldMap.getRFPhase());
		fm.setFieldMapFile(fieldMap.getFieldmapFile());
		updateApertureBucket(fieldMap, fm.getAper());
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

		// setup		
		xal.smf.impl.RfGap gap = new xal.smf.impl.RfGap(dtlRfGap.getEssId()+":G");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		
		// following are used to calculate E0TL
		double length = dtlRfGap.getLength()*1e-3; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.setLength(0);
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		/*gap.getRfGap().setGapOffset(dblVal)*/	
		gap.setPosition(length/2.);
		
		ESSRfCavity cavity = new ESSRfCavity(dtlRfGap.getEssId())
		{
			{
				channelSuite = new RFCavityChannelSuite(dtlRfGap.getEssId());
			}
		};
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);		
		cavity.getRfField().setAmplitude(E0TL * 1e-6 / length);
		cavity.getRfField().setFrequency(dtlRfGap.getFrequency());		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		gap.getRfGap().setTTF(1.0);		
		
		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_startCoefs(new double[] {});
		} else {				
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		
		
		updateApertureBucket(dtlRfGap, gap.getAper());		
		
		cavity.setPosition(sectionPosition);
		cavity.setLength(length);
		add(cavity);
	}

	@Override
	public void visit(final DtlDriftTube dtlDriftTube) {
		double L1 = dtlDriftTube.getNoseConeUpLength() * 1e-3;
		double Lq = dtlDriftTube.getQuadLength()*1e-3;
		double L2 = dtlDriftTube.getNoseConeDnLength() * 1e-3;
		double G = dtlDriftTube.getFieldGradient();
		
		final String essId = dtlDriftTube.getEssId();
		
		final MagnetSupply ps = new MagnetSupply(essId);
		magnetPowerSupplies.put(ps.getId(), ps);
		xal.smf.impl.Quadrupole quad = new xal.smf.impl.Quadrupole(essId + ":Q") { // there's no setter for type (you need to extend class)
			{
				_type="Q"; 
				channelSuite = new ElectromagnetChannelSuite(essId);
				mainSupplyId = ps.getId();
			}
		};
		
		quad.setPosition(L1 + Lq*0.5); //always position on center!
		quad.setLength(Lq); // effLength below is actually the only one read 
		quad.getMagBucket().setEffLength(Lq);
					
		quad.setDfltField(G);
		quad.getMagBucket().setPolarity(1);
		updateApertureBucket(dtlDriftTube, quad.getAper());
		
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
		
		// setup		
		// QUAD1,2
		final MagnetSupply ps1 = new MagnetSupply(essId+"A");
		magnetPowerSupplies.put(ps1.getId(), ps1);
		
		xal.smf.impl.Quadrupole quad1 = new xal.smf.impl.Quadrupole(essId+":Q1") { // there's no setter for type (you need to extend class)
			{_type="Q";
			channelSuite = new ElectromagnetChannelSuite(essId,"B1");
			mainSupplyId = ps1.getId();
			}
		};
		quad1.setPosition(0.5*Lq1); //always position on center!
		quad1.setLength(Lq1); // effLength below is actually the only one read 
		quad1.getMagBucket().setEffLength(Lq1);
		quad1.setDfltField(B1);
		quad1.getMagBucket().setPolarity(1);
		
		final MagnetSupply ps2 = new MagnetSupply(essId+"B");
		magnetPowerSupplies.put(ps2.getId(), ps2);
		
		xal.smf.impl.Quadrupole quad2 = new xal.smf.impl.Quadrupole(essId+":Q2") { // there's no setter for type (you need to extend class)
			{_type="Q"; 
			channelSuite = new ElectromagnetChannelSuite(essId,"B2");	
			mainSupplyId = ps2.getId();
			}
		};
		quad2.setPosition(L-0.5*Lq2); //always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read 
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2);
		quad2.getMagBucket().setPolarity(1);
		
		
		// GAP
		xal.smf.impl.RfGap gap = new xal.smf.impl.RfGap(essId+":G");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		gap.setPosition(0.5*L-g);
		// following are used to calculate E0TL
		double length = L-Lq1-Lq2; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		gap.getRfGap().setTTF(1.0);		
		/*gap.getRfGap().setGapOffset(dblVal)*/		
		
		ESSRfCavity dtlTank = new ESSRfCavity(essId)
		{
			{
				channelSuite = new RFCavityChannelSuite(essId);
			}
		};; // this could also be rfcavity, makes no difference
		dtlTank.addNode(quad1);
		dtlTank.addNode(gap);
		dtlTank.addNode(quad2);
		dtlTank.getRfField().setPhase(Phis);		
		dtlTank.getRfField().setAmplitude(E0TL * 1e-6 / length);
		dtlTank.getRfField().setFrequency(dtlCell.getFrequency());		
		/*cavity.getRfField().setStructureMode(dblVal);*/
				
		// TTF		
		if (betas == 0.0) {			
			dtlTank.getRfField().setTTFCoefs(new double[] {0.0});
			dtlTank.getRfField().setTTF_startCoefs(new double[] {0.0});
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setTTF_startCoefs(new double[] {betas, Ts, kTs, k2Ts});
		}		
		dtlTank.setLength(L);
		dtlTank.setPosition(sectionPosition);		
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
	public void visit(final ControlPoint controlPoint) {
		if ("bpm".equals(controlPoint.getType()) || "bpm".equals(controlPoint.getModel())) {
			xal.smf.impl.BPM bpm = new BPM(controlPoint.getEssId()) {
				{
					channelSuite = new BPMChannelSuite(controlPoint.getDevName());
				}
			};
			bpm.getBPMBucket().setFrequency(linac.getBeamFrequency() * section.getRFHarmonic());
			bpm.getBPMBucket().setLength(1.0);
			bpm.getBPMBucket().setOrientation(1);
			bpm.setPosition(sectionPosition);
			add(bpm);
		}
	}
	
	@Override
	public void visit(final Monitor monitor) {
		if ("bpm".equals(monitor.getMonitorType().toLowerCase())) {
			xal.smf.impl.BPM bpm = new BPM(monitor.getEssId()) {
				{
					channelSuite = new BPMChannelSuite(monitor.getEssId());
				}
			};
			bpm.getBPMBucket().setFrequency(monitor.getFrequency());
			bpm.getBPMBucket().setLength(monitor.getLength());
			bpm.getBPMBucket().setOrientation(1);
			bpm.setLength(monitor.getLength());
			bpm.setPosition(sectionPosition+monitor.getLength()/2.);
			add(bpm);
		}
	}

	public static void main(String[] args) throws JAXBException, SAXException, ParserConfigurationException, IOException, URISyntaxException {
		if (args.length < 2) {
			System.out.println("Usage: OpenXALExporter <linaclego.xml> <openxal.xdxf>");
			System.exit(-1);
		}

		export(args[0], args[1]);		
	}
}
