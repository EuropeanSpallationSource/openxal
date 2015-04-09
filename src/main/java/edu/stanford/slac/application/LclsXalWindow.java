package edu.stanford.slac.application;

import java.awt.BorderLayout;

import javax.swing.JPanel;
//import gov.sns.application.XalDocument;
//import gov.sns.application.XalWindow;


import xal.extension.application.XalDocument;
import xal.extension.application.XalWindow;

public abstract class LclsXalWindow extends XalWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9039997782476857041L;
	private JPanel stateBar;

	public LclsXalWindow(XalDocument document) {
		super(document);
	}

	public void makeFrame() {
		super.makeFrame();
		stateBar = new JPanel(new BorderLayout());
		getContentPane().add(stateBar, "South");
	}
	
	public JPanel getStateBar(){
		return stateBar;
	}
	
}
