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
}