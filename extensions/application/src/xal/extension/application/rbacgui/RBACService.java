package xal.extension.application.rbacgui;

import java.awt.Component;

import se.esss.ics.rbac.access.Credentials;
import xal.rbac.AccessDeniedException;
import xal.rbac.RBACException;
import xal.rbac.RBACLogin;
import xal.rbac.RBACSubject;

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
        Credentials credentials = null;
        credentials = pane.getCredentials();
        System.out.println("Credentials recieved.");
        try {
            subject = login.authenticate(credentials.getUsername(), credentials.getPassword());
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
        resource = "Xal" + resource.replace(" ", "");
        System.out.println("User asking for permisson: " + permission + " on resource: " + resource);
        try {
            boolean grant = subject.hasPermission(resource, permission);
            if(grant){
                System.out.println("Permission granted");
            }else{
                System.out.println("Permission denied");
            }
            return grant;
        } catch (AccessDeniedException | RBACException e) {
            System.out.println("Authorization failed.");
            return false;
        }
       
    }
}
