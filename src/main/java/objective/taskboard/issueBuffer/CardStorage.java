package objective.taskboard.issueBuffer;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import objective.taskboard.data.Issue;

public interface CardStorage {

    void commit();

    void rollback();
    
    Date getLastRemoteUpdatedDate();
    
    void setLastRemoteUpdatedDate(Date t);
    
    void storeIssue(String key, Issue issue);
    
    void removeIssue(String key);

    Map<String, Issue> issues();

    Set<String> getCurrentProjects();

    void putProjects(Set<String> currentProjects);
}
