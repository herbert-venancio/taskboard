package objective.taskboard.followup.kpi.services;

import static java.util.Collections.emptyList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public class IssueKpiTestRepository {
    
    private List<IssueKpiMetadata> issuesKpi;
    private List<Issue> issues;
    private KpiEnvironment environment;
    
    public IssueKpiTestRepository(KpiEnvironment environment) {
        this.environment = environment;
    }

    public Map<String,IssueKpi> getIssuesKpi(){
        if(issuesKpi == null)
            buildAll();
        return issuesKpi.stream().collect(Collectors.toMap(IssueKpiMetadata::getIssueKey,IssueKpiMetadata::getIssue));
    }
    
    List<Issue> getIssues() {
        if(issues == null)
            buildAll();
        return issues;
    }
    
    Map<String,List<IssueKpi>> getIssuesByProject(){
        if(issuesKpi == null)
            buildAll();
        
        Map<String, List<IssueKpiMetadata>> metadataByProject = issuesKpi.stream().collect(Collectors.groupingBy(IssueKpiMetadata::getProject));
        Map<String,List<IssueKpi>> issuesByProject = new LinkedHashMap<>();
        for (Entry<String,List<IssueKpiMetadata>> entry : metadataByProject.entrySet()) {
            List<IssueKpi> issuesKpiByProject = entry.getValue().stream().map(IssueKpiMetadata::getIssue).collect(Collectors.toList());
            issuesByProject.put(entry.getKey(), issuesKpiByProject);
        }
        return issuesByProject;
    }
    
    private void buildAll() {
        buildIssuesKpi();
        buildIssues();
    }

    private void buildIssues() {
        issues = environment.getAllIssueMockers().stream()
                                .map(IssueKpiMocker::mock)
                                .collect(Collectors.toList());
    }

    public Map<KpiLevel, List<IssueKpi>> getIssuesByLevel() {
        return getIssuesKpi().values().stream().collect(Collectors.groupingBy(IssueKpi::getLevel));
    }

    private void buildIssuesKpi() {
        List<IssueKpiMocker> allIssues = environment.getAllIssueMockers();
        List<String> issuesWithoutProject = allIssues.stream()
                                                .filter(IssueKpiMocker::noProjectConfigured)
                                                .map(IssueKpiMocker::getIssueKey)
                                                .collect(Collectors.toList());
        if(!issuesWithoutProject.isEmpty())
            throw new AssertionError(String.format("Issues without project configured: %s", issuesWithoutProject));
        
        Map<String,IssueKpiMocker> byKey = allIssues.stream().collect(Collectors.toMap(IssueKpiMocker::getIssueKey, Function.identity()));
        
        issuesKpi = allIssues.stream()
                    .map(IssueKpiMocker::buildIssueKpi)
                    .map(i -> {
                        String project = byKey.get(i.getIssueKey()).project();
                        return new IssueKpiMetadata(i, project);
                    })
                    .collect(Collectors.toList());
    }

    List<IssueKpi> getIssuesByLevel(KpiLevel level){
        return Optional.ofNullable(getIssuesByLevel().get(level)).orElse(emptyList());
    }
    
    class IssueKpiMetadata {
        
        private String issueKey;
        private IssueKpi issue;
        private String project;

        public IssueKpiMetadata(IssueKpi issue, String project) {
            this.issueKey = issue.getIssueKey();
            this.issue = issue;
            this.project = project;
        }

        private String getIssueKey() {
            return issueKey;
        }

        private IssueKpi getIssue() {
            return issue;
        }

        private String getProject() {
            return project;
        }
    }
    

}
