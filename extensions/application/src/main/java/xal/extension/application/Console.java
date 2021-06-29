/*
 * Console.java
 *
 * Created on March 18, 2003, 1:42 PM
 */
package xal.extension.application;

import xal.tools.IconLib;

import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * The Console captures standard output and standard error streams. Both are
 * displayed in a console window. One console serves the entire application.
 * Standard output appears in black text while standard error appears in red
 * text.
 *
 * @author t6p
 */
class Console {

    /**
     * console character limit
     */
    private static final int CHAR_LIMIT = 250000;

    /**
     * the console instance
     */
    private static final Console CONSOLE;

    /**
     * preference key for logging
     */
    private static final String LOGGING_KEY = "LogOutput";

    /**
     * logging preferences
     */
    private static final Preferences LOG_PREFS;

    private static final Logger LOGGER = Logger.getLogger(Console.class.getName());

    /**
     * file writer where log files are stored
     */
    private Writer logWriter;

    /**
     * indicates whether the output should be logged to a file
     */
    private boolean logsOutput;

    // stream variables
    private final PrintStream standardOut;
    private final PrintStream standardErr;
    private final ConsoleOutHandler outStream;
    private final ConsoleErrHandler errStream;

    // view variables
    private JFrame frame;
    private boolean neverShown;
    private JTextPane textView;
    private Style outStyle;
    private Style errStyle;
    private DefaultStyledDocument document;

    // static initializer
    static {
        LOG_PREFS = xal.tools.apputils.Preferences.nodeForPackage(Console.class);
        CONSOLE = new Console();
    }

    /**
     * Constructor
     */
    public Console() {
        neverShown = true;
        outStream = new ConsoleOutHandler();
        errStream = new ConsoleErrHandler();
        standardOut = System.out;
        standardErr = System.err;

        logsOutput = LOG_PREFS.getBoolean(LOGGING_KEY, false);
        if (logsOutput) {
            configureLogs();
        }

        makeTextView();
        makeFrame();
    }

