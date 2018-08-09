package objective.taskboard.data;

import java.io.Serializable;

public class Subtask implements Serializable {
    private static final long serialVersionUID = -1394125985489540555L;
    public final String issueTypeUri;
    public final String issueKey;
    public final String summary;
    public final String status;
    public final String color;
    public final String issueType;
    
    public Subtask(String key, String summary, String status, String issueType, String issueTypeUri, String color) {
        this.issueKey = key;
        this.summary = summary;
        this.status = status;
        this.issueType = issueType;
        this.issueTypeUri = issueTypeUri;
        this.color = color;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((issueKey == null) ? 0 : issueKey.hashCode());
        result = prime * result + ((issueType == null) ? 0 : issueType.hashCode());
        result = prime * result + ((issueTypeUri == null) ? 0 : issueTypeUri.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Subtask other = (Subtask) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (issueKey == null) {
            if (other.issueKey != null)
                return false;
        } else if (!issueKey.equals(other.issueKey))
            return false;
        if (issueType == null) {
            if (other.issueType != null)
                return false;
        } else if (!issueType.equals(other.issueType))
            return false;
        if (issueTypeUri == null) {
            if (other.issueTypeUri != null)
                return false;
        } else if (!issueTypeUri.equals(other.issueTypeUri))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (summary == null) {
            if (other.summary != null)
                return false;
        } else if (!summary.equals(other.summary))
            return false;
        return true;
    }
}
