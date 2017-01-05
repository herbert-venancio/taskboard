package objective.taskboard.jira;

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

import static com.google.common.collect.Lists.newArrayList;
import static objective.taskboard.config.CacheConfiguration.JIRA_FIELD_METADATA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Field;

import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;

@Service
public class FieldMetadataService {

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Cacheable(JIRA_FIELD_METADATA)
    public List<Field> getFieldsMetadata() {
        Iterable<Field> fields = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getFields());
        return newArrayList(fields);
    }

}
