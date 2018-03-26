package objective.taskboard.jira.client;

import java.util.List;

import retrofit.http.GET;

public class JiraFieldDataDto {

    public interface Service {
        @GET("/rest/api/latest/field")
        List<JiraFieldDataDto> all();
    }

    public enum FieldType {
        JIRA, CUSTOM
    }

    private String id;
    private String name;
    private FieldType fieldType;

    public JiraFieldDataDto() {
    }

    public JiraFieldDataDto(String id, String name, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

}
