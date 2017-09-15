package objective.taskboard.data;

import org.joda.time.DateTime;

public class Changelog {

    public final String author;
    public final String field;
    public final String from;
    public final String to;
    public final DateTime timestamp;

    public Changelog(String author, String field, String from, String to, DateTime timestamp) {
        this.author = author;
        this.field = field;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }
}
