package xal.extension.application.rbacgui;

import java.awt.Component;

import se.esss.ics.rbac.access.Credentials;
import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.RBACException;
import xal.extension.application.rbac.RBACLogin;
import xal.extension.application.rbac.RBACSubject;

public class RBACService {
    
    private static RBACLogin login;
    private static RBACSubject subject;

    public static void initialize(){
        login = RBACLogin.newRBACLogin();
    }
    
    public static boolean authenticate(){
        System.out.println("Starting authentication.");
        AuthenticationPane pane = new AuthenticationPane();
        pane.createDialog((Component) null).setVisible(true);
        Credentials[] credentials = null;
        credentials[0] = pane.getCredentials();
        System.out.println("Credentials recieved.");
        try {
            subject = login.authenticate(credentials[0].getUsername(), credentials[0].getPassword());
            System.out.println("Authentication successful.");
            return true;
        } catch (AccessDeniedException | RBACException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Authentication failed.");
        return false;
    }
    
    public static boolean authorize(String resource, String permission){
        System.out.println("Starting authorization.");
        try {
            return subject.hasPermission("Xal" + resource.replace(" ", ""), permission);
        } catch (AccessDeniedException | RBACException e) {
            System.out.println("Authorization failed.");
            return false;
        }
       
    }
}
