package objective.taskboard.cluster.base;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.cluster.base.BaseClusterService;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.project.config.IssueTypeSizesProvider;
import objective.taskboard.project.config.IssueTypeSizesProvider.IssueTypeSize;

@RunWith(MockitoJUnitRunner.class)
public class BaseClusterServiceTest {

    private IssueTypeSizesProvider issueTypeSizesProvider = mock(IssueTypeSizesProvider.class);
    private ClusterRepositoryMock repository = new ClusterRepositoryMock();
    private BaseClusterService service = new BaseClusterService(repository, issueTypeSizesProvider);

    @Before
    public void setUp() {
        repository.reset();
    }

    @Test
    public void findABaseCluster_notFound() {
        Optional<BaseClusterDto> baseClusterOptional = service.findById(999L);
        Assert.assertFalse(baseClusterOptional.isPresent());
    }

    @Test
    public void findABaseCluster_withoutItems() {
        mockBaseCluster(
            cluster("Base Cluster")
        );
        Optional<BaseClusterDto> baseClusterOptional = service.findById(1L);
        assertTrue(baseClusterOptional.isPresent());

        BaseClusterDto baseCluster = baseClusterOptional.get();
        assertBaseCluster(baseCluster, 1L, "Base Cluster");
    }

    @Test
    public void findABaseCluster_foundWithItems() {
        mockBaseCluster(
            cluster("Base Cluster",
                item("BALLPARK - Dev", "XS", 1.0, 2.0),
                item("BALLPARK - Alpha", "XS", 2.0, 3.0),
                item("BALLPARK - Alpha", "XL", 5.0, 8.0)
            )
        );
        issueTypeSizes(
            issueTypeSize("BALLPARK - Dev", "XS"),
            issueTypeSize("BALLPARK - Dev", "S"),
            issueTypeSize("BALLPARK - Dev", "M"),
            issueTypeSize("BALLPARK - Dev", "L"),
            issueTypeSize("BALLPARK - Dev", "XL"),
            issueTypeSize("BALLPARK - Alpha", "XS"),
            issueTypeSize("BALLPARK - Alpha", "S"),
            issueTypeSize("BALLPARK - Alpha", "M"),
            issueTypeSize("BALLPARK - Alpha", "L"),
            issueTypeSize("BALLPARK - Alpha", "XL")
        );
        Optional<BaseClusterDto> cluster = service.findById(1L);
        assertTrue(cluster.isPresent());

        assertBaseCluster(cluster.get(), 1L, "Base Cluster",
            "BALLPARK - Alpha | XS | 2.0 | 3.0",
            "BALLPARK - Alpha | S | 0.0 | 0.0",
            "BALLPARK - Alpha | M | 0.0 | 0.0",
            "BALLPARK - Alpha | L | 0.0 | 0.0",
            "BALLPARK - Alpha | XL | 5.0 | 8.0",
            "BALLPARK - Dev | XS | 1.0 | 2.0",
            "BALLPARK - Dev | S | 0.0 | 0.0",
            "BALLPARK - Dev | M | 0.0 | 0.0",
            "BALLPARK - Dev | L | 0.0 | 0.0",
            "BALLPARK - Dev | XL | 0.0 | 0.0"
        );
    }

