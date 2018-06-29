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

package objective.taskboard.filterPreferences;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.domain.UserPreferences;
import objective.taskboard.domain.UserPreferences.LevelPreference;
import objective.taskboard.domain.UserPreferences.Preferences;
import objective.taskboard.repository.UserPreferencesRepository;

@Service
public class UserPreferencesService {

    private UserPreferencesRepository repository;

    @Autowired
    public UserPreferencesService(UserPreferencesRepository repository) {
        this.repository = repository;
    }

    public UserPreferences getLoggedUserPreferences() {
        Optional<UserPreferences> userPreferences = repository.findOneByJiraUser(CredentialsHolder.username());
        if (userPreferences.isPresent())
            return userPreferences.get();
        return new UserPreferences(CredentialsHolder.username());
    }

    public void save(String jiraUser, Preferences preferences) {
        Optional<UserPreferences> userPreferencesOpt = repository.findOneByJiraUser(jiraUser);
        if (userPreferencesOpt.isPresent()) {
            UserPreferences updatedUserPreferences = userPreferencesOpt.get();
            updatedUserPreferences.setPreferences(preferences);
            repository.updatePreferences(updatedUserPreferences);
        } else {
            repository.add(new UserPreferences(jiraUser, preferences));
        }
    }

    public void applyLoggedUserPreferencesOnCardFieldFilter(List<CardFieldFilter> cardFieldFilters) {
        applyPreferencesOnCardFieldFilter(cardFieldFilters, getLoggedUserPreferences());
    }

    private void applyPreferencesOnCardFieldFilter(List<CardFieldFilter> cardFieldFilters, UserPreferences userPreferences) {
        final Map<String, Boolean> filterPreferences = userPreferences.getPreferences().filterPreferences;
        if (filterPreferences.isEmpty())
            return;

        cardFieldFilters.stream().forEach(cardFieldFilter -> {
            cardFieldFilter.getFilterFieldsValues().stream().forEach(filterFieldValue -> {
                if (filterPreferences.containsKey(filterFieldValue.getValue()))
                    filterFieldValue.setSelected(filterPreferences.get(filterFieldValue.getValue()));
            });
        });
    }

    public void applyLoggedUserPreferencesOnLaneConfiguration(List<LaneConfiguration> lanesConfiguration) {
        applyPreferencesOnLaneConfiguration(lanesConfiguration, getLoggedUserPreferences());
    }

    private void applyPreferencesOnLaneConfiguration(List<LaneConfiguration> lanesConfiguration, UserPreferences userPreferences) {
        List<LevelPreference> levelPreferences = userPreferences.getPreferences().levelPreferences;
        if (levelPreferences.isEmpty())
            return;

        lanesConfiguration.stream().forEach(laneConf -> {
            Optional<LevelPreference> levelPreferenceOpt = levelPreferences.stream()
                    .filter(levelPref -> levelPref.level.equals(laneConf.getLevel()))
                    .findFirst();
            if (!levelPreferenceOpt.isPresent())
                return;

            LevelPreference levelPreference = levelPreferenceOpt.get();
            laneConf.setShowHeader(levelPreference.showHeader);
            laneConf.setShowLaneTeam(levelPreference.showLaneTeam);
            laneConf.setShowLevel(levelPreference.showLevel);
            laneConf.setWeight(levelPreference.weightLevel);
        });
    }

}