package objective.taskboard.it;

import org.junit.Test;

public class HierarchicalFilterTest extends AbstractUIIntegrationTest {
    @Test
    public void whenIssueFilterIsEnabled_OnlyIssueAndChildrenShowUp() {
        LoginPage.to(webDriver).login("foo", "bar");
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.waitUserLabelToBe("foo");
        mainPage.issue("TASKB-637").enableHierarchicalFilter();
        mainPage.assertVisibleIssues("TASKB-637", "TASKB-680", "TASKB-638", "TASKB-678", "TASKB-679");
    }
}
