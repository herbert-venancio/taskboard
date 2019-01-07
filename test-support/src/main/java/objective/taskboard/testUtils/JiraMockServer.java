package objective.taskboard.testUtils;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static spark.Service.ignite;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import objective.taskboard.RequestBuilder;
import objective.taskboard.utils.IOUtilities;
import spark.ExceptionHandler;
import spark.Route;
import spark.Service;

public class JiraMockServer {

    private static final String UNKNOWNUSER = "unknownuser";
    private static final String APPLICATION_JSON = "application/json";
    private static final int MAX_RESULTS = 100;
    private Service server;

    public static void main(String[] args) {
        new JiraMockServer().defineRoutesAndStart();
    }

    public static void begin() {
        Thread thread = new Thread(new JiraMockServer()::defineRoutesAndStart);
        thread.setDaemon(true);
        thread.start();
    }

    public int port() {
        return server.port();
    }

    public JiraMockServer port(int port) {
        ensureInitialized();
        server.port(port);
        return this;
    }

    public JiraMockServer loadPlugins(boolean flag) {
        this.loadPlugin = flag;
        return this;
    }

    public JiraMockServer startAndWait() {
        defineRoutesAndStart();
        server.awaitInitialization();
        return this;
    }

    @SuppressWarnings({ "rawtypes" })
    public void defineRoutesAndStart() {
        loadMap();
        exception(Exception.class, (ex, req, res) -> {
            ex.printStackTrace();
        });

        post("/reset", (req, res) ->{
            dirtySearchIssuesByKey.clear();
            deletedSearchIssuesByKey.clear();
            username = null;
            searchFailureEnabled = false;
            searchAfterInitEnabled = false;
            transitionFailureEnabled = false;
            webhooks.clear();
            projectEdits.clear();
            issueEdits.clear();
            return "";
        });

        post("/force-search-failure", (req, res) ->{
            searchFailureEnabled = true;
            return "";
        });

        post("/fix-search-failure", (req, res) ->{
            searchFailureEnabled = false;
            return "";
        });

        post("/enable-search-after-init", (req, res) -> {
            searchAfterInitEnabled = true;
            return "";
        });

        post("/disable-search-after-init", (req, res) -> {
            searchAfterInitEnabled = false;
            return "";
        });

        post("/force-transition-failure", (req, res) -> {
            transitionFailureEnabled = true;
            return "";
        });

        post("/fix-transition-failure", (req, res) -> {
            transitionFailureEnabled = false;
            return "";
        });

        get("/rest/api/latest/project",  (req, res) ->{
            return loadMockData("project.response.json");
        });

        get("/rest/api/latest/project/:projectkey", (req, res) ->{
            String project = loadMockData("project_" + req.params(":projectkey") + ".response.json");
            return applyProjectEdits(project);
        });

        get("rest/api/latest/status",  (req, res) ->{
            return loadMockData("status.response.json");
        });

        get("rest/api/latest/priority",  (req, res) ->{
            return loadMockData("priority.response.json");
        });

        get("rest/api/latest/field", (req, res) -> loadMockData("field.response.json"));

        get("/rest/api/latest/issue/createmeta",  (req, res) ->{
            return loadMockData("createmeta.response.json");
        });

        post("/rest/api/latest/issue",  (req, res) ->{
            countIssueCreated++;
            String loadMockData = loadMockData("createissue.response.json");
            loadMockData = loadMockData.replace("[ISSUE-KEY]","TASK-"+countIssueCreated);
            return loadMockData;
        });

        post("/rest/api/latest/issueLink",  (req, res) ->{
            return "";
        });

        get("rest/api/latest/issuetype",  (req, res) ->{
            return loadMockData("issuetype.response.json");
        });

        get("rest/api/latest/issueLinkType",  (req, res) ->{
            return loadMockData("issuelinktype.response.json");
        });

        get("rest/api/latest/mypermissions",  (req, res) ->{
            return loadMockData("mypermissions.response.json");
        });

        get("rest/api/latest/user",  (req, res) ->loaduser(req.queryParams("username")));
        get("rest/api/latest/myself",  (req, res) ->loaduser(username));

        get("rest/api/latest/serverInfo",  (req, res) -> {
            String auth = new String(Base64.getDecoder().decode(req.headers("Authorization").replace("Basic ","").getBytes()));
            username = auth.split(":")[0];

            if (UNKNOWNUSER.equalsIgnoreCase(username)) {
                res.status(401);
                return "";
            }

            return loadMockData("serverInfo.response.json");
        });

        post("/rest/api/latest/search", APPLICATION_JSON, (req,res) -> {
            if (searchFailureEnabled)
                throw new IllegalStateException("Emulated error");

            if (searchAfterInitEnabled)
                return loadMockData("search_after_init.json");

            Map searchData = gson.fromJson(req.body(), java.util.Map.class);
            return makeFakeRequest(searchData);
        });

        get("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            String issueKey = req.params(":issueKey");
            if (issueHasBeenDeleted(issueKey))
                return "";

            JSONObject issueDataForKey = getIssueDataForKey(issueKey);
            if (issueDataForKey != null) {
                JSONObject jsonObject = issueDataForKey.getJSONArray("issues").getJSONObject(0);
                jsonObject = applyIssueEdits(clone(jsonObject));
                jsonObject.put("names", issueDataForKey.getJSONObject("names"));
                jsonObject.put("schema", issueDataForKey.getJSONObject("schema"));
                return jsonObject.toString();
            }

            String loadMockData = loadMockData(issueKey+".json");

            if (loadMockData == null)
                return null;

            JSONObject issueData = new JSONObject(loadMockData);
            String self = issueData.getString("self").replace("54.68.128.117:8100", "localhost:4567");
            issueData.put("self", self);
            return issueData.toString();
        });

