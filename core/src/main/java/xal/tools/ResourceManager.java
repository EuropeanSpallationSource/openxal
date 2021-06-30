//
//  ResourceManager.java
//  xal
//
//  Created by Thomas Pelaia on 5/23/2014.
//  Copyright 2014 Oak Ridge National Lab. All rights reserved.
//
package xal.tools;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.*;

/**
 * <p>
 * Provide normalized methods for getting resources There are two separate
 * mechanisms for getting resources (jar based and file based)
 * <br>
 * <br>
 * - The jar based resource manager is the standard mechanism and it searches
 * for resources in the binary's jar files. This the only option that should be
 * used in production.
 * <br>
 * <br>
 * - The file based resource manager can be set as the default if the
 * environment variable OPENXAL_FIND_RESOURCES_IN_ROOT is set to true. The
 * OPENXAL_HOME environment variable must be set to the root of the project. The
 * file based resource manager searches for resources directly on the file
 * system relative to the project. This may be useful in development for IDE's
 * that compile code in real time and do not generate the usual jar files. This
 * option should not be used in production.
 * </p>
 */
public abstract class ResourceManager {

    protected static final String RESOURCES_FILE_SEARCH_PROPERTY = "OPENXAL_FIND_RESOURCES_IN_ROOT";

    /**
     * default resource manager
     */
    private static final ResourceManager DEFAULT_MANAGER;

    /**
     * static initializer
     */
    static {
        DEFAULT_MANAGER = useFileResourceManager() ? getFileResourceManager() : getJarredResourceManager();
    }

    /**
     * determine whether to use the file resource manager by first looking at
     * the OPENXAL_FIND_RESOURCES_IN_HOME property and then the corresponding
     * environment variable if necessary
     */
    private static boolean useFileResourceManager() {
        // the property set to true indicates whether to find resources under the OPENXAL_HOME directory instead of the jar files
        final boolean hasProperty = System.getProperty(RESOURCES_FILE_SEARCH_PROPERTY) != null;

        // first check system properties and if it exists then use it's value
        if (hasProperty) {
            return Boolean.getBoolean(RESOURCES_FILE_SEARCH_PROPERTY);
        // check for an environment variable of the same name
        } else {
            final String environment = System.getenv(RESOURCES_FILE_SEARCH_PROPERTY);
            return Boolean.parseBoolean(environment);
        }
    }

    /**
     * get the singleton instance of the jarred resource manager
     */
    private static FileResourceManager getFileResourceManager() {
        return FileResourceManager.getInstance();
    }

    /**
     * get the singleton instance of the jarred resource manager
     */
    private static JarredResourceManager getJarredResourceManager() {
        return JarredResourceManager.getInstance();
    }

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param resourcePath to the resource relative to the group's resources
     * directory
     * @return URL to the resource
     */
    public URL fetchResourceURL(final Class<?> rootClass, final String resourcePath) {
        return fetchResourceURL(null, rootClass, resourcePath);
    }

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param subdomain subdomain under which to search (e.g. for "core" with a
     * subdomain of "test" we must search "core/test")
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param resourcePath to the resource relative to the group's resources
     * directory
     * @return URL to the resource
     */
    public abstract URL fetchResourceURL(final String subdomain, final Class<?> rootClass, final String resourcePath);

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param resourcePath to the resource relative to the group's resources
     * directory
     * @return URL to the resource
     */
    public static URL getResourceURL(final Class<?> rootClass, final String resourcePath) {
        return getResourceURL(null, rootClass, resourcePath);
    }

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param subdomain subdomain under which to search (e.g. for "core" with a
     * subdomain of "test" we must search "core/test")
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param resourcePath to the resource relative to the group's resources
     * directory
     * @return URL to the resource
     */
    public static URL getResourceURL(final String subdomain, final Class<?> rootClass, final String resourcePath) {
        final URL resourceURL = DEFAULT_MANAGER.fetchResourceURL(subdomain, rootClass, resourcePath);
        return resourceURL;
    }

    /**
     * get the path to the project home based on the "xal.home" property or
     * corresponding "OPENXAL_HOME" environment variable if necessary
     */
    public static String getProjectHomePath() {
        // the property set to true indicates whether to find resources under the OPENXAL_HOME directory instead of the jar files
        final String path = System.getProperty("xal.home");

        return path != null ? path : System.getenv("OPENXAL_HOME");
    }
}

/**
 * Resource manager that loads resources from jar files
 */
