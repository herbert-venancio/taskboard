package objective.taskboard.jira;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueJqlBuilderServiceTest {
    @InjectMocks
    private JiraIssueJqlBuilderService subject;

    @Mock
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Mock
    private FilterCachedRepository filterRepository;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Test
    public void whenProjectsJql_produceProjectsJql() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        String actual = subject.projectsJql().trim();
        
        assertEquals("project in ('PROJ')", actual);
    }
    
    @Test
    public void whenIssuesJqlWithoutFilters_shouldProduceSearchOnlyForProjects() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        
        when(filterRepository.getCache()).thenReturn(asList());
        
        String actual = subject.buildQueryForIssues().trim();
        
        assertEquals("(project in ('PROJ') )", actual);
    }
    
    @Test
    public void whenIssuesJql_produceJqlToSearchForIssues() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        
        when(filterRepository.getCache()).thenReturn(
                asList(filter().issueTypeId(10l).status(66l).build(),
                       filter().issueTypeId(30l).status(76l).build(),
                       filter().issueTypeId(10l).status(86l).limitInDays("-14d").build(),
                       filter().issueTypeId(10l).status(42l).limitInDays("-14d").build()
                       )
                );
        
        when(jiraProperties.getStatusesDeferredIds()).thenReturn(asList(666l, 999l));
        
        String actual = subject.buildQueryForIssues().trim();
        
        assertEquals("(project in ('PROJ') ) AND ("
                + "(status=66 AND type=10) "
                + "OR (status=76 AND type=30) "
                + "OR (type=10 AND status CHANGED TO 86 AFTER -14d) "
                + "OR (type=10 AND status CHANGED TO 42 AFTER -14d) "
                + "OR (status in (666,999))"
                + ")", 
                actual);
    }
    
    private FilterBuilder filter() {
        return new FilterBuilder();
    }
    
    private static class FilterBuilder {
        Filter filter = new Filter();
        
        public FilterBuilder issueTypeId(Long l) {
            filter.setIssueTypeId(l);
            return this;
        }
        
        public FilterBuilder status(long l) {
            filter.setStatusId(l);
            return this;
        }
        
        public FilterBuilder limitInDays(String s) {
            filter.setLimitInDays(s);
            return this;
        }
        
        public Filter build() {
            return filter;
        }
    }
}