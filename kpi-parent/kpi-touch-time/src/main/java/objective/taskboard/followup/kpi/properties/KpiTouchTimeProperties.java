package objective.taskboard.followup.kpi.properties;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("kpi")
@PropertySource("classpath:kpi.properties")
@PropertySource(value="file:./kpi.properties", ignoreResourceNotFound=true)
@Validated
public class KpiTouchTimeProperties {

    @NotEmpty
    @Valid
    private List<String> progressingStatuses;

    @NotEmpty
    @Valid
    private List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs;

    public List<String> getProgressingStatuses() {
        return progressingStatuses;
    }

    public void setProgressingStatuses(List<String> progressingStatuses) {
        this.progressingStatuses = progressingStatuses;
    }

    public List<TouchTimeSubtaskConfiguration> getTouchTimeSubtaskConfigs() {
        return touchTimeSubtaskConfigs;
    }

    public void setTouchTimeSubtaskConfigs(List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs) {
        this.touchTimeSubtaskConfigs = touchTimeSubtaskConfigs;
    }

}
