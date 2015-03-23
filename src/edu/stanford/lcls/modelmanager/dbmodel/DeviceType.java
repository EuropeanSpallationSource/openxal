package edu.stanford.lcls.modelmanager.dbmodel;

import javax.swing.JFrame;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;

import xal.extension.application.Application;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.data.OpticsSwitcher;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.Quadrupole;

public class DeviceType {
	private Accelerator defaultAccelerator;
	//static final private String[] COMBO_SEQ = { "FULL MACHINE",
	//		"CATHODE TO GUN SPECT DUMP", "CATHODE TO 135-MEV SPECT DUMP",
	//		"CATHODE TO 52SL2" };
	//private AcceleratorSeq seq;
	//private java.util.List<AcceleratorNode> allNodes;

	public DeviceType(JFrame parent) {
		loadDefaultAccelerator(parent);
		//seq = defaultAccelerator.getSequence(COMBO_SEQ[0]);
		//allNodes = seq.getAllNodes();
		// defaultAccelerator = XMLDataManager.loadDefaultAccelerator();
	}

	public String getDeviceType(String elementName) {
		if (defaultAccelerator != null) {
			try {
				AcceleratorNode node = defaultAccelerator
						.getNodeWithId(elementName);
				if(node instanceof Quadrupole){
					if(((Quadrupole) node).getDfltField() >= 0){
						return "FQUAD";
					}
					else
						return "DQUAD";
				}
				return node.getType();
			} catch (Exception e1) {
//				ModelManagerFeature.getMessageLogger().error("Exception: " + e1.getMessage());			
				try {
					AcceleratorNode node = defaultAccelerator
							.getNodeWithId(elementName + ":RG"); //device end with ":RG"
					return node.getType();
				} catch (Exception e2) {
					//e2.printStackTrace();
//					ModelManagerFeature.getMessageLogger().error("Exception: " + e2.getMessage());			
					return "marker";
				}
			}
		} else {
			return null;
		}
	}

	public String getDeviceLength(String elementName) {
		if (defaultAccelerator != null) {
			try {
				AcceleratorNode node = defaultAccelerator
						.getNodeWithId(elementName);
				return String.valueOf(node.getLength());
			} catch (Exception e1) {
//				ModelManagerFeature.getMessageLogger().error("Exception: " + e1.getMessage());			
				try {
					AcceleratorNode node = defaultAccelerator
							.getNodeWithId(elementName + ":RG"); //device end with ":RG"
					return String.valueOf(node.getLength());
				} catch (Exception e2) {
//					ModelManagerFeature.getMessageLogger().error("Exception: " + e2.getMessage());			
					//e2.printStackTrace();
					return String.valueOf(0.0);
				}
			}
		} else {
			return null;
		}
	}
	
	public String getEPICSName(String elementName){
		if (defaultAccelerator != null) {
			try {
				AcceleratorNode node = defaultAccelerator
						.getNodeWithId(elementName);
				return String.valueOf(node.getEId());
			} catch (Exception e1) {
//				ModelManagerFeature.getMessageLogger().error("Exception: " + e1.getMessage());			
				try {
					AcceleratorNode node = defaultAccelerator
							.getNodeWithId(elementName + ":RG"); //device end with ":RG"
					return String.valueOf(node.getEId());
				} catch (Exception e2) {
//					ModelManagerFeature.getMessageLogger().error("Exception: " + e2.getMessage());			
					//e2.printStackTrace();
					return "";
				}
			}
		} else {
			return null;
		}
	}

	protected boolean loadDefaultAccelerator(JFrame parent) {
		Accelerator defaultAccelerator = null;
		try {
			defaultAccelerator = XMLDataManager.loadDefaultAccelerator();
		} catch (Exception exception) {
			Message.error("Exception: Cannot open default XAL accelerator for loading.", true);			
			Application.displayError("Exception thrown while loading the default accelerator",
					"Failed to load default accelerator", exception);
		}

		if (defaultAccelerator != null) {
			// setAccelerator( defaultAccelerator, XMLDataManager.defaultPath()
			// );
			this.defaultAccelerator = defaultAccelerator;
			return true;
		} else {
			/* TODO OPENXAL OpticSwitcher's constructor is protected...
			OpticsSwitcher switcher = new OpticsSwitcher(parent, true);
			switcher.showNearOwner();
			if (switcher.getDefaultOpticsPath() != null) {
				return loadDefaultAccelerator(parent);
			}
			*/
		}
		return false;
	}

}
