package objective.taskboard.it;

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

import objective.taskboard.RequestBuilder;
import objective.taskboard.RequestResponse;
import objective.taskboard.TestMain;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.junit.Before;

import java.util.concurrent.*;

public abstract class AbstractIntegrationTest {

    private static final ExecutorService service = Executors.newSingleThreadExecutor();
    private static final long TIMEOUT_IN_SECONDS = 120;

    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException {
        waitServerReady();
        resetJiraMock();
        resetIssueBuffer();
    }

    protected void waitServerReady() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Void> f = service.submit(() -> {
            while (true) {
                try {
                    RequestResponse response = RequestBuilder
                            .url(getSiteBase() + "/ws/issues/issue-buffer-state")
                            .credentials("foo", "bar").get();

                    if (response.responseCode < 400)
                        if (response.content.equals(IssueBufferState.ready.toString()) ||
                                response.content.equals(IssueBufferState.updating.toString()))
                            return null;
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
        }
    }

    public static String getSiteBase(){
        return "http://localhost:8900/";
    }

    protected void resetJiraMock() {
        RequestBuilder.url("http://localhost:4567/reset").post();
    }

    protected void resetIssueBuffer() {
        RequestBuilder.url(getSiteBase()+"/test/resetbuffer").get();
    }
}
