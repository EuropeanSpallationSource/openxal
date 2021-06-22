//
//  ConcreteBlob.java
//  xal
//
//  Created by Thomas Pelaia on 1/3/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.database;

import java.sql.Blob;
import java.io.*;
import java.sql.SQLException;


/**
 * Concrete implementation of an SQL Blob.
 */
public class ConcreteBlob implements Blob {
	protected byte[] data;
	
	
	/**
	 * Primary Constructor
	 * @param capacity The number of bytes allocated for data storage.
	 */
	public ConcreteBlob( final int capacity ) {
		data = new byte[ capacity ];
	}
	
	
	/**
	 * Constructor
	 */
	public ConcreteBlob() {
		this( 0 );
	}
	
	
	/**
	 * Get an input stream that can read the BLOB data.
	 */
        @Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream( data );
	}
	
        @Override
	public InputStream getBinaryStream(long position, long length) {
		final byte[] data = new byte[(int)length - (int) position];
		System.arraycopy( this.data, (int)position, data, 0, (int) length);		
		return new ByteArrayInputStream( data );
	}
		
        @Override
	public void free() {
		// clean up after the Blob object finished
	}
	
	/**
	 * Get the specified part of the blob data as an array of bytes.
     * @param position
	 */
        @Override
	public byte[] getBytes( final long position, final int length ) {
		final byte[] data = new byte[length - (int)position];
		System.arraycopy( this.data, (int)position, data, 0, length);
		
		return data;
	}
	
	
	/**
	 * Get the number of bytes in this BLOB.
	 */
        @Override
	public long length() {
		return data.length;
	}
	
	
	/**
	 * Get the position of the first occurrence of pattern in this BLOB starting at the position specified by start.
	 */
        @Override
	public long position( final Blob pattern, final long start ) throws SQLException {
		return position( pattern.getBytes( 0, (int)pattern.length() ), start );
	}
	
	
	/**
	 * Get the position of the first occurrence of pattern in this BLOB starting at the position specified by start.
	 */
        @Override
	public long position( final byte[] pattern, long start ) {
		final long MAX_INDEX = data.length - pattern.length + 1;
		
		for ( long index = 0 ; index < MAX_INDEX ; index++, start++ ) {
			if ( isMatch( pattern, 0, (int)start ) )  return start;
		}
			  
		return -1;
	}
	
	
	/**
	 * Determine if the pattern matches the specified range of bytes in this BLOB.
	 * 
	 * @param pattern the pattern of bytes to match against data bytes
	 * @param offset the index of the byte in pattern against which to match the corresponding byte in data
	 * @param start the index in of the byte in data against which to match the corresponding byte in pattern
	 */
	private boolean isMatch( final byte[] pattern, final int offset, final int start ) {
		// if the offset equals pattern length then we have successfully matched every byte
		// if the byte in pattern matches the corresponding byte in data then check the next byte and so forth
		return offset == pattern.length ? true : ( (pattern[offset] == data[start]) ? isMatch( pattern, offset+1, start+1 ) : false );
	}
	
	
	/**
	 * Get an output stream for writing to this BLOB.  This implementation simply throws an unsupported operation exception.
	 */
        @Override
	public OutputStream setBinaryStream( final long position ) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Set the specified bytes.
	 */
        @Override
	public int setBytes( long position, byte[] bytes, int offset, int length ) {
		System.arraycopy( bytes, offset, this.data, (int)position, length);
		
		return length;
	}
	
	
	/**
	 * Set the specified bytes.
	 */
        @Override
	public int setBytes( long position, byte[] bytes ) {		
		return setBytes( position, bytes, 0, bytes.length );
	}
	
	
	/**
	 * Truncate this BLOB to be the specified length.
	 */
        @Override
	public void truncate( long length ) {
		final byte[] data = new byte[(int)length];
		System.arraycopy( this.data, 0, data, 0, (int)length );
		this.data = data;
	} 
	
}
