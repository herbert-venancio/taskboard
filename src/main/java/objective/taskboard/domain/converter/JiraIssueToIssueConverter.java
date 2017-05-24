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

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.controller.IssuePriorityService;
import objective.taskboard.data.CustomField;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.domain.ParentIssueLink;
import objective.taskboard.domain.converter.IssueTeamService.InvalidTeamException;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraService;
import objective.taskboard.repository.ParentIssueLinkRepository;

@Slf4j
@Service
public class JiraIssueToIssueConverter {

    public static final String INVALID_TEAM = "INVALID_TEAM";

    @Autowired
    private ParentIssueLinkRepository parentIssueLinkRepository;

    @Autowired
    private IssueTeamService issueTeamService;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private IssueColorService issueColorService;

    @Autowired
    private StartDateStepService startDateStepService;
    
    @Autowired
    private IssuePriorityService priorityService;

    private List<String> parentIssueLinks;

    private Map<String, IssueMetadata> metadatasByIssueKey = newHashMap();

    @PostConstruct
    private void loadParentIssueLinks() {
        parentIssueLinks = parentIssueLinkRepository.findAll().stream()
                               .map(ParentIssueLink::getDescriptionIssueLink)
                               .collect(toList());
    }

    public List<objective.taskboard.data.Issue> convert(List<Issue> issueList) {
        loadParentIssueLinks();

        metadatasByIssueKey = newHashMap();
        metadatasByIssueKey = issueList.stream()
            .collect(toMap(i -> i.getKey(), i -> new IssueMetadata(i, jiraProperties, parentIssueLinks, log)));

        List<objective.taskboard.data.Issue> converted = issueList.stream()
                                  .map(i -> convert(i))
                                  .collect(toList());
        System.out.println(converted.size());
        return converted;
    }

    public objective.taskboard.data.Issue convert(Issue jiraIssue) {
        IssueMetadata metadata = new IssueMetadata(jiraIssue, jiraProperties, parentIssueLinks, log);
        metadatasByIssueKey.put(jiraIssue.getKey(), metadata);

        String avatarCoAssignee1 = jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getAvatarUri("24x24").toString() : "";
        String avatarCoAssignee2 = metadata.getCoAssignees().stream()
                .map(c -> c.getAvatarUrl())
                .filter(url -> !url.equals(avatarCoAssignee1))
                .findFirst().orElse("");

        List<String> issueTeams = newArrayList();
        List<String> usersTeam = newArrayList();
        try {
            IssueMetadata parentMetadata = getParentMetadata(metadata);

            Map<String, List<String>> mapUsersTeams = issueTeamService.getIssueTeams(metadata, parentMetadata);

            for (List<String> teams : mapUsersTeams.values())
                issueTeams.addAll(teams);

            issueTeams = issueTeams.stream()
                    .distinct()
                    .collect(toList());

            usersTeam.addAll(mapUsersTeams.keySet());
        } catch (InvalidTeamException e) {
            issueTeams.add(INVALID_TEAM);
            usersTeam.addAll(e.getUsersInInvalidTeam());
        }
        Long priorityOrder = priorityService.determinePriority(jiraIssue);
        objective.taskboard.data.Issue i = objective.taskboard.data.Issue.from(
                jiraIssue.getId(),
                jiraIssue.getKey(),
                jiraIssue.getProject().getKey(),
                jiraIssue.getProject().getName(),
                jiraIssue.getIssueType().getId(),
                jiraIssue.getIssueType().getIconUri().toASCIIString(),
                jiraIssue.getSummary() != null ? jiraIssue.getSummary() : "",
                jiraIssue.getStatus().getId(),
                startDateStepService.get(jiraIssue),
                avatarCoAssignee1,
                avatarCoAssignee2,
                metadata.getParentKey(),
                getParentTypeId(metadata),
                getParentTypeIconUri(metadata),
                metadata.getDependenciesIssuesKey(),
                issueColorService.getColor(getClassOfServiceId(metadata)),
                String.join(",", getCoAssigneesName(metadata)),
                jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getName() : "",
                String.join(",", usersTeam),
                jiraIssue.getPriority() != null ? jiraIssue.getPriority().getId() : 0l,
                jiraIssue.getDueDate() != null ? jiraIssue.getDueDate().toDate() : null,
                jiraIssue.getCreationDate().getMillis(),
                jiraIssue.getDescription() != null ? jiraIssue.getDescription() : "",
                issueTeams,
                getComments(metadata),
                metadata.getLabels(),
                metadata.getComponents(),
                getCustomFields(metadata),
                priorityOrder
        );
        return i;
    }

