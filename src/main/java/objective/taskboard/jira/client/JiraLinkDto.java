package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraLinkDto {
    @JsonProperty
    private JiraLinkTypeDto type;
    
    @JsonProperty
    private LinkedIssue inwardIssue;
    
    @JsonProperty
    private LinkedIssue outwardIssue;
    
    private JiraIssueLinkTypeDto linkIssueType;

    public JiraIssueLinkTypeDto getIssueLinkType() {
        if (linkIssueType != null) return linkIssueType;
        JiraIssueLinkTypeDto.Direction direction = determineDirection();
        return linkIssueType = new JiraIssueLinkTypeDto(
                type.name, 
                direction == JiraIssueLinkTypeDto.Direction.INBOUND?type.inward: type.outward,
                direction);
    }

    public LinkedIssue getTargetIssue() {
        if (inwardIssue != null)
            return inwardIssue;
        return outwardIssue;
    }

    public String getTargetIssueKey() {
        return getTargetIssue().key;
    }

    private JiraIssueLinkTypeDto.Direction determineDirection() {
        return inwardIssue != null
                ? JiraIssueLinkTypeDto.Direction.INBOUND
                : JiraIssueLinkTypeDto.Direction.OUTBOUND;
    }    

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class LinkedIssue {
        public String key;
        public JiraIssueDtoFields fields;
    }
}
