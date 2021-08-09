//
// MacAdaptor.java: Source file for 'MacAdaptor'
// Project xal
//
// Created by t6p on 3/23/2010
//
package xal.extension.application.platform;

import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.application.Application;

/**
 * MacAdaptor provides Mac OS X specific support using reflection so it only
 * gets loaded for Mac OS X
 */
public class MacAdaptor {

    private static final Logger LOGGER = Logger.getLogger(MacAdaptor.class.getName());

    /**
     * Constructor
     */
    private MacAdaptor() {
    }

    /**
     * perform Mac initialization
     */
    // no way around it since newProxyInstance takes an array of typed Class and which isn't allowed
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initialize() {
        // display the menu bar at the top of the screen consistent with the Mac look and feel
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        try {
            // modern quit handler
            final Class<?> macQuitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");

            // modern about handler
            final Class<?> macAboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");

            // get the Mac application instance
            final Class<?> macApplicationClass = Class.forName("com.apple.eawt.Application");
            final Method appMethod = macApplicationClass.getMethod("getApplication");
            final Object macApplication = appMethod.invoke(null);

            // register the quit handler to handle Mac quit events through XAL
            final Object quitProxy = Proxy.newProxyInstance(MacAdaptor.class.getClassLoader(), new Class[]{macQuitHandlerClass}, new MacQuitHandler());
            final Method quitRegistrationMethod = macApplicationClass.getMethod("setQuitHandler", macQuitHandlerClass);
            quitRegistrationMethod.invoke(macApplication, quitProxy);

            // register the about box handler to handle Mac about box events through XAL
            final Object aboutProxy = Proxy.newProxyInstance(MacAdaptor.class.getClassLoader(), new Class[]{macAboutHandlerClass}, new MacAboutHandler());
            final Method aboutRegistrationMethod = macApplicationClass.getMethod("setAboutHandler", macAboutHandlerClass);
            aboutRegistrationMethod.invoke(macApplication, aboutProxy);
        } catch (ClassNotFoundException exception) {
            initializeFallback();
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * handle the Mac quit event
     */
    private static class MacQuitHandler implements InvocationHandler {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            try {
                final String methodName = method.getName();
                final Object response = args[1];

                // get the XAL application
                final xal.extension.application.Application xalApp = xal.extension.application.Application.getApp();

                // attempt to quit the application using the default XAL behavior
                if (methodName.equals("handleQuitRequestWith")) {
                    xalApp.quit();
                    response.getClass().getMethod("cancelQuit").invoke(response);
                }
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }

            return null;
        }
    }

    /**
     * handle the Mac about event
     */
    private static class MacAboutHandler implements InvocationHandler {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            final String methodName = method.getName();

            // show the about box if the method matches this request
            if (methodName.equals("handleAbout")) {
                Application.showAboutBox();
            }

            return null;
        }
    }

    /**
     * Perform initialization for the fallback event system. Called when the
     * modern event system is not present. This method should be removed at a
     * reasonable time in the future.
     */
    // no way around it since newProxyInstance takes an array of typed Class and which isn't allowed
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void initializeFallback() {
        try {
            // dynamically get the Mac specific extensions
            final Class<?> macApplicationClass = Class.forName("com.apple.eawt.Application");
            final Class<?> macEventListenerClass = Class.forName("com.apple.eawt.ApplicationListener");

            // get the Mac application instance for our application
            final Method appMethod = macApplicationClass.getMethod("getApplication");
            final Object macApplication = appMethod.invoke(null);

            // register our handler to handle Mac events from com.apple.eawt.ApplicationListener
            final Object proxy = Proxy.newProxyInstance(MacAdaptor.class.getClassLoader(), new Class[]{macEventListenerClass}, new MacEventHandler());
            final Method registrationMethod = macApplicationClass.getMethod("addApplicationListener", macEventListenerClass);
            registrationMethod.invoke(macApplication, proxy);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * Obsolete class to handle old style Mac Events (quit and show about box)
     * ignoring other events. This class should be removed at a reasonable time
     * in the future.
     */
    private static class MacEventHandler implements InvocationHandler {

        // no way around it since getMethod takes an array of typed Class and which isn't allowed
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            try {
                final String methodName = method.getName();
                final Object event = args[0];
                // method to indicate whether we handled the event
                final Method markMethod = event.getClass().getMethod("setHandled", Boolean.TYPE);

                // get the XAL application
                final xal.extension.application.Application xalApp = xal.extension.application.Application.getApp();

                switch (methodName) {
                    case "handleQuit":
                        // attempt to quit the application using the default XAL behavior
                        xalApp.quit();
                        // if we get to this point then we haven't quit the application
                        markMethod.invoke(event, false);
                        break;
                    case "handleAbout":
                        // display the about box
                        xal.extension.application.Application.showAboutBox();
                        markMethod.invoke(event, true);
                        break;
                    default:
                        // no other events are handled
                        markMethod.invoke(event, false);
                        break;
                }
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }

            return null;
        }
    }
}
