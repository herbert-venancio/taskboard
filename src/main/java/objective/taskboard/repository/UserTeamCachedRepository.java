package objective.taskboard.repository;

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

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import objective.taskboard.auth.Authenticator;
import objective.taskboard.data.UserTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserTeamCachedRepository {

    @Autowired
    private UserTeamRepository userTeamRepository;

    @Autowired
    private Authenticator authenticator;

    private List<UserTeam> cache;

    @PostConstruct
    private void load() {
        authenticator.authenticateAsServer();
        loadCache();
    }

    public UserTeam findByUserName(String userName) {
        if (cache == null)
            throw new RuntimeException("loadCache() must be executed.");

        Optional<UserTeam> first = cache.stream().filter(ut -> ut.getUserName().equals(userName) && ut.getEndDate() == null).findFirst();
        return first.orElse(null);
    }

    public List<UserTeam> getCache() {
        return ImmutableList.copyOf(cache);
    }

    public void loadCache() {
        log.info("------------------------------ > loadCache()");
        this.cache = userTeamRepository.findByEndDate(null);
    }

}
