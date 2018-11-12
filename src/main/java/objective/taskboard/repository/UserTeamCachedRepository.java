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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;

@Service
public class UserTeamCachedRepository {

    private static final Logger log = LoggerFactory.getLogger(UserTeamCachedRepository.class);

    @Autowired
    private UserTeamRepository userTeamRepository;

    private List<UserTeam> cache;

    @PostConstruct
    private void load() {
        loadCache();
    }

    public List<UserTeam> findByUserName(String userName) {
        if (cache == null)
            loadCache();

        return cache.stream()
                    .filter(ut -> ut.getUserName().equals(userName))
                    .collect(toList());
    }

    public List<UserTeam> findByTeam(String team) {
        if (cache == null)
            loadCache();

        return cache.stream()
                    .filter(ut -> Objects.equals(ut.getTeam(), team))
                    .collect(toList());
    }

    public List<UserTeam> findByUsernameAndRoles(String username, UserTeamRole... roles) {
        List<UserTeamRole> rolesList = asList(roles);
        return findByUserName(username).stream()
                .filter(ut -> rolesList.contains(ut.getRole()))
                .collect(toList());
    }

    public Optional<UserTeam> findByUsernameTeamAndRoles(String username, String team, UserTeamRole... roles) {
        return findByUsernameAndRoles(username, roles).stream()
                .filter(ut -> ut.getTeam().equals(team))
                .findAny();
    }

    public List<UserTeam> getCache() {
        return ImmutableList.copyOf(cache);
    }

    public void loadCache() {
        log.info("------------------------------ > loadCache()");
        this.cache = userTeamRepository.findByEndDate(null);
    }

}
