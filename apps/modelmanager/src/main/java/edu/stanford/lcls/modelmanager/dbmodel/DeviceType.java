package edu.stanford.lcls.modelmanager.dbmodel;

import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;

public class DeviceType {
	private AcceleratorSeq defaultAccelerator;
	//static final private String[] COMBO_SEQ = { "FULL MACHINE",
	//		"CATHODE TO GUN SPECT DUMP", "CATHODE TO 135-MEV SPECT DUMP",
	//		"CATHODE TO 52SL2" };
	//private AcceleratorSeq seq;
	//private java.util.List<AcceleratorNode> allNodes;

	public DeviceType(AcceleratorSeq acc) {
		defaultAccelerator = acc;
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
}
