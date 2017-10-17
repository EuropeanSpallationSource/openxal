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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
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

        Document doc = Jsoup.connect(elogUrl).get();

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
                    isValid = false;
                }
            } catch (IOException ex) {
                isValid = false;
                Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return isValid;
    }

    /**
     * Get the types of entries that can be created in a given logbook
     *
     * @param elogUrl URL to the logbook (elog + logbook)
     * @return
     */
    public static String[] getTypes(String elogUrl) {
        if (!isValidUrl(elogUrl)) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        String[] types = null;

        try {
            InputStream file = new URL(new URL(elogUrl).toExternalForm() + "?cmd=download").openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Options Type")) {
                    types = line.substring(line.indexOf("=") + 2).split(", ");
                }
            }

            reader.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return types;
    }

    /**
     * Get the categories that can be chosen for an entry in a given logbook
     *
     * @param elogUrl URL to the logbook (elog + logbook)
     * @return
     */
    public static String[] getCategories(String elogUrl) {
        if (!isValidUrl(elogUrl)) {
            throw new RuntimeException("Not a valid logbook address provided.");
        }

        String[] categories = null;

        try {
            InputStream file = new URL(new URL(elogUrl).toExternalForm() + "?cmd=download").openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Options Category")) {
                    categories = line.substring(line.indexOf("=") + 2).split(", ");
                }
            }

            reader.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return categories;
    }

    /**
     * Method to submit a new entry to a logbook.
     *
     * @param author Author name.
     * @param subject Subject of the entry.
     * @param body Text body of the message. Can be plain text, HTML, or ELCode.
     * @param category Category of the entry.
     * @param type Type of the entry.
     * @param encoding Encoding, can be plain, HTML, or ELCode.
     * @param screenshot Optional parameter to attach a screenshot from a JavaFX
     * application.
     * @param logbook Name of the logbook to be used to post.
     * @param elogUrl URL to the elog server. If null, default server is used.
     * 
     * @return Http respose code. If everything was ok, it should return 200.
     * @throws IOException
     */
    public static int submit(String author, String subject, String body,
            String category, String type, String encoding,
            WritableImage screenshot, String logbook, String elogUrl) throws IOException {

        if (category == null) {
            category = "General";
        }
        if (type == null) {
            type = "Routine";
        }

        URL elog = null;
        try {
            if (elogUrl == null) {
                elog = new URL("http://elog.esss.lu.se/");

            } else {
                elog = new URL(elogUrl);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
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
        BufferedWriter httpRequestBodyWriter
                = new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

        if (author == null || subject == null || body == null) {
            throw new Error("Missing one of the following fields: author, subject or body.");
        }

        // Specify command (Submit)
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"cmd\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write("Submit");
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        // Specify author
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"Author\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write(author);
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        // Specify type
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"Type\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write(type);
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        // Specify category
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"Category\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write(category);
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        if (encoding != null) {
            if (encoding.equals("plain") || encoding.equals("HTML") || encoding.equals("ELCode")) {
                // Specify encoding
                httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"encoding\"");
                httpRequestBodyWriter.write("\r\n\r\n");
                httpRequestBodyWriter.write(encoding);
                httpRequestBodyWriter.write("\r\n");
                httpRequestBodyWriter.flush();
            } else {
                try {
                    throw new Error("Invalid message encoding. Valid options: plain, HTML, ELCode.");
                } catch (Exception ex) {
                    Logger.getLogger(Jelog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Specify subject
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"Subject\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write(subject);
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        // Specify text
        httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"Text\"");
        httpRequestBodyWriter.write("\r\n\r\n");
        httpRequestBodyWriter.write(body);
        httpRequestBodyWriter.write("\r\n");
        httpRequestBodyWriter.flush();

        // Writes the attached image (if available)
        if (screenshot != null) {
            httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
            httpRequestBodyWriter.write("Content-Disposition: form-data;"
                    + "name=\"attfile\";"
                    + "filename=\"screenshot.png\""
                    + "\r\nContent-Type: image/png\r\n");
            httpRequestBodyWriter.write("Content-Transfer-Encoding: binary\r\n\r\n");
            httpRequestBodyWriter.flush();

            byte[] imageInByte;
            try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
                ImageIO.write(SwingFXUtils.fromFXImage(screenshot, null), "png", byteOutput);
                byteOutput.flush();
                imageInByte = byteOutput.toByteArray();
            }

            outputStreamToRequestBody.write(imageInByte);
            outputStreamToRequestBody.flush();
        }

        // Mark the end of the multipart http request
        httpRequestBodyWriter.write("\r\n--" + boundaryString + "--\r\n");
        httpRequestBodyWriter.flush();

        // Close the streams
        outputStreamToRequestBody.close();
        httpRequestBodyWriter.close();

        Integer responseCode = urlConnection.getResponseCode();
        if (responseCode != 200) {
            throw new Error("Error " + responseCode.toString()
                    + " received when trying to submit the new entry.");
        }

        // Get message ID of the new entry
        urlConnection.getInputStream().close();
        int indexId = urlConnection.getURL().getFile().lastIndexOf('/') + 1;
        int messageId = Integer.parseInt(urlConnection.getURL().getFile().substring(indexId));
        
        return messageId;
    }
}
