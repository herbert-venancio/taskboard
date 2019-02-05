package objective.taskboard.followup.budget;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardCustomerPermission;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

public class BudgetChartServiceTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void load_userHasPermission_OkWithDataReturned() throws Exception {
        BudgetChartService subject = new BudgetChartServiceBuilder()
                .point().date("2018-10-21").value(0.0).addPointToDone()
                .point().date("2018-10-22").value(20.0).addPointToDone()
                .point().date("2018-10-23").value(30.0).addPointToDone()
                .withAuthorizedUser()
                .withValidProject("TASKB")
                .build();

        BudgetChartData data = subject.load(ZoneId.of("UTC"), "TASKB");
        
        assertResults(data.scopeDone, 
                "2018-10-21 | 0.0",
                "2018-10-22 | 20.0",
                "2018-10-23 | 30.0"
                );
    }

    @Test
    public void load_userDoesntHavePermission_ProjectNotFoundExceptionThrown() throws Exception {
        BudgetChartService subject = new BudgetChartServiceBuilder()
                .withNonAuthorizedUser()
                .build();

        thrown.expect(ProjectNotFoundException.class);
        subject.load(ZoneId.of("UTC"), "TASKB");
    }

    private String budgetChartDataPointToString(BudgetChartDataPoint p) {
        return String.format(Locale.US, "%s | %.1f", 
                p.date,
                p.value);
    }

    private void assertResults(List<BudgetChartDataPoint> points, String... expectedBudgetChartDataPoints) {
        String actual = points.stream()
                .map(p -> budgetChartDataPointToString(p))
                .collect(joining("\n"));

        String expected = Stream.of(expectedBudgetChartDataPoints)
                .map(i -> i.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }

    private class BudgetChartServiceBuilder {
        private class BudgetChartDataPointDraft {
            private LocalDate date;
            private double value;
        }

        private BudgetChartCalculator calculator = mock(BudgetChartCalculator.class);
        private ProjectFilterConfigurationCachedRepository projects = mock(ProjectFilterConfigurationCachedRepository.class);
        private ProjectDashboardCustomerPermission dashboardCustomerPermission = mock(ProjectDashboardCustomerPermission.class);
        private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        private BudgetChartData bcd = new BudgetChartData();
        private BudgetChartDataPointDraft budgetChartDataPointDraft;

        public BudgetChartServiceBuilder point() {
            budgetChartDataPointDraft = new BudgetChartDataPointDraft();
            return this;
        }

        public BudgetChartServiceBuilder date(String date) {
            budgetChartDataPointDraft.date = LocalDate.parse(date);
            return this;
        }
        
        public BudgetChartServiceBuilder value (double value) {
            budgetChartDataPointDraft.value = value;
            return this;
        }

        public BudgetChartServiceBuilder addPointToDone() {
            bcd.scopeDone.add(new BudgetChartDataPoint(
                    budgetChartDataPointDraft.date,
                    budgetChartDataPointDraft.value));
            return this;
        }
        
        public BudgetChartServiceBuilder withAuthorizedUser() {
            when(dashboardCustomerPermission.isAuthorizedFor("TASKB")).thenReturn(true);
            return this;
        }
        
        public BudgetChartServiceBuilder withNonAuthorizedUser() {
            when(dashboardCustomerPermission.isAuthorizedFor("TASKB")).thenReturn(false);
            return this;
        }
        
        public BudgetChartServiceBuilder withValidProject(String projectKey) {
            when(projects.getProjectByKey(projectKey)).thenReturn(Optional.of(project));
            return this;
        }
                
        public BudgetChartService build() {
            when(calculator.calculate(ZoneId.of("UTC"), project)).thenReturn(bcd);
            return new BudgetChartService(calculator, projects, dashboardCustomerPermission);
        }
    }
    
}
