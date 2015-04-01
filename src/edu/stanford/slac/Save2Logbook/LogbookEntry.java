package edu.stanford.slac.Save2Logbook;

/**
 * @author Koosh
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class LogbookEntry 
{
	//basic GUI strings
	private String author = "AUTHOR";
	private String title = "TITLE";
	private String comments = "COMMENTS HERE";
	private String metainfo;
	private String timeTag;
	
	//have to be passed in through enum's -- check the LocationEnum/KeywordEnum/SeverityEnum etc
	private String severity = SeverityEnum.NONE.toString();
	private String location = LocationEnum.NOTSET.toString();
	private String keyword = KeywordEnum.NONE.toString();
	private String category = "USERLOG";
	
	//used for file directories, filenames, and date
	private String dir;
	private String fileTag;
	private String isodate;
	
	//keep track of the files being passed in and out of the logbook
	private ArrayList<String> filePaths = new ArrayList<String>();	
	private ArrayList<String> snapshotPaths = new ArrayList<String>();
	private ArrayList<String> linkPaths = new ArrayList<String>();
	
	//check class FileCopy and ImageConverter for more information about how they work
	private GregorianCalendar calendar;
	private ImageConverter ic = new ImageConverter();;	
	private FileCopy copier = new FileCopy();
	
	private WriteLogbookXML xml = new WriteLogbookXML();
	
	//caseTwoA - (check class LogbookUseCases for more information on how to
		//use these constructors appropriately 
	public LogbookEntry(Container component, JFrame frame, String jpgFile, String imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					try
					{
						saveSnapshot(component);
					}
					catch (IOException ioException)
					{
						System.err.println("Error Saving JPG");
					}
				}		
				if (jpgFile !=null)
				{
					addJPGFile(jpgFile);
				}
				if (imageFile != null)
				{
					addImageFile(imageFile);
				}
				if (writeXML)
				{
					WriteXMLAndFinish();				
				}	
			}
	
	//caseTwoB
	LogbookEntry(Container[] component, JFrame frame, String jpgFile, String imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					takeMultipleSnapshots(component);
				}		
				if (jpgFile !=null)
				{
					addJPGFile(jpgFile);
				}
				if (imageFile != null)
				{
					addImageFile(imageFile);
				}
				if (writeXML)
				{
					WriteXMLAndFinish();				
				}	
			}
	//caseTwoC
	LogbookEntry(Container component, JFrame frame, String []jpgFile, String[]imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					try
					{
						saveSnapshot(component);
					}
					catch (IOException ioException)
					{
						System.err.println("Error Saving JPG");
					}
				}		
				if (jpgFile !=null)
				{
					for (int i = 0; i < jpgFile.length; i++)
					{
						String j = jpgFile[i];
						if (j == null)
							break;
						addJPGFile(jpgFile[i]);
					}
				}
				if (imageFile !=null && imageFile.length != 0)
				{
					for (int i = 0; i < imageFile.length; i++)
					{
						String j = imageFile[i];
						if (j == null)
							break;
						addImageFile(imageFile[i]);
					}
				}
				if (writeXML)
				{
					WriteXMLAndFinish();				
				}	
			}
	//caseTwoD
	LogbookEntry(Container[] component, JFrame frame, String []jpgFile, String[]imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword, Boolean writeXML)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					takeMultipleSnapshots(component);					
				}		
				if (jpgFile !=null)
				{
					for (int i = 0; i < jpgFile.length; i++)
					{
						String j = jpgFile[i];
						if (j == null)
							break;
						addJPGFile(jpgFile[i]);
					}
				}
				if (imageFile !=null && imageFile.length != 0)
				{
					for (int i = 0; i < imageFile.length; i++)
					{
						String j = imageFile[i];
						if (j == null)
							break;
						addImageFile(imageFile[i]);
					}
				}
				if (writeXML)
				{
					WriteXMLAndFinish();				
				}	
			}
	//caseThreeA
	public LogbookEntry(Boolean displayGUI, Container component, JFrame frame, String jpgFile, String imageFile, String author, String title,
		String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
		{
			setTime();	
			this.author = author;
			this.title = title;
			this.comments = comments;
			this.severity = severity.toString();
			this.location = location.toString();
			this.keyword = keyword.toString();
			if (displayGUI)
			{
				LogbookLiteDialog dialog = new LogbookLiteDialog(this, frame);
				String[] args = new String[10];
				dialog.main(args);
			}	
			
			if (component != null) {
			try {
				saveSnapshot(component);
			} catch (IOException ioException) {
				System.err.println("Error Saving JPG");
			}
		}
		if (jpgFile != null) {
			addJPGFile(jpgFile);
		}
		if (imageFile != null) {
			addImageFile(imageFile);
		}

		}
	// caseThreeB
	LogbookEntry(Boolean displayGUI, Container[] component, JFrame frame, String jpgFile, String imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					takeMultipleSnapshots(component);
				}		
				if (jpgFile !=null)
				{
					addJPGFile(jpgFile);
				}
				if (imageFile != null)
				{
					addImageFile(imageFile);
				}
				if (displayGUI)
				{
					if (frame != null)
					{
						displayLogbookDialog(frame);
					}
					else 
					{
						displayLogbookDialogWithoutFrame();
					}
				}	
			}
	//caseThreeC
	LogbookEntry(Boolean displayGUI, Container[] component, JFrame frame, String[] jpgFile, String[] imageFile, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (jpgFile !=null)
				{
					for (int i = 0; i < jpgFile.length; i++)
					{
						String j = jpgFile[i];
						if (j == null)
							break;
						addJPGFile(jpgFile[i]);
					}
				}
				if (imageFile !=null && imageFile.length != 0)
				{
					for (int i = 0; i < imageFile.length; i++)
					{
						String j = imageFile[i];
						if (j == null)
							break;
						addImageFile(imageFile[i]);
					}
				}
				if (component != null)
				{
					takeMultipleSnapshots(component);					
				}		
				
				if (displayGUI)
				{
					if (frame != null)
					{
						displayLogbookDialog(frame);
					}
					else
					{
						displayLogbookDialogWithoutFrame();
					}
				}	
			}
	//caseFourA
	LogbookEntry(Container[] component, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					takeMultipleSnapshots(component);					
				}	
			}
	//caseFourB
	public LogbookEntry(Container component, String author, String title,
			String comments, SeverityEnum severity, LocationEnum location, KeywordEnum keyword)
			{
				setTime();	
				this.author = author;
				this.title = title;
				this.comments = comments;
				this.severity = severity.toString();
				this.location = location.toString();
				this.keyword = keyword.toString();
				
				if (component != null)
				{
					try
					{
						saveSnapshot(component);
					}
					catch (IOException ioException)
					{
						System.err.println("Error Saving JPG");
					}
				}	
			}
	//caseFourC
	public LogbookEntry(String author, String title, String comments, SeverityEnum severity, 
			LocationEnum location, KeywordEnum keyword)
	{
		setTime();
		this.author = author;
		this.title = title;
		this.comments = comments;
		this.severity = severity.toString();
		this.location = location.toString();
		this.keyword = keyword.toString();
	}
	//caseOne
	public LogbookEntry()
	{
		setTime();
	}
	
	private void resetTime()
	{
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
		Calendar cal = calendar.getInstance(tz);
		Date c = cal.getTime();
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
		String fullDate = dateTimeFormatter.format(c);
		this.fileTag = fullDate.substring(0, fullDate.length()-1);
	}
	public void resize(String image, String output)
	{
		ImageIcon firstImage = new ImageIcon(image);
		Image img = firstImage.getImage();
		ImageUtil i = new ImageUtil();
		
		
		int width = 400;
		try
		{
			i.resize(new File(image), new File(output), width, 1f);
		}	
		catch(IOException ie)
		{
			System.err.println(ie.getMessage());
		}
		
	}
	private void setTime()
	{
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
		Calendar cal = calendar.getInstance(tz);
		int year = cal.get(Calendar.YEAR);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		Date c = cal.getTime();
		
		//uses SimpleDateFormat to get the file tag format
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
		String fullDate = dateTimeFormatter.format(c);
		fileTag = fullDate.substring(0, fullDate.length()-1);
		metainfo = fileTag + ".xml";
		
		SimpleDateFormat dateYearFormatter = new SimpleDateFormat("yyyy-MM-dd");
		isodate = dateYearFormatter.format(c);
	
		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
		timeTag = timeFormatter.format(c);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM");		
		String date = dateFormatter.format(c);
		
		String variable = System.getenv("PHYSICS_DATA");
		dir = variable+"/logbook/data/";	
		File file = new File(dir);
		file.mkdirs();	
		
	}	
	
	//allows user to take a snapshot with a container (usually
		//frame.getContentPane()
	public void saveSnapshot(final Container component)
	throws IOException 
	{
			//resets the fileTag
			resetTime();
			
			//takes a snapshot
			BufferedImage image = new BufferedImage( component.getWidth(), component.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
        	component.paintAll(image.createGraphics());
        	
        	//sets the name and directory of the file
        	//String psFile = dir+fileTag+".ps";   
        	String jpegFile = dir+fileTag+".jpeg";
        	String jpgFile = dir+fileTag+".jpg";
			File defaultFile = new File(jpegFile);
			defaultFile.mkdirs();
			
			//writes the file to the jpeg
			ImageIO.write( image, "JPEG", defaultFile);
			
			//converts the postscript to a jpg
			//ic.convertToElog(jpgFile, psFile);
			
			//adds both to the arraylists
			resize(jpegFile, jpgFile);
			snapshotPaths.add(jpgFile);
			linkPaths.add(jpegFile);
			
			
			//success
			System.out.println("Sucessfully took snapshot");
	}
	
	//allows the user to take a snapshot for himself
	public void takeOwnSnapshot(Component component, String outputFilePath)
	{
		try
		{	
			//the same thing as saveSnapshot, but with an outputFilePath
			BufferedImage image = new BufferedImage( component.getWidth(), component.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
	    	component.paintAll(image.createGraphics()); 
	    	String jpgFile = outputFilePath;   
			File defaultFile = new File(jpgFile);
			defaultFile.mkdirs();
			ImageIO.write( image, "jpg", defaultFile);	
		}
		catch (IOException ioException)
		{
			System.err.println("Error Saving JPG");
		}
	}
	
	//take MultipleSnapshots loops saveSnapshot with multiple components
	public void takeMultipleSnapshots(Component[] components)
	{
		for (int i = 0; i < components.length; i++)
		{
			if (components[i] == null)
			{
				break;
			}
			try
			{						
				resetTime();
				BufferedImage image = new BufferedImage( components[i].getWidth(), components[i].getHeight(), BufferedImage.TYPE_3BYTE_BGR );
	        	components[i].paintAll(image.createGraphics()); 
	        	String jpgFile = dir+fileTag+".jpg";  
	        	String jpegFile = dir+fileTag+".jpeg"; 
				File defaultFile = new File(jpegFile);
				defaultFile.mkdirs();
				ImageIO.write( image, "jpeg", defaultFile);
				//ic.convertToElog(jpgFile,jpegFile);
				linkPaths.add(jpegFile);
				resize(jpegFile, jpgFile);
				snapshotPaths.add(jpgFile);
			}
			catch (IOException ioException)
			{
				System.err.println("Error Saving JPG");
			}
		}
		System.out.println("Sucessfully took snapshots");
	}
	
	//allows the user to add a Non-Jpeg image file
	public void addImageFile(String dir)
	{
		resetTime();
		try
		{
			FileCopy.check(dir);
			filePaths.add(dir);
		}
		catch (IOException i)
		{
			System.err.println(i.getMessage());
		}
	}
	
	//allows user to add a jpeg file which is converted to postscript
	public void addJPGFile(String dir)
	{
		resetTime();
		String ext = new String();
		try
		{
			File file = new File(dir);
			FileCopy.check(dir);
			String e = file.getName();
			//finds the extension (.jpeg or .jpg) and replaces it  with  .ps for the postscript
			for (int i = e.length()-1; i > 0; i--)
			{
				if (e.charAt(i) == '.')
				{
					ext = e.substring(0, i);
					
					break;
				}
			}	
			ic.convertToElog(dir, this.dir+ext+".ps");			
			copier.copy(dir, this.dir+file.getName());
			String jpgFile = this.dir+file.getName();
			resize(jpgFile, jpgFile);			
			snapshotPaths.add(this.dir+file.getName());
			linkPaths.add(this.dir+ext+".ps");
			System.out.println("Sucessfully added " + file.getName() + " and converted to postscript");
		}
		catch (IOException i)
		{
			System.err.println(i.getMessage());
		}
	}
	
	//allows the user to add multiple files at once
	public void addMultipleImageFiles(String[]filePaths)
	{
		for (int i = 0; i < filePaths.length; i ++)
		{
			resetTime();
			try
			{
				FileCopy.check(filePaths[i]);
				this.filePaths.add(filePaths[i]);
			}
			catch (IOException ie)
			{
				System.err.println(ie.getMessage());
			}
		}
	}
	
	//pulls up the logbook dialog
		//should be last piece of code envoked for the particular file
	public void displayLogbookDialog(JFrame frame)
	{
		LogbookDialog dialog = new LogbookDialog(this, frame);
		String[] args = new String[10];
		dialog.main(args);
	}
	
	//pulls up the logbook dialog without a frame
	public void displayLogbookDialogWithoutFrame()
	{
		LogbookDialog dialog = new LogbookDialog(this);
		String[] args = new String[10];
		dialog.main(args);
	}
	

	//this should always be the last function envoked unless a GUI is brought up
	public void WriteXMLAndFinish()
	{
		xml.writeToLogbook(this);
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		if (author == null)
			author = "";
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		if (title == null)
			title = "";
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(SeverityEnum severity) {
		this.severity = severity.toString();
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(LocationEnum location) {
		this.location = location.toString();
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(KeywordEnum keyword) {
		this.keyword = keyword.toString();
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		if (comments == null)
			comments ="";
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the filePaths
	 */
	public ArrayList<String> getFilePaths() {
		return filePaths;
	}
	
	public ArrayList<String> getLinkPaths() {
		return linkPaths;
	}
	public ArrayList<String> getSnapshotPaths() {
		return snapshotPaths;
	}

	/**
	 * @return the calendar
	 */
	public GregorianCalendar getCalendar() {
		return calendar;
	}

	/**
	 * @return the ic
	 */
	public ImageConverter getIc() {
		return ic;
	}

	/**
	 * @return the dir
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * @return the fileTag
	 */
	public String getFileTag() {
		resetTime();
		return fileTag;
	}

	public String getMetainfo() {
		return metainfo;
	}
	/**
	 * @return the isodate
	 */
	public String getIsodate() {
		return isodate;
	}

	public String getTimeTag() {
		return timeTag;
	}

	public void setTimeTag(String timeTag) {
		this.timeTag = timeTag;
	}

}
