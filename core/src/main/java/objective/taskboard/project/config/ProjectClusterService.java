package objective.taskboard.project.config;

import static objective.taskboard.utils.NumberUtils.numberEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterProvider;

@Service
public class ProjectClusterService {

    private final FollowupClusterProvider clusterProvider;
    private final IssueTypeSizesProvider issueTypeSizesProvider;
    private final ProjectClusterItemRepository projectClusterItemRepository;

    @Autowired
    public ProjectClusterService(
            FollowupClusterProvider clusterProvider,
            IssueTypeSizesProvider issueTypeSizesProvider,
            ProjectClusterItemRepository projectClusterItemRepository) {
        this.clusterProvider = clusterProvider;
        this.issueTypeSizesProvider = issueTypeSizesProvider;
        this.projectClusterItemRepository = projectClusterItemRepository;
    }

    public List<ProjectClusterItemDto> getItems(ProjectFilterConfiguration project) {
        FollowupCluster cluster = clusterProvider.getFor(project);

        return issueTypeSizesProvider.get().stream()
                .map(typeSize -> toProjectClusterItemDto(cluster, typeSize.getIssueType(), typeSize.getSize()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateItems(ProjectFilterConfiguration project, List<ProjectClusterItemDto> itemsUpdate) {
        FollowupCluster cluster = clusterProvider.getFor(project);

        itemsUpdate.forEach(itemUpdate -> {
            Optional<FollowUpClusterItem> matchedItemOptional = cluster.getClusterFor(itemUpdate.getIssueType(), itemUpdate.getSizing());
            if (!matchedItemOptional.isPresent()) {
                if (itemUpdate.getEffort() > 0D || itemUpdate.getCycle() > 0D)
                    projectClusterItemRepository.create(project.getProjectKey(), itemUpdate);
                return;
            }

            FollowUpClusterItem matchedItem = matchedItemOptional.get();
            if (matchedItem.isFromBaseCluster()) {
                boolean equalEffort = numberEquals(matchedItem.getEffort(), itemUpdate.getEffort());
                boolean equalCycle = numberEquals(matchedItem.getCycle(), itemUpdate.getCycle());
                if (!equalEffort || !equalCycle)
                    projectClusterItemRepository.create(project.getProjectKey(), itemUpdate);
                return;
            }
            projectClusterItemRepository.update(matchedItem.getEntityId(), itemUpdate);
        });
    }

    private ProjectClusterItemDto toProjectClusterItemDto(FollowupCluster cluster, String issueType, String size) {
        Optional<FollowUpClusterItem> matchedItem = cluster.getClusterFor(issueType, size);
        if (matchedItem.isPresent())
            return new ProjectClusterItemDto(matchedItem.get());
        return new ProjectClusterItemDto(issueType, size, 0D, 0D, false);
    }

}
