package objective.taskboard.controller;

import java.util.ArrayList;

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

public class ProjectCreationData {
    public String projectKey;
    public String teamLeader;
    public ArrayList<ProjectCreationDataTeam> teams;

    public static class ProjectCreationDataTeam {
        public String name;
        public ArrayList<String> members;
        public ArrayList<ProjectCreationDataWip> wipConfigurations;
    }

    public static class ProjectCreationDataWip {
        public Long statusId;
        public Integer wip;
    }
}