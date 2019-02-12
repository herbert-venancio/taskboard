package objective.taskboard.followup.kpi.properties;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("kpi")
@PropertySource("classpath:kpi.properties")
@Validated
public class KPIProperties {

    @NotNull
    @Valid
    private IssueTypeChildrenStatusHierarchy featuresHierarchy;

    @NotNull
    @Valid
    private IssueTypeChildrenStatusHierarchy demandHierarchy;

    @NotEmpty
    @Valid
    private List<String> progressingStatuses;

    @Valid
    private CumulativeFlowDiagramProperties cumulativeFlowDiagram = new CumulativeFlowDiagramProperties();

    @NotEmpty
    @Valid
    private List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs;

    @Valid
    private KpiCycleTimeProperties cycleTime;

    @Valid
    private KpiLeadTimeProperties leadTime = new KpiLeadTimeProperties();

    public KpiLeadTimeProperties getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(KpiLeadTimeProperties leadTime) {
        this.leadTime = leadTime;
    }

    public KpiCycleTimeProperties getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(KpiCycleTimeProperties cycleTime) {
        this.cycleTime = cycleTime;
    }

    public IssueTypeChildrenStatusHierarchy getFeaturesHierarchy() {
        return featuresHierarchy;
    }

    public void setFeaturesHierarchy(IssueTypeChildrenStatusHierarchy featuresHierarchy) {
        this.featuresHierarchy = featuresHierarchy;
    }

    public List<String> getProgressingStatuses() {
        return progressingStatuses;
    }

    public void setProgressingStatuses(List<String> progressingStatuses) {
        this.progressingStatuses = progressingStatuses;
    }

    public IssueTypeChildrenStatusHierarchy getDemandHierarchy() {
        return demandHierarchy;
    }

    public void setDemandHierarchy(IssueTypeChildrenStatusHierarchy demandHierarchy) {
        this.demandHierarchy = demandHierarchy;
    }

    public CumulativeFlowDiagramProperties getCumulativeFlowDiagram() {
        return cumulativeFlowDiagram;
    }

    public void setCumulativeFlowDiagram(CumulativeFlowDiagramProperties cumulativeFlowDiagram) {
        this.cumulativeFlowDiagram = cumulativeFlowDiagram;
    }

    public List<TouchTimeSubtaskConfiguration> getTouchTimeSubtaskConfigs() {
        return touchTimeSubtaskConfigs;
    }

    public void setTouchTimeSubtaskConfigs(List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs) {
        this.touchTimeSubtaskConfigs = touchTimeSubtaskConfigs;
    }

}
