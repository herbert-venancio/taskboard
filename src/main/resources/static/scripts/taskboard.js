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
var CUSTOMFIELD = {
    TAMANHO: "customfield_18520",
    CLASSE_DE_SERVICO: "customfield_18522",
    IMPEDIDO: "customfield_18521"
};

var TAMANHO = {
    PP: 'XS',
    P: 'S',
    M: 'M',
    G: 'L',
    GG: 'XL'
};

var ISSUETYPE_ID = {
    DEMANDA: 11700
};

var STATUS_ID = {
    DONE: 10118,
    FECHADO: 10118,
    CANCELADO: 9999999
};

var TRANSITION_REQUIRED_COMMENT = [];

function Taskboard() {
    var self = this;

    var aspectFilters;
    var issues;
    var filteredIssues;
    var laneConfiguration;
    var usingHierarchicalFilter = false;

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

    this.isUsingHierarchicalFilter = function() {
        return usingHierarchicalFilter;
    };

    this.toggleHierarchicalFilter = function() {
        usingHierarchicalFilter = !usingHierarchicalFilter;
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
        return teams.includes("NO TEAM");
    };

}

var taskboard = new Taskboard();
