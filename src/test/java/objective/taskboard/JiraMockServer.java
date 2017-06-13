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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    
    public static void defineRoutesAndStart() {
        Spark.exception(Exception.class, (ex, req, res) -> {
            ex.printStackTrace();
        });
        
        get("/rest/api/latest/project",  (req, res) ->{
            return loadMockData("project.response.json");
        });
        
        get("/rest/api/latest/project/TASKB", (req, res) ->{
            return loadMockData("project_TASKB.response.json");
        });
        
        get("rest/api/latest/status",  (req, res) ->{
            return loadMockData("status.response.json");
        });
        
        get("rest/api/latest/priority",  (req, res) ->{
            return loadMockData("priority.response.json");
        });
        
        get("rest/api/latest/issuetype",  (req, res) ->{
            return loadMockData("issuetype.response.json");
        });
        
        get("rest/api/latest/user",  (req, res) ->{
            return loadMockData("user.response.json");
        });
        
        get("rest/api/latest/serverInfo",  (req, res) ->{
            return loadMockData("serverInfo.response.json");
        });
        
        post("/rest/api/latest/search", "application/json", (req,res) -> {
            @SuppressWarnings("rawtypes")
            Map searchData = gson.fromJson(req.body(), java.util.Map.class);
            return makeFakeRequest(Math.round((Double)searchData.get("startAt")));
        });
        
        get("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            String issueKey = req.params(":issueKey");
            String loadMockData = loadMockData(issueKey+".json");
            if (loadMockData == null) {
                return retryFromRealServer(issueKey);
            }
            return loadMockData;
        });
    }
    
    private static String retryFromRealServer(String issueKey) {
        String string = RequestBuilder
                .url("http://54.68.128.117:8100/rest/api/2/issue/" + issueKey + "?expand=schema,names,transitions")
                .credentials("lousa", "objective").get().content;
        try {
            FileUtils.writeStringToFile(new File("/tmp/" + issueKey + ".json"), string, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return string;
    }
    
    private static String makeFakeRequest(long l) {
        String result = loadMockData("search"+l+".json");
        if (result == null)
            return "{\"startAt\":0,\"maxResults\":100,\"total\":605,\"issues\":[]}";
        return result;
    }
    

    private static String loadMockData(String name) {
        InputStream stream = JiraMockServer.class.getResourceAsStream("/" + environment() +"/" + name);
        if (stream == null) 
            return null;
        try {
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String environment() {
        return "objective-jira-teste";
    }

    
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
