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

import java.util.ArrayList;
import java.util.List;

import objective.taskboard.domain.Lane;

public class LaneConfiguration {

    private final long id;
    private final int order;
    private final String level;
    private final List<StageConfiguration> stages;
    private final List<RuleConfiguration> rules;
    private final boolean showParentIconInSynthetic;

    private double weight;
    private boolean showHeader;
    private boolean showLevel;
    private boolean showLaneTeam;

    private LaneConfiguration(
            long id, int order, String level, List<StageConfiguration> stages, List<RuleConfiguration> rules, boolean showParentIconInSynthetic,
            double weight, boolean showHeader, boolean showLevel, boolean showLaneTeam
            ) {
        this.id = id;
        this.order = order;
        this.level = level;
        this.stages = stages;
        this.rules = rules;
        this.showParentIconInSynthetic = showParentIconInSynthetic;

        this.weight = weight;
        this.showHeader = showHeader;
        this.showLevel = showLevel;
        this.showLaneTeam = showLaneTeam;
    }

    public static LaneConfiguration from(Lane lane) {
        return new LaneConfiguration(
                lane.getId(), lane.getOrdem(), lane.getName(), new ArrayList<>(), new ArrayList<>(), lane.isShowParentIconInSynthetic(),
                lane.getWeight(), lane.isShowHeader(), true, lane.showLaneTeam
                );
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

    public String getLevel() {
        return this.level;
    }

    public List<StageConfiguration> getStages() {
        return this.stages;
    }

    public List<RuleConfiguration> getRules() {
        return this.rules;
    }

    public boolean isShowParentIconInSynthetic() {
        return this.showParentIconInSynthetic;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public boolean isShowLevel() {
        return this.showLevel;
    }

    public void setShowLevel(boolean showLevel) {
        this.showLevel = showLevel;
    }

    public boolean isShowLaneTeam() {
        return this.showLaneTeam;
    }

    public void setShowLaneTeam(boolean showLaneTeam) {
        this.showLaneTeam = showLaneTeam;
    }

}
