package xal.extension.application.rbac;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import xal.XalException;

/**
 * Static class that provides a simple interface to start a RBAC Single Sign On (SSO) server if it is not already 
 * running on the machine. SSO server is provided as a binary jar file build from ESS RBAC distribution.
 * 
 * @version 0.1 4 Jan 2016
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class SingleSignOnServerManager {

	/** Port used by RBAC SSO server */
	private static int ssoPort = 9421;

	/** SSO server jar file name */
	private static String jarName = "rbac-sso-server.jar";
	
	/**
	 * Checks to see if RBAC SSO server is running.
	 *
	 * @return <code>true</code> if SSO server is running, <code>false</code> if it is not.
	 */
	private static boolean isSSORunning() {
		ServerSocket ss = null;
	    try {
	        ss = new ServerSocket(ssoPort);
	        ss.setReuseAddress(true);
	        return false;
	    } catch (IOException e) {
	    } finally {
	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return true;
	}

	
	/**
	 * Copies a SSO server jar file from resources to the system temporary folder, as defined in 
	 * <code>java.io.tmpdir</code> property.
	 * 
	 * @throws IOException If SSO jar file cannot be deployed.
	 */
    private static void copySsoJar() throws IOException {
    	 File folder = new File(System.getProperty("java.io.tmpdir"));
         if (!folder.exists()) {
        	 folder.mkdirs();
         }
         
         File file = new File(folder, jarName);
         if (file.exists()) {
        	 return;
         }

         InputStream stream = RBACPlugin.class.getClassLoader().getResourceAsStream(jarName);
         if (stream == null) {
             throw new IOException("SSO jar is missing.");
         }

         try (FileOutputStream os = new FileOutputStream(file)) {
             byte[] b = new byte[1024];
             int len = 0;
             do {
                 len = stream.read(b);
                 if (len < 1) {
                     break;
                 }
                 os.write(b, 0, len);
             } while(len > 0);
         }
	}

	/**
	 * Starts the SSO server if it is not already running, if the server is already running this method does nothing. 
	 * SSO server binary is copied to the temporary location on the machine and is then executed in a separate process
	 * which persists even after the current process is killed. The SSO server runs on its default port 9421.
	 * 
	 * @throws XalException If SSO server cannot be started.
	 */
	public static void startSSO() throws XalException {
		// if SSO is already running there is nothing to do
		if (isSSORunning())
			return;
		
		try {
			copySsoJar();
		} catch (IOException ex) {
			throw new XalException("Failed to copy SSO server jar.");
		}
		
		final String[] commandLine = new String[] { 
				System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
				"-jar",
				System.getProperty("java.io.tmpdir") + File.separator + jarName
		};

		try {
			Runtime.getRuntime().exec(commandLine);
		} catch (Exception ex) {
			throw new XalException("Failed to execute the start SSO server.");
		}
	}

	/**
	 * This class should not be instanced.
	 */
	private SingleSignOnServerManager() { }

}
