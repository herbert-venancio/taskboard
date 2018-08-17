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
            .clickHoldingCtrl()
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
            .clickHoldingCtrl();
        
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
            .clickHoldingCtrl()
            .moveToTop();
        
        stepToReview.assertIssueList("TASKB-614", "TASKB-535");
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
            .clickHoldingCtrl();
        
        mainPage.assertSelectedIssues(TASK614);
        
        mainPage.issue(TASK614)
            .clickHoldingCtrl();
        
        mainPage.assertSelectedIssues();
        
        mainPage
            .issue(TASK614)
            .dragOver(TASK535);
        
        stepToReview.assertIssueList(TASK614, TASK535);
        
        mainPage.issue(TASK535)
            .clickHoldingCtrl()
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
        
        expediteIssue.clickHoldingCtrl();
        
        mainPage.assertSelectedIssues();
        
        expediteIssue.issueDetails()
            .assertCardName("TASKB-637 teste6")
            .assertClassOfService("Expedite");
    }
    
    @Test
    public void whenSelectTwoCardsInSequenceAndDoASingleClickOnThird_shouldOpenIssueDetailsOfThirdAndRemoveSelection() {       
        String[] issuesStep = new String[]
            {
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
        
        mainPage.lane("Operational")
            .boardStep("To Do")
            .assertIssueList(issuesStep);

        mainPage.issue("TASKB-625")
            .clickHoldingCtrl();

        mainPage.assertSelectedIssues("TASKB-625");
        
        mainPage.issue("TASKB-627")
            .clickHoldingCtrl();
        
        mainPage.assertSelectedIssues("TASKB-627");
        
        mainPage.issue("TASKB-643")
            .click()
            .issueDetails()
            .assertCardName("TASKB-643 yyy")
            .assertClassOfService("Standard")
            .closeDialog();
        
        mainPage.assertSelectedIssues();
    }
}
