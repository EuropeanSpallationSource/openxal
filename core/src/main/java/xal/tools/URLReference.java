//
// URLReference.java: Source file for 'URLReference'
// Project xal
//
// Created by Tom Pelaia II on 5/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//
package xal.tools;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * URLReference
 */
public class URLReference {

    /**
     * root if any
     */
    private final String rootSpec;

    /**
     * URL spec
     */
    private final String urlSpec;

    private static final Logger LOGGER = Logger.getLogger(URLReference.class.getName());

    /**
     * Create a new reference testing whether the url spec is rooted in the
     * root.
     *
     * @param rootSpec root URL spec if any
     * @param urlSpec URL spec to represent
     */
    private URLReference(final String rootSpec, final String urlSpec) {
        this.rootSpec = rootSpec;
        this.urlSpec = urlSpec;
    }

    /**
     * Create a new reference testing whether the url spec is rooted in the
     * root.
     *
     * @param possibleRoot possible root for the specified full URL spec
     * @param fullURLSpec full URL spec to represent
     */
    public static URLReference getInstance(final URL possibleRoot, final String fullURLSpec) {
        final String rootSpec = isRootedIn(possibleRoot, fullURLSpec) ? possibleRoot.toString() : null;
        final String urlSpec = rootSpec != null ? getRelativeURLSpec(rootSpec, fullURLSpec) : fullURLSpec;
        return new URLReference(rootSpec, urlSpec);
    }

    /**
     * Get the full URL Spec
     */
    public String getFullURLSpec() {
        return rootSpec != null ? rootSpec + urlSpec : urlSpec;
    }

    /**
     * Get the URL spec which is relative to the root if possible and absolute
     * if not
     */
    public String getURLSpec() {
        return isRooted() ? urlSpec : getFullURLSpec();
    }

    /**
     * Get the URL spec relative to the root
     */
    private static String getRelativeURLSpec(final String rootSpec, final String fullURLSpec) {
        return isRootedIn(rootSpec, fullURLSpec) ? fullURLSpec.substring(rootSpec.length()) : null;
    }

    /**
     * Test whether the file is rooted in the directory
     */
    public static boolean isRootedIn(final File directory, final File file) {
        try {
            return directory != null && file != null ? isRootedIn(directory.toURI().toURL(), file.toURI().toURL()) : false;
        } catch (MalformedURLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            throw new RuntimeException("Exception testing whether the file is rooted in the directory.", exception);
        }
    }

    /**
     * Test whether the URL is rooted in the specified root URL
     */
    public static boolean isRootedIn(final URL rootURL, final URL url) {
        return rootURL != null && url != null ? isRootedIn(rootURL, url.toString()) : false;
    }

    /**
     * Test whether the URL spec is rooted in the specified root URL
     */
    public static boolean isRootedIn(final URL rootURL, final String urlSpec) {
        return rootURL != null && urlSpec != null ? isRootedIn(rootURL.toString(), urlSpec) : false;
    }

    /**
     * Test whether the URL spec is rooted in the specified root URL
     */
    private static boolean isRootedIn(final String rootSpec, final String urlSpec) {
        return rootSpec != null && urlSpec != null ? urlSpec.startsWith(rootSpec) : false;
    }

    /**
     * Test whether the URL Spec is rooted in the possible root
     */
    public boolean isRooted() {
        return rootSpec != null;
    }

    /**
     * Generate and return URL References for all url Specs which are valid
     */
    public static URLReference[] getValidReferences(final URL possibleRoot, final String[] urlSpecs) {
        final List<URLReference> references = urlSpecs != null ? new ArrayList<>(urlSpecs.length) : new ArrayList<>();
        for (final String urlSpec : urlSpecs) {
            if (isValid(urlSpec)) {
                references.add(URLReference.getInstance(possibleRoot, urlSpec));
            }
        }

        return references.toArray(new URLReference[0]);
    }

    /**
     * Test whether the URL spec represents a valid file
     */
    public boolean isValid() {
        return isValid(getFullURLSpec());
    }

    /**
     * Test whether the URL spec represents a valid file
     */
    private static boolean isValid(final String fullUrlSpec) {
        try {
            new URL(fullUrlSpec).openStream().close();     // test if the file really exists
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    /**
     * Overridden to return the URL spec
     */
    @Override
    public String toString() {
        return getURLSpec();
    }
}
