package objective.taskboard.it;

import org.junit.Test;

public class TemplateFollowupCrudUiIt extends AuthenticatedIntegrationTest {
    @Test
    public void whenCreateANewFollowupTemplate_VerifyIfItHasNameProjectsAndFile() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage
            .openTemplateFollowUpDialog()
            .tryToCreateATemplateWithoutName()
            .tryToCreateATemplateWithoutSelectAProject()
            .tryToCreateATemplateWithoutSelectAFile()
            .createATemplate("Template Success Test", 1);
    }
}