    @Test
    public void findAllBaseClusters() {
        mockBaseClusters(
            cluster("Base Cluster",
                item("BALLPARK - Dev", "XS", 1.0, 2.0),
                item("BALLPARK - Alpha", "XS", 2.0, 3.0),
                item("BALLPARK - Alpha", "XL", 5.0, 8.0)
            ),
            cluster("GS Cluster Default",
                item("Technical Analysis", "XL", 10.0, 10.0),
                item("Development", "S", 1.0, 2.0),
                item("Development", "M", 3.0, 5.0),
                item("Development", "XS", 5.0, 10.0),
                item("Support", "S", 1.0, 1.0),
                item("Support", "M", 2.0, 2.0)
            )
        );
        issueTypeSizes(
            issueTypeSize("BALLPARK - Dev", "XS"),
            issueTypeSize("BALLPARK - Dev", "S"),
            issueTypeSize("BALLPARK - Dev", "M"),
            issueTypeSize("BALLPARK - Dev", "L"),
            issueTypeSize("BALLPARK - Dev", "XL"),
            issueTypeSize("BALLPARK - Alpha", "XS"),
            issueTypeSize("BALLPARK - Alpha", "S"),
            issueTypeSize("BALLPARK - Alpha", "M"),
            issueTypeSize("BALLPARK - Alpha", "L"),
            issueTypeSize("BALLPARK - Alpha", "XL"),
            issueTypeSize("Support", "XS"),
            issueTypeSize("Support", "S"),
            issueTypeSize("Support", "M"),
            issueTypeSize("Support", "L"),
            issueTypeSize("Support", "XL")
        );
        List<BaseClusterDto> clusters = service.findAll();

        assertEquals(2L, clusters.size());

        assertBaseCluster(clusters.get(0), 1L, "Base Cluster",
            "BALLPARK - Alpha | XS | 2.0 | 3.0",
            "BALLPARK - Alpha | S | 0.0 | 0.0",
            "BALLPARK - Alpha | M | 0.0 | 0.0",
            "BALLPARK - Alpha | L | 0.0 | 0.0",
            "BALLPARK - Alpha | XL | 5.0 | 8.0",
            "BALLPARK - Dev | XS | 1.0 | 2.0",
            "BALLPARK - Dev | S | 0.0 | 0.0",
            "BALLPARK - Dev | M | 0.0 | 0.0",
            "BALLPARK - Dev | L | 0.0 | 0.0",
            "BALLPARK - Dev | XL | 0.0 | 0.0",
            "Support | XS | 0.0 | 0.0",
            "Support | S | 0.0 | 0.0",
            "Support | M | 0.0 | 0.0",
            "Support | L | 0.0 | 0.0",
            "Support | XL | 0.0 | 0.0"
        );

        assertBaseCluster(clusters.get(1), 2L, "GS Cluster Default",
            "BALLPARK - Alpha | XS | 0.0 | 0.0",
            "BALLPARK - Alpha | S | 0.0 | 0.0",
            "BALLPARK - Alpha | M | 0.0 | 0.0",
            "BALLPARK - Alpha | L | 0.0 | 0.0",
            "BALLPARK - Alpha | XL | 0.0 | 0.0",
            "BALLPARK - Dev | XS | 0.0 | 0.0",
            "BALLPARK - Dev | S | 0.0 | 0.0",
            "BALLPARK - Dev | M | 0.0 | 0.0",
            "BALLPARK - Dev | L | 0.0 | 0.0",
            "BALLPARK - Dev | XL | 0.0 | 0.0",
            "Support | XS | 0.0 | 0.0",
            "Support | S | 1.0 | 1.0",
            "Support | M | 2.0 | 2.0",
            "Support | L | 0.0 | 0.0",
            "Support | XL | 0.0 | 0.0"
        );
    }

    @Test
    public void createACluster() {
        assertTrue(repository.findAll().isEmpty());

        BaseClusterDto cluster =
            clusterDto(null, "Base Cluster",
                itemDto("BALLPARK - Dev", "XS", 1.0, 2.0),
                itemDto("BALLPARK - Alpha", "XS", 2.0, 3.0),
                itemDto("BALLPARK - Alpha", "XL", 5.0, 8.0)
            );

        service.create(cluster);
        List<SizingCluster> allClusters = repository.findAll();

        assertEquals(1L, allClusters.size());

        assertSizingCluster(allClusters.get(0), 1L, "Base Cluster",
            "BALLPARK - Dev | notused | XS | 1.0 | 2.0 | null",
            "BALLPARK - Alpha | notused | XS | 2.0 | 3.0 | null",
            "BALLPARK - Alpha | notused | XL | 5.0 | 8.0 | null"
        );
    }

