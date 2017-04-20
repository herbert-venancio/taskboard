package objective.taskboard.repository;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.WipConfiguration;

public interface WipConfigurationRepository extends JpaRepository<WipConfiguration, Long> {
    List<WipConfiguration> findByTeam(String teamName);

//    @Query("SELECT W                                  " +
//           "  FROM WIP_CONFIG W                       " +
//           "  JOIN TEAM T ON T.NAME = W.TEAM          " +
//           "  JOIN PROJECT_TEAM P ON P.TEAM_ID = T.ID " +
//           "  JOIN USER_TEAM UT ON UT.TEAM = T.NAME   " +
//           " WHERE W.STATUS = ':status'               " +
//           "   AND P.PROJECT_KEY = ':project'         " +
//           "   AND UT.END_DATE IS NULL                " +
//           "   AND UT.USER_NAME = ':user'             " +
//           " ORDER BY W.WIP, T.NAME")
    List<WipConfiguration> findByTeamAndStatus(String team, String status);
}
