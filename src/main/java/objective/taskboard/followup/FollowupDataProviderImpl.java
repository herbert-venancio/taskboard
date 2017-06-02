package objective.taskboard.followup;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;

import objective.taskboard.data.CustomField;
import objective.taskboard.data.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.BallparkMapping;
import objective.taskboard.jira.MetadataService;

@Service
public class FollowupDataProviderImpl implements FollowupDataProvider {
    @Autowired
    private JiraProperties jiraProperties;
    
    @Autowired
    private IssueBufferService issueBufferService;
    
    @Autowired
    private MetadataService metadataService;

    private Map<Long, Status> statusesMetadata;

    private Map<Long, IssueType> issueTypeMetadata;

    private Map<String, Issue> demandsByKey;
    private Map<String, Issue> featuresByKey;
    private Map<String, FollowUpData> followUpBallparks;
    
    
    @Override
    public List<FollowUpData> getJiraData() {
        loadStatusAndIssueMapping();
        LinkedList<Issue> issues = new LinkedList<>(issueBufferService.getIssues());
        
        followUpBallparks = new LinkedHashMap<String, FollowUpData>();
        
        demandsByKey = makeDemandBallparks(issues);
        
        featuresByKey = makeFeatureBallparks(issues);
        
        final List<FollowUpData> result = makeSubtasks(issues);

        result.addAll(followUpBallparks.values());
        
        return result;
    }
    
    private Map<String, Issue> makeDemandBallparks(LinkedList<Issue> issues) {
        Map<String, Issue> demands = new LinkedHashMap<>();
        Iterator<Issue> it = issues.iterator();
        while(it.hasNext()) {
            Issue issue = it.next();
            if (isDemand(issue)) {
                followUpBallparks.put(issue.getIssueKey(), createBallparkDemand(issue));
                demands.put(issue.getIssueKey(), issue);
                it.remove();
            }
        }
        return demands;
    }
    
    private Map<String, Issue> makeFeatureBallparks(LinkedList<Issue> issues) {
        Map<String, Issue> features = new LinkedHashMap<String, Issue>();
        
        Iterator<Issue> it = issues.iterator();
        while(it.hasNext()) {
            Issue issue = it.next();
            if (!isFeature(issue)) 
                continue;
            it.remove();
            
            Issue demand = demandsByKey.get(issue.getParent());
            if (demand != null)
                followUpBallparks.remove(demand.getIssueKey());
            
            features.put(issue.getIssueKey(), issue);
            
            if (jiraProperties.getFeatureStatusThatDontGenerateBallpark().contains(issue.getStatus()))
                continue;
            
            for (BallparkMapping mapping : jiraProperties.getBallparkMappings()) {
                if (!mapping.getParentIssueType().equals(issue.getType())) continue;
                String tshirtSize = getTshirtSize(issue, mapping.getTshirtCustomFieldId());
                if (tshirtSize == null) continue;
                
                String issueKeyAndTShirtSize = issue.getIssueKey()+mapping.getTshirtCustomFieldId();
                followUpBallparks.put( issueKeyAndTShirtSize, createBallparkFeature(demand, issue, mapping));
            }
        }
        return features;
    }

    private List<FollowUpData> makeSubtasks(LinkedList<Issue> issues) {
        
        final List<FollowUpData> subtasksFollowups = new LinkedList<FollowUpData>();
        Iterator<Issue> it = issues.iterator();
        while(it.hasNext()) {
            Issue issue = it.next();
            Issue feature = featuresByKey.get(issue.getParent());
            
            Issue demand  = demandsByKey.get(feature.getParent());
            if (demand != null)
                followUpBallparks.remove(demand.getIssueKey());
            
            subtasksFollowups.add(createSubTaskFollowup(demand, feature, issue));

            if (jiraProperties.getSubtaskStatusThatDontPreventBallparkGeneration().contains(issue.getStatus()))
                continue;
            
            String featureTshirtForThisSubTask = "";
            
            for (BallparkMapping mapping : jiraProperties.getBallparkMappings()) {
                if (mapping.getJiraIssueTypes().contains(issue.getType()) && 
                    mapping.getParentIssueType().equals(feature.getType())) 
                    featureTshirtForThisSubTask = mapping.getTshirtCustomFieldId();
            }
            
            followUpBallparks.remove(feature.getIssueKey()+featureTshirtForThisSubTask);
        }
        return subtasksFollowups;
    }

