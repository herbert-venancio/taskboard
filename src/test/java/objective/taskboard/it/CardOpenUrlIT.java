package objective.taskboard.it;

import org.junit.Test;

public class CardOpenUrlIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenOpenBookmarkableLink_ShouldOpenThisIssue(){
        webDriver.get(AbstractUIIntegrationTest.getSiteBase()+"/#/card/TASKB-684");
        IssueDetails issueDetails = new IssueDetails(webDriver);
            issueDetails.assertCardName("TASKB-684 Review of teste4")
            .closeDialog();
    }

    @Test
    public void whenClickChildOrParentIssue_ShouldOpenThisIssue(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-626");
        issue
            .click()
            .issueDetails()
            .assertCardName("TASKB-626 teste4")
            .openFirstChildCard()
            .assertCardName("TASKB-684 Review of teste4")
            .openParentCard()
            .assertCardName("TASKB-626 teste4")
            .closeDialog();
    }

}
