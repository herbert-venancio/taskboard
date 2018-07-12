package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.jira.data.Status;

public class FollowUpDataGeneratorTransitionsTest extends FollowUpDataGeneratorTestBase {

    private static final int SUBTASK_TRANSITIONS_DATASET_INDEX = 2;

    @Before
    public void beforeTransitionsDataTests() throws InterruptedException, ExecutionException {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" +
                "    tshirtCustomFieldId: Dev_Tshirt\n" +
                "    jiraIssueTypes:\n" +
                "      - "+ devIssueType + "\n");
    }

    @Test
    public void checkHeaders() {
        // given
        FollowUpData jiraData = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT);
        List<AnalyticsTransitionsDataSet> analytics = jiraData.analyticsTransitionsDsList;
        List<SyntheticTransitionsDataSet> synthetic = jiraData.syntheticsTransitionsDsList;

        // then
        assertThat(analytics.get(0).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "UATing", "To UAT", "Doing", "To Do", "Open")));
        assertThat(analytics.get(1).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "QAing", "To QA", "Feature Reviewing", "To Feature Review", "Alpha Testing", "To Alpha Test", "Doing", "To Do", "Open")));
        assertThat(analytics.get(2).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")));

        assertThat(synthetic.get(0).headers, equalTo(asList("Date", "Type", "Cancelled", "Done", "UATing", "To UAT", "Doing", "To Do", "Open")));
        assertThat(synthetic.get(1).headers, equalTo(asList("Date", "Type", "Cancelled", "Done", "QAing", "To QA", "Feature Reviewing", "To Feature Review", "Alpha Testing", "To Alpha Test", "Doing", "To Do", "Open")));
        assertThat(synthetic.get(2).headers, equalTo(asList("Date", "Type", "Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")));
    }

    @Test
    public void issuePassedThroughAllStatus_thenAllColumnsShouldBeFilled() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        assertThat(dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.size(), is(1));
        
        List<ZonedDateTime> transitionsDate = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(transitionsDate.size(), is(7));
        assertThat(transitionsDate.get(0), is(parseDateTime("2020-01-07")));
        assertThat(transitionsDate.get(1), is(parseDateTime("2020-01-06")));
        assertThat(transitionsDate.get(2), is(parseDateTime("2020-01-05")));
        assertThat(transitionsDate.get(3), is(parseDateTime("2020-01-04")));
        assertThat(transitionsDate.get(4), is(parseDateTime("2020-01-03")));
        assertThat(transitionsDate.get(5), is(parseDateTime("2020-01-02")));
        assertThat(transitionsDate.get(6), is(parseDateTime("2020-01-01")));
    }
    
    @Test
    public void issuesNotConfiguredAsSubtasks_thenShouldNotBeFilled() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled),
                subtask().id(100).key("PROJ-100").issueType(alphaIssueType)
                    .transition("Open", "2020-01-08")
                    .transition("To Do", "2020-01-09")
                    .transition("Doing", "2020-01-10")
                    .transition("To Review", "2020-01-11")
                    .transition("Reviewing", "2020-01-12")
                    .transition("Done", "2020-01-13")
                    .transition("Cancelled", "2020-01-14").issueStatus(statusCancelled)
        );
        
        jiraProperties.getIssuetype().setSubtasks(getSubtasksIssueTypeDetails(devIssueType));

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        assertThat(dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.size(), is(1));
        
        AnalyticsTransitionsDataRow devTransitions = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0);
        assertThat(devTransitions.issueKey, is("PROJ-100"));
        List<ZonedDateTime> devTransitionsDates = devTransitions.transitionsDates;
        assertThat(devTransitionsDates.size(), is(7));
        assertThat(devTransitionsDates.get(0), is(parseDateTime("2020-01-07")));
        assertThat(devTransitionsDates.get(1), is(parseDateTime("2020-01-06")));
        assertThat(devTransitionsDates.get(2), is(parseDateTime("2020-01-05")));
        assertThat(devTransitionsDates.get(3), is(parseDateTime("2020-01-04")));
        assertThat(devTransitionsDates.get(4), is(parseDateTime("2020-01-03")));
        assertThat(devTransitionsDates.get(5), is(parseDateTime("2020-01-02")));
        assertThat(devTransitionsDates.get(6), is(parseDateTime("2020-01-01")));
    }
    
    @Test
    public void issuesWithDifferentType_thenShouldFillSeveralRows() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled),
                subtask().id(100).key("PROJ-100").issueType(alphaIssueType)
                    .transition("Open", "2020-01-08")
                    .transition("To Do", "2020-01-09")
                    .transition("Doing", "2020-01-10")
                    .transition("To Review", "2020-01-11")
                    .transition("Reviewing", "2020-01-12")
                    .transition("Done", "2020-01-13")
                    .transition("Cancelled", "2020-01-14").issueStatus(statusCancelled)
        );
        
        jiraProperties.getIssuetype().setSubtasks(getSubtasksIssueTypeDetails(devIssueType,alphaIssueType));

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        assertThat(dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.size(), is(2));
        
        AnalyticsTransitionsDataRow devTransitions = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0);
        assertThat(devTransitions.issueKey, is("PROJ-100"));
        List<ZonedDateTime> devTransitionsDates = devTransitions.transitionsDates;
        assertThat(devTransitionsDates.size(), is(7));
        assertThat(devTransitionsDates.get(0), is(parseDateTime("2020-01-07")));
        assertThat(devTransitionsDates.get(1), is(parseDateTime("2020-01-06")));
        assertThat(devTransitionsDates.get(2), is(parseDateTime("2020-01-05")));
        assertThat(devTransitionsDates.get(3), is(parseDateTime("2020-01-04")));
        assertThat(devTransitionsDates.get(4), is(parseDateTime("2020-01-03")));
        assertThat(devTransitionsDates.get(5), is(parseDateTime("2020-01-02")));
        assertThat(devTransitionsDates.get(6), is(parseDateTime("2020-01-01")));
        
        AnalyticsTransitionsDataRow alphaTransitions = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(1);
        assertThat(alphaTransitions.issueKey, is("PROJ-100"));
        List<ZonedDateTime> alphaTransitionsDates = alphaTransitions.transitionsDates;
        assertThat(alphaTransitionsDates.size(), is(7));
        assertThat(alphaTransitionsDates.get(0), is(parseDateTime("2020-01-14")));
        assertThat(alphaTransitionsDates.get(1), is(parseDateTime("2020-01-13")));
        assertThat(alphaTransitionsDates.get(2), is(parseDateTime("2020-01-12")));
        assertThat(alphaTransitionsDates.get(3), is(parseDateTime("2020-01-11")));
        assertThat(alphaTransitionsDates.get(4), is(parseDateTime("2020-01-10")));
        assertThat(alphaTransitionsDates.get(5), is(parseDateTime("2020-01-09")));
        assertThat(alphaTransitionsDates.get(6), is(parseDateTime("2020-01-08")));
    }
    
    @Test
    public void noSubtasksPropertyConfigured_thenNothingNotBeFilled() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled),
                subtask().id(100).key("PROJ-100").issueType(alphaIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled)
        );
        
        jiraProperties.getIssuetype().setSubtasks(getSubtasksIssueTypeDetails());

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then      
        assertThat(dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.size(), is(0));
    }
    
    @Test
    public void issueStatusGoneBackAndForth_shouldReturnLastChange() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Open", "2020-01-03") // last
                .transition("To Do", "2020-01-04") // last
                .transition("Doing", "2020-01-05") // last
                .transition("To Review", "2020-01-06")
                .transition("Reviewing", "2020-01-07")
                .transition("To Review", "2020-01-08") // last
                .transition("Reviewing", "2020-01-09") // last
                .transition("Done", "2020-01-10").issueStatus(statusDone) // last
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDateTime("2020-01-10")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDateTime("2020-01-09")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDateTime("2020-01-08")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDateTime("2020-01-05")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDateTime("2020-01-04")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-03")));
    }
    
    @Test
    public void issueStatusGoneForthAndBack_shouldNotReturnTransitionsAfterBackTransition() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Open", "2020-01-03") // last
                .transition("To Do", "2020-01-04") 
                .transition("Doing", "2020-01-05") 
                .transition("To Review", "2020-01-06")
                .transition("Doing", "2020-01-07")
                .transition("To Do", "2020-01-08").issueStatus(statusToDo) // last
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDateTime("2020-01-08")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-03")));
    }

    @Test
    public void issueGoBackStatus_shouldNotConsiderNextStatus() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Doing", "2020-01-03")
                .transition("To Do", "2020-01-04")
                .transition("Open", "2020-01-05").issueStatus(statusOpen)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(5), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-05")));
    }

    @Test
    public void issueChangeAllStatusInOneDay_shouldFillAllTransitions() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").issueType(devIssueType)
                    .created("2020-01-01")
                    .transition("To Do", "2020-01-01")
                    .transition("Doing", "2020-01-01")
                    .transition("To Review", "2020-01-01")
                    .transition("Reviewing", "2020-01-01")
                    .transition("Done", "2020-01-01").issueStatus(statusDone)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.get(0), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDateTime("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDateTime("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDateTime("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDateTime("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDateTime("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-01")));
    }

    @Test
    public void issueWasCancelled() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").issueType(devIssueType)
                    .created("2020-01-01")
                    .transition("Cancelled", "2020-01-02").issueStatus(statusCancelled)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).analyticsTransitionsDsList;

        // then
        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(5), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-01")));
    }

    @Test
    public void issueCreatedAndNoStatusChange_shouldHaveOneRowWithOneOpenIssue() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                        .created("2017-01-01").issueStatus(statusOpen)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(1));
        SyntheticTransitionsDataRow firstRow = rows.get(0);
        assertThat(firstRow.date, is(parseDateTime("2017-01-01")));
        assertThat(firstRow.amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
    }

    @Test
    public void twoIssuesCreatedOnDifferentDaysAndNoStatusChange_shouldHaveTwoRows() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                        .created("2017-01-01").issueStatus(statusOpen)
                , subtask().id(100).key("PROJ-101").issueType(devIssueType)
                        .created("2017-01-02").issueStatus(statusOpen)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(2));
        SyntheticTransitionsDataRow firstRow = rows.get(0);
        assertThat(firstRow.amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        SyntheticTransitionsDataRow secondRow = rows.get(1);
        assertThat(secondRow.amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 2)));
    }

    @Test
    public void someIssuesWithTransitions() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .created("2017-01-01")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("To Review", "2017-01-08")
                    .transition("Reviewing", "2017-01-09")
                    .transition("Done", "2017-01-10").issueStatus(statusDone)
                , subtask().id(101).key("PROJ-101").issueType(devIssueType)
                    .created("2017-01-02")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-02")
                    .transition("To Review", "2017-01-02")
                    .transition("Reviewing", "2017-01-02")
                    .transition("Done", "2017-01-02").issueStatus(statusDone)
                , subtask().id(102).key("PROJ-102").issueType(devIssueType)
                    .created("2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("Done", "2017-01-08").issueStatus(statusDone)
                , subtask().id(103).key("PROJ-103").issueType(devIssueType)
                    .created("2017-01-04")
                    .transition("Cancelled", "2017-01-05").issueStatus(statusCancelled)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(10));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(0, 1, 0, 0, 0, 0, 1)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(0, 1, 0, 0, 0, 0, 2)));
        assertThat(rows.get(3).amountOfIssueInStatus, equalTo(asList(0, 1, 0, 0, 0, 2, 1)));
        assertThat(rows.get(4).amountOfIssueInStatus, equalTo(asList(1, 1, 0, 0, 2, 0, 0)));
        assertThat(rows.get(5).amountOfIssueInStatus, equalTo(asList(1, 1, 0, 1, 1, 0, 0)));
        assertThat(rows.get(6).amountOfIssueInStatus, equalTo(asList(1, 1, 1, 0, 1, 0, 0)));
        assertThat(rows.get(7).amountOfIssueInStatus, equalTo(asList(1, 2, 0, 1, 0, 0, 0)));
        assertThat(rows.get(8).amountOfIssueInStatus, equalTo(asList(1, 2, 1, 0, 0, 0, 0)));
        assertThat(rows.get(9).amountOfIssueInStatus, equalTo(asList(1, 3, 0, 0, 0, 0, 0)));
    }
    
    @Test
    public void someIssuesWithTransitions_withSeveraltypes_shouldAllAppear() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .created("2017-01-01")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("To Review", "2017-01-08")
                    .transition("Reviewing", "2017-01-09")
                    .transition("Done", "2017-01-10").issueStatus(statusDone)
                , subtask().id(101).key("PROJ-101").issueType(devIssueType)
                    .created("2017-01-02")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-02")
                    .transition("To Review", "2017-01-02")
                    .transition("Reviewing", "2017-01-02")
                    .transition("Done", "2017-01-02").issueStatus(statusDone)
                , subtask().id(102).key("PROJ-102").issueType(alphaIssueType)
                    .created("2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("Done", "2017-01-08").issueStatus(statusDone)
                , subtask().id(103).key("PROJ-103").issueType(alphaIssueType)
                    .created("2017-01-04")
                    .transition("Cancelled", "2017-01-05").issueStatus(statusCancelled)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(20));
        assertSyntheticData(rows.get(0),"Dev","2017-01-01",asList(0,0,0,0,0,0,1));
        assertSyntheticData(rows.get(1),"Alpha","2017-01-01",asList(0, 0, 0, 0, 0, 0, 0));
        
        assertSyntheticData(rows.get(2),"Dev","2017-01-02",asList(0, 1, 0, 0, 0, 0, 1));
        assertSyntheticData(rows.get(3),"Alpha","2017-01-02",asList(0, 0, 0, 0, 0, 0, 0));
        
        assertSyntheticData(rows.get(4),"Dev","2017-01-03",asList(0, 1, 0, 0, 0, 0, 1));
        assertSyntheticData(rows.get(5),"Alpha","2017-01-03",asList(0, 0, 0, 0, 0, 0, 1));
        
        assertSyntheticData(rows.get(6),"Dev","2017-01-04",asList(0, 1, 0, 0, 0, 1, 0));
        assertSyntheticData(rows.get(7),"Alpha","2017-01-04",asList(0, 0, 0, 0, 0, 1, 1));
        
        assertSyntheticData(rows.get(8),"Dev","2017-01-05",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(9),"Alpha","2017-01-05",asList(1, 0, 0, 0, 1, 0, 0));
        
        assertSyntheticData(rows.get(10),"Dev","2017-01-06",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(11),"Alpha","2017-01-06",asList(1, 0, 0, 1, 0, 0, 0));
        
        assertSyntheticData(rows.get(12),"Dev","2017-01-07",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(13),"Alpha","2017-01-07",asList(1, 0, 1, 0, 0, 0, 0));
        
        assertSyntheticData(rows.get(14),"Dev","2017-01-08",asList(0, 1, 0, 1, 0, 0, 0));
        assertSyntheticData(rows.get(15),"Alpha","2017-01-08",asList(1, 1, 0, 0, 0, 0, 0));
        
        assertSyntheticData(rows.get(16),"Dev","2017-01-09",asList(0, 1, 1, 0, 0, 0, 0));
        assertSyntheticData(rows.get(17),"Alpha","2017-01-09",asList(1, 1, 0, 0, 0, 0, 0));
        
        assertSyntheticData(rows.get(18),"Dev","2017-01-10",asList(0, 2, 0, 0, 0, 0, 0));
        assertSyntheticData(rows.get(19),"Alpha","2017-01-10",asList(1, 1, 0, 0, 0, 0, 0));
    }
    
    @Test
    public void someIssuesWithTransitions_withSeveraltypes_notConfiguredAsSubtask_shouldNotAppear() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                    .created("2017-01-01")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("To Review", "2017-01-08")
                    .transition("Reviewing", "2017-01-09")
                    .transition("Done", "2017-01-10").issueStatus(statusDone)
                , subtask().id(101).key("PROJ-101").issueType(devIssueType)
                    .created("2017-01-02")
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-02")
                    .transition("To Review", "2017-01-02")
                    .transition("Reviewing", "2017-01-02")
                    .transition("Done", "2017-01-02").issueStatus(statusDone)
                , subtask().id(102).key("PROJ-102").issueType(alphaIssueType)
                    .created("2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("Done", "2017-01-08").issueStatus(statusDone)
                , subtask().id(103).key("PROJ-103").issueType(alphaIssueType)
                    .created("2017-01-04")
                    .transition("Cancelled", "2017-01-05").issueStatus(statusCancelled)
        );

        
        jiraProperties.getIssuetype().setSubtasks(getSubtasksIssueTypeDetails(devIssueType));
        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(10));
        assertSyntheticData(rows.get(0),"Dev","2017-01-01",asList(0,0,0,0,0,0,1));
        assertSyntheticData(rows.get(1),"Dev","2017-01-02",asList(0, 1, 0, 0, 0, 0, 1));
        assertSyntheticData(rows.get(2),"Dev","2017-01-03",asList(0, 1, 0, 0, 0, 0, 1));
        assertSyntheticData(rows.get(3),"Dev","2017-01-04",asList(0, 1, 0, 0, 0, 1, 0));
        assertSyntheticData(rows.get(4),"Dev","2017-01-05",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(5),"Dev","2017-01-06",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(6),"Dev","2017-01-07",asList(0, 1, 0, 0, 1, 0, 0));
        assertSyntheticData(rows.get(7),"Dev","2017-01-08",asList(0, 1, 0, 1, 0, 0, 0));
        assertSyntheticData(rows.get(8),"Dev","2017-01-09",asList(0, 1, 1, 0, 0, 0, 0));
        assertSyntheticData(rows.get(9),"Dev","2017-01-10",asList(0, 2, 0, 0, 0, 0, 0));
    }
    
    private void assertSyntheticData(SyntheticTransitionsDataRow row, String type, String date , List<Integer> amountOfIssues) {
        assertThat(row.issueType, equalTo(type));
        assertThat(row.date, is(parseDateTime(date)));
        assertThat(row.amountOfIssueInStatus, equalTo(amountOfIssues));
    }

    @Test
    public void issueWasCancelled_shouldCalculateDateRangeCorrectly() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").issueType(devIssueType)
                    .created("2020-01-01")
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(7));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(3).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(4).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(5).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(6).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void issueTransitionsWithRandomHours_shouldCalculateSyntheticAtTheEndOfTheDay() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                        .created("2020-01-01", "00:00:00")
                        .transition("To Do", "2020-01-02", "01:00:00")
                        .transition("Doing", "2020-01-03", "04:00:00")
                        .transition("To Review", "2020-01-04", "12:00:00")
                        .transition("Reviewing", "2020-01-05", "15:00:00")
                        .transition("Done", "2020-01-06", "23:59:59").issueStatus(statusDone)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT).syntheticsTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(6));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 1, 0)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 1, 0, 0)));
        assertThat(rows.get(3).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 1, 0, 0, 0)));
        assertThat(rows.get(4).amountOfIssueInStatus, equalTo(asList(0, 0, 1, 0, 0, 0, 0)));
        assertThat(rows.get(5).amountOfIssueInStatus, equalTo(asList(0, 1, 0, 0, 0, 0, 0)));
    }

    @Test
    public void mergeFinalizationStatusOnSyntheticDataSet() {
        // given
        Map<Long, Status> statusMap = new LinkedHashMap<>();
        statusMap.put(statusOpen,       new Status(statusOpen,       "Open",  CATEGORY_NEW));
        statusMap.put(statusToDo,       new Status(statusToDo,       "To Do", CATEGORY_IN_PROGRESS));
        statusMap.put(statusDoing,      new Status(statusDoing,      "Doing", CATEGORY_IN_PROGRESS));
        statusMap.put(statusCancelled,  new Status(statusCancelled,  "Cancelled", CATEGORY_DONE));
        statusMap.put(statusDone,       new Status(statusDone,       "Done", CATEGORY_DONE));
        doReturn(statusMap).when(metadataService).getStatusesMetadata();

        issues(
                subtask().id(100).key("PROJ-100").issueType(devIssueType)
                        .created("2020-01-01")
                        .transition("To Do", "2020-01-02")
                        .transition("Doing", "2020-01-02")
                        .transition("To Review", "2020-01-02")
                        .transition("Reviewing", "2020-01-02")
                        .transition("Done", "2020-01-02").issueStatus(statusDone)
                , subtask().id(101).key("PROJ-101").issueType(devIssueType)
                        .created("2020-01-02")
                        .transition("Cancelled", "2020-01-03").issueStatus(statusCancelled)
        );

        // when
        FollowUpData jiraData = subject.generate(ZoneId.systemDefault(), DEFAULT_PROJECT);
        List<AnalyticsTransitionsDataSet> analytics = jiraData.analyticsTransitionsDsList;
        List<SyntheticTransitionsDataSet> synthetic = jiraData.syntheticsTransitionsDsList;

        // then
        assertThat(analytics.get(0).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "UATing", "To UAT", "Doing", "To Do", "Open")));
        assertThat(analytics.get(1).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "QAing", "To QA", "Feature Reviewing", "To Feature Review", "Alpha Testing", "To Alpha Test", "Doing", "To Do", "Open")));
        assertThat(analytics.get(2).headers, equalTo(asList("PKEY", "Type", "Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")));

        assertThat(synthetic.get(0).headers, equalTo(asList("Date", "Type", "Cancelled/Done", "UATing", "To UAT", "Doing", "To Do", "Open")));
        assertThat(synthetic.get(1).headers, equalTo(asList("Date", "Type", "Cancelled/Done", "QAing", "To QA", "Feature Reviewing", "To Feature Review", "Alpha Testing", "To Alpha Test", "Doing", "To Do", "Open")));
        assertThat(synthetic.get(2).headers, equalTo(asList("Date", "Type", "Cancelled/Done", "Reviewing", "To Review", "Doing", "To Do", "Open")));

        List<ZonedDateTime> subtaskTransitionsDatesFirstRow = analytics.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDateTime("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDateTime("2020-01-01")));
        List<ZonedDateTime> subtaskTransitionsDatesSecondRow = analytics.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(1).transitionsDates;
        assertThat(subtaskTransitionsDatesSecondRow.size(), is(7));
        assertThat(subtaskTransitionsDatesSecondRow.get(0), is(parseDateTime("2020-01-03")));
        assertThat(subtaskTransitionsDatesSecondRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesSecondRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesSecondRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesSecondRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesSecondRow.get(5), nullValue());
        assertThat(subtaskTransitionsDatesSecondRow.get(6), is(parseDateTime("2020-01-02")));

        List<SyntheticTransitionsDataRow> rows = synthetic.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(3));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 1)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 1)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(2, 0, 0, 0, 0, 0)));
    }
}