    @Test
    public void updateACluster() {
        mockBaseCluster(
            cluster("Base Cluster",
                item("BALLPARK - Dev", "XS", 1.0, 2.0),
                item("BALLPARK - Dev", "S", 0.0, 0.0),
                item("BALLPARK - Dev", "M", 0.0, 0.0),
                item("BALLPARK - Dev", "L", 0.0, 0.0),
                item("BALLPARK - Dev", "XL", 0.0, 0.0),
                item("BALLPARK - Alpha", "XS", 2.0, 3.0),
                item("BALLPARK - Alpha", "S", 0.0, 0.0),
                item("BALLPARK - Alpha", "M", 0.0, 0.0),
                item("BALLPARK - Alpha", "L", 0.0, 0.0),
                item("BALLPARK - Alpha", "XL", 5.0, 8.0)
            )
        );
        issueTypeSizes(
            issueTypeSize("BALLPARK - Alpha", "XS"),
            issueTypeSize("BALLPARK - Alpha", "S"),
            issueTypeSize("BALLPARK - Alpha", "M"),
            issueTypeSize("BALLPARK - Alpha", "L"),
            issueTypeSize("BALLPARK - Alpha", "XL"),
            issueTypeSize("Support", "XS"),
            issueTypeSize("Support", "S"),
            issueTypeSize("Support", "M"),
            issueTypeSize("Support", "L"),
            issueTypeSize("Support", "XL")
        );
        BaseClusterDto clusteToUpdate = clusterDto(1L, "Base Cluster updated",
            itemDto("BALLPARK - Alpha", "XS", 2.0, 3.0),
            itemDto("BALLPARK - Alpha", "S", 0.0, 0.0),
            itemDto("BALLPARK - Alpha", "M", 1.0, 0.0),
            itemDto("BALLPARK - Alpha", "L", 0.0, 0.0),
            itemDto("BALLPARK - Alpha", "XL", 5.0, 8.0),
            itemDto("Support", "XS", 2.1, 3.1),
            itemDto("Support", "S", 0.0, 0.0),
            itemDto("Support", "M", 2.0, 2.0),
            itemDto("Support", "L", 0.0, 0.0),
            itemDto("Support", "XL", 0.0, 0.0)
        );
        Optional<BaseClusterDto> clusterUpdated = service.update(1L, clusteToUpdate);

        assertBaseCluster(clusterUpdated.get(), 1L, "Base Cluster updated",
            "BALLPARK - Alpha | XS | 2.0 | 3.0",
            "BALLPARK - Alpha | S | 0.0 | 0.0",
            "BALLPARK - Alpha | M | 1.0 | 0.0",
            "BALLPARK - Alpha | L | 0.0 | 0.0",
            "BALLPARK - Alpha | XL | 5.0 | 8.0",
            "Support | XS | 2.1 | 3.1",
            "Support | S | 0.0 | 0.0",
            "Support | M | 2.0 | 2.0",
            "Support | L | 0.0 | 0.0",
            "Support | XL | 0.0 | 0.0"
        );
        Optional<SizingCluster> clusterPersisted = repository.findById(clusterUpdated.get().getId());

        assertSizingCluster(clusterPersisted.get(), 1L, "Base Cluster updated",
            "BALLPARK - Dev | notused | XS | 1.0 | 2.0 | null",
            "BALLPARK - Dev | notused | S | 0.0 | 0.0 | null",
            "BALLPARK - Dev | notused | M | 0.0 | 0.0 | null",
            "BALLPARK - Dev | notused | L | 0.0 | 0.0 | null",
            "BALLPARK - Dev | notused | XL | 0.0 | 0.0 | null",
            "BALLPARK - Alpha | notused | XS | 2.0 | 3.0 | null",
            "BALLPARK - Alpha | notused | S | 0.0 | 0.0 | null",
            "BALLPARK - Alpha | notused | M | 1.0 | 0.0 | null",
            "BALLPARK - Alpha | notused | L | 0.0 | 0.0 | null",
            "BALLPARK - Alpha | notused | XL | 5.0 | 8.0 | null",
            "Support | notused | XS | 2.1 | 3.1 | null",
            "Support | notused | S | 0.0 | 0.0 | null",
            "Support | notused | M | 2.0 | 2.0 | null",
            "Support | notused | L | 0.0 | 0.0 | null",
            "Support | notused | XL | 0.0 | 0.0 | null"
        );

    }

    @Test
    public void updateACluster_whenClusterIsNotFound() {
        BaseClusterDto clusteToUpdate = clusterDto(1L, "Base Cluster updated",
            itemDto("Support", "XS", 2.1, 3.1),
            itemDto("Support", "S", 0.0, 0.0),
            itemDto("Support", "M", 2.0, 2.0),
            itemDto("Support", "L", 0.0, 0.0),
            itemDto("Support", "XL", 0.0, 0.0)
        );

        Optional<BaseClusterDto> clusterUpdated = service.update(1L, clusteToUpdate);
        assertTrue(!clusterUpdated.isPresent());
    }

