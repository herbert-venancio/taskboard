package objective.taskboard.project.config;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.jira.properties.JiraProperties;

@Component
public class IssueTypeSizesProvider {

    private final JiraProperties jiraProperties;

    @Autowired
    public IssueTypeSizesProvider(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
    }

    public List<IssueTypeSize> get() {
        List<String> issueType = asList(
                "Alpha Bug",
                "Alpha Test",
                "Backend Development",
                "BALLPARK - Alpha Test",
                "BALLPARK - Backend Development",
                "BALLPARK - Demand",
                "BALLPARK - Developer Support",
                "BALLPARK - Development",
                "BALLPARK - Feature Review",
                "BALLPARK - Frontend Development",
                "BALLPARK - Planning",
                "BALLPARK - QA/UAT Support",
                "BALLPARK - Subtask",
                "BALLPARK - Tech Planning",
                "BALLPARK - UX",
                "Dev Support",
                "Feature Planning",
                "Feature Review",
                "Frontend Development",
                "QA",
                "Sub-Task",
                "Tech Planning",
                "UAT",
                "UX");

        List<String> sizes = jiraProperties.getCustomfield().getTShirtSize().getSizes();

        return issueType.stream()
            .flatMap(type -> toIssueTypeSizes(type, sizes).stream())
            .collect(toList());
    }

    private List<IssueTypeSize> toIssueTypeSizes(String issueType, List<String> sizes) {
        return sizes.stream()
            .map(size -> new IssueTypeSize(issueType, size))
            .collect(toList());
    }

    static class IssueTypeSize {
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
