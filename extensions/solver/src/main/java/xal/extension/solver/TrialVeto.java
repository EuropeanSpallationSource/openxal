/*
 * TrialVeto.java
 *
 * Created Wednesday June 9, 2004 2:39 pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver;
  
 import xal.extension.solver.constraint.Constraint;
 
 

 /**
 * Trial veto turns down a trial based on a particular trial point.
 * @author ky6  
 * @author t6p
 */
 public final class TrialVeto {
	 protected final String reason;
	 protected final Object userInfo;
	 protected final Trial trial;
	 protected final Constraint constraint;
	 
	 
	 /**
	  * Primary Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  * @param reason describing why the veto was made
	  * @param userInfo supplying additional user information to associate with the veto
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint, final String reason, final Object userInfo ) {
		 this.trial = trial;
		 this.constraint = constraint;
		 this.reason = reason;
		 this.userInfo = userInfo;
	 }
	 
	 
	 /**
	  * Primary Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  * @param reason describing why the veto was made
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint, final String reason ) {
		 this( trial, constraint, reason, null );
	 }
	 
	
	 /**
	  * Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint ) {
		 this( trial, constraint, "" );
	 }
	 
	 
	 /**
	 * Get the trial point to be tested.
	 * @return trialPoint.
	 */
	 public TrialPoint getTrialPoint() {
		 return trial.getTrialPoint();
	 }
	 
	 
	 /**
	 * Get the trial.
	 * @return trial.
	 */
	 public Trial getTrial() {
		 return trial;
	 }
	 
	 
	 /**
	 * Get the constraint.
	 * @return constraint.
	 */
	 public Constraint getConstraint() {
		 return constraint;
	 }
	 
	 
	 /**
	  * Get the reason for the veto
	  * @return the reason for the veto
	  */
	 public String getReason() {
		 return reason;
	 }
	 
	 
	 /**
	  * Get the user info
	  * @return the user info
	  */
	 public Object getUserInfo() {
		 return userInfo;
	 }
 }
