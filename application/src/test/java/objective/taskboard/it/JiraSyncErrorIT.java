package objective.taskboard.it;

import org.junit.Test;

import objective.taskboard.testUtils.JiraMockController;

public class JiraSyncErrorIT extends AuthenticatedIntegrationTest {
    @Test
    public void sucessfulStartup_ShouldShowOkIcon() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.assertOkIcon();
    }
    
    @Test
    public void whenErrorDuringInitialization_ShouldShowErrorIcon() {
        JiraMockController.emulateSearchError();
        resetIssueBuffer();
        
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.reload();
        mainPage.assertStatusIconIsInitialisationError();
    }
}
