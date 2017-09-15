package objective.taskboard.followup.impl;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.SyntheticTransitionsDataRow;
import objective.taskboard.followup.SyntheticTransitionsDataSet;

public class FollowUpTransitionsDataProviderTest extends AbstractFollowUpDataProviderTest {

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
    public void issuePassedThroughAllStatus_thenAllColumnsShouldBeFilled() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueStatus(statusCancelled)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-03")
                    .transition("To Review", "2020-01-04")
                    .transition("Reviewing", "2020-01-05")
                    .transition("Done", "2020-01-06")
                    .transition("Cancelled", "2020-01-07")
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.getJiraData(defaultProjects()).analyticsTransitionsDsList;

        // then
        List<DateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDate("2020-01-02")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDate("2020-01-03")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDate("2020-01-04")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDate("2020-01-05")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDate("2020-01-06")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDate("2020-01-07")));
    }

    @Test
    public void issueStatusGoneBackAndForth_shouldReturnLastChange() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueStatus(statusDone)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Open", "2020-01-03") // last
                .transition("To Do", "2020-01-04") // last
                .transition("Doing", "2020-01-05") // last
                .transition("To Review", "2020-01-06")
                .transition("Reviewing", "2020-01-07")
                .transition("To Review", "2020-01-08") // last
                .transition("Reviewing", "2020-01-09") // last
                .transition("Done", "2020-01-10") // last
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.getJiraData(defaultProjects()).analyticsTransitionsDsList;

        // then
        List<DateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDate("2020-01-03")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDate("2020-01-04")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDate("2020-01-05")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDate("2020-01-08")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDate("2020-01-09")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDate("2020-01-10")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), nullValue());
    }

    @Test
    public void issueGoBackStatus_shouldNotConsiderNextStatus() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueStatus(statusOpen)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Doing", "2020-01-03")
                .transition("To Do", "2020-01-04")
                .transition("Open", "2020-01-05")
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.getJiraData(defaultProjects()).analyticsTransitionsDsList;

        // then
        List<DateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.size(), is(7));
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDate("2020-01-05")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(5), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(6), nullValue());
    }

    @Test
    public void issueChangeAllStatusInOneDay_shouldFillAllTransitions() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").startDate(parseDate("2020-01-01"))
                    .transition("To Do", "2020-01-01")
                    .transition("Doing", "2020-01-01")
                    .transition("To Review", "2020-01-01")
                    .transition("Reviewing", "2020-01-01")
                    .transition("Done", "2020-01-01").issueStatus(statusDone)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.getJiraData(defaultProjects()).analyticsTransitionsDsList;

        // then
        List<DateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(2), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(3), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(4), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(5), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(6), nullValue());
    }

    @Test
    public void issueWasCancelled() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").startDate(parseDate("2020-01-01"))
                    .transition("Cancelled", "2020-01-02").issueStatus(statusCancelled)
        );

        // when
        List<AnalyticsTransitionsDataSet> dataList = subject.getJiraData(defaultProjects()).analyticsTransitionsDsList;

        // then
        List<DateTime> subtaskTransitionsDatesFirstRow = dataList.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows.get(0).transitionsDates;
        assertThat(subtaskTransitionsDatesFirstRow.get(0), is(parseDate("2020-01-01")));
        assertThat(subtaskTransitionsDatesFirstRow.get(1), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(2), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(3), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(4), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(5), nullValue());
        assertThat(subtaskTransitionsDatesFirstRow.get(6), is(parseDate("2020-01-02")));
    }

    @Test
    public void issueCreatedAndNoStatusChange_shouldHaveOneRowWithOneOpenIssue() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueStatus(statusOpen).startDate(parseDate("2017-01-01"))
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.getJiraData(defaultProjects()).cfdTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(1));
        SyntheticTransitionsDataRow firstRow = rows.get(0);
        assertThat(firstRow.amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void twoIssuesCreatedOnDifferentDaysAndNoStatusChange_shouldHaveTwoRows() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").issueStatus(statusOpen).startDate(parseDate("2017-01-01"))
                , subtask().id(100).key("PROJ-101").issueStatus(statusOpen).startDate(parseDate("2017-01-02"))
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.getJiraData(defaultProjects()).cfdTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(2));
        SyntheticTransitionsDataRow firstRow = rows.get(0);
        assertThat(firstRow.amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        SyntheticTransitionsDataRow secondRow = rows.get(1);
        assertThat(secondRow.amountOfIssueInStatus, equalTo(asList(2, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void someIssuesWithTransitions() {
        // given
        issues(
                subtask().id(100).key("PROJ-100").startDate(parseDate("2017-01-01"))
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-03")
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("To Review", "2017-01-08")
                    .transition("Reviewing", "2017-01-09")
                    .transition("Done", "2017-01-10").issueStatus(statusDone)
                , subtask().id(101).key("PROJ-101").startDate(parseDate("2017-01-02"))
                    .transition("To Do", "2017-01-02")
                    .transition("Doing", "2017-01-02")
                    .transition("To Review", "2017-01-02")
                    .transition("Reviewing", "2017-01-02")
                    .transition("Done", "2017-01-02").issueStatus(statusDone)
                , subtask().id(102).key("PROJ-102").startDate(parseDate("2017-01-03"))
                    .transition("To Do", "2017-01-04")
                    .transition("Doing", "2017-01-05")
                    .transition("To Review", "2017-01-06")
                    .transition("Reviewing", "2017-01-07")
                    .transition("Done", "2017-01-08").issueStatus(statusDone)
                , subtask().id(103).key("PROJ-103").startDate(parseDate("2017-01-04"))
                    .transition("Cancelled", "2017-01-05").issueStatus(statusCancelled)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.getJiraData(defaultProjects()).cfdTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(10));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 1, 0)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(2, 0, 0, 0, 0, 1, 0)));
        assertThat(rows.get(3).amountOfIssueInStatus, equalTo(asList(1, 2, 0, 0, 0, 1, 0)));
        assertThat(rows.get(4).amountOfIssueInStatus, equalTo(asList(0, 0, 2, 0, 0, 1, 1)));
        assertThat(rows.get(5).amountOfIssueInStatus, equalTo(asList(0, 0, 1, 1, 0, 1, 1)));
        assertThat(rows.get(6).amountOfIssueInStatus, equalTo(asList(0, 0, 1, 0, 1, 1, 1)));
        assertThat(rows.get(7).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 1, 0, 2, 1)));
        assertThat(rows.get(8).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 1, 2, 1)));
        assertThat(rows.get(9).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 3, 1)));
    }

    @Test
    public void issueWasCancelled_shouldCalculateDateRangeCorrectly() {
        // given
        issues(
                subtask().id(100).key("PROJ-101").startDate(parseDate("2020-01-01"))
                    .transition("Cancelled", "2020-01-07").issueStatus(statusCancelled)
        );

        // when
        List<SyntheticTransitionsDataSet> sets = subject.getJiraData(defaultProjects()).cfdTransitionsDsList;

        // then
        List<SyntheticTransitionsDataRow> rows = sets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(rows.size(), is(7));
        assertThat(rows.get(0).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(1).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(2).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(3).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(4).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(5).amountOfIssueInStatus, equalTo(asList(1, 0, 0, 0, 0, 0, 0)));
        assertThat(rows.get(6).amountOfIssueInStatus, equalTo(asList(0, 0, 0, 0, 0, 0, 1)));
    }
}