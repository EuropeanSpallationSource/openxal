package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.sns.tools.data.*;


/** database table configuration */
class DBTableConfiguration {
	/** database table name */
	final protected String TABLE_NAME;
	final protected String SCHEMA_NAME;
	
	/** map of table column names keyed by attributes */
	final protected Map<String,String> COLUMN_ATTRIBUTE_MAP;
	
	/** map of SQL queries keyed by name */
	final protected Map<String,String> QUERY_MAP;
	
	/** map of database data types keyed by name */
	final protected Map<String,String> DATA_TYPE_MAP;
	
	
	/** Constructor */
	protected DBTableConfiguration( final DataAdaptor tableAdaptor ) {
		SCHEMA_NAME = tableAdaptor.stringValue( "schema" );
		TABLE_NAME = tableAdaptor.stringValue( "table" );
		COLUMN_ATTRIBUTE_MAP = getColumnAttributeMap( tableAdaptor );
		QUERY_MAP = getQueryMap( tableAdaptor );
		DATA_TYPE_MAP = getDataTypes( tableAdaptor );
	}
	
	
	/** get a new instance of the database table configuraton */
	static public DBTableConfiguration getInstance( final DataAdaptor adaptor ) {
		return new DBTableConfiguration( adaptor );
	}
	
	/**
	 * Get the schema name
	 * @return the database schema name
	 */
	public String getSchemaName() {
		return SCHEMA_NAME;
	}
	
	/**
	 * Get the table name
	 * @return the database table name
	 */
	public String getTableName() {
		return TABLE_NAME;
	}
	
	
	/**
	 * Get the column corresponding to the specified attribute
	 * @param attribute the attribute fow which to fetch the column
	 * @return column corresponding to the specified attribute
	 */
	public String getColumn( final String attribute ) {
		return COLUMN_ATTRIBUTE_MAP.get( attribute );
	}
	
	
	/**
	 * Get the SQL for the specified query
	 * @param queryName the name of the query to get
	 * @return the SQL for the specifie query
	 */
	public String getQuerySQL( final String queryName ) {
		return QUERY_MAP.get( queryName );
	}
	
	
	/**
	 * Get the data type for the specified name
	 * @param name name of the data type
	 * @return database data type
	 */
	public String getDataType( final String name ) {
		return DATA_TYPE_MAP.get( name );
	}
	
	
	/**
	 * Get the map of columns keyed by attributes.
	 * @param tableAdaptor the adaptor for the database table
	 * @return map of columns keyed by attributes
	 */
	static protected Map<String,String> getColumnAttributeMap( final DataAdaptor tableAdaptor ) {
		final List<DataAdaptor> columnAdaptors = tableAdaptor.childAdaptors( "column" );
		final Map<String,String> map = new HashMap<String,String>( columnAdaptors.size() );
		
		for ( final DataAdaptor columnAdaptor : columnAdaptors ) {
			map.put( columnAdaptor.stringValue( "attribute" ), columnAdaptor.stringValue( "column" ) );
		}
		
		return map;
	}
	
	
	/**
	 * Get the map of SQL queries keyed by name
	 * @param tableAdaptor the adaptor for the database table
	 * @return map of SQL keyed by query name
	 */
	static protected Map<String,String> getQueryMap( final DataAdaptor tableAdaptor ) {
		final List<DataAdaptor> queryAdaptors = tableAdaptor.childAdaptors( "query" );
		final Map<String,String> map = new HashMap<String,String>( queryAdaptors.size() );
		
		for ( final DataAdaptor queryAdaptor : queryAdaptors ) {
			map.put( queryAdaptor.stringValue( "name" ), queryAdaptor.stringValue( "sql" ) );
		}
		return map;
	}
	
	
	/**
	 * Get the map of database data types keyed by name
	 * @param tableAdaptor the adaptor for the database table
	 * @return map of database data types keyed by name
	 */
	static protected Map<String,String> getDataTypes( final DataAdaptor tableAdaptor ) {
		final List<DataAdaptor> typeAdaptors = tableAdaptor.childAdaptors( "datatype" );
		final Map<String,String> map = new HashMap<String,String>( typeAdaptors.size() );
		
		for ( final DataAdaptor typeAdaptor : typeAdaptors ) {
			map.put( typeAdaptor.stringValue( "name" ), typeAdaptor.stringValue( "type" ) );
		}
		
		return map;
	}
}

