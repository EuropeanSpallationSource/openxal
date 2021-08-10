//
//  DBConfiguration.java
//  xal
//
//  Created by Tom Pelaia on 7/23/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.database;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.DataAdaptor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.*;

/**
 * load a database configuration
 */
public class DBConfiguration {

    private static final Logger LOGGER = Logger.getLogger(DBConfiguration.class.getName());

    /**
     * key for getting the URL from the preferences
     */
    protected static final String PREFERENCES_URL_KEY = "configURL";

    private static final String ADAPTOR_ATTR = "adaptor";
    private static final String PASSWORD_ATTR = "password";
    private static final String SERVER_ATTR = "server";
    private static final String NAME_ATTR = "name";
    private static final String DEFAULT_ATTR = "default";

    /**
     * name of the default database adaptor
     */
    private final String defaultDatabaseAdaptorName;

    /**
     * name of the default server
     */
    private final String defaultServerName;

    /**
     * name of the default account
     */
    private final String defaultAccountName;

    /**
     * table mapping database adaptor names to database adaptor class names
     */
    private final Map<String, String> databaseAdaptorMap;

    /**
     * table of servers keyed by name
     */
    private final Map<String, DBServerConfig> servers;

    /**
     * table of accounts keyed by name
     */
    private final Map<String, DBAccountConfig> accounts;

    /**
     * table of schema urls keyed by name
     */
    private final Map<String, URL> schemaUrls;

    /**
     * Primary Constructor
     */
    private DBConfiguration(final String defaultDBAdaptorName, final Map<String, String> dbAdaptorMap, final String defaultServerName, final Map<String, DBServerConfig> servers, final String defaultAccountName, final Map<String, DBAccountConfig> accounts, final Map<String, URL> schemaUrls) {
        defaultDatabaseAdaptorName = defaultDBAdaptorName;
        this.defaultServerName = defaultServerName;
        this.defaultAccountName = defaultAccountName;

        databaseAdaptorMap = dbAdaptorMap;
        this.servers = servers;
        this.accounts = accounts;
        this.schemaUrls = schemaUrls;
    }

    /**
     * get the default server name
     */
    public String getDefaultServerName() {
        return defaultServerName;
    }

    /**
     * get the default account name
     */
    public String getDefaultAccountName() {
        return defaultAccountName;
    }

    /**
     * determine whether this configuration has the named account
     */
    public boolean hasAccount(final String accountName) {
        return accounts.containsKey(accountName);
    }

    /**
     * determine whether this configuration has the named server
     */
    public boolean hasServer(final String serverName) {
        return servers.containsKey(serverName);
    }

    /**
     * get an alpha-numerically ordered list of account names
     */
    public List<String> getAccountNames() {
        final Set<String> nameSet = accounts.keySet();
        final List<String> names = new ArrayList<>(nameSet);
        Collections.sort(names);
        return names;
    }

    /**
     * get an alpha-numerically ordered list of server names
     */
    public List<String> getServerNames() {
        final Set<String> nameSet = servers.keySet();
        final List<String> names = new ArrayList<>(nameSet);
        Collections.sort(names);
        return names;
    }

    /**
     * Generate a new connection dictionary for the specified account name and
     * server configuration name
     *
     * @param accountName name of the account for which to initialize the
     * connection dictionary (or null to use the default account if any)
     * @param serverName name of the database server for which to initialize the
     * connection dictionary (or null to use the default server if any)
     * @return a new connection dictionary from the database configuration
     */
    public ConnectionDictionary newConnectionDictionary(final String accountName, final String serverName) {
        final DBAccountConfig account = accountName != null && accountName.length() > 0 ? accounts.get(accountName) : accounts.get(defaultAccountName);

        // get the default server name for the account if any
        final String accountServerName = account != null ? account.getDefaultServerName() : null;

        // resolve the server name to use by first selecting the serverName parameter if not null, then falling back to the account's default server name if not null and finally falling back to the DEFAULT_SERVER_NAME
        final String resolvedServerName = serverName != null && serverName.length() > 0 ? serverName : accountServerName != null && accountServerName.length() > 0 ? accountServerName : defaultServerName;

        // get the resolved server configuration
        final DBServerConfig serverConfig = servers.get(resolvedServerName);

        final ConnectionDictionary connectionDictionary = new ConnectionDictionary();
        if (serverConfig != null) {
            final String urlSpec = serverConfig.getURLSpec();
            if (urlSpec != null) {
                connectionDictionary.setURLSpec(urlSpec);
            }
            final String adaptorClassSpec = serverConfig.getAdaptorClassSpec();
            if (adaptorClassSpec != null) {
                connectionDictionary.setDatabaseAdaptorClass(adaptorClassSpec);
            }
        }

        if (account != null) {
            final String user = account.getUserName();
            if (user != null) {
                connectionDictionary.setUser(user);
            }
            final String password = account.getPassword();
            if (password != null) {
                connectionDictionary.setPassword(account.getPassword());
            }
        }

        return connectionDictionary;
    }

