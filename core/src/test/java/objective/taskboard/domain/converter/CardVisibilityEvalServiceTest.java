package objective.taskboard.domain.converter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.filter.LaneService;

public class CardVisibilityEvalServiceTest {
    
    LaneService laneService;
    
    CardVisibilityEvalService subject;

    @Before
    public void setup() {
        laneService = mock(LaneService.class);
        subject = new CardVisibilityEvalService(laneService, null);
    }
    
    @Test
    public void withChangeLog_CalculateMaxVisibilityDate() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        long status = 42;
        IssuesConfiguration filter = makeFilter(status);
        when(laneService.getFilters()).thenReturn(Arrays.asList(filter));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(status, instant(2120, 1, 15), asList(changelog));
        
        assertEquals(29, dt.get().atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void withWithoutLimit_ShouldReturnEmpty() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        long status = 42;
        IssuesConfiguration filter = makeFilter(status, null);
        when(laneService.getFilters()).thenReturn(Arrays.asList(filter));
        
        assertFalse(subject.calculateVisibleUntil(status, instant(2120, 1, 15), asList(changelog)).isPresent());
    }
    
    @Test
    public void withFilters_ReturnNotPresent() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));

        when(laneService.getFilters()).thenReturn(asList());
        Optional<Instant> dt =  subject.calculateVisibleUntil(43l, instant(2120, 1, 15), asList(changelog));
        
        
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withMultipleMatchingChangeLogs_CalculateMaxVisibilityDateBasedOnMostRecent() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 16));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43", date(2120, 1, 17));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus", date(2120, 1, 17));
        
        long status = 42;
        IssuesConfiguration filter = makeFilter(status);
        when(laneService.getFilters()).thenReturn(Arrays.asList(filter));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(status, instant(2120, 1, 15), asList(changelog1, changelog2, changelog3,changelog4));
        
        assertEquals(30, dt.get().atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void whenNoFiltersMatchStatus_ShouldReturnEmpty() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 16));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43", date(2120, 1, 17));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus", date(2120, 1, 17));
        
        when(laneService.getFilters()).thenReturn(Arrays.asList(makeFilter(55)));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(42l, instant(2120, 1, 15), asList(changelog1, changelog2, changelog3,changelog4));
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withMultipleFilters_ShouldUserOnlyFiltersMatchingCorrectStatus() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 16));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43", date(2120, 1, 17));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus", date(2120, 1, 17));
        
        long status = 42;
        when(laneService.getFilters()).thenReturn(Arrays.asList(makeFilter(55),makeFilter(status)));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(status, instant(2120, 1, 15), asList(changelog1, changelog2, changelog3,changelog4));
        
        assertEquals(30, dt.get().atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void withMultipleFilters_ShouldUserOnlyFiltersMatchingCorrectStatusAndThatHasLimitConfigured() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 15));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 16));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43", date(2120, 1, 17));
        
        long status = 42;
        when(laneService.getFilters()).thenReturn(Arrays.asList(makeFilter(55),makeFilter(status,null),makeFilter(status)));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(status, instant(2120, 1, 15), asList(changelog1, changelog2, changelog3));
        
        assertEquals(30, dt.get().atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void withStatusMatchingFilter_ShouldUseLastUpdatedDateToCalculate() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 8));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42", date(2120, 1, 9));
        
        long status = 43;
        when(laneService.getFilters()).thenReturn(Arrays.asList(makeFilter(43),makeFilter(status,null),makeFilter(status)));
        
        Optional<Instant> dt =  subject.calculateVisibleUntil(status, instant(2120, 1, 10), asList(changelog1, changelog2));
        
        assertEquals(24, dt.get().atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_MONTH));
    }
    
    ZonedDateTime date(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 1, 0, 0, 0, ZoneId.systemDefault());
    }
    
    Instant instant(int year, int month, int day) {
        return date(year,month, day).toInstant();
    }

    private IssuesConfiguration makeFilter(long status) {
        return makeFilter(status, -14);
    }
    
    private IssuesConfiguration makeFilter(long status, Integer limit) {
        return new IssuesConfiguration(0L, status, limit);
    }
}