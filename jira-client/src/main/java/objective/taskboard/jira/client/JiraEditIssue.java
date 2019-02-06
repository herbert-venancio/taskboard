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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import retrofit.http.GET;
import retrofit.http.Path;

public class JiraEditIssue {
    private static final Logger log = LoggerFactory.getLogger(JiraCreateIssue.class);

    public interface Service {
        @GET("/rest/api/latest/issue/{issueKey}/editmeta?expand=projects.issuetypes.fields")
        JiraEditIssue getIssueMetadata(@Path("issueKey") String issueKey);
    }

    public Map<String, FieldInfoMetadata> fields;

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

    public static class FieldInfoMetadata {

        static final Set<String> customFieldsTypesWithFieldOption = ImmutableSet.of(
                "com.atlassian.jira.plugin.system.customfieldtypes:select"
        );

        public String id;
        public String name;
        public boolean required;
        public FieldSchema schema;
        private List<Object> allowedValues;
        public List<Object> getAllowedValues() {
            if(allowedValues == null)
                return null;
            if(allowedValues.isEmpty())
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
}
