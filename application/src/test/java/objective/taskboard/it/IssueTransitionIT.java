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

import org.junit.Test;

import objective.taskboard.RequestBuilder;
import objective.taskboard.testUtils.JiraMockController;
import objective.taskboard.utils.IOUtilities;

public class IssueTransitionIT extends AuthenticatedIntegrationTest {
    
    @Test
    public void whenTransitionIsPerformed_ShouldRemoveIssueFromSourceStepAndMoveToTarget(){
        MainPage mainPage = MainPage.produce(webDriver);
        
        LaneFragment operational = mainPage.lane("Operational");
        
        operational.boardStep("To Do").issueCountBadge(14);
        operational.boardStep("To Do").assertIssueList(
                "TASKB-680",
                "TASKB-625",
                "TASKB-627",
                "TASKB-643",
                "TASKB-644",
                "TASKB-659",
                "TASKB-661",
                "TASKB-663",
                "TASKB-664",
                "TASKB-681",
                "TASKB-682",
                "TASKB-683",
                "TASKB-684",
                "TASKB-686"
                );
        operational.boardStep("Doing").issueCountBadge(5);
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                );
        
        mainPage.issue("TASKB-625")
            .click()
            .issueDetails()
            .transitionClick("Doing")
            .confirm();
        
        mainPage.issueDetails().assertIsClosed();

        operational.boardStep("To Do").issueCountBadge(6);
        operational.boardStep("To Do").assertIssueList(
                "TASKB-680",
                "TASKB-627",
                "TASKB-643",
                "TASKB-644",
                "TASKB-659",
                "TASKB-661",
                "TASKB-663",
                "TASKB-664",
                "TASKB-681",
                "TASKB-682",
                "TASKB-683",
                "TASKB-684",
                "TASKB-686"
                );
        
        operational.boardStep("Doing").issueCountBadge(7);
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-625",
                "TASKB-646"
                );
    }

    @Test
    public void whenInvalidTransitionIsPerformed_ShouldGoToTargetStepAndReturnToSourceStep_ShouldShowErrorInIssueAndInMessage() {
        JiraMockController.emulateTransitionError();

        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        LaneFragment operational = mainPage.lane("Operational");

        String[] issuesFromTodo = {
                "TASKB-680",
                "TASKB-625",
                "TASKB-627",
                "TASKB-643",
                "TASKB-644",
                "TASKB-659",
                "TASKB-661",
                "TASKB-663",
                "TASKB-664",
                "TASKB-681",
                "TASKB-682",
                "TASKB-683",
                "TASKB-684",
                "TASKB-686"
            };

        String[] issuesFromDoing = {
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
            };

        operational.boardStep("To Do").issueCountBadge(issuesFromTodo.length);
        operational.boardStep("To Do").assertIssueList(issuesFromTodo);
        operational.boardStep("Doing").issueCountBadge(issuesFromDoing.length);
        operational.boardStep("Doing").assertIssueList(issuesFromDoing);

        TestIssue issue = mainPage.issue("TASKB-625");
        issue
            .click()
            .issueDetails()
            .transitionClick("Doing")
            .confirm();

        issue.assertHasError(true);

        operational.boardStep("To Do").issueCountBadge(issuesFromTodo.length);
        operational.boardStep("To Do").assertIssueList(issuesFromTodo);
        operational.boardStep("Doing").issueCountBadge(issuesFromDoing.length);
        operational.boardStep("Doing").assertIssueList(issuesFromDoing);

        String errorText = "Transition of issue \"TASKB-625\" to \"Doing\" failed: Assignee is required.";

        IssueErrorToast issueErrorToast = mainPage.issueErrorToast();

        IssueDetails issueDetails = issueErrorToast.
            assertOpened().
            clickOpen(1);

        issueDetails
            .assertHasError(errorText)
            .closeError()
            .closeDialog();

        issueErrorToast.assertClosed();
        issue.assertHasError(false);

    }

    @Test
    public void givenIssueWithClassOfServiceFromParent_whenTransitionIssueAndWebhookIsEnabled_thenIssueShouldKeepClassOfServiceFromParent() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        mainPage.issue("TASKB-237").click()
            .issueDetails().transitionClick("Done").confirm()
            .assertIsClosed();

        LaneFragment deployable = mainPage.lane("Deployable");
        deployable.boardStep("QAing").assertIssueList();
        deployable.boardStep("Done").assertIssueList("TASKB-237");

        TestIssue issue = mainPage.issue("TASKB-237");
        issue.assertCardColor("rgb(254, 229, 188)");

        String body = IOUtilities.resourceToString("webhook/TASKB-237_updatePayload.json");
        RequestBuilder
            .url(getSiteBase()+"/webhook/TASKB")
            .header("Content-Type", "application/json")
            .body(body)
            .post();

        mainPage.refreshToast().assertNotVisible();
        issue.assertCardColor("rgb(254, 229, 188)");
    }

    @Test
    public void givenTransitionWithRequiredField_whenPerformTransition_thenRequiredFieldShouldBeFilledWhenSupported() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        IssueDetails issueDetails = mainPage.issue("TASKB-545").click()
                .issueDetails()
                .transitionClick("Back to 'To Review'");

        issueDetails.alertModal()
            .assertMessage("Can't perform this transition because it requires fields not supported in taskboard. Please, perform the transition on Jira.")
            .clickOnOk();

        issueDetails.transitionClick("Deferred")
            .alertModal()
            .assertMessage("Can't perform this transition because 'Fix Version/s' field is required, but the project doesn't have any versions.")
            .clickOnOk();

        issueDetails.transitionClick("Done")
            .fieldsRequiredModal()
            .confirm()
            .assertRequiredMessageIsVisible("fixVersions")
            .addVersion("fixVersions", "1.0")
            .addVersion("fixVersions", "2.0")
            .confirm()
            .assertIsClosed();

        issueDetails.assertIsClosed();

        LaneFragment operational = mainPage.lane("Operational");
        operational.boardStep("Reviewing").issueCountBadge(0);
        operational.boardStep("Reviewing").assertIssueList();
        operational.boardStep("Done").issueCountBadge(9);
        operational.boardStep("Done").assertIssueList(
                "TASKB-638",
                "TASKB-678",
                "TASKB-679",
                "TASKB-545",
                "TASKB-656",
                "TASKB-657",
                "TASKB-658",
                "TASKB-660",
                "TASKB-662"
                );
    }

}
