package objective.taskboard.followup;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

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
    public String demandType = "";
    public String demandStatus  = "";
    public Long demandId;
    public String demandNum = "";
    public String demandSummary = "";
    public String demandDescription = "";

    public String taskType;
    public String taskStatus;
    public Long taskId;
    public String taskNum;
    public String taskSummary;
    public String taskDescription;
    public String taskFullDescription;
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
    public Double demandBallpark = 0.0;
    public Double taskBallpark;

    public String queryType;
    
    @Override
    public String toString() {
        return
                   " planningType           : " + planningType
                +"\n project                : "+ project
                +"\n demandType             : "+ demandType
                +"\n demandStatus           : "+ demandStatus
                +"\n demandId               : "+ defaultIfNull(demandId,"")
                +"\n demandNum              : "+ demandNum
                +"\n demandSummary          : "+ demandSummary
                +"\n demandDescription      : "+ demandDescription
                +"\n taskType               : "+ taskType
                +"\n taskStatus             : "+ taskStatus
                +"\n taskId                 : "+ taskId
                +"\n taskNum                : "+ taskNum
                +"\n taskSummary            : "+ taskSummary
                +"\n taskDescription        : "+ taskDescription
                +"\n taskFullDescription    : "+ taskFullDescription
                +"\n taskRelease            : "+ taskRelease
                +"\n subtaskType            : "+ subtaskType
                +"\n subtaskStatus          : "+ subtaskStatus
                +"\n subtaskId              : "+ subtaskId
                +"\n subtaskNum             : "+ subtaskNum
                +"\n subtaskSummary         : "+ subtaskSummary
                +"\n subtaskDescription     : "+ subtaskDescription
                +"\n subtaskFullDescription : "+ subtaskFullDescription
                +"\n tshirtSize             : "+ tshirtSize
                +"\n worklog                : "+ worklog
                +"\n wrongWorklog           : "+ wrongWorklog
                +"\n demandBallpark         : "+ demandBallpark
                +"\n taskBallpark           : "+ taskBallpark
                +"\n queryType              : "+ queryType;
    }
}