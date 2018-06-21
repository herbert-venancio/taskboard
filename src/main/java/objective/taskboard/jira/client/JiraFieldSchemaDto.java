package objective.taskboard.jira.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JiraFieldSchemaDto {

    private Long customId;
    private CustomFieldTypes custom;
    private FieldSchemaType type;
    private FieldSchemaType items;

    public JiraFieldSchemaDto() {}

    public JiraFieldSchemaDto(Long customId, CustomFieldTypes custom, FieldSchemaType type, FieldSchemaType items) {
        this.customId = customId;
        this.custom = custom;
        this.type = type;
        this.items = items;
    }

    public Long getCustomId() {
        return customId;
    }

    public CustomFieldTypes getCustom() {
        return custom;
    }

    public FieldSchemaType getType() {
        return type;
    }

    public FieldSchemaType getItems() {
        return items;
    }

    public static class FieldSchemaType {

        static final Map<String, FieldSchemaType> constants = new LinkedHashMap<>();

        public static final FieldSchemaType array = valueOf("array");
        public static final FieldSchemaType option = valueOf("option");

        public final String text;

        private FieldSchemaType(String text) {
            this.text = text;
        }

        @JsonCreator
        public synchronized static FieldSchemaType valueOf(String text) {
            return constants.computeIfAbsent(text, FieldSchemaType::new);
        }
    }

    public static class CustomFieldTypes {

        static final Map<String, CustomFieldTypes> constants = new LinkedHashMap<>();

        public static final CustomFieldTypes multiselect = valueOf("com.atlassian.jira.plugin.system.customfieldtypes:multiselect");

        public final String text;

        public CustomFieldTypes(String text) {
            this.text = text;
        }

        @JsonCreator
        public synchronized static CustomFieldTypes valueOf(String text) {
            return constants.computeIfAbsent(text, CustomFieldTypes::new);
        }
    }

}
