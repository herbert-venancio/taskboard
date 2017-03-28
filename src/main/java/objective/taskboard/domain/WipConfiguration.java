package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wip_config")
public class WipConfiguration extends TaskboardEntity implements Serializable {
    private static final long serialVersionUID = -7054505295750531721L;

    @Setter
    @Getter
    protected String team;
    
    @Setter
    @Getter
    protected String status;
    
    @Setter
    @Getter
    protected Integer wip;
}
