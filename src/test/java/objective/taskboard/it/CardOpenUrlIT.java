package objective.taskboard.it;

import static objective.taskboard.it.IssueUpdateFieldJson.STATUS_DEFERRED;

import org.junit.Test;

public class CardOpenUrlIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenOpenBookmarkableLink_ShouldOpenThisIssue(){
        new MainPage(webDriver).navigateTo("/#/card/TASKB-684");
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

    @Test
    public void whenOpenDeferredIssue_ShouldOpenThisIssue(){
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        emulateUpdateIssue("TASKB-606", STATUS_DEFERRED);
        mainPage.typeSearch("TASKB-606").assertVisibleIssues();
        mainPage.clearSearch();

        mainPage.navigateTo("/#/card/TASKB-606");
        mainPage.issueDetails().assertCardName("TASKB-606 Onda 6 - Pacote 3");
    }
}
