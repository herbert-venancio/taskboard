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
    private CumulativeFlowDiagramProperties CumulativeFlowDiagram = new CumulativeFlowDiagramProperties();

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
        return CumulativeFlowDiagram;
    }

    public void setCumulativeFlowDiagram(CumulativeFlowDiagramProperties cumulativeFlowDiagram) {
        CumulativeFlowDiagram = cumulativeFlowDiagram;
    }

}
