package objective.taskboard.configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.configuration.exception.DashboardConfigurationDuplicateException;
import objective.taskboard.configuration.exception.DashboardConfigurationNotFoundException;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

public class DashboardConfigurationControllerTest {

    private static final String DEFAULT_PROJECT_KEY = "TEST";
    private DashboardConfigurationController subject;
    private DashboardConfigurationService service = mock(DashboardConfigurationService.class);
    private DashboardConfiguration defaultConfiguration;
    private ProjectAdministrationPermission authorizer = mock(ProjectAdministrationPermission.class);

    @Before
    public void setUp() {
        defaultConfiguration = new DashboardConfiguration();
        defaultConfiguration.setId(2L);
        defaultConfiguration.setTimelineDaysToDisplay(10);
    }
    
    @Test
    public void givenProjectConfigured_whenRetrieving_thenHappyPath() {
        mockServiceToReturnDefaultConfiguration();
    
        subject = new DashboardConfigurationController(service, authorizer);
    
        AssertResponse.of(subject.retrieve(DEFAULT_PROJECT_KEY))
            .httpStatus(HttpStatus.OK)
            .bodyAsJson("{\"id\":2,\"timelineDaysToDisplay\":10}");
    }

    @Test
    public void givenWithoutAdminPermission_whenCreating_thenNotFoundResponse() {
        withoutAdminPermission();
        
        DashboardConfigurationDto defaultConfigurationDto = new DashboardConfigurationDto();
        defaultConfigurationDto.timelineDaysToDisplay = 10;

        subject = new DashboardConfigurationController(service, authorizer);

        AssertResponse.of(subject.create(DEFAULT_PROJECT_KEY, defaultConfigurationDto))
                .httpStatus(HttpStatus.NOT_FOUND)
                .emptyBody();
    }

    @Test
    public void givenWithoutAdminPermission_whenUpdating_thenNotFoundResponse() {
        withoutAdminPermission();
        
        DashboardConfigurationDto defaultConfigurationDto = new DashboardConfigurationDto();
        defaultConfigurationDto.id = 2L;
        defaultConfigurationDto.timelineDaysToDisplay = 10;

        subject = new DashboardConfigurationController(service, authorizer);

        AssertResponse.of(subject.update(DEFAULT_PROJECT_KEY, defaultConfigurationDto))
                .httpStatus(HttpStatus.NOT_FOUND)
                .emptyBody();
    }

    @Test
    public void givenConfigurationNotFound_whenRetrieving_thenNotFoundResponse() {
        when(service.retrieveConfiguration(DEFAULT_PROJECT_KEY)).thenReturn(Optional.empty());

        subject = new DashboardConfigurationController(service, authorizer);

        AssertResponse.of(subject.retrieve(DEFAULT_PROJECT_KEY))
            .httpStatus(HttpStatus.NOT_FOUND)
            .emptyBody();
    }
    
    @Test
    public void givenWithAdminPermissionAndProjectNotYetConfigured_whenCreating_thenHappyPath() {
        withAdminPermission();
        when(service.persistConfigurationForProject(eq(DEFAULT_PROJECT_KEY), any(DashboardConfiguration.class)))
        	.thenReturn(defaultConfiguration);
        
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = new DashboardConfigurationDto();
        newConfigurationDto.timelineDaysToDisplay = 10;
        
        AssertResponse.of(subject.create(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.CREATED)
            .bodyAsJson(
                    "{"
                        + "\"id\":" + defaultConfiguration.getId() + ","
                        + "\"timelineDaysToDisplay\":" + defaultConfiguration.getTimelineDaysToDisplay()
                    + "}");
    }
    
    @Test
    public void givenWithAdminPermissionAndProjectAlreadyConfigured_whenCreating_thenUnprocessableRequestResponse() {
        withAdminPermission();
        when(service.persistConfigurationForProject(eq(DEFAULT_PROJECT_KEY), any(DashboardConfiguration.class)))
            .thenThrow(new DashboardConfigurationDuplicateException(DEFAULT_PROJECT_KEY));

        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = new DashboardConfigurationDto();
        newConfigurationDto.timelineDaysToDisplay = 10;
        
        AssertResponse.of(subject.create(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .bodyAsString("A dashboard configuration already exists for project TEST.");
    }
    
    @Test
    public void givenWithAdminPermissionAndNullDto_whenCreating_thenBadRequest() {
        withAdminPermission();
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = null;
        
        AssertResponse.of(subject.create(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString("Null DTO cannot be parsed.");
    }
    
    @Test
    public void givenWithAdminPermissionAndNullDto_whenUpdating_thenBadRequest() {
        withAdminPermission();
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = null;
        
        AssertResponse.of(subject.update(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString("Null DTO cannot be parsed.");
    }
    
    @Test
    public void givenWithAdminPermissionAndInvalidTimeline_whenCreating_thenBadRequest() {
        withAdminPermission();
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = new DashboardConfigurationDto();
        newConfigurationDto.timelineDaysToDisplay = 0;
        
        AssertResponse.of(subject.create(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString("Timeline days to show must be positive.");
    }
    
    @Test
    public void givenWithAdminPermissionAndInvalidTimeline_whenUpdating_thenBadRequest() {
        withAdminPermission();
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = new DashboardConfigurationDto();
        newConfigurationDto.timelineDaysToDisplay = -1;
        
        AssertResponse.of(subject.update(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString("Timeline days to show must be positive.");
    }

    @Test
    public void givenWithAdminPermissionAndProjectNotYetConfigured_whenUpdating_thenMethodNotAllowed() {
        withAdminPermission();
        when(service.updateConfigurationForProject(eq(DEFAULT_PROJECT_KEY), any(DashboardConfiguration.class)))
            .thenThrow(new DashboardConfigurationNotFoundException(DEFAULT_PROJECT_KEY));

        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto mewConfiguration = new DashboardConfigurationDto();
        mewConfiguration.timelineDaysToDisplay = 10;
        AssertResponse.of(subject.update(DEFAULT_PROJECT_KEY, mewConfiguration))
            .httpStatus(HttpStatus.METHOD_NOT_ALLOWED)
            .bodyAsString("Missing dashboard configuration for project TEST.");
    }
    
    @Test
    public void givenWithAdminPermissionAndProjectAlreadyConfigured_whenUpdating_thenHappyPath() {
        withAdminPermission();
        when(service.updateConfigurationForProject(eq(DEFAULT_PROJECT_KEY), any(DashboardConfiguration.class)))
        	.thenReturn(defaultConfiguration);
        subject = new DashboardConfigurationController(service, authorizer);

        DashboardConfigurationDto newConfigurationDto = new DashboardConfigurationDto();
        newConfigurationDto.id = 2L;
        newConfigurationDto.timelineDaysToDisplay = 10;

        AssertResponse.of(subject.update(DEFAULT_PROJECT_KEY, newConfigurationDto))
            .httpStatus(HttpStatus.OK)
            .bodyAsJson("{\"id\":2,\"timelineDaysToDisplay\":10}");
    }

    private void withoutAdminPermission() {
        when(authorizer.isAuthorizedFor(DEFAULT_PROJECT_KEY)).thenReturn(false);
    }

    private void withAdminPermission() {
        when(authorizer.isAuthorizedFor(DEFAULT_PROJECT_KEY)).thenReturn(true);
    }

    private void mockServiceToReturnDefaultConfiguration() {
        when(service.retrieveConfiguration(DEFAULT_PROJECT_KEY)).thenReturn(Optional.of(defaultConfiguration));
    }
}
