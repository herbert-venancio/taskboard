package objective.taskboard.rules;

import java.util.TimeZone;

import org.junit.rules.ExternalResource;

/**
 * Sets default time zone for tests, and restore system-default afterwards
 */
public class TimeZoneRule extends ExternalResource {

    private final TimeZone targetTimeZone;
    private final TimeZone defaultTimeZone;

    public TimeZoneRule(String id) {
        this(TimeZone.getTimeZone(id));
    }

    public TimeZoneRule(TimeZone timeZone) {
        targetTimeZone = timeZone;
        defaultTimeZone = TimeZone.getDefault();
    }

    @Override
    protected void before() throws Throwable {
        TimeZone.setDefault(targetTimeZone);
    }

    @Override
    protected void after() {
        TimeZone.setDefault(defaultTimeZone);
    }
}
