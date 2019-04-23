package objective.taskboard.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import objective.taskboard.domain.converter.BooleanConverter;

@Entity
@Table(name = "stage")
public class Stage extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Integer ordem;

    private Double weight;

    @Convert(converter = BooleanConverter.class)
    private boolean showHeader;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hexdacimal color!")
    private String color;

    @ManyToOne
    @JoinColumn(name = "lane")
    private Lane lane;

    @OneToMany(mappedBy = "stage", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Step> steps;

    public String getName() {
        return this.name;
    }

    public Integer getOrdem() {
        return this.ordem;
    }

    public Double getWeight() {
        return this.weight;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public String getColor() {
        return this.color;
    }

    public Lane getLane() {
        return this.lane;
    }

    public List<Step> getSteps() {
        return this.steps;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setOrdem(final Integer ordem) {
        this.ordem = ordem;
    }

    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    public void setShowHeader(final boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public void setLane(final Lane lane) {
        this.lane = lane;
    }

    public void setSteps(final List<Step> steps) {
        this.steps = steps;
    }

}
