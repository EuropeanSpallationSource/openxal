package edu.stanford.lcls.modelmanager.util;

import java.io.File;
import java.util.Collection;

import xal.tools.data.EditContext;
import xal.tools.data.DataTable;
import xal.tools.xml.XmlTableIO;

/**
 * Loader of missing parameter tables. Default tables are taken 
 * from openxal jels extension.
 *
 * @version 0.1 3 Sep 2015
 * @author Blaz Kranjc
 */
public class DefaultTableLoader {
	private final Collection<String> requiredTables;
	static public final String PARAMS_FILEPATH = "edu/stanford/lcls/modelmanager/util/model.params";
	private final EditContext defaultContext;

	/**
	 * Class Constructor
	 */
	public DefaultTableLoader() {
		defaultContext = new EditContext();
		File paramsFile = new File(getClass().getClassLoader().getResource(PARAMS_FILEPATH).getFile());
		XmlTableIO.readTableGroupFromFile(defaultContext, "params", paramsFile);
		requiredTables = defaultContext.getTableNames();
	}

	/**
	 * Loads missing tables from default selection into provided context.
	 * 
	 * @param context Context to which tables are loaded.
	 */
	public void loadDefaultTables(EditContext context) {
		for (String tableId : requiredTables) {
			if (! (context.getTable(tableId) == null)) {
				continue;
			}
			context.addTableToGroup(defaultContext.getTable(tableId), "params");
		}
	}
}


