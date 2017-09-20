package objective.taskboard.data;

import java.time.ZonedDateTime;

public class Changelog {

    public final String author;
    public final String field;
    public final String from;
    public final String to;
    public final ZonedDateTime timestamp;

    public Changelog(String author, String field, String from, String to, ZonedDateTime timestamp) {
        this.author = author;
        this.field = field;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }
}
