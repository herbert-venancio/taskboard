package objective.taskboard.issueBuffer;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import objective.taskboard.data.Issue;
import objective.taskboard.data.IssueScratch;
import objective.taskboard.domain.converter.IncompleteIssueException;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.domain.converter.ParentProvider;
import objective.taskboard.jira.SearchIssueVisitor;

public class IssueBufferServiceSearchVisitor implements SearchIssueVisitor {
    private final JiraIssueToIssueConverter issueConverter;
    private final CardRepo issueByKey;
    private final Map<String, List<IssueScratch>> pending = new LinkedHashMap<>();

    private final ParentProvider provider;
    
    public IssueBufferServiceSearchVisitor(JiraIssueToIssueConverter issueConverter, CardRepo issueBuffer) {
        this.issueConverter = issueConverter;
        issueByKey = issueBuffer;
        
        provider = parentKey -> {
            Issue issue = issueByKey.get(parentKey);
            if (issue == null)
                return Optional.empty();
            
            return Optional.of(issue);
        };
    }
    
    public CardRepo getIssuesByKey() {
        return issueByKey;
    }
    
    @Override
    public void processIssue(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        String currentIssueKey = jiraIssue.getKey();
        try {
            issueByKey.putOnlyIfNewer(currentIssueKey, issueConverter.convertSingleIssue(jiraIssue, provider));
        }catch(IncompleteIssueException e) {
            String parentKey = e.getMissingParentKey();
            List<IssueScratch> issuesDependingOnParent = pending.get(parentKey);
            if (issuesDependingOnParent == null) {
                issuesDependingOnParent = new ArrayList<>();
                pending.put(parentKey, issuesDependingOnParent);
            }
            issuesDependingOnParent.add(e.getIncompleteIssue());
            return;
        }
        
        if (!pending.containsKey(currentIssueKey)) 
            return;
        
        LinkedList<IssueScratch> issuesToConvert = new LinkedList<>();
        
        issuesToConvert.addAll(pending.remove(currentIssueKey));
        
        while (issuesToConvert.size() > 0) {
            IssueScratch scratch = issuesToConvert.poll();
            issueByKey.putOnlyIfNewer(scratch.getIssueKey(), issueConverter.createIssueFromScratch(scratch, provider));
            
            issuesToConvert.addAll(defaultIfNull(pending.remove(scratch.getIssueKey()), emptyList()));
        }
    }

    @Override
    public void complete() {
        validateNotPending();
    }
    
    private void validateNotPending() {
        List<String> missingParents = new LinkedList<String>();
        for (Entry<String, List<IssueScratch>> each : pending.entrySet()) 
            if (each.getValue().size() > 0) 
                missingParents.add(each.getKey());
        
        if (missingParents.size() > 0)
            throw new IllegalStateException("Some parents were never found: " + StringUtils.join(missingParents,","));
        
        for (Issue each : issueByKey.values()) {
            if (each.getCustomFields() == null ||
                each.getColor() == null ||
                each.getTeams() == null ||
                each.getUsersTeam() == null) {
                throw new IllegalStateException("issue " + each.getIssueKey() + " has invalid null fields");
            }
        }
    }    
}
