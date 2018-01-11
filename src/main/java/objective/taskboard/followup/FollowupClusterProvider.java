package objective.taskboard.followup;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowUpClusterItemRepository;
import objective.taskboard.followup.data.Template;

@Component
public class FollowupClusterProvider {
    @Autowired
    private FollowUpClusterItemRepository clusterItemRepository;
    
    @Autowired
    private TemplateService templateService;
    
    public FollowupCluster getFor(Template followUpConfiguration) {
        List<FollowUpClusterItem> clusterItems = clusterItemRepository.findByFollowUpConfiguration(followUpConfiguration);
        return new FollowupClusterImpl(clusterItems);
    }
    
    public Optional<FollowupCluster> getForProject(String projectKey) {
        Template followUpConfiguration = templateService.findATemplateOnlyMatchedWithThisProjectKey(Arrays.asList(projectKey));
        if (followUpConfiguration == null)
            return Optional.empty();
        
        return Optional.of(getFor(followUpConfiguration));
    }

    public Optional<FollowupCluster> getFor(ProjectFilterConfiguration project) {
        return getForProject(project.getProjectKey());
    }
}
