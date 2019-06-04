package objective.taskboard.timeline;

import static java.time.Month.JANUARY;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DashboardTimelineTest {

    private static final Optional<LocalDate> JAN_FIRST_2019 = Optional.of(LocalDate.of(2019, JANUARY, 1));

	@Test
    public void givenTimelineWithEmptyDates_whenVerifyingEmptyness_thenTrue() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), Optional.empty());
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithOnlyStartDateSet_whenVerifyingEmptyness_thenFalse() {
        DashboardTimeline timeline = new DashboardTimeline(JAN_FIRST_2019, Optional.empty());
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithOnlyEndDateSet_whenVerifyingEmptyness_thenFalse() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), JAN_FIRST_2019);
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(false);
    }
    
    @Test
    public void givenTimelineWithBothDatesSet_whenVerifyingEmptyness_thenFalse() {
        DashboardTimeline timeline = new DashboardTimeline(JAN_FIRST_2019, JAN_FIRST_2019);
        Assertions.assertThat(timeline.hasBothDates()).isEqualTo(true);
    }
}
