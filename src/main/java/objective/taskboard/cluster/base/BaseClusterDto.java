package objective.taskboard.cluster.base;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;

class BaseClusterDto {

    private Long id;
    private String name;
    private List<BaseClusterItemDto> items = new ArrayList<>();

    public BaseClusterDto() {
    }

    public BaseClusterDto(Long id, String name, List<BaseClusterItemDto> items) {
        this.id = id;
        this.name = name;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BaseClusterItemDto> getItems() {
        return items;
    }

    public void setItems(List<BaseClusterItemDto> items) {
        this.items = items;
    }

    public SizingCluster toEntity() {
        SizingCluster clusterEntity = new SizingCluster(this.name);
        List<SizingClusterItem> items = this.items.stream()
            .map(i -> i.toEntity(clusterEntity))
            .collect(toList());

        clusterEntity.setItems(items);
        return clusterEntity;
    }
}
