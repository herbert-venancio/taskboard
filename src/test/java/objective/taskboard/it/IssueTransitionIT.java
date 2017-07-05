package objective.taskboard.it;

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
