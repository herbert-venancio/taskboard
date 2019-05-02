package objective.taskboard.domain.converter;

import static objective.taskboard.domain.converter.StartDateStepServiceTestDSL.withFilterFor;
import static objective.taskboard.domain.converter.StartDateStepServiceTestDSL.withStage;
import static objective.taskboard.domain.converter.StartDateStepServiceTestDSL.withStep;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.testUtils.JsonBuilder;
import objective.taskboard.testUtils.JsonBuilder.JsonObjectBuilder;
import objective.taskboard.testUtils.JsonBuilder.JsonObjectPropertyBuilder;

public class StartDateStepServiceTest {

    private StartDateStepServiceTestDSL dsl;

    @Before
    public void setupMocks() {
        BiMap<Long, String> issueTypeMap = ImmutableBiMap.<Long, String>builder()
                .put(7L, "Demanda")
                .build();
        BiMap<Long, String> statusMap = ImmutableBiMap.<Long, String>builder()
                .put(1L, "Aberto")
                .put(10151L, "OG")
                .put(10152L, "Planejamento")
                .build();
        dsl = new StartDateStepServiceTestDSL(issueTypeMap, statusMap);
    }

    @Test
    public void givenIssueMovedBetweenStatusOnSameStep_whenCalculateStartDate_thenDateIsFirstTimeItEnteredBallparkStep() {
        givenLane("Demand"
                , withStage("Ballpark"
                        , withStep("Ballpark"
                                , withFilterFor().issueType("Demanda").status("Aberto")
                                , withFilterFor().issueType("Demanda").status("OG")
                        )
                )
                , withStage("Planning"
                        , withStep("Planning"
                                , withFilterFor().issueType("Demanda").status("Planejamento")
                        )
                )
        );

        JiraIssueDto issue = givenJiraIssueDto(
                withId(10000L)
                , withKey("TEST-1000")
                , withFields(
                        withStatus("OG")
                        , withIssueType("Demanda")
                        , withCreated("2018-08-30T14:01:40Z")
                        , withUpdated("2019-01-21T19:46:26Z")
                )
                , withChangeLog(
                        movedFrom("Aberto").to("OG").on("2018-09-03T19:31:46Z")
                        , movedFrom("OG").to("Aberto").on("2018-09-03T19:32:05Z")
                        , movedFrom("Aberto").to("OG").on("2019-01-09T12:18:04Z")
                )
        );

        // when
        long value = dsl.startDateStepService.get(issue);

        // then
        assertThat(Instant.ofEpochMilli(value)).isEqualTo(Instant.parse("2018-09-03T19:31:46Z"));
    }

    @Test
    public void givenIssueMovedToPlanningStepThenBackToBallparkStep_whenCalculateStartDate_thenDateIsLastTimeItEnteredBallparkStep() {
        givenLane("Demand"
                , withStage("Ballpark"
                        , withStep("Ballpark"
                                , withFilterFor().issueType("Demanda").status("Aberto")
                                , withFilterFor().issueType("Demanda").status("OG")
                        )
                )
                , withStage("Planning"
                        , withStep("Planning"
                                , withFilterFor().issueType("Demanda").status("Planejamento")
                        )
                )
        );

        JiraIssueDto issue = givenJiraIssueDto(
                withId(10000L)
                , withKey("TEST-1000")
                , withFields(
                        withStatus("OG")
                        , withIssueType("Demanda")
                        , withCreated("2018-08-30T14:01:40Z")
                        , withUpdated("2019-01-21T19:46:26Z")
                )
                , withChangeLog(
                        movedFrom("Aberto").to("OG").on("2018-09-03T19:31:46Z")
                        , movedFrom("OG").to("Planejamento").on("2018-09-03T19:32:05Z")
                        , movedFrom("Planejamento").to("OG").on("2019-01-09T12:18:04Z")
                )
        );

        // when
        long value = dsl.startDateStepService.get(issue);

        // then
        assertThat(Instant.ofEpochMilli(value)).isEqualTo(Instant.parse("2019-01-09T12:18:04Z"));
    }

    private void givenLane(String laneName, StartDateStepServiceTestDSL.StageBuilder... stages) {
        dsl.givenLane(laneName, stages);
    }

    private JiraIssueDto givenJiraIssueDto(JsonObjectPropertyBuilder... builders) {
        return new JiraIssueDtoBuilder(builders).build();
    }

    private JsonObjectPropertyBuilder withId(long id) {
        return JsonBuilder.property("id", id);
    }

    private JsonObjectPropertyBuilder withKey(String key) {
        return JsonBuilder.property("key", key);
    }

    private JsonObjectPropertyBuilder withFields(JsonObjectPropertyBuilder... builders) {
        return JsonBuilder.property("fields", JsonBuilder.object(builders));
    }

    private JsonObjectPropertyBuilder withCreated(String instant) {
        long created = Instant.parse(instant).toEpochMilli();
        return JsonBuilder.property("created", created);
    }

    private JsonObjectPropertyBuilder withUpdated(String instant) {
        long updated = Instant.parse(instant).toEpochMilli();
        return JsonBuilder.property("updated", updated);
    }

    private JsonObjectPropertyBuilder withStatus(String status) {
        return dsl.withStatus(status);
    }

    private JsonObjectPropertyBuilder withIssueType(String issueType) {
        return dsl.withIssueType(issueType);
    }

    private JsonObjectPropertyBuilder withChangeLog(StartDateStepServiceTestDSL.ChangelogItemBuilder... builders) {
        return JsonBuilder.property("changelog"
                , JsonBuilder.object(JsonBuilder.property("histories", JsonBuilder.array(builders))));
    }

    private StartDateStepServiceTestDSL.ChangelogItemBuilder movedFrom(String statusName) {
        return dsl.movedFrom(statusName);
    }

    private static class JiraIssueDtoBuilder {

        private final JsonObjectBuilder jsonBuilder;

        public JiraIssueDtoBuilder(JsonObjectPropertyBuilder[] builders) {
            jsonBuilder = JsonBuilder.object(builders);
        }

        public JiraIssueDto build() {
            return jsonBuilder.as(JiraIssueDto.class);
        }

    }

}
