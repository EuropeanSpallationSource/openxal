/*
 * Hint.java
 *
 * Created Friday June 11, 2004 11:07 am
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver.hint;
 
 /**
 * Hint is the abstract class used for implementing hints.
 * 
 * @author ky6
 */
 public abstract class Hint {
	 /** the Hint's label */
	 protected String label;
	 
	 
	 /**Creates a new instance of Hint*/
	 public Hint( final String aHint ) {
		 label = aHint;
	 }
	 
	 
	 /**
	  * Get this hint's label.
	  * @return this hint's label.
	  */
	 public String getLabel() {
		 return label;
	 }
	 
	 
	 /** 
	  * Get the type identifier of this Hint which will be used to fetch this hint in a table of hints.  Subclasses should
	  * override this method to return a unique string identifying the hint.
	  * @return the unique type identifier of this Hint
	  */
	 public abstract String getType();
 }
