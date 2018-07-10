/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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
package objective.taskboard.it;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import objective.taskboard.rules.CleanupDataFolderRule;
import objective.taskboard.testUtils.JiraMockServer;
import objective.taskboard.utils.IOUtilities;

import org.junit.Before;

import objective.taskboard.RequestBuilder;
import objective.taskboard.RequestResponse;
import objective.taskboard.TestMain;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.junit.Rule;

public abstract class AbstractIntegrationTest {

    private static final String JIRA_LOCAL_REST_API_ISSUE_URL = "http://localhost:4567/rest/api/latest/issue/";
    private static final ExecutorService service = Executors.newSingleThreadExecutor();
    private static final long TIMEOUT_IN_SECONDS = 120;

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
 
    protected static void emulateUpdateIssue(String issueKey, String fieldsJson) { 
        RequestBuilder 
            .url(JIRA_LOCAL_REST_API_ISSUE_URL + issueKey) 
            .body("{\"fields\":" + fieldsJson + "}") 
            .put(); 
 
        String payloadJson = "_updatePayload.json";
        sendNotificationByWebhook(issueKey, payloadJson); 
    }

    protected static void emulateDeleteIssue(String issueKey) { 
        RequestBuilder 
            .url(JIRA_LOCAL_REST_API_ISSUE_URL + issueKey)
            .delete(); 
 
        String payloadJson = "_deletePayload.json";
        sendNotificationByWebhook(issueKey, payloadJson); 
    }

    private static void sendNotificationByWebhook(String issueKey, String payloadJson) {
        String body = IOUtilities.resourceToString("webhook/" + issueKey + payloadJson);

        RequestBuilder 
            .url(getSiteBase()+"/webhook/TASKB") 
            .header("Content-Type", "application/json") 
            .body(body) 
            .post();
    } 

}
