/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2016 Objective Solutions
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

function SearchFilter() {

    var searchData = {
        query: undefined
        , release: undefined
        , updatedIssues: undefined
        , hierarchy: undefined
        , dependencies: undefined
    };
    var rootHierarchicalFilter;

    var hasAnyFilter = function() {
        if(searchData.query || searchData.release)
            return true;
        if(searchData.updatedIssues && searchData.updatedIssues.length)
            return true;
        if(searchData.hierarchy && searchData.hierarchy.length)
            return true;
        if(searchData.dependencies && searchData.dependencies.length)
            return true;
    }

    var matchByString = function(issue, searchString) {
        if (!searchString)
            return true;

        var searchUpperCase = searchString.toUpperCase();
        if (matchByAttribute(issue.issueKey, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.assignee, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.subResponsaveis, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.summary, searchUpperCase))
            return true;
        else if (issue.customfields.release && matchByAttribute(issue.customfields.release.value, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.usersTeam, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.labels, searchUpperCase))
            return true;
        else if (matchByAttribute(issue.components, searchUpperCase))
            return true;

        return false;
    };

    var matchByAttribute = function(attribute, search) {
        if (!Array.isArray(attribute))
            attribute = [ attribute ];

        for (var index in attribute) {
            var value = attribute[index];
            if (value && value.toUpperCase().indexOf(search) !== -1)
                return true;
        }
        return false;
    };

    var matchByRelease = function(issue, releaseId) {
        if (!releaseId)
            return true;

        return issue.releaseId && issue.releaseId == releaseId;
    };

    var matchByKey = function(issue, keys) {
        if(!keys || !keys.length)
            return true;

        return _.contains(keys, issue.issueKey);
    };

    this.updateFilter = function(source, change) {
        jQuery.extend(searchData, change)
        source.fire('iron-signal', {name:'search-filter-changed', data:searchData});
    }

    this.match = function(issue) {
        if (!issue || !hasAnyFilter())
            return true;

        if(searchData.updatedIssues && searchData.updatedIssues.length)
            return matchByKey(issue, searchData.updatedIssues);

        return matchByString(issue, searchData.query)
            && matchByRelease(issue, searchData.release)
            && matchByKey(issue, searchData.hierarchy)
            && matchByKey(issue, searchData.dependencies);
    };

    this.isHierarchyRoot = function(issueKey) {
        return rootHierarchicalFilter === issueKey;
    }

    this.isDependency = function(issueKey) {
        if(!searchData.dependencies || !searchData.dependencies.length)
            return false;

        return _.contains(searchData.dependencies, issueKey);
    };

    this.getRootHierarchicalFilter = function() {
        return rootHierarchicalFilter;
    };

    this.toggleRootHierarchicalFilter = function(source, issueKey) {
        rootHierarchicalFilter = rootHierarchicalFilter == issueKey ? null : issueKey;
        source.fire('iron-signal', {name: 'hierarchical-filter-changed'});

        if (rootHierarchicalFilter) {
            source.fire('iron-signal', {name: 'search-filter-reset'});
        } else {
            source.fire('iron-signal', {name: 'search-filter-restore'});
        }

        var hierarchy = taskboard.getHierarchyMatch(rootHierarchicalFilter);
        var dependencies = taskboard.getDependenciesMatch(hierarchy);
        this.updateFilter(source, {
            hierarchy: hierarchy
            , dependencies: dependencies
        });
    };
}

var searchFilter = new SearchFilter();