    @Test
    public void updateACluster_whenClusterIsFoundWithoutItems() {
        mockBaseCluster(
            cluster("Base Cluster")
        );
        issueTypeSizes(
            issueTypeSize("Support", "XS"),
            issueTypeSize("Support", "S"),
            issueTypeSize("Support", "M"),
            issueTypeSize("Support", "L"),
            issueTypeSize("Support", "XL")
        );
        BaseClusterDto clusteToUpdate = clusterDto(1L, "Base Cluster updated",
            itemDto("Support", "XS", 2.1, 3.1),
            itemDto("Support", "S", 0.0, 0.0),
            itemDto("Support", "M", 2.0, 2.0),
            itemDto("Support", "L", 0.0, 0.0),
            itemDto("Support", "XL", 0.0, 0.0)
        );

        Optional<BaseClusterDto> clusterUpdated = service.update(1L, clusteToUpdate);

        assertBaseCluster(clusterUpdated.get(), 1L, "Base Cluster updated",
            "Support | XS | 2.1 | 3.1",
            "Support | S | 0.0 | 0.0",
            "Support | M | 2.0 | 2.0",
            "Support | L | 0.0 | 0.0",
            "Support | XL | 0.0 | 0.0"
        );
    }

    private void mockBaseCluster(final SizingCluster cluster) {
        repository.save(cluster);
    }

    private void mockBaseClusters(final SizingCluster...clusters) {
        asList(clusters).forEach(this::mockBaseCluster);
    }

    private BaseClusterDto clusterDto(final Long clusterId, final String clusterName, final BaseClusterItemDto...items) {
        return new BaseClusterDto(clusterId, clusterName, asList(items));
    }

    private BaseClusterItemDto itemDto(final String subtaskTypeName, final String sizing, final Double effort, final Double cycle) {
        return new BaseClusterItemDto(subtaskTypeName, sizing, effort, cycle);
    }

    private SizingCluster cluster(final String clusterName, final SizingClusterItem...items) {
        SizingCluster baseCluster = new SizingCluster(clusterName);
        List<SizingClusterItem> itemsList = asList(items);
        itemsList.forEach(i -> i.setBaseCluster(baseCluster));
        baseCluster.setItems(itemsList);
        return baseCluster;
    }

    private SizingClusterItem item(final String subtaskTypeName, final String sizing, final Double effort, final Double cycle) {
        return new SizingClusterItem(subtaskTypeName, "notused", sizing, effort, cycle, null, null);
    }

    private void issueTypeSizes(IssueTypeSize... issueTypeSizes) {
        when(issueTypeSizesProvider.get()).thenReturn(asList(issueTypeSizes));
    }

    private IssueTypeSize issueTypeSize(String issueType, String size) {
        return new IssueTypeSize(issueType, size);
    }

    private void assertSizingCluster(final SizingCluster actualCluster, final Long expectedClusterId, final String expectedClusterName, final String... expectedItems) {
        assertEquals(expectedClusterId, actualCluster.getId());
        assertEquals(expectedClusterName, actualCluster.getName());
        assertSizingClusterItems(asList(expectedItems), actualCluster.getItems());
    }

    private void assertSizingClusterItems(final List<String> expetedItems, final List<SizingClusterItem> actualItems) {
        String expected = StringUtils.join(expetedItems, "\n");
        String current = actualItems.stream()
            .map(i -> i.getSubtaskTypeName() + " | " +
                i.getParentTypeName() + " | " +
                i.getSizing() + " | " +
                i.getEffort() + " | " +
                i.getCycle() + " | " +
                i.getProjectKey().orElse("null"))
            .collect(joining("\n"));

        assertEquals(expected, current);
    }

    private void assertBaseCluster(final BaseClusterDto actualCluster, final Long expectedClusterId, final String expectedClusterName, final String... expectedItems) {
        assertEquals(expectedClusterId, actualCluster.getId());
        assertEquals(expectedClusterName, actualCluster.getName());
        assertBaseClusterItems(asList(expectedItems), actualCluster.getItems());
    }

    private void assertBaseClusterItems(final List<String> expetedItems, final List<BaseClusterItemDto> actualItems) {
        String expected = StringUtils.join(expetedItems, "\n");
        String current = actualItems.stream()
            .map(i -> i.getSubtaskTypeName() + " | " +
                i.getSizing() + " | " +
                i.getEffort() + " | " +
                i.getCycle()
                )
            .collect(joining("\n"));

        assertEquals(expected, current);
    }
}
