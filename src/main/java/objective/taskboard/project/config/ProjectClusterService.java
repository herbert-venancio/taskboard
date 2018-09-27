package objective.taskboard.project.config;

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
import objective.taskboard.followup.cluster.SizingClusterItem;

@Service
public class ProjectClusterService {

    private final FollowupClusterProvider clusterProvider;
    private final IssueTypeSizesProvider issueTypeSizesProvider;
    private final ProjectClusterItemDao projectClusterItemDao;

    @Autowired
    public ProjectClusterService(
            FollowupClusterProvider clusterProvider,
            IssueTypeSizesProvider issueTypeSizesProvider,
            ProjectClusterItemDao projectClusterItemDao) {
        this.clusterProvider = clusterProvider;
        this.issueTypeSizesProvider = issueTypeSizesProvider;
        this.projectClusterItemDao = projectClusterItemDao;
    }

    public List<ProjectClusterItem> getItems(ProjectFilterConfiguration project) {
        FollowupCluster cluster = clusterProvider.getFor(project);

        return issueTypeSizesProvider.get().stream()
                .map(typeSize -> toProjectClusterItem(cluster, typeSize.getIssueType(), typeSize.getSize()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateItems(ProjectFilterConfiguration project, List<ProjectClusterItem> itemsUpdate) {
        List<SizingClusterItem> clusterItems = projectClusterItemDao.findByProjectKey(project.getProjectKey());

        for (ProjectClusterItem itemUpdate : itemsUpdate) {
            Optional<SizingClusterItem> matchedItem = findCorrespondingClusterItem(clusterItems, itemUpdate);

            if (matchedItem.isPresent()) {
                projectClusterItemDao.update(matchedItem.get(), itemUpdate);
                continue;
            }
            projectClusterItemDao.create(project, itemUpdate);
        }
    }

    private ProjectClusterItem toProjectClusterItem(FollowupCluster cluster, String issueType, String size) {
        Optional<FollowUpClusterItem> matchedItem = cluster.getClusterFor(issueType, size);
        if (matchedItem.isPresent())
            return new ProjectClusterItem(matchedItem.get());
        return new ProjectClusterItem(issueType, size, 0D, 0D);
    }

    private Optional<SizingClusterItem> findCorrespondingClusterItem(List<SizingClusterItem> clusterItems, ProjectClusterItem itemUpdate) {
        if (clusterItems.isEmpty())
            return Optional.empty();

        return clusterItems.stream()
                .filter(item -> matchesClusterItem(item, itemUpdate.issueType, itemUpdate.sizing))
                .findFirst();
    }

    private boolean matchesClusterItem(SizingClusterItem item, String issueType, String sizing) {
        return item.getSubtaskTypeName().equals(issueType) && item.getSizing().equals(sizing);
    }

    public static class ProjectClusterItem {
        private final String issueType;
        private final String sizing;
        private final Double effort;
        private final Double cycle;

        public ProjectClusterItem(String issueType, String sizing, Double effort, Double cycle) {
            this.issueType = issueType;
            this.sizing = sizing;
            this.effort = effort;
            this.cycle = cycle;
        }

        public ProjectClusterItem(FollowUpClusterItem clusterItem) {
            this.issueType = clusterItem.getSubtaskTypeName();
            this.sizing = clusterItem.getSizing();
            this.effort = clusterItem.getEffort();
            this.cycle = clusterItem.getCycle();
        }

        public String getIssueType() {
            return issueType;
        }

        public String getSizing() {
            return sizing;
        }

        public Double getEffort() {
            return effort;
        }

        public Double getCycle() {
            return cycle;
        }
    }

}
