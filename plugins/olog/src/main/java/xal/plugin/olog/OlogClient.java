/*
 * Copyright (C) 2021 European Spallation Source ERIC.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.plugin.olog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;
import org.json.JSONArray;
import xal.extension.logbook.LogbookException;
import xal.tools.apputils.Preferences;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class OlogClient {

    private static final Logger LOGGER = Logger.getLogger(OlogClient.class.getName());

    private static OlogClient client;
    private String serverUrl;
    private final static char[] MULTIPART_CHARS
            = "_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();
    private static final String LOGBOOK_ENTRY_TYPE_PROPERTY = "entryTypes";

    private static String credentials;
    private String username;

    private static HttpClient httpClient;
    private static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
    };

    private OlogClient() {
    }

    public static OlogClient getClient() {
        if (client == null) {
            client = new OlogClient();

            // Setting the default cookie manager
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());

                httpClient = HttpClient.newBuilder()
                        .cookieHandler(CookieHandler.getDefault())
                        .sslContext(sslContext)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                Logger.getLogger(OlogClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return client;
    }

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

    public boolean login(String username, char[] password) throws OlogUnauthorizedException, LogbookException {
        setCredentials(username, password);
        String boundaryString = "---" + generateBoundary();

        StringBuilder body = new StringBuilder();
        body.append("--").append(boundaryString).append("\r\n");
        body.append("Content-Disposition: form-data; name=\"username\"\r\n\r\n");
        body.append(username).append("\r\n");
        body.append("--").append(boundaryString).append("\r\n");
        body.append("Content-Disposition: form-data; name=\"password\"\r\n\r\n");
        body.append(String.copyValueOf(password)).append("\r\n");
        body.append("--").append(boundaryString).append("--").append("\r\n");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(new URL(serverUrl), "login").toURI())
                    .header("Content-Type", "multipart/form-data; boundary=" + boundaryString)
                    .header("Authorization", credentials)
                    .method("POST", HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                logout();
                return false;
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            Logger.getLogger(OlogClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(OlogClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public String getUserLogedIn() {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URL(new URL(serverUrl), "user").toURI())
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return username;
            } else {
                return null;
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            Logger.getLogger(OlogClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(OlogClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void setCredentials(String username, char[] password) {
        this.username = username;
        String auth = username + ":" + String.copyValueOf(password);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));

        credentials = "Basic " + new String(encodedAuth);
    }

    public void logout() {
        credentials = null;
        username = null;
    }

    public boolean isLoggedIn() {
        return credentials != null;
    }

    public void setServerUrl(String url) {
        serverUrl = url;
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        serverUrl += "Olog/";
    }

    public List<String> getLogbooks() throws LogbookException {
        List<String> logbooks = new ArrayList<>();
        JSONArray responseJson = new JSONArray(httpGet("logbooks"));

        for (Object logbook : responseJson) {
            logbooks.add(((JSONObject) logbook).getString("name"));
        }

        return logbooks;
    }

    public List<JSONObject> getLogEntries() throws LogbookException {
        List<JSONObject> logEntries = new ArrayList<>();
        JSONArray responseJson = new JSONArray(httpGet("logs"));

        for (Object logEntry : responseJson) {
            logEntries.add((JSONObject) logEntry);
        }

        return logEntries;
    }

    public List<JSONObject> getLogEntries(String searchString) throws LogbookException {
        List<JSONObject> logEntries = new ArrayList<>();
        JSONArray responseJson = new JSONArray(httpGet("logs?" + searchString));

        for (Object logEntry : responseJson) {
            logEntries.add((JSONObject) logEntry);
        }

        return logEntries;
    }

    public List<String> getTags() throws LogbookException {
        List<String> tags = new ArrayList<>();
        JSONArray responseJson = new JSONArray(httpGet("tags"));

        for (Object tag : responseJson) {
            tags.add(((JSONObject) tag).getString("name"));
        }

        return tags;
    }

    public List<String> getEntryTypeList() {
        List<String> entryType = new ArrayList<>();

        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(this.getClass());
        String defaultTypes = defaults.get(LOGBOOK_ENTRY_TYPE_PROPERTY, "");

        if (defaultTypes.strip().equals("")) {
            entryType.addAll(List.of("Normal", "Shift Start", "Shift End", "Fault", "Beam Loss", "Beam Configuration", "Crew", "Expert Intervention Call"));
        } else {
            String[] split = defaultTypes.split(",");
            entryType.addAll(Arrays.asList(split));
        }

        return entryType;
    }

    public Map<String, List<String>> getProperties() throws LogbookException {
        Map<String, List<String>> properties = new HashMap<>();
        JSONArray responseJson = new JSONArray(httpGet("properties"));

        for (Object property : responseJson) {
            JSONObject p = (JSONObject) property;
            List<String> attributes = new ArrayList<>();
            for (Object att : p.getJSONArray("attributes")) {
                attributes.add(((JSONObject) att).getString("name"));
            }
            properties.put(p.getString("name"), attributes);
        }

        return properties;
    }

    private String httpGet(String command) throws LogbookException {
        try {
            URL url = new URL(new URL(serverUrl), command);
            // Connect to the web server endpoint
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            }
            // If the response code is not OK, then throw an exception.
            throw new LogbookException("HTTP GET failed for URL: " + url.toString() + " with response code " + responseCode, responseCode);
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    public String submitEntry(LogEntry log) throws LogbookException, OlogUnauthorizedException {
        String jsonEntry = httpPut("logs", log.toJson());

        if (log.hasAttachments()) {
            Long logId = new JSONObject(jsonEntry).getLong("id");
            for (OlogAttachment attachment : log.getAttachments()) {
                jsonEntry = postAttachment(logId, attachment);
            }
        }

        return jsonEntry;
    }

    private String httpPut(String command, String params) throws LogbookException, OlogUnauthorizedException {
        if (credentials == null) {
            throw new LogbookException("User is not logged in.");
        }

        try {
            URL url = new URL(new URL(serverUrl), command);
            // Connect to the web server endpoint
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("PUT");

            urlConnection.setRequestProperty("Authorization", credentials);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            urlConnection.setDoOutput(true);
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                return response.toString();
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new OlogUnauthorizedException();
            }
            // If the response code is not OK, then throw an exception.     
            throw new LogbookException("HTTP PUT failed for URL: " + url.toString());
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    private String postAttachment(Long logId, OlogAttachment attachment) throws LogbookException, OlogUnauthorizedException {
        try {
            String boundaryString = "-----" + generateBoundary();

            URL url = new URL(new URL(serverUrl), "logs/attachments/" + logId.toString());
            // Connect to the web server endpoint
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

            urlConnection.setRequestProperty("Authorization", credentials);

            urlConnection.setDoOutput(true);
            try (OutputStream os = urlConnection.getOutputStream(); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "ISO-8859-1"))) {
                writer.write("--" + boundaryString + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"filename\"\r\n");
                writer.write("Content-Type: application/json\r\n\r\n");
                writer.write(attachment.getFileName() + "\r\n");

                writer.write("--" + boundaryString + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"fileMetadataDescription\"\r\n");
                writer.write("Content-Type: application/json\r\n\r\n");
                writer.write(attachment.getMimeType() + "\r\n");

                if (attachment.hasUuid()) {
                    writer.write("--" + boundaryString + "\r\n");
                    writer.write("Content-Disposition: form-data; name=\"id\"\r\n");
                    writer.write("Content-Type: application/json\r\n\r\n");
                    writer.write(attachment.getUuid() + "\r\n");
                }

                writer.write("--" + boundaryString + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"" + attachment.getFileName() + "\"\r\n");
                writer.write("Content-Type: application/octet-steam\r\n\r\n");
                writer.flush();
                attachment.getContent().writeTo(os);
                os.flush();
                writer.write("\r\n");

                // Mark the end of the multipart http request
                writer.write("\r\n--" + boundaryString + "--\r\n");
                writer.flush();
            }

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                return response.toString();
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new OlogUnauthorizedException();
            }

            // If the response code is not OK, then throw an exception.
            throw new LogbookException("HTTP POST failed for URL: " + url.toString());
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }
}
