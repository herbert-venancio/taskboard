package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.EXTRA_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.SIZING_FIELD_ID_TAG;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;

import objective.taskboard.sizingImport.JiraFacade.IssueCustomFieldOptionValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldObjectValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldValue;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

class SizingImporter {

    private final Logger log = LoggerFactory.getLogger(SizingImporter.class);
    
    private final SizingImportConfig importConfig;
    private final JiraFacade jiraFacade;
    private final List<SizingImporterListener> listeners = new ArrayList<>();
    
    public SizingImporter(SizingImportConfig importConfig, JiraFacade jiraFacade) {
        this.importConfig = importConfig;
        this.jiraFacade = jiraFacade;
    }
    
    public void addListener(SizingImporterListener listener) {
        listeners.add(listener);
    }

    public void executeImport(String projectKey, List<SizingImportLine> allLines) {
        List<SizingImportLine> linesToImport = allLines.stream()
                .filter(SizingImportLine::isNotImported)
                .collect(toList());
        
        notifyImportStarted(allLines.size(), linesToImport.size());

        Project project = jiraFacade.getProject(projectKey);

        Map<String, CimIssueType> featureTypesByName = jiraFacade.requestFeatureTypes(projectKey).stream()
                .collect(toMap(CimIssueType::getName, Function.identity()));
        
        Map<Name, Version> importedVersions = recoverImportedVersions(project);
        Map<Name, ImportedDemand> importedDemands = recoverImportedDemands(allLines);

        for (SizingImportLine line : linesToImport) {
            notifyLineImportStarted(line);
            
            List<String> errors = getValidationErrors(line, featureTypesByName);
            if (!errors.isEmpty()) {
                notifyLineError(line, errors);
                continue;
            }
            
            String featureIssueKey;
            try {
                String versionName = line.getPhase();
                Version release = getOrCreateVersion(projectKey, versionName, importedVersions);
                ImportedDemand demand = getOrCreateDemand(projectKey, release, line.getDemand(), importedDemands);
                CimIssueType featureType = featureTypesByName.get(line.getType());

                BasicIssue featureIssue = createFeature(projectKey, release, demand.getIssueKey(), featureType, line);
                featureIssueKey = featureIssue.getKey();

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                notifyLineError(line, asList(errorMessage));
                log.error("Failed to import spreadsheet line " + line.getRowNumber(), e);
                continue;
            }

            notifyLineImportFinished(line, featureIssueKey);
        }

        notifyImportFinished();
    }

