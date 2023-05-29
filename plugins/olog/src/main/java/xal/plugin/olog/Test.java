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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import xal.extension.logbook.Attachment;
import xal.extension.logbook.LogbookException;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LogbookException, IOException, OlogUnauthorizedException {
        OlogClient client = OlogClient.getClient();

        client.setServerUrl("http://olog-migration-test.cslab.esss.lu.se:8080/Olog");

        System.out.println(client.getLogbooks());
        System.out.println(client.getTags());
        System.out.println(client.getProperties());

//        System.out.println(client.getLogEntries());
//        System.out.println(client.getLogEntries("description=testt*"));
        client.login("user", new char[]{'u', 's', 'e', 'r', 'P', 'a', 's', 's'});

        LogEntry log = new LogEntry("title", "this is the body", "Normal", List.of("Operations", "Facility"));
        log.addTag("DTL");
        log.addTags(List.of("PBI", "Buildings"));

        Property shift = new Property("Shift Info");
        shift.addAttribute("Shift Lead", "Juan");
        shift.addAttribute("Shift Lead Email", "j@e");
        shift.addAttribute("Operator", "Pepe");
        shift.addAttribute("Shift Lead Phone", "2470");
        shift.addAttribute("Shift ID", "123");
        shift.addAttribute("Operator Phone", "0987");

        log.addProperty(shift);

        log.addAttachment(new Attachment(new File("/Users/juanfestebanmuller/test.pdf")));
        OlogAttachment att = new OlogAttachment(new File("/Users/juanfestebanmuller/test.png"));
        att.generateUuid();
        log.addAttachment(att);

        System.out.println(log.toJson());
        try {
            String jsonEntry = client.submitEntry(log);
            Long logId = new JSONObject(jsonEntry).getLong("id");
            System.out.println(jsonEntry);
        } catch (Exception e) {
            System.out.println("Exception in submitting entry");
            System.out.println(e);
        }
    }

}
