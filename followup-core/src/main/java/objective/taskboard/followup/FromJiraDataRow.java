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
package objective.taskboard.followup;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import objective.taskboard.data.Worklog;

public class FromJiraDataRow {
    
    public static final String PLANNING_TYPE_BALLPARK = "Ballpark";
    public static final String PLANNING_TYPE_PLAN = "Plan";
    
    public static final String QUERY_TYPE_SUBTASK_PLAN = "SUBTASK PLAN";
    public static final String QUERY_TYPE_DEMAND_BALLPARK = "DEMAND BALLPARK";
    public static final String QUERY_TYPE_FEATURE_BALLPARK = "FEATURE BALLPARK";
    
    public String planningType;
    public String project;
    public String demandType = "";
    public String demandStatus  = "";
    public Long demandId;
    public String demandNum = "";
    public String demandSummary = "";
    public String demandDescription = "";
    public Integer demandStatusPriority = 0;
    public Long demandPriorityOrder = 0l;
    public Long demandStartDateStepMillis;
    public String demandAssignee;
    public ZonedDateTime demandDueDate;
    public ZonedDateTime demandCreated;
    public String demandLabels;
    public String demandComponents;
    public String demandReporter;
    public String demandCoAssignees;
    public String demandClassOfService;
    public ZonedDateTime demandUpdatedDate;
    public Double demandCycletime;
    public Boolean demandIsBlocked;
    public String demandLastBlockReason;
    public Map<String, String> demandExtraFields;

    public String taskType = "";
    public String taskStatus = "";
    public Long taskId = 0L;
    public String taskNum = "";
    public String taskSummary = "";
    public String taskDescription = "";
    public String taskFullDescription = "";
    public Double taskAdditionalEstimatedHours;
    public String taskRelease = "";
    public Integer taskStatusPriority = 0;
    public Long taskPriorityOrder = 0l;
    public Long taskStartDateStepMillis;
    public String taskAssignee;
    public ZonedDateTime taskDueDate;
    public ZonedDateTime taskCreated;
    public String taskLabels;
    public String taskComponents;
    public String taskReporter;
    public String taskCoAssignees;
    public String taskClassOfService;
    public ZonedDateTime taskUpdatedDate;
    public Double taskCycletime;
    public Boolean taskIsBlocked;
    public String taskLastBlockReason;
    public Map<String, String> taskExtraFields;

    public String subtaskType;
    public String subtaskStatus;
    public Long subtaskId = 0L;
    public String subtaskNum;
    public String subtaskSummary;
    public String subtaskDescription;
    public String subtaskFullDescription;
    public Integer subtaskStatusPriority = 0;
    public Long subtaskPriorityOrder = 0l;
    public Long subtaskStartDateStepMillis;
    public String subtaskAssignee;
    public ZonedDateTime subtaskDueDate;
    public ZonedDateTime subtaskCreated;
    public String subtaskLabels;
    public String subtaskComponents;
    public String subtaskReporter;
    public String subtaskCoAssignees;
    public String subtaskClassOfService;
    public ZonedDateTime subtaskUpdatedDate;
    public Double subtaskCycletime;
    public Boolean subtaskIsBlocked;
    public String subtaskLastBlockReason;
    public Map<String, String> subtaskExtraFields;

    public String tshirtSize;
    public Double worklog;
    public Double wrongWorklog;
    public Double demandBallpark = 0.0;
    public Double taskBallpark = 0.0;
    public String queryType;
    public List<Worklog> worklogs;
}
