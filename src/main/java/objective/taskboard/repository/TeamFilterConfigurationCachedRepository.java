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
package objective.taskboard.repository;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import objective.taskboard.domain.TeamFilterConfiguration;

@Service
public class TeamFilterConfigurationCachedRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TeamFilterConfigurationCachedRepository.class);
    @Autowired
    private TeamFilterConfigurationRepository repository;

    private List<TeamFilterConfiguration> cache = Lists.newArrayList();

    @PostConstruct
    private void load() {
        loadCache();
    }

    public List<TeamFilterConfiguration> getCache() {
        return ImmutableList.copyOf(cache);
    }

    public TeamFilterConfiguration findByTeamId(Long teamId) {
        for (TeamFilterConfiguration teamFilterConfiguration : cache) {
            if (teamFilterConfiguration.getTeamId() == teamId)
                return teamFilterConfiguration;
        }
        return null;
    }

    public void loadCache() {
        log.info("------------------------------ > TeamFilterConfigurationRepository.loadCache()");
        List<TeamFilterConfiguration> findAll = repository.findAll();
        this.cache = findAll;
    }

    public TeamFilterConfiguration save(TeamFilterConfiguration teamFilterConfiguration) {
        TeamFilterConfiguration persisted = repository.save(teamFilterConfiguration);
        loadCache();
        return persisted;
    }
}
