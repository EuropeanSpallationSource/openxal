package edu.stanford.slac.Save2Logbook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * @author Koosh
 */
public class ImageConverter 
{
	ImageConverter()
	{}
	
	public void convertToElog(String jpgFile,String psFile)
	throws IOException
	{
		//input and output for the conversion to postscript, input being the jpg output being the postscript
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(psFile));
		InputStream is = new BufferedInputStream(new FileInputStream(jpgFile));
		
		//getting the right StreamPrintService to convert the image
		DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
		StreamPrintServiceFactory[] psfactories =
			StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
				flavor, psMimeType);
		
		//if the length of psfactories is 0 then no service factory which can perform the conversion
		if (psfactories.length > 0)
		{
			//creating a print job to print to the output file (the post script)
			StreamPrintService psService = psfactories[0].getPrintService(fos);
			DocPrintJob dpj = psService.createPrintJob();
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			//input and type of conversion
			Doc doc = new SimpleDoc(is,flavor,null);	
			
			try
			{
				//printing from input to output file .ps
				dpj.print(doc, pras);
			}
			catch (PrintException pe)
			{
				System.err.println("Error Converting jpg to ps");
				return;
			}
		}
	}
	
	public void convertGIFtoPS(String gifFile,String psFile)
	throws IOException
	{
		//input and output for the conversion to postscript, input being the jpg output being the postscript
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(psFile));
		InputStream is = new BufferedInputStream(new FileInputStream(gifFile));
		
		//getting the right StreamPrintService to convert the image
		DocFlavor flavor = DocFlavor.INPUT_STREAM.GIF;
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
		StreamPrintServiceFactory[] psfactories =
			StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
				flavor, psMimeType);
		
		//if the length of psfactories is 0 then no service factory which can perform the conversion
		if (psfactories.length > 0)
		{
			//creating a print job to print to the output file (the post script)
			StreamPrintService psService = psfactories[0].getPrintService(fos);
			DocPrintJob dpj = psService.createPrintJob();
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			//input and type of conversion
			Doc doc = new SimpleDoc(is,flavor,null);	
			
			try
			{
				//printing from input to output file .ps
				dpj.print(doc, pras);
			}
			catch (PrintException pe)
			{
				System.err.println("Error Converting gif to ps");
				return;
			}
		}
	}
}