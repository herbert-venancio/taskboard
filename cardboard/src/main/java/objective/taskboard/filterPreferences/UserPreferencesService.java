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
        return repository.findOneByJiraUser(CredentialsHolder.defineUsername())
                .orElseGet(() -> new UserPreferences(CredentialsHolder.defineUsername()));
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

        cardFieldFilters.forEach(cardFieldFilter ->
            cardFieldFilter.getFilterFieldsValues().forEach(filterFieldValue -> {
                if (filterPreferences.containsKey(filterFieldValue.getValue()))
                    filterFieldValue.setSelected(filterPreferences.get(filterFieldValue.getValue()));
            })
        );
    }

    public LaneConfiguration applyLoggedUserPreferencesOnLaneConfiguration(LaneConfiguration laneConf) {
        return getUserLevelPreferences(laneConf.getLevel())
                .map(levelPreference -> LaneConfiguration.from(laneConf, levelPreference))
                .orElse(laneConf);
    }

    private Optional<UserPreferences.LevelPreference> getUserLevelPreferences(String laneLevel) {
        return repository.findOneByJiraUser(CredentialsHolder.defineUsername())
                .flatMap(userPreferences -> userPreferences.getPreferences().levelPreferences.stream()
                        .filter(levelPref -> levelPref.level.equals(laneLevel))
                        .findFirst());
    }
}