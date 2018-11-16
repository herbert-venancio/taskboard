package objective.taskboard.project.config;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import objective.taskboard.followup.cluster.SizingClusterItem;

class ProjectClusterItemRepositoryMock implements ProjectClusterItemRepository {
    private final Map<Long, SizingClusterItem> data = new HashMap<>();
    private long id = 1;

    @Override
    public void create(String projectKey, ProjectClusterItemDto itemUpdate) {
        SizingClusterItem newItem = new SizingClusterItem(
                itemUpdate.getIssueType(),
                "notused",
                itemUpdate.getSizing(),
                itemUpdate.getEffort(),
                itemUpdate.getCycle(),
                projectKey,
                null);
        newItem.setId(id++);
        data.put(newItem.getId(), newItem);
    }

    @Override
    public void update(Long id, ProjectClusterItemDto itemUpdate) {
        SizingClusterItem item = data.get(id);
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
                item.getProjectKey().orElse("null"),
                item.getBaseCluster().isPresent() ? item.getBaseCluster().get().getId().toString() : "null");
    }

}
