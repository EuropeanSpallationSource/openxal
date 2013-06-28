/*
 * @(#)VADocument.java          1.5 07/15/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.virtualaccelerator;

import java.util.*;
import java.util.prefs.*;
import java.net.*;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.JToggleButton.ToggleButtonModel;

import xal.smf.application.*;
import xal.application.*;
import xal.tools.bricks.WindowReference;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.data.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.model.*;
import xal.model.probe.*;  // Probe for t3d header
import xal.model.alg.*;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ICoordinateState;
import xal.sim.scenario.*;
import xal.smf.*;
import xal.model.xml.*;
import xal.model.xml.ParsingException;

import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseVector;
import xal.tools.swing.KeyValueFilteredTableModel;
import xal.tools.swing.DecimalField;
import xal.tools.apputils.files.*;
import xal.tools.apputils.pvlogbrowser.PVLogSnapshotChooser;
import xal.sim.sync.PVLoggerDataSource;
import xal.tools.dispatch.*;


/**
 * VADocument is a custom AcceleratorDocument for virtual accelerator application.
 * @version 1.5 15 Jul 2004
 * @author Paul Chu
 */
public class VADocument extends AcceleratorDocument implements ActionListener, PutListener {
    /** default BPM waveform size */
    final static private int DEFAULT_BPM_WAVEFORM_SIZE = VAServer.DEFAULT_ARRAY_SIZE;
    
    /** default BPM waveform data size (part of the waveform to populate with data) */
    final static private int DEFAULT_BPM_WAVEFORM_DATA_SIZE = 250;
    
	/** The document for the text pane in the main window. */
	protected PlainDocument textDocument;

	/** For on-line model */
	protected Scenario model;

	private Probe myProbe;

	String dataSource = Scenario.SYNC_MODE_LIVE;

	protected String theProbeFile;

	int runT3d_OK = 0;

	private JDialog setNoise = new JDialog();

	private DecimalField df1, df2, df3, df4, df5;

	private DecimalField df11, df21, df31, df41, df51;

	private double quadNoise = 0.0;

	private double dipoleNoise = 0.0;

	private double correctorNoise = 0.0;
	
	private double solNoise = 0.0;

	private double bpmNoise = 0.0;

	private double rfAmpNoise = 0.0;

	private double rfPhaseNoise = 0.0;

	private double quadOffset = 0.0;

	private double dipoleOffset = 0.0;

	private double correctorOffset = 0.0;

	private double solOffset = 0.0;

	private double bpmOffset = 0.0;

	private double rfAmpOffset = 0.0;

	private double rfPhaseOffset = 0.0;

	private JButton done = new JButton("OK");

	private volatile boolean vaRunning = false;

	private java.util.List<RfCavity> rfCavities;

	private java.util.List<Electromagnet> mags;

	private java.util.List<BPM> bpms;

	private java.util.List<ProfileMonitor> wss;

	private Channel beamOnEvent;

	private Channel beamOnEventCount;
	
	private Channel slowDiagEvent;
	
	private Channel _repRateChannel;
	
	// timestamp of last update
	private Date _lastUpdate;
	
	private long beamOnEventCounter = 0;

	private List<ReadbackSetRecord> READBACK_SET_RECORDS;

	private LinkedHashMap<Channel, Double> ch_noiseMap;

	private LinkedHashMap<Channel, Double> ch_offsetMap;
	
	private VAServer _vaServer;

	private RecentFileTracker _probeFileTracker;

	// for on/off-line mode selection
	ToggleButtonModel olmModel = new ToggleButtonModel();

	ToggleButtonModel pvlogModel = new ToggleButtonModel();

	ToggleButtonModel pvlogMovieModel = new ToggleButtonModel();

	private boolean isFromPVLogger = false;
	private boolean isForOLM = false;

	private PVLogSnapshotChooser plsc;

	private JDialog pvLogSelector;

	private PVLoggerDataSource plds;
		
	/** bricks window reference */
	private WindowReference _windowReference;
	
	/** readback setpoint table model */
	private KeyValueFilteredTableModel<ReadbackSetRecord> READBACK_SET_TABLE_MODEL;

	/** timer to synch the readbacks with the setpoints and also sync the model */
	final private DispatchTimer MODEL_SYNC_TIMER;

	/** model sync period in milliseconds */
	private long _modelSyncPeriod;


	/** Create a new empty document */
	public VADocument() {
		this( null );

	}

	/**
	 * Create a new document loaded from the URL file
	 * @param url  The URL of the file to load into the new document.
	 */
	public VADocument( final java.net.URL url ) {
		setSource( url );

		// queue to synchronize readbacks with setpoints as well as the online model
		final DispatchQueue modelSyncQueue = DispatchQueue.createSerialQueue( "Model Sync Queue" );
		MODEL_SYNC_TIMER = DispatchTimer.getCoalescingInstance( DispatchQueue.createSerialQueue( "" ), getOnlineModelSynchronizer() );

		// set the default model sync period to 1 second
		_modelSyncPeriod = 1000;

		READBACK_SET_RECORDS = new ArrayList<ReadbackSetRecord>();
		
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
		_windowReference = windowReference;
		READBACK_SET_TABLE_MODEL = new KeyValueFilteredTableModel<ReadbackSetRecord>( new ArrayList<ReadbackSetRecord>(), "node.id", "readbackChannel.channelName", "lastReadback", "setpointChannel.channelName", "lastSetpoint" );
		READBACK_SET_TABLE_MODEL.setColumnClass( "lastReadback", Number.class );
		READBACK_SET_TABLE_MODEL.setColumnClass( "lastSetpoint", Number.class );
		READBACK_SET_TABLE_MODEL.setColumnName( "node.id", "Node" );
		READBACK_SET_TABLE_MODEL.setColumnName( "readbackChannel.channelName", "Readback PV" );
		READBACK_SET_TABLE_MODEL.setColumnName( "lastReadback", "Readback" );
		READBACK_SET_TABLE_MODEL.setColumnName( "setpointChannel.channelName", "Setpoint PV" );
		READBACK_SET_TABLE_MODEL.setColumnName( "lastSetpoint", "Setpoint" );
		
		final JTextField filterField = (JTextField)windowReference.getView( "FilterField" );
		READBACK_SET_TABLE_MODEL.setInputFilterComponent( filterField );
		
		makeTextDocument();
		
		// probe file management
		_probeFileTracker = new RecentFileTracker( 1, this.getClass(), "recent_probes" );
		
		_lastUpdate = new Date();

		if ( url == null )  return;
	}
	
	
	/** Make a main window by instantiating the my custom window. Set the text pane to use the textDocument variable as its document. */
	public void makeMainWindow() {
		mainWindow = (XalWindow)_windowReference.getWindow();
		
		final JTable readbackTable = (JTable)_windowReference.getView( "ReadbackTable" );
		readbackTable.setCellSelectionEnabled( true );
		readbackTable.setModel( READBACK_SET_TABLE_MODEL );
		
		makeNoiseDialog();
		
		if (getSource() != null) {
			java.net.URL url = getSource();
			DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			update( documentAdaptor.childAdaptor("VADocument") );
		}

		setHasChanges(false);
	}