    /**
     * configure the logs for recording output
     */
    private void configureLogs() {
        if (logWriter == null) {
            try {
                final String homePath = System.getProperty("user.home");
                final Date now = new Date();
                final String year = new SimpleDateFormat("yyyy").format(now);
                final String appName = Application.getAdaptor().applicationName();
                // log directory is of the form ~/.xal/logs/${current year}/${appname}
                final File logDirectory = new File(new File(new File(new File(new File(homePath), ".xal"), "logs"), String.valueOf(year)), appName);
                if (!logDirectory.exists()) {
                    logDirectory.mkdirs();
                }
                final File logFile = new File(logDirectory, appName + "_" + new SimpleDateFormat("yyyyMMdd'_'HHmmss'_'SSS").format(now) + ".log");
                logWriter = new BufferedWriter(new FileWriter(logFile));
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }
    }

    /**
     * Make the frame for the console.
     */
    private void makeFrame() {
        frame = new JFrame("Console");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setTitle(Application.getAdaptor().applicationName() + " - Console");
        frame.getContentPane().setLayout(new BorderLayout());

        generateContentsFor(frame.getContentPane());
    }

    /**
     * Make contents and add them to the container.
     *
     * @param containter the container to which the contents are added.
     */
    private void generateContentsFor(final Container container) {
        final Box buttonBar = new Box(BoxLayout.X_AXIS);
        final JButton clearButton = new JButton();
        clearButton.setIcon(IconLib.getIcon("custom", "Clear24.gif"));
        clearButton.setToolTipText("Clear the console...");

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    document.remove(0, document.getLength());
                } catch (BadLocationException exception) {
                }
            }
        });

        buttonBar.add(clearButton);
        buttonBar.add(Box.createGlue());

        final JCheckBox logCheckBox = new JCheckBox("Persistent Log");
        logCheckBox.setToolTipText("Enable/disable persistent logging for all applications. Launch an application using -Dxal.admin=true to enable this option.");
        logCheckBox.setSelected(logsOutput);

        logCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                logsOutput = logCheckBox.isSelected();
                if (logsOutput) {
                    configureLogs();
                }
                LOG_PREFS.putBoolean(LOGGING_KEY, logsOutput);
            }
        });
        buttonBar.add(logCheckBox);
        // if the application was launched with the -Dxal.admin=true flag, then allow the user to change the logging flag
        logCheckBox.setEnabled(Boolean.parseBoolean(System.getProperty("xal.admin")));

        container.add(buttonBar, "North");

        Box box = new Box(BoxLayout.Y_AXIS);
        container.add(box, "Center");

        JScrollPane scrollPane = new JScrollPane(textView);
        box.add(scrollPane, "Center");
    }

    /**
     * Make the view that holds the text. The text is set to be un-editable.
     */
    private void makeTextView() {
        StyleContext context = new StyleContext();
        document = new DefaultStyledDocument(context);
        textView = new JTextPane(document);
        textView.setEditable(false);

        outStyle = context.addStyle(null, null);
        StyleConstants.setForeground(outStyle, Color.black);

        errStyle = context.addStyle(null, null);
        StyleConstants.setForeground(errStyle, Color.red);
    }

    /**
     * Sets the console to capture standard output.
     */
    static void captureOutput() {
        System.setOut(new PrintStream(CONSOLE.outStream));
    }

    /**
     * Sets the console to capture standard error.
     */
    static void captureErr() {
        System.setErr(new PrintStream(CONSOLE.errStream));
    }

    /**
     * Show the console. If the console has never been shown before, place it
     * relative to the sender, otherwise simply show it where it was last placed
     * by the user. However, if the console window is on a different screen than
     * the sender, bring the console window back and display it relative to the
     * sender.
     *
     * @param sender The component relative to which the console should be
     * positioned
     */
    static void showNear(final java.awt.Component sender) {
        if (CONSOLE.neverShown) {
            CONSOLE.frame.setLocationRelativeTo(sender);
            CONSOLE.neverShown = false;
        } else if (!sender.getGraphicsConfiguration().getDevice().getIDstring().equals(CONSOLE.frame.getGraphicsConfiguration().getDevice().getIDstring())) {
            // if the console window is on a different screen bring it to the same screen as the sender
            CONSOLE.frame.setVisible(false);
            CONSOLE.frame.setLocationRelativeTo(sender);
        }
        CONSOLE.frame.setVisible(true);
    }

    /**
     * Hide the console.
     */
    static void hide() {
        CONSOLE.frame.setVisible(false);
    }

    /**
     * The internal class whose instance handles the output stream. The output
     * is inserted into the text pane's document as black text.
     */
    protected class ConsoleOutHandler extends OutputStream {

        /**
         * Write output to both standard out and the Console view
         *
         * @param character The character to write
         */
        @Override
        public void write(final int character) {
            try {
                standardOut.write(character);
                document.insertString(document.getLength(), String.valueOf((char) character), outStyle);
                if (document.getLength() > CHAR_LIMIT) {
                    document.remove(0, CHAR_LIMIT / 10);		// shed the first 10 percent
                }
                if (logsOutput) {
                    logWriter.write(character);
                    logWriter.flush();
                }
            } catch (IOException | BadLocationException exception) {
            }
        }
    }

    /**
     * The internal class whose instance handles the error stream. The output is
     * inserted into the text pane's document as red text.
     */
    protected class ConsoleErrHandler extends OutputStream {

        /**
         * Write output to both standard err and the Console view
         *
         * @param character The character to write
         */
        @Override
        public void write(final int character) {
            try {
                standardErr.write(character);
                document.insertString(document.getLength(), String.valueOf((char) character), errStyle);
                if (document.getLength() > CHAR_LIMIT) {
                    document.remove(0, CHAR_LIMIT / 10);		// shed the first 10 percent
                }
                if (logsOutput) {
                    logWriter.write(character);
                    logWriter.flush();
                }
            } catch (IOException | BadLocationException exception) {
            }
        }
    }
}
