/*
 * RecentFileTracker.java
 *
 * Created on Thu May 20 10:01:29 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.tools.apputils.files;

import xal.tools.StringJoiner;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;
import java.net.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * RecentFileTracker caches recently accessed files into the the user's
 * preferences and has accessors for getting the recent files and the most
 * recent folder.
 *
 * @author tap
 */
public class RecentFileTracker {

    /**
     * default buffer size for a tracker
     */
    protected static final int DEFAULT_BUFFER_SIZE = 10;

    /**
     * pattern for storing the URL spec in a string
     */
    private static final Pattern URL_SPEC_STORE_PATTERN;

    private static final Logger LOGGER = Logger.getLogger(RecentFileTracker.class.getName());

    /**
     * buffer size for this tracker
     */
    protected final int recentUrlsBufferSize;

    /**
     * preferences storage
     */
    protected final Preferences prefs;

    /**
     * ID for the preferences
     */
    protected final String preferenceId;

    // static initializer
    static {
        // specs are enclosed within quotes
        URL_SPEC_STORE_PATTERN = Pattern.compile("\"[^\"]*\"");
    }

    /**
     * Primary constructor
     *
     * @param bufferSize the buffer size of the recent URL specs to cache
     * @param prefs the preferences used to save the cache of recent URL specs
     * @param preferenceID the ID of the preference to save
     */
    public RecentFileTracker(final int bufferSize, final Preferences prefs, final String preferenceID) {
        recentUrlsBufferSize = bufferSize;
        preferenceId = preferenceID;
        this.prefs = prefs;
    }

    /**
     * Constructor which generates the preferences from the specified preference
     * node
     *
     * @param bufferSize the buffer size of the recent URL specs to cache
     * @param preferenceNode the node used for saving the preference
     * @param preferenceID the ID of the preference to save
     */
    public RecentFileTracker(final int bufferSize, final Class<?> preferenceNode, final String preferenceID) {
        this(bufferSize, xal.tools.apputils.Preferences.nodeForPackage(preferenceNode), preferenceID);
    }

    /**
     * Constructor with a default buffer size of 10
     *
     * @param preferenceNode the node used for saving the preference
     * @param preferenceID the ID of the preference to save
     */
    public RecentFileTracker(final Class<?> preferenceNode, final String preferenceID) {
        this(DEFAULT_BUFFER_SIZE, preferenceNode, preferenceID);
    }

    /**
     * Clear the cache of the recent URL specs
     */
    public void clearCache() {
        prefs.put(preferenceId, "");
    }

    /**
     * Cache the URL of the specified file.
     *
     * @param file the file whose URL is to be cached.
     */
    public void cacheURL(File file) {
        try {
            cacheURL(file.toURI().toURL());
        } catch (MalformedURLException exception) {
            final String message = "Exception translating the file: " + file + " to a URL.";
            throw new RuntimeException(message, exception);
        }
    }

    /**
     * Cache the URL.
     *
     * @param url the URL to cache.
     */
    public void cacheURL(URL url) {
        cacheURL(url.toString());
    }

    /**
     * Cache the URL
     *
     * @param urlSpec the URL Spec to cache.
     */
    public void cacheURL(final String urlSpec) {
        // get the current list of specs
        final String[] recentURLSpecArray = getRecentURLSpecs();
        // hold the new list of specs
        final List<String> recentSpecs = new ArrayList<>(recentUrlsBufferSize);
        // add the new spec as the first item
        recentSpecs.add(urlSpec);

        // add the original specs expect for any spec matching the new one to avoid repetitions and don't exceed the buffer size
        for (int index = 0; index < recentURLSpecArray.length && recentSpecs.size() < recentUrlsBufferSize; index++) {
            final String recentURLSpec = recentURLSpecArray[index];
            // make sure we don't repeat the new spec
            if (!recentSpecs.contains(recentURLSpec)) {
                // add the spec
                recentSpecs.add(recentURLSpec);
            }
        }

        // create a new array with the recent specs encoded
        final List<String> recentEncodedSpecs = new ArrayList<>(recentSpecs.size());
        for (final String spec : recentSpecs) {
            recentEncodedSpecs.add(encodeItem(spec));
        }

        // merge the specs into a single comma delimited string
        final StringJoiner joiner = new StringJoiner(",");
        joiner.append(recentEncodedSpecs.toArray());

        // record the preference
        prefs.put(preferenceId, joiner.toString());
    }

