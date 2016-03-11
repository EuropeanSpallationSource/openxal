/*
 * TemplateWindow.java
 *
 * Created on Fri Oct 10 15:12:03 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package edu.stanford.lcls.modelmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import xal.extension.application.XalWindow;
import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;


/**
 * TemplateViewerWindow
 *
 * @author  somebody
 */

public class ModelManagerWindow extends XalWindow implements SwingConstants {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1;
	private ModelManagerFeature mmf;
	private JPanel stateBar;

	/** Creates a new instance of MainWindow */
    public ModelManagerWindow(final ModelManagerDocument aDocument) {
        super(aDocument);
		//  make the GUI
        mmf = makeContent();
   }
    
	/**
	 * Create the GUI
	 */
	protected ModelManagerFeature makeContent() {
		Dimension screenSize = this.getToolkit().getScreenSize();
		int frameWidth = screenSize.width - 100 < 1300 ? screenSize.width - 100
				: 1300;
		int frameHeight = screenSize.height - 100 < 1000 ? screenSize.height - 100
				: 1000;
		this.setSize(frameWidth, frameHeight);
		this.setLocation((screenSize.width - frameWidth) / 2,
				(screenSize.height - frameHeight) / 2);
		return new ModelManagerFeature(this, this.getStateBar(), (ModelManagerDocument)document);
	}

	public void makeFrame() {
		super.makeFrame();
		stateBar = new JPanel(new BorderLayout());
		getContentPane().add(stateBar, "South");
	}
	
	public JPanel getStateBar(){
		return stateBar;
	}
	
/*	public Message getMessageLogger(){
		return ModelManagerFeature.getMessageLogger();
	}
*/	
	public void connectDefault(){
		mmf.connectDefault();
	}
	
	public ModelManagerFeature getModelManagerFeature() {
		return mmf;
	}
}




