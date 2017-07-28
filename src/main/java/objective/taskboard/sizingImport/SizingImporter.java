package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.sizingImport.SizingImportLine.JiraField;

class SizingImporter {

    private final Logger log = LoggerFactory.getLogger(SizingImporter.class);
    
    private final JiraUtils jiraUtils;
    private final JiraProperties jiraProperties;
    private final List<SizingImporterListener> listeners = new ArrayList<>();
    
    public SizingImporter(JiraProperties jiraProperties, JiraUtils jiraUtils) {
        this.jiraProperties = jiraProperties;
        this.jiraUtils = jiraUtils;
    }
    
    public void addListener(SizingImporterListener listener) {
        listeners.add(listener);
    }

    public void executeImport(String projectKey, List<SizingImportLine> allLines) {
        List<SizingImportLine> linesToImport = allLines.stream()
                .filter(SizingImportLine::isNotImported)
                .collect(toList());
        
        notifyImportStarted(allLines.size(), linesToImport.size());

        Project project = jiraUtils.getProject(projectKey);
        CimIssueType featureMetadata = jiraUtils.getFeatureMetadata(projectKey);
        Map<Name, Version> importedVersions = recoverImportedVersions(project);
        Map<Name, ImportedDemand> importedDemands = recoverImportedDemands(allLines);

        for (SizingImportLine line : linesToImport) {
            notifyLineImportStarted(line);
            
            List<String> errors = getValidationErrors(line, featureMetadata);
            if (!errors.isEmpty()) {
                notifyLineError(line, errors);
                continue;
            }
            
            try {
                String versionName = line.getPhase();
                Version release = getOrCreateVersion(projectKey, versionName, importedVersions);
                ImportedDemand demand = getOrCreateDemand(projectKey, release, line.getDemand(), importedDemands);
                
                BasicIssue featureIssue = createFeature(projectKey, release, demand.getIssueKey(), featureMetadata, line);
                line.setJiraKey(featureIssue.getKey());

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                notifyLineError(line, asList(errorMessage));
                log.error("Failed to import spreadsheet line " + line.getRowNumber(), e);
                continue;
            }

            notifyLineImportFinished(line);
        }

        notifyImportFinished();
    }

