/*
 * SimpleBeanEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Tom Pelaia
 * @author Patrick Scruggs
 */

package xal.extension.widgets.beaneditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.table.*;

import java.util.*;

import xal.extension.widgets.swing.*;
import xal.tools.data.*;


/** SimpleBeanEditor */
public class SimpleBeanEditor<T> extends JDialog {
    /** Private serializable version ID */
    private static final long serialVersionUID = 1L;

    /** Table model of property records */
    final private KeyValueFilteredTableModel<PropertyRecord> PROPERTY_TABLE_MODEL;

    /** List of properties that appear in the properties table */
    final private List<PropertyRecord> BEAN_PROPERTY_RECORDS;

    /** Bean that is being edited */
    final private T BEAN;

	/** model column for the value in the property table */
	final private int PROPERTY_TABLE_VALUE_COLUMN;

	 public SimpleBeanEditor( final Frame owner, final String dialogTitle, final String beanName, final T bean ) {
		 this(owner, dialogTitle, beanName, bean, true, true);
	 }
	 
    /* Constructor that takes a window parent
     * and a bean to fetch properties from
     */
    public SimpleBeanEditor( final Frame owner, final String dialogTitle, final String beanName, final T bean, boolean bottomButtons, boolean visible ) {
        super( owner, dialogTitle, true );	//Set JDialog's owner, title, and modality
        
        BEAN = bean;					// Set the bean to edit

		// generate the bean property tree
		final EditablePropertyContainer probePropertyTree = EditableProperty.getInstanceWithRoot( beanName, bean );
		//System.out.println( probePropertyTree );

		BEAN_PROPERTY_RECORDS = PropertyRecord.toRecords( probePropertyTree );

		PROPERTY_TABLE_MODEL = new KeyValueFilteredTableModel<>( BEAN_PROPERTY_RECORDS, "displayLabel", "value", "units" );
		PROPERTY_TABLE_MODEL.setMatchingKeyPaths( "path" );					// match on the path
		PROPERTY_TABLE_MODEL.setColumnName( "displayLabel", "Property" );
		PROPERTY_TABLE_MODEL.setColumnEditKeyPath( "value", "editable" );	// the value is editable if the record is editable
		PROPERTY_TABLE_VALUE_COLUMN = PROPERTY_TABLE_MODEL.getColumnForKeyPath( "value" );	// store the column for the "value" key path

        setSize( 600, 600 );			// Set the window size
        initializeComponents(bottomButtons);			// Set up each component in the editor
        setLocationRelativeTo( owner );	// Center the editor in relation to the frame that constructed the editor
        setVisible(visible);				// Make the window visible
    }
	
    
    /** 
	 * Get the probe to edit
     * @return probe associated with this editor
     */
    public T getBean() {
        return BEAN;
    }


	/** publish record values to the bean */
	private void publishToBean() {
		for ( final PropertyRecord record : BEAN_PROPERTY_RECORDS ) {
			record.publishIfNeeded();
		}
		PROPERTY_TABLE_MODEL.fireTableDataChanged();
	}


	/** revert the record values from the bean */
	private void revertFromBean() {
		for ( final PropertyRecord record : BEAN_PROPERTY_RECORDS ) {
			record.revertIfNeeded();
		}
		PROPERTY_TABLE_MODEL.fireTableDataChanged();
	}

    
    /** Initialize the components of the bean editor */
    protected void initializeComponents(boolean bottomButtons) {
        //main view containing all components
        final Box mainContainer = new Box( BoxLayout.Y_AXIS );

        //Table containing the properties that can be modified
        final JTable propertyTable = new JTable() {
            /** Serializable version ID */
            private static final long serialVersionUID = 1L;

			/** renderer for a table section */
			private final TableCellRenderer SECTION_RENDERER = makeSectionRenderer();
            
            //Get the cell editor for the table
            @Override
            public TableCellEditor getCellEditor( final int row, final int col ) {
                //Value at [row, col] of the table
                final Object value = getValueAt( row, col );

				if ( value == null ) {
					return super.getCellEditor( row, col );
				} else if (value instanceof Enum) {
					return new DefaultCellEditor(new JComboBox<Object>(((Enum<?>)value).getDeclaringClass().getEnumConstants())
						{
							private static final long serialVersionUID = 1L;

							{
								setForeground(getSelectionForeground());
								setBackground(getSelectionBackground());
							}
						}); 
				} else {
                    return getDefaultEditor( value.getClass() );
				}
            }
            
            //Get the cell renderer for the table to change how values are displayed
            @Override
            public TableCellRenderer getCellRenderer( final int row, final int column ) {
				// index of the record in the model
				final int recordIndex = this.convertRowIndexToModel( row );
				final PropertyRecord record = PROPERTY_TABLE_MODEL.getRecordAtRow( recordIndex );
				final Object value = getValueAt( row, column );

                //Set the renderer according to the property type (e.g. Boolean => checkbox display, numeric => right justified)
				if ( !record.isEditable() ) {
                    return SECTION_RENDERER;
				}
				else if ( value == null ) {
                    return super.getCellRenderer( row, column );
				}
				else if ( value instanceof Enum) {
					return new TableCellRenderer() {
						JComboBox<Object> combo = new JComboBox<Object>(((Enum<?>)value).getDeclaringClass().getEnumConstants());

						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {

							if (isSelected) {
								combo.setForeground(table.getSelectionForeground());
								combo.setBackground(table.getSelectionBackground());
							} else {
								combo.setForeground(table.getForeground());
								combo.setBackground(table.getBackground());
							}
							combo.setSelectedItem(value);
							return combo;
						}
					};
				}
				else {
					final TableCellRenderer renderer = getDefaultRenderer( value.getClass() );
					if ( renderer instanceof DefaultTableCellRenderer ) {
						final DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer)renderer;
						final int modelColumn = convertColumnIndexToModel( column );
						// highlight the cell if the column corresponds to the value and it has unpublished changes
						defaultRenderer.setForeground( modelColumn == PROPERTY_TABLE_VALUE_COLUMN && record.hasChanges() ? Color.BLUE : Color.BLACK );
					}
					return renderer;
				}
            }


			private TableCellRenderer makeSectionRenderer() {
				final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
				renderer.setBackground( Color.GRAY );
				renderer.setForeground( Color.WHITE );
				return renderer;
			}
        };
        
