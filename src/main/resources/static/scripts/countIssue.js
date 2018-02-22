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
function CountIssue() {
    function getElementsName(obj) {
        var result = [];
        for (var elementName in obj) {
            result.push(elementName);
        }
        return result;
    }

    function getObject(parent, name) {
        var nameClean = name.replace(/[^a-zA-Z0-9]/g, '_').trim();

        if (!parent[nameClean])
            parent[nameClean] = {};
        return parent[nameClean];
    }

    function getCache() {
        if (!window.cacheCount)
            window.cacheCount = {};
        return window.cacheCount;
    }

    function getParentCount(details, step) {
        var parentCount = getCache();
        parentCount = getObject(parentCount, details.level);
        parentCount = getObject(parentCount, details.stage);
        parentCount = getObject(parentCount, step);
        if (details.team)
            parentCount = getObject(parentCount, details.team);
        return parentCount;
    }

    function recCount(array, filterTeam) {
        var count = 0;
        var elementsName = getElementsName(array);
        if (filterTeam) {
            elementsName.forEach(function (elementName) {
                if (elementName != "count")
                    count += (array[elementName].count || 0);
            });
            return count;
        }

        return array.count || 0;
    }

    this.countStep = function (level, stage, step, filterTeam) {

        var cacheCount = getCache();
        if ((getElementsName(cacheCount)).length == 0)
            return 0;
        var levelCount = getObject(cacheCount, level);
        var levelStage = getObject(levelCount, stage);
        var levelStep = getObject(levelStage, step);

        return recCount(levelStep, filterTeam);
    };

    this.countStage = function (level, stage, filterTeam) {

        var cacheCount = getCache();
        if ((getElementsName(cacheCount)).length == 0)
            return 0;
        var levelCount = getObject(cacheCount, level);
        var levelStage = getObject(levelCount, stage);

        var count = 0;
        var keysStage = getElementsName(levelStage);
        keysStage.forEach(function (keyStage) {
            var levelStep = levelStage[keyStage];
            var result = recCount(levelStep, filterTeam);
            count = count + result;
        });
        return count;
    };

    this.updateCount = function (details, step, value) {
        var parentCount = getParentCount(details, step);
        if (parentCount.count == value)
            return false;
        parentCount.count = value;
        return true;
    }

}

var countIssue = new CountIssue();
