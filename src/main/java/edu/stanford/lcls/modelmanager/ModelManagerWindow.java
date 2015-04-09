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

import java.awt.Dimension;


//import edu.stanford.slac.application.LclsXalWindow;
import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.application.LclsXalWindow;

import javax.swing.*;


/**
 * TemplateViewerWindow
 *
 * @author  somebody
 */

public class ModelManagerWindow extends LclsXalWindow implements SwingConstants {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1;
	private ModelManagerFeature mmf;

	/** Creates a new instance of MainWindow */
    public ModelManagerWindow(final ModelManagerDocument aDocument) {
        super(aDocument);
		//  make the GUI
        mmf = makeContent();
//		JButton helpButton = new JButton("Help", IconLib.getIcon( IconGroup.GENERAL, "Help24.gif" ));
//		helpButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				
//			}
//		});
//		// use the framework one
//		getToolBar().add(helpButton);

//		JButton exitButton = new JButton("Exit");
//		getToolBar().add(exitButton);
//		exitButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				System.exit(0);
//			}
//		});
   }
    
	/**
	 * Create the GUI
	 */
	protected ModelManagerFeature makeContent() {
		Dimension screenSize = this.getToolkit().getScreenSize();
		int frameWidth = screenSize.width - 100 < 1200 ? screenSize.width - 100
				: 1200;
		int frameHeight = screenSize.height - 100 < 1000 ? screenSize.height - 100
				: 1000;
		this.setSize(frameWidth, frameHeight);
		this.setLocation((screenSize.width - frameWidth) / 2,
				(screenSize.height - frameHeight) / 2);
		return new ModelManagerFeature(this, this.getStateBar());
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




