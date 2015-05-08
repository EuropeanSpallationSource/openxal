package se.lu.esss.ics.jels.matcher;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;

import xal.extension.widgets.beaneditor.SimpleBeanEditor;

public class MatcherDialog extends SimpleBeanEditor<Matcher> {
	private static final long serialVersionUID = 1L;
	   
	private JProgressBar progressBar = new JProgressBar();
	private JButton match = new JButton("Match");
	
	public MatcherDialog(final Frame owner, final Matcher conf, boolean visible)
	{
		 
		 super( owner, "Matcher", null, conf, false, false );	//Set JDialog's owner, title, and modality
		 
		 Box bottomPane = new Box(BoxLayout.X_AXIS);
		 bottomPane.add(progressBar);
		 bottomPane.add(match);
		 
		 match.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				publishToBean();
				setEnabled(false);
				final Thread matcher = new Thread(getBean());
				matcher.start();
				progressBar.setIndeterminate(true);
				new Thread() {
					public void run() {
						try {
							matcher.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							progressBar.setIndeterminate(false);
							revertFromBean();
							setEnabled(true);
						}
					}
				}.start();
			}
		 });
		
		 add(bottomPane, BorderLayout.SOUTH);
		 
		 setVisible(true);
	}

	public static void main(String args[])
	{
		new MatcherDialog(null, new Matcher(Matcher.loadAccelerator()), true);
	}
}
