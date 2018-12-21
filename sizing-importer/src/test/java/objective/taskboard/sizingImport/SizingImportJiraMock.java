package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.OptionalAssert;
import org.mockito.stubbing.Answer;

import com.google.gson.Gson;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraIssueDtoFields;
import objective.taskboard.jira.client.JiraIssueDtoSearch;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.client.JiraLinkDto;
import objective.taskboard.jira.client.JiraLinkTypeDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import retrofit.client.Response;

@SuppressWarnings("rawtypes")
class SizingImportJiraMock {

    static final Answer NOT_MOCKED_EXCEPTION_ANSWER = invocation -> {
        throw new RuntimeException("not mocked");
    };
    private static final Gson gson = new Gson();

    public final JiraProject.Service projectRest = mock(JiraProject.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraCreateIssue.Service createMetadataRest = mock(JiraCreateIssue.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraIssue.Service issueRest = mock(JiraIssue.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraLinkTypeDto.Service linkTypeRest = mock(JiraLinkTypeDto.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);
    public final JiraIssueDtoSearch.Service searchRest = mock(JiraIssueDtoSearch.Service.class, NOT_MOCKED_EXCEPTION_ANSWER);

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

    SizingImportJiraMock() {
        setupRestEndpoints();
        createLinkType("Demand", "is demanded by","demands");
    }

    @SuppressWarnings("unchecked")
    private void setupRestEndpoints() {
        Controller controller = new Controller();
        doAnswer(invocation -> {
            String id = invocation.getArgumentAt(0, String.class);
            return controller.getProject(id);
        }).when(projectRest).get(any());
        doAnswer(invocation -> {
            String projectKey = invocation.getArgumentAt(0, String.class);
            List<Long> issueTypes = invocation.getArgumentAt(1, List.class);
            return controller.getCreateIssueMetadata(projectKey, issueTypes);
        }).when(createMetadataRest).getByProjectKey(any(), any());
        doAnswer(invocation -> {
            JiraIssue.Input input = invocation.getArgumentAt(0, JiraIssue.Input.class);
            return controller.createIssue(input);
        }).when(issueRest).create(any());
        doAnswer(invocation -> controller.allLinkTypes())
                .when(linkTypeRest).all();
        doAnswer(invocation -> {
            JiraIssue.LinkInput input = invocation.getArgumentAt(0, JiraIssue.LinkInput.class);
            return controller.linkIssue(input);
        }).when(issueRest).linkIssue(any());
        doAnswer(invocation -> {
            Map<String, Object> input = invocation.getArgumentAt(0, Map.class);
            return controller.searchIssues(input);
        }).when(searchRest).search(any());
    }

    class Controller {

        public JiraProject getProject(String id) {
            return Optional.ofNullable(projects.get(id))
                    .map(ScopeImporterJiraProjectMock::asJiraProject)
                    .orElse(null);
        }

        public JiraCreateIssue getCreateIssueMetadata(String projectKey, List<Long> issueTypes) {
            return Optional.ofNullable(projects.get(projectKey))
                    .map(p -> p.asJiraCreateIssue(issueTypes))
                    .orElse(null);
        }

        public JiraIssue createIssue(JiraIssue.Input input) {
            ScopeImporterJiraProjectMock project = findProject((Map) input.fields.get("project"))
                    .orElseThrow(() -> new RuntimeException("project not specified"));

            String key = String.format("%s-%d",
                    project.key,
                    project.issues.keySet().stream()
                            .mapToInt(issueKey -> Integer.valueOf(StringUtils.substringAfter(issueKey, "-")) + 1)
                            .max()
                            .orElse(1));

            ScopeImporterJiraIssueMock issue = project.createIssue(key);
            input.fields.forEach(issue::setField);
            return new JiraIssue(key);
        }

        public JiraLinkTypeDto.Response allLinkTypes() {
            JiraLinkTypeDto.Response response = new JiraLinkTypeDto.Response();
            response.issueLinkTypes = new ArrayList<>(linkTypes);
            return response;
        }

        public Response linkIssue(JiraIssue.LinkInput input) {
            createLink(input);
            return null;
        }

        public JiraIssueDtoSearch searchIssues(Map<String, Object> input) {
            String jql = (String) input.get("jql");

            String[] parts = jql.split("AND");
            Pattern jqlRegex = Pattern.compile("(project|issuetype|summary) ?([=~]) ?['\"]?(.+)['\"]?");

            Predicate<ScopeImporterJiraIssueMock> filter = (issue) -> true;

            for(String part : parts) {
                Matcher matcher = jqlRegex.matcher(part.trim());
                if(matcher.matches()) {
                    String property = matcher.group(1);
                    String op = matcher.group(2);
                    String value = matcher.group(3);
                    while(true) {
                        if ("project".equals(property)) {
                            if ("=".equals(op)) {
                                filter = filter.and(issue -> Objects.equals(issue.project.id, value) || Objects.equals(issue.project.key, value));
                                break;
                            }
                        } else if ("issuetype".equals(property)) {
                            if ("=".equals(op)) {
                                filter = filter.and(issue -> Objects.equals(issue.issueType.id.toString(), value) || Objects.equals(issue.issueType.name, value));
                                break;
                            }
                        } else if ("summary".equals(property)) {
                            if ("~".equals(op)) {
                                filter = filter.and(issue -> {
                                    String summary = (String) issue.fields.get("summary");
                                    if(StringUtils.isEmpty(summary))
                                        return false;
                                    String[] words = value.replaceAll("[\"']", "").split(" ");
                                    for(String word : words) {
                                        if(!StringUtils.containsIgnoreCase(summary, word))
                                            return false;
                                    }
                                    return true;
                                });
                                break;
                            }
                        }
                        throw new RuntimeException(property + " filter operator '" + op + "' not implemented");
                    }
                }
            }

            List<JiraIssueDto> issues = findIssues(filter)
                    .map(ScopeImporterJiraIssueMock::asJiraIssue)
                    .collect(toList());
            Map<String, Object> result = new HashMap<>();
            result.put("issues", issues);
            return gson.fromJson(gson.toJson(result), JiraIssueDtoSearch.class);
        }
    }

    private Optional<ScopeImporterJiraProjectMock> findProject(Map selector) {
        if(selector == null)
            return Optional.empty();

        return Optional.ofNullable(projects.get(selector.get("key")));
    }

    private Optional<ScopeImporterJiraIssueMock> findIssueByKey(String key) {
        return allIssues()
                .filter(i -> key.equals(i.key))
                .findFirst();
    }

    private Stream<ScopeImporterJiraIssueMock> findIssues(Predicate<ScopeImporterJiraIssueMock> filter) {
        return allIssues()
                .filter(filter);
    }

    private Stream<ScopeImporterJiraIssueMock> allIssues() {
        return projects.values().stream()
                .flatMap(project -> project.issues.values().stream());
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
        SizingImportJiraMock.ScopeImporterJiraProjectMock project = new SizingImportJiraMock.ScopeImporterJiraProjectMock(
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

        private final SizingImportJiraMock jira;
        private final String id;
        private final String key;
        private final String name;
        private final List<Version> versions = new ArrayList<>();
        private final Map<String, ScopeImporterJiraIssueMock> issues = new HashMap<>();
        private final List<ScopeImporterJiraIssueTypeMock> issueTypes = new ArrayList<>();

        private ScopeImporterJiraProjectMock(SizingImportJiraMock jira, String id, String key, String name) {
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
            SizingImportJiraMock.ScopeImporterJiraIssueMock issue = new SizingImportJiraMock.ScopeImporterJiraIssueMock(
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

        public Optional<ScopeImporterJiraIssueTypeMock> findIssueTypeByName(String name) {
            return issueTypes.stream()
                    .filter(issueType -> name.equals(issueType.name))
                    .findFirst();
        }

        public ScopeImporterJiraIssueTypeMock findIssueTypeByNameOrThrow(String name) {
            return findIssueTypeByName(name)
                    .orElseThrow(() -> new RuntimeException("unknown issue type \"" + name + "\""));
        }
    }

    static class ScopeImporterJiraIssueTypeMock {

        private final SizingImportJiraMock jira;
        public final Long id;
        private final String name;
        private final boolean subtask;
        private final Map<String, JiraCreateIssue.FieldInfoMetadata> fields = new TreeMap<>();

        public ScopeImporterJiraIssueTypeMock(SizingImportJiraMock jira, Long id, String name, boolean subtask) {
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

        private final SizingImportJiraMock jira;
        private final ScopeImporterJiraProjectMock project;
        private final long id;
        private final String key;
        private final Map<String, Object> fields = new HashMap<>();
        private ScopeImporterJiraIssueTypeMock issueType;

        public ScopeImporterJiraIssueMock(
                SizingImportJiraMock jira,
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
            Map<String, Object> fields = new TreeMap<>(this.fields);
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
                    if (value instanceof ScopeImporterJiraIssueTypeMock)
                        issueType = (ScopeImporterJiraIssueTypeMock) value;
                    else if (value instanceof String)
                        issueType = project.findIssueTypeByNameOrThrow((String) value);
                    else if(value instanceof Map)
                        issueType = project.findIssueTypeOrThrow((Map) value);
                    else
                        break;
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
            throw new RuntimeException("cannot set field \"" + key + "\"");
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
