package edu.stanford.slac.Save2Logbook;

/**
 * @author Koosh
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class WriteLogbookXML 
{
	//writeLogbookXml writes the files into an xml file and copies them over to the appropriate directories
	//check FileCopy for more information
	FileCopy copier;
	
	//default constructor
	WriteLogbookXML()
	{}
	
	public void writeToLogbook(LogbookEntry logbook)
	{		
		for (int j = 0; j < logbook.getFilePaths().size(); j++)
		{
			try
			{
				//copies all of the files that were added as non-snapshots to the directory
				File file = new File(logbook.getFilePaths().get(j));
				String destination = logbook.getDir()+file.getName();
				copier.copy(logbook.getFilePaths().get(j), destination);
				logbook.getFilePaths().set(j, destination);
			}
			catch (IOException ee)
			{
				System.out.println(ee.getMessage());
			}
		}
		System.out.println("Dir: " + logbook.getDir());
		
		FileOutputStream output;
		PrintStream print = null;
		System.out.println("File: " + logbook.getMetainfo());
		//the name of itself is stores in Meta info
		
		try
		{					
			//printing to the xml file
			output = new FileOutputStream(logbook.getDir()+logbook.getMetainfo());
			print = new PrintStream(output);
			print.println("<author>"+validateData(logbook.getAuthor())+"</author>");
			print.println("<category>"+logbook.getCategory()+"</category>");
			print.println("<title>"+validateData(logbook.getTitle())+"</title>");		
			print.println("<time>"+logbook.getTimeTag()+"</time>");					
			print.println("<isodate>"+logbook.getIsodate()+"</isodate>");
			print.println("<severity>"+logbook.getSeverity()+"</severity>");
			print.println("<keywords>"+logbook.getKeyword()+"</keywords>");	
			print.println("<metainfo>"+logbook.getMetainfo()+"</metainfo>");
			//printing a tag for each file
			for (int i = 0; i < logbook.getFilePaths().size(); i ++)
			{
				File f = new File(logbook.getFilePaths().get(i));
				if (f.exists())
				{
					print.print("<image>");
					print.print(f.getName());
					System.out.println("File: " + f.getName());
					print.println("</image>");
				}
				else
				{
					System.err.println(f.getName() + " could not be found!");
				}
			}	
			//printing a tag for each snapshot
			for (int i = 0; i < logbook.getSnapshotPaths().size(); i ++)
			{
				File f = new File(logbook.getSnapshotPaths().get(i));
				if (f.exists())
				{
					print.print("<file>");
					print.print(f.getName());
					System.out.println("Snapshot: " + f.getName());
					print.println("</file>");
				}
				else
				{
					System.err.println(f.getName() + " could not be found!");
				}
			}	
			//printing a tag for each link
			for (int i = 0; i < logbook.getLinkPaths().size(); i ++)
			{
				File f = new File(logbook.getLinkPaths().get(i));
				if (f.exists())
				{
					print.print("<link>");
					print.print(f.getName());
					System.out.println("Link: " + f.getName());
					print.println("</link>");
				}
				else
				{
					System.err.println(f.getName() + " could not be found!");
				} 
			}			
			print.println("<text>"+validateData(logbook.getComments())+"</text>");
		}
		catch (IOException ioException)
		{
			//if file was not created properly
			System.err.print("Error: You cannot access this file");
		}
		finally {
			if (print != null) {
					print.flush();
					print.close();
					File file = new File(logbook.getDir()+logbook.getMetainfo());
					// validate for zero-byte file...
					if (file.exists() && file.length() == 0)
					{
						System.out.println("Zero byte xml file found...deleting it!");
						file.delete();
					}
			}
		}
	}
	
	/**
	 * This method takes in a text string that needs to be written in xml, checks
	 * for illegal XML chars "<", ">" and "&" and replaces them with valid equivalents.
	 * 
	 * @param data the original string 
	 * @return validated data 
	 */
	private String validateData(String data)
	{
		if (data != null)
		{
			data = data.replaceAll("&", "&amp;");
			data = data.replaceAll("<","&lt;");
			data = data.replaceAll(">","&gt;");
		}		
		
		return data;		
	}
}
