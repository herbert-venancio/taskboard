package objective.taskboard.followup.kpi.properties;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import objective.taskboard.followup.kpi.KpiLevel;

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
