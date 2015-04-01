package edu.stanford.slac.Save2Logbook;

import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * @author Koosh
 */


public class LogbookUseCases
{
	
	
	public static void main(String[] args) 
	{	
		JFrame f = new JFrame();
		f.setVisible(true);	
		
		System.out.println ("Version:  Save2Logbook-R0-0-2");
		File file = new File("/afs/slac.stanford.edu/u/cd/drogind/drogind2/ws_koosh/Save2Logbook/src/edu/stanford/slac/LogbookEntry/score.png");
		
		ImageIcon firstImage = new ImageIcon(file.getPath());
		JLabel picture = new JLabel(firstImage);
			
		f.getContentPane().add(picture);
		f.pack();
		
		/*JFrame ff = new JFrame();
		Container newContentPane2 = new Container();
		ff.setContentPane(newContentPane2);
		ff.setBounds(40, 40, 500,500);
		ff.setVisible(true);*/
		
		LogbookEntry a = new LogbookEntry();
		
		try
		{
			a.saveSnapshot(f.getContentPane());
		}
		catch (IOException i)
		{}
		a.WriteXMLAndFinish();
		
		System.exit(0);
	   /*
		************************************************************************************* 
		* USE CASE(S) 1
		* (The Stick Shift)
		*************************************************************************************
		
	    * LogbookEntry();
	    *
		* Uses the default constructor which allows the user to do everything by him/herself	 
		* Note: The last statement should ALWAYS be WriteXMLAndFinish();
		* EXAMPLE:
		*/
		/**
		LogbookEntry caseOne = new LogbookEntry();
		
		try	
		{
			caseOne.saveSnapshot(f.getContentPane());
		}		
		catch (IOException i)
		{
			System.err.println(i.getMessage());
		}
		try
		{
			caseOne.saveSnapshot(ff.getContentPane());
		}
		catch (IOException i)
		{
			System.err.println(i.getMessage());
		}
		
		caseOne.addImageFile("/afs/slac.stanford.edu/u/cd/drogind/image.jpg");			
		caseOne.addImageFile("/afs/slac.stanford.edu/u/cd/drogind/image.jpg");
		caseOne.setAuthor("Koosh");
		caseOne.setSeverity(SeverityEnum.DELETE);
		caseOne.setKeyword(KeywordEnum.INJECTOR);
		caseOne.setLocation(LocationEnum.GUN);
		caseOne.setComments("I think Koosh is great..");
		caseOne.setTitle("The Greatest Program");
		/**/
		//not necessary but able to envoke GUI with statements:
		//displayLogbookDialog(JFrame frame) or displayLogbookDialogWithoutFrame() 
		
		//Last Statement
		//caseOne.WriteXMLAndFinish();
		/**/
	   /*
		*************************************************************************************
		* USE CASE(S) 2 
		* (The Wham Bam Thank You Mam) (Intended for non-GUI-one-statement-and-finish use)
		************************************************************************************* 
		
		*
		* LogbookEntry(Container component, JFrame frame, String jpgFile, String imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)	
		* 
		* LogbookEntry(Container[] component, JFrame frame, String jpgFile, String imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
		* 
		* LogbookEntry(Container component, JFrame frame, String []jpgFile, String[]imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
		*
		* LogbookEntry(Container[] component, JFrame frame, String []jpgFile, String[]imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)	
		*	
		*
		* NOTE: Watch for ambiguous constructors, DO NOT simply put null in for values
		* 		These constructors do everything without the GUI
		* 		After constructor is constructed, the xmlFile, and all files have been transferred to the 
		* 			appropriate directories
		*/
		/**
		Container[] components = new Container[5];
		components[0] = f.getContentPane();
		components[1] = ff.getContentPane();
		
		//create null types so class knows which constructor is being used
		String nullString = null;
		//creating an empty array so class knows which constructor is being used
 		String []nullArray = new String[5];		
 		//specifying jpeg file
		String jpgFile = "/afs/slac.stanford.edu/u/cd/drogind/image.jpg";
		//creating an array with one file 
		String []fileArray = new String[10];
		fileArray[0] = jpgFile;
		
		//Example A
		LogbookEntry caseTwoA = new LogbookEntry(f.getContentPane(), f, nullString,
				nullString, "Koosh", "The Magnetifier",null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS, true);
		/**
		//Example B
		LogbookEntry caseTwoB = new LogbookEntry(components, f, nullString,
				nullString, "Koosh", "The Magnetifier",null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS, true);

		//Example C
		LogbookEntry caseTwoC = new LogbookEntry(f.getContentPane(), f, nullArray,
				fileArray, "Koosh", "The Magnetifier", null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS, true);

		//Example D
		LogbookEntry caseTwoD = new LogbookEntry(components, f, fileArray,
				nullArray, "Koosh", "The Magnetifier", null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS, true);
		**/
	   /*
		************************************************************************************* 
		* USE CASE(S) 3 
		* (The Push-over) (Intended for GUI use) 
		************************************************************************************* 
		*
		* LogbookEntry(Boolean displayGUI, Container component, JFrame frame, String jpgFile, String imageFile, String author, String title,
		    String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)	
		*
		* LogbookEntry(Boolean displayGUI, Container[] component, JFrame frame, String jpgFile, String imageFile, String author, String title,
		    String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)		
		* 
		* LogbookEntry(Boolean displayGUI, Container[] component, JFrame frame, String[] jpgFile, String[] imageFile, String author, String title,
		    String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)	
		*		
		* Note: putting in false for displayGUI will not automatically create an XML file, 
		*       If you don't want a GUI use CASE 2
		*       Watch for ambiguous constructors, DO NOT simply put null in for values
		*/
		/**
		
		Container[] components = new Container[5];
		components[0] = f.getContentPane();
		components[1] = ff.getContentPane();
		
		String []nullArray = new String[0];
		String nullString = null;
		
		String jpgFile = new String();
		jpgFile = "/afs/slac.stanford.edu/u/cd/drogind/image.jpg";
		
		String []fileArray = new String[10];
		fileArray[0] = jpgFile;
		
		LogbookEntry caseThreeA = new LogbookEntry(true, f.getContentPane(), null, jpgFile, nullString, 
				"Koosh", "The Magnetifier",null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);	
		
		LogbookEntry caseThreeB = new LogbookEntry(true, components, null, nullString, jpgFile, 
				"Koosh", "The Magnetifier",null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);
		
		LogbookEntry caseThreeC = new LogbookEntry(true, components, null, fileArray, nullArray, 
				"Koosh", "The Magnetifier",null, SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);	
		
		**/
		/*
		 *************************************************************************************
		 * USE CASE(S) 4 
		 * (The Short Stop) (Intended for either GUI, nonGUI, or nonFile use)
		 *************************************************************************************
		 * 
		 * LogbookEntry(Container[] component, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
		 * 
		 * LogbookEntry(Container component, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
		 *
		 * LogbookEntry(String author, String title, String comments, SeverityEnum severity, 
			LocationEnum location, KeywordEnum keyword)
		 *
		 * NOTE: These constructors are mainly used for quick uses without additional files
		 * 		 It does not automatically create an xmlFile
		 *       Note: The last statement should ALWAYS be WriteXMLAndFinish(); 		 
	  	*/
		/**
		Container[] components = new Container[5];
		components[0] = f.getContentPane();
		components[1] = ff.getContentPane();
		
		//example 
		LogbookEntry caseFourA = new LogbookEntry(f.getContentPane(), "Koosh", "The Magnetifier", null, 
				SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);
		caseFourA.WriteXMLAndFinish(); 
		
		//example
		LogbookEntry caseFourB = new LogbookEntry(components, "Koosh", "The Magnetifier", null, 
				SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);	
		caseFourB.WriteXMLAndFinish(); 
		
		//emaple
		LogbookEntry caseFourC = new LogbookEntry("Koosh", "The Magnetifier", null, 
				SeverityEnum.MEASURE, LocationEnum.L1, KeywordEnum.DIAGNOSTICS);
		caseFourC.WriteXMLAndFinish(); 
		
		**/
	}

}












