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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import xal.extension.logbook.Attachment;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class LogEntry {

    private String body;
    private String entryType;
    private String title;
    private List<String> logbooks;

    private List<String> tags = new ArrayList<>();
    private List<Property> properties = new ArrayList<>();

    private List<OlogAttachment> attachments = new ArrayList<>();

    public LogEntry(String title, String body, String entryType, List<String> logbooks) {
        this.title = title;
        this.body = body;
        this.entryType = entryType;
        this.logbooks = logbooks;
    }

    public LogEntry(String title, String body, String entryType, List<String> logbooks, List<OlogAttachment> attachments) {
        this.title = title;
        this.body = body;
        this.entryType = entryType;
        this.logbooks = logbooks;
        this.attachments = attachments;
    }

    public LogEntry(String json) {
        JSONObject entryJson = new JSONObject(json);

        this.title = entryJson.getString("title");
        this.body = entryJson.getString("body");
        this.entryType = entryJson.getString("level");

        logbooks = new ArrayList<>();
        JSONArray logbooksArray = entryJson.getJSONArray("logbooks");
        for (Object logbook : logbooksArray) {
            logbooks.add(((JSONObject) logbook).getString("name"));
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLogbooks() {
        return logbooks;
    }

    public void setLogbooks(List<String> logbooks) {
        this.logbooks = logbooks;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void addTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public List<OlogAttachment> getAttachments() {
        return attachments;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public void addAttachment(Attachment attachment) {
        if (attachment instanceof OlogAttachment) {
            this.attachments.add((OlogAttachment) attachment);
        } else {
            this.attachments.add(new OlogAttachment(attachment));
        }
    }

    public String toJson() {
        JSONObject out = new JSONObject();

        out.put("title", title);
        out.put("description", body);
        out.put("level", entryType);

        JSONArray logbooksArray = new JSONArray();
        for (String logbook : logbooks) {
            JSONObject logbookJson = new JSONObject();
            logbookJson.put("name", logbook);
            logbooksArray.put(logbookJson);
        }
        out.put("logbooks", logbooksArray);

        if (tags != null && !tags.isEmpty()) {
            JSONArray tagsArray = new JSONArray();
            for (String tag : tags) {
                JSONObject tagJson = new JSONObject();
                tagJson.put("name", tag);
                tagsArray.put(tagJson);
            }
            out.put("tags", tagsArray);
        }

        if (properties != null && !properties.isEmpty()) {
            JSONArray propertiesArray = new JSONArray();
            for (Property property : properties) {
                JSONObject propertyJson = new JSONObject(property.toJson());
                propertiesArray.put(propertyJson);
            }
            out.put("properties", propertiesArray);
        }

        return out.toString();
    }
}
