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

function Taskboard() {
    var self = this;

    var aspectFilters;
    var issues;
    var filteredIssues;
    var laneConfiguration;
    var rootHierarchicalFilter;

    this.setAspectFilters = function(filters) {
        this.aspectFilters = filters;
    };

    this.getAspectFilters = function() {
        return this.aspectFilters;
    };

    this.getTeams = function() {
        var teamFilter = _.find(this.getAspectFilters(), function (filter) {
            return filter.description == "Team"
        });
        return teamFilter.aspectsSubitemFilter;
    };

    this.setIssues = function(issues) {
        this.issues = issues;
        this.setFilteredIssues(issues)
        this.refitSteps();
    };

    this.setFilteredIssues = function(issues) {
        this.filteredIssues = new Object()

        var steps = this.getAllSteps()
        for (s in steps) {
            var step = steps[s]
            var filters = step.issuesConfiguration
            var issuesByStep = new Array()

            for (f in filters) {
                var filter = filters[f]

                for (i in issues) {
                    var issue = issues[i]
                    if (filter.issueType == issue.type && filter.status == issue.status)
                        issuesByStep.push(issue)
                }
            }

            this.filteredIssues[step.id] = issuesByStep;
        }
    },

    this.getIssuesByStep = function(stepId) {
        if (this.filteredIssues)
            return this.filteredIssues[stepId];
    };

    this.getIssues = function() {
        return this.issues;
    };

    this.setLaneConfiguration = function(laneConfiguration) {
        this.laneConfiguration = laneConfiguration;
    };

    this.getLaneConfiguration = function() {
        return this.laneConfiguration;
    };

    this.getAllSteps = function() {
        var steps = new Array()
        for(laneIndex in this.laneConfiguration) {
            var lane = this.laneConfiguration[laneIndex]
            for(stageIndex in lane.stages) {
                steps = steps.concat(lane.stages[stageIndex].steps)
            }
        }

        return steps;
    }

    this.getFilters = function(stepId) {
        for(var lane = 0; lane < this.laneConfiguration.length; lane++)
            for(var stage = 0; stage < this.laneConfiguration[lane].stages.length; stage++)
                for(var step = 0; step < this.laneConfiguration[lane].stages[stage].steps.length; step++) {
                    var cStep = this.laneConfiguration[lane].stages[stage].steps[step];
                    if(cStep.id == stepId)
                        return cStep.issuesConfiguration;
                }
        return null;
    };

    this.getTotalLaneWeight = function() {
        var total = 0;

        if (userPreferences.getLevels().length == 0) {
            this.laneConfiguration.forEach(function(lane) {
                total += lane.weight;
            });
            return total;
        }

        userPreferences.getLevels().forEach(function(x) {
            if(x.showLevel)
                total += x.weightLevel;
        });

        return total;
    };

    this.getLaneContainerHeight = function() {
        var container = document.querySelector('#lane-container');
        return container ? container.offsetHeight : 0;
    };

    this.getLane = function(id) {
        return document.querySelector('#lane-'+id);
    };

    this.refitSteps = _.debounce(function() {
        var steps = document.querySelectorAll('board-step');
        for(var i = 0; i < steps.length; i++)
            steps[i].refit();
        self.refitTables();
    }, 100);

    this.refitTables = function() {
        $('table').floatThead('reflow');
    };

    this.getRootHierarchicalFilter = function() {
        return rootHierarchicalFilter;
    };

    this.toggleRootHierarchicalFilter = function(issueKey) {
        rootHierarchicalFilter = rootHierarchicalFilter == issueKey ? null : issueKey;
    };

    this.clearHierarchyMatch = function() {
        this.issues.forEach(function(i) {
            i.hierarchyMatch = false;
        });
    };

    this.setHierarchyMatch = function(issue) {
        issue.hierarchyMatch = true;
        setParentHierarchyMatch(issue);
        setChildrenHierarchyMatch(issue);
        setDependencyHierarchyMatch(issue);
    };

    var setParentHierarchyMatch = function(issue) {
        self.issues.forEach(function(i) {
            if (i.issueKey !== issue.parent)
                return;
            i.hierarchyMatch = true;
            setParentHierarchyMatch(i);
            setDependencyHierarchyMatch(i);
        });
    };

    var setChildrenHierarchyMatch = function(issue) {
        self.issues.forEach(function(i) {
            if (i.parent !== issue.issueKey)
                return;
            i.hierarchyMatch = true;
            setChildrenHierarchyMatch(i);
            setDependencyHierarchyMatch(i);
        });
    };

    var setDependencyHierarchyMatch = function(issue) {
        self.issues.forEach(function(i) {
            if (issue.requires.indexOf(i.issueKey) >= 0 && i.hierarchyMatch !== true)
                i.hierarchyMatch = "DEP";
        });
    };

    this.hasTeamSelected = function() {
        var teams = this.getTeams();
        for (var index in teams) {
            var team = teams[index];
            if (team.selected) return true;
        }
        return false;
    };

    this.isInvalidTeam = function(teams) {
        if (teams.includes(INVALID_TEAM))
            return true;

        var visibleTeamsToUser = this.getTeams();
        for (var index in visibleTeamsToUser) {
            var visibleTeam = visibleTeamsToUser[index];
            if (teams.includes(visibleTeam.name))
                return false;
        }

        return true;
    };

    this.getIssueTypeName = function(issueTypeId) {
        var types = JSON.parse(localStorage.getItem("issueTypes"));
        for (var i in types)
            if (types[i].id == issueTypeId)
                return types[i].name;
        return "";
    };

    this.getStatusName = function(statusId) {
        var statuses = JSON.parse(localStorage.getItem("statuses"));
        for (var i in statuses)
            if (statuses[i].id == statusId)
                return statuses[i].name;
        return "";
    };

    this.getOnlyOneSize = function(sizes) {
        return sizes.length != 1 ? null : sizes[0].value;
    };

}

var taskboard = new Taskboard();
