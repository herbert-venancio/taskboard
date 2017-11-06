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
package objective.taskboard.database;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.domain.Lane;
import objective.taskboard.repository.LaneCachedRepository;

@Service
public class TaskboardDatabaseService {

    @Autowired
    private LaneCachedRepository laneRepository;

    public List<LaneConfiguration> laneConfiguration() throws SQLException {
        return getConfigurations();
    }

    @Cacheable(CacheConfiguration.CONFIGURATION)
    private List<LaneConfiguration> getConfigurations() {
        
        List<Lane> lanes = laneRepository.getAll();
        return TaskboardConfigToLaneConfigurationTransformer.getInstance().transform(lanes);
    }

}
