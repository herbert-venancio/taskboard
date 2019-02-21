package objective.taskboard.utils;

import objective.taskboard.jira.client.JiraCreateIssue;

public class TransitionUtils {

    public static boolean isSupported(JiraCreateIssue.FieldSchema schema) {
        return isArrayOfVersion(schema);
    }

    public static boolean isArrayOfVersion(JiraCreateIssue.FieldSchema schema) {
        return schema != null
                && schema.type == JiraCreateIssue.FieldSchemaType.array
                && schema.items == JiraCreateIssue.FieldSchemaType.version;
    }
}
