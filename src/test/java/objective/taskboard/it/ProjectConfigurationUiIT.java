package objective.taskboard.it;

import org.junit.Test;

public class ProjectConfigurationUiIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenUpdateProjectConfiguration_verifyItHasValidDateRangeAndProjectionTimespan() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage
            .openMenuFilters()
            .openProjectsConfiguration()
            .openProjectConfigurationModal("TASKB")
            .tryUpdateWithInvalidStartDate()
            .tryUpdateWithInvalidEndDate()
            .tryUpdateWithInvalidDateRange()
            .updateConfiguration("01/01/2018", "03/03/2018");
    }
}
