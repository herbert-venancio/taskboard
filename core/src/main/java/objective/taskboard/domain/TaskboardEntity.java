package objective.taskboard.domain;

import java.time.Instant;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import objective.taskboard.utils.ObjectUtils;

@MappedSuperclass
public abstract class TaskboardEntity extends BaseEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    public TaskboardEntity() {
    }

    public TaskboardEntity(Long id, Instant created, Instant updated) {
        super(created, updated);
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return ObjectUtils.equals(this, o, other -> Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        // https://vladmihalcea.com/2016/06/06/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return 31;
    }

    @Override
    public String toString() {
        return "TaskboardEntity{" +
                "id=" + id +
                '}';
    }
}
