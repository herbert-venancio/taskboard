package objective.taskboard.followup.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name="TEMPLATE", uniqueConstraints = @UniqueConstraint(name="unique_template_name", columnNames = {"name"}))
public class Template extends TaskboardEntity {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String path;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "FOLLOWUP_TEMPLATE_ROLE",
                     joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", roles=" + roles +
                '}';
    }
}
