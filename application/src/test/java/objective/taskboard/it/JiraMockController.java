package objective.taskboard.it;

import objective.taskboard.RequestBuilder;

public class JiraMockController {
    private static final String JIRA_MOCK_URL = "http://localhost:4567";

    public static void resetMock() {
        while(true) {
            try {
                RequestBuilder.url(JIRA_MOCK_URL + "/reset").post();
                return;
            }catch(Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
    }
    
    public static void emulateSearchError() {
        RequestBuilder.url(JIRA_MOCK_URL + "/force-search-failure").post();
    }
    
    public static void clearSearchError() {
        RequestBuilder.url(JIRA_MOCK_URL + "/fix-search-failure").post();
    }

    public static void enableSearchAfterInit() {
        RequestBuilder.url(JIRA_MOCK_URL + "/enable-search-after-init").post();
    }

    public static void disableSearchAfterInit() {
        RequestBuilder.url(JIRA_MOCK_URL + "/disable-search-after-init").post();
    }

    public static void emulateTransitionError() {
        RequestBuilder.url(JIRA_MOCK_URL + "/force-transition-failure").post();
    }

    public static void clearTransitionError() {
        RequestBuilder.url(JIRA_MOCK_URL + "/fix-transition-failure").post();
    }
}
