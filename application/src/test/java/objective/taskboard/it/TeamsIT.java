package objective.taskboard.it;

import org.junit.Test;

import objective.taskboard.it.components.guards.AccessGuard;
import objective.taskboard.testUtils.LoginUtils;

public class TeamsIT  extends AbstractUIWithCoverageIntegrationTest {

    @Test
    public void ifProjectAdministrator_shouldntAcess() {
        LoginUtils.doLoginAsProjectAdministrator(webDriver)
            .openMenuFilters()
            .assertTeamsConfigurationExistenceBe(false);

        AccessGuard.assertCantAccess(webDriver, TeamsPage.getPageUrl());
        AccessGuard.assertCantAccess(webDriver, TeamPage.getPageUrl("TASKBOARD 1"));
    }

    @Test
    public void ifDeveloper_shouldntAcess() {
        LoginUtils.doLoginAsCustomer(webDriver)
            .openMenuFilters()
            .assertTeamsConfigurationExistenceBe(false);

        AccessGuard.assertCantAccess(webDriver, TeamsPage.getPageUrl());
        AccessGuard.assertCantAccess(webDriver, TeamPage.getPageUrl("TASKBOARD 1"));
    }

    @Test
    public void ifCustomer_shouldntAcess() {
        LoginUtils.doLoginAsDeveloper(webDriver)
            .openMenuFilters()
            .assertTeamsConfigurationExistenceBe(false);

        AccessGuard.assertCantAccess(webDriver, TeamsPage.getPageUrl());
        AccessGuard.assertCantAccess(webDriver, TeamPage.getPageUrl("TASKBOARD 1"));
    }

    @Test
    public void shouldValidate_shouldSave_andKeepOrderByNameAfterRefresh() {
        openTeamsConfigurationAsAdmin()
            .selectTeam("TASKBOARD 1")

            .assertManager("taskboard@objective.com.br")
            .assertVisibleMembers("foo", "taskboard")
            .assertSaveDisabled(true)

            .setManager("adolpho")
            .addMember()
            .addMember()
            .assertVisibleMembers("(row-added:New) ", "(row-added:New) ", "foo", "taskboard")

            .setMember(1, "guilherme")
            .assertVisibleMembers("(row-added:New) ", "(row-added:New) guilherme", "foo", "taskboard")

            .assertSaveDisabled(false)
            .save()
            .assertErrorsNotificationIsOpen("Please review the form")

            .removeMember(0)
            .save()
            .assertSaveDisabled(true)
            .assertVisibleMembers("foo", "guilherme", "taskboard")

            .refreshWithoutConfirmation()
            .assertManager("adolpho")
            .assertVisibleMembers("foo", "guilherme", "taskboard");
    }

    @Test
    public void componentsImplementationsShouldWork() {
        TeamsPage teamsPage = openTeamsConfigurationAsAdmin();

        testFilters(teamsPage);
        testTags(teamsPage);
        testLeavePageConfirmation(teamsPage);
        testSaveIsDisabledWhileFormIsPristine(teamsPage);
    }

    private void testFilters(TeamsPage teamsPage) {
        teamsPage
            .filterTeams("task")
            .assertVisibleTeams(
                    "TASKBOARD 1 | taskboard@objective.com.br | foo, taskboard",
                    "TASKBOARD 2 | jean.takano@objective.com.br | gtakeuchi, jean.takano, jhony.gomes, lohandus.ribeiro, nazar")
            .selectTeam("TASKBOARD 1")

            .assertVisibleMembers("foo", "taskboard")
            .addMember()
            .setMember(0, "guilherme")
            .assertVisibleMembers("(row-added:New) guilherme", "foo", "taskboard")

            .filterMembers("r")
            .assertVisibleMembers("(row-added:New) guilherme", "taskboard")
            .clearFilterMembers()
            .assertVisibleMembers("(row-added:New) guilherme", "foo", "taskboard")

            .backToTeamsAndLeave();
    }

    private void testTags(TeamsPage teamsPage) {
        teamsPage
            .selectTeam("TASKBOARD 1")

            .assertVisibleMembers("foo", "taskboard")
            .addMember()
            .assertVisibleMembers("(row-added:New) ", "foo", "taskboard")
            .addMember()
            .assertVisibleMembers("(row-added:New) ", "(row-added:New) ", "foo", "taskboard")
            .setMember(1, "alexandre")
            .assertVisibleMembers("(row-added:New) ", "(row-added:New) alexandre", "foo", "taskboard")
            .removeMember(0)
            .removeMember(2)
            .assertVisibleMembers("(row-added:New) alexandre", "foo")

            .backToTeamsAndLeave();
    }

    private void testLeavePageConfirmation(TeamsPage teamsPage) {
        teamsPage
            .selectTeam("TASKBOARD 1")
            .backToTeamsWithoutConfirmation()

            .selectTeam("TASKBOARD 1")
            .refreshWithoutConfirmation()
            .backToTeamsWithoutConfirmation()

            .selectTeam("TASKBOARD 1")

            .addMember()
            .backToTeamsAndStay()

            .backToTeamsAndLeave();
    }

    private void testSaveIsDisabledWhileFormIsPristine(TeamsPage teamsPage) {
        teamsPage
            .selectTeam("TASKBOARD 1")

            .assertSaveDisabled(true)
            .setManager("alexandre")
            .assertSaveDisabled(false)

            .backToTeamsAndLeave()

            .selectTeam("TASKBOARD 1")

            .assertSaveDisabled(true)
            .addMember()
            .assertSaveDisabled(false)
            .setMember(0, "alexandre")
            .assertSaveDisabled(false)

            .backToTeamsAndLeave()

            .selectTeam("TASKBOARD 1")

            .assertSaveDisabled(true)
            .removeMember(0)
            .assertSaveDisabled(false)

            .backToTeamsAndLeave();
    }

    private TeamsPage openTeamsConfigurationAsAdmin() {
        return LoginUtils.doLoginAsAdmin(webDriver)
                .openMenuFilters()
                .openTeamsConfiguration();
    }

}
