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
        
        mainPage.refreshToast().assertVisible();
    }
}
