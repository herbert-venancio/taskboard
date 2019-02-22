package objective.taskboard.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.data.FieldsRequiredInTransition;
import objective.taskboard.jira.data.Status;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.utils.TransitionUtils;

public class TransitionDto {

    public final long id;
    public final String name;
    public final Status to;
    public final List<FieldDto> fields;
    public final long order;

    public TransitionDto(long id, String name, Status to, List<FieldDto> fields, long order) {
        this.id = id;
        this.name = name;
        this.to = to;
        this.fields = fields;
        this.order = order;
    }

    public static TransitionDto from(Transition t, long order, Map<Long, FieldsRequiredInTransition> fieldsRequiredInTransitions) {
        return new TransitionDto(t.id, t.name, t.to, fields(t, fieldsRequiredInTransitions), order);
    }

    private static List<FieldDto> fields(Transition transition, Map<Long, FieldsRequiredInTransition> fieldsRequiredInTransitions) {
        return transition.getFields().stream()
                .map(f -> FieldDto.from(f, fieldsRequiredInTransitions.get(transition.id)))
                .collect(Collectors.toList());
    }

    public String getErrorMessage() {
        for (FieldDto field : fields) {
            if (!field.required)
                continue;

            if (!TransitionUtils.isSupported(field.schema)) {
                return "Can't perform this transition because it requires fields not supported in taskboard."
                        + " Please, perform the transition on Jira.";
            }

            if (TransitionUtils.isArrayOfVersion(field.schema) && field.allowedValues.isEmpty()) {
                return "Can't perform this transition because '" + field.name
                        + "' field is required, but the project doesn't have any versions.";
            }
        }
        return null;
    }

    public static class FieldDto {

        public final String id;
        public final String name;
        public final boolean required;
        public final JiraCreateIssue.FieldSchema schema;
        public final List<Object> allowedValues;

        public FieldDto(String id, String name, boolean required, JiraCreateIssue.FieldSchema schema, List<Object> allowedValues) {
            this.id = id;
            this.name = name;
            this.required = required;
            this.schema = schema;
            this.allowedValues = allowedValues;
        }

        public static FieldDto from(JiraCreateIssue.FieldInfoMetadata field, FieldsRequiredInTransition fieldsRequiredInTransition) {
            boolean required = Optional.ofNullable(fieldsRequiredInTransition)
                    .map(frt -> frt.requiredFields.contains(field.id))
                    .orElse(field.required);
            return new FieldDto(field.id, field.name, required, field.schema, field.getAllowedValues());
        }
    }
}
