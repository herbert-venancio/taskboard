package objective.taskboard.followup;

import com.google.common.collect.Sets;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.followup.impl.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

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
    }

    @Test
    public void whenGenerateSubTaskCfdData_dataGroupsShouldBeCorrect() {
        // given
        AnalyticsTransitionsDataSet subTaskAnalytics = followupData.analyticsTransitionsDsList.get(2);
        Date minDate = getMinDate(subTaskAnalytics);
        Date maxDate = getMaxDate(subTaskAnalytics);

        // when
        CumulativeFlowDiagramDataSet cfd = subject.getCumulativeFlowDiagramDataSet("TASKB");

        // then
        assertThat(cfd.lanes).containsExactly(TYPE_SUBTASKS);
        assertThat(cfd.types).containsExactlyInAnyOrder("Development", "Review", "Sub-Task");
        assertThat(cfd.labels).containsExactly("To Do", "Doing", "Reviewing", "UAT", "Done");
        assertThat(cfd.dates).first().isEqualTo(minDate);
        assertThat(cfd.dates).last().isEqualTo(maxDate);
    }

    @Test
    public void whenGenerateSubTaskCfdData_dataIndicesShouldBeInsideRanges() {
        // when
        CumulativeFlowDiagramDataSet cfd = subject.getCumulativeFlowDiagramDataSet("TASKB");

        // then
        assertThat(cfd.data).allMatch(d -> d.lane >= 0 && d.lane < cfd.lanes.size());
        assertThat(cfd.data).allMatch(d -> d.type >= 0 && d.type < cfd.types.size());
        assertThat(cfd.data).allMatch(d -> d.label >= 0 && d.label < cfd.labels.size());
        assertThat(cfd.data).allMatch(d -> d.index >= 0 && d.index < cfd.dates.size());
    }

    @Test
    public void whenGenerateSubTaskCfdData_dataShouldBeOrdered() {
        // when
        CumulativeFlowDiagramDataSet cfd = subject.getCumulativeFlowDiagramDataSet("TASKB");

        // then
        for(Pair<String, String> entry : cartesianProduct(cfd.labels, cfd.types)) {
            String label = entry.getLeft();
            String type = entry.getRight();

            List<CumulativeFlowDiagramDataPoint> filteredDataPoints = cfd.data.stream()
                    .filter(d -> type.equals(cfd.types.get(d.type)))
                    .filter(d -> label.equals(cfd.labels.get(d.label)))
                    .collect(toList());

            // index should be sequential
            int index = 0;
            for(CumulativeFlowDiagramDataPoint d : filteredDataPoints) {
                assertThat(d.index).isEqualTo(index++);
            }

            // count should always increase
            int issueCount = 0;
            for(CumulativeFlowDiagramDataPoint d : filteredDataPoints) {
                assertThat(d.count).isGreaterThanOrEqualTo(issueCount);
                issueCount = d.count;
            }
        }
    }

    @Test
    public void whenGenerateSubTaskCfdData_maxCountForInitialStatusShouldBeEqualIssueCount() {
        // given
        AnalyticsTransitionsDataSet subTaskAnalytics = followupData.analyticsTransitionsDsList.get(2);
        String initialStatus = "To Do";

        // when
        CumulativeFlowDiagramDataSet cfd = subject.getCumulativeFlowDiagramDataSet("TASKB");

        // then
        int developmentCount = (int) subTaskAnalytics.rows.stream().filter(row -> "Development".equals(row.issueType)).count();
        int reviewCount = (int) subTaskAnalytics.rows.stream().filter(row -> "Review".equals(row.issueType)).count();
        int subTaskCount = (int) subTaskAnalytics.rows.stream().filter(row -> "Sub-Task".equals(row.issueType)).count();

        Map<String, Integer> issueCountByType = new HashMap<>();
        issueCountByType.put("Development", developmentCount);
        issueCountByType.put("Review", reviewCount);
        issueCountByType.put("Sub-Task", subTaskCount);

        for(Map.Entry<String, Integer> entry : issueCountByType.entrySet()) {
            String type = entry.getKey();
            int expectedCount = entry.getValue();
            int actualCount = cfd.data.stream()
                    .filter(d -> type.equals(cfd.types.get(d.type)))
                    .filter(d -> initialStatus.equals(cfd.labels.get(d.label)))
                    .mapToInt(d -> d.count)
                    .max().orElseThrow(
                            () -> new AssertionError(String.format("Count of '%s' issues in '%s' status should exists", type, initialStatus)));
            assertThat(actualCount).isEqualTo(expectedCount);
        }
    }

    @Test
    public void invalidProject() {
        try {
            subject.getCumulativeFlowDiagramDataSet("INVALID");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Unknown project <INVALID>");
        }
    }

    private Date getMinDate(AnalyticsTransitionsDataSet analytics) {
        ZonedDateTime min = null;
        for(AnalyticsTransitionsDataRow row : analytics.rows) {
            for(ZonedDateTime date : row.transitionsDates) {
                if(date == null)
                    continue;
                if(min == null || date.isBefore(min))
                    min = date;
            }
        }
        return min == null ? null : Date.from(min.toInstant());
    }

    private Date getMaxDate(AnalyticsTransitionsDataSet analytics) {
        ZonedDateTime max = null;
        for(AnalyticsTransitionsDataRow row : analytics.rows) {
            for(ZonedDateTime date : row.transitionsDates) {
                if(date == null)
                    continue;
                if(max == null || date.isAfter(max))
                    max = date;
            }
        }
        return max == null ? null : Date.from(max.toInstant());
    }

    private Iterable<Pair<String, String>> cartesianProduct(List<String> labels, List<String> types) {
        List<Set<String>> sets = asList(
                newLinkedHashSet(labels)
                , newLinkedHashSet(types)
        );
        return Sets.cartesianProduct(sets)
                .stream().map(entry -> Pair.of(entry.get(0), entry.get(1)))
                .collect(toList());
    }
}
