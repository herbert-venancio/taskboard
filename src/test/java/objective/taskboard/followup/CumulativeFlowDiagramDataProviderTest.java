package objective.taskboard.followup;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tomcat.util.buf.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.Constants;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class CumulativeFlowDiagramDataProviderTest {

    private static final LocalDate TODAY_DATE = LocalDate.now();

    @Mock
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Mock
    private FollowUpDataProviderFromCurrentState followUpDataProviderFromCurrentState;

    @InjectMocks
    private CumulativeFlowDiagramDataProvider subject;

    private FollowupData followupData;

    @Before
    public void setup() {
        followupData = FollowUpHelper.getBiggerFollowupData();
        FollowUpDataSnapshot snapshot = new FollowUpDataSnapshot(TODAY_DATE, followupData);
        doReturn(snapshot).when(followUpDataProviderFromCurrentState).getJiraData(eq("TASKB"));
        doReturn(true).when(projectRepository).exists(eq("TASKB"));

        FollowupData emptyFollowupData = new FollowupData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, emptyList()), emptyList(), emptySynthetics());
        FollowUpDataSnapshot emptySnapshot = new FollowUpDataSnapshot(TODAY_DATE, emptyFollowupData);
        doReturn(emptySnapshot).when(followUpDataProviderFromCurrentState).getJiraData(eq("EMPTY"));
        doReturn(true).when(projectRepository).exists(eq("EMPTY"));
    }

    private List<SyntheticTransitionsDataSet> emptySynthetics() {
        List<SyntheticTransitionsDataSet> synthetics = FollowUpHelper.getBiggerSyntheticTransitionsDataSet();
        return synthetics
                .stream()
                .map(ds -> new SyntheticTransitionsDataSet(ds.issueType, ds.headers, Collections.emptyList()))
                .collect(toList());
    }

    @Test
    public void whenGenerateSubTaskCfdData_dataShouldBeOrdered() {
        // when
        CumulativeFlowDiagramDataSet cfd = subject.getCumulativeFlowDiagramDataSet("TASKB");
        String[] actualStatus = cfd.dataByStatus.keySet().toArray(new String[0]);
        
        assertEquals("To Do,Doing,Reviewing,UAT,Done", StringUtils.join(actualStatus));
        
        AnalyticsTransitionsDataSet subTaskAnalytics = followupData.analyticsTransitionsDsList.get(2);
        
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<String> allDates = subTaskAnalytics.rows.stream()
                .map(row -> row.transitionsDates)
                .flatMap(r->r.stream())
                .filter(it -> it != null)
                .map(d->d.toLocalDate())
                .distinct()
                .sorted()
                .map(d -> format.format(d))
                .collect(Collectors.toList());
        
        String expectedDates = allDates.toString();
        
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String actualDates = cfd.dataByStatus.get("To Do").stream()
            .map(each -> each.date)
            .map(d -> df.format(d))
            .distinct()
            .sorted()
            .collect(Collectors.toList())
            .toString();
        
        assertEquals(expectedDates, actualDates);
        
        String expectedTypes = subTaskAnalytics.rows.stream()
                .map(row -> row.issueType)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
                .toString();

        String actualTypes = cfd.dataByStatus.get("To Do").stream()
                .map(each -> each.type)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
                .toString();
        
        assertEquals(expectedTypes, actualTypes);
    }

    @Test
    public void invalidProject() {
        try {
            subject.getCumulativeFlowDiagramDataSet("INVALID");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Unknown project <INVALID>", e.getMessage());
        }
    }
}
