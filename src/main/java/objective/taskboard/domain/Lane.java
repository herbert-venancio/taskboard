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
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import objective.taskboard.domain.converter.BooleanConverter;

@Entity
@Table(name = "lane")
public class Lane extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 3755116141467717335L;

    @Convert(converter = BooleanConverter.class)
    public boolean showLaneTeam;

    private String name;

    private Integer ordem;

    private double weight;

    @Convert(converter = BooleanConverter.class)
    private boolean showHeader;
    
    @Column(name = "SHOW_PARENT_ICON_SINT")
    @Convert(converter = BooleanConverter.class)
    private boolean showParentIconInSynthetic;

    @OneToMany(mappedBy = "lane", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Stage> stages;

    @OneToMany(mappedBy = "lane", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private Collection<Rule> rules;

    public Lane() {
    }

    public boolean isShowLaneTeam() {
        return this.showLaneTeam;
    }

    public String getName() {
        return this.name;
    }

    public Integer getOrdem() {
        return this.ordem;
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public boolean isShowParentIconInSynthetic() {
        return this.showParentIconInSynthetic;
    }

    public List<Stage> getStages() {
        return this.stages;
    }

    public Collection<Rule> getRules() {
        return this.rules;
    }

    public void setShowLaneTeam(final boolean showLaneTeam) {
        this.showLaneTeam = showLaneTeam;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setOrdem(final Integer ordem) {
        this.ordem = ordem;
    }

    public void setWeight(final double weight) {
        this.weight = weight;
    }

    public void setShowHeader(final boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setShowParentIconInSynthetic(final boolean showParentIconInSynthetic) {
        this.showParentIconInSynthetic = showParentIconInSynthetic;
    }

    public void setStages(final List<Stage> stages) {
        this.stages = stages;
    }

    public void setRules(final Collection<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "Lane{" +
                "showLaneTeam=" + showLaneTeam +
                ", name='" + name + '\'' +
                ", ordem=" + ordem +
                ", weight=" + weight +
                ", showHeader=" + showHeader +
                ", showParentIconInSynthetic=" + showParentIconInSynthetic +
                ", stages=" + stages +
                ", rules=" + rules +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Lane lane = (Lane) o;

        if (showLaneTeam != lane.showLaneTeam) return false;
        if (Double.compare(lane.weight, weight) != 0) return false;
        if (showHeader != lane.showHeader) return false;
        if (showParentIconInSynthetic != lane.showParentIconInSynthetic) return false;
        if (name != null ? !name.equals(lane.name) : lane.name != null) return false;
        if (ordem != null ? !ordem.equals(lane.ordem) : lane.ordem != null) return false;
        if (stages != null ? !stages.equals(lane.stages) : lane.stages != null) return false;
        return rules != null ? rules.equals(lane.rules) : lane.rules == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (showLaneTeam ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (ordem != null ? ordem.hashCode() : 0);
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (showHeader ? 1 : 0);
        result = 31 * result + (showParentIconInSynthetic ? 1 : 0);
        result = 31 * result + (stages != null ? stages.hashCode() : 0);
        result = 31 * result + (rules != null ? rules.hashCode() : 0);
        return result;
    }
}