        //Set the table to allow one-click edit
        ((DefaultCellEditor) propertyTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
        propertyTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			{
        		setHorizontalAlignment(JLabel.RIGHT);
        	}
        	
            public void setValue(Object value) {                
                setText((value == null) ? "" : String.format(Locale.ROOT, "%10.7g", value));
            }
        });
        
        //Resize the last column
        propertyTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN);
        //Allow single selection only
		propertyTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        //Set the model to the table
        propertyTable.setModel( PROPERTY_TABLE_MODEL );

        //Configure the text field to filter the table
        final JTextField filterTextField = new JTextField();
		filterTextField.setMaximumSize( new Dimension( 32000, filterTextField.getPreferredSize().height ) );
        filterTextField.putClientProperty( "JTextField.variant", "search" );
        filterTextField.putClientProperty( "JTextField.Search.Prompt", "Property Filter" );
		PROPERTY_TABLE_MODEL.setInputFilterComponent( filterTextField );
        mainContainer.add( filterTextField, BorderLayout.NORTH );

        //Add the scrollpane to the table with a vertical scrollbar
        final JScrollPane scrollPane = new JScrollPane( propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        mainContainer.add( scrollPane );

        
        //Add everything to the dialog
        add( mainContainer );
        
        if (bottomButtons) {
        	mainContainer.add( initializeControlPanel() );
        }
    }
    
    
    protected Box initializeControlPanel()
    {

        // button to revert changes back to last saved state
        final JButton revertButton = new JButton( "Revert" );
		revertButton.setToolTipText( "Revert values back." );
        revertButton.setEnabled( false );

        // button to publish changes
        final JButton publishButton = new JButton( "Publish" );
		publishButton.setToolTipText( "Publish values." );
        publishButton.setEnabled( false );

        // button to publish changes and dismiss the panel
        final JButton okayButton = new JButton( "Okay" );
		okayButton.setToolTipText( "Publish values and dismiss the dialog." );
        okayButton.setEnabled( true );

        //Add the action listener as the ApplyButtonListener
        revertButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				revertFromBean();
				revertButton.setEnabled( false );
				publishButton.setEnabled( false );
			}
		});

        //Add the action listener as the ApplyButtonListener
        publishButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				publishToBean();
				revertButton.setEnabled( false );
				publishButton.setEnabled( false );
			}
		});

        //Add the action listener as the ApplyButtonListener
        okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					publishToBean();
					dispose();
				}
				catch( Exception exception ) {
					JOptionPane.showMessageDialog( SimpleBeanEditor.this, exception.getMessage(), "Error Publishing", JOptionPane.ERROR_MESSAGE );
					System.err.println( "Exception publishing values: " + exception );
				}
			}
		});
        

		PROPERTY_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<PropertyRecord>,PropertyRecord>() {
			public void recordModified( final KeyValueTableModel<PropertyRecord> source, final PropertyRecord record, final String keyPath, final Object value ) {
				revertButton.setEnabled( true );
				publishButton.setEnabled( true );
			}
		});

		//Add the buttons to the bottom of the dialog
        final Box controlPanel = new Box( BoxLayout.X_AXIS );
		controlPanel.add( revertButton );
		controlPanel.add( Box.createHorizontalGlue() );
        controlPanel.add( publishButton );
        controlPanel.add( okayButton );
        
        return controlPanel;
    }
}
