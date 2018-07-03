package objective.taskboard.it;

import org.junit.Test;

public class StepIT extends AuthenticatedIntegrationTest {

    @Test
    public void expediteIssuesMustBeOnFeaturedArea_orderedByCreateDate() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        final String REVIEW_EXPEDITE_1 = "TASKB-637";
        mainPage.lane("Deployable").boardStep("To Feature Review").assertIssueList(
                REVIEW_EXPEDITE_1,
                "TASKB-626",
                "TASKB-639",
                "TASKB-685"
                );

        final String DONE_EXPEDITE_1 = "TASKB-638";
        final String DONE_EXPEDITE_2 = "TASKB-678";
        final String DONE_EXPEDITE_3 = "TASKB-679";
        mainPage.lane("Operational").boardStep("Done").assertIssueList(
                DONE_EXPEDITE_1,
                DONE_EXPEDITE_2,
                DONE_EXPEDITE_3,
                "TASKB-656",
                "TASKB-657",
                "TASKB-658",
                "TASKB-660",
                "TASKB-662"
                );
    }

}
