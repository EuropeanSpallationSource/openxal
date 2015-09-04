package eu.ess.lt.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import eu.ess.bled.Subsystem;

/**
 * Interface defining an input parsing object, that can create BLED from a
 * specific source type. This source is a file, formatted in a
 * specific way. Elements are read through the specified file.
 * Feedback regarding import can be shared using
 * the specified responseWriter.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
 *         Prelog</a>
 * @author Blaz Kranjc
 */
public interface Importer {
	/**
	 * Starts the import procedure. Elements will be read form the specified
	 * file and import feedback through the
	 * <code>responseWriter</code>. 
	 * 
	 * @param fileName
	 *            {@link String} name of the file to be parsed.
	 * @param repsonseWriter
	 *            {@link PrintWriter} used for giving back the feedback on the
	 *            import.
	 * @return the collections of all imported systems
	 * @throws IOException
	 *             if there is an exception reading the source.
	 */
	public Collection<Subsystem> importFromTraceWin(String fileName, PrintWriter repsonseWriter)
			throws IOException;
}
