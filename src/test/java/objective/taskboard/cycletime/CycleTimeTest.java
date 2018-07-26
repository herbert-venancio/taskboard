package objective.taskboard.cycletime;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.cycletime.CycleTimeProperties.Time;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.testUtils.FixedClock;

@RunWith(MockitoJUnitRunner.class)
public class CycleTimeTest {

    private static final long DERREFED_STATUS_ID = 10102L;
    private static final long CANCELED_STATUS_ID = 10101L;
    private static final long COMPLETED_STATUS_ID = 10001L;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Mock
    private CycleTimeProperties cycleTimeProperties;

    @Mock
    private FixedClock clock;

    @Mock
    private JiraProperties jiraProperties;

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private CycleTime subject;

    @Before
    public void setup() {
        Instant now = ZonedDateTime.of(2017, 12, 18, 15, 57, 28, 867000000, ZONE_ID).toInstant();
        when(clock.now()).thenReturn(now);

        when(cycleTimeProperties.getStartBusinessHours())
            .thenReturn(new Time(9, 0, "am"));
        
        when(cycleTimeProperties.getEndBusinessHours())
            .thenReturn(new Time(6, 0, "pm"));

        when(jiraProperties.getStatusesCompletedIds())
            .thenReturn(asList(COMPLETED_STATUS_ID));
        
        when(jiraProperties.getStatusesCanceledIds())
            .thenReturn(asList(CANCELED_STATUS_ID));
        
        when(jiraProperties.getStatusesDeferredIds())
            .thenReturn(asList(DERREFED_STATUS_ID));
    }

    @Test
    public void whenGetCycleTime_returnCorrectValue() {
        ZonedDateTime startDate = ZonedDateTime.of(2017, 12, 12, 15, 54, 03, 0, ZONE_ID);
        Double cycleTimeValue = subject.getCycleTime(startDate.toInstant(), ZONE_ID, 0L).get();

        Double expected = 4.006353919753087;
        assertEquals(expected, cycleTimeValue);
    }

    @Test
    public void ifStartDateIsTheSameDayOfNow_calcJustTheDifferenceBetweenThemInMillis() {
        ZonedDateTime startDate = ZonedDateTime.of(2017, 12, 18, 10, 0, 0, 0, ZONE_ID);
        Double cycleTimeValue = subject.getCycleTime(startDate.toInstant(), ZONE_ID, 0L).get();

        Double expected = 0.6620020679012346;
        assertEquals(expected, cycleTimeValue);
    }

    @Test
    public void ifIncalculableStatus_returnNegative() {
        ZonedDateTime startDate = ZonedDateTime.of(2017, 12, 12, 15, 54, 03, 0, ZONE_ID);
        Double expected = 0D;

        Double cycleTimeWithCompletedStatus = subject.getCycleTime(startDate.toInstant(), ZONE_ID, COMPLETED_STATUS_ID).orElse(0D);
        assertEquals(expected, cycleTimeWithCompletedStatus);

        Double cycleTimeWithCanceledStatus = subject.getCycleTime(startDate.toInstant(), ZONE_ID, CANCELED_STATUS_ID).orElse(0D);
        assertEquals(expected, cycleTimeWithCanceledStatus);

        Double cycleTimeWithDeferredStatus = subject.getCycleTime(startDate.toInstant(), ZONE_ID, DERREFED_STATUS_ID).orElse(0D);
        assertEquals(expected, cycleTimeWithDeferredStatus);
        
        long otherStatusId = 1L;
        Double cycleTimeWithOtherStatus = subject.getCycleTime(startDate.toInstant(), ZONE_ID, otherStatusId).get();
        assertNotEquals(expected, cycleTimeWithOtherStatus);
    }

    @Test
    public void ifNowIsBeforeStartDate_returnZero() {
        ZonedDateTime startDate = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZONE_ID);
        Double cycleTimeValue = subject.getCycleTime(startDate.toInstant(), ZONE_ID, 0L).get();

        Double expected = 0D;
        assertEquals(expected, cycleTimeValue);
    }

}
