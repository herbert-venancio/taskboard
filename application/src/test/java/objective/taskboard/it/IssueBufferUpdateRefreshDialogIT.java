package objective.taskboard.it;

import org.junit.Test;

import objective.taskboard.RequestBuilder;
import objective.taskboard.testUtils.JiraMockController;

public class IssueBufferUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {

    @Test
    public void givenChildrenNotVisibleBecauseOfParent_whenDeleteTheIssueLink_thenChildrenAndRefreshToastShouldShowUp() {
        MainPage.produce(webDriver)
            .typeSearch("TASKB-628").assertVisibleIssues()
            .typeSearch("TASKB-630").assertVisibleIssues()
            .typeSearch("TASKB-633").assertVisibleIssues()
            .typeSearch("TASKB-634").assertVisibleIssues()
            .clearSearch().errorToast().close();

        JiraMockController.enableSearchAfterInit();
        forceUpdateIssueBuffer();

        MainPage mainPage = MainPage.produce(webDriver);
        String[] updatedIssues = {"TASKB-630", "TASKB-633", "TASKB-634"};
        mainPage.assertUpdatedIssues(updatedIssues);
        mainPage.refreshToast().assertVisible().showOnlyUpdated();
        mainPage.assertVisibleIssues(updatedIssues);
        mainPage.refreshToast().dismiss();
        mainPage.lane("Demand").boardStep("Open").assertIssueList("TASKB-20");
        MainPage typeSearch = mainPage.typeSearch("TASKB-628");
		typeSearch.assertVisibleIssues();
    }

    private void forceUpdateIssueBuffer() {
        RequestBuilder.url(getSiteBase() + "/test/force-update-issue-buffer")
            .credentials("foo", "bar").get();
    }
}
