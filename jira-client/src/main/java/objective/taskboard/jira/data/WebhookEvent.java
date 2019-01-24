package objective.taskboard.jira.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum WebhookEvent {

    ISSUE_CREATED(Category.ISSUE, "jira:issue_created"),
    ISSUE_DELETED(Category.ISSUE, "jira:issue_deleted"),
    ISSUE_UPDATED(Category.ISSUE, "jira:issue_updated"),
    ISSUE_MOVED(Category.ISSUE, "jira:issue_updated"),
    WORKLOG_UPDATED(Category.ISSUE, "jira:worklog_updated"),
    VERSION_CREATED(Category.VERSION, "jira:version_created"),
    VERSION_DELETED(Category.VERSION, "jira:version_deleted"),
    VERSION_MERGED(Category.VERSION, "jira:version_merged"),
    VERSION_UPDATED(Category.VERSION, "jira:version_updated"),
    VERSION_MOVED(Category.VERSION, "jira:version_moved"),
    VERSION_RELEASED(Category.VERSION, "jira:version_released"),
    VERSION_UNRELEASED(Category.VERSION, "jira:version_unreleased");

    public final Category category;
    public final String typeName;

    WebhookEvent(Category type, String typeName) {
        this.category = type;
        this.typeName = typeName;
    }

    @JsonCreator
    public static WebhookEvent from(String typeName) {
        for(WebhookEvent item : values())
            if(item.typeName.equals(typeName))
                return item;

        return null;
    }

    public enum Category {
        ISSUE
        , VERSION
    }
}