        put("/rest/api/latest/issue/:issueKey",  (req, res) ->{
            JSONObject reqData = new JSONObject(req.body());
            String issueKey = req.params(":issueKey");

            if (issueHasBeenDeleted(issueKey))
                return "";

            JSONObject issueSearchData = getIssueDataForKey(issueKey);
            JSONArray issues = issueSearchData.getJSONArray("issues");
            JSONObject issue = issues.getJSONObject(0);
            JSONObject fields = issue.getJSONObject("fields");
            JSONObject reqFields = reqData.getJSONObject("fields");
            Iterator keys = reqFields.keys();
            while(keys.hasNext()) {
                String aKey = keys.next().toString();

                switch(aKey) {
                    case "assignee":
                        setAssignee(fields, reqFields.getJSONObject(aKey));
                        break;
                    case "description":
                        setDescription(fields, reqFields);
                        break;
                    case "customfield_11456"://co-assignee
                        setCoassignee(fields, aKey, reqFields.getJSONArray(aKey));
                        break;
                    case "status":
                        setStatus(fields, reqFields.getJSONObject(aKey));
                        break;
                    case "customfield_11440"://Class Of Service
                        setClassOfService(fields, aKey, reqFields.getJSONObject(aKey));
                        break;
                    case "customfield_11455"://Release
                        setRelease(fields, aKey, reqFields.getJSONObject(aKey));
                        break;
                    case "customfield_10100"://Assigned team
                        fields.put(aKey, reqFields.getString(aKey));
                        break;
                    case "customfield_11451"://blocked
                        fields.put(aKey, reqFields.getJSONArray(aKey));
                        break;
                    case "customfield_11452"://last block reason
                        fields.put(aKey, reqFields.getString(aKey));
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Field in Mock : " + aKey);
                }
                fields.put("updated", nowIso8601());
            }
            res.status(204);

            sendWebhookWithContentInIssue("jira:issue_updated", issue);

            return "";
        });

        delete("/rest/api/latest/issue/:issueKey", (req, res) -> {
            String issueKey = req.params(":issueKey");
            dirtySearchIssuesByKey.remove(issueKey);
            deletedSearchIssuesByKey.put(issueKey, null);
            return "";
        });

        get("/rest/api/latest/issue/:issueId/transitions", (req,res) ->{
            String issueKey = issueKeyByIssueId.getOrDefault(req.params("issueId"), req.params("issueId"));
            if (issueKey == null)
                throw new IllegalArgumentException("Issue id " + req.params("issueId") + " not found");

            if (issueHasBeenDeleted(issueKey))
                return "";

            JSONObject issueSearchData = getIssueDataForKey(issueKey);
            JSONArray issues = issueSearchData.getJSONArray("issues");
            JSONObject result = new JSONObject();
            result.put("expand", "transitions");
            result.put("transitions", issues.getJSONObject(0).getJSONArray("transitions"));
            return result.toString();
        });

