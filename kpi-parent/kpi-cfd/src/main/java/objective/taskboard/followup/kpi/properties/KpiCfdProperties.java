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
public class KpiCfdProperties {

    @Valid
    private CumulativeFlowDiagramProperties cumulativeFlowDiagram = new CumulativeFlowDiagramProperties();

    public CumulativeFlowDiagramProperties getCumulativeFlowDiagram() {
        return cumulativeFlowDiagram;
    }

    public void setCumulativeFlowDiagram(CumulativeFlowDiagramProperties cumulativeFlowDiagram) {
        this.cumulativeFlowDiagram = cumulativeFlowDiagram;
    }
}
