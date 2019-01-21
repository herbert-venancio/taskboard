package objective.taskboard.jira.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum IssueEventTypeName {
    ISSUE_MOVED("issue_moved");

    public final String typeName;

    IssueEventTypeName(String typeName) {
        this.typeName = typeName;
    }

    @JsonCreator
    public static IssueEventTypeName from(String typeName) {
        for(IssueEventTypeName item : values())
            if(item.typeName.equals(typeName))
                return item;

        return null;
    }
}
