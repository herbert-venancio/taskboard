/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
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

    @Override
    public String toString() {
        return "Stage{" +
                "name='" + name + '\'' +
                ", ordem=" + ordem +
                ", weight=" + weight +
                ", showHeader=" + showHeader +
                ", color='" + color + '\'' +
                ", lane=" + lane +
                ", steps=" + steps +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Stage stage = (Stage) o;

        if (showHeader != stage.showHeader) return false;
        if (name != null ? !name.equals(stage.name) : stage.name != null) return false;
        if (ordem != null ? !ordem.equals(stage.ordem) : stage.ordem != null) return false;
        if (weight != null ? !weight.equals(stage.weight) : stage.weight != null) return false;
        if (color != null ? !color.equals(stage.color) : stage.color != null) return false;
        if (lane != null ? !lane.equals(stage.lane) : stage.lane != null) return false;
        return steps != null ? steps.equals(stage.steps) : stage.steps == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (ordem != null ? ordem.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (showHeader ? 1 : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (lane != null ? lane.hashCode() : 0);
        result = 31 * result + (steps != null ? steps.hashCode() : 0);
        return result;
    }
}
