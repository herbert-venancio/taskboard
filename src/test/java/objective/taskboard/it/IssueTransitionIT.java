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

import org.junit.Test;

public class IssueTransitionIT extends AuthenticatedIntegrationTest {
    
    @Test
    public void whenTransitionIsPerformed_ShouldRemoveIssueFromSourceStepAndMoveToTarget(){
        MainPage mainPage = MainPage.produce(webDriver);
        
        LaneFragment operational = mainPage.lane("Operational");
        
        operational.boardStep("To Do").issueCountBadge(14);
        operational.boardStep("To Do").assertIssueList(
                "TASKB-625",
                "TASKB-627",
                "TASKB-643",
                "TASKB-644",
                "TASKB-659",
                "TASKB-661",
                "TASKB-663",
                "TASKB-664",
                "TASKB-680",
                "TASKB-681",
                "TASKB-682",
                "TASKB-683",
                "TASKB-684",
                "TASKB-686"
                );
        operational.boardStep("Doing").issueCountBadge(2);
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-646"
                );        
        
        mainPage.issue("TASKB-625")
            .click()
            .issueDetails()
            .transitionClick("Doing")
            .confirm();
        
        mainPage.issueDetails().assertIsHidden();

        operational.boardStep("To Do").issueCountBadge(13);
        operational.boardStep("To Do").assertIssueList(
                "TASKB-627",
                "TASKB-643",
                "TASKB-644",
                "TASKB-659",
                "TASKB-661",
                "TASKB-663",
                "TASKB-664",
                "TASKB-680",
                "TASKB-681",
                "TASKB-682",
                "TASKB-683",
                "TASKB-684",
                "TASKB-686"
                );
        
        operational.boardStep("Doing").issueCountBadge(3);
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-625",
                "TASKB-646"
                );
    }
}
