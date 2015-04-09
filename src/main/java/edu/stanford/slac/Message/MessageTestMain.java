package edu.stanford.slac.Message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import edu.stanford.slac.logapi.MessageLogAPI;

public class MessageTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MessageLogAPI err = MessageLogAPI.getInstance("Message Test Main");

		//JTextPane j = new JTextPane();
		//JTextPane s = j;
		//JFrame f = new JFrame("JTextPane");

		JTextPane j = new JTextPane();
		JScrollPane s = new JScrollPane(j); 
		JFrame f = new JFrame("JTextPane in JScrollPane");

		//JTextArea j = new JTextArea();
		//JTextArea s = j;
		//JFrame f = new JFrame("JTextArea");

		//JTextArea j = new JTextArea();
		//JScrollPane s = new JScrollPane(j); 
		//JFrame f = new JFrame("JTextArea in JScrollPane");


		j.setBackground(Color.LIGHT_GRAY);
		j.setFont( new Font(j.getFont().toString(), Font.BOLD, 16));
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(700, 500));
		f.setLayout(new BorderLayout());
		f.add(s);
		f.pack();
		f.setVisible(true);

		Message.setSwingWidget(j);

		Message.info("Current date format: `" + Message.getPackage().getDateFormat() + "'");
		Message.getPackage().setDateFormat("d-MMM-y H:mm:ss.S ");
		Message.info("New date format: `" + Message.getPackage().getDateFormat() + "'");

		Message.info("Trying logger.log methods:");
		Message.log("informational message", Message.INFO);
		Message.log("warning message", Message.WARNING);
		Message.log("error message", Message.ERROR);
		Message.log("fatal message", Message.FATAL);
		Message.log("debug message", Message.DEBUG);
		Message.log("Green message", Color.GREEN);
		Message.log("Magenta message", Color.MAGENTA);

		Message.info("Trying logger.level methods:");
		Message.info("informational message");
		Message.warning("warning message");
		Message.error("error message");
		Message.fatal("fatal message");
		Message.debug("debug message");

		for (int i=0; i<10000; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Message.info(Integer.toString(1+i) + " seconds.");
			Message.warning(Integer.toString(1+i) + " seconds.");
			Message.error(Integer.toString(1+i) + " seconds.");
			Message.fatal(Integer.toString(1+i) + " seconds.");
			Message.debug(Integer.toString(1+i) + " seconds.");
		}

	}

}