    /**
     * encode the item for caching
     */
    private static String encodeItem(final String item) {
        // place quotes around the item
        return "\"" + item + "\"";
    }

    /**
     * decode the encoded item
     */
    private static String decodeItem(final String encodedItem) {
        if (encodedItem == null || encodedItem.length() == 0) {
            return null;
        }
        final int encodedLength = encodedItem.length();
        if (encodedLength > 2 && encodedItem.startsWith("\"") && encodedItem.endsWith("\"")) {
            // strip the starting and ending quotes
            return encodedItem.substring(1, encodedLength - 1);
        } else {
            return null;
        }
    }

    /**
     * Get the array of URLs corresponding to recently registered URLs. Fetch
     * the recent items from the list saved in the user's preferences for the
     * preference node.
     *
     * @return The array of recent URLs.
     */
    public String[] getRecentURLSpecs() {
        final String pathsStr = prefs.get(preferenceId, "");
        // check whether the paths are encoded using the new format ( quotes around each URL Spec )
        if (pathsStr != null && pathsStr.length() > 2 && pathsStr.startsWith("\"") && pathsStr.endsWith("\"")) {
            final Matcher matcher = URL_SPEC_STORE_PATTERN.matcher(pathsStr);
            final List<String> urlSpecs = new ArrayList<>();
            while (matcher.find()) {
                final String encodedItem = matcher.group();
                urlSpecs.add(decodeItem(encodedItem));
            }
            return urlSpecs.toArray(new String[urlSpecs.size()]);
        } else {
            // old format uses comma delimited items
            return getTokens(pathsStr, ",");
        }
    }

    /**
     * Get the folder corresponding to the most recently cached URL.
     *
     * @return the most recent folder accessed
     */
    public File getRecentFolder() {
        final File recentFile = getMostRecentFile();
        return recentFile != null ? recentFile.getParentFile() : null;
    }

    /**
     * Get the folder path corresponding to the most recently cached URL.
     *
     * @return path to the most recent folder accessed or null if none has been
     * accessed
     */
    public String getRecentFolderPath() {
        final File recentFolder = getRecentFolder();
        return recentFolder != null ? recentFolder.getPath() : null;
    }

    /**
     * Get the most recent file
     *
     * @return the most recently accessed file.
     */
    public File getMostRecentFile() {
        final String[] recentURLSpecs = getRecentURLSpecs();
        final String recentSpec = recentURLSpecs.length > 0 ? recentURLSpecs[0] : null;
        try {
            if (recentSpec != null) {
                final URL recentURL = new URL(recentSpec);
                return new File(recentURL.toURI());
            } else {
                return null;
            }
        } catch (MalformedURLException | URISyntaxException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return null;
        }
    }

    /**
     * Set the file chooser's current directory to the recent folder.
     *
     * @param fileChooser the file chooser for which to set the current
     * directory
     * @return the file chooser (same as the argument)
     */
    public JFileChooser applyRecentFolder(final JFileChooser fileChooser) {
        final File recentFolder = getRecentFolder();
        if (recentFolder != null) {
            fileChooser.setCurrentDirectory(recentFolder);
        }

        return fileChooser;
    }

    /**
     * Set the file chooser's selected file to the most recent file.
     *
     * @param fileChooser the file chooser for which to set the current
     * directory
     * @return the file chooser (same as the argument)
     */
    public JFileChooser applyMostRecentFile(final JFileChooser fileChooser) {
        final File recentFile = getMostRecentFile();
        if (recentFile != null && recentFile.exists()) {
            fileChooser.setSelectedFile(recentFile);
        }

        return fileChooser;
    }

    /**
     * Parse a string into tokens where whitespace is the delimiter.
     *
     * @param string The string to parse.
     * @return The array of tokens.
     */
    protected static String[] getTokens(final String string) {
        return getTokens(string, " \t");
    }

    /**
     * Parse a string into tokens with the specified delimiter.
     *
     * @param string The string to parse.
     * @param delim The delimiter
     * @return The array of tokens.
     */
    protected static String[] getTokens(final String string, final String delim) {
        final StringTokenizer tokenizer = new StringTokenizer(string, delim);
        final int numTokens = tokenizer.countTokens();
        final String[] tokens = new String[numTokens];

        for (int index = 0; index < numTokens; index++) {
            tokens[index] = tokenizer.nextToken();
        }

        return tokens;
    }
}
