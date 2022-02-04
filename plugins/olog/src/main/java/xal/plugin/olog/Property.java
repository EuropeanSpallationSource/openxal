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
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Property {

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty value = new SimpleStringProperty("");

    // Key-value pairs
    private final List<Property> attributes = new ArrayList<>();

    public Property(String name) {
        this.name.set(name);
    }

    public Property(String name, String value) {
        this.name.set(name);
        this.value.set(value);
    }

    public Property(String name, Map<String, String> attributes) {
        this.name.set(name);

        for (Entry<String, String> entry : attributes.entrySet()) {
            this.attributes.add(new Property(entry.getKey(), entry.getValue()));
        }
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public String getName() {
        return name.getValue();
    }

    public String getValue() {
        return value.getValue();
    }

    public void addAttribute(String key, String value) {
        attributes.add(new Property(key, value));
    }

    public List<Property> getAttributes() {
        return attributes;
    }

    public String toJson() {
        JSONObject out = new JSONObject();

        out.put("name", getName());

        JSONArray attributesArray = new JSONArray();

        for (Property att : attributes) {
            JSONObject attJson = new JSONObject();
            attJson.put("name", att.getName());
            attJson.put("value", att.getValue());
            attributesArray.put(attJson);
        }
        out.put("attributes", attributesArray);

        return out.toString();
    }
}
