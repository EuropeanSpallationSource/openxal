/*
 * TemplateDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package edu.stanford.lcls.modelmanager;

import edu.stanford.lcls.modelmanager.dbmodel.DataManager;
import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;
import xal.extension.application.Commander;
import xal.extension.application.smf.AcceleratorDocument;
import xal.tools.xml.XmlDataAdaptor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * TemplateDocument
 *
 * @author  somebody
 */
public class ModelManagerDocument extends AcceleratorDocument {
	
	private Action helpAction; 
	
//	HelpWindow hw = new HelpWindow();
	
	/** Create a new empty document */
    public ModelManagerDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public ModelManagerDocument(java.net.URL url) {
        setSource(url);
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new ModelManagerWindow(this);
		if (getSource() != null) {
/*			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			DataAdaptor da1 = xda.childAdaptor("AcceleratorApplicationTemplate");

			//restore accelerator file
			this.setAcceleratorFilePath(da1.childAdaptor("accelerator")
					.stringValue("xalFile"));
			
			String accelUrl = this.getAcceleratorFilePath();
			try {
				this.setAccelerator(XMLDataManager.acceleratorWithPath(accelUrl), this
						.getAcceleratorFilePath());
			} catch (Exception exception) {
				JOptionPane
						.showMessageDialog(
								null,
								"Hey - I had trouble parsing the accelerator input xml file you fed me",
								"AOC error", JOptionPane.ERROR_MESSAGE);
			}
			this.acceleratorChanged();

			// set up the right sequence combo from selected primaries:
			List temp = da1.childAdaptors("sequences");
			if (temp.isEmpty())
				return; // bail out, nothing left to do

			ArrayList seqs = new ArrayList();
			DataAdaptor da2a = da1.childAdaptor("sequences");
			String seqName = da2a.stringValue("name");

			temp = da2a.childAdaptors("seq");
			Iterator itr = temp.iterator();
			while (itr.hasNext()) {
				DataAdaptor da = (DataAdaptor) itr.next();
				seqs.add(getAccelerator().getSequence(da.stringValue("name")));
			}
			setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
*/
		}
		setHasChanges(false);
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
/*		DataAdaptor daLevel1 = xda.createChild("AcceleratorApplicationTemplate");
		//save accelerator file
		DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
		try {
			daXMLFile.setValue("xalFile", new URL(this.getAcceleratorFilePath()).getPath());
		} catch (java.net.MalformedURLException e) {
			daXMLFile.setValue("xalFile",this.getAcceleratorFilePath());
		}
		// save selected sequences
		ArrayList seqs;
		if (getSelectedSequence() != null) {
			DataAdaptor daSeq = daLevel1.createChild("sequences");
			daSeq.setValue("name", getSelectedSequence().getId());
			if (getSelectedSequence().getClass() == AcceleratorSeqCombo.class) {
				AcceleratorSeqCombo asc = (AcceleratorSeqCombo) getSelectedSequence();
				seqs = (ArrayList) asc.getConstituentNames();
			} else {
				seqs = new ArrayList();
				seqs.add(getSelectedSequence().getId());
			}

			Iterator itr = seqs.iterator();

			while (itr.hasNext()) {
				DataAdaptor daSeqComponents = daSeq.createChild("seq");
				daSeqComponents.setValue("name", itr.next());
			}
		}
*/		
		// write to the document file
		xda.writeToUrl(url);
		setHasChanges(false);
    }
    
    public void customizeCommands(Commander commander) {
//        helpAction = new AbstractAction("show-myHelp", IconLib.getIcon( IconGroup.GENERAL, "Help24.gif")) {
        helpAction = new AbstractAction("show-myHelp") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
//                HelpWindow.showNear( Application.getActiveWindow() );
//				try {
//					hw.loadLink(new URL("https://confluence.slac.stanford.edu/display/LCLSHELP/Model+Manager#ModelManager-Introduction"));
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				}

				// launch a web browser and use confluence URL as the help contents
//            	System.out.println("launching firefox...");
        		try {
					Desktop.getDesktop().browse(new URI("https://confluence.slac.stanford.edu/display/LCLSHELP/Model+Manager#ModelManager-Introduction"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
            }
        };
        
//        action.setEnabled( HelpWindow.isAvailable() );
//        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_HELP, 0 ) );
        
//		helpAction.putValue(Action.NAME, "show-myHelp");
		commander.registerAction(helpAction);
        
    }
    
    public void acceleratorChanged() {
		if (accelerator != null) {

			setHasChanges(true);
		}
	}

	public void selectedSequenceChanged() {
		if (selectedSequence != null) {
			System.out.println("Sequence selected: " + selectedSequence.getId());
			Message.info("Beamline selected: " + selectedSequence.getId());
			setHasChanges(true);
		}
	}
	
/*	public Message getMessageLogger(){
		return ((ModelManagerWindow)mainWindow).getMessageLogger();
	}
*/	
	public void connectDefault(){
		((ModelManagerWindow)mainWindow).connectDefault();
	}

	/**
	 * clean up before close the app
	 */
	public void willClose() {
		//TODO clear all DB connections
		ModelManagerFeature.getBrowserModel().closeDBConnection();
	}
}




