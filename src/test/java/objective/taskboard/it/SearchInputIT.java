package objective.taskboard.it;

import org.junit.Test;

public class SearchInputIT extends AbstractUIIntegrationTest {
    @Test
    public void whenIssueKeyIsTyped_onlyOneIssueShouldBeFound() {
        LoginPage.to(webDriver).login("foo", "bar");
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.waitUserLabelToBe("foo");
        mainPage.typeSearch("TASKB-61");
        mainPage.assertVisibleIssues("TASKB-610", "TASKB-611", "TASKB-612", "TASKB-613");
    }
}
