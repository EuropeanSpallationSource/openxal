package se.lu.esss.ics.jels.matcher;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.Timer;

import xal.extension.widgets.beaneditor.SimpleBeanEditor;
import xal.model.IAlgorithm;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;

public class MatcherDialog extends SimpleBeanEditor<Matcher> {
	private static final long serialVersionUID = 1L;
	   
	private JProgressBar progressBar = new JProgressBar();
	private JButton match = new JButton("Match");
	private JButton abort = new JButton("Abort");
	
	public MatcherDialog(final Frame owner, final Matcher conf, boolean visible)
	{
		 
		 super( owner, "Matcher", null, conf, false, false );	//Set JDialog's owner, title, and modality
		 
		 Box bottomPane = new Box(BoxLayout.X_AXIS);
		 bottomPane.add(progressBar);
		 bottomPane.add(new JSeparator());
		 bottomPane.add(match);
		 bottomPane.add(abort);
		 
		 progressBar.setVisible(false);
		 abort.setEnabled(false);
		 
		 match.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				match.setEnabled(false);
				setEnabled(false);
				
				publishToBean();
		
				final Thread matcher = new Thread(getBean());
				matcher.start();
				//progressBar.setIndeterminate(true);
				
				progressBar.setMaximum(100);
				progressBar.setValue(0);
				progressBar.setVisible(true);
				abort.setEnabled(true);
				
				new Thread() {
					public void run() {
						try {
							while (matcher.isAlive()) {
								matcher.join(500);
								progressBar.setValue((int)(getBean().getProgress()*100));
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							progressBar.setVisible(false);
							reloadBean();
							setEnabled(true);
							match.setEnabled(true);
							abort.setEnabled(false);
						}
					}
				}.start();
			}
		 });
		
		 
		 abort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBean().abort();
			}
		});
		 
		 add(bottomPane, BorderLayout.SOUTH);
		 
		 setVisible(true);
	}

	public static void main(String args[]) throws InstantiationException
	{
		Accelerator accelerator = Matcher.loadAccelerator();
		//IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( accelerator );
		IAlgorithm tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
		
		EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker );
		
		new MatcherDialog(null, new Matcher(accelerator, probe), true);
	}
}
