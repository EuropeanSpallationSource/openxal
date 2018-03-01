/*
 * @(#)VADocument.java          1.5 07/15/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.virtualaccelerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.extension.application.Application;
import xal.extension.application.Commander;
import xal.extension.application.XalWindow;
import xal.extension.application.smf.AcceleratorDocument;
import xal.extension.bricks.WindowReference;
import xal.extension.jels.smf.impl.ESSIonSourceCoil;
import xal.extension.jels.smf.impl.ESSIonSourceMFC;
import xal.extension.jels.smf.impl.ESSIonSourceMagnetron;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.swing.DecimalField;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;  // Probe for t3d header
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.service.pvlogger.apputils.browser.PVLogSnapshotChooser;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.NoSuchChannelException;
import xal.smf.Ring;
import xal.smf.TimingCenter;
import xal.smf.attr.BPMBucket;
import xal.smf.impl.BPM;
import xal.smf.impl.Bend;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetTrimSupply;
import xal.smf.impl.ProfileMonitor;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RingBPM;
import xal.smf.impl.SCLCavity;
import xal.smf.impl.Solenoid;
import xal.smf.impl.TrimmedQuadrupole;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.QualifierFactory;
import xal.smf.impl.qualify.TypeQualifier;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.SimpleSimResultsAdaptor;
import xal.tools.data.DataAdaptor;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.dispatch.DispatchTimer;
//TODO: CKA - Many unused imports
import xal.tools.xml.XmlDataAdaptor;

/**
 * <p>
 * <h4>CKA NOTES:</h4>
 * - In method <code>{@link #createDefaultProbe()}</code> a
 * <code>TransferMapProbe</code> is created in the case of a ring. The method
 * <code>TransferMapState#setPhaseCoordinates</code> is called to create an
 * initial static erorr. This does nothing because transfer map probes do not
 * have phase coordinates any longer, the method is deprecated.
 * <br/>
 * <br/>
 * - The static noise for the above call is hard coded. As are many features in
 * this class.
 * </p>
 *
 * VADocument is a custom AcceleratorDocument for virtual accelerator
 * application.
 *
 * @version 1.6 13 Jul 2015
 * @author Paul Chu
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class VADocument extends AcceleratorDocument implements ActionListener, PutListener {

    /**
     * default BPM waveform size
     */
    final static private int DEFAULT_BPM_WAVEFORM_SIZE = VAServer.DEFAULT_ARRAY_SIZE;

    /**
     * default BPM waveform data size (part of the waveform to populate with
     * data)
     */
    final static private int DEFAULT_BPM_WAVEFORM_DATA_SIZE = 250;

    /**
     * factory for server channels Not sure whether it is better for this to be
     * static and shared across all documents. For now we will just use a common
     * server factory across all documents (possibly prevents server conflicts).
     */
    final static private ChannelFactory CHANNEL_SERVER_FACTORY = ChannelFactory.newServerFactory();

    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;

    /**
     * For on-line model
     */
    protected Scenario modelScenario;

    /* template probe which may be configured and then copied as the currentProbe for use in the simulation */
    private Probe<?> baseProbe;

    /* probe which was copied from the base probe and is being used in the simulation */
    private Probe<?> currentProbe;

    String dataSource = Scenario.SYNC_MODE_LIVE;

    int runT3d_OK = 0;

    private JDialog setNoise = new JDialog();

    private DecimalField df_quadNoise, df_bendNoise, df_dipCorrNoise, df_bpmNoise, df_solNoise, df_rfAmpNoise, df_rfPhaseNoise;

    private DecimalField df_quadStatErr, df_bendStatErr, df_dipCorrStatErr, df_bpmStatErr, df_solStatErr, df_rfAmpStatErr, df_rfPhaseStatErr;

    private DecimalField df_quadStatHorMisalign, df_bpmStatHorMisalign, df_solStatHorMisalign, df_rfCavStatHorMisalign;
    private DecimalField df_quadStatVerMisalign, df_bpmStatVerMisalign, df_solStatVerMisalign, df_rfCavStatVerMisalign;

    private double quadNoise = 0.0;

    private double dipoleNoise = 0.0;

    private double correctorNoise = 0.0;

    private double solNoise = 0.0;

    private double bpmNoise = 0.0;

    private double rfAmpNoise = 0.0;

    private double rfPhaseNoise = 0.0;

    private double quadStaticError = 0.0;

    private double dipoleStaticError = 0.0;

    private double correctorStaticError = 0.0;

    private double solStaticError = 0.0;

    private double bpmStaticError = 0.0;

    private double rfAmpStaticError = 0.0;

    private double rfPhaseStaticError = 0.0;

    private double quadStatHorMisalign = 0.0;
    private double quadStatVerMisalign = 0.0;

    private double bpmStatHorMisalign = 0.0;
    private double bpmStatVerMisalign = 0.0;

    private JButton done = new JButton("OK");

    private volatile boolean vaRunning = false;
    // add by liyong
    private java.util.List<AcceleratorNode> nodes;     // TODO: CKA - NEVER USED
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

    private LinkedHashMap<Channel, Double> ch_staticErrorMap;

    private LinkedHashMap<BPM, Double> bpm_staticErrorMapX;
    private LinkedHashMap<BPM, Double> bpm_staticErrorMapY;

    private VAServer _vaServer;

    protected Commander commander;

