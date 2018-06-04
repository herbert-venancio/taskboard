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
package objective.taskboard.controller;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Issue.CardTeam;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataCachedService;
import objective.taskboard.jira.data.Status;
import objective.taskboard.repository.WipConfigurationRepository;


@RestController
@RequestMapping("/api/wip-validator")
public class WipValidatorController {

    private static final String CLASS_OF_SERVICE_EXPEDITE = "Expedite";
    private static final Logger log = LoggerFactory.getLogger(WipValidatorController.class);

    @Autowired
    private WipConfigurationRepository wipConfigRepo;

    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private MetadataCachedService metadataService;
    
    @Autowired
    private IssueBufferService cardService;

    @RequestMapping
    public ResponseEntity<WipValidatorResponse> validate(
            @RequestParam("issue") String issueKey,
            @RequestParam("status") String newStatusName) {
        WipValidatorResponse response = new WipValidatorResponse();

        try {
            Issue issue = cardService.getIssueByKey(issueKey);
            if (issue == null) {
                response.message = "Issue " + issueKey + " not found";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (newStatusName == null || newStatusName.isEmpty()) {
                response.message = "Query parameter 'status' is required";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (isClassOfServiceExpedite(issue)) {
                response.message = "Class of service is " + CLASS_OF_SERVICE_EXPEDITE;
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }

            if (isIssueTypeToIgnore(issue)) {
                response.message = "Issue Type " + issue.getIssueTypeName() + " is ignored on WIP count.";
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }
            
            Optional<Status> newStatus = metadataService.getStatusesMetadata().values().stream()
                    .filter(s -> s.name.equals(newStatusName))
                    .findFirst();
            
            if (!newStatus.isPresent()) {
                response.message = "Status '" + newStatusName + "' not found";
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }

            Optional<WipConfiguration> optionalWipConfig = getWipConfig(issue, newStatus.get().id);
            if (!optionalWipConfig.isPresent()) {
                response.message = "No wip configuration was found";
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }

            WipConfiguration wipConfig = optionalWipConfig.get();

            long issueCount = countIssuesForTheTeamInTheSameStep(wipConfig, issue);

            if (issueCount >= wipConfig.getWip()) {
                response.isWipExceeded = true;
                response.message = "You can't exceed your team's WIP limit ";
            }

            response.message += String.format("(Team: %s, Actual: %d, Limit: %d)", wipConfig.getTeam(),
                    issueCount, wipConfig.getWip());

            return new ResponseEntity<WipValidatorResponse>(response, OK);
        } catch (Exception e) {
            log.error("Wip validation failed", e);
            response.message = e.getMessage() == null ? e.toString() : e.getMessage();
            return new ResponseEntity<WipValidatorResponse>(response, INTERNAL_SERVER_ERROR);
        }
    }

    private long countIssuesForTheTeamInTheSameStep(WipConfiguration wipConfig, Issue iss) {
        return cardService.getAllIssues().stream()
            .filter(issue -> isNotIgnored(issue))
            .filter(issue -> isWipApplicable(wipConfig, issue, iss.getTeams()))
            .count();
    }
    
    private boolean isNotIgnored(Issue issue) {
        return !jiraProperties.getWip().getIgnoreIssuetypesIds().contains(issue.getType());
    }
    
    private boolean isWipApplicable(WipConfiguration wipConfig, Issue issue, Set<CardTeam> set) {
        return wipConfig.isApplicable(issue.getType(), issue.getStatus()) &&
                set.stream().anyMatch(t -> issue.getTeams().contains(t));
    }

    private boolean isClassOfServiceExpedite(Issue issue) {
    	if (issue.getClassOfServiceValue() == null) return false;
        	return issue.getClassOfServiceValue().equals(CLASS_OF_SERVICE_EXPEDITE);
    }

    private Optional<WipConfiguration> getWipConfig(Issue issueByKey, Long newStatusId) {
        long issueTypeId = issueByKey.getType();

        List<String> teamNames = issueByKey.getTeams().stream().map(t->t.name).collect(Collectors.toList());
        List<WipConfiguration> wipConfigsApplicableToIssue = wipConfigRepo.findByTeamIn(teamNames).stream()
                .filter(c -> c.isApplicable(issueTypeId, newStatusId))
                .collect(toList());

        Optional<WipConfiguration> smallestWip = wipConfigsApplicableToIssue.stream()
                .min(comparing(WipConfiguration::getWip));

        return smallestWip;
    }

    private Boolean isIssueTypeToIgnore(Issue issue) {
        if (jiraProperties.getWip() == null)
            return false;
        List<Long> idsToIgnore = jiraProperties.getWip().getIgnoreIssuetypesIds();
        return idsToIgnore.contains(issue.getType());
    }

}