        post("/rest/api/latest/issue/:issueId/transitions", (req, res)-> {
            if (transitionFailureEnabled) {
                res.type(APPLICATION_JSON);
                res.status(400);
                return loadMockData("transition_failure.response.json");
            }
            String issueKey = issueKeyByIssueId.getOrDefault(req.params("issueId"), req.params("issueId"));
            if (issueHasBeenDeleted(issueKey))
                return "";

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
            issue.getJSONObject("fields").put("updated", nowIso8601());

            return "";
        });

        post("/rest/api/latest/version", APPLICATION_JSON, (req,res) -> {
            return loadMockData("createversion.response.json");
        });

        put("/rest/api/latest/version/:versionId", APPLICATION_JSON, (req,res) -> {
            JSONObject body = new JSONObject(req.body());
            final String versionId = req.params(":versionId");
            final String newName = body.getString("name");
            projectEdits.add(project -> {
                try {
                    if(!"TASKB".equals(project.getString("key")))
                        return;

                    JSONArray array = project.getJSONArray("versions");
                    for(int i = 0; i < array.length(); ++i) {
                        JSONObject version = array.getJSONObject(i);
                        if(!versionId.equals(version.getString("id")))
                            continue;

                        version.put("name", newName);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
            issueEdits.add(issue -> {
               try {
                   JSONObject fields = issue.getJSONObject("fields");

                   JSONObject project = fields.getJSONObject("project");
                   if(!"TASKB".equals(project.getString("key")))
                       return;

                   if(fields.isNull("customfield_11455"))
                       return;

                   JSONObject versionCustomField = fields.getJSONObject("customfield_11455");
                   if(!versionId.equals(versionCustomField.getString("id")))
                       return;

                   versionCustomField.put("name", newName);
               } catch (JSONException e) {
                   throw new RuntimeException(e);
               }
            });
            sendWebhookWithContentInResourceFile("TASKB", "jira:version_updated", "TASKB_version_update.json");
            // not really correct response
            return loadMockData("createversion.response.json");
        });

        get("/rest/api/latest/statuscategory", (req, res) -> {
            return loadMockData("status-categories.json");
        });

        post("/rest/webhooks/1.0/webhook", APPLICATION_JSON, (req,res) -> {
            WebHookConfiguration webhook = gson.fromJson(req.body(), WebHookConfiguration.class);
            return webhookRegistration(webhook);
        });

        get("/secure/viewavatar", (req, res) -> {
        	String avatarId = req.queryParams("avatarId");
        	String avatarType = req.queryParams("avatarType");
        	if (!avatarType.equals("issuetype"))
        		return null;

        	byte[] data = loadBinaryImage(avatarId+".svg");
        	if (data == null)
        		data = loadBinaryImage(avatarId+".png");

        	if (data == null)
        		return null; // not found

        	String mimeType = getMimeType(data);
        	if (mimeType.equals("application/xml"))
        		mimeType = "image/svg+xml";
			res.type(mimeType);
			return data;
        });


        if(loadPlugin) {
            new JiraPluginMock().load(server);
        }

        get("*", (req, res) -> {
            res.type(APPLICATION_JSON);
            res.status(404);
            return "{\"message\":\"null for uri: " + req.url() + "\",\"status-code\":404}";
        });
    }

    public String getMimeType(byte data[]) throws Exception {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(data));
        return URLConnection.guessContentTypeFromStream(is);
    }

    private String applyProjectEdits(String project) throws JSONException {
        if(StringUtils.isEmpty(project) || projectEdits.isEmpty())
            return project;

        return applyEdits(new JSONObject(project), projectEdits).toString();
    }

    private static JSONObject applyIssueEdits(JSONObject issue) {
        return applyEdits(issue, issueEdits);
    }

    private static JSONObject applyEdits(JSONObject t, List<Consumer<JSONObject>> edits) {
        for(Consumer<JSONObject> edit : edits) {
            edit.accept(t);
        }
        return t;
    }

    private static JSONObject getIssueDataForKey(String issueKey) {
        JSONObject issueSearchData = dirtySearchIssuesByKey.get(issueKey);
        if (issueSearchData == null) {
            issueSearchData = clone(searchIssuesByKey.get(issueKey));
            dirtySearchIssuesByKey.put(issueKey, issueSearchData);
        }
        return issueSearchData;
    }

    private static int countIssueCreated;

    private static void setClassOfService(JSONObject fields, String aKey, JSONObject newClassOfService) throws JSONException {
        JSONObject makeClassOfService = createEmptyClassOfService();
        makeClassOfService.put("id", "");
        makeClassOfService.put("value", newClassOfService.get("value"));
        fields.put(aKey, newClassOfService);
        fields.put("updated", nowIso8601());
    }

    private static void setRelease(JSONObject fields, String aKey, JSONObject newRelease) throws JSONException {
        fields.put(aKey, newRelease);
        fields.put("updated", nowIso8601());
    }

    private static JSONObject createEmptyClassOfService() throws JSONException {
        JSONObject classOfService = new JSONObject();
        classOfService.put("id", "");
        classOfService.put("self", "");
        classOfService.put("value", "");
        return classOfService;
    }

    private static void setStatus(JSONObject fields, JSONObject newStatus) throws JSONException {
        JSONObject makeStatus = createEmptyStatus();
        makeStatus.put("id", newStatus.get("id"));
        makeStatus.put("name", newStatus.get("name"));
        fields.put("status", makeStatus);
        fields.put("updated", nowIso8601());
    }

    private static JSONObject createEmptyStatus() throws JSONException {
        JSONObject status = new JSONObject();
        status.put("description", "");
        status.put("iconUrl", "");
        status.put("id", "");
        status.put("name", "");
        status.put("self", "");
        status.put("statusCategory", "");
        return status;
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
        fields.put("updated", nowIso8601());
    }

    private static void setDescription(JSONObject fields, JSONObject reqFields) throws JSONException {
        fields.put("description", reqFields.getString("description"));
    }

    private static String nowIso8601() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneOffset.UTC).format(Instant.now());
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
        avatarUrls.put("16x16", "http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=16");
        avatarUrls.put("24x24", "http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=24");
        avatarUrls.put("32x32", "http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=32");
        avatarUrls.put("48x48", "http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=48");
        return avatarUrls;
    }

