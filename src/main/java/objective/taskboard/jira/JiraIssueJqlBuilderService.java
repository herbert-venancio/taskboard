package objective.taskboard.jira;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.issueBuffer.CardRepo;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class JiraIssueJqlBuilderService {
    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;
    
    @Autowired
    private FilterCachedRepository filterRepository;
    
    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private MetadataCachedService metadataService;
    
    public String projectsJql(CardRepo cardsRepo) {
        String jql = projectsSqlWithoutTimeConstraint();
        return addTimeConstraintIfPresent(cardsRepo.getLastUpdatedDate(), jql);
    }

    public String projectsSqlWithoutTimeConstraint() {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        String projectKeys = "'" + projects.stream()
                                           .map(ProjectFilterConfiguration::getProjectKey)
                                           .collect(Collectors.joining("','")) + "'";
        String jql = String.format("project in (%s) ", projectKeys);
        return jql;
    }

    public String buildQueryForIssues(CardRepo cardsRepo) {
        String jql = buildQueryForIssuesWithouTimeConstraint();
        jql = addTimeConstraintIfPresent(cardsRepo.getLastUpdatedDate(), jql);
        return addNewProjectsIfPresent(cardsRepo, jql);
    }
    
    public String buildQueryForIssuesWithouTimeConstraint() {
        List<Filter> filters = filterRepository.getCache();
        List<IssuesConfiguration> configs = filters.stream().map(x -> IssuesConfiguration.fromFilter(x)).collect(Collectors.toList());
        
        String projectsJql = projectsSqlWithoutTimeConstraint();
        String issueTypeAndStatusJql = issueTypeAndStatusAndLimitInDays(configs);
        if (issueTypeAndStatusJql.isEmpty()) 
            return String.format("(%s)", projectsJql);
        
        issueTypeAndStatusJql += " OR (status in ("+StringUtils.join(jiraProperties.getStatusesDeferredIds(),",")+"))";
        return String.format("(%s) AND (%s)", projectsJql, issueTypeAndStatusJql);
    }
    
    private String addNewProjectsIfPresent(CardRepo cardsRepo, String jql) {
        Set<String> newProjectKeys = projectRepository.getProjects().stream().map(p->p.getProjectKey()).collect(Collectors.toSet());
        Optional<Set<String>> currentProjects = cardsRepo.getCurrentProjects();
        if (!currentProjects.isPresent()) 
            return jql;
        
        newProjectKeys.removeAll(currentProjects.get());
        if (newProjectKeys.size() == 0) 
            return jql;
        
        String newProjects = "'" + newProjectKeys.stream().collect(Collectors.joining("','")) + "'";
        return "(" + jql + ") OR (project in ("+newProjects+"))";
    }

    private String addTimeConstraintIfPresent(Optional<Date> lastRemoteUpdatedDate, String jql) {
        if (!lastRemoteUpdatedDate.isPresent()) return jql;
            
        ZonedDateTime timeInJiraTz = ZonedDateTime.ofInstant(lastRemoteUpdatedDate.get().toInstant(), metadataService.getJiraTimeZone());
        String withTimeConstraint = "("+jql+") AND updated >= '" +ofPattern("yyyy-MM-dd HH:mm").format(timeInJiraTz)+"'";
        
        return withTimeConstraint;
    }

    private String issueTypeAndStatusAndLimitInDays(List<IssuesConfiguration> configs) {
        List<IssuesConfiguration> configsWithRangeDate = configs.stream().filter(c -> !(c.getLimitInDays() == null)).collect(Collectors.toList());
        List<IssuesConfiguration> configsWithoutRangeDate = configs.stream().filter(c -> c.getLimitInDays() == null).collect(Collectors.toList());

        List<String> configStatusAndType = configsWithoutRangeDate.stream().map(x -> String.format("(status=%d AND type=%d)", x.getStatus(), x.getIssueType())).collect(Collectors.toList());
        List<String> configStatusAndTypeAndLimitInDays = configsWithRangeDate.stream().map(c -> String.format(" OR (type=%d AND status CHANGED TO %d AFTER %s)",
                c.getIssueType(), c.getStatus(), c.getLimitInDays())).collect(Collectors.toList());

        return String.join(" OR ", configStatusAndType) + String.join("", configStatusAndTypeAndLimitInDays);
    }    
}