	/** get the model sync period in milliseconds */
	public long getModelSyncPeriod() {
		return _modelSyncPeriod;
	}


	/** update the model sync period in milliseconds */
	public void setModelSyncPeriod( final long period ) {
		_modelSyncPeriod = period;
		MODEL_SYNC_TIMER.startNowWithInterval( _modelSyncPeriod, 0 );
		setHasChanges( true );
	}
	
	
	/** Make the noise dialog box */
	private void makeNoiseDialog() {
		JPanel settingPanel = new JPanel();
		JPanel noiseLevelPanel = new JPanel();
		JPanel offsetPanel = new JPanel();
		
		// for noise %
		noiseLevelPanel.setLayout(new GridLayout(7, 1));
		noiseLevelPanel.add(new JLabel("Noise Level for Device Type:"));
		
		JLabel percent = new JLabel("%");
		NumberFormat numberFormat;
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(3);
        
		JPanel noiseLevel1 = new JPanel();
		noiseLevel1.setLayout(new GridLayout(1, 3));
		JLabel label1 = new JLabel("Quad: ");
		df1 = new DecimalField( 0., 5, numberFormat );
		noiseLevel1.add(label1);
		noiseLevel1.add(df1);
		noiseLevel1.add(percent);
		noiseLevelPanel.add(noiseLevel1);

		JPanel noiseLevel2 = new JPanel();
		noiseLevel2.setLayout(new GridLayout(1, 3));
		JLabel label2 = new JLabel("Bending Dipole: ");
		percent = new JLabel("%");
		df2 = new DecimalField( 0., 5, numberFormat );
		noiseLevel2.add(label2);
		noiseLevel2.add(df2);
		noiseLevel2.add(percent);
		noiseLevelPanel.add(noiseLevel2);
		
		JPanel noiseLevel3 = new JPanel();
		noiseLevel3.setLayout(new GridLayout(1, 3));
		df3 = new DecimalField( 0., 5, numberFormat );
		noiseLevel3.add(new JLabel("Dipole Corr.: "));
		noiseLevel3.add(df3);
		noiseLevel3.add(new JLabel("%"));
		noiseLevelPanel.add(noiseLevel3);
		
		JPanel noiseLevel5 = new JPanel();
		noiseLevel5.setLayout(new GridLayout(1, 3));
		df5 = new DecimalField( 0., 5, numberFormat );
		noiseLevel5.add(new JLabel("Solenoid: "));
		noiseLevel5.add(df5);
		noiseLevel5.add(new JLabel("%"));
		noiseLevelPanel.add(noiseLevel5);
		
		JPanel noiseLevel4 = new JPanel();
		noiseLevel4.setLayout(new GridLayout(1, 3));
		df4 = new DecimalField( 0., 5, numberFormat );
		noiseLevel4.add(new JLabel("BPM: "));
		noiseLevel4.add(df4);
		noiseLevel4.add(new JLabel("%"));
		noiseLevelPanel.add(noiseLevel4);
		
		// for offsets
		offsetPanel.setLayout(new GridLayout(7, 1));
		offsetPanel.add(new JLabel("Offset for Device Type:"));
		
		JPanel offset1 = new JPanel();
		offset1.setLayout(new GridLayout(1, 2));
		df11 = new DecimalField( 0., 5, numberFormat );
		offset1.add(new JLabel("Quad: "));
		offset1.add(df11);
		offsetPanel.add(offset1);
		
		JPanel offset2 = new JPanel();
		offset2.setLayout(new GridLayout(1, 2));
		df21 = new DecimalField( 0., 5, numberFormat );
		offset2.add(new JLabel("Bending Dipole: "));
		offset2.add(df21);
		offsetPanel.add(offset2);
		
		JPanel offset3 = new JPanel();
		offset3.setLayout(new GridLayout(1, 2));
		df31 = new DecimalField( 0., 5, numberFormat );
		offset3.add(new JLabel("Dipole Corr.: "));
		offset3.add(df31);
		offsetPanel.add(offset3);
		
		JPanel offset5 = new JPanel();
		offset5.setLayout(new GridLayout(1, 2));
		df51 = new DecimalField( 0., 5, numberFormat );
		offset5.add(new JLabel("Solenoid: "));
		offset5.add(df51);
		offsetPanel.add(offset5);
		
		JPanel offset4 = new JPanel();
		offset4.setLayout(new GridLayout(1, 2));
		df41 = new DecimalField( 0., 5, numberFormat );
		offset4.add(new JLabel("BPM: "));
		offset4.add(df41);
		offsetPanel.add(offset4);
		
		// put everything together
		setNoise.setBounds(300, 300, 300, 300);
		setNoise.setTitle("Set Noise Level...");
		settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
		settingPanel.add(noiseLevelPanel);
		settingPanel.add(offsetPanel);
		setNoise.getContentPane().setLayout(new BorderLayout());
		setNoise.getContentPane().add(settingPanel, BorderLayout.CENTER);
		setNoise.getContentPane().add(done, BorderLayout.SOUTH);
		done.setActionCommand("noiseSet");
		done.addActionListener(this);
		setNoise.pack();
	}
	
	

