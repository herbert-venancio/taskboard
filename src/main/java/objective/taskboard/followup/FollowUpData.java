package objective.taskboard.followup;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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

public class FollowUpData {
    public String planningType;
    public String project;
    public String demandType;
    public String demandStatus;
    public String demandId;
    public String demandNum;
    public String demandSummary;
    public String demandDescription;

    public String taskType;
    public String taskStatus;
    public Long taskId;
    public String taskNum;
    public String taskSummary;
    public String taskDescription;
    public String taskFullSescription;
    public String taskRelease;

    public String subtaskType;
    public String subtaskStatus;
    public Long subtaskId;
    public String subtaskNum;
    public String subtaskSummary;
    public String subtaskDescription;
    public String subtaskFullDescription;
    public String tshirtSize;
    
    public Double worklog;
    public Double wrongWorklog;
    public Double demandBallpark;
    public Double taskBallpark;

    public String queryType;
}
