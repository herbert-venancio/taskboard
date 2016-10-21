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
import objective.taskboard.domain.Stage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StageConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final int order;
    private final String stage;
    private final String color;
    private final double weight;
    private final boolean showHeader;
    private final List<StepConfiguration> steps;

    public static StageConfiguration from(Stage stage) {
        return new StageConfiguration(stage.getId(), stage.getOrdem(), stage.getName(), stage.getColor(), stage.getWeight(),
                stage.isShowHeader(), new ArrayList<>());
    }

    public void addStepConfiguration(StepConfiguration step) {
        steps.add(step);
    }

}
