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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Maps;

import objective.taskboard.domain.IssueColorService;
import objective.taskboard.domain.ParentIssueLink;
import objective.taskboard.domain.converter.IssueTeamService.InvalidTeamException;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.repository.ParentIssueLinkRepository;

@Service
public class JiraIssueToIssueConverter {

    private final String SEM_TIME = "NO TEAM";

    @Autowired
    private ParentIssueLinkRepository parentIssueLinkRepository;
    
    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private IssueColorService issueColorService;
    
    @Autowired
    private IssueTeamService issueTeamService;
    
    private List<String> parentIssueLinks;

    @PostConstruct
    private void loadParentIssueLinks() {
        parentIssueLinks = parentIssueLinkRepository.findAll().stream()
                               .map(ParentIssueLink::getDescriptionIssueLink)
                               .collect(Collectors.toList());
    }

    public List<objective.taskboard.data.Issue> convert(List<Issue> issueList) {
        Map<String, IssueMetadata> metadataByIssueKey = metadataByIssueKey(issueList);
        
        List<objective.taskboard.data.Issue> converted = issueList.stream()
                .map(i -> convert(i, metadataByIssueKey))
                .collect(Collectors.toList());
        return converted;
    }
    
    public objective.taskboard.data.Issue convert(Issue jiraIssue,  List<Issue> allIssues) {
        return convert(jiraIssue, metadataByIssueKey(allIssues));
    }

    private Map<String, IssueMetadata> metadataByIssueKey(List<Issue> issueList) {
        return issueList.stream()
                .collect(toMap(i -> i.getKey(), i -> new IssueMetadata(i, jiraProperties, parentIssueLinks)));
    }

    private objective.taskboard.data.Issue convert(Issue jiraIssue,  Map<String, IssueMetadata> metadataByIssueKey) {
        IssueMetadata metadata = new IssueMetadata(jiraIssue, jiraProperties, parentIssueLinks);

        List<String> coAssignees = metadata.getCoAssignees().stream()
                .map(x -> x.getName())
                .collect(Collectors.toList());
        
        String subResponsavel1 = jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getAvatarUri("24x24").toString() : "";
        String subResponsavel2 = metadata.getCoAssignees().stream()
                .map(s -> s.getAvatarUrl())
                .filter(url -> !url.equals(subResponsavel1))
                .findFirst().orElse("");

        long parentTypeId = getParentTypeId(metadata);
        Long issueTypeId = jiraIssue.getIssueType().getId();
        String color = issueColorService.getColor(issueTypeId, parentTypeId);
        
        List<String> teamGroups = newArrayList(); 
        List<String> usersInvalidTeam = newArrayList();
        try {
            IssueMetadata parentMetadata = metadataByIssueKey.get(metadata.getParentKey());
            teamGroups.addAll(issueTeamService.getIssueTeams(metadata, parentMetadata));
        } catch (InvalidTeamException e) {
            teamGroups.add(SEM_TIME);
            usersInvalidTeam.addAll(e.getUsersInInvalidTeam());
        }
        
        return objective.taskboard.data.Issue.from(
                jiraIssue.getKey(),
                jiraIssue.getProject().getKey(),
                jiraIssue.getProject().getName(),
                issueTypeId,
                jiraIssue.getIssueType().getIconUri().toASCIIString(),
                jiraIssue.getSummary() != null ? jiraIssue.getSummary() : "",
                jiraIssue.getStatus().getId(),
                subResponsavel1,
                subResponsavel2,
                metadata.getParentKey(),
                parentTypeId,
                getParentTypeIconUri(metadata),
                metadata.getRequiredIssuesKey(),
                String.join(",", coAssignees),
                jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getName() : "",
                String.join(",", usersInvalidTeam),
                jiraIssue.getPriority() != null ? jiraIssue.getPriority().getId() : 0l,
                jiraIssue.getDueDate() != null ? jiraIssue.getDueDate().toDate() : null,
                jiraIssue.getDescription() != null ? jiraIssue.getDescription() : "",
                teamGroups,
                getComments(metadata),
                getCustomFields(metadata),
                color
        );
    }

    private Map<String, Object> getCustomFields(IssueMetadata metadata) {
        Map<String, Object> customFields = Maps.newHashMap();
        customFields.put(jiraProperties.getCustomfield().getTShirtSize().getId(), metadata.getTShirtSize());
        customFields.put(jiraProperties.getCustomfield().getClassOfService().getId(), metadata.getClassOfService());
        customFields.put(jiraProperties.getCustomfield().getBlocked().getId(), metadata.getBlocked());
        return customFields;
    }

    private String getParentTypeIconUri(IssueMetadata metadata) {
        if (metadata.getRealParent() != null)
            return metadata.getRealParent().getTypeIconUrl();
        return "";
    }

    private long getParentTypeId(IssueMetadata metadata) {
        if (metadata.getRealParent() != null)
            return metadata.getRealParent().getTypeId();
        return 0;
    }

    private String getComments(IssueMetadata metadata) {
        if (metadata.getComments().isEmpty())
            return "";
        return metadata.getComments().get(0);
    }

}
