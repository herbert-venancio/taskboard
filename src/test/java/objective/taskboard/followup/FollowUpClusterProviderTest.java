package objective.taskboard.followup;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.followup.cluster.SizingClusterItemRepository;
import objective.taskboard.followup.cluster.SizingClusterRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpClusterProviderTest {

    private static final String TASKB_PROJECT_KEY = "TASKB";
    private static final String TASKB_TWO_PROJECT_KEY = "TASKB_TWO";
    private static final String TASKB_THREE_PROJECT_KEY = "TASKB_THREE";
    private static final String BALLPARK_DEV = "BALLPARK - Dev";
    private static final String DEV = "Dev";
    private static final String BALLPARK_ALPHA = "BALLPARK - Alpha";
    private static final String NOTUSED = "notused";
    
    private SizingCluster baseCluster = new SizingCluster(1L, "Base Sizing Cluster");
    
    @Mock
    private ProjectFilterConfigurationCachedRepository projectRepository;
    
    @Mock
    private SizingClusterRepository clusterRepository;
    
    @Mock
    private SizingClusterItemRepository clusterItemRepository;
    
    @InjectMocks
    private FollowupClusterProvider followupClusterProvider;
    
    @Test
    public void shouldReturnEmptyWhen_projectNotFound() {
        
        when(projectRepository.getProjectByKey(Mockito.anyString()))
            .thenReturn(Optional.empty());
        
        FollowupCluster followupCluster = followupClusterProvider.getForProject("UNKNOWN_PROJECT");
        
        assertTrue(followupCluster.getClusterItems().isEmpty());
    }
    
    @Test
    public void shouldReturnOnlyItemsByProject_whenProjectHasNotCluster() {
        
        when(clusterRepository.findById(Optional.empty()))
            .thenReturn(Optional.empty());
        
        ProjectFilterConfiguration projectOne = new ProjectFilterConfiguration(TASKB_PROJECT_KEY, 1L);
        
        List<SizingClusterItem> itemsByProjectMock = generateProjectItemsMock(baseCluster, TASKB_PROJECT_KEY);
        
        when(projectRepository.getProjectByKey(TASKB_PROJECT_KEY))
            .thenReturn(Optional.of(projectOne));
        
        when(clusterItemRepository.findByProjectKeyOrBaseCluster(TASKB_PROJECT_KEY, Optional.empty()))
            .thenReturn(Optional.of(itemsByProjectMock));
        
        FollowupCluster followupCluster = followupClusterProvider.getForProject(TASKB_PROJECT_KEY);
        List<FollowUpClusterItem> clusterItems = followupCluster.getClusterItems();
        
        ProjectFilterConfiguration expectedProject = new ProjectFilterConfiguration(TASKB_PROJECT_KEY, 1L);
        
        List<FollowUpClusterItem> expectedItems = Arrays.asList(
                new FollowUpClusterItem(expectedProject, BALLPARK_ALPHA, NOTUSED, "XS", 1.0, 0.0),
                new FollowUpClusterItem(expectedProject, BALLPARK_DEV, NOTUSED, "M", 5.0, 0.0),
                new FollowUpClusterItem(expectedProject, DEV, NOTUSED, "S", 2.0, 0.0)
                );
        
        assertFollowUpItemsEquals(expectedItems, clusterItems);
    }
    
    @Test
    public void shouldReturnOnlyItemsByProject_whenProjectHasClusterWithoutItems() {
        
        when(clusterRepository.findById(Optional.ofNullable(baseCluster.getId())))
            .thenReturn(Optional.ofNullable(baseCluster));
        
        ProjectFilterConfiguration projectOne = new ProjectFilterConfiguration(TASKB_PROJECT_KEY, 1L);
        projectOne.setBaseClusterId(1L);
        
        List<SizingClusterItem> itemsByProjectMock = generateProjectItemsMock(baseCluster, TASKB_PROJECT_KEY);
        
        when(projectRepository.getProjectByKey(TASKB_PROJECT_KEY))
            .thenReturn(Optional.of(projectOne));
        
        when(clusterItemRepository.findByProjectKeyOrBaseCluster(TASKB_PROJECT_KEY, Optional.ofNullable(baseCluster)))
            .thenReturn(Optional.of(itemsByProjectMock));
        
        FollowupCluster followupCluster = followupClusterProvider.getForProject(TASKB_PROJECT_KEY);
        List<FollowUpClusterItem> clusterItems = followupCluster.getClusterItems();
        
        ProjectFilterConfiguration expectedProject = new ProjectFilterConfiguration(TASKB_PROJECT_KEY, 1L);
        expectedProject.setBaseClusterId(1L);
        
        List<FollowUpClusterItem> expectedItems = Arrays.asList(
                new FollowUpClusterItem(expectedProject, BALLPARK_ALPHA, NOTUSED, "XS", 1.0, 0.0),
                new FollowUpClusterItem(expectedProject, BALLPARK_DEV, NOTUSED, "M", 5.0, 0.0),
                new FollowUpClusterItem(expectedProject, DEV, NOTUSED, "S", 2.0, 0.0)
                );
        
        assertFollowUpItemsEquals(expectedItems, clusterItems);
    }
    
    @Test
    public void shouldReturnEmptyWhen_projectFoundWithoutItems() {
        ProjectFilterConfiguration projectTwo = new ProjectFilterConfiguration(TASKB_TWO_PROJECT_KEY, 2L);

        when(projectRepository.getProjectByKey(TASKB_TWO_PROJECT_KEY))
            .thenReturn(Optional.of(projectTwo));

        when(clusterItemRepository.findByProjectKeyOrBaseCluster(Mockito.anyString(), Mockito.any()))
            .thenReturn(Optional.empty());
        
        FollowupCluster followupCluster = followupClusterProvider.getForProject(TASKB_TWO_PROJECT_KEY);
        assertTrue(followupCluster.getClusterItems().isEmpty());
    }
    
    @Test
    public void shouldReturnItemsByProjectAndByClusterWhen_hasOverride() {
        
        when(clusterRepository.findById(Optional.ofNullable(baseCluster.getId())))
            .thenReturn(Optional.ofNullable(baseCluster));

        ProjectFilterConfiguration projectThree = new ProjectFilterConfiguration(TASKB_THREE_PROJECT_KEY, 3L);
        projectThree.setBaseClusterId(1L);
        
        when(projectRepository.getProjectByKey(TASKB_THREE_PROJECT_KEY))
            .thenReturn(Optional.of(projectThree));
        
        List<SizingClusterItem> itemsProjectThreeMock = generateProjectItemsMock(baseCluster, TASKB_THREE_PROJECT_KEY);
        
        SizingClusterItem item4 = new SizingClusterItem(BALLPARK_DEV,   NOTUSED, "M",  4.0, 0.0, TASKB_THREE_PROJECT_KEY, null);
        item4.setId(4L);
        SizingClusterItem item5 = new SizingClusterItem(BALLPARK_ALPHA, NOTUSED, "M",  8.0, 0.0, TASKB_THREE_PROJECT_KEY, null);
        item5.setId(5L);
        SizingClusterItem item6 = new SizingClusterItem(BALLPARK_ALPHA, NOTUSED, "M",  18.0, 1.0, TASKB_THREE_PROJECT_KEY, null);
        item6.setId(6L);
        SizingClusterItem item7 = new SizingClusterItem(DEV,            NOTUSED, "S",  12.0, 1.0, TASKB_THREE_PROJECT_KEY, null);
        item7.setId(7L);
        SizingClusterItem item8 = new SizingClusterItem(DEV,            NOTUSED, "M",  1.0, 0.0, null, baseCluster);
        item8.setId(8L);
        
        itemsProjectThreeMock.add(item4);
        itemsProjectThreeMock.add(item5);
        itemsProjectThreeMock.add(item6);
        itemsProjectThreeMock.add(item7);
        itemsProjectThreeMock.add(item8);
        
        when(clusterItemRepository.findByProjectKeyOrBaseCluster(TASKB_THREE_PROJECT_KEY, Optional.ofNullable(baseCluster)))
            .thenReturn(Optional.of(itemsProjectThreeMock));
        
        FollowupCluster followupCluster = followupClusterProvider.getForProject(TASKB_THREE_PROJECT_KEY);
        
        List<FollowUpClusterItem> clusterItems = followupCluster.getClusterItems();
        
        ProjectFilterConfiguration expectedProject = new ProjectFilterConfiguration(TASKB_THREE_PROJECT_KEY, 3L);
        expectedProject.setBaseClusterId(1L);
        
        List<FollowUpClusterItem> expectedItems = Arrays.asList(
                new FollowUpClusterItem(expectedProject, BALLPARK_ALPHA, NOTUSED, "M", 18.0, 1.0),
                new FollowUpClusterItem(expectedProject, BALLPARK_ALPHA, NOTUSED, "XS", 1.0, 0.0),
                new FollowUpClusterItem(expectedProject, BALLPARK_DEV, NOTUSED, "M", 4.0, 0.0),
                new FollowUpClusterItem(expectedProject, DEV, NOTUSED, "M", 1.0, 0.0),
                new FollowUpClusterItem(expectedProject, DEV, NOTUSED, "S", 12.0, 1.0)
                );
        
        assertFollowUpItemsEquals(expectedItems, clusterItems);
    }
    
    private List<SizingClusterItem> generateProjectItemsMock(final SizingCluster baseCluster, final String projectKey) {

        List<SizingClusterItem> items = new ArrayList<SizingClusterItem>();
        
        SizingClusterItem item1 = new SizingClusterItem(BALLPARK_DEV,   NOTUSED, "M",  5.0, 0.0, projectKey, null);
        item1.setId(1L);
        
        SizingClusterItem item2 = new SizingClusterItem(BALLPARK_ALPHA, NOTUSED, "XS", 1.0, 0.0, projectKey, null);
        item2.setId(2L);
        
        SizingClusterItem item3 = new SizingClusterItem(DEV,            NOTUSED, "S",  2.0, 0.0, projectKey, null);
        item3.setId(3L);
        
        items.add(item1);
        items.add(item2);
        items.add(item3);

        return items;
    }
    
    private void assertFollowUpItemsEquals(List<FollowUpClusterItem> expectedItems,
            List<FollowUpClusterItem> clusterItems) {
        
        String expected = StringUtils.join(expectedItems, "\n");
        String current = StringUtils.join(clusterItems, "\n");
        
        assertEquals(expected, current);
    }
}