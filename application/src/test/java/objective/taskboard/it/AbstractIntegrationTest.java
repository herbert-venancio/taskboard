package objective.taskboard.it;

import static java.util.Arrays.asList;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;

import objective.taskboard.RequestBuilder;
import objective.taskboard.RequestResponse;
import objective.taskboard.TestMain;
import objective.taskboard.issueBuffer.IssueBufferState;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.rules.CleanupDataFolderRule;
import objective.taskboard.testUtils.JiraMockServer;
import objective.taskboard.utils.IOUtilities;

public abstract class AbstractIntegrationTest {

    private static final String JIRA_LOCAL_REST_API_ISSUE_URL = "http://localhost:4567/rest/api/latest/issue/";
    private static final ExecutorService service = Executors.newSingleThreadExecutor();
    private static final long TIMEOUT_IN_SECONDS = 120;
    private static final String TASKB_PROJECT_KEY = "TASKB";

    @Rule
    public CleanupDataFolderRule clean = new CleanupDataFolderRule(Paths.get("rootDataTest/data/followup-templates"));

    @Before
    public final void setupIntegrationTest() {
        JiraMockController.resetMock();
        waitServerReady();
        resetIssueBuffer();
    }

    protected void waitServerReady() {
        final Future<Void> f = service.submit(() -> {
            while (true) {
                try {
                    RequestResponse response = RequestBuilder
                            .url(getSiteBase() + "/ws/issues/issue-buffer-state")
                            .credentials("foo", "bar").get();

                    if (response.responseCode < 400) {
                        if (response.content.equals(IssueBufferState.ready.toString()))
                            return null;
                    }
                } catch(Exception e) {
                    // just assume 'not ready'
                }
                System.out.println("Integration tests: Server not ready... waiting");
                Thread.sleep(1000);
            }
        });
        try {
            f.get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch(TimeoutException ex) {
            throw new IllegalStateException("Server did not come up after " + TIMEOUT_IN_SECONDS + " seconds.\n"
                    + "If you're running from eclipse, make sure to run " + TestMain.class.getName());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } 
    }

    public static String getSiteBase() {
        return "http://localhost:8900";
    }

    public static String getAppBaseUrl() {
        return getSiteBase() + "/app/";
    }

    protected void resetIssueBuffer() {
        RequestBuilder.url(getSiteBase()+"/test/resetbuffer").credentials("foo", "bar").get();
    }
    
    protected void registerWebhook(String eventType) {
        JiraMockServer.registerWebhook(getSiteBase(), eventType);
    }
    
    protected void emulateVersionUpdate(String version, String newName) { 
        registerWebhook("jira:version_updated"); 
 
        RequestBuilder 
                .url("http://localhost:4567/rest/api/latest/version/" + version) 
                .body("{\"name\":\"" + newName + "\"}") 
                .put(); 
    }

    protected static void emulateMoveIssueToProject(final String sourceIssueKey, final String destinationProjectKey) {
        String jsonBody = IOUtilities.resourceToString("webhook/" + destinationProjectKey + "_movePayload.json");
        String newIssueKey = getNewIssueKey(jsonBody);

        emulateCreateIssueWithoutWebhookNotification(newIssueKey, jsonBody);
        emulateDeleteIssueWithoutWebhookNotification(sourceIssueKey);
        sendNotificationByWebhook(newIssueKey, "-issue_moved.json", destinationProjectKey);
    }

    private static String getNewIssueKey(final String jsonBody) {
        String newIssueKey;
        try {
            JSONObject jsonObject = new JSONObject(jsonBody);

            newIssueKey = jsonObject
                .getJSONArray("issues")
                .getJSONObject(0)
                .get("key")
                .toString();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newIssueKey;
    }

    protected static void emulateCreateIssueTaskb800subtaskOf624() {
        String issueJson = IOUtilities.resourceToString("webhook/create-TASKB-800-subtaskof-TASKB-624.json");

         emulateCreateIssueWithoutWebhookNotification("TASKB-800", issueSearchResponseGenerator(issueJson));

         RequestBuilder
            .url(getSiteBase() + "/webhook/" + TASKB_PROJECT_KEY)
            .header("Content-Type", "application/json")
            .body(webhookJsonGenerator(WebhookEvent.ISSUE_CREATED, issueJson))
            .post();
    }

    protected static void emulateUpdateIssue(String issueKey, IssueUpdateFieldJson... jsonList) {
        if (jsonList.length == 0)
            throw new IllegalArgumentException();

        String jsonValue = asList(jsonList).stream()
            .map(json -> json.json)
            .collect(Collectors.joining(","));
        emulateUpdateIssue(issueKey, String.join(",", jsonValue));
    }

    private static void emulateUpdateIssue(String issueKey, String fieldsJson) {
        RequestBuilder
            .url(JIRA_LOCAL_REST_API_ISSUE_URL + issueKey)
            .body("{\"fields\":" + fieldsJson + "}")
            .put();

        String payloadJson = "_updatePayload.json";
        sendNotificationByWebhook(issueKey, payloadJson, TASKB_PROJECT_KEY);
    }

    private static String issueSearchResponseGenerator(String issueJson) {
        return "{ \"issues\": [" + issueJson + "]}";
    }

     private static String webhookJsonGenerator(WebhookEvent webhookEvent, String issueJson) {
        return "{" +
                "\"timestamp\": 1517590550124," + 
                "\"webhookEvent\": \""+ webhookEvent.typeName +"\"," + 
                "\"user\": {" + 
                "    \"self\": \"http:\\/\\/localhost:4567\\/rest\\/api\\/latest\\/user?username=foo\"," + 
                "    \"name\": \"Foo\"," + 
                "    \"key\": \"foo\"," + 
                "    \"emailAddress\": \"foo@test.com\"," + 
                "    \"avatarUrls\": {" + 
                "        \"48x48\": \"http:\\/\\/www.gravatar.com\\/avatar\\/64e1b8d34f425d19e1ee2ea7236d3028?d=mm&s=48\"," + 
                "        \"24x24\": \"http:\\/\\/www.gravatar.com\\/avatar\\/64e1b8d34f425d19e1ee2ea7236d3028?d=mm&s=24\"," + 
                "        \"16x16\": \"http:\\/\\/www.gravatar.com\\/avatar\\/64e1b8d34f425d19e1ee2ea7236d3028?d=mm&s=16\"," + 
                "        \"32x32\": \"http:\\/\\/www.gravatar.com\\/avatar\\/64e1b8d34f425d19e1ee2ea7236d3028?d=mm&s=32\"" + 
                "    }," + 
                "    \"displayName\": \"Foo\"," + 
                "    \"active\": true," + 
                "    \"timeZone\": \"America\\/Sao_Paulo\"" + 
                "}," +
                "\"issue\": " + issueJson +
            "}";
    }

     protected static void emulateDeleteIssue(final String issueKey) {
        emulateDeleteIssueWithoutWebhookNotification(issueKey);

        String payloadJson = "_deletePayload.json";
        sendNotificationByWebhook(issueKey, payloadJson, TASKB_PROJECT_KEY);
    }

    private static void sendNotificationByWebhook(String issueKey, String payloadJson, final String projectKey) {
        String body = IOUtilities.resourceToString("webhook/" + issueKey + payloadJson);

        RequestBuilder
            .url(getSiteBase() + "/webhook/" + projectKey)
            .header("Content-Type", "application/json")
            .body(body)
            .post();
    }

    private static void emulateDeleteIssueWithoutWebhookNotification(final String issueKey) {
        RequestBuilder
            .url(JIRA_LOCAL_REST_API_ISSUE_URL + issueKey)
            .delete();
    }

    protected static void emulateCreateIssueWithoutWebhookNotification(final String issueKey, final String createBody) {
        RequestBuilder
                .url(JIRA_LOCAL_REST_API_ISSUE_URL + issueKey)
                .body(createBody)
                .post();
    }
}
