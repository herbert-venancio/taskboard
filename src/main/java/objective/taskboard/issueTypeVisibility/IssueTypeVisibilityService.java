package objective.taskboard.issueTypeVisibility;

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

import com.atlassian.jira.rest.client.api.domain.IssueType;
import objective.taskboard.domain.IssueTypeConfiguration;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.repository.IssueTypeConfigurationCachedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IssueTypeVisibilityService {

    @Autowired
    private IssueTypeConfigurationCachedRepository issueTypeConfigurationRepository;

    @Autowired
    private MetadataService metadataService;

    private List<IssueTypeConfiguration> loadIssueTypeConfiguration() {
        return issueTypeConfigurationRepository.getCache();
    }

    private List<IssueTypeConfiguration> getIssueTypeConfiguration() {
        return loadIssueTypeConfiguration();
    }

    public List<IssueType> getVisibleIssueTypes() {
        List<Long> ids = getIssueTypeConfiguration().stream().map(IssueTypeConfiguration::getIssueTypeId).collect(Collectors.toList());
        return metadataService.getIssueTypeMetadataAsLoggedInUser().values().stream().filter(t -> ids.contains(t.getId())).collect(Collectors.toList());
    }

    public Map<Long, List<Long>> getIssueTypeVisibility() {
        return getIssueTypeConfiguration().stream().filter(t -> t.getParentIssueTypeId() == 0).collect(
                Collectors.toMap(IssueTypeConfiguration::getIssueTypeId, t -> getChildren(t.getIssueTypeId())));
    }

    private List<Long> getChildren(long parent) {
        return getIssueTypeConfiguration().stream().filter(x -> x.getParentIssueTypeId() == parent)
                .map(IssueTypeConfiguration::getIssueTypeId).collect(Collectors.toList());
    }

}
