package objective.taskboard.data;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Changelog implements Serializable{
    private static final long serialVersionUID = -3285607409955190563L;
    
    public final String author;
    public final String field;
    public final String from;
    public final String to;
    
    public final ZonedDateTime timestamp;//NOSONAR
    public final String originalTo;

    public Changelog(String author, String field, String fromStringVal, String toStringVal, String originalTo, ZonedDateTime timestamp) {
        this.author = author;
        this.field = field;
        this.from = fromStringVal;
        this.to = toStringVal;
        this.timestamp = timestamp;
        this.originalTo = originalTo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((originalTo == null) ? 0 : originalTo.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
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
        Changelog other = (Changelog) obj;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (field == null) {
            if (other.field != null)
                return false;
        } else if (!field.equals(other.field))
            return false;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (originalTo == null) {
            if (other.originalTo != null)
                return false;
        } else if (!originalTo.equals(other.originalTo))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }
}