class JarredResourceManager extends ResourceManager {

    /**
     * singleton instance
     */
    private static final JarredResourceManager RESOURCE_MANAGER;

    // static initializer
    static {
        RESOURCE_MANAGER = new JarredResourceManager();
    }

    /**
     * get the singleton instance
     */
    public static JarredResourceManager getInstance() {
        return RESOURCE_MANAGER;
    }

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param subdomain subdomain under which to search (e.g. for "core" with a
     * subdomain of "test" we must search "core/test")
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param path to the resource relative to the group's resources directory
     * @return URL to the resource
     */
    @Override
    public URL fetchResourceURL(final String subdomain, final Class<?> rootClass, final String resourcePath) {
        final URL directResourceURL = fetchDirectResourceURL(rootClass, resourcePath);
        return directResourceURL != null ? directResourceURL : fetchContainerResourceURL(rootClass, resourcePath);
    }

    /**
     * Look relative to the class (applies to core)
     */
    private URL fetchDirectResourceURL(final Class<?> rootClass, final String resourcePath) {
        return rootClass.getResource(resourcePath);
    }

    /**
     * Look in the component's corresponding resources directory
     */
    public URL fetchContainerResourceURL(final Class<?> rootClass, final String resourcePath) {
        final PackagePartition packagePartition = PackagePartition.getValidInstance(rootClass);

        if (packagePartition != null) {
            // e.g. app, extension, plugin, service
            final String componentType = packagePartition.componentType;
            // e.g. application, widgets, pvlogger, scan1d, launcher
            final String component = packagePartition.componentName;

            // e.g. "/xal/"
            final StringBuilder pathBuilder = new StringBuilder("/" + packagePartition.packagePrefix + "/");
            pathBuilder.append(componentType);
            pathBuilder.append("/").append(component).append("/resources");

            final String packageSuffix = packagePartition.packageSuffix;
            if (packageSuffix != null && packageSuffix.length() > 0) {
                final String suffixPath = packageSuffix.replaceAll("\\.", "/");
                pathBuilder.append("/").append(suffixPath);
            }

            pathBuilder.append("/").append(resourcePath);

            final String path = pathBuilder.toString();
            return rootClass.getResource(path);
        } else {
            return null;
        }
    }
}

/**
 * Resource manager that loads resources from file system
 */
class FileResourceManager extends ResourceManager {

    /**
     * root location of the project
     */
    private static final String PROJECT_HOME_PROPERTY = "OPENXAL_HOME";

    /**
     * singleton instance
     */
    private static final FileResourceManager RESOURCE_MANAGER;

    /**
     * root relative to which the resources will be found
     */
    private final File rootFile;

    // static initializer
    static {
        RESOURCE_MANAGER = createInstance();
    }

    /**
     * get the singleton instance
     */
    public static FileResourceManager getInstance() {
        return RESOURCE_MANAGER;
    }

    /**
     * create an instance using the system properties
     */
    private static FileResourceManager createInstance() {
        final String home = getProjectHomePath();
        if (home != null) {
            return new FileResourceManager(home);
        } else {
            throw new RuntimeException(RESOURCES_FILE_SEARCH_PROPERTY + " property set to true, but " + PROJECT_HOME_PROPERTY + " is not set, so cannot initialize ResourceManager.");
        }
    }

    /**
     * Constructor
     */
    public FileResourceManager(final String rootPath) {
        rootFile = new File(rootPath);
    }

    /**
     * Constructor
     */
    public FileResourceManager(final File rootFile) {
        this.rootFile = rootFile;
    }

    /**
     * Get the URL to the specified resource relative to the specified class
     *
     * @param subdomain subdomain under which to search (e.g. for "core" with a
     * subdomain of "test" we must search "core/test")
     * @param rootClass class at the root of the group (this class must be at
     * the same location as the resources directory in the jar file)
     * @param path to the resource relative to the group's resources directory
     * @return URL to the resource
     */
    @Override
    public URL fetchResourceURL(final String subdomain, final Class<?> rootClass, final String resourcePath) {

        // first look for the resource in core
        final URL coreResourceURL = fetchCoreResourceURL(subdomain, rootClass, resourcePath);
        if (coreResourceURL != null) {
            return coreResourceURL;
        } else {
            // look for the resource based on the package's component (e.g. extension, app, service, etc.)
            final URL componentResourceURL = fetchContainerResourceURL(rootClass, false, resourcePath);
            if (componentResourceURL != null) {
                return componentResourceURL;
            } else {
                // sometimes resources for apps and services are associated with a corresponding app/service specific extension (e.g. services/pvlogger/extension/resources/configuraiton.xml)
                return fetchContainerResourceURL(rootClass, true, resourcePath);
            }
        }
    }

