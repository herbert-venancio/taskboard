package objective.taskboard.followup.kpi.enviroment;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;

public class ProjectServiceMocker {
    
    private MockedServices services;
    private ProjectService projectService = Mockito.mock(ProjectService.class);
    private Map<String, ProjectBuilder> projects = new LinkedHashMap<>();
    
    public ProjectServiceMocker(MockedServices services) {
        this.services = services;
    }

    public ProjectBuilder withKey(String key) {
        projects.putIfAbsent(key, new ProjectBuilder(key));
        return projects.get(key);
    }
    
    public ProjectService getService() {
        projects.values().stream().forEach(p -> p.mockProject(projectService));
        return projectService;
    }

    public MockedServices eoPs() {
        return services;
    }

    public class ProjectBuilder {

        private String key;
        private String startDate;
        private String deliveryDate;

        public ProjectBuilder(String key) {
            this.key = key;
        }

        public void mockProject(ProjectService projectService) {
            ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);
            
            Mockito.when(project.getStartDate()).thenReturn(getStartDate());
            Mockito.when(project.getDeliveryDate()).thenReturn(getDeliveryDate());
                    
            Mockito.when(projectService.getTaskboardProjectOrCry(key)).thenReturn(project);
            
        }

        private Optional<LocalDate> getStartDate() {
            return Optional.ofNullable(LocalDate.parse(startDate));
        }

        private Optional<LocalDate> getDeliveryDate() {
            return Optional.ofNullable(LocalDate.parse(deliveryDate));
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
