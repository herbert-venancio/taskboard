package objective.taskboard.timeline;

import static java.time.Month.JANUARY;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DashboardTimelineTest {

    @Test
    public void givenTimelineWithEmptyDates_whenVerifyingEmptyness_thenTrue() throws Exception {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), Optional.empty());
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithOnlyStartDateSet_whenVerifyingEmptyness_thenFalse() throws Exception {
        DashboardTimeline timeline = new DashboardTimeline(Optional.of(LocalDate.of(2019, JANUARY, 1)), Optional.empty());
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithOnlyEndDateSet_whenVerifyingEmptyness_thenFalse() throws Exception {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), Optional.of(LocalDate.of(2019, JANUARY, 1)));
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithBothDatesSet_whenVerifyingEmptyness_thenFalse() throws Exception {
        DashboardTimeline timeline = new DashboardTimeline(Optional.of(LocalDate.of(2019, JANUARY, 1)), Optional.of(LocalDate.of(2019, JANUARY, 1)));
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(true);
    }
}
