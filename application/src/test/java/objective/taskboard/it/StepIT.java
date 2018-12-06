package objective.taskboard.it;

import static objective.taskboard.it.IssueUpdateFieldJson.CLASS_OF_SERVICE_EXPEDITE;
import static objective.taskboard.it.IssueUpdateFieldJson.CLASS_OF_SERVICE_STANDARD;

import org.junit.Test;

public class StepIT extends AuthenticatedIntegrationTest {

    @Test
    public void expediteIssuesMustBeOnExpediteArea_orderedByCreateDate() {
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

    @Test
    public void givenStandardClassOfServiceIssue_whenChangedToExpedite_thenChangeTheIssueToExpediteArea() throws InterruptedException {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        final String DEMAND_TO_EXPEDITE = "TASKB-606";

        BoardStepFragment demandDoing = mainPage.lane("Demand").boardStep("Doing");
        demandDoing.assertIssueList(
                "TASKB-611",
                "TASKB-612",
                DEMAND_TO_EXPEDITE,
                "TASKB-608",
                "TASKB-604",
                "TASKB-605",
                "TASKB-609",
                "TASKB-613",
                "TASKB-607",
                "TASKB-610"
                );

        emulateUpdateIssue("TASKB-606", CLASS_OF_SERVICE_EXPEDITE);

        demandDoing.assertIssueList(
                DEMAND_TO_EXPEDITE,
                "TASKB-611",
                "TASKB-612",
                "TASKB-608",
                "TASKB-604",
                "TASKB-605",
                "TASKB-609",
                "TASKB-613",
                "TASKB-607",
                "TASKB-610"
                );
    }

    @Test
    public void givenExpediteClassOfServiceIssue_whenChangedToExpedite_thenChangeTheIssueToExpediteArea() throws InterruptedException {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();

        final String DEMAND_TO_EXPEDITE = "TASKB-606";

        emulateUpdateIssue("TASKB-606", CLASS_OF_SERVICE_EXPEDITE);

        BoardStepFragment demandDoing = mainPage.lane("Demand").boardStep("Doing");
        demandDoing.assertIssueList(
                DEMAND_TO_EXPEDITE,
                "TASKB-611",
                "TASKB-612",
                "TASKB-608",
                "TASKB-604",
                "TASKB-605",
                "TASKB-609",
                "TASKB-613",
                "TASKB-607",
                "TASKB-610"
                );

        emulateUpdateIssue("TASKB-606", CLASS_OF_SERVICE_STANDARD);

        demandDoing.assertIssueList(
                "TASKB-611",
                "TASKB-612",
                DEMAND_TO_EXPEDITE,
                "TASKB-608",
                "TASKB-604",
                "TASKB-605",
                "TASKB-609",
                "TASKB-613",
                "TASKB-607",
                "TASKB-610"
                );
    }

}