    /**
     * generate a new connection dictionary for the specified account name and
     * the default database server
     */
    public ConnectionDictionary newConnectionDictionary(final String accountName) {
        return newConnectionDictionary(accountName, null);
    }

    /**
     * Get the available connection dictionary which is the most preferred
     *
     * @param useDefaultIfNeeded use the default account if none of the listed
     * accounts is available
     * @param accounts ordered (most preferred is first) accounts to search
     * among
     */
    public ConnectionDictionary availableConnectionDictionary(final boolean useDefaultIfNeeded, final String... accounts) {
        for (final String account : accounts) {
            if (hasAccount(account)) {
                return newConnectionDictionary(account);
            }
        }
        return useDefaultIfNeeded ? defaultConnectionDictionary() : null;
    }

    /**
     * Get the available connection dictionary which is the most preferred and
     * use the default one if there are no matches
     *
     * @param accounts ordered (most preferred is first) accounts to search
     * among
     */
    public ConnectionDictionary availableConnectionDictionary(final String... accounts) {
        return availableConnectionDictionary(true, accounts);
    }

    /**
     * generate a new connection dictionary from the default database server
     * configuration and default account
     */
    public ConnectionDictionary defaultConnectionDictionary() {
        return newConnectionDictionary(defaultAccountName, defaultServerName);
    }

    /**
     * load the configuration from the default configuration URL
     */
    public static DBConfiguration getInstance() {
        try {
            return hasDefaultConfiguration() ? getInstance(getDefaultURL()) : null;
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Malformed URL specification", exception);
        }
    }

    /**
     * load a configuration from the specified URL
     */
    public static DBConfiguration getInstance(final URL configURL) {
        final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl(configURL, false);
        return getInstance(documentAdaptor, configURL);
    }

