package objective.taskboard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.configuration.exception.DashboardConfigurationDuplicateException;
import objective.taskboard.configuration.exception.DashboardConfigurationNotFoundException;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;


public class DashboardConfigurationServiceTest {

    private ProjectService projectService = mock(ProjectService.class);
    private DashboardConfigurationRepository dashboardConfigurationRepository = mock(DashboardConfigurationRepository.class);
    private DashboardConfigurationService subject;
    private String defaultProjectKey;
    private ProjectFilterConfiguration defaultProject;
    private DashboardConfiguration defaultConfiguration;
    private DashboardConfiguration updatedConfiguration;
    
    @Before
    public void setup() {
        defaultProjectKey = "TEST";
        defaultProject = new ProjectFilterConfiguration(defaultProjectKey, 0L);
        defaultConfiguration = new DashboardConfiguration();
        defaultConfiguration.setId(2L);
        defaultConfiguration.setTimelineDaysToDisplay(10);
        defaultConfiguration.setProject(defaultProject);
        updatedConfiguration = new DashboardConfiguration();
        updatedConfiguration.setId(2L);
        updatedConfiguration.setTimelineDaysToDisplay(20);
        updatedConfiguration.setProject(defaultProject);
        subject = new DashboardConfigurationService(projectService, dashboardConfigurationRepository);
    }

    @Test
    public void givenProjectExistsAndConfigurationExists_whenRetrievingConfiguration_thenHappyPath() {
        projectExists();
        dashboardConfigurationExists();
        
        Optional<DashboardConfiguration> actual = subject.retrieveConfiguration(defaultProjectKey);
        assertThat(actual).isPresent();
        assertThat(actual).map(DashboardConfiguration::getId).hasValue(2L);
        assertThat(actual).map(DashboardConfiguration::getTimelineDaysToDisplay).hasValue(10);
        assertThat(actual).map(DashboardConfiguration::getProject)
            .map(ProjectFilterConfiguration::getProjectKey).hasValue("TEST");
    }
    
    @Test
    public void givenProjectExistsAndProjectNotYetConfigured_whenPersistingConfiguration_thenHappyPath() {
        projectExists();
        missingDashboardConfiguration();

        mockSaveToReturnDefaultConfiguration();
        
        DashboardConfiguration actual = subject.persistConfigurationForProject(defaultProjectKey, defaultConfiguration);
        
        assertThat(actual.getId()).isEqualTo(2L);
        assertThat(actual.getTimelineDaysToDisplay()).isEqualTo(10);
        assertThat(actual.getProject().getProjectKey()).isEqualTo("TEST");
    }

    @Test
    public void givenProjectExistsAndProjectConfigured_whenUpdatingConfiguration_thenHappyPath() {
        projectExists();
        dashboardConfigurationExists();

        mockSaveToReturnUpdatedConfiguration();
        
        subject = new DashboardConfigurationService(projectService, dashboardConfigurationRepository);
        
        DashboardConfiguration actual = subject.updateConfigurationForProject(defaultProjectKey, updatedConfiguration);
        
        assertThat(actual.getId()).isEqualTo(2L);
        assertThat(actual.getTimelineDaysToDisplay()).isEqualTo(20);
        assertThat(actual.getProject().getProjectKey()).isEqualTo("TEST");
    }

    @Test
    public void givenProjectExistsAndMissingConfiguration_whenRetrievingConfiguration_thenEmptyConfiguration() {
        projectExists();
        missingDashboardConfiguration();
        
        Optional<DashboardConfiguration> actual = subject.retrieveConfiguration(defaultProjectKey);
        assertThat(actual).isEmpty();
    }
    
    @Test
    public void givenProjectExistsAndConfigurationExists_whenCreatingConfiguration_thenDuplicateException() {
        projectExists();
        dashboardConfigurationExists();
        
        DashboardConfiguration newConfiguration = new DashboardConfiguration();
        newConfiguration.setTimelineDaysToDisplay(30);
        assertThatExceptionOfType(DashboardConfigurationDuplicateException.class)
            .isThrownBy(() -> subject.persistConfigurationForProject(defaultProjectKey, newConfiguration))
            .withMessage(String.format(DashboardConfigurationDuplicateException.MESSAGE, defaultProjectKey));
    }
    
    @Test
    public void givenProjectExistsAndProjectNotYetConfigured_whenUpdatingConfiguration_thenNotFoundException() {
        projectExists();
        missingDashboardConfiguration();
        
        assertThatExceptionOfType(DashboardConfigurationNotFoundException.class)
            .isThrownBy(() -> subject.updateConfigurationForProject(defaultProjectKey, Mockito.any(DashboardConfiguration.class)))
            .withMessage(String.format(DashboardConfigurationNotFoundException.MESSAGE, defaultProjectKey));
    }

    @Test
    public void givenMissingProject_whenRetrievingConfiguration_thenNotFoundException() {
        missingProject();
        
        assertThatExceptionOfType(DashboardConfigurationNotFoundException.class)
            .isThrownBy(() -> subject.retrieveConfiguration(defaultProjectKey))
            .withMessage(String.format(DashboardConfigurationNotFoundException.MESSAGE, defaultProjectKey));
    }
    
    @Test
    public void givenMissingProject_whenCreatingConfiguration_thenNotFoundException() {
        missingProject();
        
        assertThatExceptionOfType(DashboardConfigurationNotFoundException.class)
            .isThrownBy(() -> subject.persistConfigurationForProject(defaultProjectKey, Mockito.any(DashboardConfiguration.class)))
            .withMessage(String.format(DashboardConfigurationNotFoundException.MESSAGE, defaultProjectKey));
    }
    
    @Test
    public void givenMissingProject_whenUpdatingConfiguration_thenNotFoundException() {
        missingProject();
        
        assertThatExceptionOfType(DashboardConfigurationNotFoundException.class)
            .isThrownBy(() -> subject.updateConfigurationForProject(defaultProjectKey, Mockito.any(DashboardConfiguration.class)))
            .withMessage(String.format(DashboardConfigurationNotFoundException.MESSAGE, defaultProjectKey));
    }

    private void mockSaveToReturnDefaultConfiguration() {
        when(dashboardConfigurationRepository.save(Mockito.any(DashboardConfiguration.class))).thenReturn(defaultConfiguration);
    }

    private void mockSaveToReturnUpdatedConfiguration() {
        when(dashboardConfigurationRepository.save(Mockito.any(DashboardConfiguration.class))).thenReturn(updatedConfiguration);
    }

    private void dashboardConfigurationExists() {
        when(dashboardConfigurationRepository.findByProject(defaultProject)).thenReturn(Optional.of(defaultConfiguration));
    }

    private void missingDashboardConfiguration() {
        when(dashboardConfigurationRepository.findByProject(defaultProject)).thenReturn(Optional.empty());
    }

    private void projectExists() {
        when(projectService.getTaskboardProject(defaultProjectKey)).thenReturn(Optional.of(defaultProject));
    }

    private void missingProject() {
        when(projectService.getTaskboardProject(defaultProjectKey)).thenReturn(Optional.empty());
    }
}