    private FollowUpData createBallparkDemand(Issue demand) {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Ballpark";
        followUpData.project = demand.getProject();
        
        followUpData.demandId = demand.getId();
        followUpData.demandType = getIssueTypeName(demand);
        followUpData.demandStatus= getIssueStatusName(demand);
        followUpData.demandNum = demand.getIssueKey();
        followUpData.demandSummary = demand.getSummary();
        followUpData.demandDescription = issueDescription("M", demand);
        
        followUpData.taskType = "BALLPARK - Demand";
        followUpData.taskStatus = followUpData.demandStatus;
        followUpData.taskId = 0L;
        followUpData.taskNum = followUpData.demandNum;
        followUpData.taskSummary="Dummy Feature";
        followUpData.taskDescription = issueDescription(0, demand.getSummary());
        followUpData.taskFullDescription = issueFullDescription("BALLPARK - Demand", "M", 0, demand.getSummary());
        followUpData.taskRelease = (String) defaultIfNull(getRelease(demand), "No release set");
        
        followUpData.subtaskType = "BALLPARK - Demand";
        followUpData.subtaskStatus = followUpData.demandStatus;
        followUpData.subtaskId = 0L;
        followUpData.subtaskNum = demand.getProjectKey()+"-0";
        followUpData.subtaskSummary = followUpData.demandSummary;
        followUpData.subtaskDescription = issueDescription("M", 0, demand.getSummary());
        followUpData.subtaskFullDescription = issueFullDescription("BALLPARK - Demand", "M", 0, demand.getSummary());
        followUpData.tshirtSize = "M";
        followUpData.worklog = 0.0;
        followUpData.wrongWorklog = demand.getTimeTracking().getTimeSpentMinutes()/60.0; 
        followUpData.demandBallpark = demand.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.taskBallpark = 0.0;
        followUpData.queryType = "DEMAND BALLPARK";
        return followUpData;
    }
    
    private FollowUpData createBallparkFeature(Issue demand, Issue task, BallparkMapping m) {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Ballpark";
        followUpData.project = task.getProject();
        
        if (demand != null) {
            followUpData.demandType = getIssueTypeName(demand);
            followUpData.demandStatus= getIssueStatusName(demand);
            followUpData.demandId = demand.getId();
            followUpData.demandNum = demand.getIssueKey();
            followUpData.demandSummary = demand.getSummary();
            followUpData.demandDescription = issueDescription("", demand);
        }
        
        followUpData.taskType = getIssueTypeName(task);
        followUpData.taskStatus = getIssueStatusName(task);
        followUpData.taskId = task.getId();
        followUpData.taskNum = task.getIssueKey();
        followUpData.taskSummary=task.getSummary();
        followUpData.taskDescription = issueDescription(task);
        followUpData.taskFullDescription = issueFullDescription(task);
        followUpData.taskRelease = (String) defaultIfNull(getRelease(task), "No release set");
        
        followUpData.subtaskType = m.getIssueType();
        followUpData.subtaskStatus = getIssueStatusName(task);
        followUpData.subtaskId = 0L;
        followUpData.subtaskNum = task.getProjectKey()+"-0";
        followUpData.subtaskSummary = m.getIssueType();
        followUpData.subtaskDescription = issueDescription(0, task.getSummary());
        followUpData.subtaskFullDescription = issueFullDescription(m.getIssueType(), "", 0, task.getSummary());
        followUpData.tshirtSize = getTshirtSize(task, m.getTshirtCustomFieldId());
        followUpData.worklog = 0.0;
        followUpData.wrongWorklog = task.getTimeTracking().getTimeSpentMinutes()/60.0; 
        followUpData.demandBallpark = demand.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.taskBallpark = task.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.queryType = "FEATURE BALLPARK";
        return followUpData;
    }

