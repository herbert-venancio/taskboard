package objective.taskboard.project.config;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupClusterImpl;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.project.config.IssueTypeSizesProvider.IssueTypeSize;

public class ProjectClusterServiceTest {

    private static final String PROJ = "PROJ";

    private FollowupClusterProvider clusterProvider = mock(FollowupClusterProvider.class);
    private IssueTypeSizesProvider issueTypeSizesProvider = mock(IssueTypeSizesProvider.class);
    private ProjectClusterItemRepositoryMock projectClusterItemRepositoryMock = new ProjectClusterItemRepositoryMock();
    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);

    private ProjectClusterService subject = new ProjectClusterService(clusterProvider, issueTypeSizesProvider, projectClusterItemRepositoryMock);
    
    @Before
    public void setUp() {
        when(project.getProjectKey()).thenReturn(PROJ);
    }

    @Test
    public void whenGetItems_thenShouldReturnAllItems() {
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
            followUpClusterItem(1L, "Alpha Test", "XS", 1D, 1D, true),
            followUpClusterItem(2L, "Alpha Test", "S", 2D, 2D, true),
            followUpClusterItem(3L, "Alpha Test", "M", 3D, 3D, true),
            followUpClusterItem(4L, "Alpha Test", "L", 4D, 4D, false),
            followUpClusterItem(5L, "Alpha Test", "XL", 5D, 5D, false),
            followUpClusterItem(6L, "Backend Development", "XS", 1D, 1D, true),
            followUpClusterItem(7L, "Backend Development", "S", 2D, 2D, true),
            followUpClusterItem(8L, "Backend Development", "M", 3D, 3D, true)
        );

        List<ProjectClusterItemDto> projectClusterItemsDto = subject.getItems(project);

        assertItems(projectClusterItemsDto,
                "Alpha Test | XS | 1 | 1 | true",
                "Alpha Test | S | 2 | 2 | true",
                "Alpha Test | M | 3 | 3 | true",
                "Alpha Test | L | 4 | 4 | false",
                "Alpha Test | XL | 5 | 5 | false",
                "Backend Development | XS | 1 | 1 | true",
                "Backend Development | S | 2 | 2 | true",
                "Backend Development | M | 3 | 3 | true",
                "Backend Development | L | 0 | 0 | false",
                "Backend Development | XL | 0 | 0 | false",
                "Feature Planning | XS | 0 | 0 | false",
                "Feature Planning | S | 0 | 0 | false",
                "Feature Planning | M | 0 | 0 | false",
                "Feature Planning | L | 0 | 0 | false",
                "Feature Planning | XL | 0 | 0 | false");
    }

    @Test
    public void givenNoItems_whenUpdateItems_thenShouldCreateOnlyItemsWithEffortOrCycleGreaterThanZero() {
        followUpCluster();

        projectClusterItemRepositoryMock.assertItems();

        List<ProjectClusterItemDtoBuilder> builders = new ArrayList<>();
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("XS").effort(2D).cycle(2D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("S").effort(3D).cycle(3D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("M").effort(4D).cycle(4D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("L").effort(5D).cycle(5D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("XL").effort(6D).cycle(6D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("XS").effort(2D).cycle(2D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("S").effort(3D).cycle(3D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("M").effort(0D).cycle(4D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("L").effort(5D).cycle(0D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("XL").effort(0D).cycle(0D));
        List<ProjectClusterItemDto> itemsDtoUpdate = builders.stream().map(b -> b.build()).collect(Collectors.toList());

        subject.updateItems(project, itemsDtoUpdate);

        projectClusterItemRepositoryMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null",
                "Backend Development | notused | XS | 2 | 2 | PROJ | null",
                "Backend Development | notused | S | 3 | 3 | PROJ | null",
                "Backend Development | notused | M | 0 | 4 | PROJ | null",
                "Backend Development | notused | L | 5 | 0 | PROJ | null");
    }

    @Test
    public void givenProjectClusterItems_whenUpdateItems_thenShouldUpdateThem() {
        followUpCluster(
            followUpClusterItem(1L, "Alpha Test", "XS", 1D, 1D, false),
            followUpClusterItem(2L, "Alpha Test", "S", 2D, 2D, false),
            followUpClusterItem(3L, "Alpha Test", "M", 3D, 3D, false),
            followUpClusterItem(4L, "Alpha Test", "L", 4D, 4D, false),
            followUpClusterItem(5L, "Alpha Test", "XL", 5D, 5D, false)
        );

        projectCluster(
            projectClusterItem().issueType("Alpha Test").sizing("XS").effort(1D).cycle(1D),
            projectClusterItem().issueType("Alpha Test").sizing("S").effort(2D).cycle(2D),
            projectClusterItem().issueType("Alpha Test").sizing("M").effort(3D).cycle(3D),
            projectClusterItem().issueType("Alpha Test").sizing("L").effort(4D).cycle(4D),
            projectClusterItem().issueType("Alpha Test").sizing("XL").effort(5D).cycle(5D)
        );

        List<ProjectClusterItemDto> itemsDtoUpdate = new ArrayList<>();
        itemsDtoUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("XS").effort(2D).cycle(2D).build());
        itemsDtoUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("S").effort(3D).cycle(3D).build());
        itemsDtoUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("M").effort(4D).cycle(4D).build());
        itemsDtoUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("L").effort(5D).cycle(5D).build());
        itemsDtoUpdate.add(projectClusterItem().issueType("Alpha Test").sizing("XL").effort(6D).cycle(6D).build());

        subject.updateItems(project, itemsDtoUpdate);

        projectClusterItemRepositoryMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null");
    }

    @Test
    public void givenProjectClusterItemsFromBaseCluster_whenUpdateItems_thenShouldCreateOnlyWhenChanged() {
        followUpCluster(
            followUpClusterItem(1L, "Alpha Test", "XS", 1D, 1D, false),
            followUpClusterItem(2L, "Alpha Test", "S", 2D, 2D, false),
            followUpClusterItem(3L, "Alpha Test", "M", 3D, 3D, false),
            followUpClusterItem(4L, "Alpha Test", "L", 4D, 4D, false),
            followUpClusterItem(5L, "Alpha Test", "XL", 5D, 5D, false),
            followUpClusterItem(6L, "Backend Development", "XS", 1D, 1D, true),
            followUpClusterItem(7L, "Backend Development", "S", 2D, 3D, true),
            followUpClusterItem(8L, "Backend Development", "M", 4D, 3D, true),
            followUpClusterItem(9L, "Backend Development", "L", 5D, 5D, true),
            followUpClusterItem(10L, "Backend Development", "XL", 6D, 6D, true)
        );

        projectCluster(
            projectClusterItem().issueType("Alpha Test").sizing("XS").effort(1D).cycle(1D),
            projectClusterItem().issueType("Alpha Test").sizing("S").effort(2D).cycle(2D),
            projectClusterItem().issueType("Alpha Test").sizing("M").effort(3D).cycle(3D),
            projectClusterItem().issueType("Alpha Test").sizing("L").effort(4D).cycle(4D),
            projectClusterItem().issueType("Alpha Test").sizing("XL").effort(5D).cycle(5D)
        );

        List<ProjectClusterItemDtoBuilder> builders = new ArrayList<>();
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("XS").effort(2D).cycle(2D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("S").effort(3D).cycle(3D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("M").effort(4D).cycle(4D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("L").effort(5D).cycle(5D));
        builders.add(projectClusterItem().issueType("Alpha Test").sizing("XL").effort(6D).cycle(6D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("XS").effort(2D).cycle(2D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("S").effort(3D).cycle(3D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("M").effort(4D).cycle(4D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("L").effort(5D).cycle(5D));
        builders.add(projectClusterItem().issueType("Backend Development").sizing("XL").effort(7D).cycle(7D));
        List<ProjectClusterItemDto> itemsDtoUpdate = builders.stream().map(b -> b.build()).collect(Collectors.toList());

        subject.updateItems(project, itemsDtoUpdate);

        projectClusterItemRepositoryMock.assertItems(
                "Alpha Test | notused | XS | 2 | 2 | PROJ | null",
                "Alpha Test | notused | S | 3 | 3 | PROJ | null",
                "Alpha Test | notused | M | 4 | 4 | PROJ | null",
                "Alpha Test | notused | L | 5 | 5 | PROJ | null",
                "Alpha Test | notused | XL | 6 | 6 | PROJ | null",
                "Backend Development | notused | XS | 2 | 2 | PROJ | null",
                "Backend Development | notused | S | 3 | 3 | PROJ | null",
                "Backend Development | notused | M | 4 | 4 | PROJ | null",
                "Backend Development | notused | XL | 7 | 7 | PROJ | null");
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

    private FollowUpClusterItem followUpClusterItem(Long entityId, String issueType, String size, Double effort, Double cycle, Boolean isFromBaseCluster) {
        return new FollowUpClusterItem(entityId, project, issueType, "notused", size, effort, cycle, isFromBaseCluster);
    }

    private void projectCluster(ProjectClusterItemDtoBuilder... builders) {
        asList(builders).stream()
            .forEach(builder -> projectClusterItemRepositoryMock.create(PROJ, builder.build()));
    }

    private ProjectClusterItemDtoBuilder projectClusterItem() {
        return new ProjectClusterItemDtoBuilder();
    }

    private class ProjectClusterItemDtoBuilder {
        private String issueType;
        private String sizing;
        private Double effort;
        private Double cycle;

        private ProjectClusterItemDtoBuilder issueType(String issueType) {
            this.issueType = issueType;
            return this;
        }

        private ProjectClusterItemDtoBuilder cycle(Double cycle) {
            this.cycle = cycle;
            return this;
        }

        private ProjectClusterItemDtoBuilder effort(Double effort) {
            this.effort = effort;
            return this;
        }

        private ProjectClusterItemDtoBuilder sizing(String sizing) {
            this.sizing = sizing;
            return this;
        }

        private ProjectClusterItemDto build() {
            return new ProjectClusterItemDto(issueType, sizing, effort, cycle, false);
        }
    }

    private void assertItems(List<ProjectClusterItemDto> clusterItems, String... expectedItems) {
        String expected = Stream.of(expectedItems)
                .map(item -> item.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        String actual = clusterItems.stream()
                .map(item -> toString(item))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }

    private String toString(ProjectClusterItemDto item) {
        return String.format("%s | %s | %.0f | %.0f | %b",
                item.getIssueType(),
                item.getSizing(),
                item.getEffort(),
                item.getCycle(),
                item.isFromBaseCluster());
    }

}