    /**
     * Look relative to the class (applies to core)
     */
    private URL fetchCoreResourceURL(final String subdomain, final Class<?> rootClass, final String resourcePath) {
        try {
            // first try to find a site specific resource
            final File siteCoreResource = fetchCoreResourceFile(subdomain, rootClass, "site", resourcePath);
            if (siteCoreResource.exists()) {
                return siteCoreResource.toURI().toURL();
            // next try to find the resource in the common component
            } else {
                final File coreResource = fetchCoreResourceFile(subdomain, rootClass, null, resourcePath);
                if (coreResource.exists()) {
                    return coreResource.toURI().toURL();
                } else {
                    return null;
                }
            }
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Malformed URL when fetching resource URL from file.", exception);
        }
    }

    /**
     * Look relative to the class (applies to core)
     */
    private File fetchCoreResourceFile(final String subdomain, final Class<?> rootClass, final String prefix, final String resourcePath) {
        final File baseFile = prefix != null ? new File(rootFile, prefix) : rootFile;
        final File coreDirectory = new File(baseFile, "core");
        // search under the core's subdomain (e.g. "test) if any otherwise search directly under core
        final File subdomainDirectory = subdomain != null ? new File(coreDirectory, subdomain) : coreDirectory;
        final File resourcesDirectory = new File(subdomainDirectory, "resources");

        String pathFromResources;
        // resource path is absolute and hence relative to "resources" root
        if (resourcePath.startsWith("/")) {
            // strip the leading "/"
            pathFromResources = resourcePath.substring(1);
        // resource path is relative and hence relative to the root class's package
        } else {
            // replace package dot delimiter with URL slash delimiter (should work on all platforms if we use URLs here instead of files)
            final String packagePath = rootClass.getPackage().getName().replaceAll("\\.", "/");
            pathFromResources = packagePath + "/" + resourcePath;
        }

        // use URLs to avoid file system path separator dependencies
        try {
            final URL resourcesURL = resourcesDirectory.toURI().toURL();
            final URL resourceURL = new URL(resourcesURL, pathFromResources);

            return new File(resourceURL.toURI());
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Malformed URL when fetching resource URL from file.", exception);
        } catch (URISyntaxException exception) {
            throw new RuntimeException("URI syntax exception when fetching resource URL from file.", exception);
        }
    }

    /**
     * Look in the component's corresponding resources directory
     */
    public URL fetchContainerResourceURL(final Class<?> rootClass, final boolean includeExtension, final String resourcePath) {
        try {
            // first try to find a site specific resource
            final File siteContainerResource = fetchContainerResourceFile(rootClass, "site", includeExtension, resourcePath);
            if (siteContainerResource != null && siteContainerResource.exists()) {
                return siteContainerResource.toURI().toURL();
            // next try to find the resource in the common component
            } else {
                final File componentResource = fetchContainerResourceFile(rootClass, null, includeExtension, resourcePath);
                if (componentResource != null && componentResource.exists()) {
                    return componentResource.toURI().toURL();
                } else {
                    return null;
                }
            }
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Malformed URL when fetching component resource URL from file.", exception);
        }
    }

