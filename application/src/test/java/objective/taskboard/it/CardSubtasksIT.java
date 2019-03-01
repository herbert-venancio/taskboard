package objective.taskboard.it;

import org.junit.Test;

public class CardSubtasksIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenNewSubtaskIsAdded_shouldUpdateSubtaskListWithTheNewItem() {
        MainPage mainPage = MainPage.produce(webDriver);

        mainPage.errorToast().close();

        IssueDetails issueDetailsBeforeReload = mainPage.issue("TASKB-624")
            .click()
            .issueDetails();

        issueDetailsBeforeReload
            .assertSubtasks("TASKB-625")
            .addSubtaskForm()
            .addSubtaskIssueType("Feature Review")
            .addSubtaskSummary("Sub-task for issue TASKB-624")
            .addSubtaskSize("S")
            .saveSubtaskForm();

        emulateCreateIssueTaskb800subtaskOf624();

        issueDetailsBeforeReload
            .assertRefreshWarnIsOpen()
            .clickOnRefreshWarning()
            .assertSubtasks("TASKB-625", "TASKB-800");

        mainPage.reload();

        mainPage.issue("TASKB-800");

        mainPage.issue("TASKB-624")
            .issueDetails()
            .assertSubtasks("TASKB-625", "TASKB-800");
    }
}
