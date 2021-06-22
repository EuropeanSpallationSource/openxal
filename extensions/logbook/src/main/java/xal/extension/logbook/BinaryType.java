//
//  BinaryType.java
//  xal
//
//  Created by Thomas Pelaia on 12/30/04.
//  Copyright 2004 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.logbook;

import java.net.*;


/**
 * Record identifying an entry image type or an entry attachment type for the electronic logbook.
 */
class BinaryType {
	/** primary key for the binary image/attachment type */
	private final long id;
	
	/** file extension for the binary type */
	private final String extension;
	
	
	/**
	 * Constructor
	 */
	public BinaryType( final long id, final String extension ) {
		this.id = id;
		this.extension = extension;
	}
	
	
	/**
	 * Get the ID.
	 *
	 * @return ID
	 */
	public long getID() {
		return id;
	}
	
	
	/**
	 * Get the file extension corresponding to the binary type.
	 *
	 * @return the file extension
	 */
	public String getExtension() {
		return extension;
	}
	
	
	/**
	 * Get the MIME type corresponding to this binary type.
	 *
	 * @return the MIME type
	 */
	public String getMIMEType() {
		return URLConnection.getFileNameMap().getContentTypeFor("abc." + extension );
	}
	
	
	/**
	 * Determine if the binary data type is an image type.
	 *
	 * @return true if the binary type is an image type and false if not.
	 */
	public boolean isImageType() {
		return getMIMEType().startsWith("image/");
	}
	
	
	/**
	 * Get the kind of binary data ("I" for image or "A" for attachment).
	 * 
	 * @return "I" for image or "A" for attachment
	 */
	public String getTypeCode() {
		return isImageType() ? "I" : "A";
	}
	
	
	/**
	 * Get a string representation of this binary type.
	 * 
	 * @return the extension
	 */
        @Override
	public String toString() {
		return "ID: " + id + ", extension: " + extension;
	}
}
