package objective.taskboard.project.config;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupClusterImpl;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.project.config.IssueTypeSizesProvider.IssueTypeSize;
import objective.taskboard.project.config.ProjectClusterService.ProjectClusterItem;

public class ProjectClusterServiceTest {

    private static final String PROJ = "PROJ";

    private FollowupClusterProvider clusterProvider = mock(FollowupClusterProvider.class);
    private IssueTypeSizesProvider issueTypeSizesProvider = mock(IssueTypeSizesProvider.class);
    private ProjectClusterItemDaoMock projectClusterItemDaoMock = new ProjectClusterItemDaoMock();
    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);

    private ProjectClusterService subject = new ProjectClusterService(clusterProvider, issueTypeSizesProvider, projectClusterItemDaoMock);

    @Test
    public void whenGetProjectCluster_thenShouldReturnClusterSuccessfully() {
        issueTypeSizes(
            issueTypeSize("Alpha Test", "XS"),
            issueTypeSize("Alpha Test", "S"),
            issueTypeSize("Alpha Test", "M"),
            issueTypeSize("Alpha Test", "L"),
            issueTypeSize("Alpha Test", "XL"),
            issueTypeSize("Backend Development", "XS"),
            issueTypeSize("Backend Development", "S"),
            issueTypeSize("Backend Development", "M"),
            issueTypeSize("Backend Development", "L"),
            issueTypeSize("Backend Development", "XL"),
            issueTypeSize("Feature Planning", "XS"),
            issueTypeSize("Feature Planning", "S"),
            issueTypeSize("Feature Planning", "M"),
            issueTypeSize("Feature Planning", "L"),
            issueTypeSize("Feature Planning", "XL")
        );

        followUpCluster(
            followUpClusterItem("Alpha Test", "XS", 1D, 1D),
            followUpClusterItem("Alpha Test", "S", 2D, 2D),
            followUpClusterItem("Alpha Test", "M", 3D, 3D),
            followUpClusterItem("Alpha Test", "L", 4D, 4D),
            followUpClusterItem("Alpha Test", "XL", 5D, 5D),
            followUpClusterItem("Backend Development", "XS", 1D, 1D),
            followUpClusterItem("Backend Development", "S", 2D, 2D),
            followUpClusterItem("Backend Development", "M", 3D, 3D)
        );

        List<ProjectClusterItem> projectClusterItems = subject.getItems(project);

        assertEquals(15, projectClusterItems.size());
        assertItems(projectClusterItems,
                "Alpha Test | XS | 1 | 1",
                "Alpha Test | S | 2 | 2",
                "Alpha Test | M | 3 | 3",
                "Alpha Test | L | 4 | 4",
                "Alpha Test | XL | 5 | 5",
                "Backend Development | XS | 1 | 1",
                "Backend Development | S | 2 | 2",
                "Backend Development | M | 3 | 3",
                "Backend Development | L | 0 | 0",
                "Backend Development | XL | 0 | 0",
                "Feature Planning | XS | 0 | 0",
                "Feature Planning | S | 0 | 0",
                "Feature Planning | M | 0 | 0",
                "Feature Planning | L | 0 | 0",
                "Feature Planning | XL | 0 | 0");
    }

    @Test
    public void giveNoProjectCluster_whenUpdateProjectCluster_thenShouldCreate() {
        when(project.getProjectKey()).thenReturn(PROJ);

        projectClusterItemDaoMock.assertItems();

        subject.updateItems(project, requestForUpdate());

        projectClusterItemDaoMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null",
                "Backend Development | notused | XS | 2 | 2 | PROJ | null",
                "Backend Development | notused | S | 3 | 3 | PROJ | null",
                "Backend Development | notused | M | 4 | 4 | PROJ | null",
                "Backend Development | notused | L | 5 | 5 | PROJ | null",
                "Backend Development | notused | XL | 6 | 6 | PROJ | null");
    }

    @Test
    public void givenProjectCluster_whenUpdateProjectCluster_thenShouldUpdate() {
        when(project.getProjectKey()).thenReturn(PROJ);

        projectCluster(
            projectClusterItem().issueType("Alpha Test").sizing("XS").effort(1D).cycle(1D),
            projectClusterItem().issueType("Alpha Test").sizing("S").effort(2D).cycle(2D),
            projectClusterItem().issueType("Alpha Test").sizing("M").effort(3D).cycle(3D),
            projectClusterItem().issueType("Alpha Test").sizing("L").effort(4D).cycle(4D),
            projectClusterItem().issueType("Alpha Test").sizing("XL").effort(5D).cycle(5D)
        );

        List<ProjectClusterItem> itemsUpdate = new ArrayList<>();
        itemsUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("XS").effort(2D).cycle(2D).build());
        itemsUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("S").effort(3D).cycle(3D).build());
        itemsUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("M").effort(4D).cycle(4D).build());
        itemsUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("L").effort(5D).cycle(5D).build());
        itemsUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("XL").effort(6D).cycle(6D).build());

        subject.updateItems(project, itemsUpdate);

        projectClusterItemDaoMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null");
    }

    @Test
    public void givenNoMatchedProjectCluster_whenUpdateProjectCluster_thenShouldCreate() {
        when(project.getProjectKey()).thenReturn(PROJ);

        projectCluster(
            projectClusterItem().issueType("Alpha Test").sizing("XS").effort(1D).cycle(1D),
            projectClusterItem().issueType("Alpha Test").sizing("S").effort(2D).cycle(2D),
            projectClusterItem().issueType("Alpha Test").sizing("M").effort(3D).cycle(3D),
            projectClusterItem().issueType("Alpha Test").sizing("L").effort(4D).cycle(4D),
            projectClusterItem().issueType("Alpha Test").sizing("XL").effort(5D).cycle(5D)
        );

        subject.updateItems(project, requestForUpdate());

        projectClusterItemDaoMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null",
                "Backend Development | notused | XS | 2 | 2 | PROJ | null",
                "Backend Development | notused | S | 3 | 3 | PROJ | null",
                "Backend Development | notused | M | 4 | 4 | PROJ | null",
                "Backend Development | notused | L | 5 | 5 | PROJ | null",
                "Backend Development | notused | XL | 6 | 6 | PROJ | null");
    }

    private void issueTypeSizes(IssueTypeSize... issueTypeSizes) {
        when(issueTypeSizesProvider.get()).thenReturn(asList(issueTypeSizes));
    }

    private IssueTypeSize issueTypeSize(String issueType, String size) {
        return new IssueTypeSize(issueType, size);
    }

    private void followUpCluster(FollowUpClusterItem... followUpClusterItems) {
        when(clusterProvider.getFor(project)).thenReturn(new FollowupClusterImpl(asList(followUpClusterItems)));
    }

    private FollowUpClusterItem followUpClusterItem(String issueType, String size, Double effort, Double cycle) {
        return new FollowUpClusterItem(project, issueType, "notused", size, effort, cycle);
    }

    private void projectCluster(ProjectClusterItemBuilder... builders) {
        asList(builders).stream()
            .forEach(builder -> projectClusterItemDaoMock.create(project, builder.build()));
    }

    private ProjectClusterItemBuilder projectClusterItem() {
        return new ProjectClusterItemBuilder();
    }

    private class ProjectClusterItemBuilder {
        private String issueType;
        private String sizing;
        private Double effort;
        private Double cycle;

        private ProjectClusterItemBuilder issueType(String issueType) {
            this.issueType = issueType;
            return this;
        }

        private ProjectClusterItemBuilder cycle(Double cycle) {
            this.cycle = cycle;
            return this;
        }

        private ProjectClusterItemBuilder effort(Double effort) {
            this.effort = effort;
            return this;
        }

        private ProjectClusterItemBuilder sizing(String sizing) {
            this.sizing = sizing;
            return this;
        }

        private ProjectClusterItem build() {
            return new ProjectClusterItem(issueType, sizing, effort, cycle);
        }
    }

    private void assertItems(List<ProjectClusterItem> clusterItems, String... expectedItems) {
        String expected = Stream.of(expectedItems)
                .map(item -> item.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        String actual = clusterItems.stream()
                .map(item -> toString(item))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }

    private String toString(ProjectClusterItem item) {
        return String.format("%s | %s | %.0f | %.0f",
                item.getIssueType(),
                item.getSizing(),
                item.getEffort(),
                item.getCycle());
    }

    private List<ProjectClusterItem> requestForUpdate() {
        List<ProjectClusterItem> items = new ArrayList<>();
        items.add(projectClusterItem().issueType("Alpha Test").sizing("XS").effort(2D).cycle(2D).build());
        items.add(projectClusterItem().issueType("Alpha Test").sizing("S").effort(3D).cycle(3D).build());
        items.add(projectClusterItem().issueType("Alpha Test").sizing("M").effort(4D).cycle(4D).build());
        items.add(projectClusterItem().issueType("Alpha Test").sizing("L").effort(5D).cycle(5D).build());
        items.add(projectClusterItem().issueType("Alpha Test").sizing("XL").effort(6D).cycle(6D).build());
        items.add(projectClusterItem().issueType("Backend Development").sizing("XS").effort(2D).cycle(2D).build());
        items.add(projectClusterItem().issueType("Backend Development").sizing("S").effort(3D).cycle(3D).build());
        items.add(projectClusterItem().issueType("Backend Development").sizing("M").effort(4D).cycle(4D).build());
        items.add(projectClusterItem().issueType("Backend Development").sizing("L").effort(5D).cycle(5D).build());
        items.add(projectClusterItem().issueType("Backend Development").sizing("XL").effort(6D).cycle(6D).build());
        return items;
    }

}
