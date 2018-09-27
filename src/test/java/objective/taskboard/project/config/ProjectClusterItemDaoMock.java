package objective.taskboard.project.config;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.project.config.ProjectClusterService.ProjectClusterItem;

public class ProjectClusterItemDaoMock implements ProjectClusterItemDao {
    private final Map<Long, SizingClusterItem> data = new HashMap<>();
    private long id = 0;

    @Override
    public List<SizingClusterItem> findByProjectKey(String projectKey) {
        return data.values().stream()
            .filter(item -> item.getProjectKey().isPresent() && item.getProjectKey().get().equals(projectKey))
            .collect(toList());
    }

    @Override
    public void create(ProjectFilterConfiguration project, ProjectClusterItem itemUpdate) {
        SizingClusterItem newItem = new SizingClusterItem(
                itemUpdate.getIssueType(),
                "notused",
                itemUpdate.getSizing(),
                itemUpdate.getEffort(),
                itemUpdate.getCycle(),
                project.getProjectKey(),
                null);
        newItem.setId(id++);
        data.put(newItem.getId(), newItem);
    }

    @Override
    public void update(SizingClusterItem item, ProjectClusterItem itemUpdate) {
        item.setEffort(itemUpdate.getEffort());
        item.setCycle(itemUpdate.getCycle());
        data.put(item.getId(), item);
    }

    public void assertItems(String... expectedItems) {
        String expected = Stream.of(expectedItems)
                .map(item -> item.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        String actual = data.values().stream()
                .map(item -> toString(item))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }

    private static String toString(SizingClusterItem item) {
        return String.format("%s | %s | %s | %.0f | %.0f | %s | %s",
                item.getSubtaskTypeName(),
                item.getParentTypeName(),
                item.getSizing(),
                item.getEffort(),
                item.getCycle(),
                item.getProjectKey().isPresent() ? item.getProjectKey().get() : "null",
                item.getBaseCluster().isPresent() ? item.getBaseCluster().get().getId().toString() : "null");
    }

}