	/**
	 * Save the document to the specified URL.
	 * @param url The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {

		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor daLevel1 = xda.createChild("VA");
		//save accelerator file
		DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
		daXMLFile.setValue("xmlFile", this.getAcceleratorFilePath());
		//save probe file
		if (theProbeFile != null) {
			DataAdaptor envProbeXMLFile = daLevel1.createChild("env_probe");
			envProbeXMLFile.setValue("probeXmlFile", theProbeFile);
		}

		// save selected sequences
		List<String> sequenceNames;
		if ( getSelectedSequence() != null ) {
			DataAdaptor daSeq = daLevel1.createChild("sequences");
			daSeq.setValue("name", getSelectedSequence().getId());
			if ( getSelectedSequence() instanceof AcceleratorSeqCombo ) {
				AcceleratorSeqCombo asc = (AcceleratorSeqCombo) getSelectedSequence();
				sequenceNames = asc.getConstituentNames();
			}
            else {
				sequenceNames = new ArrayList<String>();
				sequenceNames.add( getSelectedSequence().getId() );
			}

            for ( final String sequenceName : sequenceNames ) {
				DataAdaptor daSeqComponents = daSeq.createChild( "seq" );
				daSeqComponents.setValue( "name", sequenceName );
			}
		}

		daLevel1.setValue( "modelSyncPeriod", _modelSyncPeriod );

		xda.writeToUrl(url);
		setHasChanges(false);
	}
	
	
	/**
	 * Instantiate a new PlainDocument that servers as the document for the text
	 * pane. Create a handler of text actions so we can determine if the
	 * document has changes that should be saved.
	 */
	private void makeTextDocument() {
		textDocument = new PlainDocument();
		textDocument.addDocumentListener(new DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				setHasChanges(true);
			}

			public void removeUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}

