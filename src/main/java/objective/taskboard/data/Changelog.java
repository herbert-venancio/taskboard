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
}