    @SuppressWarnings("rawtypes")
    private static String makeFakeRequest(Map searchData) {
        long startAt = Math.round((Double)searchData.getOrDefault("startAt", 0.0));
        String jql = searchData.get("jql").toString();
        String result = loadSearchDataCached(startAt, jql);
        if (result == null)
            return "{\"startAt\":" + startAt + ",\"maxResults\":100,\"total\":316,\"issues\":[]}";
        return result;
    }

    private static String loadSearchFile(long startAt, String jql) {
        String datFileName = "search"+startAt+".json";

        if (jql.toLowerCase().contains("key in")) {
            String issueKey = jql.replaceAll("(?i).*key in [(]([^)]*)[)].*", "$1");
            if (issueKey.equals("TASKB-673,TASKB-665,TASKB-648,TASKB-628,TASKB-619,TASKB-621,TASKB-616,TASKB-6,TASKB-206,TASKB-186,TASKB-142,TASKB-136,TASKB-135,TASKB-130,TASKB-125,TASKB-123,TASKB-122,TASKB-99,TASKB-71,TASKB-171,TASKB-225,TASKB-194,TASKB-182,TASKB-179,TASKB-158,TASKB-150,TASKB-96,TASKB-68"))
                issueKey = "missing_parents";

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

    static String loadMockData(String name) {
        return IOUtilities.resourceToString(JiraMockServer.class,"/"+environment() +"/" + name);
    }

    static byte [] loadBinaryImage(String name) {
        return IOUtilities.resourceToBytes(JiraMockServer.class,"/"+environment() +"/images/" + name);
    }

    private static String environment() {
        return "objective-jira-teste";
    }

    private static void loadMap() {
        long startAt = 0;
        while(true) {
            try {
                String searchData = loadSearchFile(startAt*100, "project in (TASKB)");
                if (searchData == null)
                    break;

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

    private static String webhookRegistration(WebHookConfiguration configuration) throws JSONException {
        webhooks.add(configuration);
        JSONObject response = new JSONObject();
        response.put("name", configuration.name);
        response.put("url", configuration.url);
        response.put("excludeBody", false);
        JSONObject filters = new JSONObject();
        filters.put("issue-related-events-section", "");
        response.put("filters", filters);
        response.put("events", configuration.events);
        response.put("enabled", true);
        response.put("self", "http://localhost:4567/rest/webhooks/1.0/webhook/" + webhooks.size());
        response.put("lastUpdatedUser", "admin");
        response.put("lastUpdatedDisplayName", "admin");
        response.put("lastUpdated", System.currentTimeMillis());
        return response.toString();
    }

    private static void sendWebhookWithContentInResourceFile(String projectKey, String eventType, String resourceName) {
        sendWebhookContent(projectKey, eventType, IOUtilities.resourceToString("webhook/" + resourceName));
    }

    private void sendWebhookWithContentInIssue(String eventType, JSONObject issue) {
        JSONObject webhookData = new JSONObject();
        try {
            webhookData.put("issue", issue);
            webhookData.put("user", new JSONObject(loaduser(username)));
            webhookData.put("webhookEvent", eventType);
            webhookData.put("timestamp", System.currentTimeMillis());
            String projectKey = issue.getJSONObject("fields").getJSONObject("project").getString("key");

            sendWebhookContent(projectKey, eventType, webhookData.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendWebhookContent(String projectKey, String eventType, String body) {
        webhooks.stream()
                .filter(webhook -> webhook.events.contains(eventType))
                .map(webhook ->
                        (Runnable) () -> {
                            Map<String, String> parameters = singletonMap("project.key", projectKey);
                            String webhookUrl = StrSubstitutor.replace(webhook.url, parameters);
                            RequestBuilder.url(webhookUrl)
                                    .header("Content-Type", APPLICATION_JSON)
                                    .body(body)
                                    .post();
                        }
                )
                .forEach(executorService::submit);
    }

    private static JSONObject clone(JSONObject jsonObject) {
        try {
            if (jsonObject == null) {
                System.out.println();
            }
            return new JSONObject(jsonObject.toString());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> issueKeyByIssueId = new LinkedHashMap<String, String>();
    private static Map<String, JSONObject> searchIssuesByKey = new LinkedHashMap<>();
    private static Map<String, JSONObject> dirtySearchIssuesByKey = new LinkedHashMap<>();
    private static Map<String, JSONObject> deletedSearchIssuesByKey = new LinkedHashMap<>();
    private static String username;
    private static boolean searchFailureEnabled = false;
    private static boolean searchAfterInitEnabled = false;
    private static boolean transitionFailureEnabled = false;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static List<Consumer<JSONObject>> projectEdits = new ArrayList<>();
    private static List<Consumer<JSONObject>> issueEdits = new ArrayList<>();
    private static List<WebHookConfiguration> webhooks = new ArrayList<>();
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loadPlugin = true;

    private void ensureInitialized() {
        if(server == null)
            server = ignite();
    }

    private <T extends Exception> void exception(Class<T> exceptionClass, ExceptionHandler<? super T> handler) {
        ensureInitialized();
        server.exception(exceptionClass, handler);
    }

    private void get(String path, Route route) {
        ensureInitialized();
        server.get(path, route);
    }

    private void post(String path, Route route) {
        ensureInitialized();
        server.post(path, route);
    }

    private void post(String path, String acceptType, Route route) {
        ensureInitialized();
        server.post(path, acceptType, route);
    }

    private void put(String path, Route route) {
        ensureInitialized();
        server.put(path, route);
    }

    private void put(String path, String acceptType, Route route) {
        ensureInitialized();
        server.put(path, acceptType, route);
    }

    private void delete(String path, Route route) {
        ensureInitialized();
        server.delete(path, route);
    }

    private String loaduser(String username) {
        String loadMockData = loadMockData("user.response.json");
        loadMockData = loadMockData
                .replace("\"key\": \"taskboard\"", "\"key\": \"" + username + "\"")
                .replace("\"name\": \"taskboard\"", "\"name\": \"" + username + "\"")
                .replace("\"displayName\": \"Taskboard\",", "\"displayName\": \"" + capitalize(username) + "\",");
        return loadMockData;
    }

    public static String findUsers(String nameStart) throws JSONException {
        String loadMockData = loadMockData("users-autocomplete.response.json");
        JSONArray allUsers = new JSONArray(loadMockData);
        int allUsersSize = allUsers.length();

        JSONArray filteredUserList = new JSONArray();
        for(int i = 0; i < allUsersSize; i++) {
            JSONObject user = allUsers.getJSONObject(i);
            if (user.getString("displayName").toLowerCase().matches("(?i).*\\b"+nameStart.toLowerCase()+".*"))
                filteredUserList.put(user);
            else if (user.getString("name").toLowerCase().matches(".*\\b"+nameStart.toLowerCase()+".*"))
                filteredUserList.put(user);
        }

        return filteredUserList.toString();
    }

    public static void registerWebhook(String url, String... events) {
        String eventList = String.join(",", Arrays.stream(events)
                .map(event -> "\"" + event + "\"")
                .collect(toList()));

        String registerWebhook = "{" +
                "  \"name\": \"my webhook via rest\"," +
                "  \"url\": \"" + url + "/webhook/${project.key}\"," +
                "  \"events\": [" + eventList + "]" +
                "}";

        RequestBuilder.url("http://localhost:4567/rest/webhooks/1.0/webhook")
                .body(registerWebhook)
                .post();
    }

    private static class WebHookConfiguration {
        public String name;
        public String url;
        public List<String> events;
    }

    private static String loadSearchDataCached(long startAt, String jql) {
        if (jql.toLowerCase().contains("key in")) {
            String issueKey = jql.replaceAll("(?i).*key in [(]([^)]*)[)].*", "$1");
            if (issueKey.equals("TASKB-673,TASKB-665,TASKB-648,TASKB-628,TASKB-619,TASKB-621,TASKB-616,TASKB-6,TASKB-206,TASKB-186,TASKB-142,TASKB-136,TASKB-135,TASKB-130,TASKB-125,TASKB-123,TASKB-122,TASKB-99,TASKB-71,TASKB-171,TASKB-225,TASKB-194,TASKB-182,TASKB-179,TASKB-158,TASKB-150,TASKB-96,TASKB-68")) {
                issueKey = "missing_parents";
                String hardcoded = loadMockData("search_" + issueKey + ".json");

                if (hardcoded != null)
                    return hardcoded;
            }

            if (issueHasBeenDeleted(issueKey))
                return "";

            return getIssueDataForKey(issueKey).toString();
        }
        return getIssuesCached(startAt);
    }

    private static String getIssuesCached(long startAt) {
        JSONObject issues = new JSONObject();
        try {
            AtomicInteger issueCount = new AtomicInteger();

            List<Object> issuesFiltered = searchIssuesByKey.keySet().stream()
                .filter(i -> !issueHasBeenDeleted(i))
                .filter(i -> isInIntervalFiltered(startAt, issueCount))
                .map(JiraMockServer::getIssueData)
                .collect(toList());

            issues.put("expand", "");
            issues.put("issues", issuesFiltered);
            issues.put("maxResults", MAX_RESULTS);
            issues.put("startAt", startAt);
            issues.put("total", searchIssuesByKey.size() - deletedSearchIssuesByKey.size());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        return removeEscapeCaracters(issues);
    }

    private static boolean isInIntervalFiltered(long startAt, AtomicInteger issueCount) {
        int count = issueCount.getAndIncrement();
        return count >= startAt && count < (startAt + MAX_RESULTS);
    }

    private static Object getIssueData(String issueKey) {
        try {
            return getIssueDataForKey(issueKey).getJSONArray("issues").get(0);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String removeEscapeCaracters(JSONObject issues) {
        return issues.toString().replace("\\/", "/");
    }

    private static boolean issueHasBeenDeleted(String issueKey) {
        return deletedSearchIssuesByKey.containsKey(issueKey);
    }
}
