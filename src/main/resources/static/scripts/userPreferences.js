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

    var _userPreferences;

    this.getPreferences = function() {
        return _userPreferences;
    };

    this.setPreferences = function(preferences) {
        _userPreferences = preferences;
    };

    this.getView = function() {
        return _userPreferences.visibilityConfiguration;
    };

    this.getLane = function() {
        return _userPreferences.laneConfiguration[0];
    };

    this.getFilters = function() {
        return _userPreferences.filterPreferences;
    };

    this.setFilter = function(filter, value) {
        this.getFilters()[filter] = value;
    };

    this.getLevels = function() {
        return _userPreferences.levelPreferences;
    };

    this.setLevels = function(levels) {
        _userPreferences.levelPreferences = levels;
    };

}

var userPreferences = new UserPreferences();
