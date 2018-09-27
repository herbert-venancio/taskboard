package objective.taskboard.followup.cluster;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "sizing_cluster")
public class SizingCluster extends TaskboardEntity {

    private String name;

    @OneToMany(mappedBy = "baseCluster", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<SizingClusterItem> items;

    public SizingCluster() {
    }

    public SizingCluster(Long id, String name) {
        this.setId(id);
        this.setName(name);
    }

    public SizingCluster(String name) {
        this.setName(name);
    }

    public SizingCluster(String name, List<SizingClusterItem> items) {
        super();
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notBlank(name);
        this.name = name;
    }

    public List<SizingClusterItem> getItems() {
        return items;
    }

    public void setItems(List<SizingClusterItem> items) {
        this.items = items;
    }
}
