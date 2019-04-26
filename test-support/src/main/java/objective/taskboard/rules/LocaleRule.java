package objective.taskboard.rules;

import java.util.Locale;

import org.junit.rules.ExternalResource;

public class LocaleRule extends ExternalResource {

    private final Locale targetLocale;
    private final Locale defaultLocale;

    public LocaleRule(String id) {
        this(Locale.forLanguageTag(id));
    }

    public LocaleRule(Locale timeZone) {
        targetLocale = timeZone;
        defaultLocale = Locale.getDefault();
    }

    @Override
    protected void before() throws Throwable {
        Locale.setDefault(targetLocale);
    }

    @Override
    protected void after() {
        Locale.setDefault(defaultLocale);
    }
}