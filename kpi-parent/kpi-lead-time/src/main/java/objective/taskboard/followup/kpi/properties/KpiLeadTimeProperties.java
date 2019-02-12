package objective.taskboard.followup.kpi.properties;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("kpi")
@PropertySource("classpath:kpi.properties")
@PropertySource(value="file:./kpi.properties", ignoreResourceNotFound=true)
@Validated
public class KpiLeadTimeProperties {

    @Valid
    private LeadTimeProperties leadTime = new LeadTimeProperties();

    public LeadTimeProperties getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(LeadTimeProperties leadTime) {
        this.leadTime = leadTime;
    }

}
