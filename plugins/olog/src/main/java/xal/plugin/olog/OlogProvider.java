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

import eu.ess.xaos.tools.annotation.ServiceProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import xal.extension.logbook.Attachment;
import xal.extension.logbook.LogbookException;
import xal.extension.logbook.LogbookProvider;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
@ServiceProvider(service = LogbookProvider.class, order = 10)
public class OlogProvider extends LogbookProvider {

    public static final String ENTRY_TYPE_STR = "Entry Type";
    public static final String TAGS_STR = "Tags";
    public static final String SUBJECT_STR = "Subject";
    public static final String PROPERTIES_STR = "Properties";

    private OlogClient client = OlogClient.getClient();

    @Override
    public boolean login(String username, char[] password) {
        client.setCredentials(username, password);

        return true;
    }

    @Override
    public void logout() {
        client.forgetCredentials();
    }

    @Override
    public long post(String[] logbooks, Map<String, List<String>> fields, String textBody) throws LogbookException {
        return post(logbooks, fields, textBody, new ArrayList<>());
    }

    /**
     *
     * @param logbooks Array with a list of logbooks where the entry should be
     * posted.
     * @param fields Olog allows for the following attributes: Entry Type, Tags,
     * Subject, and Properties. From those, Entry Type and Subject are required,
     * the others are optional.
     * @param textBody Message body test.
     * @param attachments
     * @return
     * @throws LogbookException
     */
    @Override
    public long post(String[] logbooks, Map<String, List<String>> fields, String textBody, List<Attachment> attachments) throws LogbookException {
        OlogClient client = OlogClient.getClient();
        client.setServerUrl(getServer());

        if (!fields.containsKey(SUBJECT_STR)) {
            throw new LogbookException("Missing Subject field");
        }
        if (!fields.containsKey(ENTRY_TYPE_STR)) {
            throw new LogbookException("Missing Entry Type field");
        }

        LogEntry log = new LogEntry(fields.get("Subject").get(0), textBody, fields.get(ENTRY_TYPE_STR).get(0), List.of(logbooks));

        if (fields.containsKey(TAGS_STR)) {
            log.addTags(fields.get(TAGS_STR));
        }

        if (fields.containsKey(PROPERTIES_STR)) {
            for (String property : fields.get(PROPERTIES_STR)) {
                Pair<String, Map<String, String>> p = parseProperty(property);
                log.addProperty(new Property(p.getKey(), p.getValue()));
            }
        }

        for (Attachment att : attachments) {
            log.addAttachment(new OlogAttachment(att));
        }

        try {
            String jsonEntry = client.submitEntry(log);
            return new JSONObject(jsonEntry).getLong("id");
        } catch (JSONException | LogbookException | OlogUnauthorizedException e) {
            System.out.println("Exception in submitting entry");
        }

        return 0L;
    }

    @Override
    public long post(Map<String, List<String>> defaultAttributes) throws LogbookException {
        return post(null, null, defaultAttributes);

    }

    @Override
    public long post(String[] defaultLogbooks, Map<String, List<String>> defaultAttributes) throws LogbookException {
        return post(null, defaultLogbooks, defaultAttributes);
    }

    @Override
    public long post(List<Attachment> attachments, Map<String, List<String>> defaultAttributes) throws LogbookException {
        return post(attachments, null, defaultAttributes);
    }

    @Override
    public long post(List<Attachment> attachments, String[] defaultLogbooks,
            Map<String, List<String>> defaultAttributes) throws LogbookException {
        // Set server URL in Olog client class.
        client.setServerUrl(getServer());
        
        Stage stage = null;

        FXMLLoader fxmlLoader = new FXMLLoader(OlogPostEntryController.class.getResource("/fxml/OlogPostEntryScene.fxml"));

        Parent root;
        try {
            root = (Parent) fxmlLoader.load();
        } catch (LoadException ex) {
            // Issues with configuration. The Controller will show an error dialog.
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error while trying to post an entry in the logbook.");
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, null, ex);
            return 0L;
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }

        OlogPostEntryController controller = fxmlLoader.<OlogPostEntryController>getController();

        if (defaultLogbooks != null && defaultLogbooks.length != 0) {
            controller.setDefaultLogbooks(defaultLogbooks);
        }

        if (!defaultAttributes.isEmpty()) {
            for (Entry<String, List<String>> attribute : defaultAttributes.entrySet()) {
                controller.addDefaultAttribute(attribute.getKey(), attribute.getValue());
            }
        }

        if (attachments != null) {
            controller.setAttachments(attachments);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(OlogPostEntryController.class.getResource("/styles/olog.css").toExternalForm());

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("ESS Logbook");
        stage.setScene(scene);

        stage.minHeightProperty().bind(((Region) root).minHeightProperty().add(stage.getHeight() - ((Region) root).getHeight()));

        stage.showAndWait();

        return controller.getLogId();
    }

    protected static Pair parseProperty(String property) {
        String name = getJsonAttribute("name", property);
        String attributes = getJsonAttribute("attributes", property);

        Map<String, String> attributeMap = getAttributeMap(attributes);

        return new Pair(name, attributeMap);
    }

    private static String getJsonAttribute(String attName, String jsonString) {
        int nameIdx = jsonString.indexOf("\"" + attName + "\":");
        int valueEndIdx;
        int valueStartPar = jsonString.indexOf("\"", nameIdx + ("\"" + attName + "\":").length());
        int valueStartBra = jsonString.indexOf("[", nameIdx + ("\"" + attName + "\":").length());
        int valueStartCurl = jsonString.indexOf("{", nameIdx + ("\"" + attName + "\":").length());

        if (valueStartPar != -1 && (valueStartPar < valueStartBra || valueStartBra == -1) && (valueStartPar < valueStartCurl || valueStartCurl == -1)) {
            valueEndIdx = jsonString.indexOf("\"", valueStartPar + 1);
            return jsonString.substring(valueStartPar + 1, valueEndIdx);
        } else if (valueStartBra != -1 && (valueStartBra < valueStartPar || valueStartPar == -1) && (valueStartBra < valueStartCurl || valueStartCurl == -1)) {
            valueEndIdx = jsonString.indexOf("]", valueStartBra + 1);
            return jsonString.substring(valueStartBra + 1, valueEndIdx);
        } else if (valueStartCurl != -1 && (valueStartCurl < valueStartBra || valueStartBra == -1) && (valueStartCurl < valueStartPar || valueStartPar == -1)) {
            valueEndIdx = jsonString.indexOf("}", valueStartCurl + 1);
            return jsonString.substring(valueStartCurl + 1, valueEndIdx);
        } else {
            return "";
        }
    }

    private static Map<String, String> getAttributeMap(String jsonString) {
        Map<String, String> attributeMap = new HashMap<>();

        for (String att : jsonString.split("\\},\\{")) {
            if (att.startsWith("{")) {
                att = att.substring(1);
            }
            attributeMap.put(getJsonAttribute("name", att), getJsonAttribute("value", att));
        }
        return attributeMap;
    }

    /**
     * Utility class to transform a Map with an Olog property into a Json
     * string.
     *
     * @param nameMap
     * @param attributesMap
     * @return
     */
    public static String getJsonAttributes(String nameMap, Map<String, String> attributesMap) {
        String propertyString = "{\"name\":\"" + nameMap + "\",\"attributes\":[";

        for (Entry<String, String> att : attributesMap.entrySet()) {
            propertyString = propertyString.concat("{\"name\":\"" + att.getKey() + "\",\"value\":\"" + att.getValue() + "\"},");
        }

        return propertyString.concat("]}");
    }
}
