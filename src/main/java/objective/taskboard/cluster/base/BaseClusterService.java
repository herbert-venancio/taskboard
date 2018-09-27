package objective.taskboard.cluster.base;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.cluster.ClusterRepository;
import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.project.config.IssueTypeSizesProvider;
import objective.taskboard.project.config.IssueTypeSizesProvider.IssueTypeSize;

@Service
public class BaseClusterService {

    private final ClusterRepository repository;
    private final IssueTypeSizesProvider issueTypeSizesProvider;

    @Autowired
    public BaseClusterService(
            ClusterRepository repository,
            IssueTypeSizesProvider issueTypeSizesProvider
            ) {
        this.repository = repository;
        this.issueTypeSizesProvider = issueTypeSizesProvider;
    }

    public Optional<BaseClusterDto> findById(final Long id) {
        Optional<SizingCluster> sizingClusterOptional = repository.findById(id);
        if(!sizingClusterOptional.isPresent())
            return Optional.empty();

        SizingCluster sizingCluster = sizingClusterOptional.get();
        BaseClusterDto clusterDto = toBaseClusterDto(sizingCluster);
        return Optional.ofNullable(clusterDto);
    }

    public List<BaseClusterDto> findAll() {
        List<SizingCluster> allSizingClusters = repository.findAll();
        return toBaseClusterDto(allSizingClusters);
    }

    @Transactional
    public SizingCluster create(final BaseClusterDto baseCluster) {
        return repository.save(baseCluster.toEntity());
    }

    @Transactional
    public Optional<BaseClusterDto> update(final long clusterId, final BaseClusterDto clusterToUpdate) {
        Optional<SizingCluster> clusterSavedOptional = repository.findById(clusterId);
        if (!clusterSavedOptional.isPresent())
            return empty();

        SizingCluster clusterSaved = clusterSavedOptional.get();
        clusterSaved.setName(clusterToUpdate.getName());

        List<SizingClusterItem> itemsToCreate = new ArrayList<SizingClusterItem>();

        clusterToUpdate.getItems().forEach(i -> {
            Optional<SizingClusterItem> matchedItem = findCorrespondingSizingClusterItem(i, clusterSaved.getItems());

            if (matchedItem.isPresent()) {
                SizingClusterItem item = matchedItem.get();
                item.setCycle(i.getCycle());
                item.setEffort(i.getEffort());
                return;
            }
            itemsToCreate.add(i.toEntity(clusterSaved));
        });
        List<SizingClusterItem> itemsToUpdate = new ArrayList<SizingClusterItem>(clusterSaved.getItems());
        itemsToUpdate.addAll(itemsToCreate);

        clusterSaved.setItems(itemsToUpdate);
        SizingCluster clusteUpdated = repository.save(clusterSaved);

        return Optional.ofNullable(toBaseClusterDto(clusteUpdated));
    }

    private Optional<SizingClusterItem> findCorrespondingSizingClusterItem(final BaseClusterItemDto item, final List<SizingClusterItem> itemsSaved) {
        if (itemsSaved.isEmpty())
            return empty();

        return itemsSaved.stream()
            .filter(i -> matchesSizingClusterItem(i, item.getSubtaskTypeName(), item.getSizing()))
            .findFirst();
    }

    private BaseClusterDto toBaseClusterDto(SizingCluster cluster) {
        List<IssueTypeSize> issueTypes = issueTypeSizesProvider.get();

        List<BaseClusterItemDto> baseClusterItemsDto =
            issueTypes.stream()
                .sorted(Comparator.comparing(IssueTypeSize::getIssueType))
                .map(i -> toBaseClusterItemDto(i, cluster.getItems()))
                .collect(toList());

        return new BaseClusterDto(cluster.getId(), cluster.getName(), baseClusterItemsDto);
    }

    private List<BaseClusterDto> toBaseClusterDto(final List<SizingCluster> sizingClusters) {
        return sizingClusters.stream().map(c -> toBaseClusterDto(c))
            .collect(toList());
    }

    private BaseClusterItemDto toBaseClusterItemDto(final IssueTypeSize issueType, final List<SizingClusterItem> items) {
        SizingClusterItem clusterItem =
            items.stream().filter(i -> matchesSizingClusterItem(i, issueType.getIssueType(), issueType.getSize()))
                .findFirst()
                .orElse(new SizingClusterItem(issueType.getIssueType(), "notused", issueType.getSize(), 0.0, 0.0, null, null));

        return new BaseClusterItemDto(clusterItem);
    }

    private Boolean matchesSizingClusterItem(final SizingClusterItem item, final String subtaskTypeName, final String sizing) {
        return item.getSubtaskTypeName().equals(subtaskTypeName) && item.getSizing().equals(sizing);
    }
}