    /**
     * load a configuration from the specified configuration document adaptor
     */
    public static DBConfiguration getInstance(final DataAdaptor documentAdaptor, URL baseURL) {
        final DataAdaptor configAdaptor = documentAdaptor.childAdaptor("dbconfig");

        final DataAdaptor dbAdaptorGroup = configAdaptor.childAdaptor("adaptors");
        final String defaultDBAdaptorName = dbAdaptorGroup.hasAttribute(DEFAULT_ATTR) ? dbAdaptorGroup.stringValue(DEFAULT_ATTR) : null;
        final List<DataAdaptor> dbAdaptors = dbAdaptorGroup.childAdaptors(ADAPTOR_ATTR);
        final Map<String, String> dbAdaptorTable = new HashMap<>();
        for (final DataAdaptor dbAdaptor : dbAdaptors) {
            final String name = dbAdaptor.stringValue(NAME_ATTR);
            final String className = dbAdaptor.stringValue("class");
            dbAdaptorTable.put(name, className);
        }

        final DataAdaptor serverGroup = configAdaptor.childAdaptor("servers");
        final String defaultServerName = serverGroup.hasAttribute(DEFAULT_ATTR) ? serverGroup.stringValue(DEFAULT_ATTR) : null;
        final List<DataAdaptor> serverAdaptors = serverGroup.childAdaptors(SERVER_ATTR);
        final Map<String, DBServerConfig> serverTable = new HashMap<>();
        for (final DataAdaptor serverAdaptor : serverAdaptors) {
            final String name = serverAdaptor.stringValue(NAME_ATTR);
            final String url = serverAdaptor.stringValue("url");
            final String dbAdaptorName = serverAdaptor.hasAttribute(ADAPTOR_ATTR) ? serverAdaptor.stringValue(ADAPTOR_ATTR) : defaultDBAdaptorName;
            final String dbAdaptorClassName = dbAdaptorTable.get(dbAdaptorName);
            final DBServerConfig serverConfig = new DBServerConfig(name, url, dbAdaptorClassName);
            serverTable.put(name, serverConfig);
        }

        final DataAdaptor accountGroup = configAdaptor.childAdaptor("accounts");
        final String defaultAccountName = accountGroup.hasAttribute(DEFAULT_ATTR) ? accountGroup.stringValue(DEFAULT_ATTR) : null;
        final List<DataAdaptor> accountAdaptors = accountGroup.childAdaptors("account");
        final Map<String, DBAccountConfig> accountTable = new HashMap<>();
        for (final DataAdaptor accountAdaptor : accountAdaptors) {
            final String name = accountAdaptor.stringValue(NAME_ATTR);
            final String user = accountAdaptor.stringValue("user");
            final String password = accountAdaptor.hasAttribute(PASSWORD_ATTR) ? accountAdaptor.stringValue(PASSWORD_ATTR) : null;
            final String serverName = accountAdaptor.hasAttribute(SERVER_ATTR) ? accountAdaptor.stringValue(SERVER_ATTR) : null;
            accountTable.put(name, new DBAccountConfig(name, user, password, serverName));
        }

        final DataAdaptor schemasGroup = configAdaptor.childAdaptor("schemas");
        final List<DataAdaptor> schemaAdaptors = schemasGroup.childAdaptors("schema");
        final Map<String, URL> schemaUrls = new HashMap<>();
        for (final DataAdaptor schemaAdaptor : schemaAdaptors) {
            final String name = schemaAdaptor.stringValue(NAME_ATTR);
            try {
                URL url = new URL(baseURL, schemaAdaptor.stringValue("url"));
                schemaUrls.put(name, url);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }

        return new DBConfiguration(defaultDBAdaptorName, dbAdaptorTable, defaultServerName, serverTable, defaultAccountName, accountTable, schemaUrls);
    }

    /**
     * determine whether a default configuration has been specified
     */
    public static boolean hasDefaultConfiguration() {
        String urlSpec = null;
        try {
            urlSpec = getDefaultURLSpec();
            return urlSpec != null && !urlSpec.isEmpty() && new File(new URL(urlSpec).toURI()).exists();
        } catch (MalformedURLException | URISyntaxException exception) {
            LOGGER.log(Level.INFO, "Database configuration: " + urlSpec, exception);
            return false;
        }
    }

    /**
     * Get the user preferences for this class
     *
     * @return the user preferences for this class
     */
    protected static Preferences getDefaults() {
        return xal.tools.apputils.Preferences.nodeForPackage(DBConfiguration.class);
    }

    /**
     * Get the URL Spec of the default connection dictionary's properties file
     *
     * @return the URL Spec of the configuration
     */
    public static String getDefaultURLSpec() {
        return getDefaults().get(PREFERENCES_URL_KEY, "");
    }

    /**
     * Set the URL spec of the default configuration.
     *
     * @param urlSpec URL spec of the configuration
     * @throws java.util.prefs.BackingStoreException if the url spec failed to
     * be saved as a default
     */
    public static void setDefaultURLSpec(final String urlSpec) throws BackingStoreException {
        Preferences preferences = getDefaults();
        preferences.put(PREFERENCES_URL_KEY, urlSpec);
        preferences.flush();
    }

    /**
     * Get the URL of the default configuration
     *
     * @return the URL of the default configuration
     * @throws java.net.MalformedURLException if the default URL spec cannot
     * form a valid URL
     */
    public static URL getDefaultURL() throws MalformedURLException {
        if (hasDefaultConfiguration()) {
            return new URL(getDefaultURLSpec());
        } else {
            return null;
        }
    }

    /**
     * Set the URL of the default configuration.
     *
     * @param url URL of the configuration.
     * @throws java.util.prefs.BackingStoreException if the url failed to be
     * saved as a default
     */
    public static void setDefaultURL(final URL url) throws BackingStoreException {
        setDefaultURLSpec(url.toString());
    }

    /**
     * Get the default database adaptor
     *
     * @return the database adaptor
     */
    public DatabaseAdaptor getDefaultDatabaseAdaptor() {
        if (databaseAdaptorMap == null) {
            return null;
        }

        final String className = databaseAdaptorMap.get(defaultDatabaseAdaptorName);
        if (className == null || className.equals("")) {
            return null;
        }
        try {
            final Class<?> databaseAdaptorClass = Class.forName(className);
            return (DatabaseAdaptor) databaseAdaptorClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException exception) {
            final String message = "Failed to instantiate database adaptor for class:  " + className;
            throw new RuntimeException(message, exception);
        }
    }

    /**
     * Returns url location of a file with database schema description
     *
     * @param name schema
     * @return url pointing to a file
     */
    public URL getSchemaURL(String name) {
        return schemaUrls.get(name);
    }
}

/**
 * holds a database server configuration
 */
class DBServerConfig {

    /**
     * local name for the server (not an official name)
     */
    private final String name;

    /**
     * URL specification
     */
    private final String urlSpec;

    /**
     * string representation for the adaptor class
     */
    private final String adaptorClassSpec;

    /**
     * Constructor
     */
    public DBServerConfig(final String name, final String url, final String adaptorClass) {
        this.name = name;
        urlSpec = url;
        adaptorClassSpec = adaptorClass;
    }

    /**
     * get the local server name
     */
    public String getName() {
        return name;
    }

    /**
     * get the URL spec
     */
    public String getURLSpec() {
        return urlSpec;
    }

    /**
     * get the string representation for the database adaptor class
     */
    public String getAdaptorClassSpec() {
        return adaptorClassSpec;
    }
}

/**
 * holds a database account configuration
 */
class DBAccountConfig {

    /**
     * local account name (not an official name)
     */
    private final String name;

    /**
     * user name
     */
    private final String userName;

    /**
     * user's password
     */
    private final String password;

    /**
     * name of the default server for the specified account if any
     */
    private final String defaultServerName;

    /**
     * Constructor
     */
    public DBAccountConfig(final String name, final String user, final String password, final String defaultServerName) {
        this.name = name;
        userName = user;
        this.password = password;
        this.defaultServerName = defaultServerName;
    }

    /**
     * get the account name
     */
    public String getName() {
        return name;
    }

    /**
     * get the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * get the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * get the account's default server name
     */
    public String getDefaultServerName() {
        return defaultServerName;
    }
}
