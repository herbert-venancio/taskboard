package objective.taskboard.followup;

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
    
    
    @Override
    public List<FollowUpData> getJiraData() {
        Map<String, FollowUpData> followUpData = new LinkedHashMap<String, FollowUpData>();
        loadStatusAndIssueMapping();
        
        final Map<String, Issue> demands = new LinkedHashMap<>();
        final Map<String, Issue> features = new LinkedHashMap<>();
        
        final List<FollowUpData> result = new LinkedList<FollowUpData>();
        
        LinkedList<Issue> issues = new LinkedList<>(issueBufferService.getIssues());
        Iterator<Issue> iterator = issues.iterator();
        while(iterator.hasNext()) {
            Issue issue = iterator.next();
            if (isDemand(issue)) {
                followUpData.put(issue.getIssueKey(), createDemand(issue));
                demands.put(issue.getIssueKey(), issue);
                iterator.remove();
            }
        }
        
        iterator = issues.iterator();
        while(iterator.hasNext()) {
            Issue issue = iterator.next();
            if (isFeature(issue)) {
                //followUpData.put(issue.getIssueKey(), createTask(demands, issue));
                features.put(issue.getIssueKey(), issue);
                iterator.remove();
            }
        }
        
        iterator = issues.iterator();
        while(iterator.hasNext()) {
            Issue issue = iterator.next();
            Issue task = features.get(issue.getParent());
            Issue demand  = demands.get(task.getParent());
            result.add(createSubTask(demand, task, issue));
            followUpData.remove(demand.getIssueKey());
        }

        result.addAll(followUpData.values());
        
        return result;
    }


    private FollowUpData createTask(Map<String, Issue> demands, Issue task) {
        Issue parentDemand = demands.get(task.getParent());
        FollowUpData followUpData = new FollowUpData();
        followUpData.project = parentDemand.getProject();
        followUpData.demandId = parentDemand.getId();
        followUpData.planningType = "Ballpark";
        followUpData.demandType = getIssueTypeName(parentDemand);
        followUpData.demandStatus= getIssueStatusName(parentDemand);
        followUpData.demandNum = parentDemand.getIssueKey();
        followUpData.demandSummary = parentDemand.getSummary();
        Integer issueNum = Integer.parseInt(parentDemand.getIssueKey().replace(parentDemand.getProjectKey()+"-", ""));
        followUpData.demandDescription = String.format("%05d - %s", issueNum, parentDemand.getDescription());
        
        followUpData.taskType = getIssueTypeName(task);
        followUpData.taskStatus = getIssueStatusName(task);
        followUpData.taskId = task.getId();
        followUpData.taskNum = task.getIssueKey();
        followUpData.taskSummary=task.getSummary();
        followUpData.taskDescription = String.format("%05d - %s", 0, task.getDescription());
        followUpData.taskFullDescription = "BALLPARK - Demand | M | " + followUpData.taskDescription;
        followUpData.taskRelease = "No release set";
        
        followUpData.subtaskType = "BALLPARK - Demand";
        followUpData.subtaskStatus = followUpData.demandStatus;
        followUpData.subtaskId = 0L;
        followUpData.subtaskNum = task.getProjectKey()+"-0";
        followUpData.subtaskSummary = followUpData.demandSummary;
        followUpData.subtaskDescription = "M | " + String.format("%05d - %s", 0, task.getSummary());
        followUpData.subtaskFullDescription = "BALLPARK - Demand | M | " + followUpData.taskDescription;
        followUpData.tshirtSize = "M";
        followUpData.worklog = 0.0;
        followUpData.wrongWorklog = task.getTimeTracking().getTimeSpentMinutes()/60.0; 
        followUpData.demandBallpark = task.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.taskBallpark = 0.0;
        followUpData.queryType = "FEATURE BALLPARK";
        return followUpData;
    }

    private FollowUpData createDemand(Issue demand) {
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
        followUpData.taskRelease = "No release set";
        
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

    private FollowUpData createSubTask(Issue demand, Issue task, Issue subtask) {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Plan";
        followUpData.project = demand.getProject();
        followUpData.demandId = demand.getId();
        followUpData.demandType = getIssueTypeName(demand);
        followUpData.demandStatus= getIssueStatusName(demand);
        followUpData.demandNum = demand.getIssueKey();
        followUpData.demandSummary = demand.getSummary();
        followUpData.demandDescription = issueDescription("",demand);
        
        followUpData.taskType = getIssueTypeName(task);
        followUpData.taskStatus = getIssueStatusName(task);
        followUpData.taskId = task.getId();
        followUpData.taskNum = task.getIssueKey();
        followUpData.taskSummary=task.getSummary();
        followUpData.taskDescription = issueDescription(task);
        followUpData.taskFullDescription = issueFullDescription(task);
        followUpData.taskRelease = "No release set";
        
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
        followUpData.demandBallpark = demand.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.taskBallpark = task.getTimeTracking().getOriginalEstimateMinutes()/60.0;
        followUpData.queryType = "SUBTASK PLAN";
        return followUpData;
    }

    private String getTshirtSize(Issue i) {
        if (isFeature(i)) return "";
        if (isDemand(i)) return "M";
        return ((CustomField)i.getCustomFields().get(jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId())).getValue().toString();
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
