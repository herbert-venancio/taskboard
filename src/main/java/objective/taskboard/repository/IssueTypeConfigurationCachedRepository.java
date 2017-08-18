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

import objective.taskboard.domain.IssueTypeConfiguration;

@Service
public class IssueTypeConfigurationCachedRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueTypeConfigurationCachedRepository.class);
    @Autowired
    private IssueTypeConfigurationRepository issueTypeConfigurationRepository;

    private List<IssueTypeConfiguration> cache = Lists.newArrayList();

    @PostConstruct
    private void load() {
        loadCache();
    }

    public List<IssueTypeConfiguration> getCache() {
        return ImmutableList.copyOf(cache);
    }

    public void loadCache() {
        log.info("------------------------------ > IssueTypeConfigurationCachedRepository.loadCache()");
        this.cache = issueTypeConfigurationRepository.findAll();
    }

}
