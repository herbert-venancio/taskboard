package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.hamcrest.Matchers;

import objective.taskboard.followup.kpi.StatusTransition;

public class StatusTransitionAsserter {
    
    private Optional<StatusTransition> opStatus;
    private StatusTransition status;
    private ZoneId timezone;

    public StatusTransitionAsserter(ZoneId timezone, Optional<StatusTransition> status) {
        this.timezone = timezone;
        this.opStatus = status;
    }

    public DateChecker dateAterLeavingLastProgressingStatus() {
        Optional<LocalDate> localDate = status.getDateAfterLeavingLastProgressingStatus();
        return new DateChecker(localDate.flatMap(d -> Optional.of(d.atStartOfDay(timezone))));
    }

    public StatusTransitionAsserter isPresent() {
        assertTrue(opStatus.isPresent());
        status = opStatus.get();
        return this;
    }

    public DateChecker firstDateOnProgressing() {
        return new DateChecker(status.firstDateOnProgressing(timezone));
    }

    public class DateChecker {
        private Optional<ZonedDateTime> date;

        private DateChecker(Optional<ZonedDateTime> date) {
            this.date = date;
        }

        public StatusTransitionAsserter is(String expectedDate) {
            String actual = this.date.map(d -> d.toLocalDate().toString()).orElse("Not Found");
            assertThat(actual, Matchers.is(expectedDate));

            return StatusTransitionAsserter.this;
        }

        public StatusTransitionAsserter isNotPresent() {
            assertFalse(date.isPresent());
            return StatusTransitionAsserter.this;
        }

    }

    public DatedStatusTransition atDate(String date) {
        return new DatedStatusTransition(parseDateTime(date));
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
}
