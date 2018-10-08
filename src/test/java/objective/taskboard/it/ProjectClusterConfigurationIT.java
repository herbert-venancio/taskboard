package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.testUtils.ProjectInfo;

public class ProjectClusterConfigurationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;

    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }

    @Test
    public void shouldSaveOnlyValidValues() {
        goToClusterConfiguration(TASKB)
            .assertSaveDisabled(true)
            .setEffort("Alpha Test", "S", "")
            .setCycle("Alpha Bug", "M", "-1")
            .assertSaveDisabled(false)
            .save()
            .assertSnackbarErrorIsOpen()

            .assertIsFromBaseCluster("Alpha Test", "S", true)
            .setEffort("Alpha Test", "S", "3.13")
            .setCycle("Alpha Bug", "M", "4")
            .save()
            .assertIsFromBaseCluster("Alpha Test", "S", false)
            .assertSnackbarSavedIsOpen()
            .assertSaveDisabled(true)
            .refresh()
            .assertIsFromBaseCluster("Alpha Test", "S", false)
            .assertEffort("Alpha Test", "S", "3.13")
            .assertCycle("Alpha Bug", "M", "4");
    }

    @Test
    public void whenLeaveWithoutSave_shouldConfirm() {
        goToClusterConfiguration(TASKB)
            .setEffort("Alpha Test", "S", "3.13")
            .tryToGoToProfileConfigurationButStay()

            .save()
            .goToProfileConfigurationWithoutLeaveConfirmation()
            .goToClusterConfiguration()
            .setEffort("Alpha Test", "S", "2.13")
            .goToProfileConfigurationWithLeaveConfirmation()

            .goToClusterConfiguration()
            .assertEffort("Alpha Test", "S", "3.13");
    }

    private ProjectClusterConfiguration goToClusterConfiguration(ProjectInfo projectInfo) {
        return ProjectConfigurationOperator.openFromMainMenu(mainPage, projectInfo)
            .openAdvancedConfigurations()
            .selectClusterConfiguration();
    }
}