    /**
     * Look in the component's corresponding resources directory
     */
    public File fetchContainerResourceFile(final Class<?> rootClass, final String prefix, final boolean includeExtension, final String resourcePath) {
        final PackagePartition packagePartition = PackagePartition.getValidInstance(rootClass);

        if (packagePartition != null) {
            // e.g. app, extension, plugin, service
            final String componentType = packagePartition.componentType;
            // e.g. application, widgets, pvlogger, scan1d, launcher
            final String component = packagePartition.componentName;

            // e.g. ${OPENXAL_HOME} or ${OPENXAL_HOME}/site
            final File baseDirectory = prefix != null ? new File(rootFile, prefix) : rootFile;
            if (!baseDirectory.exists()) {
                return null;
            }

            // e.g. ${OPENXAL_HOME}/extensions
            final File componentTypeRoot = new File(baseDirectory, componentType + "s");
            if (!componentTypeRoot.exists()) {
                return null;
            }

            // e.g. ${OPENXAL_HOME}/extensions/application
            final File componentDirectory = new File(componentTypeRoot, component);
            if (!componentDirectory.exists()) {
                return null;
            }

            // e.g. ${OPENXAL_HOME}/site/services/pvlogger/extension
            final File resourcesParent = includeExtension ? new File(componentDirectory, "extension") : componentDirectory;
            if (!resourcesParent.exists()) {
                return null;
            }

            // e.g. ${OPENXAL_HOME}/extensions/application/resources
            final File resourcesDirectory = new File(resourcesParent, "resources");
            if (!resourcesDirectory.exists()) {
                return null;
            }

            String pathFromResources;
            // resource path is absolute and hence relative to "resources" root
            if (resourcePath.startsWith("/")) {
                // strip the leading "/"
                pathFromResources = resourcePath.substring(1);
            // resource path is relative and hence relative to the root class's package suffix (i.e. relative to component)
            } else {
                // replace package dot delimiter with URL slash delimiter (should work on all platforms if we use URLs here instead of files)
                final String packageSuffix = packagePartition.packageSuffix;
                // e.g. smf  (replacing dots with /)
                final String relativePackagePath = packageSuffix != null ? packageSuffix.replaceAll("\\.", "/") : null;
                // e.g. smf/menudef.properties
                pathFromResources = relativePackagePath != null ? relativePackagePath + "/" + resourcePath : resourcePath;
            }

            // use URLs to avoid file system path separator dependencies
            try {
                final URL resourcesURL = resourcesDirectory.toURI().toURL();
                //${OPENXAL_HOME}/extensions/application/resources/smf/menudef.properties
                // e.g. file:
                final URL resourceURL = new URL(resourcesURL, pathFromResources);

                // e.g. ${OPENXAL_HOME}/extensions/application/resources/smf/menudef.properties
                return new File(resourceURL.toURI());
            } catch (MalformedURLException exception) {
                throw new RuntimeException("Malformed URL when fetching resource URL from file.", exception);
            } catch (URISyntaxException exception) {
                throw new RuntimeException("URI syntax exception when fetching resource URL from file.", exception);
            }
        } else {
            return null;
        }
    }
}

/**
 * package parsed into parts
 */
class PackagePartition {

    /**
     * pattern to match an XAL package name
     */
    protected static final Pattern XAL_PACKAGE_PATTERN = Pattern.compile("^(\\w+)\\.(\\w+)\\.(\\w+)(\\..+)?$");

    /**
     * package name
     */
    public final String packageName;

    /**
     * prefix to the package
     *
     * e.g. xal
     */
    public final String packagePrefix;

    /**
     * component type
     *
     * e.g. app, extension, plugin, service
     */
    public final String componentType;

    /**
     * name of the component
     */
    /**
     * e.g. application, widgets, pvlogger, scan1d, launcher
     */
    public final String componentName;

    /**
     * package suffix
     *
     * e.g. smf in xal.extension.application.smf
     */
    public final String packageSuffix;

    /**
     * Constructor
     */
    public PackagePartition(final Class<?> rootClass) {
        packageName = rootClass.getPackage().getName();

        final Matcher packageMatcher = XAL_PACKAGE_PATTERN.matcher(packageName);
        final int groupCount = packageMatcher.groupCount();

        final String[] parts = new String[groupCount];

        if (packageMatcher.matches()) {
            // e.g. xal
            packagePrefix = groupCount > 0 ? packageMatcher.group(1) : null;

            // e.g. extension
            componentType = groupCount > 1 ? packageMatcher.group(2) : null;

            // e.g. application
            componentName = groupCount > 2 ? packageMatcher.group(3) : null;

            // last package substring stripping the leading "."
            if (groupCount > 3) {
                final String rawSuffix = packageMatcher.group(4);
                // e.g. smf
                packageSuffix = rawSuffix != null ? rawSuffix.substring(1) : null;
            } else {
                packageSuffix = null;
            }
        } else {
            packagePrefix = null;
            componentType = null;
            componentName = null;
            packageSuffix = null;
        }
    }

    /**
     * get an instance of the package partition if it has at least the three
     * required parts (everything but suffix) or null if not
     */
    public static PackagePartition getValidInstance(final Class<?> rootClass) {
        final PackagePartition partition = new PackagePartition(rootClass);
        // if it has the component name it has prefix, type and name and thus is valid
        return partition.componentName != null ? partition : null;
    }
}
