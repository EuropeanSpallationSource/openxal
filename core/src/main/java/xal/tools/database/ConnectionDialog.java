/*
 *  ConnectionDialog.java
 *
 *  Created on Fri Feb 20 15:15:21 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.database;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.sql.Connection;

/**
 * ConnectionDialog displays a dialog allowing the user to supply the database
 * URL, their user ID and their password. A connection dictionary is returned to
 * the user based on their input.
 *
 * @author tap
 */
public class ConnectionDialog extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(ConnectionDialog.class.getName());

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * label for the submit button
     */
    private final String submitLabel;

    /**
     * the connection dictionary selected by the user
     */
    private ConnectionDictionary dictionary;

    /**
     * database configuration
     */
    private DBConfiguration configuration;

    /**
     * file chooser for browsing to a connection dictionary
     */
    private JFileChooser dictionaryBrowser;

    /**
     * box for the server menu
     */
    private Box serverOptionBox;

    /**
     * box containing a form of custom server fields
     */
    private Box serverCustomForm;

    /**
     * menu of available servers
     */
    private JComboBox<String> serverMenu;

    /**
     * field for entering the adaptor specification
     */
    private JTextField adaptorField;

    /**
     * field for entering the database URL
     */
    private JTextField urlField;

    /**
     * field for entering the user's ID
     */
    private JTextField userField;

    /**
     * field for entering the user's Password
     */
    private JPasswordField passwordField;

    /**
     * Primary Constructor
     *
     * @param owner The frame which owns this dialog window.
     * @param dictionary The initial connection dictionary.
     * @param submitLabel The label to use for the submit button.
     */
    protected ConnectionDialog(Frame owner, final ConnectionDictionary dictionary, final String submitLabel) {
        super(owner, "Connection Dialog", true);

        this.submitLabel = submitLabel;
        setup(dictionary);
    }

    /**
     * Constructor with a default submit button label of "Connect".
     *
     * @param owner The frame which owns this dialog window.
     * @param dictionary The initial connection dictionary.
     */
    protected ConnectionDialog(Frame owner, final ConnectionDictionary dictionary) {
        this(owner, dictionary, "Submit");
    }

    /**
     * Constructor with the default submit button label and an empty connection
     * dictionary.
     *
     * @param owner The frame which owns this dialog window.
     */
    protected ConnectionDialog(Frame owner) {
        this(owner, new ConnectionDictionary());
    }

    /**
     * Primary Constructor
     *
     * @param owner The dialog which owns this dialog window.
     * @param dictionary The initial connection dictionary.
     * @param submitLabel The label to use for the submit button.
     */
    protected ConnectionDialog(Dialog owner, final ConnectionDictionary dictionary, final String submitLabel) {
        super(owner, "Connection Dialog", true);

        this.submitLabel = submitLabel;
        setup(dictionary);
    }

    /**
     * Constructor with a default submit button label of "Connect".
     *
     * @param owner The dialog which owns this dialog window.
     * @param dictionary The initial connection dictionary.
     */
    protected ConnectionDialog(Dialog owner, final ConnectionDictionary dictionary) {
        this(owner, dictionary, "Submit");
    }

    /**
     * Constructor with the default submit button label and an empty connection
     * dictionary.
     *
     * @param owner The dialog which owns this dialog window.
     */
    protected ConnectionDialog(Dialog owner) {
        this(owner, new ConnectionDictionary());
    }

    /**
     * Common initialization.
     *
     * @param dictionary The initial connection dictionary.
     */
    protected void setup(final ConnectionDictionary dictionary) {
        this.dictionary = null;

        final ConnectionDictionary baseDictionary = (dictionary != null) ? dictionary : new ConnectionDictionary();

        makeContent();
        loadDictionary(baseDictionary);
    }

    /**
     * Load the specified connection dictionary.
     *
     * @param dictionary the connection dictionary to load
     */
    public void loadDictionary(final ConnectionDictionary dictionary) {
        String adaptorClass = null;
        try {
            final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
            adaptorClass = (adaptor != null) ? adaptor.getClass().getName() : null;
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error constructing dialog contents.", exception);
        }

        adaptorField.setText(adaptorClass);
        urlField.setText(dictionary.getURLSpec());
        userField.setText(dictionary.getUser());
        passwordField.setText(dictionary.getPassword());

        loadDefaultConfiguration();
    }

    /**
     * load the default configuration
     */
    private void loadDefaultConfiguration() {
        final DBConfiguration configuration = DBConfiguration.getInstance();
        this.configuration = configuration;
        String selectedServerItem = null;
        if (configuration != null) {
            final List<String> servers = new Vector<>(configuration.getServerNames());
            servers.add(0, "Custom");
            serverMenu.removeAllItems();
            for (final String server : servers) {
                serverMenu.addItem(server);
                final ConnectionDictionary serverDictionary = configuration.newConnectionDictionary(null, server);
                if (serverDictionary != null) {
                    final String urlSpec = serverDictionary.getURLSpec();
                    if (urlSpec != null && urlSpec.equals(urlField.getText())) {
                        selectedServerItem = server;
                    }
                }
            }
            if (selectedServerItem != null) {
                serverMenu.setSelectedItem(selectedServerItem);
            }

            // display the server options if there are options available other than the trivial custom option
            if (servers.size() > 1) {
                setDisplayServerOptions(true);
                setDisplayServerCustomForm(selectedServerItem == null);
            } else {
                setDisplayServerOptions(false);
                setDisplayServerCustomForm(true);
            }
        } else {
            serverOptionBox.setVisible(false);
        }
    }

    /**
     * Set whether to display the server options
     *
     * @param shouldDisplay true to display server options and false to display
     * the custom options instead
     */
    private void setDisplayServerOptions(final boolean shouldDisplay) {
        serverOptionBox.setVisible(shouldDisplay);
        pack();
    }

    /**
     * Set whether to display the custom server form
     *
     * @param shouldDisplay true to display server options and false to display
     * the custom options instead
     */
    private void setDisplayServerCustomForm(final boolean shouldDisplay) {
        serverCustomForm.setVisible(shouldDisplay);
        pack();
    }

    /**
     * Get the connection user's dictionary.
     *
     * @return the user's connection dictionary
     */
    public ConnectionDictionary getConnectionDictionary() {
        return dictionary;
    }

    /**
     * Show the connection dialog
     *
     * @return The connection dictionary based on user input
     */
    protected ConnectionDictionary showDialog() {
        pack();
        setLocationRelativeTo(getOwner());
        userField.requestFocusInWindow();	// put the user field in focus since it is the most likely to be edited first by the user
        setVisible(true);
        return dictionary;
    }

    /**
     * Attempt to connect to the database using the supplied database adaptor
     * and the connection dictionary specified by the user via the dialog box.
     *
     * @param databaseAdaptor the database adaptor to use for the connection
     * @return the new connection or null if the user canceled the dialog
     */
    public Connection showConnectionDialog(final DatabaseAdaptor databaseAdaptor) {
        ConnectionDictionary dictionary = showDialog();

        // check if the user cancelled the dialog
        if (dictionary == null) {
            return null;
        }

        try {
            return databaseAdaptor.getConnection(dictionary);
        } catch (DatabaseException exception) {
            JOptionPane.showMessageDialog(getOwner(), exception.getMessage(), "Connection Error!", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Database connection error.", exception);
            return showConnectionDialog((JFrame) getOwner(), databaseAdaptor, dictionary);
        }
    }

    /**
     * Display the dialog and return the connection dictionary.
     *
     * @param owner The window that owns dialog box
     * @return The connection dictionary based on user input
     */
    public static ConnectionDictionary showDialog(final Frame owner) {
        return new ConnectionDialog(owner).showDialog();
    }

    /**
     * Display the dialog and return the connection dictionary. Initialize the
     * new connection dictionary with the supplied one except that we ignore the
     * password.
     *
     * @param owner The window that owns dialog box
     * @param dictionary The dictionary from which to initialize the new
     * connection dictionary
     * @return The connection dictionary based on user input
     */
    public static ConnectionDictionary showDialog(final Frame owner, final ConnectionDictionary dictionary) {
        return new ConnectionDialog(owner, dictionary).showDialog();
    }

    /**
     * Display the dialog and return the connection dictionary. Initialize the
     * new connection dictionary with the supplied one except that we ignore the
     * password.
     *
     * @param owner The window that owns dialog box
     * @param dictionary The dictionary from which to initialize the new
     * connection dictionary
     * @param submitLabel The label to use for the submit button
     * @return The connection dictionary based on user input
     */
    public static ConnectionDictionary showDialog(final Frame owner, final ConnectionDictionary dictionary, final String submitLabel) {
        return new ConnectionDialog(owner, dictionary, submitLabel).showDialog();
    }

    /**
     * Display the dialog and return the connection dictionary. Initialize the
     * new connection dictionary with the supplied one except that we ignore the
     * password.
     *
     * @param owner The window that owns dialog box
     * @param databaseAdaptor The database adaptor to use to make the connection
     * @param dictionary The connection dictionary from which to initialize the
     * new connection dictionary
     * @return The connection dictionary based on user input
     */
    public static Connection showConnectionDialog(final Frame owner, final DatabaseAdaptor databaseAdaptor, final ConnectionDictionary dictionary) {
        return getInstance(owner, dictionary).showConnectionDialog(databaseAdaptor);
    }

    /**
     * Display the dialog and return the connection dictionary. Start with an
     * empty connection dictionary.
     *
     * @param owner The window that owns dialog box
     * @param databaseAdaptor The database adaptor to use to make the connection
     * @return The connection dictionary based on user input
     */
    public static Connection showConnectionDialog(final Frame owner, final DatabaseAdaptor databaseAdaptor) {
        return showConnectionDialog(owner, databaseAdaptor, new ConnectionDictionary());
    }

    /**
     * Get a new instance of the connection dialog.
     *
     * @param owner The window that owns the new connection dialog box
     * @param dictionary The connection dictionary from which to initialize the
     * new connection dictionary
     * @return A new instance of the connection dialog
     */
    public static ConnectionDialog getInstance(final Frame owner, final ConnectionDictionary dictionary) {
        return new ConnectionDialog(owner, dictionary, "Connect");
    }

    /**
     * Get a new instance of the connection dialog.
     *
     * @param owner The window that owns the new connection dialog box
     * @param dictionary The connection dictionary from which to initialize the
     * new connection dictionary
     * @return A new instance of the connection dialog
     */
    public static ConnectionDialog getInstance(final Dialog owner, final ConnectionDictionary dictionary) {
        return new ConnectionDialog(owner, dictionary, "Connect");
    }

    /**
     * Make the Dialog content
     */
    protected void makeContent() {
        setSize(250, 130);
        getContentPane().setLayout(new BorderLayout());
        Box mainView = new Box(BoxLayout.Y_AXIS);
        getContentPane().add(mainView);

        Dimension fieldSize;

        serverOptionBox = new Box(BoxLayout.X_AXIS);
        serverOptionBox.add(Box.createGlue());
        serverMenu = new JComboBox<>();
        serverOptionBox.add(new JLabel("Server: "));
        serverOptionBox.add(serverMenu);
        mainView.add(serverOptionBox);

        serverCustomForm = new Box(BoxLayout.Y_AXIS);
        mainView.add(serverCustomForm);

        Box adaptorBox = new Box(BoxLayout.X_AXIS);
        adaptorField = new JTextField(30);
        fieldSize = adaptorField.getPreferredSize();
        adaptorField.setMinimumSize(fieldSize);
        adaptorField.setMaximumSize(fieldSize);
        adaptorBox.add(Box.createGlue());
        adaptorBox.add(new JLabel("Adaptor (optional): "));
        adaptorBox.add(adaptorField);
        serverCustomForm.add(adaptorBox);

        Box urlBox = new Box(BoxLayout.X_AXIS);
        urlField = new JTextField(30);
        fieldSize = urlField.getPreferredSize();
        urlField.setMinimumSize(fieldSize);
        urlField.setMaximumSize(fieldSize);
        urlBox.add(Box.createGlue());
        urlBox.add(new JLabel("Database URL: "));
        urlBox.add(urlField);
        serverCustomForm.add(urlBox);

        Box userBox = new Box(BoxLayout.X_AXIS);
        userField = new JTextField(20);
        userField.setMinimumSize(fieldSize);
        userField.setMaximumSize(fieldSize);
        userBox.add(Box.createGlue());
        userBox.add(new JLabel("User: "));
        userBox.add(userField);
        mainView.add(userBox);

        Box passBox = new Box(BoxLayout.X_AXIS);
        passBox.add(Box.createGlue());
        passBox.add(new JLabel("Password: "));
        passwordField = new JPasswordField(20);
        fieldSize = passwordField.getPreferredSize();
        passwordField.setMinimumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);
        passBox.add(passwordField);
        mainView.add(passBox);
        mainView.add(Box.createGlue());

        Box buttonBox = new Box(BoxLayout.X_AXIS);
        mainView.add(buttonBox);

        final JButton configureButton = new JButton("Configure...");
        configureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                final boolean changed = ConnectionPreferenceController.displayPathPreferenceSelector(ConnectionDialog.this);
                if (changed) {
                    loadDefaultConfiguration();
                }
            }
        });

        buttonBox.add(configureButton);
        buttonBox.add(Box.createGlue());

        final JButton cancelButton = new JButton("Cancel");
        buttonBox.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                setVisible(false);
                dispose();
            }
        });

        final JButton submitButton = new JButton(submitLabel);
        getRootPane().setDefaultButton(submitButton);
        buttonBox.add(submitButton);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                dictionary = new ConnectionDictionary();

                if (userField.getText() != null) {
                    dictionary.setUser(userField.getText());
                }
                if (passwordField.getPassword() != null) {
                    dictionary.setPassword(String.valueOf(passwordField.getPassword()));
                }
                if (urlField.getText() != null) {
                    dictionary.setURLSpec(urlField.getText());
                }
                if (adaptorField.getText() != null) {
                    dictionary.setDatabaseAdaptorClass(adaptorField.getText());
                }
                setVisible(false);
                dispose();
            }
        });

        serverMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                final int selectedIndex = serverMenu.getSelectedIndex();
                if (selectedIndex > 0) {
                    final Object selection = serverMenu.getSelectedItem();
                    if (selection != null && configuration != null) {
                        final String serverName = selection.toString();
                        final ConnectionDictionary dictionary = configuration.newConnectionDictionary(null, serverName);
                        final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
                        adaptorField.setText(adaptor != null ? adaptor.getClass().getCanonicalName() : "");
                        urlField.setText(dictionary.getURLSpec());
                        setDisplayServerCustomForm(false);
                    } else {
                        setDisplayServerCustomForm(true);
                    }
                } else if (selectedIndex == 0) {
                    setDisplayServerCustomForm(true);
                }
            }
        });

        setResizable(false);
    }
}
