/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 /* Here we use the JSON interface of Bitbucket to connect to the site */
package xal.extension.tracewinimporter;

import java.io.File;
import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.io.IOUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emanuelelaface
 */
public class GitParser {

    private URI[] sourceFileNames;
    private String[] sequenceNames;
    private String basePath;

    public String getBasePath() {
        return basePath;
    }

    public URI[] getSourceFileNames() {
        return sourceFileNames;
    }

    public String[] getSequenceNames() {
        return sequenceNames;
    }

    public boolean URL2Json(String urlString) {
        JSONObject json_main_page = null;
        JSONObject json_seq_data = null;
        JSONArray json_seq_list = null;
        JSONObject coso = null;
        String seq_name = null;
        String seq_url;
        List<URI> files = new ArrayList<>();
        List<String> sequences = new ArrayList<>();
        try {
            do {
                URL url = new URL(urlString);
                json_main_page = new JSONObject(IOUtils.toString(url.openStream()));
                json_seq_list = json_main_page.getJSONArray("values");
                for (int i = 0; i < json_seq_list.length(); i++) {
                    System.out.println(json_seq_list.getJSONObject(i).getString("path"));
                    seq_name = json_seq_list.getJSONObject(i).getString("path");
                    if ( seq_name.substring(0, 1).matches("\\d+(\\.\\d+)?")
                            && Integer.parseInt(seq_name.substring(0, 1)) > 2
                            && Integer.parseInt(seq_name.substring(2, 3)) == 0) {
                        seq_url = json_seq_list.getJSONObject(i).getJSONObject("links").getJSONObject("self").getString("href");
                        sequences.add(seq_name.substring(4));
                        seq_url += "Beam_Physics/lattice.dat";
                        files.add(new URI(seq_url));
                    }
                }
                if (json_main_page.has("next"))
                    urlString = json_main_page.getString("next");
                else
                    urlString = null;
            } while (urlString != null);

            sourceFileNames = files.toArray(new URI[]{});
            sequenceNames = sequences.toArray(new String[]{});
            basePath = new File(sourceFileNames[0].getPath()).getParentFile().getParentFile().getParent();
            basePath = new URL(sourceFileNames[0].toURL().getProtocol() , sourceFileNames[0].toURL().getHost() , sourceFileNames[0].toURL().getPort() , basePath).toString();
            System.out.println(basePath);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
