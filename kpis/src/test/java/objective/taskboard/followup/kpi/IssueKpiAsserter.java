package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.junit.Assert;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiAsserter {

    private IssueKpi subject;
    private Map<String, StatusAsserter> statusesAsserter = new LinkedHashMap<>();
    private KpiEnvironment environment;

    public IssueKpiAsserter(IssueKpi subject, KpiEnvironment environment) {
        this.subject = subject;
        this.environment = environment;
    }

    public IssueKpiAsserter withChild(String childKey) {
        Optional<IssueKpi> child = subject.getChildren().stream().filter(c -> childKey.equals(c.getIssueKey())).findFirst();
        if(!child.isPresent())
            Assert.fail(String.format("Child %s not found", childKey));

        return new IssueKpiAsserter(child.get(),this.environment);
    }

    public StatusAsserter atStatus(String status) {
        statusesAsserter.putIfAbsent(status, new StatusAsserter(status));
        return statusesAsserter.get(status);
    }

    public DateChecker atDate(String date) {
        return new DateChecker(date);
    }

    public SubtaskChecker givenSubtaskType(String type) {
        return new SubtaskChecker(environment.getType(type));
    }

    public SubtaskChecker givenSubtaskStatus(String status) {
        return new SubtaskChecker(status);
    }

    public RangeAsserter rangeBasedOnProgressingStatuses() {
        return new RangeAsserter();
    }

    public class SubtaskChecker {
        private List<Worklog> childrenWorklogs;

        public SubtaskChecker(IssueTypeDTO type) {
            this.childrenWorklogs = subject.getWorklogFromChildrenTypeId(type.id());
        }

        public SubtaskChecker(String status) {
            this.childrenWorklogs = subject.getWorklogFromChildrenStatus(status);
        }

        public SubtaskChecker hasTotalWorklogs(int quantity){
            assertThat(childrenWorklogs.size(),is(quantity));
            return this;
        }

        public SubtaskChecker withTotalValue(int totalExpected) {
            Integer totalEffort = childrenWorklogs.stream().map(w -> w.timeSpentSeconds).reduce(Integer::sum).orElse(0);
            assertThat(totalEffort,is(totalExpected));
            return this;
        }

        public IssueKpiAsserter eoSc() {
            return IssueKpiAsserter.this;
        }


        public SubtaskChecker doesNotHaveWorklogs() {
            return hasTotalWorklogs(0);
        }

    }

    public class RangeAsserter {

        private LocalDate startDate;

        public RangeAsserter() {}

        public RangeAsserter startsOn(String date) {
            this.startDate = LocalDate.parse(date);
            return this;
        }

        public IssueKpiAsserter endsOn(String end) {
            Optional<Range<LocalDate>> opRange = getRange();
            assertTrue(opRange.isPresent());
            Range<LocalDate> range = opRange.get();

            LocalDate endDate = LocalDate.parse(end);
            assertThat(range.getMinimum(),is(startDate));
            assertThat(range.getMaximum(),is(endDate));

            return IssueKpiAsserter.this;


        }
        private Optional<Range<LocalDate>> getRange() {
            return subject.getDateRangeBasedOnProgressingStatuses(environment.getTimezone());
        }

        public IssueKpiAsserter isNotPresent() {
            Optional<Range<LocalDate>> opRange = getRange();
            assertFalse(opRange.isPresent());
            return IssueKpiAsserter.this;
        }


    }

    public class DateChecker {
        private ZonedDateTime date;

        private DateChecker(String date) {
            this.date = parseDateTime(date);
        }

        public DateChecker isOnStatus(String status) {
            assertTrue(subject.isOnStatusOnDay(status, date));
            return this;
        }

        public DateChecker isNotOnStatus(String status) {
            assertFalse(subject.isOnStatusOnDay(status, date));
            return this;
        }

        public DateChecker hasNotTransitedToAnyStatus(String... statuses) {
            assertFalse(subject.hasTransitedToAnyStatusOnDay(date, statuses));
            return this;
        }

        public DateChecker hasTransitedToAnyStatus(String... statuses) {
            assertTrue(subject.hasTransitedToAnyStatusOnDay(date, statuses));
            return this;
        }

        public DateChecker hasEffort(long effort) {
            assertThat(subject.getEffortUntilDate(date),is(effort));
            return this;
        }

        public IssueKpiAsserter eoDc() {
            return IssueKpiAsserter.this;

        }

    }

    public class StatusAsserter {
        private String status;

        private StatusAsserter(String status) {
            this.status = status;
        }

        public StatusAsserter hasTotalEffort(long effort) {
            assertThat(IssueKpiAsserter.this.subject.getEffort(this.status), is(effort));
            return this;
        }

        public StatusAsserter hasTotalEffortInHours(double effort) {
            long effortInseconds = DateTimeUtils.hoursToSeconds(effort);
            return hasTotalEffort(effortInseconds);
        }

        public IssueKpiAsserter doesNotHaveEffort() {
            assertThat(IssueKpiAsserter.this.subject.getEffort(this.status), is(0l));

            return IssueKpiAsserter.this;
        }

        public WithDate untilDate(String date) {
            return new WithDate(DateTimeUtils.parseDateTime(date));
        }

        public IssueKpiAsserter eoSa() {
            return IssueKpiAsserter.this;
        }

        public class WithDate {
            private ZonedDateTime date;

            public WithDate(ZonedDateTime date) {
                this.date = date;
            }

            public StatusAsserter hasEffort(long effort) {
                assertThat(subject.getEffortFromStatusUntilDate(status, date),is(effort));
                return StatusAsserter.this;
            }
        }
    }
}
