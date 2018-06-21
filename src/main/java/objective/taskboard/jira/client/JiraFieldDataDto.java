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
    private JiraFieldSchemaDto schema;

    public JiraFieldDataDto() {
    }

    public JiraFieldDataDto(String id, String name, FieldType fieldType, JiraFieldSchemaDto schema) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
        this.schema = schema;
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

    public JiraFieldSchemaDto getSchema() {
        return schema;
    }
}
