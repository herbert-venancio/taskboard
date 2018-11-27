package objective.taskboard.domain.converter;

import java.io.Serializable;

public class IssueCoAssignee implements Serializable {
    private static final long serialVersionUID = -2039913413310229491L;
    private final String name;
    private final String avatarUrl;

    public IssueCoAssignee(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueCoAssignee that = (IssueCoAssignee) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return avatarUrl != null ? avatarUrl.equals(that.avatarUrl) : that.avatarUrl == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IssueMetadata.IssueCoAssignee(name=" + this.getName() + ", avatarUrl=" + this.getAvatarUrl() + ")";
    }
}