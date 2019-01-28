package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;

public class StatusTransitionAsserter {
    private StatusTransition status;
    private ZoneId timezone;
    private Map<String, StatusTransition> statusMap = new LinkedHashMap<>();

    public StatusTransitionAsserter(ZoneId timezone, Optional<StatusTransition> status) {
        this.timezone = timezone;
        this.status = status.orElseThrow(() -> new AssertionError("Status should have been built"));
        initializeMap();
    }

    private void initializeMap() {
        Optional<StatusTransition> statusIndex = Optional.of(this.status);
        while(statusIndex.isPresent()) {
            StatusTransition currentStatus = statusIndex.get();
            statusMap.put(currentStatus.status,currentStatus);
            statusIndex = currentStatus.next;
        }
    }

    public DateChecker dateAterLeavingLastProgressingStatus() {
        Optional<LocalDate> localDate = status.getDateAfterLeavingLastProgressingStatus();
        return new DateChecker(localDate);
    }

    public DateChecker firstDateOnProgressing() {
        Optional<LocalDate> date = status.firstDateOnProgressing(timezone);
        return new DateChecker(date);
    }

    public DatedStatusTransition atDate(String date) {
        return new DatedStatusTransition(parseDateTime(date));
    }

    public StatusTransitionNodeAsserter status(String statusName) {
        StatusTransition statusTransition = statusMap.get(statusName);
        return new StatusTransitionNodeAsserter(statusTransition);
    }

    public class DateChecker {
        private Optional<LocalDate> date;

        private DateChecker(Optional<LocalDate> date) {
            this.date = date;
        }

        public StatusTransitionAsserter is(String expectedDate) {
            String actual = this.date.map(d -> d.toString()).orElse("Not Found");
            assertThat(actual, Matchers.is(expectedDate));

            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter isNotPresent() {
            assertFalse(date.isPresent());
            return StatusTransitionAsserter.this;
        }

    }

    public class DatedStatusTransition {
        private ZonedDateTime date;

        private DatedStatusTransition(ZonedDateTime date) {
            this.date = date;
        }

        public StatusTransitionAsserter doesNotHaveStatus() {
            assertFalse(givenDate().isPresent());
            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter isStatus(String status) {
            Optional<StatusTransition> givenDate = givenDate();
            assertTrue(givenDate.isPresent());
            assertTrue(givenDate.get().isStatus(status));

            return StatusTransitionAsserter.this;
        }

        public Optional<StatusTransition> givenDate() {
            Optional<StatusTransition> givenDate = StatusTransitionAsserter.this.status.givenDate(date);
            return givenDate;
        }

    }

    public class StatusTransitionNodeAsserter {

        private StatusTransition subject;

        public StatusTransitionNodeAsserter(StatusTransition statusTransition) {
            this.subject = statusTransition;
        }

        public StatusTransitionAsserter hasTotalEffortInSeconds(long seconds) {
            assertThat(subject.getEffort()).as("Effort fom Status %s",subject.status).isEqualTo(seconds);
            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter hasEnterDate(String date) {
            Assertions.assertThat(subject.getEnterDate(timezone)).isPresent();
            Assertions.assertThat(subject.getEnterDate(timezone).get()).isEqualTo(parseZonedDateTime(date));
            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter hasNoEnterDate() {
            Assertions.assertThat(subject.getEnterDate(timezone)).isEmpty();
            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter hasExitDate(String date) {
            Assertions.assertThat(subject.getExitDate(timezone)).isPresent();
            Assertions.assertThat(subject.getExitDate(timezone).get()).isEqualTo(parseZonedDateTime(date));
            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter hasNoExitDate() {
            Assertions.assertThat(subject.getExitDate(timezone)).isEmpty();
            return StatusTransitionAsserter.this;
        }

        private ZonedDateTime parseZonedDateTime(String date) {
            return parseDateTime(date, "00:00:00", timezone);
        }
    }
}
