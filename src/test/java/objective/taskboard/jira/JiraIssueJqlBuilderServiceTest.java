package objective.taskboard.jira;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.DateTimeUtilSupport.date;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.issueBuffer.CardRepo;
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
    
    @Mock
    private MetadataCachedService metadataService;
    
    @Mock
    private CardRepo cardsRepo;
    
    @Before
    public void setup() {
        when(metadataService.getJiraTimeZone()).thenReturn(ZoneId.of("America/Sao_Paulo"));
        when(cardsRepo.getCurrentProjects()).thenReturn(Optional.of(Sets.newSet("PROJ")));
    }
    
    @Test
    public void whenProjectsJql_produceProjectsJql() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        
        String actual = subject.projectsSqlWithoutTimeConstraint().trim();
        
        assertEquals("project in ('PROJ')", actual);
    }
    
    @Test
    public void whenProjectsJqlWithDate_produceProjectsJqlWithUpdatedDate() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        
        when(cardsRepo.getLastUpdatedDate()).thenReturn(Optional.of(date(2017,5,1,10,30)));
        String actual = subject.projectsJql(cardsRepo).trim();
        
        assertEquals("(project in ('PROJ') ) AND updated >= '2017-05-01 10:30'", actual);
    }
    
    
    @Test
    public void whenIssuesJqlWithoutFilters_shouldProduceSearchOnlyForProjects() {
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectKey("PROJ");
        when(projectRepository.getProjects()).thenReturn(asList(projectFilterConfiguration));
        
        when(filterRepository.getCache()).thenReturn(asList());
        when(cardsRepo.getLastUpdatedDate()).thenReturn(Optional.empty());
        String actual = subject.buildQueryForIssues(cardsRepo).trim();
        
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
        
        when(cardsRepo.getLastUpdatedDate()).thenReturn(Optional.empty());
        
        String actual = subject.buildQueryForIssues(cardsRepo).trim();
        
        assertEquals("(project in ('PROJ') ) AND ("
                + "(status=66 AND type=10) "
                + "OR (status=76 AND type=30) "
                + "OR (type=10 AND status CHANGED TO 86 AFTER -14d) "
                + "OR (type=10 AND status CHANGED TO 42 AFTER -14d) "
                + "OR (status in (666,999))"
                + ")", 
                actual);
    }
    
    @Test
    public void whenIssuesJqlWithUpdateDate_produceJqlToSearchForIssues() {
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
        
        when(cardsRepo.getLastUpdatedDate()).thenReturn(Optional.of(date(2017,5,1,10,30)));
        
        String actual = subject.buildQueryForIssues(cardsRepo).trim();
        
        assertEquals("((project in ('PROJ') ) AND ("
                + "(status=66 AND type=10) "
                + "OR (status=76 AND type=30) "
                + "OR (type=10 AND status CHANGED TO 86 AFTER -14d) "
                + "OR (type=10 AND status CHANGED TO 42 AFTER -14d) "
                + "OR (status in (666,999))"
                + ")) AND updated >= '2017-05-01 10:30'", 
                actual);
    }
    
    @Test
    public void whenIssuesJqlANewProjectShowsUpBetweenInvocations_jqlMustSearchNewProjectIssuesWithoutUpdateConstraint() {
        ProjectFilterConfiguration p1 = new ProjectFilterConfiguration();
        p1.setProjectKey("PROJ");
        List<ProjectFilterConfiguration> projects = new ArrayList<>();
        projects.add(p1);
        when(projectRepository.getProjects()).thenReturn(projects);
        
        when(filterRepository.getCache()).thenReturn(
                asList(filter().issueTypeId(10l).status(66l).build(),
                       filter().issueTypeId(30l).status(76l).build(),
                       filter().issueTypeId(10l).status(86l).limitInDays("-14d").build(),
                       filter().issueTypeId(10l).status(42l).limitInDays("-14d").build()
                       )
                );
        
        when(jiraProperties.getStatusesDeferredIds()).thenReturn(asList(666l, 999l));
        
        when(cardsRepo.getLastUpdatedDate()).thenReturn(Optional.of(date(2017,5,1,10,30)));
        
        subject.buildQueryForIssues(cardsRepo).trim();
        
        ProjectFilterConfiguration p2 = new ProjectFilterConfiguration();
        p2.setProjectKey("PROJ2");
        projects.add(p2);
        
        String actualWithNewProject = subject.buildQueryForIssues(cardsRepo).trim();
        
        assertEquals("(((project in ('PROJ','PROJ2') ) AND ("
                + "(status=66 AND type=10) "
                + "OR (status=76 AND type=30) "
                + "OR (type=10 AND status CHANGED TO 86 AFTER -14d) "
                + "OR (type=10 AND status CHANGED TO 42 AFTER -14d) "
                + "OR (status in (666,999))"
                + ")) AND updated >= '2017-05-01 10:30') OR (project in ('PROJ2'))", 
                actualWithNewProject);
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