package xal.extension.application.rbacgui;

import java.awt.Component;

import se.esss.ics.rbac.access.Credentials;
import xal.rbac.AccessDeniedException;
import xal.rbac.RBACException;
import xal.rbac.RBACLogin;
import xal.rbac.RBACSubject;

/**
 * RBACService is a gateway for communication between application and RBAC modules. 
 * 
 * @version 0.1 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class RBACService {
    
    private static RBACLogin login = RBACLogin.newRBACLogin();
    private static RBACSubject subject;
    
    /**
     * Method for authenticating user.
     * 
     * <p>
     * Asks user for credentials and then calls RBACLogin specified with package in pom and saves subject for later use in authorization.
     * </p>
     * @return true if authentication was successful and false if not or if user canceled. 
     */
    public static synchronized boolean authenticate(){
        System.out.println("Starting authentication.");
        //Getting information from user.
        AuthenticationPane pane = new AuthenticationPane();
        pane.createDialog((Component) null).setVisible(true);
        Credentials credentials = null;
        credentials = pane.getCredentials();
        if(credentials == null){
            //User pressed cancel
            System.out.println("Canceling...");
            subject = null;
            return false;
        }
        try {
            subject = login.authenticate(credentials.getUsername(), credentials.getPassword());
            System.out.println("Authentication successful.");
            return true;
        } catch (RBACException e) {
            System.err.println("Error while authenticating.");
            e.printStackTrace();
        }catch(AccessDeniedException e){
            System.out.println("Access denied.");
        }
        System.out.println("Authentication failed.");
        return false;
        
    }
    
    /**
     * Method for authorizating user.
     * 
     * <p>
     * Takes subject received when authenticating and asks for permissions.
     * If user logged out in the mean time asks user to auhenticate again and retires.
     * </p>
     * @param resource to authorizate for.
     * @param permission to authorizate for.
     * @return true if permission is granted, false if user is not authenticated or authorization failed.
     */
    public static synchronized boolean authorize(String resource, String permission){
        if(subject == null){
            System.err.println("Authenticate first.");
            return false;
        }
        System.out.println("Starting authorization.");
        //Setting name of the application
        resource = "Xal" + resource.replace(" ", "");//CHECK Currently set for openxal applications only!!!
        System.out.println("User asking for permisson: " + permission + " on resource: " + resource);
        try {
            boolean grant = subject.hasPermission(resource, permission);
            if(grant){
                System.out.println("Permission granted");
            }else{
                System.out.println("Permission denied");
            }
            return grant;
        } catch (RBACException e) {
            System.out.println("Authorization failed.");
            return false;
        }catch(AccessDeniedException e){
            System.out.println("User logged out.");
            System.out.println("Please log in again.");
            //We let user log in again
            authenticate();
            //We try to authorize again.
            return authorize(resource, permission);
        }
       
    }
}
