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
function UserPreferences() {

    var cacheUserPreferences;

    var getDefaultVisibilityConfiguration = function () {
        return {
            "showSynthetic": false
        };
    };

    this.getPreferences = function () {
        return cacheUserPreferences;
    };

    this.setPreferences = function (preferences) {
        cacheUserPreferences = preferences;
    };

    this.getView = function () {
        return cacheUserPreferences.visibilityConfiguration ||
            (cacheUserPreferences.visibilityConfiguration = getDefaultVisibilityConfiguration());
    };

    this.getLanes = function () {
        return cacheUserPreferences.laneConfiguration || (cacheUserPreferences.laneConfiguration = [{'showCount': false}]);
    };

    this.getFilters = function () {
        return cacheUserPreferences.filterPreferences || (cacheUserPreferences.filterPreferences = {});
    };

    this.setFilter = function (filter, value) {
        this.getFilters()[filter] = value;
    };

    this.getLevels = function () {
        return cacheUserPreferences.levelPreferences || (cacheUserPreferences.levelPreferences = []);
    };

    this.setLevels = function (levels) {
        cacheUserPreferences.levelPreferences = levels;
    };

    this.getLevel = function (id) {
        return _.find(this.getLevels(), function(level) {
            return level.id == id;
        });
    };

}

var userPreferences = new UserPreferences();