//  private RecentFileTracker _probeFileTracker;
    // for on/off-line mode selection
    ToggleButtonModel olmModel = new ToggleButtonModel();

    ToggleButtonModel pvlogModel = new ToggleButtonModel();

    ToggleButtonModel pvlogMovieModel = new ToggleButtonModel();

    private boolean isFromPVLogger = false;
    private boolean isForOLM = false;

    private PVLogSnapshotChooser plsc;

    private JDialog pvLogSelector;

    private PVLoggerDataSource plds;

    /**
     * bricks window reference
     */
    private WindowReference _windowReference;

    /**
     * readback setpoint table model
     */
    private KeyValueFilteredTableModel<ReadbackSetRecord> READBACK_SET_TABLE_MODEL;

    /**
     * timer to synch the readbacks with the setpoints and also sync the model
     */
    final private DispatchTimer MODEL_SYNC_TIMER;

    /**
     * model sync period in milliseconds
     */
    private long _modelSyncPeriod;

    public DiagPlot _diagplot;

    /**
     * Create a new empty document
     */
    public VADocument() {
        this(null);

    }

    /**
     * Create a new document loaded from the URL file
     *
     * @param url The URL of the file to load into the new document.
     */
    public VADocument(final java.net.URL url) {
        setSource(url);

        // timer to synchronize readbacks with setpoints as well as the online model
        MODEL_SYNC_TIMER = DispatchTimer.getCoalescingInstance(DispatchQueue.createSerialQueue(""), getOnlineModelSynchronizer());

        // set the default model sync period to 1 second
        _modelSyncPeriod = 1000;

        READBACK_SET_RECORDS = new ArrayList<ReadbackSetRecord>();

        final WindowReference windowReference = getDefaultWindowReference("MainWindow", this);
        _windowReference = windowReference;
        READBACK_SET_TABLE_MODEL = new KeyValueFilteredTableModel<ReadbackSetRecord>(new ArrayList<ReadbackSetRecord>(), "node.id", "readbackChannel.channelName", "lastReadback", "setpointChannel.channelName", "lastSetpoint");
        READBACK_SET_TABLE_MODEL.setColumnClass("lastReadback", Number.class);
        //READBACK_SET_TABLE_MODEL.setColumnClass( "lastSetpoint", Number.class );
        READBACK_SET_TABLE_MODEL.setColumnClass("lastSetpoint", Double.class);
        READBACK_SET_TABLE_MODEL.setColumnName("node.id", "Node");
        READBACK_SET_TABLE_MODEL.setColumnName("readbackChannel.channelName", "Readback PV");
        READBACK_SET_TABLE_MODEL.setColumnName("lastReadback", "Readback");
        READBACK_SET_TABLE_MODEL.setColumnName("setpointChannel.channelName", "Setpoint PV");
        READBACK_SET_TABLE_MODEL.setColumnName("lastSetpoint", "Setpoint");

        READBACK_SET_TABLE_MODEL.setColumnEditable("lastSetpoint", true);

        final JTextField filterField = (JTextField) windowReference.getView("FilterField");
        READBACK_SET_TABLE_MODEL.setInputFilterComponent(filterField);

        makeTextDocument();

        // probe file management
        //_probeFileTracker = new RecentFileTracker( 1, this.getClass(), "recent_probes" );
        _lastUpdate = new Date();

        if (url == null) {
            return;
        }
    }

    /**
     * Override the nextChannelFactory() method to return this document's
     * channel server factory.
     *
     * @return this document's channel server factory
     */
    @Override
    public ChannelFactory nextChannelFactory() {
        //System.out.println( "Getting the server channel factory..." );
        return CHANNEL_SERVER_FACTORY;
    }

    /**
     * Make a main window by instantiating the my custom window. Set the text
     * pane to use the textDocument variable as its document.
     */
    @Override
    public void makeMainWindow() {
        mainWindow = (XalWindow) _windowReference.getWindow();

        final JTable readbackTable = (JTable) _windowReference.getView("ReadbackTable");
        readbackTable.setCellSelectionEnabled(true);
        readbackTable.setModel(READBACK_SET_TABLE_MODEL);

        /**
         * add digaplot
         */
        final FunctionGraphsJPanel beamdispplot = (FunctionGraphsJPanel) _windowReference.getView("BeamDispPlot");
        final FunctionGraphsJPanel sigamplot = (FunctionGraphsJPanel) _windowReference.getView("SigmaPlot");
        _diagplot = new DiagPlot(beamdispplot, sigamplot);

        makeNoiseDialog();

        if (getSource() != null) {
            java.net.URL url = getSource();
            DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl(url, false);
            update(documentAdaptor.childAdaptor("MpxDocument"));
        }

        setHasChanges(false);
    }

    /**
     * get the model sync period in milliseconds
     */
    public long getModelSyncPeriod() {
        return _modelSyncPeriod;
    }

    /**
     * update the model sync period in milliseconds
     */
    public void setModelSyncPeriod(final long period) {
        _modelSyncPeriod = period;
        MODEL_SYNC_TIMER.startNowWithInterval(_modelSyncPeriod, 0);
        setHasChanges(true);
    }

    /**
     * Make the noise dialog box
     */
    private void makeNoiseDialog() {
        JPanel settingPanel = new JPanel();
        JPanel noiseLevelPanel = new JPanel();
        JPanel staticErrorPanel = new JPanel();
        JPanel staticHorMisalignPanel = new JPanel();
        JPanel staticVerMisalignPanel = new JPanel();

        // for noise %
        noiseLevelPanel.setLayout(new GridLayout(8, 1));
        noiseLevelPanel.add(new JLabel("Noise Level"));

        NumberFormat numberFormat;
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(3);

        JPanel quadNoisePanel = new JPanel();
        quadNoisePanel.setLayout(new GridLayout(1, 3));
        JLabel label1 = new JLabel("Quad: ");
        df_quadNoise = new DecimalField(0., 5, numberFormat);
        quadNoisePanel.add(label1);
        quadNoisePanel.add(df_quadNoise);
        quadNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(quadNoisePanel);

        JPanel bendNoisePanel = new JPanel();
        bendNoisePanel.setLayout(new GridLayout(1, 3));
        JLabel label2 = new JLabel("Bending Dipole: ");
        df_bendNoise = new DecimalField(0., 5, numberFormat);
        bendNoisePanel.add(label2);
        bendNoisePanel.add(df_bendNoise);
        bendNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(bendNoisePanel);

        JPanel dipCorrNoisePanel = new JPanel();
        dipCorrNoisePanel.setLayout(new GridLayout(1, 3));
        df_dipCorrNoise = new DecimalField(0., 5, numberFormat);
        dipCorrNoisePanel.add(new JLabel("Dipole Corr.: "));
        dipCorrNoisePanel.add(df_dipCorrNoise);
        dipCorrNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(dipCorrNoisePanel);

        JPanel solNoisePanel = new JPanel();
        solNoisePanel.setLayout(new GridLayout(1, 3));
        df_solNoise = new DecimalField(0., 5, numberFormat);
        solNoisePanel.add(new JLabel("Solenoid: "));
        solNoisePanel.add(df_solNoise);
        solNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(solNoisePanel);

        JPanel rfAmpNoisePanel = new JPanel();
        rfAmpNoisePanel.setLayout(new GridLayout(1, 3));
        df_rfAmpNoise = new DecimalField(0., 5, numberFormat);
        rfAmpNoisePanel.add(new JLabel("RF amp: "));
        rfAmpNoisePanel.add(df_rfAmpNoise);
        rfAmpNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(rfAmpNoisePanel);

        JPanel rfPhaseNoisePanel = new JPanel();
        rfPhaseNoisePanel.setLayout(new GridLayout(1, 3));
        df_rfPhaseNoise = new DecimalField(0., 5, numberFormat);
        rfPhaseNoisePanel.add(new JLabel("RF phase: "));
        rfPhaseNoisePanel.add(df_rfPhaseNoise);
        rfPhaseNoisePanel.add(new JLabel(" %"));
        noiseLevelPanel.add(rfPhaseNoisePanel);

        JPanel bpmNoisePanel = new JPanel();
        bpmNoisePanel.setLayout(new GridLayout(1, 3));
        df_bpmNoise = new DecimalField(0., 5, numberFormat);
        bpmNoisePanel.add(new JLabel("BPM: "));
        bpmNoisePanel.add(df_bpmNoise);
        bpmNoisePanel.add(new JLabel(" mm"));
        noiseLevelPanel.add(bpmNoisePanel);

        // for static errors
        staticErrorPanel.setLayout(new GridLayout(8, 1));
        staticErrorPanel.add(new JLabel("Static error"));

        JPanel quadStatErrPanel = new JPanel();
        quadStatErrPanel.setLayout(new GridLayout(1, 2));
        df_quadStatErr = new DecimalField(0., 5, numberFormat);
        quadStatErrPanel.add(new JLabel("Quad: "));
        quadStatErrPanel.add(df_quadStatErr);
        quadStatErrPanel.add(new JLabel(" T/m"));
        staticErrorPanel.add(quadStatErrPanel);

        JPanel bendStatErrPanel = new JPanel();
        bendStatErrPanel.setLayout(new GridLayout(1, 2));
        df_bendStatErr = new DecimalField(0., 5, numberFormat);
        bendStatErrPanel.add(new JLabel("Bending Dipole: "));
        bendStatErrPanel.add(df_bendStatErr);
        bendStatErrPanel.add(new JLabel(" T"));
        staticErrorPanel.add(bendStatErrPanel);

        JPanel dipCorrStatErrPanel = new JPanel();
        dipCorrStatErrPanel.setLayout(new GridLayout(1, 2));
        df_dipCorrStatErr = new DecimalField(0., 5, numberFormat);
        dipCorrStatErrPanel.add(new JLabel("Dipole Corr.: "));
        dipCorrStatErrPanel.add(df_dipCorrStatErr);
        dipCorrStatErrPanel.add(new JLabel(" T"));
        staticErrorPanel.add(dipCorrStatErrPanel);

        JPanel solStatErrPanel = new JPanel();
        solStatErrPanel.setLayout(new GridLayout(1, 2));
        df_solStatErr = new DecimalField(0., 5, numberFormat);
        solStatErrPanel.add(new JLabel("Solenoid: "));
        solStatErrPanel.add(df_solStatErr);
        solStatErrPanel.add(new JLabel(" T"));
        staticErrorPanel.add(solStatErrPanel);

        JPanel rfAmpStatErrPanel = new JPanel();
        rfAmpStatErrPanel.setLayout(new GridLayout(1, 2));
        df_rfAmpStatErr = new DecimalField(0., 5, numberFormat);
        rfAmpStatErrPanel.add(new JLabel("RF amp: "));
        rfAmpStatErrPanel.add(df_rfAmpStatErr);
        rfAmpStatErrPanel.add(new JLabel(" kV"));
        staticErrorPanel.add(rfAmpStatErrPanel);

        JPanel rfPhaseStatErrPanel = new JPanel();
        rfPhaseStatErrPanel.setLayout(new GridLayout(1, 2));
        df_rfPhaseStatErr = new DecimalField(0., 5, numberFormat);
        rfPhaseStatErrPanel.add(new JLabel("RF phase: "));
        rfPhaseStatErrPanel.add(df_rfPhaseStatErr);
        rfPhaseStatErrPanel.add(new JLabel(" deg"));
        staticErrorPanel.add(rfPhaseStatErrPanel);

        JPanel bpmStatErrPanel = new JPanel();
        bpmStatErrPanel.setLayout(new GridLayout(1, 2));
        df_bpmStatErr = new DecimalField(0., 5, numberFormat);
        bpmStatErrPanel.add(new JLabel("BPM: "));
        bpmStatErrPanel.add(df_bpmStatErr);
        bpmStatErrPanel.add(new JLabel(" mm"));
        staticErrorPanel.add(bpmStatErrPanel);

        // for horizontal misalignments (static)
        staticHorMisalignPanel.setLayout(new GridLayout(8, 1));
        staticHorMisalignPanel.add(new JLabel("Horizontal misalignments"));

        JPanel quadStatHorMisalignPanel = new JPanel();
        quadStatHorMisalignPanel.setLayout(new GridLayout(1, 2));
        df_quadStatHorMisalign = new DecimalField(0., 5, numberFormat);
        quadStatHorMisalignPanel.add(new JLabel("Quad: "));
        quadStatHorMisalignPanel.add(df_quadStatHorMisalign);
        quadStatHorMisalignPanel.add(new JLabel(" mm"));
        staticHorMisalignPanel.add(quadStatHorMisalignPanel);

        JPanel bpmStatHorMisalignPanel = new JPanel();
        bpmStatHorMisalignPanel.setLayout(new GridLayout(1, 2));
        df_bpmStatHorMisalign = new DecimalField(0., 5, numberFormat);
        bpmStatHorMisalignPanel.add(new JLabel("bpm: "));
        bpmStatHorMisalignPanel.add(df_bpmStatHorMisalign);
        bpmStatHorMisalignPanel.add(new JLabel(" mm"));
        staticHorMisalignPanel.add(bpmStatHorMisalignPanel);

        // for vertical misalignments (static)
        staticVerMisalignPanel.setLayout(new GridLayout(8, 1));
        staticVerMisalignPanel.add(new JLabel("Vertical misalignments"));

        JPanel quadStatVerMisalignPanel = new JPanel();
        quadStatVerMisalignPanel.setLayout(new GridLayout(1, 2));
        df_quadStatVerMisalign = new DecimalField(0., 5, numberFormat);
        quadStatVerMisalignPanel.add(new JLabel("Quad: "));
        quadStatVerMisalignPanel.add(df_quadStatVerMisalign);
        quadStatVerMisalignPanel.add(new JLabel(" mm"));
        staticVerMisalignPanel.add(quadStatVerMisalignPanel);

        JPanel bpmStatVerMisalignPanel = new JPanel();
        bpmStatVerMisalignPanel.setLayout(new GridLayout(1, 2));
        df_bpmStatVerMisalign = new DecimalField(0., 5, numberFormat);
        bpmStatVerMisalignPanel.add(new JLabel("bpm: "));
        bpmStatVerMisalignPanel.add(df_bpmStatVerMisalign);
        bpmStatVerMisalignPanel.add(new JLabel(" mm"));
        staticVerMisalignPanel.add(bpmStatVerMisalignPanel);

        // put everything together
        setNoise.setBounds(300, 300, 900, 600);
        setNoise.setTitle("Define errors...");
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.X_AXIS));
        settingPanel.add(noiseLevelPanel);
        settingPanel.add(staticErrorPanel);
        settingPanel.add(staticHorMisalignPanel);
        settingPanel.add(staticVerMisalignPanel);
        setNoise.getContentPane().setLayout(new BorderLayout());
        setNoise.getContentPane().add(settingPanel, BorderLayout.CENTER);
        setNoise.getContentPane().add(done, BorderLayout.SOUTH);
        done.setActionCommand("noiseSet");
        done.addActionListener(this);
        setNoise.pack();
    }

    /**
     * Save the document to the specified URL.
     *
     * @param url The URL to which the document should be saved.
     */
    @Override
    public void saveDocumentAs(URL url) {

        XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor daLevel1 = xda.createChild("VA");
        //save accelerator file
        DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
        daXMLFile.setValue("xmlFile", this.getAcceleratorFilePath());

        // save selected sequences
        List<String> sequenceNames;
        if (getSelectedSequence() != null) {
            DataAdaptor daSeq = daLevel1.createChild("sequences");
            daSeq.setValue("name", getSelectedSequence().getId());
            if (getSelectedSequence() instanceof AcceleratorSeqCombo) {
                AcceleratorSeqCombo asc = (AcceleratorSeqCombo) getSelectedSequence();
                sequenceNames = asc.getConstituentNames();
            } else {
                sequenceNames = new ArrayList<String>();
                sequenceNames.add(getSelectedSequence().getId());
            }

            for (final String sequenceName : sequenceNames) {
                DataAdaptor daSeqComponents = daSeq.createChild("seq");
                daSeqComponents.setValue("name", sequenceName);
            }
            DataAdaptor daNoise = daLevel1.createChild("noiseLevels");
            daNoise.setValue("quad", quadNoise);
            daNoise.setValue("dipole", dipoleNoise);
            daNoise.setValue("corrector", correctorNoise);
            daNoise.setValue("bpm", bpmNoise);
            daNoise.setValue("sol", solNoise);
            daNoise.setValue("rfAmp", rfAmpNoise);
            daNoise.setValue("rfPhase", rfPhaseNoise);

            DataAdaptor daStaticError = daLevel1.createChild("staticErrors");
            daStaticError.setValue("quad", quadStaticError);
            daStaticError.setValue("dipole", dipoleStaticError);
            daStaticError.setValue("corrector", correctorStaticError);
            daStaticError.setValue("bpm", bpmStaticError);
            daStaticError.setValue("sol", solStaticError);
            daStaticError.setValue("rfAmp", rfAmpStaticError);
            daStaticError.setValue("rfPhase", rfPhaseStaticError);

            DataAdaptor daStaticHorMisalign = daLevel1.createChild("horizontalMisalignments");
            daStaticHorMisalign.setValue("quad", quadStatHorMisalign);
            daStaticHorMisalign.setValue("bpm", bpmStatHorMisalign);

            DataAdaptor daStaticVerMisalign = daLevel1.createChild("verticalMisalignments");
            daStaticVerMisalign.setValue("quad", quadStatVerMisalign);
            daStaticVerMisalign.setValue("bpm", bpmStatVerMisalign);
        }

        daLevel1.setValue("modelSyncPeriod", _modelSyncPeriod);

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
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                setHasChanges(true);
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
        });
    }

    /**
     * Create the default probe from the edit context.
     */
    private void createDefaultProbe() {
        if (selectedSequence != null) {
            try {
                baseProbe = (selectedSequence instanceof xal.smf.Ring) ? createRingProbe(selectedSequence) : createEnvelopeProbe(selectedSequence);
                currentProbe = baseProbe.copy();
                currentProbe.initialize();
                modelScenario.setProbe(currentProbe);
            } catch (Exception exception) {
                displayError("Error Creating Probe", "Probe Error", exception);
            }
        }
    }

    /**
     * create a new ring probe
     */
    static private TransferMapProbe createRingProbe(final AcceleratorSeq sequence) throws InstantiationException {
        final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker(sequence);
        return ProbeFactory.getTransferMapProbe(sequence, tracker);
    }

    /**
     * create a new envelope probe
     */
    static private EnvelopeProbe createEnvelopeProbe(final AcceleratorSeq sequence) throws InstantiationException {
        final IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
        return ProbeFactory.getEnvelopeProbe(sequence, tracker);
    }

    @Override
    public void customizeCommands(Commander commander) {
        // open probe editor
        // TODO: implement probe editor support
        this.commander = commander;
        Action probeEditorAction = new AbstractAction("probe-editor") {
            static final long serialVersionUID = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (baseProbe != null) {
                    stopServer();
                    final SimpleProbeEditor probeEditor = new SimpleProbeEditor(getMainWindow(), baseProbe);
                    baseProbe = probeEditor.getProbe();

                    currentProbe = baseProbe.copy();
                    currentProbe.initialize();
                    if (modelScenario != null) {
                        modelScenario.setProbe(currentProbe);
                    }
                } else {
                    //Sequence has not been selected
                    displayError("Probe Editor Error", "You must select a sequence before attempting to edit the probe.");
                }
            }
        };
        probeEditorAction.putValue(Action.NAME, "probe-editor");
        commander.registerAction(probeEditorAction);

        // action for using online model as engine
        olmModel.setSelected(true);
        olmModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                isForOLM = true;
                isFromPVLogger = false;
            }
        });
        commander.registerModel("olm", olmModel);

        // action for using PV logger snapshot through online model
        pvlogModel.setSelected(false);
        pvlogModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                isForOLM = true;
                isFromPVLogger = true;

                if (pvLogSelector == null) {
                    // for PV Logger snapshot chooser
                    plsc = new PVLogSnapshotChooser();
                    pvLogSelector = plsc.choosePVLogId();
                } else {
                    pvLogSelector.setVisible(true);
                }
            }
        });
        commander.registerModel("pvlogger", pvlogModel);

        // action for direct replaying of PVLogger logged data
        pvlogMovieModel.setSelected(false);
        pvlogMovieModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                isForOLM = false;
                isFromPVLogger = true;

                if (pvLogSelector == null) {
                    // for PV Logger snapshot chooser
                    plsc = new PVLogSnapshotChooser();
                    pvLogSelector = plsc.choosePVLogId();
                } else {
                    pvLogSelector.setVisible(true);
                }
            }
        });
        commander.registerModel("pvlogMovie", pvlogMovieModel);

        // action for running model and Diagnostics acquisition
        Action runAction = new AbstractAction() {
            static final long serialVersionUID = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (vaRunning) {
                    JOptionPane.showMessageDialog(getMainWindow(), "Virtual Accelerator has already started.", "Warning!", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                if (!Application.getApp().authorizeWithRBAC("Start")) {
                    JOptionPane.showMessageDialog(getMainWindow(), "You are unauthorized for this action.", "Warning!", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                if (getSelectedSequence() == null) {
                    JOptionPane.showMessageDialog(getMainWindow(), "You need to select sequence(s) first.", "Warning!", JOptionPane.PLAIN_MESSAGE);
                } else {
                    // use PV logger
                    if (isFromPVLogger) {
                        long pvLoggerId = plsc.getPVLogId();

                        runServer();

                        plds = new PVLoggerDataSource(pvLoggerId);

                        // use PVLogger to construct the model
                        if (isForOLM) {
                            // load the settings from the PV Logger
                            putSetPVsFromPVLogger();
                            // synchronize with the online model
                            MODEL_SYNC_TIMER.setEventHandler(getOnlineModelSynchronizer());
                        } else {      // directly use PVLogger data for replay
                            MODEL_SYNC_TIMER.setEventHandler(getPVLoggerSynchronizer());
                        }
                    } // use online model
                    else {
                        if (currentProbe == null) {
                            createDefaultProbe();
                            if (currentProbe == null) {
                                displayWarning("Warning!", "You need to select probe file first.");
                                return;
                            }
                            actionPerformed(event);
                        } else {
                            runServer();
                        }

                        // put the initial B_Book PVs to the server
                        configFieldBookPVs();

                        //put "set" PVs to the server
                        putSetPVs();

                        // continuously loop through the next 3 steps
                        System.out.println("Setup to synchronize the online model periodically...");
                        MODEL_SYNC_TIMER.setEventHandler(getOnlineModelSynchronizer());
                    }

                    MODEL_SYNC_TIMER.startNowWithInterval(_modelSyncPeriod, 0);
                    MODEL_SYNC_TIMER.resume();
                }
            }
        };
        runAction.putValue(Action.NAME, "run-va");
        commander.registerAction(runAction);

        // stop the channel access server
        Action stopAction = new AbstractAction() {
            static final long serialVersionUID = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                stopServer();
            }
        };

        stopAction.putValue(Action.NAME, "stop-va");
        commander.registerAction(stopAction);

        // set noise level
        Action setNoiseAction = new AbstractAction() {
            static final long serialVersionUID = 0;

            @Override
            public void actionPerformed(ActionEvent event) {

                df_quadNoise.setValue(quadNoise);
                df_bendNoise.setValue(dipoleNoise);
                df_dipCorrNoise.setValue(correctorNoise);
                df_bpmNoise.setValue(bpmNoise);
                df_solNoise.setValue(solNoise);
                df_rfAmpNoise.setValue(rfAmpNoise);
                df_rfPhaseNoise.setValue(rfPhaseNoise);

                df_quadStatErr.setValue(quadStaticError);
                df_bendStatErr.setValue(dipoleStaticError);
                df_dipCorrStatErr.setValue(correctorStaticError);
                df_bpmStatErr.setValue(bpmStaticError);
                df_solStatErr.setValue(solStaticError);
                df_rfAmpStatErr.setValue(rfAmpStaticError);
                df_rfPhaseStatErr.setValue(rfPhaseStaticError);

                df_quadStatHorMisalign.setValue(quadStatHorMisalign);
                df_quadStatVerMisalign.setValue(quadStatVerMisalign);

                df_bpmStatHorMisalign.setValue(bpmStatHorMisalign);
                df_bpmStatVerMisalign.setValue(bpmStatVerMisalign);
                setNoise.setVisible(true);
            }
        };
        setNoiseAction.putValue(Action.NAME, "set-noise");
        commander.registerAction(setNoiseAction);

        // configure synchronization
        final Action synchConfigAction = new AbstractAction() {
            static final long serialVersionUID = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                final String result = JOptionPane.showInputDialog(getMainWindow(), "Set the Model Synchronization Period (milliseconds): ", _modelSyncPeriod);
                if (result != null) {
                    try {
                        final long modelSyncPeriod = Long.parseLong(result);
                        setModelSyncPeriod(modelSyncPeriod);
                    } catch (Exception exception) {
                        displayError("Error setting Model Sync Period!", exception.getMessage());
                    }
                }
            }
        };
        synchConfigAction.putValue(Action.NAME, "sync-config");
        commander.registerAction(synchConfigAction);
    }

    /**
     * handle this document being closed
     */
    @Override
    public void willClose() {
        System.out.println("Document will be closed");
        destroyServer();
    }

    public void update(final DataAdaptor adaptor) {
        if (getSource() != null) {
            XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(), false);
            DataAdaptor da1 = xda.childAdaptor("VA");

            //restore accelerator file
            applySelectedAcceleratorWithDefaultPath(da1.childAdaptor("accelerator").stringValue("xmlFile"));

            // set up the right sequence combo from selected primaries:
            List<DataAdaptor> temp = da1.childAdaptors("sequences");
            if (temp.isEmpty()) {
                return; // bail out, nothing left to do
            }
            ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
            DataAdaptor da2a = da1.childAdaptor("sequences");
            String seqName = da2a.stringValue("name");

            DataAdaptor daNoise = da1.childAdaptor("noiseLevels");
            if (daNoise != null) {
                quadNoise = daNoise.doubleValue("quad");
                dipoleNoise = daNoise.doubleValue("dipole");
                correctorNoise = daNoise.doubleValue("corrector");
                bpmNoise = daNoise.doubleValue("bpm");
                solNoise = daNoise.doubleValue("sol");
                rfAmpNoise = daNoise.doubleValue("rfAmp");
                rfPhaseNoise = daNoise.doubleValue("rfPhase");
            }

            DataAdaptor daStaticError = da1.childAdaptor("staticErrors");
            if (daStaticError != null) {
                quadStaticError = daStaticError.doubleValue("quad");
                dipoleStaticError = daStaticError.doubleValue("dipole");
                correctorStaticError = daStaticError.doubleValue("corrector");
                bpmStaticError = daStaticError.doubleValue("bpm");
                solStaticError = daStaticError.doubleValue("sol");
                rfAmpStaticError = daStaticError.doubleValue("rfAmp");
                rfPhaseStaticError = daStaticError.doubleValue("rfPhase");
            }

            DataAdaptor daStaticHorMisalign = da1.childAdaptor("horizontalMisalignments");
            if (daStaticHorMisalign != null) {
                quadStatHorMisalign = daStaticHorMisalign.doubleValue("quad");
                bpmStatHorMisalign = daStaticHorMisalign.doubleValue("bpm");
            }

            DataAdaptor daStaticVerMisalign = da1.childAdaptor("verticalMisalignments");
            if (daStaticVerMisalign != null) {
                quadStatVerMisalign = daStaticVerMisalign.doubleValue("quad");
                bpmStatVerMisalign = daStaticVerMisalign.doubleValue("bpm");
            }

            temp = da2a.childAdaptors("seq");
            for (final DataAdaptor da : temp) {
                seqs.add(getAccelerator().getSequence(da.stringValue("name")));
            }
            if (seqName.equals("Ring")) {
                setSelectedSequence(new Ring(seqName, seqs));
            } else {
                setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
            }

            setSelectedSequenceList(seqs.subList(0, seqs.size()));

            createDefaultProbe();

            modelScenario.setProbe(currentProbe);

            if (da1.hasAttribute("modelSyncPeriod")) {
                _modelSyncPeriod = da1.longValue("modelSyncPeriod");
            }
        }

    }

    protected Scenario getScenario() {
        return modelScenario;
    }

    protected boolean isVARunning() {
        return vaRunning;
    }

    /**
     * update the limit channels based on changes to the Field Book channels
     */
    private void updateLimitChannels() {
        for (final Electromagnet magnet : mags) {
            try {
                final Channel bookChannel = magnet.getMainSupply().findChannel(MagnetMainSupply.FIELD_BOOK_HANDLE);
                final Channel fieldChannel = magnet.getMainSupply().findChannel(MagnetMainSupply.FIELD_SET_HANDLE);
                if (bookChannel != null) {
                    if (bookChannel.isConnected()) {
                        final double bookField = bookChannel.getValDbl();
                        final double warningStaticError = 0.05 * Math.abs(bookField);
                        final double alarmStaticError = 0.1 * Math.abs(bookField);

                        final String[] warningPVs = fieldChannel.getWarningLimitPVs();

                        final Channel lowerWarningChannel = CHANNEL_SERVER_FACTORY.getChannel(warningPVs[0], fieldChannel.getValueTransform());
                        //                        System.out.println( "Lower Limit PV: " + lowerWarningChannel.channelName() );
                        if (lowerWarningChannel.connectAndWait()) {
                            lowerWarningChannel.putValCallback(bookField - warningStaticError, this);
                        }

                        final Channel upperWarningChannel = CHANNEL_SERVER_FACTORY.getChannel(warningPVs[1], fieldChannel.getValueTransform());
                        if (upperWarningChannel.connectAndWait()) {
                            upperWarningChannel.putValCallback(bookField + warningStaticError, this);
                        }

                        final String[] alarmPVs = fieldChannel.getAlarmLimitPVs();

                        final Channel lowerAlarmChannel = CHANNEL_SERVER_FACTORY.getChannel(alarmPVs[0], fieldChannel.getValueTransform());
                        if (lowerAlarmChannel.connectAndWait()) {
                            lowerAlarmChannel.putValCallback(bookField - alarmStaticError, this);
                        }

                        final Channel upperAlarmChannel = CHANNEL_SERVER_FACTORY.getChannel(alarmPVs[1], fieldChannel.getValueTransform());
                        if (upperAlarmChannel.connectAndWait()) {
                            upperAlarmChannel.putValCallback(bookField + alarmStaticError, this);
                        }
                    }
                }
            } catch (NoSuchChannelException exception) {
                System.err.println(exception.getMessage());
            } catch (ConnectionException exception) {
                System.err.println(exception.getMessage());
            } catch (GetException exception) {
                System.err.println(exception.getMessage());
            } catch (PutException exception) {
                System.err.println(exception.getMessage());
            }
        }

        Channel.flushIO();
    }

    /**
     * This method is for populating the readback PVs
     */
    private void putReadbackPVs() {
        // set beam trigger PV to "on"
        try {
            final Date now = new Date();
            if (_repRateChannel != null) {
                final double updatePeriod = 0.001 * (now.getTime() - _lastUpdate.getTime());  // period of update in seconds
                _repRateChannel.putValCallback(1.0 / updatePeriod, this);
            }
            _lastUpdate = now;
            if (beamOnEvent != null) {
                beamOnEvent.putValCallback(0, this);
            }
            beamOnEventCounter++;
            if (beamOnEventCount != null) {
                beamOnEventCount.putValCallback(beamOnEventCounter, this);
            }
            if (slowDiagEvent != null) {
                slowDiagEvent.putValCallback(0, this);
            }
        } catch (ConnectionException e) {
            System.err.println(e);
        } catch (PutException e) {
            System.err.println(e);
        }

        // get the "set" PV value, add noise, and then put to the corresponding readback PV.
        for (final ReadbackSetRecord record : READBACK_SET_RECORDS) {
            try {
                record.updateReadback(ch_noiseMap, ch_staticErrorMap, this);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        Channel.flushIO();
        final int rowCount = READBACK_SET_TABLE_MODEL.getRowCount();
        if (rowCount > 0) {
            READBACK_SET_TABLE_MODEL.fireTableRowsUpdated(0, rowCount - 1);
        }

        updateLimitChannels();
    }

    /**
     * populate the readback PVs from the PV Logger
     */
    private void putReadbackPVsFromPVLogger() {
        final Map<String, Double> qPVMap = plds.getMagnetMap();

        // set beam trigger PV to "on"
        try {
            if (beamOnEvent != null) {
                beamOnEvent.putVal(0);
            }
            beamOnEventCounter++;
            if (beamOnEventCount != null) {
                beamOnEventCount.putVal(beamOnEventCounter);
            }
            if (slowDiagEvent != null) {
                slowDiagEvent.putVal(0);
            }
        } catch (ConnectionException e) {
            System.err.println(e);
        } catch (PutException e) {
            System.err.println(e);
        }

        // get the "set" PV value, add noise, and then put to the corresponding readback PV.
        for (final ReadbackSetRecord record : READBACK_SET_RECORDS) {
            try {
                final String readbackPV = record.getReadbackChannel().channelName();

                if (qPVMap.containsKey(readbackPV)) {
                    final double basisValue = qPVMap.get(readbackPV).doubleValue();
                    record.updateReadback(basisValue, ch_noiseMap, ch_staticErrorMap, this);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        READBACK_SET_TABLE_MODEL.fireTableDataChanged();
    }

    /**
     * initialize the field book PVs from the default values
     */
    private void configFieldBookPVs() {
        for (final Electromagnet magnet : mags) {
            try {
                final Channel bookChannel = magnet.getMainSupply().findChannel(MagnetMainSupply.FIELD_BOOK_HANDLE);
                if (bookChannel != null) {
                    if (bookChannel.connectAndWait()) {
                        final double bookField = magnet.toCAFromField(magnet.getDfltField());
                        bookChannel.putValCallback(bookField, this);
                    }
                }
            } catch (NoSuchChannelException exception) {
                System.err.println(exception.getMessage());
            } catch (ConnectionException exception) {
                System.err.println(exception.getMessage());
            } catch (PutException exception) {
                System.err.println(exception.getMessage());
            }
        }
    }

    /**
     * populate all the "set" PV values from design values
     */
    private void putSetPVs() {
        // for all magnets
        for (final Electromagnet em : mags) {
            try {
                Channel ch = em.getMainSupply().getAndConnectChannel(MagnetMainSupply.FIELD_SET_HANDLE);
                final double setting = em.toCAFromField(em.getDfltField());
                //System.out.println("Ready to put " + setting + " to " + ch.getId());
                ch.putValCallback(setting, this);

                if (em instanceof TrimmedQuadrupole) {
                    Channel trimChannel = ((TrimmedQuadrupole) em).getTrimSupply().getAndConnectChannel(MagnetTrimSupply.FIELD_SET_HANDLE);
                    //System.out.println("Ready to put " + 0.0 + " to " + trimChannel.getId());
                    trimChannel.putValCallback(0.0, this);
                }
            } catch (NoSuchChannelException e) {
                System.err.println(e.getMessage());
            } catch (ConnectionException e) {
                System.err.println(e.getMessage());
            } catch (PutException e) {
                System.err.println(e.getMessage());
            }
        }

        // for all rf cavities
        for (final RfCavity rfCavity : rfCavities) {
            try {
                final Channel ampSetCh = rfCavity.findChannel(RfCavity.CAV_AMP_SET_HANDLE);
                if (ampSetCh.isValid()) {
                    ampSetCh.connectAndWait();
                    //System.out.println("Ready to put " + rfCavity.getDfltCavAmp() + " to " + ampSetCh.getId());
                    if (rfCavity instanceof xal.smf.impl.SCLCavity) {
                        ampSetCh.putValCallback(rfCavity.getDfltCavAmp() * ((SCLCavity) rfCavity).getStructureTTF(), this);
                    } else {
                        ampSetCh.putValCallback(rfCavity.getDfltCavAmp(), this);
                    }
                }

                final Channel phaseSetCh = rfCavity.findChannel(RfCavity.CAV_PHASE_SET_HANDLE);
                if (phaseSetCh.isValid()) {
                    phaseSetCh.connectAndWait();
                    //System.out.println("Ready to put " + rfCavity.getDfltCavPhase() + " to " + phaseSetCh.getId());
                    phaseSetCh.putValCallback(rfCavity.getDfltCavPhase(), this);
                }
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
        final Map<String, Double> qPSPVMap = plds.getMagnetPSMap();

        for (final Electromagnet em : mags) {
            try {
                Channel ch = em.getMainSupply().getAndConnectChannel(MagnetMainSupply.FIELD_SET_HANDLE);
                //System.out.println("Ready to put " + Math.abs(em.getDfltField()) + " to " + ch.getId());

                final String channelID = ch.getId();
                if (qPSPVMap.containsKey(channelID)) {
                    ch.putValCallback(qPSPVMap.get(channelID).doubleValue(), this);
                }
            } catch (NoSuchChannelException e) {
                System.err.println(e.getMessage());
            } catch (ConnectionException e) {
                System.err.println(e.getMessage());
            } catch (PutException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * This method is for populating the diagnostic PVs (only BPMs + WSs for
     * now)
     */
    protected void putDiagPVs() {
        // CKA Nov 25, 2013
        SimpleSimResultsAdaptor cmpCalcEngine = new SimpleSimResultsAdaptor(modelScenario.getTrajectory());

        /**
         * temporary list data for getting the array bpm and ws datas
         */
        int i = 0;
        List<Double> tempBPMx = new ArrayList<Double>();
        List<Double> tempBPMy = new ArrayList<Double>();
        List<Double> tempBPMp = new ArrayList<Double>();

        List<Double> tempWSx = new ArrayList<Double>();
        List<Double> tempWSy = new ArrayList<Double>();
        List<Double> tempWSp = new ArrayList<Double>();

        List<Double> tempbeampos = new ArrayList<Double>();
        List<Double> tempbeamx = new ArrayList<Double>();
        List<Double> tempbeamy = new ArrayList<Double>();
        List<Double> tempsigmaz = new ArrayList<Double>();

        final Iterator<? extends ProbeState<?>> stateIter = modelScenario.getTrajectory().stateIterator();
        while (stateIter.hasNext()) {
            final ProbeState<?> state = stateIter.next();
//          EnvelopeProbeState state = (EnvelopeProbeState) stateIter.next();
            double position = state.getPosition();
            final PhaseVector coordinateVector = cmpCalcEngine.computeFixedOrbit(state);
            double x = coordinateVector.getx() * 1000;
            double y = coordinateVector.gety() * 1000;

            final Twiss[] twiss = cmpCalcEngine.computeTwissParameters(state);
            double sigmaz = twiss[2].getEnvelopeRadius() * 1000;

            tempbeampos.add(position);
            tempbeamx.add(x);
            tempbeamy.add(y);
            tempsigmaz.add(sigmaz);
        }

        double beamp[] = new double[tempbeampos.size()];
        double beamx[] = new double[tempbeampos.size()];
        double beamy[] = new double[tempbeampos.size()];
        double beamsigmaz[] = new double[tempbeampos.size()];

        for (i = 0; i < tempbeampos.size(); i++) {
            beamp[i] = tempbeampos.get(i);
            beamx[i] = tempbeamx.get(i);
            beamy[i] = tempbeamy.get(i);
            beamsigmaz[i] = tempsigmaz.get(i);
        }
        try {
            _diagplot.showbeampositionplot(beamp, beamx, beamy);
            _diagplot.showsigmazplot(beamp, beamsigmaz);
        } catch (ConnectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (GetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // for BPMs
        for (final BPM bpm : bpms) {
            final Channel bpmXAvgChannel = bpm.getChannel(BPM.X_AVG_HANDLE);
            final Channel bpmXTBTChannel = bpm.getChannel(BPM.X_TBT_HANDLE);   // TODO: CKA - NEVER USED
            final Channel bpmYAvgChannel = bpm.getChannel(BPM.Y_AVG_HANDLE);
            final Channel bpmYTBTChannel = bpm.getChannel(BPM.Y_TBT_HANDLE);  // TODO: CKA - NEVER USED
            final Channel bpmAmpAvgChannel = bpm.getChannel(BPM.AMP_AVG_HANDLE);

            try {
                ProbeState<?> probeState = modelScenario.getTrajectory().stateForElement(bpm.getId());

                // CKA - Transfer map probes and Envelope probes both exposed ICoordinateState
                //       so we should be able to compute a "fixed orbit" in any context
                final PhaseVector coordinates = cmpCalcEngine.computeFixedOrbit(probeState);
                // For SNS Ring BPM system, we only measure the signal with respect to the center of the beam pipe.

                final double xAvg;
                final double yAvg;

                if (isSelectedSequenceRing()) {
                    // TO-DO: the turn by turn arrays should really be generated from betatron motion rather than random data about the nominal
                    final double[] xTBT = NoiseGenerator.noisyArrayForNominal(coordinates.getx() * 1000.0 - bpm.getXOffset(), DEFAULT_BPM_WAVEFORM_SIZE, DEFAULT_BPM_WAVEFORM_DATA_SIZE, bpmNoise, bpm_staticErrorMapX.get(bpm));
                    xAvg = NoiseGenerator.getAverage(xTBT, DEFAULT_BPM_WAVEFORM_DATA_SIZE);

                    final double[] yTBT = NoiseGenerator.noisyArrayForNominal(coordinates.gety() * 1000.0 - bpm.getYOffset(), DEFAULT_BPM_WAVEFORM_SIZE, DEFAULT_BPM_WAVEFORM_DATA_SIZE, bpmNoise, bpm_staticErrorMapY.get(bpm));
                    yAvg = NoiseGenerator.getAverage(yTBT, DEFAULT_BPM_WAVEFORM_DATA_SIZE);

                } else {
                    xAvg = NoiseGenerator.setValForPV(coordinates.getx() * 1000.0 - bpm.getXOffset(), bpmNoise, bpm_staticErrorMapX.get(bpm), false);
                    yAvg = NoiseGenerator.setValForPV(coordinates.gety() * 1000.0 - bpm.getYOffset(), bpmNoise, bpm_staticErrorMapY.get(bpm), false);
                }
                bpmXAvgChannel.putValCallback(xAvg, this);
                //                    bpmXTBTChannel.putValCallback( xTBT, this );  // don't post to channel access until the turn by turn data is generated correctly
                bpmYAvgChannel.putValCallback(yAvg, this);
                //                    bpmYTBTChannel.putValCallback( yTBT, this );  // don't post to channel access until the turn by turn data is generated correctly

                tempBPMp.add(getSelectedSequence().getPosition(bpm));
                tempBPMx.add(xAvg);
                tempBPMy.add(yAvg);

                // hardwired BPM amplitude noise and static error to 5% and 0.1mm (randomly) respectively
                bpmAmpAvgChannel.putVal(NoiseGenerator.setValForPV(20., 5., 0.1, true));
                if (!isSelectedSequenceRing()) {
                    // calculate the BPM phase (for linac only)
                    final Channel bpmPhaseAvgChannel = bpm.getChannel(BPM.PHASE_AVG_HANDLE);
                    bpmPhaseAvgChannel.putValCallback(probeState.getTime() * 360. * (((BPMBucket) bpm.getBucket("bpm")).getFrequency() * 1.e6) % 360.0, this);
                }
            } catch (ConnectionException e) {
                System.err.println(e.getMessage());
            } catch (PutException e) {
                System.err.println(e.getMessage());
            }
        }

        /**
         * the array of bpm data
         */
        double bpmp[] = new double[tempBPMp.size()];
        double bpmx[] = new double[tempBPMp.size()];
        double bpmy[] = new double[tempBPMp.size()];
        /**
         * get the bpmdata[] from the list
         */
        for (i = 0; i < tempBPMp.size(); i++) {
            bpmp[i] = tempBPMp.get(i);
            bpmx[i] = tempBPMx.get(i);
            bpmy[i] = tempBPMy.get(i);
        }
        /**
         * showBPMplot
         */
        try {
            _diagplot.showbpmplot(bpmp, bpmx, bpmy);
        } catch (ConnectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (GetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // for WSs
        for (final ProfileMonitor ws : wss) {
            Channel wsX = ws.getChannel(ProfileMonitor.H_SIGMA_M_HANDLE);
            Channel wsY = ws.getChannel(ProfileMonitor.V_SIGMA_M_HANDLE);

            try {
                ProbeState<?> probeState = modelScenario.getTrajectory().stateForElement(ws.getId());
                if (modelScenario.getProbe() instanceof EnvelopeProbe) {
                    final Twiss[] twiss = ((EnvelopeProbeState) probeState).getCovarianceMatrix().computeTwiss();
                    wsX.putValCallback(twiss[0].getEnvelopeRadius() * 1000., this);
                    wsY.putValCallback(twiss[1].getEnvelopeRadius() * 1000., this);

                    tempWSp.add(ws.getPosition());
                    tempWSx.add(twiss[0].getEnvelopeRadius() * 1000);
                    tempWSy.add(twiss[1].getEnvelopeRadius() * 1000);
                }
            } catch (ConnectionException e) {
                System.err.println(e.getMessage());
            } catch (PutException e) {
                System.err.println(e.getMessage());
            }
        }

        /**
         * the array of ws data
         */
        double wsp[] = new double[tempWSp.size()];
        double wsx[] = new double[tempWSp.size()];
        double wsy[] = new double[tempWSp.size()];
        /**
         * get the wsdata[] from the list
         */
        for (i = 0; i < tempWSp.size(); i++) {
            wsp[i] = tempWSp.get(i);
            wsx[i] = tempWSx.get(i);
            wsy[i] = tempWSy.get(i);
        }
        /**
         * showWSplot
         */
        try {
            _diagplot.showsigmaplot(wsp, wsx, wsy);
        } catch (ConnectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (GetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Channel.flushIO();
    }

    private void putDiagPVsFromPVLogger() {
        // for BPMs
        final Map<String, Double> bpmXMap = plds.getBPMXMap();
        final Map<String, Double> bpmYMap = plds.getBPMYMap();
        final Map<String, Double> bpmAmpMap = plds.getBPMAmpMap();
        final Map<String, Double> bpmPhaseMap = plds.getBPMPhaseMap();

        for (final BPM bpm : bpms) {
            Channel bpmX = bpm.getChannel(BPM.X_AVG_HANDLE);
            Channel bpmY = bpm.getChannel(BPM.Y_AVG_HANDLE);
            Channel bpmAmp = bpm.getChannel(BPM.AMP_AVG_HANDLE);

            try {
                System.err.println("Now updating " + bpm.getId());

                if (bpmXMap.containsKey(bpmX.getId())) {
                    bpmX.putVal(NoiseGenerator.setValForPV(bpmXMap.get(bpmX.getId()).doubleValue(), bpmNoise, bpm_staticErrorMapX.get(bpm), false));
                }

                if (bpmYMap.containsKey(bpmY.getId())) {
                    bpmY.putVal(NoiseGenerator.setValForPV(bpmYMap.get(bpmY.getId()).doubleValue(), bpmNoise, bpm_staticErrorMapY.get(bpm), false));
                }

                // BPM amplitude
                if (bpmAmpMap.containsKey(bpmAmp.getId())) {
                    bpmAmp.putVal(NoiseGenerator.setValForPV(bpmAmpMap.get(bpmAmp.getId()).doubleValue(), 5., 0.1, false));
                }
                // BPM phase (for linac only)
                if (!(currentProbe instanceof TransferMapProbe)) {
                    Channel bpmPhase = bpm.getChannel(BPM.PHASE_AVG_HANDLE);
                    if (bpmPhaseMap.containsKey(bpmPhase.getId())) {
                        bpmPhase.putVal(bpmPhaseMap.get(bpmPhase.getId()).doubleValue());
                    }
                }

            } catch (ConnectionException e) {
                System.err.println(e.getMessage());
            } catch (PutException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    /**
     * handle the CA put callback
     */
    @Override
    public void putCompleted(final Channel chan) {
    }

    /**
     * Returns a distributed static error, uniformly distributed within +/-
     * value given
     */
    private double getStaticError(double staticErrorSigma) {
        return staticErrorSigma * (Math.random() - 0.5) * 2;
    }

    private double getNoiseForElement(AcceleratorNode element) {
        if (element.isKindOf(Quadrupole.s_strType)) {
            return quadNoise;
        }
        if (element.isKindOf(Bend.s_strType)) {
            return dipoleNoise;
        }
        if (element.isKindOf(HDipoleCorr.s_strType)) {
            return correctorNoise;
        }
        if (element.isKindOf(VDipoleCorr.s_strType)) {
            return correctorNoise;
        }
        if (element.isKindOf(Solenoid.s_strType)) {
            return solNoise;
        }
        return 0.0;
    }

    private double getStaticErrorForElement(AcceleratorNode element) {
        double staticError = 0.0;
        if (element.isKindOf(Quadrupole.s_strType)) {
            staticError = quadStaticError;
        }
        if (element.isKindOf(Bend.s_strType)) {
            staticError = dipoleStaticError;
        }
        if (element.isKindOf(HDipoleCorr.s_strType)) {
            staticError = correctorStaticError;
        }
        if (element.isKindOf(VDipoleCorr.s_strType)) {
            staticError = correctorStaticError;
        }
        if (element.isKindOf(Solenoid.s_strType)) {
            staticError = solStaticError;
        }
        return getStaticError(staticError);
    }

    // Used to set horizontal misalignments in misalignElements()
    private double getStaticHorizontalMisalignmentForElement(AcceleratorNode element) {
        double horizontalMisalignment = 0.0;
        if (element.isKindOf(Quadrupole.s_strType)) {
            horizontalMisalignment = quadStatHorMisalign;
        }
        if (element.isKindOf(BPM.s_strType)) {
            horizontalMisalignment = bpmStatHorMisalign;
        }
        return getStaticError(horizontalMisalignment);
    }

    // Used to set vertical misalignments in misalignElements()
    private double getStaticVerticalMisalignmentForElement(AcceleratorNode element) {
        double verticalMisalignment = 0.0;
        if (element.isKindOf(Quadrupole.s_strType)) {
            verticalMisalignment = quadStatVerMisalign;
        }
        if (element.isKindOf(BPM.s_strType)) {
            verticalMisalignment = bpmStatVerMisalign;
        }
        return getStaticError(verticalMisalignment);
    }

    /**
     * create the map between the "readback" and "set" PVs
     */
    private void configureReadbacks() {
        READBACK_SET_RECORDS.clear();

        ch_noiseMap = new LinkedHashMap<Channel, Double>();
        ch_staticErrorMap = new LinkedHashMap<Channel, Double>();
        bpm_staticErrorMapX = new LinkedHashMap<BPM, Double>();
        bpm_staticErrorMapY = new LinkedHashMap<BPM, Double>();

        if (selectedSequence != null) {
            // for magnet PVs
            for (final Electromagnet em : mags) {
                READBACK_SET_RECORDS.add(new ReadbackSetRecord(em, em.getChannel(Electromagnet.FIELD_RB_HANDLE), em.getChannel(MagnetMainSupply.FIELD_SET_HANDLE)));
                // handle the trimmed magnets
                if (em.isKindOf(TrimmedQuadrupole.s_strType)) {
                    READBACK_SET_RECORDS.add(new ReadbackSetRecord(em, em.getChannel(MagnetTrimSupply.FIELD_RB_HANDLE), em.getChannel(MagnetTrimSupply.FIELD_SET_HANDLE)));
                    ch_noiseMap.put(em.getChannel(MagnetTrimSupply.FIELD_RB_HANDLE), 0.0);
                    ch_staticErrorMap.put(em.getChannel(MagnetTrimSupply.FIELD_RB_HANDLE), 0.0);
                }
                // set up the map between the magnet readback PV and its noise level
                ch_noiseMap.put(em.getChannel(Electromagnet.FIELD_RB_HANDLE), getNoiseForElement(em));
                ch_staticErrorMap.put(em.getChannel(Electromagnet.FIELD_RB_HANDLE), getStaticErrorForElement(em));
            }

            // for RF PVs
            for (final RfCavity rfCav : rfCavities) {
                final Channel ampSetChannel = rfCav.findChannel(RfCavity.CAV_AMP_SET_HANDLE);
                final Channel ampReadChannel = rfCav.findChannel(RfCavity.CAV_AMP_AVG_HANDLE);
                if (ampReadChannel != null && ampReadChannel.isValid()) {
                    if (ampSetChannel != null && ampSetChannel.isValid()) {
                        READBACK_SET_RECORDS.add(new ReadbackSetRecord(rfCav, ampReadChannel, ampSetChannel));
                    }
                    ch_noiseMap.put(ampReadChannel, rfAmpNoise);
                    ch_staticErrorMap.put(ampReadChannel, getStaticError(rfAmpStaticError));
                }

                final Channel phaseSetChannel = rfCav.findChannel(RfCavity.CAV_PHASE_SET_HANDLE);
                final Channel phaseReadChannel = rfCav.findChannel(RfCavity.CAV_PHASE_AVG_HANDLE);
                if (phaseReadChannel != null && phaseReadChannel.isValid()) {
                    if (phaseSetChannel != null && phaseSetChannel.isValid()) {
                        READBACK_SET_RECORDS.add(new ReadbackSetRecord(rfCav, phaseReadChannel, phaseSetChannel));
                    }
                    ch_noiseMap.put(phaseReadChannel, rfPhaseNoise);
                    ch_staticErrorMap.put(phaseReadChannel, getStaticError(rfPhaseStaticError));
                }
            }

            TypeQualifier typeQualifier = QualifierFactory.qualifierWithStatusAndTypes(true, ESSIonSourceMagnetron.s_strType);
            List<ESSIonSourceMagnetron> magnetrons = getSelectedSequence().getAllInclusiveNodesWithQualifier(typeQualifier);
            for (final ESSIonSourceMagnetron magnetron : magnetrons) {
                READBACK_SET_RECORDS.add(new ReadbackSetRecord(magnetron,
                        magnetron.findChannel(ESSIonSourceMagnetron.FORWD_PRW_RB_HANDLE),
                        magnetron.findChannel(ESSIonSourceMagnetron.FORWD_PRW_S_HANDLE)));
            }

            typeQualifier = QualifierFactory.qualifierWithStatusAndTypes(true, ESSIonSourceMFC.s_strType);
            List<ESSIonSourceMFC> mfcs = getSelectedSequence().getAllInclusiveNodesWithQualifier(typeQualifier);
            for (final ESSIonSourceMFC mfc : mfcs) {
                READBACK_SET_RECORDS.add(new ReadbackSetRecord(mfc,
                        mfc.findChannel(ESSIonSourceMFC.VOLTAGE_RB_HANDLE),
                        mfc.findChannel(ESSIonSourceMFC.VOLTAGE_SET_HANDLE)));
            }

            typeQualifier = QualifierFactory.qualifierWithStatusAndTypes(true, ESSIonSourceCoil.s_strType);
            List<ESSIonSourceCoil> coils = getSelectedSequence().getAllInclusiveNodesWithQualifier(typeQualifier);
            for (final ESSIonSourceCoil coil : coils) {
                READBACK_SET_RECORDS.add(new ReadbackSetRecord(coil,
                        coil.findChannel(ESSIonSourceCoil.I_HANDLE),
                        coil.findChannel(ESSIonSourceCoil.I_SET_HANDLE)));
            }

            // for BPMs
            for (final BPM bpm : bpms) {
                bpm_staticErrorMapX.put(bpm, getStaticError(bpmStaticError));
                bpm_staticErrorMapY.put(bpm, getStaticError(bpmStaticError));
            }

            Collections.sort(READBACK_SET_RECORDS, new ReadbackSetRecordPositionComparator(selectedSequence));
            READBACK_SET_TABLE_MODEL.setRecords(new ArrayList<ReadbackSetRecord>(READBACK_SET_RECORDS));
        }
    }

    /**
     * run the VA server
     */
    private void runServer() {
        vaRunning = true;
    }

    /**
     * stop the VA Server
     */
    private void stopServer() {
        MODEL_SYNC_TIMER.suspend();
        vaRunning = false;
    }

    /**
     * destroy the VA Server
     */
    void destroyServer() {
        try {
            stopServer();
            if (_vaServer != null) {
                _vaServer.destroy();
                _vaServer = null;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void acceleratorChanged() {
        if (accelerator != null) {
            stopServer();

            baseProbe = null;
            currentProbe = null;

            _repRateChannel = accelerator.getTimingCenter().findChannel(TimingCenter.REP_RATE_HANDLE);
            beamOnEvent = accelerator.getTimingCenter().findChannel(TimingCenter.BEAM_ON_EVENT_HANDLE);
            beamOnEventCount = accelerator.getTimingCenter().findChannel(TimingCenter.BEAM_ON_EVENT_COUNT_HANDLE);
            slowDiagEvent = accelerator.getTimingCenter().findChannel(TimingCenter.SLOW_DIAGNOSTIC_EVENT_HANDLE);

            setHasChanges(true);
        }
    }

    @Override
    public void selectedSequenceChanged() {
        destroyServer();

        if (selectedSequence != null) {
            try {
                _vaServer = new VAServer(selectedSequence);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            /**
             * get all nodes(add by liyong)
             */
            nodes = getSelectedSequence().getAllNodes();

            // get electro magnets
            TypeQualifier typeQualifier = QualifierFactory.qualifierWithStatusAndTypes(true, Electromagnet.s_strType);
            mags = getSelectedSequence().<Electromagnet>getAllNodesWithQualifier(typeQualifier);

            // get all the rf cavities
            typeQualifier = QualifierFactory.qualifierWithStatusAndTypes(true, RfCavity.s_strType);  // TODO: CKA - No Effect
            rfCavities = getSelectedSequence().getAllInclusiveNodesWithQualifier(typeQualifier);

            // get all the BPMs
            bpms = getSelectedSequence().<BPM>getAllNodesWithQualifier(QualifierFactory.qualifierWithStatusAndType(true, "BPM"));

            // get all the wire scanners
            wss = getSelectedSequence().getAllNodesWithQualifier(QualifierFactory.qualifierWithStatusAndType(true, ProfileMonitor.PROFILE_MONITOR_TYPE));
            System.out.println(wss);

            // should create a new map for "set" <-> "readback" PV mapping
            configureReadbacks();

            // misalign the selected sequence (also sets new modelScenario and probe)
            misalignElements();

            setHasChanges(true);
        } else {
            modelScenario = null;
            baseProbe = null;
            currentProbe = null;
        }
    }

    public void buildOnlineModel() {
        try {
            //   model.resetProbe();
            modelScenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
            modelScenario.resync();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("noiseSet")) {
            quadNoise = df_quadNoise.getDoubleValue();
            dipoleNoise = df_bendNoise.getDoubleValue();
            correctorNoise = df_dipCorrNoise.getDoubleValue();
            bpmNoise = df_bpmNoise.getDoubleValue();
            solNoise = df_solNoise.getDoubleValue();
            rfAmpNoise = df_rfAmpNoise.getDoubleValue();
            rfPhaseNoise = df_rfPhaseNoise.getDoubleValue();

            quadStaticError = df_quadStatErr.getDoubleValue();
            dipoleStaticError = df_bendStatErr.getDoubleValue();
            correctorStaticError = df_dipCorrStatErr.getDoubleValue();
            bpmStaticError = df_bpmStatErr.getDoubleValue();
            solStaticError = df_solStatErr.getDoubleValue();
            rfAmpStaticError = df_rfAmpStatErr.getDoubleValue();
            rfPhaseStaticError = df_rfPhaseStatErr.getDoubleValue();

            setHasChanges(true);

            /**
             * add below
             */
            if (selectedSequence != null) {
                configureReadbacks();
                misalignElements();
            }
            setNoise.setVisible(false);
        }
    }

    private void misalignElements() {

        quadStatHorMisalign = df_quadStatHorMisalign.getDoubleValue();
        quadStatVerMisalign = df_quadStatVerMisalign.getDoubleValue();

        bpmStatHorMisalign = df_bpmStatHorMisalign.getDoubleValue();
        bpmStatVerMisalign = df_bpmStatVerMisalign.getDoubleValue();

        for (final AcceleratorNode node : getSelectedSequence().getAllNodes()) {
            // note that node offsets are in metres
            node.setXOffset(getStaticHorizontalMisalignmentForElement(node) * 0.001);
            node.setYOffset(getStaticVerticalMisalignmentForElement(node) * 0.001);
        }

        // for on-line model
        try {
            modelScenario = Scenario.newScenarioFor(getSelectedSequence());
        } catch (ModelException exception) {
            System.err.println(exception.getMessage());
        }

        // setting up the default probe
        createDefaultProbe();
    }

    /**
     * synchronize the readbacks with setpoints and synchronize with the online
     * model
     */
    private void syncOnlineModel() {
        if (vaRunning) {
            // add noise, populate "read-back" PVs
            putReadbackPVs();

            // re-sync lattice and run model
            buildOnlineModel();
            try {
                modelScenario.getProbe().reset();
                modelScenario.run();
                // put diagnostic node PVs
                putDiagPVs();
            } catch (ModelException exception) {
                System.err.println(exception.getMessage());
            }
        }
    }

    /**
     * Get a runnable that syncs the online model
     */
    private Runnable getOnlineModelSynchronizer() {
        return new Runnable() {
            @Override
            public void run() {
                syncOnlineModel();
            }
        };
    }

    /**
     * synchronize the readbacks with setpoints and synchronize with the online
     * model
     */
    private void syncPVLogger() {
        if (vaRunning) {
            putSetPVsFromPVLogger();
            putReadbackPVsFromPVLogger();
            putDiagPVsFromPVLogger();
        }
    }

    /**
     * Get a runnable that syncs with the PV Logger
     */
    private Runnable getPVLoggerSynchronizer() {
        return new Runnable() {
            @Override
            public void run() {
                syncPVLogger();
            }
        };
    }
}

/**
 * compare readback set records by their position within a sequence
 */
class ReadbackSetRecordPositionComparator implements Comparator<ReadbackSetRecord> {

    /**
     * sequence within which the nodes are ordered
     */
    final AcceleratorSeq SEQUENCE;

    /**
     * Constructor
     */
    public ReadbackSetRecordPositionComparator(final AcceleratorSeq sequence) {
        SEQUENCE = sequence;
    }

    /**
     * compare the records based on location relative to the start of the
     * sequence
     */
    @Override
    public int compare(final ReadbackSetRecord record1, final ReadbackSetRecord record2) {
        if (record1 == null && record2 == null) {
            return 0;
        } else if (record1 == null) {
            return -1;
        } else if (record2 == null) {
            return 1;
        } else {
            final double position1 = SEQUENCE.getPosition(record1.getNode());
            final double position2 = SEQUENCE.getPosition(record2.getNode());
            return position1 > position2 ? 1 : position1 < position2 ? -1 : 0;
        }
    }

    /**
     * all comparators of this class are the same
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof ReadbackSetRecordPositionComparator;
    }

    /**
     * override hashCode() as required for consistency with equals()
     */
    @Override
    public int hashCode() {
        return 1;   // constant since all comparators of this class are equivalent
    }
}

/**
 * show bpm and ws plots
 */
class DiagPlot {

    protected FunctionGraphsJPanel _beampositionplot;
    protected FunctionGraphsJPanel _sigamplot;
    protected BasicGraphData DataBeamx;
    protected BasicGraphData DataBeamy;
    protected BasicGraphData DataBPMx;
    protected BasicGraphData DataBPMy;
    protected BasicGraphData Datasigmaz;
    protected BasicGraphData DataWSx;
    protected BasicGraphData DataWSy;

    public DiagPlot(FunctionGraphsJPanel beampositionplot, FunctionGraphsJPanel sigamplot) {
        _beampositionplot = beampositionplot;
        _sigamplot = sigamplot;
        setupPlot(beampositionplot, sigamplot);
    }

    public void showbeampositionplot(double[] p, double[] x, double[] y) throws ConnectionException, GetException {
        DataBeamx.updateValues(p, x);
        DataBeamy.updateValues(p, y);
    }

    public void showbpmplot(double[] p, double[] x, double[] y) throws ConnectionException, GetException {
        DataBPMx.updateValues(p, x);
        DataBPMy.updateValues(p, y);
    }

    public void showsigmazplot(double[] p, double[] sigmaz) throws ConnectionException, GetException {
        Datasigmaz.updateValues(p, sigmaz);
    }

    public void showsigmaplot(double[] wsp, double[] wsx, double[] wsy) throws ConnectionException, GetException {
        DataWSx.updateValues(wsp, wsx);
        DataWSy.updateValues(wsp, wsy);
    }

    public void setupPlot(FunctionGraphsJPanel beampositionplot, FunctionGraphsJPanel sigamplot) {
        /**
         * setup beamdispplot
         */
        // labels
        beampositionplot.setName("BeamDisp_PLOT");
        beampositionplot.setAxisNameX("Position(m)");
        beampositionplot.setAxisNameY("Beam displacement (mm)");

        beampositionplot.setNumberFormatX(new DecimalFormat("0.00E0"));
        beampositionplot.setNumberFormatY(new DecimalFormat("0.00E0"));

        // add legend support
        beampositionplot.setLegendPosition(FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY);
        beampositionplot.setLegendKeyString("Legend");
        beampositionplot.setLegendBackground(Color.lightGray);
        beampositionplot.setLegendColor(Color.black);
        beampositionplot.setLegendVisible(true);

        /**
         * setup sigamplot
         */
        // labels
        sigamplot.setName("Sigma_PLOT");
        sigamplot.setAxisNameX("Position(m)");
        sigamplot.setAxisNameY("Beam Envelope(mm)");

        sigamplot.setNumberFormatX(new DecimalFormat("0.00E0"));
        sigamplot.setNumberFormatY(new DecimalFormat("0.00E0"));

        // add legend support
        sigamplot.setLegendPosition(FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY);
        sigamplot.setLegendKeyString("Legend");
        sigamplot.setLegendBackground(Color.lightGray);
        sigamplot.setLegendColor(Color.black);
        sigamplot.setLegendVisible(true);

        DataBeamx = new BasicGraphData();
        DataBeamy = new BasicGraphData();
        DataBPMx = new BasicGraphData();
        DataBPMy = new BasicGraphData();
        DataWSx = new BasicGraphData();
        DataWSy = new BasicGraphData();
        Datasigmaz = new BasicGraphData();

        DataBeamx.setGraphProperty(_beampositionplot.getLegendKeyString(), "BeamxAvg");
        DataBeamy.setGraphProperty(_beampositionplot.getLegendKeyString(), "BeamyAvg");
        DataBPMx.setGraphProperty(_beampositionplot.getLegendKeyString(), "BPMxAvg");
        DataBPMy.setGraphProperty(_beampositionplot.getLegendKeyString(), "BPMyAvg");
        DataWSx.setGraphProperty(_sigamplot.getLegendKeyString(), "sigmax");
        DataWSy.setGraphProperty(_sigamplot.getLegendKeyString(), "sigmay");
        Datasigmaz.setGraphProperty(_sigamplot.getLegendKeyString(), "sigmaz");

        DataBeamx.setGraphColor(Color.blue);
        DataBeamy.setGraphColor(Color.orange);
        DataBPMx.setGraphColor(Color.RED);
        DataBPMy.setGraphColor(Color.BLACK);
        DataWSx.setGraphColor(Color.RED);
        DataWSy.setGraphColor(Color.BLACK);
        Datasigmaz.setGraphColor(Color.blue);

        _beampositionplot.addGraphData(DataBeamx);
        _beampositionplot.addGraphData(DataBeamy);
        _beampositionplot.addGraphData(DataBPMx);
        _beampositionplot.addGraphData(DataBPMy);
        _sigamplot.addGraphData(DataWSx);
        _sigamplot.addGraphData(DataWSy);
        _sigamplot.addGraphData(Datasigmaz);
    }

}
