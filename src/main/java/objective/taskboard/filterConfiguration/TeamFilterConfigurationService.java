package objective.taskboard.filterConfiguration;

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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Team;
import objective.taskboard.domain.TeamFilterConfiguration;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamFilterConfigurationCachedRepository;

@Service
public class TeamFilterConfigurationService {

    @Autowired
    private TeamFilterConfigurationCachedRepository teamFilterConfigurationRepository;

    @Autowired
    private TeamCachedRepository teamRepository;

    @Cacheable("visibleTeams")
    public List<Team> getVisibleTeams() {
        List<Long> teamConfig = teamFilterConfigurationRepository.getCache().stream()
                .map(TeamFilterConfiguration::getTeamId)
                .collect(Collectors.toList());
        return teamRepository.getCache().stream()
                .filter(t -> teamConfig.contains(t.getId()))
                .collect(Collectors.toList());
    }

}
