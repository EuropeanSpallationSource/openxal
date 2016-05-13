//
//  KnobControl.java
//  xal
//
//  Created by Thomas Pelaia on 12/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import xal.extension.widgets.swing.Wheelswitch;
import xal.tools.dispatch.DispatchQueue;


/**
 *  view for allowing users to use a knob 
 *  
 *  @author unknown
 *  @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class KnobControl extends Box implements KnobListener {
    
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    /** Default display format */
    private static final String DEFAULT_FORMAT = "+#.#####";

	/** queue on which to perform resync operations */
	final private DispatchQueue RESYNC_QUEUE;

	/** the knob to edit */
	final protected Knob KNOB;
	
	/** wheel for setting the current knob value */
	final protected Wheelswitch KNOB_WHEEL;

	/** handler of wheel events */
	final private WheelEventHandler WHEEL_EVENT_HANDLER;

	/** slider displaying the current setting as a fraction of the knob's range */
	final protected JSlider KNOB_SLIDER;
	
	/** indicator of whether the knob is ready */
	final protected JButton READY_INDICATOR;

	/** button to resync the knob settings with the live values */
	final private JButton LIVE_RESYNC_BUTTON;

	/** determine if the control is synching */
	volatile protected boolean _isSynching;

	/** indicates that a rescyn is needed */
	volatile private boolean _needsResync;

	/** indicates that a resync with limits update is needed */
	volatile private boolean _needsResyncWithLimitsUpdate;
	
	
	/** Constructor */
	public KnobControl( final Knob knob ) {
		super( BoxLayout.Y_AXIS );

		// guarantees that resync operations happen atomically
		RESYNC_QUEUE = DispatchQueue.createSerialQueue( "Knob Resync" );
		
		KNOB = knob;
		KNOB_SLIDER = new JSlider();		

		WHEEL_EVENT_HANDLER = new WheelEventHandler();
		KNOB_WHEEL = createKnobWheel();
		
		READY_INDICATOR = new JButton();
		READY_INDICATOR.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String inactiveExcuse = knob.getInactiveExcuse();
				JOptionPane.showMessageDialog( KNOB_WHEEL, inactiveExcuse, "Status Problems", JOptionPane.WARNING_MESSAGE );
			}
		});

		LIVE_RESYNC_BUTTON = new JButton( "Resync" );
		LIVE_RESYNC_BUTTON.setToolTipText( "Resync the knob values with the live machine values." );
		LIVE_RESYNC_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				knob.resync();
				resync( true );
				updateReadyStatus();
			}
		});
				
		buildView();
		
		knob.addKnobListener( this );
	}
	
	
	/** build view */
	protected void buildView() {
		add( KNOB_WHEEL );
		add( KNOB_SLIDER );
		
		KNOB_SLIDER.setEnabled( false );
		
		final Box buttonRow = new Box( BoxLayout.X_AXIS );
		add( buttonRow );
		buttonRow.add( READY_INDICATOR );
		buttonRow.add( LIVE_RESYNC_BUTTON );
		buttonRow.add( createZeroButton() );
		buttonRow.add( Box.createHorizontalGlue() );
		
		add( Box.createVerticalGlue() );
	}


	/** perform the action later on the swing thread to guarantee atomic behavior */
	static private void performOnSwingThread( final Runnable runnable ) {
		DispatchQueue.getMainQueue().dispatchAsync( runnable );
	}

	
	/** update connection status */
	protected void updateReadyStatus() {
		performOnSwingThread( new Runnable() {
			public void run() {
				if ( !KNOB.hasElements() ) {
					READY_INDICATOR.setText( "No Channels" );
					READY_INDICATOR.setEnabled( false );
					KNOB_WHEEL.setEnabled( false );
				}
				else if ( KNOB.isReady() ) {
					READY_INDICATOR.setText( "Ready" );
					READY_INDICATOR.setToolTipText( null );
					READY_INDICATOR.setEnabled( false );
					KNOB_WHEEL.setToolTipText( null );
					KNOB_WHEEL.setEnabled( true );
				}
				else {
					READY_INDICATOR.setText( "Show Problems..." );
					READY_INDICATOR.setForeground( Color.RED );
					READY_INDICATOR.setEnabled( true );
					KNOB_WHEEL.setEnabled( false );
				}
				READY_INDICATOR.repaint();
				READY_INDICATOR.validate();
			}
		} );
	}
	
	
	/** create the knob wheel */
	protected Wheelswitch createKnobWheel() {
		final Wheelswitch wheel = new Wheelswitch();
		wheel.setFormat( DEFAULT_FORMAT );
		wheel.setValue( KNOB.getCurrentSetting() );
		
		setWheelEventEnable( wheel, true );

		return wheel;
	}


	/** set whether to handle wheel events */
	private void setWheelEventEnable( final Wheelswitch wheel, final boolean handleEvents ) {
		if ( handleEvents ) {
			wheel.addPropertyChangeListener( "value", WHEEL_EVENT_HANDLER );
		}
		else {
			wheel.removePropertyChangeListener( "value", WHEEL_EVENT_HANDLER );
		}
	}

	

	/** handler of property change events for the wheel switch */
	private class WheelEventHandler implements PropertyChangeListener {
		public void propertyChange( final PropertyChangeEvent event ) {
			performOnSwingThread( new Runnable() {
				public void run() {
					if ( !_isSynching ) {
						final double value = KNOB_WHEEL.getValue();
						KNOB.setValue( value );
					}
				}
			});
		}
	}

	

	/** create the zero button */
	protected JButton createZeroButton() {
		final JButton zeroButton = new JButton( "zero" );
		zeroButton.setToolTipText( "Define the current setting as the \"zero\" setpoint." );
		zeroButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				KNOB.zero();
			}
		});
		
		return zeroButton;
	}
	
	
	/** resynchronize the control with the knob settings */
	protected void resync( final boolean forceLimitsUpdate ) {
		_needsResync = true;		// flag that a resync is needed
		_needsResyncWithLimitsUpdate |= forceLimitsUpdate;		// flag that a resync with limits update is needed
		queueResync();
	}


	/** queue the resync operation */
	private void queueResync() {
		// queue resync operations serially for atomic configuration of the knob
		RESYNC_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				if ( _needsResync || _needsResyncWithLimitsUpdate ) {		// only perform the resync if necessary minimizing multiple resyncs
					try {
						// gui operations must occur on main dispatch thread (blocking the resync queue so these operations are performed serially)
						DispatchQueue.getMainQueue().dispatchSync( new Runnable() {
							public void run() {
								_needsResync = false;		// mark basic resync as done so future requests force a fresh resync
								_isSynching = true;

								final double scale = 1.0e-6 * ( KNOB.getUpperLimit() - KNOB.getLowerLimit() );
								final double value = KNOB.getCurrentSetting();
								if ( scale > 0 && ( Math.abs( value - KNOB_WHEEL.getValue() ) > scale ) ) {
									KNOB_WHEEL.setValue( value );
								}

								if ( KNOB.isReady() && ( _needsResyncWithLimitsUpdate || KNOB.limitsNeedUpdate() ) ) {
									_needsResyncWithLimitsUpdate = false;			// mark resync with limits update as done so future requests force a fresh resync
									
									final double lowerLimit = KNOB.getLowerLimit();
									if ( lowerLimit != KNOB_WHEEL.getGraphMin() ) {
										KNOB_WHEEL.setGraphMin( lowerLimit );
									}

									final double upperLimit = KNOB.getUpperLimit();
									if ( upperLimit != KNOB_WHEEL.getGraphMax() ) {
										KNOB_WHEEL.setGraphMax( upperLimit );
									}

									final String wheelFormat = generateWheelFormat( lowerLimit, upperLimit );
									KNOB_WHEEL.setFormat( wheelFormat );
								}
							}
						});
					}
					finally {
						// Mark the synching as done on the next main queue operation since the resync will fire property change events that get
						// scheduled for later dispatch on the swing thread which happens after the resync but before the following operation will get executed.
						DispatchQueue.getMainQueue().dispatchAsync( new Runnable() {
							public void run() {
								_isSynching = false;
							}
						});
					}
				}
			}
		});
	}
	
	
	/**
	 * Generate the wheel format for the specified limits.
	 * 
	 * The knob format is generated with 5 significant digits in mind, but the actual display might show more,
	 * due to the fact that the one's place digit is always shown, e.g. if a limit size is 10E6, the returned format
	 * is 6 digits long.
	 * 
	 * The format is always limited to 8 digits.
	 * 
	 * @param lowerLimit lower limit value
	 * @param upperLimit upper limit value
	 * 
	 * @return generated number format
	 */
	static final String generateWheelFormat( final double lowerLimit, final double upperLimit ) {
		final int SIGNIFICANT_DIGITS = 5;
		final int MAX_DIGITS = 8;

		final double maxLimit = Math.max( Math.abs( lowerLimit ), Math.abs( upperLimit ) );
		final int nIntegralDigits = maxLimit > 0 ? (int)Math.ceil(Math.log10(maxLimit)) : -1 * MAX_DIGITS;
		final StringBuffer buffer = new StringBuffer( "+" );

		// Always use at least one integral part digit
		buffer.append( "#" );
		
		// Limit the display value to MAX_DIGITS digits (ones digit is already included)
		final int nIntegralDigitsLimited = nIntegralDigits > MAX_DIGITS-1 ? MAX_DIGITS-1 : nIntegralDigits;

		for ( int digit = 0 ; digit < nIntegralDigitsLimited ; digit++ ) {
			buffer.append( "#" );
		}
		
		final int nDecimalDigits = SIGNIFICANT_DIGITS - nIntegralDigitsLimited;
		
		if (nDecimalDigits > 0) {
		    // Show at most MAX_DIGITS-1 decimal places (a digit is already in ones slot)
		    final int nDecimalDigitsLimited =  nDecimalDigits > MAX_DIGITS-1 ? MAX_DIGITS-1 : nDecimalDigits;

		    buffer.append( "." );

		    for ( int digit = 0 ; digit < nDecimalDigitsLimited; digit++ ) {
		        buffer.append( "#" );
		    }
		}

   		return buffer.toString();
	}
	
	
	/** event indicating that the specified knob's name has changed */
	public void nameChanged( final Knob knob, final String newName ) {}
	
	
	/** ready state changed */
	public void readyStateChanged( final Knob knob, final boolean isReady ) {
		if ( isReady ) {
			resync( true );
		}
		updateReadyStatus();			
	}
	
	
	/** event indicating that the knob's limits have changed */
	public void limitsChanged( final Knob knob, final double lowerLimit, final double upperLimit ) {
		resync( true );
	}
	
	
	/** event indicating that the knob's current value setting has changed */
	public void currentSettingChanged( final Knob knob, final double value ) {
		resync( false );
		final double lowerLimit = knob.getLowerLimit();
		final double range = knob.getUpperLimit() - lowerLimit;
		if ( range > 0.0 ) {
			final int sliderPosition = (int) ( ( value - lowerLimit ) * 100.0 / range );
			KNOB_SLIDER.setValue( sliderPosition );
		}
		else {
			KNOB_SLIDER.setValue( 50 );
		}
	}
	
	
	/** event indicating that an element has been added */
	public void elementAdded( final Knob knob, final KnobElement element ) {
		resync( true );
	}
	
	
	/** event indicating that an element has been removed */
	public void elementRemoved( final Knob knob, final KnobElement element ) {
		resync( true );
	}
	
	
	/** event indicating that the specified knob element has been modified */
	public void elementModified( final Knob knob, final KnobElement element ) {
		resync( true );
	}
	
	
	/** event indicating that the knob's most previously pending set operation has completed */
	public void valueSettingPublished( final Knob knob ) {}
}
