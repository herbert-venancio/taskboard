package objective.taskboard.it;

import org.junit.Test;

public class CardTeamsIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenIssueIsOpenWithouTeam_ShouldHaveDefaultTeam(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-601");
        issue
            .click()
            .issueDetails()
            .assertIsDefaultTeam("TASKBOARD 1");
    }

    @Test
    public void whenIssueIsOpenWithouTeam_ButProjectHasTeamByIssueType_ShouldHaveTeamByIssueType(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-637");
        issue
            .click()
            .issueDetails()
            .assertIssueType("Task")
            .assertIsTeamByIssueType("TASKBOARD 1", "Task", "Taskboard");
    }

    @Test
    public void whenIssueHasTeamInheritedFromParent_ShouldHaveSameTeamsAsItsParent(){
        MainPage mainPage = MainPage.produce(webDriver);

        final String expectedTeamByIssueType = "TASKBOARD 1";

        TestIssue parent = mainPage.issue("TASKB-624");
        parent
            .click()
            .issueDetails()
            .assertIsTeamByIssueType(expectedTeamByIssueType, "Task", "Taskboard")
            .closeDialog();

        TestIssue issue = mainPage.issue("TASKB-625");
        issue
            .click()
            .issueDetails()
            .assertAreInheritedTeams(expectedTeamByIssueType);
    }

    @Test
    public void whenAddTeam_ShouldUpdateIssueImmediatlyWithNewTeam(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-647");
        issue
            .click()
            .issueDetails()
            .addTeam("FFC")
            .assertTeams("TASKBOARD 2", "FFC");
    }

    @Test
    public void whenTeamIsReplaced_ShouldUpdateIssueImmediatlyWithNewTeam(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-601");
        issue
            .click()
            .issueDetails()
            .replaceTeam("TASKBOARD 1", "FFC")
            .assertTeams("FFC");
    }

    @Test
    public void whenRemovingTeam_ShouldUpdateIssueImmediatlyWithNewTeam(){
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-647");
        issue
            .click()
            .issueDetails()
            .addTeam("FFC")
            .removeTeam("TASKBOARD 2")
            .assertTeams("FFC");
    }

    @Test
    public void whenAddTeam_ShouldUpdateChildIssues(){
        registerWebhook("jira:issue_updated");
        MainPage mainPage = MainPage.produce(webDriver);
        TestIssue issue = mainPage.issue("TASKB-626");
        issue
            .click()
            .issueDetails()
            .addTeam("TASKBOARD 2")
            .assertTeams("TASKBOARD 1", "TASKBOARD 2")
            .closeDialog();

        mainPage.refreshToast().assertVisible().showOnlyUpdated();
        mainPage.assertVisibleIssues("TASKB-627", "TASKB-681", "TASKB-682", "TASKB-683", "TASKB-684");
        mainPage.refreshToast().dismiss().assertNotVisible();
        mainPage.lane("Operational")
            .boardStep("To Do")
            .assertIssueList(
                    "TASKB-680",
                    "TASKB-625",
                    "TASKB-627",
                    "TASKB-643",
                    "TASKB-644",
                    "TASKB-659",
                    "TASKB-661",
                    "TASKB-663",
                    "TASKB-664",
                    "TASKB-681",
                    "TASKB-682",
                    "TASKB-683",
                    "TASKB-684",
                    "TASKB-686");

        TestIssue updatedIssue = mainPage.issue("TASKB-627");
        updatedIssue
            .click()
            .issueDetails()
            .assertTeams("TASKBOARD 1", "TASKBOARD 2");
    }
}
