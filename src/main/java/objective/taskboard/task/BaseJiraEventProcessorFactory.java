package objective.taskboard.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import objective.taskboard.domain.Filter;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

public abstract class BaseJiraEventProcessorFactory {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private FilterCachedRepository filterCachedRepository;

    protected boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }

    protected boolean belongsToAnyIssueTypeFilter(Long issueTypeId) {
        if(issueTypeId == null)
            return true;

        List<Filter> filters = filterCachedRepository.getCache();
        return filters.stream().anyMatch(f -> issueTypeId.equals(f.getIssueTypeId()));
    }
}
