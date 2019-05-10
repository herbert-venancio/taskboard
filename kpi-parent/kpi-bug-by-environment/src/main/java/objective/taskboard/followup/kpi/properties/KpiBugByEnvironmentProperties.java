package objective.taskboard.followup.kpi.properties;

import java.util.LinkedList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("kpi")
@PropertySource("classpath:kpi.properties")
@PropertySource(value="file:./kpi.properties", ignoreResourceNotFound=true)
@Validated
public class KpiBugByEnvironmentProperties {

    private List<Long> bugTypes = new LinkedList<>();

    public List<Long> getBugTypes() {
        return bugTypes;
    }

    public void setBugTypes(List<Long> bugTypes) {
        this.bugTypes = bugTypes;
    }
}
