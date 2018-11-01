package objective.taskboard.it;

import org.junit.Before;
import org.junit.Test;

public class ReprioritizationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;

    @Before
    public void before() {
        mainPage = MainPage.produce(webDriver);
    }

    @Test
    public void whenTryingToChangePriorityOrderOfAExpediteIssue_nothingChanges() {
        mainPage.errorToast().close();

        final String EXPEDITE_ISSUE_1 = "TASKB-638";
        final String EXPEDITE_ISSUE_2 = "TASKB-678";
        final String EXPEDITE_ISSUE_3 = "TASKB-679";
        final String REGULAR_ISSUE = "TASKB-656";
        final String[] expectedIssueList = new String[] {
                EXPEDITE_ISSUE_1,
                EXPEDITE_ISSUE_2,
                EXPEDITE_ISSUE_3,
                REGULAR_ISSUE,
                "TASKB-657",
                "TASKB-658",
                "TASKB-660",
                "TASKB-662"
                };

        BoardStepFragment boardStepDone = mainPage.lane("Operational").boardStep("Done");

        boardStepDone.assertIssueList(expectedIssueList);

        mainPage.issue(EXPEDITE_ISSUE_2).dragOver(EXPEDITE_ISSUE_1);

        boardStepDone.assertIssueList(expectedIssueList);

        boardStepDone.scrollTo(EXPEDITE_ISSUE_3);

        mainPage.issue(REGULAR_ISSUE).dragOver(EXPEDITE_ISSUE_3);

        boardStepDone.assertIssueList(expectedIssueList);
    }

    @Test
    public void whenIssueIsDragged_AfterReloadItShouldKeepOrder() {
        mainPage.errorToast().close();

        LaneFragment operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                );

        mainPage.issue("TASKB-572").dragOver("TASKB-601");
        mainPage.reload();
        mainPage.errorToast().close();

        operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(
                "TASKB-572",
                "TASKB-601",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                );
    }

    @Test
    public void whenIssueIsPriorityOrderIsChanged_ShouldShowNotificationInAnotherBrowserAndUpdateTheOrder() {
        final String[] expectedIssueListBefore = new String[] {
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                };

        final String[] expectedIssueListAfter = new String[] {
                "TASKB-572",
                "TASKB-601",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
                };

        createAndSwitchToNewTab();

        MainPage secondTabPage = MainPage.to(webDriver);
        secondTabPage.waitUserLabelToBe("Foo");
        secondTabPage.errorToast().close();

        LaneFragment operationalInSecondTab = secondTabPage.lane("Operational");
        operationalInSecondTab.boardStep("Doing").assertIssueList(expectedIssueListBefore);
        secondTabPage.issue("TASKB-572").dragOver("TASKB-601");
        operationalInSecondTab.boardStep("Doing").assertIssueList(expectedIssueListAfter);

        switchToFirstTab();

        mainPage.errorToast().close();
        mainPage.refreshToast().assertVisible();
        LaneFragment operational = mainPage.lane("Operational");
        operational.boardStep("Doing").assertIssueList(expectedIssueListAfter);

        mainPage.refreshToast().close();
        // makes sure the model is correctly updated
        mainPage.typeSearch("TASKB-625");
        mainPage.clearSearch();
        operational.boardStep("Doing").assertIssueList(expectedIssueListAfter);
    }

    @Test
    public void givenIssuesFilteredByCardFieldFilter_whenChangePriorityOrder_thenAfterFiltersShouldWork() {
        mainPage.openMenuFilters()
            .openCardFieldFilters()
            .clickFilterFieldValue("Team", "TASKBOARD 1")
            .closeMenuFilters();

        LaneFragment deployable = mainPage.lane("Deployable");
        deployable.boardStep("Doing").assertIssueList("TASKB-6", "TASKB-641", "TASKB-645");
        mainPage.issue("TASKB-645").dragOver("TASKB-641");
        deployable.boardStep("Doing").assertIssueList("TASKB-6", "TASKB-645", "TASKB-641");

        mainPage.openMenuFilters()
            .clickCheckAllFilter("Team");

        mainPage.assertVisibleIssues();
    }

    @Test
    public void whenDoASingleClickInASelectCard_shouldKeepSelectedAndShowMessage() {
        String[] issuesStep = new String[] {"TASKB-535", "TASKB-614"};

        mainPage.lane("Operational")
            .boardStep("To Review")
            .assertIssueList(issuesStep);

        mainPage.issue("TASKB-614")
            .select()
            .click();

        mainPage.errorToast().assertErrorMessage("Cannot open details of selected Issue. Hold ctrl and click to unselect.");
    }

    @Test
    public void whenIssueIsSelected_dragInDropOfIssuesShouldBeDisabled() {
        final String TASK535 = "TASKB-535";
        final String TASK614 = "TASKB-614";

        String[] issuesStep = new String[] {TASK535, TASK614};

        BoardStepFragment stepToReview = mainPage
                .lane("Operational")
                .boardStep("To Review");

        stepToReview.assertIssueList(issuesStep);

        mainPage.issue(TASK535)
            .select();

        mainPage.assertSelectedIssues(TASK535);

        mainPage
            .issue(TASK614)
            .dragOver(TASK535);

        stepToReview.assertIssueList(issuesStep);
    }

    @Test
    public void whenMoveToTopIsPressed_theCardShouldBeReorderedToTop() {
        String[] issuesStep = new String[] {"TASKB-535", "TASKB-614"};

        BoardStepFragment stepToReview = mainPage
                .lane("Operational")
                .boardStep("To Review");

        stepToReview.assertIssueList(issuesStep);

        mainPage.issue("TASKB-614")
            .select()
            .moveToTop();

        stepToReview.assertIssueList("TASKB-614", "TASKB-535");
    }

    @Test
    public void whenFeatureIsMovedToTop_shouldAlsoMoveAllSubtasksToTop() {
        // given
        final String TASKB194 = "TASKB-194"; // Feature in "Doing" step with 2 sub-tasks
        final String TASKB342 = "TASKB-342"; // Sub-task in "To Do" step
        final String TASKB273 = "TASKB-273"; // Sub-task in "To Do" step, will be moved to "To Review" step

        final String[] expectedDeployableToDoListBefore = new String[] {
                "TASKB-186",
                TASKB194
        };

        final String[] expectedDeployableToDoListAfter = new String[] {
                TASKB194,
                "TASKB-186"
        };

        final String[] expectedOperationalDoingListBefore = new String[] {
                "TASKB-601",
                "TASKB-572",
                TASKB342,
                "TASKB-646"
        };

        final String[] expectedOperationalDoingListAfter = new String[] {
                TASKB342,
                "TASKB-601",
                "TASKB-572",
                "TASKB-646"
        };

        final String[] expectedOperationalToReviewListBefore = new String[] {
                "TASKB-535",
                TASKB273,
                "TASKB-614"
        };

        final String[] expectedOperationalToReviewListAfter = new String[] {
                TASKB273,
                "TASKB-535",
                "TASKB-614"
        };

        BoardStepFragment deployableStep = mainPage
                .lane("Deployable")
                .boardStep("To Do");
        BoardStepFragment operationalDoingStep = mainPage
                .lane("Operational")
                .boardStep("Doing");
        BoardStepFragment operationalToReviewStep = mainPage
                .lane("Operational")
                .boardStep("To Review");

        // move one sub-task to a different step, to make sure it works regardless
        operationalDoingStep.scrollTo(TASKB273);
        mainPage.issue(TASKB273)
                .click()
                .issueDetails()
                .transitionClick("To Review")
                .confirm();

        deployableStep.assertIssueList(expectedDeployableToDoListBefore);
        operationalDoingStep.assertIssueList(expectedOperationalDoingListBefore);
        operationalToReviewStep.assertIssueList(expectedOperationalToReviewListBefore);

        // when move feature to top
        mainPage.issue(TASKB194)
                .select()
                .moveToTop();

        // then
        deployableStep.assertIssueList(expectedDeployableToDoListAfter);
        operationalDoingStep.assertIssueList(expectedOperationalDoingListAfter);
        operationalToReviewStep.assertIssueList(expectedOperationalToReviewListAfter);
    }

    @Test
    public void whenTheCardIsSelectedAndUnselected_dragAndDropAndMoveToTopShouldWork() {
        final String TASK535 = "TASKB-535";
        final String TASK614 = "TASKB-614";

        String[] issuesStep = new String[] {TASK535, TASK614};

        BoardStepFragment stepToReview = mainPage.lane("Operational")
                .boardStep("To Review");
        stepToReview.assertIssueList(issuesStep);

        mainPage.issue(TASK614)
            .select();

        mainPage.assertSelectedIssues(TASK614);

        mainPage.issue(TASK614)
            .select();

        mainPage.assertSelectedIssues();

        mainPage
            .issue(TASK614)
            .dragOver(TASK535);

        stepToReview.assertIssueList(TASK614, TASK535);

        mainPage.issue(TASK535)
            .select()
            .moveToTop();

        stepToReview.assertIssueList(TASK535, TASK614);
    }

    @Test
    public void whenTrySelectedAExpediteCard_theCardShouldNotBeSelectedAndShowDetails() {
        String[] issuesStep = new String[] {"TASKB-637", "TASKB-626", "TASKB-639", "TASKB-685"};

        mainPage.lane("Deployable")
            .boardStep("To Feature Review")
            .assertIssueList(issuesStep);

        TestIssue expediteIssue = mainPage.issue("TASKB-637");

        expediteIssue.select();

        mainPage.assertSelectedIssues();

        expediteIssue.issueDetails()
            .assertCardName("TASKB-637 teste6")
            .assertClassOfService("Expedite");
    }

    @Test
    public void whenSelectTwoCardsInSequenceAndDoASingleClickOnThird_shouldOpenIssueDetailsOfThirdAndRemoveSelection() {
        String[] issuesStep = new String[]
            {
                "TASKB-601",
                "TASKB-572",
                "TASKB-342",
                "TASKB-273",
                "TASKB-646"
             };

        mainPage.lane("Operational")
            .boardStep("Doing")
            .assertIssueList(issuesStep);

        mainPage.issue("TASKB-601")
            .select();

        mainPage.assertSelectedIssues("TASKB-601");

        mainPage.issue("TASKB-572")
            .select();

        mainPage.assertSelectedIssues("TASKB-601", "TASKB-572");

        mainPage.issue("TASKB-342")
            .click()
            .issueDetails()
            .assertCardName("TASKB-342 Alfa")
            .assertClassOfService("Standard")
            .closeDialog();

        mainPage.assertSelectedIssues();
    }

    @Test
    public void whenTwoOrMoreIssuesAreSelected_allOfThemShouldBeMovedToTop() {
        String[] issuesStep = new String[] {
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
                "TASKB-686"
        };

        BoardStepFragment toDo = mainPage.lane("Operational").boardStep("To Do");

        toDo.assertIssueList(issuesStep);

        toDo.scrollTo("TASKB-627");
        mainPage
            .issue("TASKB-627")
            .select();

        toDo.scrollTo("TASKB-643");
        mainPage
            .issue("TASKB-643")
            .select();

        mainPage.assertSelectedIssues("TASKB-627", "TASKB-643");

        mainPage.issue("TASKB-627").moveToTop();

        toDo.assertIssueList(
                "TASKB-680",
                "TASKB-627",
                "TASKB-643",
                "TASKB-625",
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
    }

    @Test
    public void whenTwoOrMoreIssuesAreSelectedFromBottomToTop_OnlyFirstInBlockShouldHaveArrowButton() {
        String[] issuesStep = new String[] {
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
                "TASKB-686"
        };

        BoardStepFragment toDo = mainPage.lane("Operational").boardStep("To Do");

        toDo.assertIssueList(issuesStep);

        toDo.scrollTo("TASKB-681");
        mainPage
            .issue("TASKB-681")
            .select();

        toDo.scrollTo("TASKB-663");
        mainPage
            .issue("TASKB-663")
            .select();
        toDo.scrollTo("TASKB-661");
        mainPage
            .issue("TASKB-661")
            .select();
        toDo.scrollTo("TASKB-659");
        mainPage
            .issue("TASKB-659")
            .select();

        toDo.scrollTo("TASKB-643");
        mainPage
            .issue("TASKB-643")
            .select();
        toDo.scrollTo("TASKB-627");
        mainPage
            .issue("TASKB-627")
            .select();

        mainPage.assertSelectedIssuesWithMoveToTopButton("TASKB-627", "TASKB-659", "TASKB-681");
    }
}
