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

import static objective.taskboard.it.IssueUpdateFieldJson.ASSIGNEE_FOO;
import static objective.taskboard.it.IssueUpdateFieldJson.CLASS_OF_SERVICE_FIXED_DATE;
import static objective.taskboard.it.IssueUpdateFieldJson.PROPERTIES_EMPTY;
import static objective.taskboard.it.IssueUpdateFieldJson.RELEASE_2_0;
import static objective.taskboard.it.IssueUpdateFieldJson.STATUS_DEFERRED;
import static objective.taskboard.it.IssueUpdateFieldJson.STATUS_DOING;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebhookUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {

    private static final String _1_0 = "1.0";
    private static final String _2_0 = "2.0";
    private static final String FIXED_DATE = "Fixed Date";
    private static final String STANDARD = "Standard";
    private static final String STANDARD_COLOR = "rgb(238, 238, 238)";
    private static final String FIXED_DATE_COLOR = "rgb(254, 229, 188)";
    private MainPage mainPage; 

    @Before 
    public void beforeTest() { 
        mainPage = MainPage.produce(webDriver); 
    } 
        
    @After 
    public void afterTest() { 
        mainPage = null; 
    } 

    @Test
    public void whenUpdateHappensViaWebHookAndUpdatedIssueIsOpen_ShouldWarnUser() {
        mainPage.errorToast().close();
        IssueDetails issueDetails = mainPage
            .issue("TASKB-625")
            .click()
            .issueDetails();
        
        emulateUpdateIssue("TASKB-625", ASSIGNEE_FOO, PROPERTIES_EMPTY);
        
        issueDetails
            .assertRefreshWarnIsOpen()
            .clickOnRefreshWarning()
            .assertAssignees("foo","gtakeuchi");

        emulateDeleteIssue("TASKB-625");

        issueDetails
            .assertDeleteWarnIsOpen()
            .clickOnDeleteWarning()
            .assertIsClosed();
    }

    @Test
    public void givenAnIssueNotVisible_whenUpdateHappensViaWebHook_RefreshToastShouldNotShowUP() {
        mainPage.typeSearch("TASKB-625");

        BoardStepFragment stepToDo = mainPage.lane("Operational")
                .boardStep("To Do");
        stepToDo.assertIssueList("TASKB-625");

        mainPage.typeSearch("TASKB-61")
            .assertVisibleIssues("TASKB-611", "TASKB-612", "TASKB-613", "TASKB-610", "TASKB-614");

        emulateUpdateIssue("TASKB-625", STATUS_DOING);

        mainPage.refreshToast().assertNotVisible();
        mainPage.typeSearch("TASKB-625");
        stepToDo.assertIssueList();
        mainPage.lane("Operational")
            .boardStep("Doing")
            .assertIssueList("TASKB-625");
    }

    @Test
    public void givenVisibleIssuesWithChildren_whenParentGoToDeferred_thenAllChildrenShouldDisappear() {
        mainPage.issue("TASKB-606")
            .enableHierarchicalFilter();
        mainPage.assertVisibleIssues("TASKB-606", "TASKB-186", "TASKB-235", "TASKB-601", "TASKB-572");
        mainPage.issue("TASKB-606")
            .enableHierarchicalFilter();

        emulateUpdateIssue("TASKB-606", STATUS_DEFERRED);

        mainPage.errorToast().close();
        mainPage.refreshToast().assertNotVisible();
        mainPage.typeSearch("TASKB-606").assertVisibleIssues()
            .typeSearch("TASKB-186").assertVisibleIssues()
            .typeSearch("TASKB-235").assertVisibleIssues()
            .typeSearch("TASKB-601").assertVisibleIssues()
            .typeSearch("TASKB-572").assertVisibleIssues()
            .refreshToast().assertNotVisible();
    }

    @Test
    public void givenIssuesWithChildren_whenParentChangeTheClassOfServiceOrRelease_thenAllChildrenShouldUpdate() {
        mainPage.issue("TASKB-606")
            .assertCardColor(STANDARD_COLOR)
                .click().issueDetails()
            .assertClassOfService(STANDARD)
            .assertReleaseNotVisible()
                .closeDialog();
        
        mainPage.issue("TASKB-186")
            .assertCardColor(STANDARD_COLOR)
                .click().issueDetails()
            .assertClassOfService(STANDARD)
            .assertRelease(_1_0)
                .closeDialog();
        
        mainPage.issue("TASKB-235")
            .assertCardColor(STANDARD_COLOR)
                .click().issueDetails()
            .assertClassOfService(STANDARD)
            .assertReleaseNotVisible()
                .closeDialog();
        
        mainPage.issue("TASKB-601")
            .assertCardColor(STANDARD_COLOR)
                .click().issueDetails()
            .assertClassOfService(STANDARD)
            .assertReleaseNotVisible()
                .closeDialog();
        
        mainPage.issue("TASKB-572")
            .assertCardColor(STANDARD_COLOR)
                .click().issueDetails()
            .assertClassOfService(STANDARD)
            .assertRelease(_1_0)
                .closeDialog();

        emulateUpdateIssue("TASKB-606", CLASS_OF_SERVICE_FIXED_DATE);
        emulateUpdateIssue("TASKB-606", RELEASE_2_0);

        String[] updatedIssues = {"TASKB-606", "TASKB-186", "TASKB-235", "TASKB-601", "TASKB-572"};
        mainPage.assertUpdatedIssues(updatedIssues);
        
        mainPage.refreshToast()
            .assertVisible()
                .showOnlyUpdated();
        
        mainPage.assertVisibleIssues(updatedIssues);

        mainPage.issue("TASKB-606")
            .assertCardColor(FIXED_DATE_COLOR)
                .click().issueDetails()
            .assertClassOfService(FIXED_DATE)
            .assertRelease(_2_0)
                .closeDialog();
                
        mainPage.issue("TASKB-186")
            .assertCardColor(FIXED_DATE_COLOR)
                .click().issueDetails()
            .assertClassOfService(FIXED_DATE)
            .assertRelease(_1_0)
                .closeDialog();
                
        mainPage.issue("TASKB-235")
            .assertCardColor(FIXED_DATE_COLOR)
                .click().issueDetails()
            .assertClassOfService(FIXED_DATE)
            .assertRelease(_2_0)
                .closeDialog();
                
        mainPage.issue("TASKB-601")
            .assertCardColor(FIXED_DATE_COLOR)
                .click().issueDetails()
            .assertClassOfService(FIXED_DATE)
            .assertRelease(_2_0)
                .closeDialog();
                
        mainPage.issue("TASKB-572")
            .assertCardColor(FIXED_DATE_COLOR)
                .click().issueDetails()
            .assertClassOfService(FIXED_DATE)
                .assertRelease(_1_0);
    }

    @Test
    public void whenDeleteIssueHappensViaWebhook_thenIssueShouldDisappear() {
        mainPage.typeSearch("TASKB-625")
            .assertVisibleIssues("TASKB-625");

        emulateDeleteIssue("TASKB-625");

        mainPage.refreshToast().assertNotVisible();
        mainPage.assertVisibleIssues()
            .refreshToast().assertNotVisible();
    }
}
