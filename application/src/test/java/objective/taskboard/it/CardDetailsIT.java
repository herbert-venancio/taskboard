package objective.taskboard.it;

import org.junit.Test;

public class CardDetailsIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenDescriptionIsChanged_ShouldKeepNewValueAfterReloadPage() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        mainPage.issue("TASKB-235")
            .click()
            .issueDetails()
            .setDescription("Test");

        mainPage.reload();

        mainPage.issue("TASKB-235")
            .issueDetails()
            .assertDescription("Test");

    }

    @Test
    public void whenTaskIsBlockedAndUnblocked_ShouldShowAndHideBlockedReasonLabel() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        mainPage.issue("TASKB-235")
                .click()
                .issueDetails()
                .assertIsNotBlocked()
                .blockTask()
                .assertIsBlocked();

        mainPage.reload();

        mainPage.issue("TASKB-235")
                .issueDetails()
                .assertIsBlocked()
                .unblockTask()
                .assertIsNotBlocked();
    }

    @Test
    public void whenTaskIsBlockedAndUnblocked_ShouldBlockAndUnBlockSubtasks() {
        MainPage mainPage = MainPage.produce(webDriver);

        registerWebhook("jira:issue_updated");

        mainPage.errorToast().close();

        mainPage.issue("TASKB-637")
                .click()
                .issueDetails()
                .assertIsNotBlocked()
                .blockTask()
                .assertIsBlocked()
                .closeDialog();

        mainPage.issue("TASKB-680")
                .assertHasBlockedIcon(true)
                .click()
                .issueDetails()
                .assertIsBlocked()
                .closeDialog();

        mainPage.issue("TASKB-637")
                .click()
                .issueDetails()
                .assertIsBlocked()
                .unblockTask()
                .assertIsNotBlocked()
                .closeDialog();

        mainPage.issue("TASKB-680")
                .assertHasBlockedIcon(false)
                .click()
                .issueDetails()
                .assertIsNotBlocked();
    }

    @Test
    public void whenClassOfServiceIsReplaced_ShouldUpdateIssueImmediatlyWithNewClassOfService() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        mainPage.issue("TASKB-235")
            .click()
            .issueDetails()
            .assertClassOfService("Standard")
            .setClassOfService("Expedite");

        mainPage.reload();
        
        mainPage.issue("TASKB-235")
            .issueDetails()
            .assertClassOfService("Expedite");
    }

    @Test
    public void whenSummaryIsCanceled_ShouldKeepOldValueAfterReloadPage() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        mainPage.issue("TASKB-601")
            .click()
            .issueDetails()
            .assertSummary("Desenvolvimento");
        
        mainPage.issue("TASKB-601")
            .issueDetails()
            .setSummaryAndCancel("New title for issue")
            .assertSummary("Desenvolvimento");

        mainPage.reload();

        mainPage.issue("TASKB-601")
            .issueDetails()
            .assertSummary("Desenvolvimento");
    }

    @Test
    public void whenSummaryIsChanged_ShouldKeepNewValueAfterReloadPage() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();
        
        mainPage.issue("TASKB-235")
            .click()
            .issueDetails()
            .setSummary("New title for issue");
    
        mainPage.reload();
    
        mainPage.issue("TASKB-235")
            .issueDetails()
            .assertSummary("New title for issue");
    }
        
    @Test
    public void whenSizeIsReplaced_ShouldUpdateIssueImmediatlyWithNewSize() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        mainPage.issue("TASKB-535")
            .click()
            .issueDetails()
            .assertTShirtSize("S")
            .setTShirtSize("M");

        mainPage.reload();

        mainPage.issue("TASKB-535")
            .issueDetails()
            .assertTShirtSize("M");
    }

    @Test
    public void whenBallparkSizeIsChanged_ShouldUpdateIssueImmediatlyWithNewBallparkSize() {
        MainPage mainPage = MainPage.produce(webDriver);

    mainPage.errorToast().close();

    mainPage.issue("TASKB-235")
        .click()
        .issueDetails()
        .setBallparkSize("XL");

    mainPage.reload();

    mainPage.issue("TASKB-235")
        .issueDetails()
        .assertBallparkSize("XL");
    }
}
