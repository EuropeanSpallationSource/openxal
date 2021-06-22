/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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
 * Class to parse the lattice repository using the JSON interface
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

    /**
     * This method parses a JSON file and stores names and URLs for all the
     * sequences
     *
     * @param urlString repository URL
     * @return true if parsing was successful
     */
    public boolean URL2Json(String urlString) {
        JSONObject jsonMainPage;
        JSONArray jsonSeqList;
        String seqname;
        String seqUrl;
        List<URI> files = new ArrayList<>();
        List<String> sequences = new ArrayList<>();
        try {
            do {
                URL url = new URL(urlString);
                jsonMainPage = new JSONObject(IOUtils.toString(url.openStream()));
                jsonSeqList = jsonMainPage.getJSONArray("values");
                for (int i = 0; i < jsonSeqList.length(); i++) {
                    seqname = jsonSeqList.getJSONObject(i).getString("path");
                    if (seqname.substring(0, 1).matches("\\d+(\\.\\d+)?")
                            && Integer.parseInt(seqname.substring(2, 3)) == 0) {
                        seqUrl = jsonSeqList.getJSONObject(i).getJSONObject("links").getJSONObject("self").getString("href");
                        sequences.add(seqname.substring(4));
                        seqUrl += "Beam_Physics/lattice.dat";
                        files.add(new URI(seqUrl));
                    }
                }
                if (jsonMainPage.has("next")) {
                    urlString = jsonMainPage.getString("next");
                } else {
                    urlString = null;
                }
            } while (urlString != null);

            sourceFileNames = files.toArray(new URI[]{});
            sequenceNames = sequences.toArray(new String[]{});
            basePath = new File(sourceFileNames[0].getPath()).getParentFile().getParentFile().getParent();
            basePath = new URL(sourceFileNames[0].toURL().getProtocol(), sourceFileNames[0].toURL().getHost(), sourceFileNames[0].toURL().getPort(), basePath).toString();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
