package objective.taskboard.domain;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(updatable=false, nullable = false)
    @CreationTimestamp
    protected Instant created;

    @Column(nullable = false)
    @Version
    protected Instant updated;

    public BaseEntity() {
    }

    public BaseEntity(Instant created, Instant updated) {
        this.created = created;
        this.updated = updated;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }
}
