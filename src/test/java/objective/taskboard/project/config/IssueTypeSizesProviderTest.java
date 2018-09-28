package objective.taskboard.project.config;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.BallparkMapping;
import objective.taskboard.jira.properties.JiraProperties.CustomField;
import objective.taskboard.jira.properties.JiraProperties.CustomField.TShirtSize;
import objective.taskboard.jira.properties.JiraProperties.Followup;
import objective.taskboard.project.config.IssueTypeSizesProvider.IssueTypeSize;

@RunWith(MockitoJUnitRunner.class)
public class IssueTypeSizesProviderTest {

    @Mock
    private JiraProperties jiraProperties;

    @Mock
    private MetadataService metadataService;

    @InjectMocks
    private IssueTypeSizesProvider issueTypeProvider;

    @Before
    public void setUp() {
        CustomField customfield = new CustomField();
        customfield.setTShirtSize(new TShirtSize());
        when(jiraProperties.getCustomfield()).thenReturn(customfield);
    }

    @Test
    public void shouldReturnTwoIssueTypes_andTwoJiraIssueTypesFromBallParks() {
        followup(
                ballparkMappings(
                        10104L,
                        ballpark("BALLPARK - Planning", 10105L, 10110L),
                        ballpark("BALLPARK - UX", 10112L))
                );

        jiraIssueTypes(
                jiraIssueType(10105L, "Feature Planning"),
                jiraIssueType(10110L, "Tech Planning")
                );

        List<IssueTypeSize> issueTypes = issueTypeProvider.get();

        assertIssuesTypesSizes(issueTypes,
                "BALLPARK - Planning | XS",
                "BALLPARK - Planning | S",
                "BALLPARK - Planning | M",
                "BALLPARK - Planning | L",
                "BALLPARK - Planning | XL",
                "BALLPARK - UX | XS",
                "BALLPARK - UX | S",
                "BALLPARK - UX | M",
                "BALLPARK - UX | L",
                "BALLPARK - UX | XL",
                "Feature Planning | XS",
                "Feature Planning | S",
                "Feature Planning | M",
                "Feature Planning | L",
                "Feature Planning | XL",
                "Tech Planning | XS",
                "Tech Planning | S",
                "Tech Planning | M",
                "Tech Planning | L",
                "Tech Planning | XL"
                );
    }

    private void assertIssuesTypesSizes(final List<IssueTypeSize> issueTypes, final String...expetedItems) {
        String expected = StringUtils.join(expetedItems, "\n");
        String current = issueTypes.stream()
                .map(i -> i.getIssueType() + " | " + i.getSize())
                .collect(Collectors.joining("\n"));

        assertEquals(expected, current);
    }

    private JiraIssueTypeDto jiraIssueType(final long id, final String name) {
        return new JiraIssueTypeDto(id, name, false);
    }

    private void jiraIssueTypes(final JiraIssueTypeDto...issueTypes) {
        HashMap<Long, JiraIssueTypeDto> jiraIssueTypes = new HashMap<>();
        asList(issueTypes).forEach(i -> jiraIssueTypes.put(i.getId(), i));

        when(metadataService.getIssueTypeMetadata()).thenReturn(jiraIssueTypes);
    }

    private BallparkMapping ballpark(final String issueType, final Long...jiraIssueTypes) {
        BallparkMapping ballparkTwo = new BallparkMapping();
        ballparkTwo.setIssueType(issueType);
        ballparkTwo.setJiraIssueTypes(asList(jiraIssueTypes));
        return ballparkTwo;
    }

    private Map<Long, List<BallparkMapping>> ballparkMappings(final long issueTypeId, BallparkMapping...ballparksMapping) {
        Map<Long, List<BallparkMapping>> ballparksMappings = new HashMap<Long, List<BallparkMapping>>();
        ballparksMappings.put(issueTypeId, asList(ballparksMapping));
        return ballparksMappings;
    }

    private void followup(Map<Long, List<BallparkMapping>> ballparksMappings) {
        Followup followup = new Followup();
        followup.setBallparkMappings(ballparksMappings);
        when(jiraProperties.getFollowup()).thenReturn(followup);
    }
}