			public void insertUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}
		});
	}

    
	/** Create the default probe from the edit context. */
	private void createDefaultProbe() {
		if ( selectedSequence != null ) {
            try {
                myProbe = ( selectedSequence instanceof xal.smf.Ring ) ? createRingProbe( selectedSequence ) : createEnvelopeProbe( selectedSequence );
                
                if ( selectedSequence instanceof xal.smf.Ring ) {
                    final TransferMapState state = (TransferMapState) myProbe.createProbeState();
                    // set initial x, y slightly off-axis to introduce betatron oscillation.
                    state.setPhaseCoordinates( new PhaseVector( 0.01, 0., 0.01, 0., 0., 0. ) );
                    myProbe.applyState( state );
                }
                
                model.setProbe( myProbe );
            }
            catch ( Exception exception ) {
                displayError( "Error Creating Probe", "Probe Error", exception );
            }
		}
	}
    
    
	/** create a new ring probe */
	static private Probe createRingProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker( sequence );
		return ProbeFactory.getTransferMapProbe( sequence, tracker );
	}
    
    
	/** create a new envelope probe */
	static private Probe createEnvelopeProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final EnvelopeTracker tracker = AlgorithmFactory.createEnvelopeTracker( sequence );
		return ProbeFactory.getEnvelopeProbe( sequence, tracker );
	}


	protected void customizeCommands(Commander commander) {

		// action for probe XML file open
		Action openprobeAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				final String currentDirectory = _probeFileTracker.getRecentFolderPath();

				JFileChooser fileChooser = new JFileChooser( currentDirectory );
				fileChooser.addChoosableFileFilter( new ProbeFileFilter() );

				int status = fileChooser.showOpenDialog( getMainWindow() );
				if (status == JFileChooser.APPROVE_OPTION) {
					_probeFileTracker.cacheURL( fileChooser.getSelectedFile() );
					File file = fileChooser.getSelectedFile();
					theProbeFile = file.getPath();
					try {
						myProbe = ProbeXmlParser.parse( theProbeFile );
						model.setProbe( myProbe );
					} catch (ParsingException e) {
						System.err.println(e);
					}
				}
			}
		};
		openprobeAction.putValue(Action.NAME, "openprobe");
		commander.registerAction(openprobeAction);

		// open probe editor
        // TODO: implement probe editor support
		Action probeEditorAction = new AbstractAction("probe-editor") {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
                displayError( "Probe Editor Error", "Probe Editor is not implemented." );
                throw new RuntimeException( "Probe editor is not implemented." );

//				SimpleProbeEditor spe = new SimpleProbeEditor();
//
//				// if model has a probe
//				if (model.getProbe() != null) {
//					//reset the probe to initial state
//					model.resetProbe();
//					spe.createSimpleProbeEditor(model.getProbe());
//					// if model has no probe
//				} else {
//					// if a probe file exists, start with existing probe file
//					if (theProbeFile != null) {
//						spe.createSimpleProbeEditor(new File(theProbeFile));
//						// if no probe file exitst, start with an empty one
//					} else {
//						spe.createSimpleProbeEditor();
//					}
//				}
//				// update the model probe with the one from probe editor
//				if (spe.probeHasChanged()) {
//					//			  mxProxy.setNewProbe(spe.getProbe());
//					if (myProbe instanceof EnvelopeProbe)
//						myProbe = (EnvelopeProbe) spe.getProbe();
//					else if (myProbe instanceof TransferMapProbe)
//						myProbe = (TransferMapProbe) spe.getProbe();
//					else 
//						myProbe = spe.getProbe();
//					
//					model.setProbe(myProbe);
//				}
			}
		};
		probeEditorAction.putValue(Action.NAME, "probe-editor");
		commander.registerAction(probeEditorAction);

		// action for using online model as engine
		olmModel.setSelected(true);
		olmModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				isForOLM = true;
				isFromPVLogger = false;
			}
		});
		commander.registerModel("olm", olmModel);

		// action for using PV logger snapshot through online model
		pvlogModel.setSelected(false);
		pvlogModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				isForOLM = true;
				isFromPVLogger = true;

				if (pvLogSelector == null) {
					// for PV Logger snapshot chooser
					plsc = new PVLogSnapshotChooser();
					pvLogSelector = plsc.choosePVLogId();
				} else
					pvLogSelector.setVisible(true);
			}
		});
		commander.registerModel("pvlogger", pvlogModel);
		
		// action for direct replaying of PVLogger logged data
		pvlogMovieModel.setSelected(false);
		pvlogMovieModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				isForOLM = false;
				isFromPVLogger = true;

				if (pvLogSelector == null) {
					// for PV Logger snapshot chooser
					plsc = new PVLogSnapshotChooser();
					pvLogSelector = plsc.choosePVLogId();
				} else
					pvLogSelector.setVisible(true);
			}
		});
		commander.registerModel("pvlogMovie", pvlogMovieModel);		

		// action for running model and Diagnostics acquisition
		Action runAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				if ( vaRunning ) {
					JOptionPane.showMessageDialog( getMainWindow(), "Virtual Accelerator has already started.", "Warning!", JOptionPane.PLAIN_MESSAGE );
					return;
				}

				if ( getSelectedSequence() == null ) {
					JOptionPane.showMessageDialog( getMainWindow(), "You need to select sequence(s) first.", "Warning!", JOptionPane.PLAIN_MESSAGE );
				} 
				else {
					// use PV logger
					if ( isFromPVLogger ) {
						long pvLoggerId = plsc.getPVLogId();
						
						runServer();

						plds = new PVLoggerDataSource(pvLoggerId);
						
						// use PVLogger to construct the model
						if (isForOLM) {
							// load the settings from the PV Logger
							putSetPVsFromPVLogger();
							// synchronize with the online model
							MODEL_SYNC_TIMER.setEventHandler( getOnlineModelSynchronizer() );
						}
						else {		// directly use PVLogger data for replay
							MODEL_SYNC_TIMER.setEventHandler( getPVLoggerSynchronizer() );
						}
					}
					// use online model
					else {
						if ( theProbeFile == null && myProbe == null ) {
							createDefaultProbe();
							if ( myProbe == null ) {
								displayWarning( "Warning!", "You need to select probe file first." );
								return;
							}
							actionPerformed( event );
						} 
						else {
							runServer();
						} 
                        
                        // put the initial B_Book PVs to the server
                        configFieldBookPVs();
                        
						//put "set" PVs to the server
						putSetPVs();
						
						// continuously loop through the next 3 steps
						MODEL_SYNC_TIMER.setEventHandler( getOnlineModelSynchronizer() );
					}
					
					MODEL_SYNC_TIMER.startNowWithInterval( _modelSyncPeriod, 0 );
					MODEL_SYNC_TIMER.resume();
				}
			}
		};
		runAction.putValue(Action.NAME, "run-va");
		commander.registerAction(runAction);

		// stop the channel access server
		Action stopAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				stopServer();
			}
		};
		
		stopAction.putValue(Action.NAME, "stop-va");
		commander.registerAction(stopAction);

		// set noise level
		Action setNoiseAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				setNoise.setVisible(true);
			}
		};
		setNoiseAction.putValue(Action.NAME, "set-noise");
		commander.registerAction(setNoiseAction);

		// configure synchronization
		final Action synchConfigAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				final String result = JOptionPane.showInputDialog( getMainWindow(), "Set the Model Synchronization Period (milliseconds): ", _modelSyncPeriod );
				if ( result != null ) {
					try {
						final long modelSyncPeriod = Long.parseLong( result );
						setModelSyncPeriod( modelSyncPeriod );
					}
					catch( Exception exception ) {
						displayError( "Error setting Model Sync Period!", exception.getMessage() );
					}
				}
			}
		};
		synchConfigAction.putValue( Action.NAME, "sync-config" );
		commander.registerAction( synchConfigAction );
	}
	
	
	/** handle this document being closed */
	protected void willClose() {
		System.out.println( "Document will be closed" );
		destroyServer();
	}
	

	public void update( final DataAdaptor adaptor ) {
		if ( getSource() != null ) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl( getSource(), false );
			DataAdaptor da1 = xda.childAdaptor( "VA" );

			//restore accelerator file
			this.setAcceleratorFilePath( da1.childAdaptor( "accelerator" ).stringValue( "xmlFile" ) );

			String accelUrl = "file://" + this.getAcceleratorFilePath();
			try {
				XMLDataManager dMgr = new XMLDataManager(accelUrl);
				this.setAccelerator( dMgr.getAccelerator(), this.getAcceleratorFilePath() );
			} catch (Exception exception) {
				JOptionPane.showMessageDialog( null, "Hey - I had trouble parsing the accelerator input xml file you fed me", "VA error", JOptionPane.ERROR_MESSAGE );
			}
			this.acceleratorChanged();

			// set up the right sequence combo from selected primaries:
			List<DataAdaptor> temp = da1.childAdaptors( "sequences" );
			if ( temp.isEmpty() )  return; // bail out, nothing left to do

			ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
			DataAdaptor da2a = da1.childAdaptor( "sequences" );
			String seqName = da2a.stringValue( "name" );

			temp = da2a.childAdaptors("seq");
            for ( final DataAdaptor da : temp ) {
				seqs.add( getAccelerator().getSequence( da.stringValue("name") ) );
			}
			if (seqName.equals("Ring"))
				setSelectedSequence(new Ring(seqName, seqs));
			else 
				setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
			
			setSelectedSequenceList(seqs.subList(0, seqs.size()));

			//restore probe file
			if (da1.hasAttribute("env_probe")) { 
				DataAdaptor probeFile = da1.childAdaptor("env_probe");
				theProbeFile = probeFile.stringValue("probeXmlFile");
				if (theProbeFile.length() > 1) {
					try {
						myProbe = (EnvelopeProbe) ProbeXmlParser.parse(theProbeFile);
					} catch (ParsingException e) {
						// if we have trouble restore the probe, just create from default
						createDefaultProbe();
					}
				} 
			}
			else {
				createDefaultProbe();
			}

			model.setProbe(myProbe);

			if ( da1.hasAttribute( "modelSyncPeriod" ) ) {
				_modelSyncPeriod = da1.longValue( "modelSyncPeriod" );
			}
		}

	}
    
    
	protected Scenario getScenario() {
		return model;
	}
    
    
	protected boolean isVARunning() {
		return vaRunning;
	}
    
    
    /** update the limit channels based on changes to the Field Book channels */
    private void updateLimitChannels() {
        for ( final Electromagnet magnet : mags ) {
            try {
				final Channel bookChannel = magnet.getMainSupply().findChannel( MagnetMainSupply.FIELD_BOOK_HANDLE );
				final Channel fieldChannel = magnet.getMainSupply().findChannel( MagnetMainSupply.FIELD_SET_HANDLE );
                if ( bookChannel != null ) {
                    if ( bookChannel.isConnected() ) {
                        final double bookField = bookChannel.getValDbl();
                        final double warningOffset = 0.05 * Math.abs( bookField );
                        final double alarmOffset = 0.1 * Math.abs( bookField );
                        
                        final String[] warningPVs = fieldChannel.getWarningLimitPVs();
                        
                        final Channel lowerWarningChannel = ChannelFactory.defaultFactory().getChannel( warningPVs[0], fieldChannel.getValueTransform() );
//                        System.out.println( "Lower Limit PV: " + lowerWarningChannel.channelName() );
                        if ( lowerWarningChannel.connectAndWait() ) {
                            lowerWarningChannel.putValCallback( bookField - warningOffset, this );
                        }
                        
                        final Channel upperWarningChannel = ChannelFactory.defaultFactory().getChannel( warningPVs[1], fieldChannel.getValueTransform() );
                        if ( upperWarningChannel.connectAndWait() ) {
                            upperWarningChannel.putValCallback( bookField + warningOffset, this );
                        }
                        
                        final String[] alarmPVs = fieldChannel.getAlarmLimitPVs();
                        
                        final Channel lowerAlarmChannel = ChannelFactory.defaultFactory().getChannel( alarmPVs[0], fieldChannel.getValueTransform() );
                        if ( lowerAlarmChannel.connectAndWait() ) {
                            lowerAlarmChannel.putValCallback( bookField - alarmOffset, this );
                        }
                        
                        final Channel upperAlarmChannel = ChannelFactory.defaultFactory().getChannel( alarmPVs[1], fieldChannel.getValueTransform() );
                        if ( upperAlarmChannel.connectAndWait() ) {
                            upperAlarmChannel.putValCallback( bookField + alarmOffset, this );
                        }
                    }
                }
			} 
            catch ( NoSuchChannelException exception ) {
				System.err.println( exception.getMessage() );
			} 
            catch ( ConnectionException exception ) {
				System.err.println( exception.getMessage() );
			} 
            catch ( GetException exception ) {
				System.err.println( exception.getMessage() );
			} 
            catch ( PutException exception ) {
				System.err.println( exception.getMessage() );
			}
        }
        
        Channel.flushIO();
    }
    
    
	/** This method is for populating the readback PVs */
	private void putReadbackPVs() {
		// set beam trigger PV to "on"
		try {
			final Date now = new Date();
			if ( _repRateChannel != null ) {
				final double updatePeriod = 0.001 * ( now.getTime() - _lastUpdate.getTime() );	// period of update in seconds
				_repRateChannel.putValCallback( 1.0 / updatePeriod , this );
			}
			_lastUpdate = now;
			if ( beamOnEvent != null )  beamOnEvent.putValCallback( 0, this );
			beamOnEventCounter++;
			if ( beamOnEventCount != null )  beamOnEventCount.putValCallback( beamOnEventCounter, this );
			if ( slowDiagEvent != null )  slowDiagEvent.putValCallback( 0, this );
		} catch (ConnectionException e) {
			System.err.println(e);
		} catch (PutException e) {
			System.err.println(e);
		}
        
		// get the "set" PV value, add noise, and then put to the corresponding readback PV.
		for ( final ReadbackSetRecord record : READBACK_SET_RECORDS ) {
			try {
				record.updateReadback( ch_noiseMap, ch_offsetMap, this );
			} 
			catch (Exception e) {
				System.err.println( e.getMessage() );
			} 
		}
		Channel.flushIO();
		final int rowCount = READBACK_SET_TABLE_MODEL.getRowCount();
		if ( rowCount > 0 ) {
			READBACK_SET_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
		}
        
        updateLimitChannels();
	}
    
    
    /** populate the readback PVs from the PV Logger */
	private void putReadbackPVsFromPVLogger() {
		final Map<String,Double> qPVMap = plds.getMagnetMap();

		// set beam trigger PV to "on"
		try {
			if ( beamOnEvent != null )  beamOnEvent.putVal(0);
			beamOnEventCounter++;
			if ( beamOnEventCount != null )  beamOnEventCount.putVal(beamOnEventCounter);
			if ( slowDiagEvent != null )  slowDiagEvent.putVal( 0 );
		} catch (ConnectionException e) {
			System.err.println(e);
		} catch (PutException e) {
			System.err.println(e);
		}

		// get the "set" PV value, add noise, and then put to the corresponding readback PV.
		for ( final ReadbackSetRecord record : READBACK_SET_RECORDS ) {
			try {
				final String readbackPV = record.getReadbackChannel().channelName();

				if ( qPVMap.containsKey( readbackPV ) ) {
					final double basisValue = qPVMap.get( readbackPV ).doubleValue();
					record.updateReadback( basisValue, ch_noiseMap, ch_offsetMap, this );
				}
			} 
			catch ( Exception e ) {
				System.err.println( e.getMessage() );
			} 
		}
		READBACK_SET_TABLE_MODEL.fireTableDataChanged();
	}
    
    
    /** initialize the field book PVs from the default values */
    private void configFieldBookPVs() {
        for ( final Electromagnet magnet : mags ) {
            try {
				final Channel bookChannel = magnet.getMainSupply().findChannel( MagnetMainSupply.FIELD_BOOK_HANDLE );
                if ( bookChannel != null ) {
                    if ( bookChannel.connectAndWait() ) {
                        final double bookField = magnet.toCAFromField( magnet.getDfltField() );
                        bookChannel.putValCallback( bookField, this );
                    }
                }
			} 
            catch ( NoSuchChannelException exception ) {
				System.err.println( exception.getMessage() );
			} 
            catch ( ConnectionException exception ) {
				System.err.println( exception.getMessage() );
			} 
            catch ( PutException exception ) {
				System.err.println( exception.getMessage() );
			}
            catch ( NullPointerException exception ) {
            	System.err.println( exception.getMessage() );
            }
        }
    }
    

	/**
	 * populate all the "set" PV values from design values
	 */
	private void putSetPVs() {
		// for all magnets
        for ( final Electromagnet em : mags ) {
			try {
				Channel ch = em.getMainSupply().getAndConnectChannel( MagnetMainSupply.FIELD_SET_HANDLE );
				final double setting = em.toCAFromField( em.getDfltField() );
				//System.out.println("Ready to put " + setting + " to " + ch.getId());
				ch.putValCallback( setting, this);
				
				if ( em instanceof TrimmedQuadrupole ) {
					Channel trimChannel = ((TrimmedQuadrupole)em).getTrimSupply().getAndConnectChannel( MagnetTrimSupply.FIELD_SET_HANDLE );
					//System.out.println("Ready to put " + 0.0 + " to " + trimChannel.getId());
					trimChannel.putValCallback( 0.0, this);
				}
			}
            catch (NoSuchChannelException e) {
				System.err.println(e.getMessage());
			}
            catch (ConnectionException e) {
				System.err.println(e.getMessage());
			}
            catch (PutException e) {
				System.err.println(e.getMessage());
			}
			catch (NullPointerException e) {
				System.err.println(e.getMessage());
			}
		}

		// for all rf cavities
        for ( final RfCavity rfCavity : rfCavities ) {
			try {
				Channel ampSetCh = rfCavity
						.getAndConnectChannel(RfCavity.CAV_AMP_SET_HANDLE);
				//System.out.println("Ready to put " + rfCavity.getDfltCavAmp() + " to " + ampSetCh.getId());
				if (rfCavity instanceof xal.smf.impl.SCLCavity) {
					ampSetCh.putValCallback( rfCavity.getDfltCavAmp()*((SCLCavity)rfCavity).getStructureTTF(), this );
				}
				else {
					ampSetCh.putValCallback( rfCavity.getDfltCavAmp(), this );
				}
				Channel phaseSetCh = rfCavity.getAndConnectChannel(RfCavity.CAV_PHASE_SET_HANDLE);
				//System.out.println("Ready to put " + rfCavity.getDfltCavPhase() + " to " + phaseSetCh.getId());
				phaseSetCh.putValCallback( rfCavity.getDfltCavPhase(), this );
			} catch (NoSuchChannelException e) {
				System.err.println(e.getMessage());
			} catch (ConnectionException e) {
				System.err.println(e.getMessage());
			} catch (PutException e) {
				System.err.println(e.getMessage());
			}
		}
		Channel.flushIO();
	}

	private void putSetPVsFromPVLogger() {
		final Map<String,Double> qPSPVMap = plds.getMagnetPSMap();

        for ( final Electromagnet em : mags ) {
			try {
				Channel ch = em.getMainSupply().getAndConnectChannel( MagnetMainSupply.FIELD_SET_HANDLE );
				//System.out.println("Ready to put " + Math.abs(em.getDfltField()) + " to " + ch.getId());

                final String channelID = ch.getId();
				if ( qPSPVMap.containsKey( channelID ) )
					ch.putValCallback( qPSPVMap.get( channelID ).doubleValue(), this );
			}
            catch (NoSuchChannelException e) {
				System.err.println(e.getMessage());
			}
            catch (ConnectionException e) {
				System.err.println(e.getMessage());
			}
            catch (PutException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/** This method is for populating the diagnostic PVs (only BPMs + WSs for now) */
	protected void putDiagPVs() {
		// for BPMs
        for ( final BPM bpm : bpms ) {
			final Channel bpmXAvgChannel = bpm.getChannel( BPM.X_AVG_HANDLE );
			final Channel bpmXTBTChannel = bpm.getChannel( BPM.X_TBT_HANDLE );
			final Channel bpmYAvgChannel = bpm.getChannel( BPM.Y_AVG_HANDLE );
            final Channel bpmYTBTChannel = bpm.getChannel( BPM.Y_TBT_HANDLE );
			final Channel bpmAmpAvgChannel = bpm.getChannel( BPM.AMP_AVG_HANDLE );
            
            
			try {
				ProbeState probeState = model.getTrajectory().stateForElement( bpm.getId() );
				//System.out.println("Now updating " + bpm.getId());

				if ( probeState instanceof ICoordinateState ) {
					final PhaseVector coordinates = ((ICoordinateState)probeState).getFixedOrbit();
					// For SNS Ring BPM system, we only measure the signal with respect to the center of the beam pipe.
                    
                    // TO-DO: the turn by turn arrays should really be generated from betatron motion rather than random data about the nominal
                    final double[] xTBT = NoiseGenerator.noisyArrayForNominal( coordinates.getx() * 1000.0, DEFAULT_BPM_WAVEFORM_SIZE, DEFAULT_BPM_WAVEFORM_DATA_SIZE, bpmNoise, bpmOffset );
                    final double xAvg = NoiseGenerator.getAverage( xTBT, DEFAULT_BPM_WAVEFORM_DATA_SIZE );
                    
                    final double[] yTBT = NoiseGenerator.noisyArrayForNominal( coordinates.gety() * 1000.0, DEFAULT_BPM_WAVEFORM_SIZE, DEFAULT_BPM_WAVEFORM_DATA_SIZE, bpmNoise, bpmOffset );
                    final double yAvg = NoiseGenerator.getAverage( yTBT, DEFAULT_BPM_WAVEFORM_DATA_SIZE );
                    
					bpmXAvgChannel.putValCallback( xAvg, this );
//                    bpmXTBTChannel.putValCallback( xTBT, this );  // don't post to channel access until the turn by turn data is generated correctly
					bpmYAvgChannel.putValCallback( yAvg, this );
//                    bpmYTBTChannel.putValCallback( yTBT, this );  // don't post to channel access until the turn by turn data is generated correctly
				}

				// hardwired BPM amplitude noise and offset to 5% and 0.1mm (randomly) respectively
				bpmAmpAvgChannel.putVal( NoiseGenerator.setValForPV( 20., 5., 0.1 ) );
				// calculate the BPM phase (for linac only)
				if ( !( myProbe instanceof TransferMapProbe ) && !( bpm instanceof RingBPM ) ) {
					final Channel bpmPhaseAvgChannel = bpm.getChannel( BPM.PHASE_AVG_HANDLE );
					bpmPhaseAvgChannel.putValCallback( probeState.getTime() * 360. * ( ( (BPMBucket)bpm.getBucket("bpm") ).getFrequency() * 1.e6 ) % 360.0, this );
				}
			} catch (ConnectionException e) {
				System.err.println( e.getMessage() );
			} catch (PutException e) {
				System.err.println( e.getMessage() );
			}
		}

		// for WSs
        for ( final ProfileMonitor ws : wss ) {
			Channel wsX = ws.getChannel(ProfileMonitor.H_SIGMA_M_HANDLE);
			Channel wsY = ws.getChannel(ProfileMonitor.V_SIGMA_M_HANDLE);

			try {
				ProbeState probeState = model.getTrajectory().stateForElement( ws.getId() );
				if (model.getProbe() instanceof EnvelopeProbe) {
                    final Twiss[] twiss = ( (EnvelopeProbeState)probeState ).getCorrelationMatrix().computeTwiss();
					wsX.putValCallback( twiss[0].getEnvelopeRadius() * 1000., this );
					wsY.putValCallback( twiss[1].getEnvelopeRadius() * 1000., this );
				}
			} catch (ConnectionException e) {
				System.err.println( e.getMessage() );
			} catch (PutException e) {
				System.err.println( e.getMessage() );
			}
		}
		Channel.flushIO();
	}

	private void putDiagPVsFromPVLogger() {
		// for BPMs
		final Map<String,Double> bpmXMap = plds.getBPMXMap();
		final Map<String,Double> bpmYMap = plds.getBPMYMap();
		final Map<String,Double> bpmAmpMap = plds.getBPMAmpMap();
		final Map<String,Double> bpmPhaseMap = plds.getBPMPhaseMap();

        for ( final BPM bpm : bpms ) {
			Channel bpmX = bpm.getChannel(BPM.X_AVG_HANDLE);
			Channel bpmY = bpm.getChannel(BPM.Y_AVG_HANDLE);
			Channel bpmAmp = bpm.getChannel(BPM.AMP_AVG_HANDLE);

			try {
				System.err.println("Now updating " + bpm.getId());

				if ( bpmXMap.containsKey( bpmX.getId() ) ) {
					bpmX.putVal( NoiseGenerator.setValForPV( bpmXMap.get( bpmX.getId() ).doubleValue(), bpmNoise, bpmOffset ) );
				}
				
				if ( bpmYMap.containsKey( bpmY.getId() ) ) {
					bpmY.putVal( NoiseGenerator.setValForPV( bpmYMap.get( bpmY.getId() ).doubleValue(), bpmNoise, bpmOffset ) );
				}

				// BPM amplitude
				if (bpmAmpMap.containsKey(bpmAmp.getId()))
					bpmAmp.putVal( NoiseGenerator.setValForPV( bpmAmpMap.get( bpmAmp.getId() ).doubleValue(), 5., 0.1) );
				// BPM phase (for linac only)
				if ( !( myProbe instanceof TransferMapProbe ) ) {
					Channel bpmPhase = bpm.getChannel( BPM.PHASE_AVG_HANDLE );
					if ( bpmPhaseMap.containsKey( bpmPhase.getId() ) ) {
						bpmPhase.putVal( bpmPhaseMap.get( bpmPhase.getId() ).doubleValue() );
					}
				}

			} catch ( ConnectionException e ) {
				System.err.println( e.getMessage() );
			} catch ( PutException e ) {
				System.err.println( e.getMessage() );
			}
		}

	}
	
	
	/** handle the CA put callback */
	public void putCompleted( final Channel chan ) {}
	

	/** create the map between the "readback" and "set" PVs */
	private void configureReadbacks() {
		READBACK_SET_RECORDS.clear();
		
		ch_noiseMap = new LinkedHashMap<Channel, Double>();
		ch_offsetMap = new LinkedHashMap<Channel, Double>();
		
		if ( selectedSequence != null ) {
			// for magnet PVs
            for ( final Electromagnet em : mags ) {
				READBACK_SET_RECORDS.add( new ReadbackSetRecord( em, em.getChannel( Electromagnet.FIELD_RB_HANDLE ), em.getChannel( MagnetMainSupply.FIELD_SET_HANDLE ) ) );
				
				// handle the trimmed magnets
				if ( em.isKindOf( TrimmedQuadrupole.s_strType ) ) {
					READBACK_SET_RECORDS.add( new ReadbackSetRecord( em, em.getChannel( MagnetTrimSupply.FIELD_RB_HANDLE ), em.getChannel( MagnetTrimSupply.FIELD_SET_HANDLE ) ) );
					ch_noiseMap.put( em.getChannel( MagnetTrimSupply.FIELD_RB_HANDLE ), 0.0 );
					ch_offsetMap.put( em.getChannel( MagnetTrimSupply.FIELD_RB_HANDLE ), 0.0 );
				}
				
				// set up the map between the magnet readback PV and its noise level
				if ( em.isKindOf( Quadrupole.s_strType ) ) {
					ch_noiseMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE), quadNoise );
					ch_offsetMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE), quadOffset );
				} 
				else if ( em.isKindOf( Bend.s_strType ) ) {
					ch_noiseMap.put( em.getChannel(Electromagnet.FIELD_RB_HANDLE), dipoleNoise );
					ch_offsetMap.put( em.getChannel(Electromagnet.FIELD_RB_HANDLE), dipoleOffset );
				} 
				else if ( em.isKindOf( HDipoleCorr.s_strType ) || em.isKindOf( VDipoleCorr.s_strType ) ) {
					ch_noiseMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE ), correctorNoise );
					ch_offsetMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE ), correctorOffset );
				}
				else if ( em.isKindOf( Solenoid.s_strType ) ) {
					ch_noiseMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE ), solNoise );
					ch_offsetMap.put( em.getChannel( Electromagnet.FIELD_RB_HANDLE ), solOffset );
				}
			}
			
			// for rf PVs
            for ( final RfCavity rfCav : rfCavities ) {
				READBACK_SET_RECORDS.add( new ReadbackSetRecord( rfCav, rfCav.getChannel( RfCavity.CAV_AMP_AVG_HANDLE ), rfCav.getChannel( RfCavity.CAV_AMP_SET_HANDLE ) ) );
				READBACK_SET_RECORDS.add( new ReadbackSetRecord( rfCav, rfCav.getChannel( RfCavity.CAV_PHASE_AVG_HANDLE ), rfCav.getChannel( RfCavity.CAV_PHASE_SET_HANDLE ) ) );
				ch_noiseMap.put( rfCav.getChannel( RfCavity.CAV_AMP_AVG_HANDLE ), rfAmpNoise );
				ch_noiseMap.put( rfCav.getChannel( RfCavity.CAV_PHASE_AVG_HANDLE ), rfPhaseNoise );
				ch_offsetMap.put( rfCav.getChannel( RfCavity.CAV_AMP_AVG_HANDLE ), rfAmpOffset );
				ch_offsetMap.put( rfCav.getChannel( RfCavity.CAV_PHASE_AVG_HANDLE ), rfPhaseOffset );
			}
			
			Collections.sort( READBACK_SET_RECORDS, new ReadbackSetRecordPositionComparator( selectedSequence ) );
			READBACK_SET_TABLE_MODEL.setRecords( new ArrayList<ReadbackSetRecord>( READBACK_SET_RECORDS ) );
		}
	}
	
	
	/** run the VA server */
	private void runServer() {
		vaRunning = true;
	}
	
	
	/** stop the VA Server */
	private void stopServer() {
		MODEL_SYNC_TIMER.suspend();
		vaRunning = false;
	}
	
	
	/** destroy the VA Server */
	void destroyServer() {
		try {
			stopServer();
			if ( _vaServer != null ) {
				_vaServer.destroy();
				_vaServer = null;
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}	
	}
	
	
	public void acceleratorChanged() {
		if (accelerator != null) {
			stopServer();
			_repRateChannel = accelerator.getTimingCenter().findChannel( TimingCenter.REP_RATE_HANDLE );
			beamOnEvent = accelerator.getTimingCenter().findChannel( TimingCenter.BEAM_ON_EVENT_HANDLE );
			beamOnEventCount = accelerator.getTimingCenter().findChannel( TimingCenter.BEAM_ON_EVENT_COUNT_HANDLE );
			slowDiagEvent = accelerator.getTimingCenter().findChannel( TimingCenter.SLOW_DIAGNOSTIC_EVENT_HANDLE );
			setHasChanges( true );
		}
	}

	public void selectedSequenceChanged() {
		destroyServer();
		
		if (selectedSequence != null) {
			try {
				_vaServer = new VAServer( selectedSequence );
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}
			
			// get electro magnets
			TypeQualifier typeQualifier = QualifierFactory.qualifierWithStatusAndTypes( true, Electromagnet.s_strType );
            mags = getSelectedSequence().<Electromagnet>getAllNodesWithQualifier( typeQualifier );
			
			// get all the rf cavities
			typeQualifier = typeQualifier = QualifierFactory.qualifierWithStatusAndTypes( true, RfCavity.s_strType );
			rfCavities = getSelectedSequence().getAllInclusiveNodesWithQualifier( typeQualifier );
			
			// get all the BPMs
			bpms = getSelectedSequence().<BPM>getAllNodesWithQualifier( QualifierFactory.qualifierWithStatusAndType( true, "BPM" ) );
			
			// get all the wire scanners
			wss = getSelectedSequence().getAllNodesWithQualifier( QualifierFactory.qualifierWithStatusAndType( true, ProfileMonitor.PROFILE_MONITOR_TYPE ) );
			System.out.println( wss );
			
			// should create a new map for "set" <-> "readback" PV mapping
			configureReadbacks();

			// for on-line model
			try {
				model = Scenario.newScenarioFor( getSelectedSequence() );
			}
			catch ( ModelException exception ) {
				System.err.println( exception.getMessage() );
			}

			// setting up the default probe
            createDefaultProbe();

			setHasChanges(true);
		}
	}

	public void buildOnlineModel() {
		try {
			//	 model.resetProbe();
			model.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
			model.resync();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("noiseSet")) {
			quadNoise = df1.getDoubleValue();
			dipoleNoise = df2.getDoubleValue();
			correctorNoise = df3.getDoubleValue();
			solNoise = df5.getDoubleValue();
			bpmNoise = df4.getDoubleValue();
			quadOffset = df11.getDoubleValue();
			dipoleOffset = df21.getDoubleValue();
			correctorOffset = df31.getDoubleValue();
			bpmOffset = df41.getDoubleValue();
			solOffset = df51.getDoubleValue();

			setNoise.setVisible(false);
		}
	}

	
	/** synchronize the readbacks with setpoints and synchronize with the online model */
	private void syncOnlineModel() {
		if ( vaRunning ) {
			// add noise, populate "read-back" PVs
			putReadbackPVs();

			// re-sync lattice and run model
			buildOnlineModel();

			try {
				myProbe.reset();
				model.run();

				// put diagnostic node PVs
				putDiagPVs();
			} 
			catch ( ModelException exception ) {
				System.err.println( exception.getMessage() );
			}
		}
	}


	/** Get a runnable that syncs the online model */
	private Runnable getOnlineModelSynchronizer() {
		return new Runnable() {
			public void run() {
				syncOnlineModel();
			}
		};
	}


	/** synchronize the readbacks with setpoints and synchronize with the online model */
	private void syncPVLogger() {
		if ( vaRunning ) {
			putSetPVsFromPVLogger();
			putReadbackPVsFromPVLogger();
			putDiagPVsFromPVLogger();
		}
	}


	/** Get a runnable that syncs with the PV Logger */
	private Runnable getPVLoggerSynchronizer() {
		return new Runnable() {
			public void run() {
				syncPVLogger();
			}
		};
	}
}



