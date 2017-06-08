package objective.taskboard;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */


import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import spark.Spark;


public class JiraMockServer {
    public static void main(String[] args) {
        defineRoutesAndStart();
    }
    
    public static void begin() { 
        Thread thread = new Thread(JiraMockServer::defineRoutesAndStart);
        thread.setDaemon(true);
        thread.start();
    }
    
    @SuppressWarnings("rawtypes")
    private static Map issueInfo = new LinkedHashMap();
    
    public static void defineRoutesAndStart() {
        Spark.exception(Exception.class, (ex, req, res) -> {
            ex.printStackTrace();
        });
        
        get("rest/api/latest/project",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/project.response.json"), "UTF-8");
        });
        
        get("/rest/api/latest/project/TASKB", (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/project_TASKB.response.json"), "UTF-8");
        });
        
        get("rest/api/latest/status",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/status.response.json"), "UTF-8");
        });
        
        get("rest/api/latest/priority",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/priority.response.json"), "UTF-8");
        });
        
        get("rest/api/latest/issuetype",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/issuetype.response.json"), "UTF-8");
        });
        
        get("rest/api/latest/user",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/user.response.json"), "UTF-8");
        });
        
        get("rest/api/latest/serverInfo",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/serverInfo.response.json"), "UTF-8");
        });
        
        post("/rest/api/latest/search", "application/json", (req,res) -> {
            @SuppressWarnings("rawtypes")
            Map searchData = gson.fromJson(req.body(), java.util.Map.class);
            return makeFakeRequest(Math.round((Double)searchData.get("startAt")));
        });
        
        loadIssues();
        
        get("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            String issueKey = req.params(":issueKey");
            try {
                return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/"+issueKey+".json"), "UTF-8");
            }catch(Exception e) {
                String string=RequestBuilder
                    .url("http://54.68.128.117:8100/rest/api/2/issue/" + issueKey + "?expand=schema,names,transitions")
                    .credentials("lousa","objective")
                    .get()
                    .content;
                FileUtils.writeStringToFile(new File("/tmp/"+issueKey+".json"), string, "UTF-8");
                return string;
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map loadIssues() {
        issueInfo = new LinkedHashMap();
        
        for (int i = 0; i < 7; i++) {
            String fName = "/searchIssueAt"+(i*100)+".json";
            try {
                String json = IOUtils.toString(JiraMockServer.class.getResourceAsStream(fName),"UTF-8");
                Map<String, Object> map = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
                }.getType());
                
                for (Object o : ((ArrayList) map.get("issues"))) {
                    Map m = (Map)o;
                    issueInfo.put(m.get("key"), o);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return issueInfo;
    }
    
    private static String makeFakeRequest(long l) {
        InputStream stream = JiraMockServer.class.getResourceAsStream("/search"+l+".json");
        if (stream == null)
            return "{\"startAt\":0,\"maxResults\":100,\"total\":605,\"issues\":[]}";
        
        try {
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
