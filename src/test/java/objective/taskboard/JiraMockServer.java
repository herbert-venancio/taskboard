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
import static spark.Spark.put;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void defineRoutesAndStart() {
        loadMap();
        Spark.exception(Exception.class, (ex, req, res) -> {
            ex.printStackTrace();
        });
        
        post("/reset", (req, res) ->{
            dirtySearchIssuesByKey.clear();
            return "";
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
            String loadMockData = loadMockData("user.response.json");
            loadMockData = loadMockData.replace("\"displayName\": \"Taskboard\",", "\"displayName\": \"" + username + "\",");
            return loadMockData;
        });
        
        get("rest/api/latest/serverInfo",  (req, res) ->{
            String auth = new String(java.util.Base64.getDecoder().decode(req.headers("Authorization").replace("Basic ","").getBytes()));
            username = auth.split(":")[0];
            return loadMockData("serverInfo.response.json");
        });
        
        post("/rest/api/latest/search", "application/json", (req,res) -> {
            Map searchData = gson.fromJson(req.body(), java.util.Map.class);
            return makeFakeRequest(searchData);
        });
        
        get("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            String issueKey = req.params(":issueKey");
            String loadMockData = loadMockData(issueKey+".json");
            if (loadMockData == null) {
                return retryFromRealServer(issueKey);
            }
            return loadMockData;
        });
        
        put("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            Map reqData = gson.fromJson(req.body(), java.util.Map.class);
            String issueKey = req.params(":issueKey");
            
            Map issueSearchData = dirtySearchIssuesByKey.get(issueKey);
            if (issueSearchData == null) {
                issueSearchData = gson.fromJson(gson.toJson(searchIssuesByKey.get(issueKey)), Map.class);
                dirtySearchIssuesByKey.put(issueKey, issueSearchData);
            }
            
            List issues = (List) issueSearchData.get("issues");
            
            Map anIssue = (Map)issues.get(0);
            Map fields = (Map) anIssue.get("fields");
            Map<String, Object> reqFields = (Map) reqData.get("fields");
            for (Entry<String, Object> each: reqFields.entrySet()) {
                switch(each.getKey()) {
                    case "assignee":
                        setAssignee(fields, each);
                        break;
                    case "customfield_11456":
                        setCoassignee(fields, each);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Field in Mock : " + each.getKey());
                }
            }
            res.status(204);
            return "";
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setCoassignee(Map fields, Entry<String, Object> each) {
        List coassignees = (List) each.getValue();
        List result = new ArrayList();
        for (Object object : coassignees) {
            Map coassignee = (Map) object;
            
            coassignee.put("avatarUrls", makeAvatarUrls());
            result.add(coassignee);
        }
        fields.put(each.getKey(), result);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setAssignee(Map fields, Entry<String, Object> each) {
        Map makeAssignee = makeAssignee("");
        makeAssignee.putAll((Map) each.getValue());
        fields.put(each.getKey(), makeAssignee);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map makeAssignee(String name) {
        Map assigneeMap = new LinkedHashMap();
        assigneeMap.put("active", "true");
        
        assigneeMap.put("avatarUrls", makeAvatarUrls());
        
        assigneeMap.put("displayName", "Gabriel Takeuchi");
        assigneeMap.put("key", "gtakeuchi");
        assigneeMap.put("name", "gtakeuchi");
        assigneeMap.put("self", "http://54.68.128.117:8100/rest/api/2/user?username=gtakeuchi");
        assigneeMap.put("timeZone", "America/Sao_Paulo");
        return assigneeMap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Map makeAvatarUrls() {
        Map avatarUrls= new LinkedHashMap();
        avatarUrls.put("16x16", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=16");
        avatarUrls.put("24x24", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=24");
        avatarUrls.put("32x32", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=32");
        avatarUrls.put("48x48", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=48");
        return avatarUrls;
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
    
    @SuppressWarnings("rawtypes")
    private static String makeFakeRequest(Map searchData) {
        long startAt = Math.round((Double)searchData.get("startAt"));        
        String jql = searchData.get("jql").toString();
        String result = loadSearchFile(startAt, jql);
        if (result == null)
            return "{\"startAt\":0,\"maxResults\":100,\"total\":605,\"issues\":[]}";
        return result;
    }

    private static String loadSearchFile(long startAt, String jql) {
        String datFileName = "search"+startAt+".json";
        if (jql.contains("key IN ")) { 
            String issueKey = jql.replaceAll(".*key IN [(]([^)]*)[)].*", "$1");
            String hardcoded = loadMockData("search_" + issueKey + ".json");
            if (hardcoded != null)
                return hardcoded;
            @SuppressWarnings("rawtypes")
            Map issueData = dirtySearchIssuesByKey.get(issueKey);
            if (issueData == null)
                issueData = searchIssuesByKey.get(issueKey);
            return gson.toJson(issueData);
        }
        
        return loadMockData(datFileName);
    }

    private static String loadMockData(String name) {
        return TestUtils.loadResource(JiraMockServer.class, "/" + environment() +"/" + name);
    }
    
    private static String environment() {
        return "objective-jira-teste";
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void loadMap() {
        long startAt = 0;
        while(true) {
            String searchData = loadSearchFile(startAt, "");
            if (searchData == null) break;
            
            Map searchMap = gson.fromJson(searchData, Map.class);
           
            List issues = (List) searchMap.get("issues");
            for (Object issueDataObj  : issues) {
                Map issueData = (Map) issueDataObj;
                Map value = gson.fromJson(searchData, Map.class);
                ArrayList onlyOneIssue = new ArrayList();
                onlyOneIssue.add(issueData);
                value.put("issues", onlyOneIssue);
                value.put("total", 1);
                searchIssuesByKey.put(issueData.get("key").toString(), value);
            }
            startAt++;
        }
        System.out.println("************ DATA LOAD READY ************");
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, Map> searchIssuesByKey = new LinkedHashMap<>();
    @SuppressWarnings("rawtypes")
    private static Map<String, Map> dirtySearchIssuesByKey = new LinkedHashMap<>();
    private static String username;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
