package objective.taskboard.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tomcat.util.buf.StringUtils;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import objective.taskboard.data.Issue.CardTeam;
import objective.taskboard.domain.converter.IssueTeamService;

public class IssueTest {

    @Test
    public void ensureAllScratchFieldsAreCopied() throws IllegalArgumentException, IllegalAccessException {
        IssueScratch issueScratch = makeIssueScratch();

        Issue subject = new Issue(issueScratch, null, null, null, null, null, null, null, null, null);

        Field[] declaredFields = IssueScratch.class.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            assertEquals(field.get(issueScratch), field.get(subject));
        }
    }

    private IssueScratch makeIssueScratch(Long ...teamId)  {
        LinkedList<Long> assignedTeamsIds = new LinkedList<>(Arrays.asList(teamId));

        IssueScratch issueScratch = new IssueScratch(
                66l,
                "K-66",
                "K",
                "A project",
                1l,
                "summary",
                2l,
                3000l,
                "K", 
                Arrays.asList("x","y"),
                Arrays.asList("a","b"),
                Arrays.asList(new User("subresponsaveis")), 
                new User("assignee"),
                13l,
                new Date(),
                6000l,
                new Date(),
                "description",
                Collections.emptyList(),
                Arrays.asList("labels"),
                Arrays.asList("components"),
                false,
                "lastBlockReason",
                new LinkedHashMap<>(),
                new CustomField("id", 1.0),
                new TaskboardTimeTracking(),
                "reporter",
                new CustomField("classOfService", null),
                "releaseId",
                new LinkedList<>(),
                new LinkedList<>(),
                assignedTeamsIds,
                new LinkedHashMap<>(),
                new LinkedList<>()
                );

        Issue subject = new Issue(issueScratch, null, null, null, null, null, null, null, null, null);

        Field[] declaredFields = IssueScratch.class.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                assertEquals(field.get(issueScratch), field.get(subject));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return issueScratch;
    }

    @Test
    public void addTeam_IfAddingATeamThatIsNotTheDefault_MakeSureTheDefaultIsIncludedInTheNextResult() {
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeamId(any())).thenReturn(1L);
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.empty());
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Team teamToAdd = new Team();
        teamToAdd.setId(13L);
        subject.addTeam(teamToAdd );
        assertEquals("1,13", subject.getRawAssignedTeamsIds().stream().sorted().map(s->""+s).collect(Collectors.joining(",")));
    }

    @Test
    public void addTeam_IfAddingATeamThatIsNotTheDefault_MakeSureTheParentTeamIsIncludedInTheNextResult() {
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeamId(any())).thenReturn(1L);
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.empty());
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Issue parent = new Issue(makeIssueScratch(37L), null, null, is, null, null, null, null, null, null);
        subject.setParentCard(parent);
        Team teamToAdd = new Team();
        teamToAdd.setId(13L);
        subject.addTeam(teamToAdd);
        assertEquals("37,13", subject.getRawAssignedTeamsIds().stream().map(s->""+s).collect(Collectors.joining(",")));
    }

    @Test
    public void addTeam_IfAddingATeamThatIsNotTheDefault_MakeSureTheTeamByIssueTypeIsIncludedInTheNextResult() {
        long teamIdByIssueType = 10L;
        long teamToAddId = 13L;

        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.of(new CardTeam("Team 1", teamIdByIssueType)));
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Team teamToAdd = new Team();

        teamToAdd.setId(teamToAddId);
        subject.addTeam(teamToAdd);
        assertEquals(teamIdByIssueType+","+teamToAddId, subject.getRawAssignedTeamsIds().stream().map(s->""+s).collect(Collectors.joining(",")));
    }

    @Test
    public void removeTeam_IfRemovingInheritedTeam_setTheTeamListToBeTheInheritedMinusTheRemoved() {
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeamId(any())).thenReturn(1L);
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.empty());
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Issue parent = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        subject.setParentCard(parent);
        Team teamToAdd = new Team();
        teamToAdd.setId(37L);
        parent.addTeam(teamToAdd);
        Team teamToRemove = new Team();
        teamToRemove.setId(1L);
        subject.removeTeam(teamToRemove);
        assertEquals("37", subject.getRawAssignedTeamsIds().stream().map(s->""+s).collect(Collectors.joining(",")));
    }
    
    @Test
    public void replaceTeam_ShouldReplaceAnExistingTeamByAnother() {
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeamId(any())).thenReturn(1L);
        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Team replacementTeam = new Team();
        replacementTeam.setId(13L);
        Team teamToReplace = new Team();
        teamToReplace.setId(1L);
        subject.replaceTeam(Optional.of(teamToReplace), replacementTeam);
        
        assertEquals("13", subject.getRawAssignedTeamsIds().stream().sorted().map(s->""+s).collect(Collectors.joining(",")));
    }

    @Test
    public void addTeamThatIsNotTheDefault_ShouldNotReadTheDefault() {
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeamId(any())).thenReturn(1L);

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Team replacementTeam = new Team();
        replacementTeam.setId(13L);
        Team teamToReplace = new Team();
        teamToReplace.setId(1L);
        subject.replaceTeam(Optional.of(teamToReplace), replacementTeam);

        Team anotherTeam = new Team();
        anotherTeam.setId(3L);
        subject.addTeam(anotherTeam );

        assertEquals("3,13", subject.getRawAssignedTeamsIds().stream().sorted().map(s->""+s).collect(Collectors.joining(",")));
    }

    @Test
    public void whenGetTeams_ShouldReturnTeamNamesForIdsInTeamCustomField(){
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getTeamsForIds(Arrays.asList(37L))).thenReturn(Sets.newSet(new CardTeam("bravo1337", 37L)));
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(37L), null, null, is, null, null, null, null, null, null);

        List<String> issueTeams = toStringSet(subject.getTeams());
        assertEquals("bravo1337", StringUtils.join(issueTeams));
    }

    @Test
    public void whenIssueHasNoTeamNoTeamByIssueTypeAndNoParent_ShouldReturnDefaultProjectTeam(){
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeam(any())).thenReturn(new CardTeam("default project team", 1l));
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.empty());
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);

        List<String> issueTeams = toStringSet(subject.getTeams());
        assertEquals("default project team", StringUtils.join(issueTeams));
        assertTrue(subject.isUsingDefaultTeam());
        assertFalse(subject.isUsingParentTeam());
        assertFalse(subject.isUsingTeamByIssueType());
    }

    @Test
    public void whenIssueHasNoTeamButHasTeamByIssueTypeAndHasParentWithTeam_ShouldReturnParentTeam(){
        IssueTeamService is = mock(IssueTeamService.class);

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Issue parent = new Issue(makeIssueScratch(37L), null, null, is, null, null, null, null, null, null);
        subject.setParentCard(parent);

        when(is.getTeamsForIds(Arrays.asList(37L))).thenReturn(Sets.newSet(new CardTeam("bravo1337", 37L)));
        when(is.getTeamsForIds(Arrays.asList())).thenReturn(Sets.newSet());
        when(is.getCardTeamByIssueType(subject)).thenReturn(Optional.of(new CardTeam("bravo1337", 37L)));
        when(is.getCardTeamByIssueType(parent)).thenReturn(Optional.empty());
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        List<String> issueTeams = toStringSet(subject.getTeams());
        assertEquals("bravo1337", StringUtils.join(issueTeams));

        assertFalse(subject.isUsingDefaultTeam());
        assertTrue(subject.isUsingParentTeam());
        assertFalse(subject.isUsingTeamByIssueType());
    }

    @Test
    public void whenIssueHasNoTeamButHasTeamByIssueTypeAndHasParentWithoutTeam_ShouldReturnTheTeamByIssueType(){
        IssueTeamService is = mock(IssueTeamService.class);

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Issue parent = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        subject.setParentCard(parent);

        when(is.getCardTeamByIssueType(subject)).thenReturn(Optional.of(new CardTeam("bravo1337", 37L)));
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        List<String> issueTeams = toStringSet(subject.getTeams());
        assertEquals("bravo1337", StringUtils.join(issueTeams));

        assertFalse(subject.isUsingDefaultTeam());
        assertFalse(subject.isUsingParentTeam());
        assertTrue(subject.isUsingTeamByIssueType());
    }

    @Test
    public void whenIssueHasNoTeamNoTeamByIssueTypeAndHasParentWithoutTeam_ShouldReturnDefaultTeam(){
        IssueTeamService is = mock(IssueTeamService.class);
        when(is.getDefaultTeam(any())).thenReturn(new CardTeam("default project team", 1l));
        when(is.getCardTeamByIssueType(any())).thenReturn(Optional.empty());
        when(is.resolveTeams(any())).thenCallRealMethod();
        when(is.resolveTeamsOrigin(any())).thenCallRealMethod();

        Issue subject = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        Issue parent = new Issue(makeIssueScratch(), null, null, is, null, null, null, null, null, null);
        subject.setParentCard(parent);

        List<String> issueTeams = toStringSet(subject.getTeams());
        assertEquals("default project team", StringUtils.join(issueTeams));

        assertTrue(subject.isUsingDefaultTeam());
        assertFalse(subject.isUsingParentTeam());
        assertFalse(subject.isUsingTeamByIssueType());
    }

    @Test
    public void testIssueKey() {
        assertTrue(Issue.compareIssueKey("PROJ-1", "PROJ-2") < 0);
        assertTrue(Issue.compareIssueKey("PROJ-2", "PROJ-1") > 0);
        assertTrue(Issue.compareIssueKey("PROJ-1", "PROJ-1") == 0);
        assertTrue(Issue.compareIssueKey("PROJ-10", "PROJ-2") > 0);
        assertTrue(Issue.compareIssueKey("AROJ-10", "PROJ-2") < 0);
        assertTrue(Issue.compareIssueKey("PROJ3-10", "PROJ3-2") > 0);
        assertTrue(Issue.compareIssueKey("PROJ-3-10", "PROJ-3-2") > 0);
    }

    private List<String> toStringSet(Set<CardTeam> s) {
        return s.stream().map(c->c.name).sorted().collect(Collectors.toList());
    }
}