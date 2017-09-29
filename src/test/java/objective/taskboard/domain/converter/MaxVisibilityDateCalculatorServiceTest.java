package objective.taskboard.domain.converter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Changelog;
import objective.taskboard.domain.Filter;
import objective.taskboard.repository.FilterCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class MaxVisibilityDateCalculatorServiceTest {
    
    @Mock
    FilterCachedRepository filterRepository;
    
    @InjectMocks
    MaxVisibilityDateCalculatorService subject;
    
    @Test
    public void withChangeLog_CalculateMaxVisibilityDate() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        int status = 42;
        Filter filter = makeFilter(status);
        when(filterRepository.getCache()).thenReturn(Arrays.asList(filter));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList(changelog));
        
        assertEquals(29, dt.get().get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void withWithoutLimit_ShouldReturnEmpty() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        int status = 42;
        Filter filter = new Filter();
        filter.setStatusId(status);
        when(filterRepository.getCache()).thenReturn(Arrays.asList(filter));
        
        assertFalse(subject.calculateVisibleUntil(status, asList(changelog)).isPresent());
    }
    
    @Test
    public void withNoChangeLogs_ReturnNotPresent() {
        int status = 42;
        Filter filter = makeFilter(status);
        when(filterRepository.getCache()).thenReturn(Arrays.asList(filter));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList());
        
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withNoChangeLogsWithMatchinStatus_ReturnNotPresent() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        
        int status = 43;
        Filter filter = makeFilter(status);
        when(filterRepository.getCache()).thenReturn(Arrays.asList(filter));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList(changelog));
        
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withFilters_ReturnNotPresent() {
        Changelog changelog = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));

        when(filterRepository.getCache()).thenReturn(asList());
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(43, asList(changelog));
        
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withMultipleMatchingChangeLogs_CalculateMaxVisibilityDateBasedOnMostRecent() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 16, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        
        int status = 42;
        Filter filter = makeFilter(status);
        when(filterRepository.getCache()).thenReturn(Arrays.asList(filter));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList(changelog1, changelog2, changelog3,changelog4));
        
        assertEquals(30, dt.get().get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void whenNoFiltersMatchStatus_ShouldReturnEmpty() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 16, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        
        when(filterRepository.getCache()).thenReturn(Arrays.asList(makeFilter(55)));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(42, asList(changelog1, changelog2, changelog3,changelog4));
        assertFalse(dt.isPresent());
    }
    
    @Test
    public void withMultipleFilters_ShouldUserOnlyFiltersMatchingCorrectStatus() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 16, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog4 = new Changelog(null, "author", "1", "Done", "bogus",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        
        int status = 42;
        when(filterRepository.getCache()).thenReturn(Arrays.asList(makeFilter(55),makeFilter(status)));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList(changelog1, changelog2, changelog3,changelog4));
        
        assertEquals(30, dt.get().get(ChronoField.DAY_OF_MONTH));
    }
    
    @Test
    public void withMultipleFilters_ShouldUserOnlyFiltersMatchingCorrectStatusAndThatHasLimitConfigured() {
        Changelog changelog1 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 15, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog2 = new Changelog(null, "status", "1", "To Do", "42",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 16, 1, 0, 0, 0, ZoneId.systemDefault())));
        Changelog changelog3 = new Changelog(null, "status", "1", "Done", "43",ZonedDateTime.from(ZonedDateTime.of(2120, 1, 17, 1, 0, 0, 0, ZoneId.systemDefault())));
        
        int status = 42;
        when(filterRepository.getCache()).thenReturn(Arrays.asList(makeFilter(55),makeFilter(status,null),makeFilter(status)));
        
        Optional<LocalDateTime> dt =  subject.calculateVisibleUntil(status, asList(changelog1, changelog2, changelog3));
        
        assertEquals(30, dt.get().get(ChronoField.DAY_OF_MONTH));
    }

    private Filter makeFilter(int status) {
        Filter filter = new Filter();
        filter.setStatusId(status);
        filter.setLimitInDays("-14d");
        return filter;
    }
    
    private Filter makeFilter(int status, String limit) {
        Filter filter = new Filter();
        filter.setStatusId(status);
        filter.setLimitInDays(limit);
        return filter;
    }
}