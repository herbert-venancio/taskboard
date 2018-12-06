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

}
