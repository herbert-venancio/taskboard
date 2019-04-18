package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "holiday")
public class Holiday extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Date day;

    public Holiday() {
    }

    public String getName() {
        return this.name;
    }

    public Date getDay() {
        return this.day;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDay(final Date day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", day=" + day +
                '}';
    }
}
