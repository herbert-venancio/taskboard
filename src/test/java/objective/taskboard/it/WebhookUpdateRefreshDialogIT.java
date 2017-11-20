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

import static java.lang.Thread.sleep;

import java.io.IOException;

import org.junit.Test;

import objective.taskboard.RequestBuilder;
import objective.taskboard.utils.IOUtilities;

public class WebhookUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenUpdateHappensViaWebHook_RefreshToastShouldShowUP() throws IOException {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.issue("TASKB-625")
            .assertHasFirstAssignee();

        emulateAssignToFoo();

        mainPage.errorToast().close();
        mainPage.refreshToast().assertVisible();
        mainPage.typeSearch("TASKB-61");
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-625");
        mainPage.issue("TASKB-625")
            .assertHasFirstAssignee()
            .assertHasSecondAssignee();
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-611", "TASKB-612", "TASKB-613", "TASKB-610", "TASKB-614");
        mainPage.refreshToast().toggleShowHide();
        mainPage.refreshToast().dismiss();
        mainPage.refreshToast().assertNotVisible();
        mainPage.assertVisibleIssues("TASKB-611", "TASKB-612", "TASKB-613", "TASKB-610", "TASKB-614");
    }

    @Test
    public void whenUpdateHappensViaWebHookAndUpdatedIssueIsOpen_ShouldWarnUser() throws IOException {
        
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        IssueDetails issueDetails = mainPage
            .issue("TASKB-625")
            .click()
            .issueDetails();
        
        emulateAssignToFoo();
        
        issueDetails
            .assertRefreshWarnIsOpen()
            .clickOnWarning()
            .assertAssigneeIs("foo");
    }

    @Test
    public void givenAnIssueNotVisible_whenUpdateHappensViaWebHook_RefreshToastShouldNotShowUP() throws IOException, InterruptedException {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.typeSearch("TASKB-625");

        BoardStepFragment stepToDo = mainPage.lane("Operational")
                .boardStep("To Do");
        stepToDo.assertIssueList("TASKB-625");

        mainPage.typeSearch("TASKB-61")
            .assertVisibleIssues("TASKB-611", "TASKB-612", "TASKB-613", "TASKB-610", "TASKB-614");

        emulateTransitionIssue();
        sleep(3000);

        mainPage.refreshToast().assertNotVisible();
        mainPage.typeSearch("TASKB-625");
        stepToDo.assertIssueList();
        mainPage.lane("Operational")
            .boardStep("Doing")
            .assertIssueList("TASKB-625");
    }

    private static void emulateAssignToFoo() {
        RequestBuilder
            .url("http://localhost:4567/rest/api/latest/issue/TASKB-625")
            .body("{\"fields\":{\"assignee\":{\"name\":\"foo\"}},\"properties\":[]}")
            .put();
        
        String body = IOUtilities.resourceToString("webhook/TASKB_625_updatePayload.json");
        RequestBuilder
            .url(getSiteBase()+"/webhook/TASKB")
            .header("Content-Type", "application/json")
            .body(body)
            .post();
    }

    private static void emulateTransitionIssue() {
        RequestBuilder
            .url("http://localhost:4567/rest/api/latest/issue/TASKB-625")
            .body("{\"fields\":{\"status\":{\"id\": \"10652\",\"name\": \"Doing\"}}}")
            .put();

        String body = IOUtilities.resourceToString("webhook/TASKB_625_updatePayload.json");
        RequestBuilder
            .url(getSiteBase()+"/webhook/TASKB")
            .header("Content-Type", "application/json")
            .body(body)
            .post();
    }

    public static void main(String[] args) {
        emulateAssignToFoo();
    }
}
