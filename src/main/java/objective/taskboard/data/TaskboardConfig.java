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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskboardConfig {

    private String levelDesc;
    private String levelOrder;
    private String levelWeight;
    private String stage;
    private String stageOrder;
    private String step;
    private String stepOrder;
    private String weight;
    private String stageWeight;
    private long issueType;
    private String status;
    private String color;
    private String levelShowHeader;
    private String stageShowHeader;
    private String stepShowHeader;

    public static TaskboardConfig from(String levelDesc, String levelOrder, String levelWeight, String stage,
                                       String stageOrder, String step, String stepOrder, String weight, String stageWeight, long issueType,
                                       String status, String color, String levelShowHeader, String stageShowHeader, String stepShowHeader) {

        return new TaskboardConfig(levelDesc, levelOrder, levelWeight, stage, stageOrder, step, stepOrder, weight,
                stageWeight, issueType, status, color, levelShowHeader, stageShowHeader, stepShowHeader);
    }

}
