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
package objective.taskboard.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import objective.taskboard.domain.Lane;

public class LaneConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final int order;
    private final double weight;
    private final String level;
    private final boolean showHeader;
    private final boolean showLevel;
    private final boolean showLaneTeam;
    private final boolean showParentIconInSynthetic;
    private final List<StageConfiguration> stages;
    private final List<RuleConfiguration> rules;

    public static LaneConfiguration from(Lane lane) {
        return new LaneConfiguration(lane.getId(), lane.getOrdem(), lane.getWeight(), lane.getName(), lane.isShowHeader(), true, lane.showLaneTeam,
        		lane.isShowParentIconInSynthetic(), new ArrayList<>(), new ArrayList<>());
    }

    public void addStageConfiguration(StageConfiguration stage) {
        stages.add(stage);
    }

    public void addRuleConfiguration(RuleConfiguration rule) {
        rules.add(rule);
    }

    public long getId() {
        return this.id;
    }

    public int getOrder() {
        return this.order;
    }

    public double getWeight() {
        return this.weight;
    }

    public String getLevel() {
        return this.level;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public boolean isShowLevel() {
        return this.showLevel;
    }

    public boolean isShowLaneTeam() {
        return this.showLaneTeam;
    }

    public boolean isShowParentIconInSynthetic() {
        return this.showParentIconInSynthetic;
    }

    public List<StageConfiguration> getStages() {
        return this.stages;
    }

    public List<RuleConfiguration> getRules() {
        return this.rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LaneConfiguration that = (LaneConfiguration) o;

        if (id != that.id) return false;
        if (order != that.order) return false;
        if (Double.compare(that.weight, weight) != 0) return false;
        if (showHeader != that.showHeader) return false;
        if (showLevel != that.showLevel) return false;
        if (showLaneTeam != that.showLaneTeam) return false;
        if (showParentIconInSynthetic != that.showParentIconInSynthetic) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (stages != null ? !stages.equals(that.stages) : that.stages != null) return false;
        return rules != null ? rules.equals(that.rules) : that.rules == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + order;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (showHeader ? 1 : 0);
        result = 31 * result + (showLevel ? 1 : 0);
        result = 31 * result + (showLaneTeam ? 1 : 0);
        result = 31 * result + (showParentIconInSynthetic ? 1 : 0);
        result = 31 * result + (stages != null ? stages.hashCode() : 0);
        result = 31 * result + (rules != null ? rules.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LaneConfiguration{" +
                "id=" + id +
                ", order=" + order +
                ", weight=" + weight +
                ", level='" + level + '\'' +
                ", showHeader=" + showHeader +
                ", showLevel=" + showLevel +
                ", showLaneTeam=" + showLaneTeam +
                ", showParentIconInSynthetic=" + showParentIconInSynthetic +
                ", stages=" + stages +
                ", rules=" + rules +
                '}';
    }

    @java.beans.ConstructorProperties({"id", "order", "weight", "level", "showHeader", "showLevel", "showLaneTeam", "showParentIconInSynthetic", "stages", "rules"})
    private LaneConfiguration(final long id, final int order, final double weight, final String level, final boolean showHeader, final boolean showLevel, final boolean showLaneTeam, final boolean showParentIconInSynthetic, final List<StageConfiguration> stages, final List<RuleConfiguration> rules) {
        this.id = id;
        this.order = order;
        this.weight = weight;
        this.level = level;
        this.showHeader = showHeader;
        this.showLevel = showLevel;
        this.showLaneTeam = showLaneTeam;
        this.showParentIconInSynthetic = showParentIconInSynthetic;
        this.stages = stages;
        this.rules = rules;
    }
}
