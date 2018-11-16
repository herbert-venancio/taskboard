package objective.taskboard.followup.cluster;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
public class FollowupClusterProvider {

    private ProjectFilterConfigurationCachedRepository projectRepository;
    private SizingClusterRepository clusterRepository;
    private SizingClusterItemRepository clusterItemRepository;
    
    @Autowired
    public FollowupClusterProvider(
            ProjectFilterConfigurationCachedRepository projectRepository,
            SizingClusterRepository clusterRepository,
            SizingClusterItemRepository clusterItemRepository
            ) {
        this.projectRepository = projectRepository;
        this.clusterRepository = clusterRepository;
        this.clusterItemRepository = clusterItemRepository;
    }

    public FollowupCluster getForProject(String projectKey) {
        Optional<ProjectFilterConfiguration> project = projectRepository.getProjectByKey(projectKey);
        return project.map(this::getFor)
                .orElse(new EmptyFollowupCluster());
    }

    public FollowupCluster getFor(final ProjectFilterConfiguration project) {
        Optional<SizingCluster> cluster = clusterRepository.findById(project.getBaseClusterId());
        final List<SizingClusterItem> sizingClusterItems = clusterItemRepository.findByProjectKeyOrBaseCluster(project.getProjectKey(), cluster);
            
        if(sizingClusterItems.isEmpty())
            return new EmptyFollowupCluster();

        List<FollowUpClusterItem> followUpItems = toFollowUpClusterItems(project, sizingClusterItems);
        
        return new FollowupClusterImpl(followUpItems);
    }

    private List<FollowUpClusterItem> toFollowUpClusterItems(final ProjectFilterConfiguration project,
            final List<SizingClusterItem> allItems) {
        
        Map<String, FollowUpClusterItem> clusterItemsMap = new HashMap<>();
        
        allItems.stream()
            .filter(i-> i.getBaseCluster().isPresent())
            .sorted(Comparator.comparing(SizingClusterItem::getId))
            .forEach(i ->  clusterItemsMap.put(generateMapKey(i),
                                               convertToFollowUpClusterItem(project, i)));

        allItems.stream()
            .filter(i-> i.getProjectKey().isPresent())
            .sorted(Comparator.comparing(SizingClusterItem::getId))
            .forEach(i -> clusterItemsMap.put(generateMapKey(i),
                                              convertToFollowUpClusterItem(project, i)));

        return clusterItemsMap.values().parallelStream()
                    .sorted(Comparator.comparing(FollowUpClusterItem::getParentTypeName)
                                .thenComparing(FollowUpClusterItem::getSubtaskTypeName)
                                .thenComparing(FollowUpClusterItem::getSizing))
                    .collect(Collectors.toList());
    }

    private String generateMapKey(final SizingClusterItem i) {
        return String.format("%s:%s:%s", i.getParentTypeName(), i.getSubtaskTypeName(), i.getSizing());
    }

    private FollowUpClusterItem convertToFollowUpClusterItem(final ProjectFilterConfiguration project, final SizingClusterItem item) {
        return new FollowUpClusterItem(
                item.getId(),
                project,
                item.getSubtaskTypeName(),
                item.getParentTypeName(),
                item.getSizing(),
                item.getEffort(),
                item.getCycle(),
                item.getBaseCluster().isPresent());
    }
}
