/*
 * Copyright (c) 2017, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.extension.jelog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Library to post new entries to elog. For the moment, it only implements
 * posting new entries with a single image as attachment. In the future it may
 * implement edit, reply, delete, and other types of attachments.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class Jelog {

    private final static char[] MULTIPART_CHARS
            = "_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    /**
     * Method to generate a random Boundary String for the HTTP message.
     *
     * @return Boundary String
     */
    protected static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    /**
     * This method returns a list with the available logbooks. Be aware that
     * spaces in the name must be replaced by + signs in the URL.
     *
     * @param elogUrl Url of the elog server
     * @return List with logbook names
     * @throws java.io.IOException
     */
    public static List<String> getLogbooks(String elogUrl) throws IOException {
        if (!isValidUrl(elogUrl)) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        Document doc = Jsoup.connect((elogUrl.endsWith("/") ? elogUrl : elogUrl + '/') + "?gexp=all").get();

        List<String> logbooks = new ArrayList<>();
        for (Element element : doc.getElementsByClass("sellogbook")) {
            logbooks.add(element.getElementsByTag("a").get(0).ownText());
        }

        // If empty, resolves the redirected URL to the unique logbook and 
        // extracts its name
        if (logbooks.isEmpty()) {
            URLConnection connection = new URL(elogUrl).openConnection();
            connection.connect();
            connection.getInputStream().close();
            String logbookname = connection.getURL().getFile().replace("/", "");
            logbooks.add(logbookname);
        }

        return logbooks;
    }

    /**
     * Checks if the URL is a valid elog server, logbook or entry.
     *
     * @param elogUrl
     * @return
     */
    public static Boolean isValidUrl(String elogUrl) {
        Boolean isValid = true;

        // If logook does not exists, error 404 is returned by the server.
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(elogUrl).openConnection();
            connection.setConnectTimeout(1000);
            connection.connect();
            connection.disconnect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                isValid = false;
            }
        } catch (SocketTimeoutException ex) {
            isValid = false;
        } catch (IOException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If an entry does not exists, a webpage with an error messaged is
        // received.
        if (isValid) {
            Document doc;
            try {
                doc = Jsoup.connect(elogUrl).get();
                for (Element element : doc.getElementsByClass("errormsg")) {
                    if (!element.text().equals("No entries found")) {
                        isValid = false;
                    }
                }
            } catch (IOException ex) {
                isValid = false;
                Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return isValid;
    }

    /**
     *
     *
     * @param elogUrl URL to the elog server
     * @param logbookGroup
     * @param logbook
     * @return
     */
    public static HashMap<String, LogbookAttribute> getLogbookAttributes(String elogUrl, String logbookGroup, String logbook) {
        String[] attributes = null;
        String[] requiredAttributes = null;
        String[] lockedAttributes = null;

        HashMap<String, LogbookAttribute> attributesMap = new HashMap();

        try {
            if (!isValidUrl(elogUrl) || !isValidUrl(new URL(new URL(elogUrl), logbookGroup).toString())
                    || !isValidUrl(new URL(new URL(elogUrl), logbook).toString())) {
                throw new RuntimeException("Not a valid logbook address provided.");
            }

            InputStream file = new URL((elogUrl.endsWith("/") ? elogUrl : elogUrl + '/') + "?cmd=GetConfig").openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(file));

            String line;

            if (logbookGroup != null) {
                boolean readingSettings = false;

                // Logbook settings will overwrite global settings, since they come after
                while ((line = reader.readLine()) != null) {
                    if (!readingSettings) {
                        if (line.startsWith("[global " + logbookGroup + "]")
                                || line.startsWith("[" + logbook + "]")) {
                            readingSettings = true;
                        }
                    } else {
                        if (line.startsWith("[") && !(line.startsWith("[global " + logbookGroup + "]")
                                || line.startsWith("[" + logbook + "]"))) {
                            readingSettings = false;
                            continue;
                        }

                        if (line.startsWith("Attributes")) {
                            attributes = line.substring(line.indexOf("=") + 2).trim().split(",[\\s]*");
                            for (String attribute : attributes) {
                                if (!attributesMap.containsKey(attribute)) {
                                    attributesMap.put(attribute, LogbookAttribute.newEmpty());
                                }
                            }
                        } else if (line.startsWith("Required Attributes")) {
                            requiredAttributes = line.substring(line.indexOf("=") + 2).trim().split(",[\\s]*");
                        } else if (line.startsWith("Locked Attributes")) {
                            lockedAttributes = line.substring(line.indexOf("=") + 2).trim().split(",[\\s]*");
                        } else if (line.startsWith("Options ")) {
                            String optionName = line.substring("Options ".length(), line.indexOf("=") - 1);
                            String[] options = line.substring(line.indexOf("=") + 2).trim().replaceAll("\\{(.)\\}", "").split(",[\\s]*");
                            if (!attributesMap.containsKey(optionName)) {
                                attributesMap.put(optionName, LogbookAttribute.newEmpty());
                            }
                            attributesMap.get(optionName).setOptions(options);
                        } else if (line.startsWith("MOptions ") || line.startsWith("ROptions ")) {
                            String optionName = line.substring(line.indexOf("Options") + "Options ".length(), line.indexOf("=") - 1);
                            String[] options = line.substring(line.indexOf("=") + 2).trim().replaceAll("\\{(.)\\}", "").split(",[\\s]*");
                            if (!attributesMap.containsKey(optionName)) {
                                attributesMap.put(optionName, LogbookAttribute.newEmpty());
                            }
                            attributesMap.get(optionName).setMultioption(true);
                            attributesMap.get(optionName).setOptions(options);
                        } else if (line.startsWith("Preset ")) {
                            String optionName = line.substring("Preset ".length(), line.indexOf("=") - 1);
                            // Skipping default template
                            if (attributesMap.containsKey(optionName)) {
                                attributesMap.get(optionName).setOptions(new String[]{logbook});
                            }
                        }
                    }
                }

                // Remove unused attributes (if the ones inherited from the global configuration are overwriten)
                HashMap<String, LogbookAttribute> newAttributesMap = new HashMap();
                if (attributes != null) {
                    for (String attribute : attributes) {
                        newAttributesMap.put(attribute, attributesMap.get(attribute));
                    }
                }
                attributesMap = newAttributesMap;

                // Set the required flag to true for the locked parameters
                if (lockedAttributes != null) {
                    for (String lockedAttribute : lockedAttributes) {
                        attributesMap.get(lockedAttribute).setLocked(true);
                    }
                }

                // Set the required flag to true for the required parameters
                if (requiredAttributes != null) {
                    for (String requiredAttribute : requiredAttributes) {
                        attributesMap.get(requiredAttribute).setRequired(true);
                    }
                }
            }

            reader.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return attributesMap;
    }

    /**
     * Method to submit a new entry to a logbook.
     *
     * @param fields
     * @param body Text body of the message. Can be plain text, HTML, or ELCode.
     * @param encoding Encoding, can be plain, HTML, or ELCode.
     * @param screenshots Optional parameter to attach screenshots from a JavaFX
     * application.
     * @param attachments
     * @param logbook Name of the logbook to be used to post.
     * @param elogUrl URL to the elog server. If null, default server is used.
     * @param uname User name
     * @param upwd Password hash (SHA-256)
     *
     * @return Http respose code. If everything was ok, it should return 200.
     * @throws IOException
     */
//    public static int submit(String author, String subject, String body,
//            String category, String type, String sections, String subsections,
//            String disciplines, String deviceGroups, String special, String encoding,
//            WritableImage[] screenshots, Attachment[] attachments, String logbook,
//            String elogUrl, String uname, String upwd) throws IOException {
    public static int submit(HashMap<String, String> fields, String body, String encoding,
            WritableImage[] screenshots, Attachment[] attachments, String logbook,
            String elogUrl, String uname, String upwd) throws IOException {

        URL elog = null;
        try {
            if (elogUrl == null) {
                elog = new URL("https://logbook.esss.lu.se/");

            } else {
                elog = new URL(elogUrl);

            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        // Append the logbook name to the address
        elog = new URL(elog, logbook);

        if (!isValidUrl(elog.toString())) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        // Connect to the web server endpoint
        HttpURLConnection urlConnection = (HttpURLConnection) elog.openConnection();

        String boundaryString = "-----" + Jelog.generateBoundary();

        // Indicate that we want to write to the HTTP request body
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

        OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
        BufferedWriter httpsRequestBodyWriter
                = new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody, "ISO-8859-1"));

        // Specify command (Submit)
        sendPartPost(httpsRequestBodyWriter, "cmd", "Submit", boundaryString);

        if (uname != null && upwd != null) {
            // Specify user name
            sendPartPost(httpsRequestBodyWriter, "unm", uname, boundaryString);
            // Specify password
            sendPartPost(httpsRequestBodyWriter, "upwd", upwd, boundaryString);
        }

        // Send all other fields
        for (String field : fields.keySet()) {
            sendPartPost(httpsRequestBodyWriter, field.replaceAll(" ", "_"), fields.get(field), boundaryString);
        }

        if (encoding != null) {
            if (encoding.equals("plain") || encoding.equals("HTML") || encoding.equals("ELCode")) {
                // Specify encoding
                sendPartPost(httpsRequestBodyWriter, "encoding", encoding, boundaryString);
            } else {
                try {
                    throw new Error("Invalid message encoding. Valid options: plain, HTML, ELCode.");

                } catch (Exception ex) {
                    Logger.getLogger(Jelog.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Specify text
        sendPartPost(httpsRequestBodyWriter, "Text", body, boundaryString);

        // Write the attached images (if available)
        if (screenshots != null) {
            int i = 0;
            for (WritableImage screenshot : screenshots) {
                if (screenshot != null) {
                    httpsRequestBodyWriter.write("--" + boundaryString + "\r\n");
                    httpsRequestBodyWriter.write("Content-Disposition: form-data;"
                            + "name=\"attfile\";"
                            + "filename=\"screenshot_" + Integer.toString(i) + ".png\""
                            + "\r\nContent-Type: image/png\r\n");
                    httpsRequestBodyWriter.write("Content-Transfer-Encoding: binary\r\n\r\n");
                    httpsRequestBodyWriter.flush();

                    byte[] imageInByte;
                    try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
                        ImageIO.write(SwingFXUtils.fromFXImage(screenshot, null), "png", byteOutput);
                        byteOutput.flush();
                        imageInByte = byteOutput.toByteArray();
                    }

                    outputStreamToRequestBody.write(imageInByte);
                    outputStreamToRequestBody.flush();

                    i++;
                }
            }
        }

        // Write other attachments (if available)
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                if (attachment != null) {
                    httpsRequestBodyWriter.write("--" + boundaryString + "\r\n");
                    httpsRequestBodyWriter.write("Content-Disposition: form-data;"
                            + "name=\"attfile\";"
                            + "filename=\"" + attachment.getFileName() + "\""
                            + "\r\nContent-Type: image/png\r\n");
                    httpsRequestBodyWriter.write("Content-Transfer-Encoding: binary\r\n\r\n");
                    httpsRequestBodyWriter.flush();

                    int d;
                    while ((d = attachment.getFileContent().read()) != -1) {
                        outputStreamToRequestBody.write(d);
                    }

                    outputStreamToRequestBody.flush();
                }
            }
        }

        // Mark the end of the multipart http request
        httpsRequestBodyWriter.write("\r\n--" + boundaryString + "--\r\n");
        httpsRequestBodyWriter.flush();

        // Close the streams
        outputStreamToRequestBody.close();
        httpsRequestBodyWriter.close();

        Integer responseCode = urlConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Error("Error " + responseCode.toString()
                    + " received when trying to submit the new entry.");
        }

        // Get message ID of the new entry
        urlConnection.getInputStream().close();

        String entryURL = urlConnection.getURL().getFile();

        // Strip any error message
        int indexQM = entryURL.lastIndexOf('?');
        if (indexQM != -1) {
            entryURL = entryURL.substring(0, indexQM);
        }

        int indexId = entryURL.lastIndexOf('/') + 1;

        int messageId = Integer.parseInt(entryURL.substring(indexId));

        return messageId;
    }

    public static void setTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            //We can not recover from this exception.
            e.printStackTrace();

        }
    }

    /**
     * Method to log in. It will generate a cookie that can be used later for
     * posting new entries.
     *
     * @param username User name.
     * @param password In clear text, no hash.
     * @param remember Set to true to remember login for 31 days.
     * @param elogUrl URL of the elog server. If not provided, default server is
     * used.
     * @throws IOException
     */
    public static void login(String username, char[] password, boolean remember, String elogUrl) throws IOException {
        URL elog = null;
        try {
            if (elogUrl == null) {
                elog = new URL("https://logbook.esss.lu.se/");

            } else {
                elog = new URL(elogUrl);

            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        List<String> groups = Jelog.getLogbooks(elogUrl);
        List<String> logbooks = Jelog.getLogbooks(new URL(elog, groups.get(0)).toString());

        // Append the logbook name to the address
        elog = new URL(elog, logbooks.get(0));

        if (!isValidUrl(elog.toString())) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        // Setting the default cookie manager
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        // Connect to the web server endpoint
        HttpURLConnection urlConnection = (HttpURLConnection) elog.openConnection();

        String boundaryString = "-----" + Jelog.generateBoundary();

        // Indicate that we want to write to the HTTP request body
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

        OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
        BufferedWriter httpsRequestBodyWriter
                = new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody, "ISO-8859-1"));

        // Specify redir parameter
        sendPartPost(httpsRequestBodyWriter, "redir", "", boundaryString);

        // Specify user name
        sendPartPost(httpsRequestBodyWriter, "uname", username, boundaryString);

        // Specify password
        sendPasswordPost(httpsRequestBodyWriter, "upassword", password, boundaryString);

        // Specify remember
        if (remember) {
            sendPartPost(httpsRequestBodyWriter, "remember", "1", boundaryString);
        }

        // Mark the end of the multipart http request
        httpsRequestBodyWriter.write("\r\n--" + boundaryString + "--\r\n");
        httpsRequestBodyWriter.flush();

        // Close the streams
        outputStreamToRequestBody.close();
        httpsRequestBodyWriter.close();

        Integer responseCode = urlConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Error("Error " + responseCode.toString()
                    + " received when trying to submit the new entry.");
        }

        BufferedReader login_response = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        while ((line = login_response.readLine()) != null) {
            if (line.contains("dlgerror")) {
                throw new Error("Invalid user name or password!");
            }
        }

        login_response.close();
    }
    
    public static void logout(){
        CookieManager manager = (CookieManager) CookieHandler.getDefault();
        manager.getCookieStore().removeAll();
    }

    /**
     * If the right cookie is available, this method returns the user full name,
     * username and password hash (SHA-256).
     *
     * @param logbookUrl
     * @return String[] with user's full name, username, and password.
     * @throws IOException
     */
    public static String[] retrieveUsernameAndPassword(String logbookUrl) throws IOException {
        String author = null;
        String username = null;
        String passwordHash = null;

        if (!isValidUrl(logbookUrl)) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        String elog = (logbookUrl.endsWith("/") ? logbookUrl : logbookUrl + '/') + "?cmd=New";

        Document doc;
        try {
            doc = Jsoup.connect(elog).get();
            for (Element element : doc.getElementsByAttribute("name")) {
                switch (element.attr("name")) {
                    case "Author":
                        author = element.attr("value");
                        break;
                    case "unm":
                        username = element.attr("value");
                        break;
                    case "upwd":
                        passwordHash = element.attr("value");
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new String[]{author, username, passwordHash};
    }

    /**
     * Method to submit a part of a multi-part post http request
     *
     * @param writer
     * @param name
     * @param value
     * @param boundaryString
     * @throws IOException
     */
    private static void sendPartPost(BufferedWriter writer, String name, String value, String boundaryString) throws IOException {
        writer.write("--" + boundaryString + "\r\n");
        writer.write("Content-Disposition: form-data; name=\"" + name + "\"");
        writer.write("\r\n\r\n");
        writer.write(value);
        writer.write("\r\n");
        writer.flush();
    }

    private static void sendPasswordPost(BufferedWriter writer, String name, char[] value, String boundaryString) throws IOException {
        writer.write("--" + boundaryString + "\r\n");
        writer.write("Content-Disposition: form-data; name=\"" + name + "\"");
        writer.write("\r\n\r\n");
        for (char c : value) {
            writer.write(c);
        }
        writer.write("\r\n");
        writer.flush();
    }
    
}
