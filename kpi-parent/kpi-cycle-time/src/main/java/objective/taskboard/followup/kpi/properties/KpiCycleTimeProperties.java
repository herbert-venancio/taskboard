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
public class KpiCycleTimeProperties {

    @Valid
    private CycleTimeProperties cycleTime;

    public CycleTimeProperties getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(CycleTimeProperties cycleTime) {
        this.cycleTime = cycleTime;
    }
}
