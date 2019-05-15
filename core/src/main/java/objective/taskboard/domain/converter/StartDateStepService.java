package objective.taskboard.domain.converter;

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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.filter.LaneService;
import objective.taskboard.jira.client.ChangelogGroupDto;
import objective.taskboard.jira.client.JiraIssueDto;

@Service
public class StartDateStepService {

    private LaneService laneService;

    @Autowired
    public StartDateStepService(LaneService laneService) {
        this.laneService = laneService;
    }

    public long get(JiraIssueDto jiraIssue) {
        List<ChangelogGroupDto> changelogStatus = getChangelogsStatus(jiraIssue.getChangelog());

        if (changelogStatus.isEmpty())
            return jiraIssue.getCreationDate().getMillis();

        List<Long> statusList = laneService.getSteps(jiraIssue.getIssueType().getId(), jiraIssue.getStatus().getId()).stream()
                .flatMap(step -> step.getIssuesConfiguration().stream()
                        .map(IssuesConfiguration::getStatus))
                .collect(toList());

        ChangelogGroupDto firstStatusStep = getFirstStatusStep(changelogStatus, statusList);
        return firstStatusStep == null ?
                   jiraIssue.getCreationDate().getMillis() :
                   firstStatusStep.getCreated().getMillis();
    }

    private List<ChangelogGroupDto> getChangelogsStatus(List<ChangelogGroupDto> changelog) {
        if(changelog == null)
            return Collections.emptyList();
        return changelog.stream()
                .flatMap(changelogGroup ->
                        changelogGroup.getItems().stream()
                                .filter(changelogItem -> "status".equals(changelogItem.getField()))
                                .map(changelogItem -> new ChangelogGroupDto(changelogGroup.getAuthor()
                                        , changelogGroup.getCreated()
                                        , singletonList(changelogItem))))
                .sorted(Comparator.comparing(ChangelogGroupDto::getCreated).reversed())
                .collect(toList());
    }

    private ChangelogGroupDto getFirstStatusStep(List<ChangelogGroupDto> changelogStatus, List<Long> statusList) {
        ChangelogGroupDto firstStatusInStep = null;
        for (ChangelogGroupDto changelog : changelogStatus) {
            Long statusTo = Long.valueOf(changelog.getItems().get(0).getTo());
            if(!statusList.contains(statusTo))
                break;
            firstStatusInStep = changelog;
        }
        return firstStatusInStep;
    }
}
