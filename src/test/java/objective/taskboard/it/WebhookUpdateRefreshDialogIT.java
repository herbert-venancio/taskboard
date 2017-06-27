package objective.taskboard.it;

import java.io.IOException;

import org.junit.Test;

import objective.taskboard.RequestBuilder;
import objective.taskboard.TestUtils;

public class WebhookUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenUpdateHappensViaWebHook_RefreshToastShouldShowUP() throws IOException {
        MainPage mainPage = MainPage.produce(webDriver);
        String body = TestUtils.loadResource(getClass(), "/webhook/TASKB_625_updatePayload.json");
        RequestBuilder
            .url(getSiteBase()+"/webhook/TASKB")
            .header("Content-Type", "application/json")
            .body(body)
            .post();
        
        mainPage.typeSearch("TASKB-61");
        mainPage.refreshToast().assertVisible();
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-625");
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-610", "TASKB-611", "TASKB-612", "TASKB-613");
        mainPage.refreshToast().toggleShowHide();
        mainPage.refreshToast().dismiss();
        mainPage.refreshToast().assertNotVisible();
        mainPage.assertVisibleIssues("TASKB-610", "TASKB-611", "TASKB-612", "TASKB-613");
    }
}
