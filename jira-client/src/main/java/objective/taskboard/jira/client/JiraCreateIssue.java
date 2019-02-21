package objective.taskboard.jira.client;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableSet;

import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.data.Version;
import retrofit.http.GET;
import retrofit.http.Query;

public class JiraCreateIssue {

    private static final Logger log = LoggerFactory.getLogger(JiraCreateIssue.class);

    public interface Service {
        @GET("/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields")
        JiraCreateIssue all();
        @GET("/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields")
        JiraCreateIssue getByProjectKey(@Query("projectKeys") String projectKey);
        @GET("/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields")
        JiraCreateIssue getByProjectKey(@Query("projectKeys") String projectKey, @Query("issueTypeIds") List<Long> issueTypeIds);
    }

    public List<ProjectMetadata> projects;

    public static class ProjectMetadata {
        public String key;
        @JsonProperty("issuetypes")
        public List<IssueTypeMetadata> issueTypes;

    }

    public static class IssueTypeMetadata {
        public Long id;
        public String name;
        public boolean subtask;
        private Map<String, FieldInfoMetadata> fields;

        public IssueTypeMetadata() {}

        public IssueTypeMetadata(Long id, String name, boolean subtask, Map<String, FieldInfoMetadata> fields) {
            this.id = id;
            this.name = name;
            this.subtask = subtask;
            this.fields = fields;
        }

        public List<FieldInfoMetadata> getFields() {
            return fields.entrySet().stream()
                    .map(entry -> {
                        entry.getValue().id = entry.getKey();
                        return entry.getValue();
                    })
                    .collect(Collectors.toList());
        }

        public FieldInfoMetadata getField(String fieldId) {
            FieldInfoMetadata field = fields.get(fieldId);
            if(field == null)
                return null;
            field.id = fieldId;
            return field;
        }

        public boolean containsFieldId(String fieldId) {
            return fields.containsKey(fieldId);
        }
    }

    public static class FieldInfoMetadata {

        static final Set<String> customFieldsTypesWithFieldOption = ImmutableSet.of(
                "com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes",
                "com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons",
                "com.atlassian.jira.plugin.system.customfieldtypes:select",
                "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect",
                "com.atlassian.jira.plugin.system.customfieldtypes:multiselect"
        );

        public String id;
        public String name;
        public boolean required;
        public FieldSchema schema;
        private List<Object> allowedValues;
        public List<Object> getAllowedValues() {
            if(allowedValues == null || allowedValues.isEmpty())
                return Collections.emptyList();

            FieldSchemaType type;
            if(schema.custom != null && customFieldsTypesWithFieldOption.contains(schema.custom))
                type = FieldSchemaType.option;
            else
                type = schema.type == FieldSchemaType.array ? schema.items : schema.type;

            return allowedValues.stream()
                    .map(type.converter)
                    .collect(Collectors.toList());
        }

        public FieldInfoMetadata() {}

        public FieldInfoMetadata(String id, boolean required, String name) {
            this.id = id;
            this.required = required;
            this.name = name;
        }
    }

    @JsonSerialize(using = FieldSchemaTypeSerializer.class)
    public static class FieldSchemaType {

        static final Map<String, FieldSchemaType> constants = new LinkedHashMap<>();
        static final ObjectMapper objectMapper;

        static {
            objectMapper = new ObjectMapper();
            objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        static Function<Object, ?> remap(Class<?> toClass) {
            return raw -> {
                try {
                    return objectMapper.readValue(objectMapper.writeValueAsString(raw), toClass);
                } catch (IOException e) {
                    log.error("Unable to convert", e);
                    return raw;
                }
            };
        }

        public static final FieldSchemaType string = valueOf("string");
        public static final FieldSchemaType number = valueOf("number");
        public static final FieldSchemaType date = valueOf("date");
        public static final FieldSchemaType array = valueOf("array");
        public static final FieldSchemaType option = valueOf("option", remap(CustomFieldOption.class));
        public static final FieldSchemaType project = valueOf("project", remap(JiraProjectDto.class));
        public static final FieldSchemaType user = valueOf("user", remap(JiraUser.class));
        public static final FieldSchemaType priority = valueOf("priority", remap(JiraPriorityDto.class));
        public static final FieldSchemaType issuetype = valueOf("issuetype", remap(JiraIssueTypeDto.class));
        public static final FieldSchemaType issuelink = valueOf("issuelink", remap(JiraLinkDto.class));
        public static final FieldSchemaType version = valueOf("version", remap(Version.class));
        public static final FieldSchemaType timetracking = valueOf("timetracking");
        public static final FieldSchemaType any = valueOf("any");

        public static final FieldSchemaType component = valueOf("component");
        public static final FieldSchemaType issuelinks = valueOf("issuelinks");
        public static final FieldSchemaType attachment = valueOf("attachment");

        public final String text;
        public final Function<Object, ?> converter;

        public FieldSchemaType(String text, Function<Object, ?> converter) {
            this.text = text;
            this.converter = converter;
        }

        @JsonCreator
        public static FieldSchemaType valueOf(String text) {
            return valueOf(text, Function.identity());
        }

        public static FieldSchemaType valueOf(String text, Function<Object, ?> converter) {
            return constants.computeIfAbsent(text, key -> new FieldSchemaType(key, converter));
        }
    }

    public static class FieldSchema {
        public FieldSchemaType type;
        public String system;
        public FieldSchemaType items;
        public String custom;
        public Long customId;
    }

    public static class CustomFieldOption {

        public CustomFieldOption() { }

        public CustomFieldOption(String value) {
            this.value = value;
        }

        public String value;
    }

    public static class FieldSchemaTypeSerializer extends JsonSerializer<FieldSchemaType>
    {
        @Override
        public void serialize(FieldSchemaType value, JsonGenerator jgen,
                              SerializerProvider provider)
                throws IOException, JsonProcessingException
        {
            jgen.writeString(value.text);
        }

    }
}
