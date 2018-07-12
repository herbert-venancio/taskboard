package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class IssueStatusFlow {

    private String pKey;
    private StatusTransitionChain firstStatus;
    private String issueType;

    public IssueStatusFlow(String pKey, String issueType, StatusTransitionChain firstStatus) {
        this.pKey = pKey;
        this.issueType = issueType;
        this.firstStatus = firstStatus;
    }

    public boolean isOnStatusOnDay(String status, ZonedDateTime date) {
        Optional<StatusTransitionChain> issueStatus = firstStatus.givenDate(date);
        if(!issueStatus.isPresent())
            return false;
        return issueStatus.get().isStatus(status);
    }
    
    public String getIssueType() { 
        return issueType;
    }
    
    @Override
    public String toString() {
        return String.format("[%s]", pKey);
    }

}
