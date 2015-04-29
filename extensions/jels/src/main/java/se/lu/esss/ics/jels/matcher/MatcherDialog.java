package se.lu.esss.ics.jels.matcher;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;

import xal.extension.widgets.beaneditor.SimpleBeanEditor;
import xal.model.probe.EnvelopeProbe;



public class MatcherDialog extends SimpleBeanEditor<MatcherConfiguration> {
	private static final long serialVersionUID = 1L;
	   
	private JProgressBar progressBar = new JProgressBar();
	private JButton match = new JButton("Match");
	
	public MatcherDialog(final Frame owner, final MatcherConfiguration conf, boolean visible)
	{
		 
		 super( owner, "Matcher", null, conf, false );	//Set JDialog's owner, title, and modality
		 
		 Box bottomPane = new Box(BoxLayout.X_AXIS);
		 bottomPane.add(progressBar);
		 bottomPane.add(match);
		
		 add(bottomPane, BorderLayout.SOUTH);
		 
		 setVisible(true);
	}

	public static void main(String args[])
	{
		new MatcherDialog(null, new MatcherConfiguration(new EnvelopeProbe()), true);
	}
}
