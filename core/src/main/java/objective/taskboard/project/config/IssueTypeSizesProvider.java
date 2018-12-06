package objective.taskboard.project.config;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class IssueTypeSizesProvider {

    private final JiraProperties jiraProperties;
    private final MetadataService metadataService;

    @Autowired
    public IssueTypeSizesProvider(
            JiraProperties jiraProperties,
            MetadataService metadataService
            ) {
        this.jiraProperties = jiraProperties;
        this.metadataService = metadataService;
    }

    public List<IssueTypeSize> get() {
        HashSet<String> issueTypes = getIssueTypes();

        List<String> sizes = jiraProperties.getCustomfield()
                .getTShirtSize()
                .getSizes();

        return issueTypes.stream()
            .flatMap(type -> toIssueTypeSizes(type, sizes).stream())
            .sorted(comparing(IssueTypeSize::getIssueType))
            .collect(toList());
    }

    private List<IssueTypeSize> toIssueTypeSizes(String issueType, List<String> sizes) {
        return sizes.stream()
                .map(size -> new IssueTypeSize(issueType, size))
                .collect(toList());
    }

    private HashSet<String> getIssueTypes() {
        HashSet<String> issueTypes = new HashSet<>();
        HashSet<Long> jiraIssueTypeIds = new HashSet<>();

        jiraProperties.getFollowup().getBallparkMappings().values()
            .forEach(ballparks ->
                ballparks.forEach(it -> {
                    issueTypes.add(it.getIssueType());
                    jiraIssueTypeIds.addAll(it.getJiraIssueTypes());
                })
            );
        issueTypes.addAll(getJiraIssueTypeNames(jiraIssueTypeIds));

        return issueTypes;
    }

    private List<String> getJiraIssueTypeNames(final HashSet<Long> jiraIssueTypeIds) {
        Map<Long, JiraIssueTypeDto> issueTypeMetadata = this.metadataService.getIssueTypeMetadata();

        return jiraIssueTypeIds.stream()
                .filter(id -> issueTypeMetadata.containsKey(id))
                .map(id -> issueTypeMetadata.get(id).getName())
                .collect(toList());
    }

    public static class IssueTypeSize {
        private final String issueType;
        private final String size;

        public IssueTypeSize(String issueType, String size) {
            this.issueType = issueType;
            this.size = size;
        }

        public String getIssueType() {
            return issueType;
        }

        public String getSize() {
            return size;
        }
    }
}
