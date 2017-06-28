package objective.taskboard.it;

import org.junit.Test;

public class AssignToMeIT extends AuthenticatedIntegrationTest {
    
    @Test
    public void whenAssignToMeIsClicked_ShouldUpdateIssueImmediatlyWithAssignedUser(){
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.issue("TASKB-625")
            .click()
            .testDetails()
            .assignToMe();
        mainPage.issue("TASKB-625").testDetails().isHidden();
        mainPage.issue("TASKB-625").assertHasFirstAssignee();
    }
}
