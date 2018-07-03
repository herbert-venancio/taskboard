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
package objective.taskboard.it;

import org.junit.Test;

public class ReprioritizationIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenTryingToChangePriorityOrderOfAFeaturedIssue_nothingChanges() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        final String FEATURED_ISSUE_1 = "TASKB-638";
        final String FEATURED_ISSUE_2 = "TASKB-678";
        final String FEATURED_ISSUE_3 = "TASKB-679";
        final String REGULAR_ISSUE = "TASKB-656";
        final String[] expectedIssueList = new String[] {
                FEATURED_ISSUE_1,
                FEATURED_ISSUE_2,
                FEATURED_ISSUE_3,
                REGULAR_ISSUE,
                "TASKB-657",
                "TASKB-658",
                "TASKB-660",
                "TASKB-662"
                };

        BoardStepFragment boardStepDone = mainPage.lane("Operational").boardStep("Done");

        boardStepDone.assertIssueList(expectedIssueList);

        mainPage.issue(FEATURED_ISSUE_2).dragOver(FEATURED_ISSUE_1);

        boardStepDone.assertIssueList(expectedIssueList);

        boardStepDone.scrollTo(FEATURED_ISSUE_3);

        mainPage.issue(REGULAR_ISSUE).dragOver(FEATURED_ISSUE_3);

        boardStepDone.assertIssueList(expectedIssueList);
    }

    @Test
    public void whenIssueIsDragged_AfterReloadItShouldKeepOrder() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        
        LaneFragment operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                );

        mainPage.issue("TASKB-572").dragOver("TASKB-601");
        mainPage.reload();
        mainPage.errorToast().close();

        operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(
                "TASKB-572",
                "TASKB-601",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                );
    }

    @Test
    public void whenIssueIsPriorityOrderIsChanged_ShouldShowNotificationInAnotherBrowserAndUpdateTheOrder() {
        final String[] expectedIssueListBefore = new String[] {
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                };

        final String[] expectedIssueListAfter = new String[] {
                "TASKB-572",
                "TASKB-601",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                };

        createAndSwitchToNewTab();
        
        MainPage secondTabPage = MainPage.to(webDriver);
        secondTabPage.waitUserLabelToBe("Foo");
        secondTabPage.errorToast().close();
        
        LaneFragment operationalInSecondTab = secondTabPage.lane("Operational");
        operationalInSecondTab.boardStep("Doing").assertIssueList(expectedIssueListBefore);
        secondTabPage.issue("TASKB-572").dragOver("TASKB-601");
        operationalInSecondTab.boardStep("Doing").assertIssueList(expectedIssueListAfter);
        
        switchToFirstTab();
        
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        mainPage.refreshToast().assertVisible();
        LaneFragment operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(expectedIssueListAfter);
        
        mainPage.refreshToast().close();
        // makes sure the model is correctly updated
        mainPage.typeSearch("TASKB-625");
        mainPage.clearSearch();
        operational.boardStep("Doing").assertIssueList(expectedIssueListAfter);
    }

    @Test
    public void givenIssuesFilteredByCardFieldFilter_whenChangePriorityOrder_thenAfterFiltersShouldWork() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.openMenuFilters()
            .openCardFieldFilters()
            .clickFilterFieldValue("Team", "TASKBOARD 1")
            .closeMenuFilters();

        LaneFragment deployable = mainPage.lane("Deployable");
        deployable.boardStep("Doing").assertIssueList("TASKB-6", "TASKB-641", "TASKB-645");
        mainPage.issue("TASKB-645").dragOver("TASKB-641");
        deployable.boardStep("Doing").assertIssueList("TASKB-6", "TASKB-645", "TASKB-641");

        mainPage.openMenuFilters()
            .clickCheckAllFilter("Team");

        mainPage.assertVisibleIssues();
    }
}
