package objective.taskboard.followup;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FromJiraDataSet {
    public List<String> headers;
    public Map<String, Set<String>> extraFieldsHeaders;
    public List<FromJiraDataRow> rows;

    public FromJiraDataSet(List<String> headers, List<FromJiraDataRow> rows) {
        this(headers, emptyMap(), rows);
    }

    public FromJiraDataSet(List<String> headers, Map<String, Set<String>> extraFieldsHeaders, List<FromJiraDataRow> rows) {
        this.headers = headers;
        this.extraFieldsHeaders = extraFieldsHeaders;
        this.rows = rows;
    }
}
