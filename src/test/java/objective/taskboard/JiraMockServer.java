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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
    
    @SuppressWarnings({ "rawtypes" })
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
            
            if (loadMockData == null) 
                loadMockData = retryFromRealServer(issueKey);
            
            JSONObject issueData = new JSONObject(loadMockData);
            String self = issueData.getString("self").replace("54.68.128.117:8100", "localhost:4567");
            issueData.put("self", self);
            return issueData.toString();
        });
        
        get("/rest/api/latest/issue/:issueId/transitions", (req,res) ->{
            String issueKey = issueKeyByIssueId.get(req.params("issueId"));
            JSONObject issueSearchData = getIssueDataForKey(issueKey);
            JSONArray issues = issueSearchData.getJSONArray("issues");
            JSONObject result = new JSONObject();
            result.put("expand", "transitions");
            result.put("transitions", issues.getJSONObject(0).getJSONArray("transitions"));
            return result.toString(); 
        });
        
        put("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            JSONObject reqData = new JSONObject(req.body());
            String issueKey = req.params(":issueKey");
            
            JSONObject issueSearchData = getIssueDataForKey(issueKey);
            
            JSONArray issues = issueSearchData.getJSONArray("issues");
            JSONObject fields = issues.getJSONObject(0).getJSONObject("fields");
            JSONObject reqFields = reqData.getJSONObject("fields");
            Iterator keys = reqFields.keys();
            while(keys.hasNext()) {
                String aKey = keys.next().toString();
                
                switch(aKey) {
                    case "assignee":
                        setAssignee(fields, reqFields.getJSONObject(aKey));
                        break;
                    case "customfield_11456":
                        setCoassignee(fields, aKey, reqFields.getJSONArray(aKey));
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Field in Mock : " + aKey);
                }
                
            }
            res.status(204);
            return "";
        });
        
        post("/rest/api/latest/issue/:issueId/transitions", (req, res)-> {
            String issueKey = issueKeyByIssueId.get(req.params("issueId"));
            JSONObject issueSearchData = getIssueDataForKey(issueKey);
            JSONObject issue = issueSearchData.getJSONArray("issues").getJSONObject(0);
            JSONArray transitions = issue.getJSONArray("transitions");
            
            JSONObject transitionParam = new JSONObject(req.body());
            int transitionId = transitionParam.getJSONObject("transition").getInt("id");
            
            JSONObject status = null;
            for (int i = 0; i < transitions.length(); i++) {
                JSONObject transition = transitions.getJSONObject(i);
                if (transition.getInt("id") == transitionId) {
                    status = clone(transition.getJSONObject("to"));
                    break;
                }
            }
            if (status == null)
                throw new IllegalStateException("Invalid transition attempted");
            
            issue.getJSONObject("fields").put("status", status);
            
            return "";
        });
    }

    private static JSONObject getIssueDataForKey(String issueKey) throws JSONException {
        JSONObject issueSearchData = dirtySearchIssuesByKey.get(issueKey);
        if (issueSearchData == null) {
            issueSearchData = clone(searchIssuesByKey.get(issueKey)); 
            dirtySearchIssuesByKey.put(issueKey, issueSearchData);
        }
        return issueSearchData;
    }

    private static void setCoassignee(JSONObject coassigneeField, String aKey, JSONArray coassignees) throws JSONException {
        for (int i = 0; i < coassignees.length(); i++) {
            JSONObject coassignee = coassignees.getJSONObject(i);
            coassignee.put("avatarUrls", makeAvatarUrls());
        }
        coassigneeField.put(aKey, coassignees);
    }

    private static void setAssignee(JSONObject fields, JSONObject each) throws JSONException {
        JSONObject makeAssignee = createEmptyAssignee();
        makeAssignee.put("name", each.get("name"));
        fields.put("assignee", makeAssignee);
    }
    
    private static JSONObject createEmptyAssignee() throws JSONException {
        JSONObject assigneeMap = new JSONObject();
        assigneeMap.put("active", "true");
        
        assigneeMap.put("avatarUrls", makeAvatarUrls());
        
        assigneeMap.put("displayName", "Foo");
        assigneeMap.put("key", "foo");
        assigneeMap.put("name", "foo");
        assigneeMap.put("self", "http://localhost:4567/rest/api/latest/user?username=foo");
        assigneeMap.put("timeZone", "America/Sao_Paulo");
        return assigneeMap;
    }

    private static JSONObject makeAvatarUrls() throws JSONException {
        JSONObject avatarUrls= new JSONObject();
        avatarUrls.put("16x16", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=16");
        avatarUrls.put("24x24", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=24");
        avatarUrls.put("32x32", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=32");
        avatarUrls.put("48x48", "http://www.gravatar.com/avatar/c2b78b1d52b346ff4528044ee123cc74?d=mm&s=48");
        return avatarUrls;
    }
    
    private static String retryFromRealServer(String issueKey) {
        String issueJsonString = RequestBuilder
                .url("http://54.68.128.117:8100/rest/api/latest/issue/" + issueKey + "?expand=schema,names,transitions")
                .credentials("lousa", "objective").get().content;
        try {
            System.out.println("Writing data for issueKey: " + issueKey);
            FileUtils.writeStringToFile(new File("/tmp/" + issueKey + ".json"), issueJsonString, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return issueJsonString;
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
            
            JSONObject issueData = dirtySearchIssuesByKey.get(issueKey);
            if (issueData == null)
                issueData = searchIssuesByKey.get(issueKey);
            return issueData.toString();
        }
        
        return loadMockData(datFileName);
    }

    private static String loadMockData(String name) {
        return TestUtils.loadResource(JiraMockServer.class, "/" + environment() +"/" + name);
    }
    
    private static String environment() {
        return "objective-jira-teste";
    }
    
    private static void loadMap() {
        long startAt = 0;
        while(true) {
            try {
                String searchData = loadSearchFile(startAt, "");
                if (searchData == null) break;
                
                JSONObject single = new JSONObject(searchData);
                single.put("total", 1);
                single.put("startAt", 0);
               
                JSONObject jsonObject = new JSONObject(searchData);
               
                JSONArray jsonArray = jsonObject.getJSONArray("issues");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject issueData = jsonArray.getJSONObject(i);
                    String self = issueData.getString("self").replace("54.68.128.117:8100", "localhost:4567");
                    issueData.put("self", self);
                    
                    JSONArray transitionsArray = issueData.getJSONArray("transitions");
                    for (int j = 0; j < transitionsArray.length(); j++) {
                        JSONObject aTransition = transitionsArray.getJSONObject(j);
                        aTransition.put("fields", new JSONObject());
                    }
                    
                    JSONArray arrayOfIssues = new JSONArray();
                    arrayOfIssues.put(issueData);
                    single.put("issues", arrayOfIssues);
                    
                    searchIssuesByKey.put(issueData.getString("key"), clone(single));
                    issueKeyByIssueId.put(issueData.getString("id"), issueData.getString("key"));
                }
                startAt++;
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            } 
        }
        System.out.println("************ DATA LOAD READY ************");
    }
    
    private static JSONObject clone(JSONObject jsonObject) {
        try {
            return new JSONObject(jsonObject.toString());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> issueKeyByIssueId = new LinkedHashMap<String, String>();
    private static Map<String, JSONObject> searchIssuesByKey = new LinkedHashMap<>();
    private static Map<String, JSONObject> dirtySearchIssuesByKey = new LinkedHashMap<>();
    private static String username;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}

