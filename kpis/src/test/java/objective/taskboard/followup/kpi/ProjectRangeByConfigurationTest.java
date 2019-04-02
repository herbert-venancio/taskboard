package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.filters.ProjectRangeByConfiguration;
import objective.taskboard.utils.RangeUtils;

public class ProjectRangeByConfigurationTest {

    @Test
    public void happyDay() {
        ProjectFilterConfiguration project = withProject()
                                            .withStartDate("2018-01-10")
                                            .withDeliveryDate("2018-01-20").mock();
        
        ProjectRangeByConfiguration subject = new ProjectRangeByConfiguration(project);
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-05","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-22")));
        assertFalse(subject.isWithinRange(getRange("2018-01-02","2018-01-09")));
        assertFalse(subject.isWithinRange(getRange("2018-01-22","2018-01-25")));
    }
  
    @Test
    public void withoutStartingDate() {
        ProjectFilterConfiguration project = withProject()
                .withDeliveryDate("2018-01-20").mock();

        ProjectRangeByConfiguration subject = new ProjectRangeByConfiguration(project);
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-05","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-22")));
        assertTrue(subject.isWithinRange(getRange("2018-01-02","2018-01-09")));
        assertFalse(subject.isWithinRange(getRange("2018-01-22","2018-01-25")));
        
    }
    
    @Test
    public void withoutDeliveryDate() {
        ProjectFilterConfiguration project = withProject()
                .withStartDate("2018-01-10").mock();

        ProjectRangeByConfiguration subject = new ProjectRangeByConfiguration(project);
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-05","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-22")));
        assertFalse(subject.isWithinRange(getRange("2018-01-02","2018-01-09")));
        assertTrue(subject.isWithinRange(getRange("2018-01-22","2018-01-25")));
    }
    
    @Test
    public void timelineNotConfigured() {
        ProjectFilterConfiguration project = withProject().mock();

        ProjectRangeByConfiguration subject = new ProjectRangeByConfiguration(project);
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-05","2018-01-19")));
        assertTrue(subject.isWithinRange(getRange("2018-01-15","2018-01-22")));
        assertTrue(subject.isWithinRange(getRange("2018-01-02","2018-01-09")));
        assertTrue(subject.isWithinRange(getRange("2018-01-22","2018-01-25")));
    }
    
    private ProjectMocker withProject() {
        return new ProjectMocker();
    }
    
    private Range<LocalDate> getRange(String startRange, String endRange) {
        return RangeUtils.between(LocalDate.parse(startRange), LocalDate.parse(endRange));
    }
    
    private class ProjectMocker {
        private Optional<LocalDate> startDate = Optional.empty();
        private Optional<LocalDate> deliveryDate = Optional.empty();
        
        private ProjectMocker withStartDate(String date) {
            startDate = Optional.of(parseDateTime(date).toLocalDate());
            return this;
        }
        
        private ProjectMocker withDeliveryDate(String date) {
            deliveryDate = Optional.of(parseDateTime(date).toLocalDate());
            return this;
        }
        
        private ProjectFilterConfiguration mock() {
            ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);
            when(project.getStartDate()).thenReturn(startDate);
            when(project.getDeliveryDate()).thenReturn(deliveryDate);
            return project;
        }
        
    }

}
