/*
 * Constraint.java
 *
 * Created Wednesday June 9, 2004 2:34pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver.constraint;

 
 import xal.extension.solver.Trial;
 import xal.extension.solver.TrialVeto;


 /**
 * Constraint is the class which holds the users' constraint.
 * 
 * @author  ky6
 */
 public abstract class Constraint {
	 protected String name;
	
	 /**
	 * Creates a new instance of Constraint
	 * @param aName The name of the constraint.
	 */
	 public Constraint(String aName) {
		 name = aName;		
	 }

	
	 /**
	 * Used to validate whether a trial is valid.
	 * @param trial The trial to be validated.
	 * @return A trial veto.
	 */
	 public abstract TrialVeto validate(Trial trial);
 }
