package objective.taskboard.data;

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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import objective.taskboard.domain.Lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

}