    private List<String> getValidationErrors(SizingImportLine line, Map<String, CimIssueType> featureTypesByName) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(line.getPhase()))
            errors.add("Phase should be informed");

        if (StringUtils.isBlank(line.getDemand()))
            errors.add("Demand should be informed");

        if (StringUtils.isBlank(line.getFeature()))
            errors.add("Feature should be informed");
        
        if (StringUtils.isBlank(line.getType()))
            errors.add("Type should be informed");
        
        CimIssueType featureType = featureTypesByName.get(line.getType());
        
        if (featureType == null) {
            errors.add("Type should be one of the following: " + featureTypesByName.keySet().stream().sorted().collect(joining(", ")));
            return errors;
        }

        List<CimFieldInfo> sizingFields = featureType.getFields().values().stream()
                .filter(f -> jiraFacade.getSizingFieldIds().contains(f.getId()))
                .collect(toList());

        errors.addAll(getRequiredFieldErrors(
                sizingFields, 
                field -> line.getValue(c -> c.getDefinition().hasTag(SIZING_FIELD_ID_TAG, field.getId()))));

        List<CimFieldInfo> extraFields = importConfig.getSheetMap().getExtraFields().stream()
                .map(ef -> featureType.getFields().get(ef.getFieldId()))
                .filter(Objects::nonNull)
                .collect(toList());
        
        errors.addAll(getRequiredFieldErrors(
                extraFields, 
                field -> line.getValue(c -> c.getDefinition().hasTag(EXTRA_FIELD_ID_TAG, field.getId()))));

        errors.addAll(getUnsupportedFieldErrors(line, featureType));

        return errors;
    }

    private List<String> getUnsupportedFieldErrors(SizingImportLine line, CimIssueType featureType) {
        return line.getImportValues().stream()
                .map(ImportValue::getColumnDefinition)
                .filter(columnDefinition -> {
                    String fieldId = null;

                    if (columnDefinition.hasTag(SIZING_FIELD_ID_TAG)) {
                        fieldId = columnDefinition.getTagValue(SIZING_FIELD_ID_TAG);

                    } else if (columnDefinition.hasTag(EXTRA_FIELD_ID_TAG)) {
                        fieldId = columnDefinition.getTagValue(EXTRA_FIELD_ID_TAG);

                    } else {
                        return false;
                    }

                    return !featureType.getFields().containsKey(fieldId);
                })
                .map(cd -> String.format("Column “%s” is not valid for the type %s and should be left blank", cd.getName(), line.getType()))
                .collect(toList());
    }

    private List<String> getRequiredFieldErrors(Collection<CimFieldInfo> fields, Function<CimFieldInfo, Optional<String>> valueSupplier) {
        return fields.stream()
                .filter(f -> f.isRequired())
                .filter(f -> isBlank(valueSupplier.apply(f).orElse(null)))
                .map(f -> f.getName() + " should be informed")
                .collect(toList());
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
        Version version = jiraFacade.createVersion(projectKey, versionName);
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
        
        log.debug("creating Demand: {}", demandName);

        BasicIssue demandIssue = jiraFacade.createDemand(projectKey, demandName, release);
        ImportedDemand importedDemand = new ImportedDemand(demandIssue.getKey(), new Name(demandName));
        importedDemands.put(importedDemand.getName(), importedDemand);
        
        return importedDemand;
    }

    private BasicIssue createFeature(String projectKey, Version release, String demandKey, CimIssueType featureType, SizingImportLine line) {
        String featureName = line.getFeature();
        log.debug("creating Feature: {}", featureName);

        Collection<IssueFieldValue> fieldValues = new ArrayList<>();
        
        fieldValues.addAll(line.getImportValues().stream()
                .filter(importValue -> importValue.getColumnDefinition().hasTag(SIZING_FIELD_ID_TAG))
                .map(importValue -> {
                    String fieldId = importValue.getColumnDefinition().getTagValue(SIZING_FIELD_ID_TAG);
                    return new IssueCustomFieldOptionValue(fieldId, importValue.getValue(), featureType);
                })
                .collect(toList()));

        fieldValues.addAll(line.getImportValues().stream()
                .filter(importValue -> importValue.getColumnDefinition().hasTag(EXTRA_FIELD_ID_TAG))
                .map(importValue -> {
                    String fieldId = importValue.getColumnDefinition().getTagValue(EXTRA_FIELD_ID_TAG);
                    return new IssueFieldObjectValue(fieldId, importValue.getValue());
                })
                .collect(toList()));

        return jiraFacade.createFeature(projectKey, demandKey, featureType.getId(), featureName, release, fieldValues);
    }

    private Optional<String> findFirstDemandKey(List<SizingImportLine> linesOfDemand) {
        return linesOfDemand.stream()
                .map(line -> {
                    Issue feature = jiraFacade.getIssue(line.getJiraKey());
                    return jiraFacade.getDemandKeyGivenFeature(feature);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private void notifyImportStarted(int totalLinesCount, int linesToImportCount) {
        listeners.stream().forEach(l -> l.onImportStarted(totalLinesCount, linesToImportCount));
    }

    private void notifyLineImportStarted(SizingImportLine line) {
        listeners.stream().forEach(l -> l.onLineImportStarted(line));
    }
    
    private void notifyLineImportFinished(SizingImportLine line, String featureIssueKey) {
        listeners.stream().forEach(l -> l.onLineImportFinished(line, featureIssueKey));
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
        void onLineImportFinished(SizingImportLine line, String featureIssueKey);
        void onLineError(SizingImportLine line, List<String> errorMessages);
        void onImportFinished();
    }
}