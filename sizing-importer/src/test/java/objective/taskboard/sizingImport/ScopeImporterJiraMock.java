package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.OptionalAssert;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.google.gson.Gson;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraIssueDtoFields;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.client.JiraLinkDto;
import objective.taskboard.jira.client.JiraLinkTypeDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;

class ScopeImporterJiraMock {

    private static final Answer NOT_MOCKED_EXCEPTION_ANSWER = invocation -> {
        throw new RuntimeException("not mocked");
    };
    private static final Gson gson = new Gson();

    public final JiraProject.Service projectRest = Mockito.mock(JiraProject.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraCreateIssue.Service createMetadataRest = Mockito.mock(JiraCreateIssue.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraIssue.Service issueRest = Mockito.mock(JiraIssue.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraLinkTypeDto.Service linkTypeRest = Mockito.mock(JiraLinkTypeDto.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);

    private final IdGenerator projectIdGenerator = new IdGenerator();
    private final IdGenerator versionIdGenerator = new IdGenerator();
    private final IdGenerator issueTypeIdGenerator = new IdGenerator();
    private final IdGenerator customFieldIdGenerator = new IdGenerator();
    private final IdGenerator issueIdGenerator = new IdGenerator();
    private final IdGenerator linkTypeIdGenerator = new IdGenerator();

    private final Map<String, ScopeImporterJiraProjectMock> projects = new HashMap<>();
    private final List<ScopeImporterJiraIssueTypeMock> issueTypes = new ArrayList<>();
    private final List<JiraCreateIssue.FieldInfoMetadata> customFields = new ArrayList<>();
    private final List<Triple<JiraLinkTypeDto, ScopeImporterJiraIssueMock, ScopeImporterJiraIssueMock>> links = new ArrayList<>();

    private final List<JiraLinkTypeDto> linkTypes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    ScopeImporterJiraMock() {
        doAnswer(invocation -> {
            String id = invocation.getArgumentAt(0, String.class);
            return Optional.ofNullable(projects.get(id)).map(ScopeImporterJiraProjectMock::asJiraProject).orElse(null);
        }).when(projectRest).get(any());
        doAnswer(invocation -> {
            String projectKey = invocation.getArgumentAt(0, String.class);
            List<Long> issueTypes = invocation.getArgumentAt(1, List.class);

            return Optional.ofNullable(projects.get(projectKey))
                    .map(p -> p.asJiraCreateIssue(issueTypes))
                    .orElse(null);
        }).when(createMetadataRest).getByProjectKey(any(), any());
        doAnswer(invocation -> {
            JiraIssue.Input input = invocation.getArgumentAt(0, JiraIssue.Input.class);

            ScopeImporterJiraProjectMock project = findProject((Map) input.fields.get("project"))
                    .orElse(null);
            String summary = (String) input.fields.get("summary");

            for(Map.Entry<String, Object> entry : input.fields.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
            }

            if(project == null) {
                throw new RuntimeException("project not specified");
            }
            if(summary == null) {
                throw new RuntimeException("summary not specified");
            }

            String key = String.format("%s-%d",
                    project.key,
                    project.issues.keySet().stream()
                            .mapToInt(issueKey -> Integer.valueOf(StringUtils.substringAfter(issueKey, "-")) + 1)
                            .max()
                            .orElse(1));

            ScopeImporterJiraIssueMock issue = project.createIssue(key);
            input.fields.forEach(issue::setField);
            return new JiraIssue(key);
        }).when(issueRest).create(any());
        doAnswer(invocation -> {
            JiraLinkTypeDto.Response response = new JiraLinkTypeDto.Response();
            response.issueLinkTypes = new ArrayList<>(linkTypes);
            return response;
        }).when(linkTypeRest).all();
        doAnswer(invocation -> {
            JiraIssue.LinkInput input = invocation.getArgumentAt(0, JiraIssue.LinkInput.class);
            createLink(input);
            return null;
        }).when(issueRest).linkIssue(any());

        createLinkType("Demand", "is demanded by","demands");
    }

    private Optional<ScopeImporterJiraProjectMock> findProject(Map selector) {
        if(selector == null)
            return Optional.empty();

        return Optional.ofNullable(projects.get(selector.get("key")));
    }
    private Optional<ScopeImporterJiraIssueMock> findIssueByKey(String key) {
        return projects.values().stream()
                .flatMap(project -> project.issues.values().stream())
                .filter(i -> key.equals(i.key))
                .findFirst();
    }

    public OptionalAssert<JiraIssueDto> assertThatIssue(String key) {
        return assertThat(findIssueByKey(key).map(ScopeImporterJiraIssueMock::asJiraIssue));
    }

    public JiraLinkTypeDto createLinkType(String name, String inward, String outward) {
        return findLinkTypeByName(name)
                .orElseGet(() -> {
                    JiraLinkTypeDto link = new JiraLinkTypeDto();
                    link.id = linkTypeIdGenerator.nextAsString();
                    link.name = name;
                    link.inward = inward;
                    link.outward = outward;
                    linkTypes.add(link);
                    return link;
                });
    }

    public ScopeImporterJiraProjectMock createProject(String key, String name) {
        ScopeImporterJiraMock.ScopeImporterJiraProjectMock project = new ScopeImporterJiraMock.ScopeImporterJiraProjectMock(
                this,
                projectIdGenerator.nextAsString(),
                key,
                name);
        projects.put(key, project);
        return project;
    }

    @SuppressWarnings("unchecked")
    private void createLink(JiraIssue.LinkInput input) {
        Map<String, String> typeSelector = (Map<String, String>) input.type;
        Map<String, String> inwardSelector = (Map<String, String>) input.inwardIssue;
        Map<String, String> outwardSelector = (Map<String, String>) input.outwardIssue;
        assertThat(typeSelector.get("name")).isNotNull();
        assertThat(inwardSelector.get("key")).isNotNull();
        assertThat(outwardSelector.get("key")).isNotNull();

        JiraLinkTypeDto type = findLinkTypeByNameOrThrow(typeSelector.get("name"));
        ScopeImporterJiraIssueMock inward = findIssueByKeyOrThrow(inwardSelector.get("key"));
        ScopeImporterJiraIssueMock outward = findIssueByKeyOrThrow(outwardSelector.get("key"));

        links.add(Triple.of(type, inward, outward));
    }

    private ScopeImporterJiraIssueMock findIssueByKeyOrThrow(String key) {
        return findIssueByKey(key).orElseThrow(() -> new RuntimeException("issue not found"));
    }

    public ScopeImporterJiraIssueTypeMock createIssueType(String name, boolean subtask) {
        ScopeImporterJiraIssueTypeMock issueType = new ScopeImporterJiraIssueTypeMock(
                this,
                issueTypeIdGenerator.nextAsLong(),
                name,
                subtask
        );
        issueTypes.add(issueType);
        return issueType;
    }

    public Optional<JiraLinkTypeDto> findLinkTypeByName(String name) {
        return linkTypes.stream()
                .filter(link -> name.equals(link.name))
                .findFirst();
    }

    public JiraLinkTypeDto findLinkTypeByNameOrThrow(String name) {
        return findLinkTypeByName(name).orElseThrow(() -> new RuntimeException("unknown link type \"" + name + "\""));
    }

    static class ScopeImporterJiraProjectMock {

        private final ScopeImporterJiraMock jira;
        private final String id;
        private final String key;
        private final String name;
        private final List<Version> versions = new ArrayList<>();
        private final Map<String, ScopeImporterJiraIssueMock> issues = new HashMap<>();
        private final List<ScopeImporterJiraIssueTypeMock> issueTypes = new ArrayList<>();

        private ScopeImporterJiraProjectMock(ScopeImporterJiraMock jira, String id, String key, String name) {
            this.jira = jira;
            this.id = id;
            this.key = key;
            this.name = name;
        }

        public JiraProject asJiraProject() {
            return new JiraProject(
                    id,
                    key,
                    versions,
                    name);
        }

        public JiraCreateIssue asJiraCreateIssue(List<Long> issueTypes) {
            JiraCreateIssue.ProjectMetadata project = new JiraCreateIssue.ProjectMetadata();
            project.key = this.key;
            project.issueTypes = this.issueTypes.stream()
                    .filter(t -> issueTypes.contains(t.id))
                    .map(ScopeImporterJiraIssueTypeMock::asIssueTypeMetadata)
                    .collect(toList());

            JiraCreateIssue metadata = new JiraCreateIssue();
            metadata.projects = Collections.singletonList(project);

            return metadata;
        }

        public Version createVersion(String name) {
            Version version = new Version(jira.versionIdGenerator.nextAsString(), name);
            versions.add(version);
            return version;
        }

        public ScopeImporterJiraIssueMock createIssue(String key) {
            ScopeImporterJiraMock.ScopeImporterJiraIssueMock issue = new ScopeImporterJiraMock.ScopeImporterJiraIssueMock(
                    jira,
                    this,
                    jira.issueIdGenerator.nextAsLong(),
                    key);
            issues.put(key, issue);
            return issue;
        }

        public void addIssueType(ScopeImporterJiraIssueTypeMock issueType) {
            issueTypes.add(issueType);
        }

        private Optional<ScopeImporterJiraIssueTypeMock> findIssueType(Map selector) {
            assertThat(selector.get("id")).isNotNull();
            Long id = Long.valueOf((String) selector.get("id"));
            return issueTypes.stream()
                    .filter(issueType -> Objects.equals(issueType.id, id))
                    .findFirst();
        }

        private ScopeImporterJiraIssueTypeMock findIssueTypeOrThrow(Map selector) {
            return findIssueType(selector).orElseThrow(() -> new RuntimeException("unknown issue type"));
        }
    }

    static class ScopeImporterJiraIssueTypeMock {

        private final ScopeImporterJiraMock jira;
        public final Long id;
        private final String name;
        private final boolean subtask;
        private final Map<String, JiraCreateIssue.FieldInfoMetadata> fields = new TreeMap<>();

        public ScopeImporterJiraIssueTypeMock(ScopeImporterJiraMock jira, Long id, String name, boolean subtask) {
            this.jira = jira;
            this.id = id;
            this.name = name;
            this.subtask = subtask;
        }

        public JiraCreateIssue.FieldInfoMetadata createField(String name, boolean required) {
            Optional<JiraCreateIssue.FieldInfoMetadata> existingField = jira.findCustomFieldByName(name);
            if(existingField.isPresent()) {
                JiraCreateIssue.FieldInfoMetadata field = existingField.get();
                fields.put(field.id, field);
                return field;
            }
            String id = jira.customFieldIdGenerator.nextAsString();
            JiraCreateIssue.FieldInfoMetadata field = new JiraCreateIssue.FieldInfoMetadata(
                    id,
                    required,
                    name
            );
            jira.customFields.add(field);
            fields.put(id, field);
            return field;
        }

        public JiraCreateIssue.IssueTypeMetadata asIssueTypeMetadata() {
            return new JiraCreateIssue.IssueTypeMetadata(
                    id,
                    name,
                    subtask,
                    fields
            );
        }

        public JiraIssueTypeDto asJiraIssueType() {
            return new JiraIssueTypeDto(
                    id,
                    name,
                    subtask
            );
        }
    }

    private Optional<JiraCreateIssue.FieldInfoMetadata> findCustomFieldByName(String name) {
        return customFields.stream()
                .filter(field -> name.equals(field.name))
                .findFirst();
    }

    static class ScopeImporterJiraIssueMock {

        private final ScopeImporterJiraMock jira;
        private final ScopeImporterJiraProjectMock project;
        private final long id;
        private final String key;
        private final Map<String, Object> fields = new TreeMap<>();
        private ScopeImporterJiraIssueTypeMock issueType;

        public ScopeImporterJiraIssueMock(
                ScopeImporterJiraMock jira,
                ScopeImporterJiraProjectMock project,
                long id,
                String key) {
            this.jira = jira;
            this.project = project;
            this.id = id;
            this.key = key;
        }

        public JiraIssueDto asJiraIssue() {
            Map<String, Object> json = new HashMap<>();
            json.put("id", id);
            json.put("key", key);
            Map<String, Object> fields = new HashMap<>();
            json.put("fields", fields);

            List<Map<String, Object>> issuelinks = jira.links.stream()
                    .filter(link -> link.getMiddle() == this || link.getRight() == this)
                    .map(link -> {
                        JiraLinkTypeDto type = link.getLeft();
                        ScopeImporterJiraIssueMock inward = link.getMiddle();
                        ScopeImporterJiraIssueMock outward = link.getRight();
                        Map<String, Object> linkDto = new HashMap<>();
                        linkDto.put("type", type);
                        if(inward == this) {
                            linkDto.put("outwardIssue", inward.asLinkedIssue());
                        }
                        if(outward == this) {
                            linkDto.put("inwardIssue", inward.asLinkedIssue());
                        }
                        return linkDto;
                    })
                    .collect(toList());
            fields.put("issuelinks", issuelinks);

            fields.put("issuetype", issueType.asJiraIssueType());

            return gson.fromJson(gson.toJsonTree(json), JiraIssueDto.class);
        }

        private JiraLinkDto.LinkedIssue asLinkedIssue() {
            JiraLinkDto.LinkedIssue linked = new JiraLinkDto.LinkedIssue();
            linked.key = key;
            linked.fields = new JiraIssueDtoFields();
            linked.fields.issuetype = issueType.asJiraIssueType();
            return linked;
        }

        public JiraIssueDto asShallowJiraIssue() {
            Map<String, Object> json = new HashMap<>();
            json.put("id", id);
            json.put("key", key);
            Map<String, Object> fields = new HashMap<>();
            json.put("fields", fields);
            return gson.fromJson(gson.toJsonTree(json), JiraIssueDto.class);
        }

        public void setField(String key, Object value) {
            switch(key) {
                case "project":
                    assertThat(jira.findProject((Map) value)).hasValue(project);
                    return;
                case "issuetype":
                    issueType = project.findIssueTypeOrThrow((Map) value);
                    return;
                case "summary":
                    fields.put(key, value);
                    return;
                default:
                    if (issueType.fields.containsKey(key)) {
                        fields.put(key, value);
                        return;
                    }
            }
            throw new RuntimeException("unknown field \"" + key + "\"");
        }
    }

    static class IdGenerator {

        private int id = 1;

        public String nextAsString() {
            return Integer.toString(id++);
        }

        public long nextAsLong() {
            return (long) id++;
        }
    }
}
