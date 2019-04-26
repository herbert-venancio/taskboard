package objective.taskboard.monitor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

@RunWith(MockitoJUnitRunner.class)
public class MonitorDataProviderTest {

    @Test
    public void givenProjectHasData_thenExpectedReturnCorrectData() {
        given()
            .projectWithKey("PROJ")
            .projectWithName("Project")
            .withScopeCalculatorLabel("Scope")
            .withTimelineCalculatorLabel("Timeline")
            .withCostCalculatorLabel("Cost")

        .whenExecuteFromProject()

        .then()
            .assertResult(
                    "{" +
                        "projectKey: 'PROJ'," +
                        "projectDisplayName: 'Project'," +
                        "monitors: [" +
                            "{" +
                                "label: 'Scope'," +
                                "items: []" +
                            "}, {" +
                                "label: 'Timeline'," +
                                "items: []" +
                            "}, {" +
                                "label: 'Cost'," +
                                "items: []" +
                            "}" +
                        "]" +
                    "}");
    }

    private MonitorDataProviderTestDSL given() {
        return new MonitorDataProviderTestDSL();
    }

    private class MonitorDataProviderTestDSL {
        private ProjectService projectServiceMock = mock(ProjectService.class);
        private Project projectMock = mock(Project.class);
        private ProjectFilterConfiguration projectConfig = mock(ProjectFilterConfiguration.class);
        private ScopeMonitorCalculator scopeMonitorCalculatorMock = mock(ScopeMonitorCalculator.class);
        private TimelineMonitorCalculator timelineMonitorCalculatorMock = mock(TimelineMonitorCalculator.class);
        private CostMonitorCalculator costMonitorCalculatorMock = mock(CostMonitorCalculator.class);

        private MonitorDataProvider subject = new MonitorDataProvider(projectServiceMock, scopeMonitorCalculatorMock, timelineMonitorCalculatorMock, costMonitorCalculatorMock);
        private StrategicalProjectDataSet dataSet = new StrategicalProjectDataSet();

        private ZoneId timezone = ZoneId.of("America/Sao_Paulo");

        public MonitorDataProviderTestDSL() {
          when(projectServiceMock.getJiraProjectAsUserOrCry(anyString())).thenReturn(projectMock);
          when(timelineMonitorCalculatorMock.calculate(any(), any())).thenReturn(new MonitorData("Timeline Forecast", "#icon-notebook"));
          when(costMonitorCalculatorMock.calculate(any(), any())).thenReturn(new MonitorData("Cost", "#icon-money-bag"));
        }

        public MonitorDataProviderTestDSL projectWithKey(String projectKey) {
            when(projectMock.getKey()).thenReturn(projectKey);
            return this;
        }

        public MonitorDataProviderTestDSL projectWithName(String projectDisplayName) {
            when(projectMock.getName()).thenReturn(projectDisplayName);
            return this;
        }

        public MonitorDataProviderTestDSL withScopeCalculatorLabel(String scopeLabel) {
            when(scopeMonitorCalculatorMock.calculate(any(), any())).thenReturn(new MonitorData(scopeLabel, null));
            return this;
        }

        public MonitorDataProviderTestDSL withTimelineCalculatorLabel (String timelineLabel) {
            when(timelineMonitorCalculatorMock.calculate(any(),  any())).thenReturn(new MonitorData(timelineLabel, null));
            return this;
        }

        public MonitorDataProviderTestDSL withCostCalculatorLabel (String costLabel) {
            when(costMonitorCalculatorMock.calculate(any(), any())).thenReturn(new MonitorData(costLabel, null));
            return this;
        }

        public MonitorDataProviderTestDSL whenExecuteFromProject() {
            dataSet = subject.fromProject(projectConfig, timezone);
            return this;
        }

        public MonitorDataProviderTestDSLAsserter then() {
            return new MonitorDataProviderTestDSLAsserter();
        }

        private class MonitorDataProviderTestDSLAsserter {

            public MonitorDataProviderTestDSLAsserter assertResult(String expectedJson) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                String prettyExpected = gson.toJson(new JsonParser().parse(expectedJson));
                String dataSetJson = gson.toJson(dataSet);

                assertEquals(prettyExpected, dataSetJson);
                return this;
            }

        }

    }
}