    private List<String> getValidationErrors(SizingImportLine line, CimIssueType featureMetadata) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(line.getPhase()))
            errors.add("Phase should be informed;");

        if (StringUtils.isBlank(line.getDemand()))
            errors.add("Demand should be informed;");

        if (StringUtils.isBlank(line.getFeature()))
            errors.add("Feature should be informed;");
        
        List<String> configuredTShirtFieldsId = jiraProperties.getCustomfield().getTShirtSize().getIds();
        
        Stream<CimFieldInfo> requiredTShirtFieldsOfFeature = featureMetadata.getFields().values().stream()
            .filter(f -> configuredTShirtFieldsId.contains(f.getId()))
            .filter(CimFieldInfo::isRequired);
        
        List<String> fieldErrors = requiredTShirtFieldsOfFeature
                .filter(rf -> isBlank(line.getFieldValue(rf.getId()).orElse(null)))
                .map(rf -> rf.getName() + " should be informed")
                .collect(toList());

        errors.addAll(fieldErrors);
        
        return errors;
    }

    private Map<Name, Version> recoverImportedVersions(Project project) {
        return streamOf(project.getVersions())
                .collect(toMap(v -> new Name(v.getName()), v -> v));
    }

    private Version getOrCreateVersion(String projectKey, String versionName, Map<Name, Version> importedVersions) {
        Version previouslyImportedVersion = importedVersions.get(new Name(versionName));
        
        if (previouslyImportedVersion != null)
            return previouslyImportedVersion;
        
        log.debug("creating Version: {}", versionName);
        Version version = jiraUtils.createVersion(projectKey, versionName);
        importedVersions.put(new Name(versionName), version);
        
        return version;
    }

    private Map<Name, ImportedDemand> recoverImportedDemands(List<SizingImportLine> allLines) {
        Map<Name, List<SizingImportLine>> importedLinesByDemand = allLines.stream()
                .filter(SizingImportLine::isImported)
                .collect(groupingBy(l -> new Name(l.getDemand())));

        return importedLinesByDemand.entrySet().stream()
                .map((entry) -> { 
                    Name demandName = entry.getKey();
                    List<SizingImportLine> linesOfDemand = entry.getValue();
                    
                    return findFirstDemandKey(linesOfDemand)
                            .map(demandKey -> new ImportedDemand(demandKey, demandName)); 
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(ImportedDemand::getName, d -> d));
    }

    private ImportedDemand getOrCreateDemand(String projectKey, Version release, String demandName, Map<Name, ImportedDemand> importedDemands) {
        ImportedDemand previouslyImportedDemand = importedDemands.get(new Name(demandName));
        
        if (previouslyImportedDemand != null)
            return previouslyImportedDemand;
        
        BasicIssue demandIssue = createDemand(projectKey, release, demandName);
        ImportedDemand importedDemand = new ImportedDemand(demandIssue.getKey(), new Name(demandName));
        importedDemands.put(importedDemand.getName(), importedDemand);
        
        return importedDemand;
    }

    private BasicIssue createDemand(String projectKey, Version release, String demandName) {
        log.debug("creating Demand: {}", demandName);

        long demandTypeId = jiraProperties.getIssuetype().getDemand().getId();
        String releaseFieldId = jiraProperties.getCustomfield().getRelease().getId();

        IssueInputBuilder demandBuilder = new IssueInputBuilder(projectKey, demandTypeId)
                .setSummary(demandName)
                .setFieldValue(releaseFieldId, release);
        
        return jiraUtils.createIssue(demandBuilder);
    }

    private BasicIssue createFeature(String projectKey, Version release, String demandKey, CimIssueType featureMetadata, SizingImportLine line) {
        log.debug("creating Feature: {}", line.getFeature());

        long featureTypeId = jiraProperties.getIssuetype().getDefaultFeature().getId();
        String customFieldRelease = jiraProperties.getCustomfield().getRelease().getId();
        
        IssueInputBuilder builder = new IssueInputBuilder(projectKey, featureTypeId)
                .setSummary(line.getFeature())
                .setFieldValue(customFieldRelease, release);

        for (JiraField field : line.getFields()) {
            CustomFieldOption option = jiraUtils.getCustomFieldOption(featureMetadata, field.getId(), field.getValue());
            builder.setFieldValue(field.getId(), option);
        }
        
        BasicIssue issue = jiraUtils.createIssue(builder);
        jiraUtils.linkToDemand(demandKey, issue.getKey());
        
        return issue;
    }

    private Optional<String> findFirstDemandKey(List<SizingImportLine> linesOfDemand) {
        return linesOfDemand.stream()
                .map(line -> findDemandKeyOfFeature(line.getJiraKey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<String> findDemandKeyOfFeature(String featureIssueKey) {
        Issue featureIssue = jiraUtils.getIssue(featureIssueKey);        
        String demandIssueLinkName = jiraUtils.getDemandLink().getName();
        return streamOf(featureIssue.getIssueLinks())
                .filter(link -> link.getIssueLinkType().getName().equals(demandIssueLinkName))
                .map(IssueLink::getTargetIssueKey)
                .findFirst();
    }

    private void notifyImportStarted(int totalLinesCount, int linesToImportCount) {
        listeners.stream().forEach(l -> l.onImportStarted(totalLinesCount, linesToImportCount));
    }

    private void notifyLineImportStarted(SizingImportLine line) {
        listeners.stream().forEach(l -> l.onLineImportStarted(line));
    }
    
    private void notifyLineImportFinished(SizingImportLine line) {
        listeners.stream().forEach(l -> l.onLineImportFinished(line));
    }

    private void notifyLineError(SizingImportLine line, List<String> errorMessages) {
        listeners.stream().forEach(l -> l.onLineError(line, errorMessages));
    }

    private void notifyImportFinished() {
        listeners.stream().forEach(l -> l.onImportFinished());
    }

    private static <T> Stream<T> streamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private static class ImportedDemand {
        private final String issueKey;
        private final Name name;
        
        public ImportedDemand(String issueKey, Name name) {
            this.issueKey = issueKey;
            this.name = name;
        }
        
        public String getIssueKey() {
            return issueKey;
        }
        
        public Name getName() {
            return name;
        }
    }
    
    private static class Name {
        private final String value;

        public Name(String value) {
            this.value = value.toLowerCase();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            
            if (obj == null)
                return false;
            
            if (getClass() != obj.getClass())
                return false;
            
            Name other = (Name) obj;
            return value.equals(other.value);
        }
    }

    public interface SizingImporterListener {
        void onImportStarted(int totalLinesCount, int linesToImportCount);
        void onLineImportStarted(SizingImportLine line);
        void onLineImportFinished(SizingImportLine line);
        void onLineError(SizingImportLine line, List<String> errorMessages);
        void onImportFinished();
    }
}