    private FollowUpData createSubTaskFollowup(Issue demand, Issue task, Issue subtask) {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Plan";
        followUpData.project = task.getProject();
        
        if (demand != null) {
            followUpData.demandId = demand.getId();
            followUpData.demandType = getIssueTypeName(demand);
            followUpData.demandStatus= getIssueStatusName(demand);
            followUpData.demandNum = demand.getIssueKey();
            followUpData.demandSummary = demand.getSummary();
            followUpData.demandDescription = issueDescription("",demand);
            followUpData.demandBallpark = demand.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        }
        
        followUpData.taskType = getIssueTypeName(task);
        followUpData.taskStatus = getIssueStatusName(task);
        followUpData.taskId = task.getId();
        followUpData.taskNum = task.getIssueKey();
        followUpData.taskSummary=task.getSummary();
        followUpData.taskDescription = issueDescription(task);
        followUpData.taskFullDescription = issueFullDescription(task);
        followUpData.taskRelease = (String) defaultIfNull(getRelease(task), "No release set");
        
        followUpData.subtaskType = getIssueTypeName(subtask);
        followUpData.subtaskStatus = getIssueStatusName(subtask);
        followUpData.subtaskId = subtask.getId();
        followUpData.subtaskNum = subtask.getIssueKey();
        followUpData.subtaskSummary = subtask.getSummary();
        
        followUpData.subtaskDescription = issueDescription(subtask);
        followUpData.subtaskFullDescription = issueFullDescription(subtask);
        followUpData.tshirtSize = getTshirtSize(subtask);
        followUpData.worklog = subtask.getTimeTracking().getTimeSpentMinutes()/60.0;;
        followUpData.wrongWorklog = 0.0; 
        followUpData.taskBallpark = task.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.queryType = "SUBTASK PLAN";
        return followUpData;
    }

    private String getTshirtSize(Issue i) {
        if (isFeature(i)) return "";
        if (isDemand(i)) return "M";
        return ((CustomField)i.getCustomFields().get(jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId())).getValue().toString();
    }
    
    private String getTshirtSize(Issue i, String tshirtSizeId) {
        CustomField customField = (CustomField)i.getCustomFields().get(tshirtSizeId);
        if (customField == null) return null;
        return customField.getValue().toString();
    }
    
    private String getRelease(Issue i) {
        CustomField customField = (CustomField)i.getCustomFields().get(jiraProperties.getCustomfield().getRelease().getId());
        if (customField == null) return null;
        if (customField.getValue() == null)
            return null;
        return customField.getValue().toString();
    }

    private boolean isFeature(Issue issue) {
        return jiraProperties.getIssuetype().getFeatures().stream().anyMatch(f-> f.getId() == issue.getType());
    }

    private boolean isDemand(Issue issue) {
        return jiraProperties.getIssuelink().getDemand().getName().equals(getIssueTypeName(issue));
    }

    private String getIssueStatusName(Issue issue) {
        return statusesMetadata.get(issue.getStatus()).getName();
    }
    
    private String getIssueTypeName(Issue issue) {
        return issueTypeMetadata.get(issue.getType()).getName();
    }
    
    private void loadStatusAndIssueMapping() {
        try {
            statusesMetadata = metadataService.getStatusesMetadata();
            issueTypeMetadata = metadataService.getIssueTypeMetadata();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Could not retrieve status metadata", e);
        }
    }
    
    private String issueDescription(Issue issue) {
        return issueDescription(getTshirtSize(issue), issue.getIssueKeyNum(), issue.getSummary());
    }
    
    private String issueDescription(String size, Issue issue) {
        return issueDescription(size, issue.getIssueKeyNum(), issue.getSummary());
    }
    
    private String issueDescription(Integer issueNum, String description) {
        return issueDescription(null, issueNum, description);
    }

    private String issueDescription(String size, Integer issueNum, String description) {
        String sizePart = StringUtils.isEmpty(size)?"":size+" | ";
        return String.format("%s%05d - %s", sizePart, issueNum, description);
    }
    
    private String issueFullDescription(Issue issue) {
        return getIssueTypeName(issue) + " | " +issueDescription(getTshirtSize(issue), issue.getIssueKeyNum(), issue.getSummary());
    }
    
    private String issueFullDescription(String issueType, String size, Integer issueNum, String description) {
        return issueType + " | " +issueDescription(size, issueNum, description);
    }
}
