package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.it.config.project.ProjectClusterConfiguration;
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
                .selectProfileTab().expectConfirmationAndStay()

            .save()
                .selectProfileTab().expectNoConfirmation()
                .goToClusterConfiguration()
                .setEffort("Alpha Test", "S", "2.13")
                .selectProfileTab().expectConfirmationAndLeave()

            .goToClusterConfiguration()
                .assertEffort("Alpha Test", "S", "3.13");
    }

    @Test
    public void whenRecalculate_shouldShowCurrentValueAndNewValueFields() {
        ProjectClusterConfiguration projectClusterConfigurationTab = goToClusterConfiguration(TASKB);
        projectClusterConfigurationTab
                .openRecalculate()
                    .setStartDate("asdf")
                    .setEndDate("99/99/9999")
                    .assertStartDateHasNoError()
                    .assertEndDateHasNoError()
                .recalculate()
                    .assertRecalculateIsOpened()
                    .assertStartDateHasError()
                    .assertEndDateHasError()
                    .setStartDate("12/31/2017")
                    .setEndDate("01/01/2015")
                .recalculate()
                    .assertRecalculateIsOpened()
                    .assertEndDateHasError()
                    .setStartDate("01/01/2015")
                    .setEndDate("12/31/2017")
                .recalculate()
                    .assertRecalculateIsClosed();

        projectClusterConfigurationTab
                .assertCurrentValueNewValueIsShown("Alpha Test", "XS")
                .assertCurrentValueNewValueIsShown("Alpha Bug", "S")
                .assertCurrentValueNewValueIsShown("Backend Development", "M")
                .assertCurrentValueNewValueIsShown("BALLPARK - Alpha Test", "L")
                .assertCurrentValueNewValueIsShown("BALLPARK - Planning", "XL")
                    .setEffort("Alpha Test", "XS", "4.5")
                    .setCycle("BALLPARK - Alpha Test", "L", "14.5")
                .save()
                    .assertEffort("Alpha Test", "XS", "4.5")
                    .assertCycle("BALLPARK - Alpha Test", "L", "14.5");
    }

    private ProjectClusterConfiguration goToClusterConfiguration(ProjectInfo projectInfo) {
        return ProjectConfigurationOperator.openFromMainMenu(mainPage, projectInfo)
            .openAdvancedConfigurations()
                .selectClusterConfiguration();
    }
}
