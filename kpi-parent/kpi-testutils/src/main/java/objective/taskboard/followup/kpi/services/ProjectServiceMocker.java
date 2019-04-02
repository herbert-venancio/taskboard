package objective.taskboard.followup.kpi.services;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;

public class ProjectServiceMocker {

    private MockedServices services;
    private ProjectService projectService;
    private Map<String, ProjectBuilder> projects = new LinkedHashMap<>();

    public ProjectServiceMocker(MockedServices services) {
        this.services = services;
    }

    public ProjectBuilder withKey(String key) {
        projects.putIfAbsent(key, new ProjectBuilder(key));
        return projects.get(key);
    }

    public ProjectService getService() {
        if(projectService == null)
            mockService();
        return projectService;
    }

    public List<String> getProjectsKeys(){
        return projects.values().stream().map(p -> p.key).collect(Collectors.toList());
    }

    public ProjectFilterConfiguration getProject(String projectKey) {
        return projects.get(projectKey).getMock();
    }

     void mockService() {
         projectService =  Mockito.mock(ProjectService.class);
         configureExceptionToNotConfiguredProjects();
         projects.values().stream().forEach(p -> p.mockProject(projectService));

    }

    private void configureExceptionToNotConfiguredProjects() {
        Mockito.when(projectService.getTaskboardProjectOrCry(Mockito.any())).thenAnswer(i -> {
            String projectKey = i.getArgumentAt(0, String.class);
            throw new IllegalArgumentException("Project with key '" + projectKey + "' not found");
        });
    }

    public MockedServices eoPs() {
        return services;
    }

    public class ProjectBuilder {

        private ProjectFilterConfiguration mockedProject;
        private String key;
        private String startDate;
        private String deliveryDate;

        public ProjectBuilder(String key) {
            this.key = key;
        }

        public void mockProject(ProjectService projectService) {
            ProjectFilterConfiguration project = getMock();
            Mockito.doReturn(project).when(projectService).getTaskboardProjectOrCry(key);
            Mockito.when(projectService.taskboardProjectExists(key)).thenReturn(true);
        }

        private ProjectFilterConfiguration getMock() {
            if(mockedProject == null)
                mockedProject = prepareMock();
            return mockedProject;

        }

        private ProjectFilterConfiguration prepareMock() {
            ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);

            Mockito.when(project.getStartDate()).thenReturn(getStartDate());
            Mockito.when(project.getDeliveryDate()).thenReturn(getDeliveryDate());
            Mockito.when(project.getProjectKey()).thenReturn(key);
            return project;
        }

        private Optional<LocalDate> getStartDate() {
            return Optional.ofNullable(startDate).map(LocalDate::parse);
        }

        private Optional<LocalDate> getDeliveryDate() {
            return Optional.ofNullable(deliveryDate).map(LocalDate::parse);
        }

        public ProjectBuilder startAt(String startDate) {
            this.startDate = startDate;
            return this;
        }

        public ProjectBuilder deliveredAt(String deliveryDate) {
            this.deliveryDate = deliveryDate;
            return this;
        }

        public ProjectServiceMocker eoP() {
            return ProjectServiceMocker.this;
        }
    }
}
