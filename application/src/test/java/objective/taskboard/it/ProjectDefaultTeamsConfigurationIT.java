package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.PROJ1;
import static objective.taskboard.testUtils.ProjectInfo.PROJ2;
import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.it.config.project.ProjectDefaultTeamsConfiguration;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectDefaultTeamsConfigurationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;

    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }

    @Test
    public void shouldPersist_andOrderByIssueTypeAfterSave() {
        goToDefaultTeamsConfiguration(PROJ1)
            .assertDefaultTeam("TASKBOARD 2")

            .addDefaultTeamByIssueType()
            .setIssueType(0, "Bug")
            .setTeam(0, "TASKBOARD 1")

            .addDefaultTeamByIssueType()
            .setIssueType(0, "Demand")
            .setTeam(0, "TASKBOARD 2")

            .save()
            .assertSavedNotificationIsOpen()
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 1",
                    "Demand | TASKBOARD 2")

            .refresh()
            .assertDefaultTeam("TASKBOARD 2")
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 1",
                    "Demand | TASKBOARD 2");
    }

    @Test
    public void shouldEdit_andStillOrderByIssueTypeAfterRefresh() {
        goToDefaultTeamsConfiguration(TASKB)
            .assertDefaultTeam("TASKBOARD 1")
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 1",
                    "Sub-task | TASKBOARD 2",
                    "Task | TASKBOARD 1")

            .setDefaultTeam("TASKBOARD 2")

            .removeTeamByIssueType(2)

            .setIssueType(1, "Demand")
            .setTeam(1, "TASKBOARD 2")

            .setIssueType(0, "Alpha Test")

            .save()
            .assertSavedNotificationIsOpen()
            .refresh()

            .assertDefaultTeam("TASKBOARD 2")
            .assertDefaultTeamsByIssueType(
                    "Alpha Test | TASKBOARD 1",
                    "Demand | TASKBOARD 2");
    }

    @Test
    public void shouldDisableTeamsThatUserHasNoPermissionToSee_butUserShouldHavePermissionToEditOtherTeams() {
        goToDefaultTeamsConfiguration(PROJ2)
            .assertDefaultTeam("TASKBOARD 2")
            .assertDefaultTeamsByIssueType(
                    "QA | - (disabled)")

            .addDefaultTeamByIssueType()
            .setIssueType(0, "Bug")
            .setTeam(0, "TASKBOARD 2")

            .save()
            .assertSavedNotificationIsOpen()
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 2",
                    "QA | - (disabled)");
    }

    @Test
    public void shouldShowErrorWhenMoreThanOneIssueTypeAreRegistered() {
        goToDefaultTeamsConfiguration(TASKB)
            .assertDefaultTeam("TASKBOARD 1")
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 1",
                    "Sub-task | TASKBOARD 2",
                    "Task | TASKBOARD 1")

            .setIssueType(1, "Bug")

            .setIssueType(2, "Alpha Test")

            .addDefaultTeamByIssueType()
            .setIssueType(0, "Alpha Test")
            .setTeam(0, "TASKBOARD 2")

            .save()
            .assertErrorsNotificationIsOpen(
                    "Issue Type \"Alpha Test\" repeated.",
                    "Issue Type \"Bug\" repeated.");
    }

    @Test
    public void whenEdit_theNewValuesShouldAppersOnTheCards() {
        mainPage.issue("TASKB-611")
            .click()
            .issueDetails()
            .assertIssueType("Demand")
            .assertIsDefaultTeam("TASKBOARD 1")
            .closeDialog();

        mainPage.issue("TASKB-601")
            .click()
            .issueDetails()
            .assertIssueType("Backend Development")
            .assertIsDefaultTeam("TASKBOARD 1")
            .closeDialog();

        mainPage
            .issue("TASKB-637")
            .click()
            .issueDetails()
            .assertIssueType("Task")
            .assertIsTeamByIssueType("TASKBOARD 1", "Task", TASKB.name)
            .closeDialog();

        mainPage.issue("TASKB-625")
            .click()
            .issueDetails()
            .assertIssueType("Feature Review")
            .assertAreInheritedTeams("TASKBOARD 1")
            .closeDialog();

        goToDefaultTeamsConfiguration(TASKB)
            .assertDefaultTeam("TASKBOARD 1")
            .assertDefaultTeamsByIssueType(
                    "Bug | TASKBOARD 1",
                    "Sub-task | TASKBOARD 2",
                    "Task | TASKBOARD 1")

            .setDefaultTeam("TASKBOARD 2")

            .setIssueType(2, "Demand")
            .setTeam(2, "TASKBOARD 2")

            .addDefaultTeamByIssueType()
            .setIssueType(0, "Backend Development")
            .setTeam(0, "TASKBOARD 2")

            .save()
            .assertSavedNotificationIsOpen()
            .backToProject()
            .close();

        mainPage.issue("TASKB-611")
            .click()
            .issueDetails()
            .assertIssueType("Demand")
            .assertIsTeamByIssueType("TASKBOARD 2", "Demand", TASKB.name)
            .closeDialog();

        mainPage.issue("TASKB-601")
            .click()
            .issueDetails()
            .assertIssueType("Backend Development")
            .assertIsTeamByIssueType("TASKBOARD 2", "Backend Development", TASKB.name)
            .closeDialog();

        mainPage
            .issue("TASKB-637")
            .click()
            .issueDetails()
            .assertIssueType("Task")
            .assertIsDefaultTeam("TASKBOARD 2")
            .closeDialog();

        mainPage.issue("TASKB-625")
            .click()
            .issueDetails()
            .assertIssueType("Feature Review")
            .assertIsDefaultTeam("TASKBOARD 2")
            .closeDialog();
    }

    public ProjectDefaultTeamsConfiguration goToDefaultTeamsConfiguration(ProjectInfo projectInfo) {
        return ProjectConfigurationOperator.openFromMainMenu(mainPage, projectInfo)
            .openAdvancedConfigurations()
            .selectTeamsConfiguration();
    }

}
