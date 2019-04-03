package objective.taskboard.followup.kpi;

import static java.util.Arrays.asList;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiAsserter<T> {

    protected IssueKpi subject;
    private Map<String, StatusAsserter> statusesAsserter = new LinkedHashMap<>();
    private KpiEnvironment environment;
    private T parentContext;
    private static final long NON_EXISTENT_TYPE_ID = -1L;

    public IssueKpiAsserter(IssueKpi subject, KpiEnvironment environment, T parentContext) {
        this.subject = subject;
        this.environment = environment;
        this.parentContext = parentContext;
    }

    public IssueKpiAsserter<IssueKpiAsserter<T>> withChild(String childKey) {
        Optional<IssueKpi> child = findChildByKey(childKey);
        if(!child.isPresent())
            throw new AssertionError(String.format("Child %s not found", childKey));

        return new IssueKpiAsserter<>(child.get(),this.environment,this);
    }

    public IssueKpiAsserter<T> hasChild(String childKey) {
        Optional<IssueKpi> child = findChildByKey(childKey);
        assertThat(child).as("Child %s not found", childKey).isPresent();
        return this;
    }

    public T eoIA() {
        return parentContext;
    }

    public IssueKpiAsserter<T> hasCompletedCycle(String...statuses) {
        assertThat(subject.hasCompletedCycle(new HashSet<>(asList(statuses)))).isTrue();
        return this;
    }

    public IssueKpiAsserter<T> hasNotCompletedCycle(String...statuses) {
        assertThat(subject.hasCompletedCycle(new HashSet<>(asList(statuses)))).isFalse();
        return this;
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

    public void hasEffortSumFromChildrenWithSubtaskTypeName(double effortInHours, String typeName) {
        Optional<IssueTypeDTO> type = environment.types().getOptional(typeName);
        long expectedEffortInSeconds = DateTimeUtils.hoursToSeconds(effortInHours);
        Long typeId = type.map(IssueTypeDTO::id).orElse(NON_EXISTENT_TYPE_ID);
        Assertions.assertThat(subject.getEffortSumFromChildrenWithSubtaskTypeId(typeId)).isEqualTo(expectedEffortInSeconds);
    }

    private Optional<IssueKpi> findChildByKey(String childKey) {
        return subject.getChildren().stream().filter(c -> childKey.equals(c.getIssueKey())).findFirst();
    }

    public IssueKpiAsserter<T> hasType(String type) {
        Assertions.assertThat(subject.getIssueTypeName()).isEqualTo(type);
        return this;
    }

    public class SubtaskChecker {
        private List<ZonedWorklog> childrenWorklogs;

        public SubtaskChecker(IssueTypeDTO type) {
            this.childrenWorklogs = subject.getWorklogFromChildrenTypeId(type.id());
        }

        public SubtaskChecker(String status) {
            this.childrenWorklogs = subject.getWorklogFromChildrenStatus(status);
        }

        public SubtaskChecker hasTotalWorklogs(int quantity){
            assertThat(childrenWorklogs.size(), is(quantity));
            return this;
        }

        public SubtaskChecker withTotalValue(int totalExpected) {
            Integer totalEffort = childrenWorklogs.stream()
                    .map(ZonedWorklog::getTimeSpentSeconds).reduce(Integer::sum).orElse(0);
            assertThat(totalEffort,is(totalExpected));
            return this;
        }

        public IssueKpiAsserter<T> eoSc() {
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

        public IssueKpiAsserter<T> endsOn(String end) {
            Optional<Range<LocalDate>> opRange = getRange();
            if(!opRange.isPresent())
                throw new AssertionError("opRange is not present");
            Range<LocalDate> range = opRange.get();

            LocalDate endDate = LocalDate.parse(end);
            assertThat(range.getMinimum(), is(startDate));
            assertThat(range.getMaximum(), is(endDate));

            return IssueKpiAsserter.this;
        }

        private Optional<Range<LocalDate>> getRange() {
            return subject.getDateRangeBasedOnProgressingStatuses(environment.getTimezone());
        }

        public IssueKpiAsserter<T> isNotPresent() {
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
            assertThat(subject.getEffortUntilDate(date), is(effort));
            return this;
        }

        public IssueKpiAsserter<T> eoDc() {
            return IssueKpiAsserter.this;
        }

        public MultipleStatusesAsserter forStatuses(String ...statuses) {
            return new MultipleStatusesAsserter(Arrays.asList(statuses));
        }

        public class MultipleStatusesAsserter {
            private List<String> statuses;

            private MultipleStatusesAsserter(List<String> statuses) {
                this.statuses = statuses;
            }

            public MultipleStatusesAsserter hasEffortSumInSeconds(long effortSumInSeconds) {
                assertThat(subject.getEffortSumInSecondsFromStatusesUntilDate(statuses, DateChecker.this.date), is(effortSumInSeconds));
                return this;
            }
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

        public IssueKpiAsserter<T> doesNotHaveEffort() {
            assertThat(IssueKpiAsserter.this.subject.getEffort(this.status), is(0l));

            return IssueKpiAsserter.this;
        }

        public WithDate untilDate(String date) {
            return new WithDate(DateTimeUtils.parseDateTime(date));
        }

        public IssueKpiAsserter<T> eoSa() {
            return IssueKpiAsserter.this;
        }

        public class WithDate {
            private ZonedDateTime date;

            public WithDate(ZonedDateTime date) {
                this.date = date;
            }

            public StatusAsserter hasEffort(long effort) {
                assertThat(subject.getEffortFromStatusUntilDate(status, date), is(effort));
                return StatusAsserter.this;
            }
        }

        public StatusAsserter hasNoEffort() {
            return hasTotalEffortInHours(0);
        }
    }
}
