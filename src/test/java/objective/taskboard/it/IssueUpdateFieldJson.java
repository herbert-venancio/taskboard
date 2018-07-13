package objective.taskboard.it;

public enum IssueUpdateFieldJson {

    STATUS_DOING("{\"status\":{\"id\": \"10652\",\"name\": \"Doing\"}}"),
    STATUS_DEFERRED("{\"status\":{\"id\": \"10655\",\"name\": \"Deferred\"}}"),

    ASSIGNEE_FOO("{\"assignee\":{\"name\":\"foo\"}}"),

    CLASS_OF_SERVICE_STANDARD("{\"customfield_11440\":{\"id\": \"12606\",\"value\": \"Standard\"}}"),
    CLASS_OF_SERVICE_EXPEDITE("{\"customfield_11440\":{\"id\": \"12608\",\"value\": \"Expedite\"}}"),
    CLASS_OF_SERVICE_FIXED_DATE("{\"customfield_11440\":{\"id\": \"12607\",\"value\": \"Fixed Date\"}}"),

    RELEASE_2_0("{\"customfield_11455\":{\"id\": \"12551\",\"name\": \"2.0\"}}"),
    RELEASE_3_0("{\"customfield_11455\":{\"id\": \"12552\",\"name\": \"3.0\"}}"),

    PROPERTIES_EMPTY("\"properties\":[]");

    public String json;

    IssueUpdateFieldJson(String json) {
        this.json = json;
    }

}