    private long getParentTypeId(IssueMetadata metadata) {
        if (metadata.getRealParent() == null)
            return 0;
        return metadata.getRealParent().getTypeId();
    }

    private String getParentTypeIconUri(IssueMetadata metadata) {
        if (metadata.getRealParent() == null)
            return "";
        return metadata.getRealParent().getTypeIconUrl();
    }

    private List<String> getCoAssigneesName(IssueMetadata metadata) {
        return metadata.getCoAssignees().stream()
                .map(x -> x.getName())
                .collect(toList());
    }

    private String getComments(IssueMetadata metadata) {
        if (metadata.getComments().isEmpty())
            return "";
        return metadata.getComments().get(0);
    }

    private Map<String, Object> getCustomFields(IssueMetadata metadata) {
        Map<String, Object> customFields = newHashMap();
        customFields.put(jiraProperties.getCustomfield().getClassOfService().getId(), getClassOfServiceValue(metadata));
        customFields.put(jiraProperties.getCustomfield().getBlocked().getId(), metadata.getBlocked());
        customFields.put(jiraProperties.getCustomfield().getLastBlockReason().getId(), metadata.getLastBlockReason());
        customFields.putAll(metadata.getTShirtSizes());
        customFields.putAll(metadata.getAdditionalEstimatedHours());
        customFields.putAll(getRelease(metadata));
        return customFields;
    }

    private String getClassOfServiceValue(IssueMetadata metadata) {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        CustomField classOfService = getClassOfService(metadata);
        return classOfService == null ? defaultClassOfService : (String)classOfService.getValue();
    }

    private Long getClassOfServiceId(IssueMetadata metadata) {
        CustomField classOfService = getClassOfService(metadata);
        return classOfService == null ? 0L : classOfService.getOptionId();
    }

    private CustomField getClassOfService(IssueMetadata metadata) {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        CustomField classOfService = metadata.getClassOfService();

        boolean isNotDefaultClassOfService = classOfService != null
                                             && classOfService.getValue() != null
                                             && !classOfService.getValue().toString().equals(defaultClassOfService);
        if (isNotDefaultClassOfService)
            return classOfService;

        IssueMetadata parentMetadata = getParentMetadata(metadata);
        if (parentMetadata == null)
            return classOfService;

        return getClassOfService(parentMetadata);
    }

    private Map<String, CustomField> getRelease(IssueMetadata metadata) {
        Map<String, CustomField> release = metadata.getRelease();

        if (!release.isEmpty())
            return release;

        IssueMetadata parentMetadata = getParentMetadata(metadata);
        if (parentMetadata == null)
            return newHashMap();

        return getRelease(parentMetadata);
    }

    private IssueMetadata getParentMetadata(IssueMetadata metadata) {
        String parentKey = metadata.getParentKey();
        if (isNullOrEmpty(parentKey))
            return null;

        IssueMetadata parentMetadata = metadatasByIssueKey.get(parentKey);

        if (parentMetadata != null)
            return parentMetadata;

        Issue parent = jiraService.getIssueByKeyAsMaster(parentKey);
        parentMetadata = new IssueMetadata(parent, jiraProperties, parentIssueLinks, log);
        metadatasByIssueKey.put(parent.getKey(), parentMetadata);
        return parentMetadata;
    }
}
