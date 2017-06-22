package objective.taskboard.issueBuffer;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.data.Issue;
import objective.taskboard.domain.converter.IssueMetadata;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.ProjectService;

@Slf4j
@Service
public class AllIssuesBufferService {
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private JiraIssueToIssueConverter issueConverter;
    
    @Autowired
    private JiraIssueService jiraIssueService;
    
    private Map<String, IssueMetadata> allMetadatasByIssueKey = newHashMap();
    
    private Map<String, Issue> allIssuesBuffer = new LinkedHashMap<>();
    
    private boolean isUpdatingAllIssuesBuffer = false;
    public synchronized void updateAllIssuesBuffer() {
        if (isUpdatingAllIssuesBuffer)
            return;
        
        isUpdatingAllIssuesBuffer = true;
        Thread thread = new Thread(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                log.debug("updateAllIssuesBuffer start");
                List<Issue> list = issueConverter.convert(jiraIssueService.searchAllProjectIssues(), allMetadatasByIssueKey);
                
                allIssuesBuffer.clear();
                for (Issue issue : list) 
                    allIssuesBuffer.put(issue.getIssueKey(), issue);
                log.debug("All issues count: " + list.size());
                log.debug("updateAllIssuesBuffer complete");
                log.debug("updateAllIssuesBuffer time spent " +stopWatch.getTime());
            }finally {
                isUpdatingAllIssuesBuffer = false;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public synchronized List<Issue> getAllIssuesVisibleToUser() {
        return allIssuesBuffer.values().stream()
                .filter(t -> projectService.isProjectVisible(t.getProjectKey()))
                .collect(toList());
    }
}
