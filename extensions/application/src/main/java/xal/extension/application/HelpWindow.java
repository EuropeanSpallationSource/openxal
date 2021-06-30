/*
 * HelpWindow.java
 *
 * Created on March 26, 2002, 2:03 PM
 */
package xal.extension.application;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.Cursor;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.*;

/**
 * The help window that displays documentation on the application. There is only
 * one help window for the entire application.
 *
 * @author tap
 */
class HelpWindow extends JFrame implements SwingConstants {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * name for the help starting point resource which may or may not exist
     */
    public static final String HELP_START_RESOURCE = "Help.html";

    private static final Logger LOGGER = Logger.getLogger(HelpWindow.class.getName());

    // -------- static variables -----------------------------------------------
    private static final URL homePage;
    private static HelpWindow helpWindow;
    private static JTextPane textPane;

    // -------- instance variables ---------------------------------------------
    private boolean neverShown;
    private LinkedList<URL> pageHistory;
    private int pageHistoryIndex;

    // visual components
    private JButton backButton;
    private JButton forwardButton;
    private JButton homeButton;

    static {
        homePage = getHelpSource();
    }

    /**
     * Creates new form HelpWindow
     */
    public HelpWindow() {
        pageHistory = new LinkedList<>();

        makeView();
        neverShown = true;
        setTitle(Application.getAdaptor().applicationName() + " - Help");
        loadHome();
    }

    /**
     * Load the help contents from a file into a text pane of the help window.
     *
     * @param helpSource The URL of the help source contents.
     */
    private void loadHome() {
        try {
            textPane.setPage(homePage);
            textPane.setEditable(false);
            pageHistory.add(homePage);
            pageHistoryIndex = 0;
            updateView();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error loading the help page.", exception);
            LOGGER.log(Level.SEVERE, null, exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Test whether we can navigate forward in history.
     *
     * @return true if we can navigate forward in history and false if not.
     */
    public boolean canNavigateForward() {
        return pageHistoryIndex < (pageHistory.size() - 1);
    }

    /**
     * Test whether we can navigate back in history.
     *
     * @return true if we can navigate back in history and false if not.
     */
    public boolean canNavigateBack() {
        return pageHistoryIndex > 0;
    }

    /**
     * Load the page whose history index is the specified index.
     *
     * @param index The history index of the page to load.
     */
    public void goToPage(final int index) {
        final URL link = pageHistory.get(index);

        final Cursor lastCursor = getCursor();
        try {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            textPane.setPage(link);
            EditorKit editorKit = textPane.getEditorKit();
            pageHistoryIndex = index;
            updateView();
        } catch (IOException exception) {
            final String message = "Help is unable to hyperlink to " + link;
            LOGGER.log(Level.WARNING, message, exception);
            Application.displayError("Link Error", "Error accessing link:", exception);
        } finally {
            setCursor(lastCursor);
        }
    }

    /**
     * Load the page corresponding to the link whose history index is offset
     * from the present history index by the specified amount.
     *
     * @param increment the offset from the present history index.
     */
    public void incrementPage(final int increment) {
        goToPage(pageHistoryIndex + increment);
    }

    /**
     * Load the contents of the specified link into the text pane.
     *
     * @param link The URL of the link to load.
     */
    public void loadLink(final URL link) {
        pageHistory = new LinkedList<>(pageHistory.subList(0, pageHistoryIndex + 1));
        pageHistory.add(link);
        goToPage(pageHistoryIndex + 1);
    }

    /**
     * Enable the HelpWindow if and only if there is a URL loaded for the help
     * page.
     *
     * @return true if the HelpWindow is enabled and false otherwise.
     */
    static boolean isAvailable() {
        return homePage != null;
    }

    /**
     * Get the source URL for the help contents.
     *
     * @return The URL of the help contents.
     */
    private static URL getHelpSource() {
        return Application.getAdaptor().getResourceURL(HELP_START_RESOURCE);
    }

    /**
     * Show the help window near the specified component if the help window has
     * never been shown before. Once it has been shown, then show the help
     * window in the last place the user left it. However, if the help window is
     * on a different screen than the sender, bring the help window back
     * relative to the sender.
     *
     * @param sender The component near which the help window should be shown
     */
    private void showWindowNear(final Component sender) {
        if (neverShown) {
            setLocationRelativeTo(sender);
            neverShown = false;
        } else if (!sender.getGraphicsConfiguration().getDevice().getIDstring().equals(getGraphicsConfiguration().getDevice().getIDstring())) {
            // if the help window is on a different screen bring it to the same screen as the sender
            setVisible(false);
            setLocationRelativeTo(sender);
        }

        // don't iconify this window
        setState(Frame.NORMAL);
        // make the window visible
        setVisible(true);
    }

    /**
     * Static method for showing the single help window instance near the
     * specified component. It simply calls showWindowNear() on the single
     * instance of the help window. See that method for details on the behavior
     * of this method.
     *
     * @param sender The component near which the help window should be shown
     */
    public static void showNear(final Component sender) {
        if (helpWindow == null) {
            helpWindow = new HelpWindow();
        }
        helpWindow.showWindowNear(sender);
    }

    /**
     * Update the view to reflect the present state.
     */
    private void updateView() {
        backButton.setEnabled(canNavigateBack());
        forwardButton.setEnabled(canNavigateForward());
    }

    /**
     * Make the view that displays the help contents within the help window.
     */
    private void makeView() {
        JScrollPane scrollPane = new JScrollPane();
        textPane = new JTextPane();

        setTitle("Help");

        Box buttonRow = new Box(BoxLayout.X_AXIS);

        backButton = new JButton("<");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                incrementPage(-1);
            }
        });

        forwardButton = new JButton(">");
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                incrementPage(1);
            }
        });

        homeButton = new JButton("Home");
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                loadLink(homePage);
            }
        });

        buttonRow.add(backButton);
        buttonRow.add(forwardButton);
        buttonRow.add(Box.createHorizontalStrut(5));
        buttonRow.add(homeButton);
        buttonRow.add(Box.createHorizontalGlue());

        scrollPane.setPreferredSize(new Dimension(600, 400));
        textPane.setEditable(false);
        textPane.setFont(new Font("TimesNewRoman", 0, 12));
        textPane.setPreferredSize(new Dimension(6, 6));

        textPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                handleHyperlink(event);
            }
        });

        scrollPane.setViewportView(textPane);

        JSplitPane mainView = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, buttonRow, scrollPane);
        mainView.setDividerSize(1);
        mainView.setResizeWeight(0.0);
        mainView.setOneTouchExpandable(false);
        mainView.setEnabled(false);
        getContentPane().add(mainView);

        pack();
    }

    /**
     * Handle the hyperlink event generated when the user activates a hyperlink
     * within the help contents. The event is handled by displaying the target
     * of the hyperlink in the help window.
     */
    private void handleHyperlink(final HyperlinkEvent event) {
        if (event instanceof HTMLFrameHyperlinkEvent) {
            Cursor lastCursor = getCursor();
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            HTMLDocument document = (HTMLDocument) textPane.getDocument();
            document.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) event);
            setCursor(lastCursor);
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            loadLink(event.getURL());
        }
    }
}