/** compare readback set records by their position within a sequence */
class ReadbackSetRecordPositionComparator implements Comparator<ReadbackSetRecord> {
	/** sequence within which the nodes are ordered */
	final AcceleratorSeq SEQUENCE;
	
	
	/** Constructor */
	public ReadbackSetRecordPositionComparator( final AcceleratorSeq sequence ) {
		SEQUENCE = sequence;
	}
	
	
	/** compare the records based on location relative to the start of the sequence */
	public int compare( final ReadbackSetRecord record1, final ReadbackSetRecord record2 ) {
		if ( record1 == null && record2 == null ) {
			return 0;
		}
		else if ( record1 == null ) {
			return -1;
		}
		else if ( record2 == null ) {
			return 1;
		}
		else {
			final double position1 = SEQUENCE.getPosition( record1.getNode() );
			final double position2 = SEQUENCE.getPosition( record2.getNode() );
			return position1 > position2 ? 1 : position1 < position2 ? -1 : 0;
		}
	}
	
	
	/** */
	public boolean equals( final Object object ) {
		return true;
	}
}


class ProbeFileFilter extends javax.swing.filechooser.FileFilter {
	//Accept xml files.
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = Utils.getExtension(f);
		if (extension != null) {
			if (extension.equals(Utils.probe)) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	//The description of this filter
	public String getDescription() {
		return "Probe File";
	}

}


class Utils {
	public final static String probe = "probe";

	/**
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}

