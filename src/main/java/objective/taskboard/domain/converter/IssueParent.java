package objective.taskboard.domain.converter;

public class IssueParent {
    private final String key;
    private final long typeId;
    private final String typeIconUrl;

    public IssueParent(String key, long issueTypeId, String issueTypeIconUrl) {
        this.key = key;
        this.typeId = issueTypeId;
        this.typeIconUrl = issueTypeIconUrl;
    }

    public String getKey() {
        return this.key;
    }

    public long getTypeId() {
        return this.typeId;
    }

    public String getTypeIconUrl() {
        return this.typeIconUrl;
    }
}