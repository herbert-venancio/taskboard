package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ChangelogItemDto {

    private String field;
    private String from;
    private String to;
    private String fromString;
    private String toString;

    public ChangelogItemDto() { }

    public ChangelogItemDto(String field, String from, String to, String fromString, String toString) {
        this.field = field;
        this.from = from;
        this.to = to;
        this.fromString = fromString;
        this.toString = toString;
    }

    public String getField() {
        return field;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFromString() {
        return fromString;
    }

    public String getToString() {
        return toString;
    }

}
