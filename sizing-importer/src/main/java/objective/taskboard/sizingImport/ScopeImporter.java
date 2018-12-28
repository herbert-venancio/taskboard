package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.EXTRA_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.SIZING_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.TIMEBOX;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.sizingImport.JiraFacade.IssueCustomFieldOptionValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldObjectValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldValue;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

class ScopeImporter {

    private final Logger log = LoggerFactory.getLogger(ScopeImporter.class);

    private final SizingImportConfig importConfig;
    private final JiraFacade jiraFacade;
    private final SizingSheetImporterNotifier importerNotifier;

    public ScopeImporter(SizingImportConfig importConfig, JiraFacade jiraFacade, SizingSheetImporterNotifier importerNotifier) {
        this.importConfig = importConfig;
        this.jiraFacade = jiraFacade;
        this.importerNotifier = importerNotifier;
    }

    public void executeImport(String projectKey, List<SizingImportLineScope> allLines) {
        List<SizingImportLineScope> linesToImport = allLines.stream()
                .filter(SizingImportLineScope::isNotImported)
                .collect(toList());
        
        importerNotifier.notifySheetImportStarted(SHEET_SCOPE, allLines.size(), linesToImport.size());

        JiraProject project = jiraFacade.getProject(projectKey);

        Map<String, JiraCreateIssue.IssueTypeMetadata> featureTypesByName = jiraFacade.requestFeatureTypes(projectKey).stream()
                .collect(toMap(t -> t.name, Function.identity()));
        
        Map<Name, Version> importedVersions = recoverImportedVersions(project);
        Map<Name, ImportedDemand> importedDemands = recoverImportedDemands(project, allLines);

        for (SizingImportLineScope line : linesToImport) {
            importerNotifier.notifyLineImportStarted(line);
            
            List<String> errors = getValidationErrors(line, featureTypesByName);
            if (!errors.isEmpty()) {
                importerNotifier.notifyLineError(line, errors);
                continue;
            }
            
            String featureIssueKey;
            try {
                String versionName = line.getPhase();
                Version release = getOrCreateVersion(projectKey, versionName, importedVersions);
                ImportedDemand demand = getOrCreateDemand(projectKey, release, line.getDemand(), importedDemands);
                JiraCreateIssue.IssueTypeMetadata featureType = featureTypesByName.get(line.getType());

                JiraIssue featureIssue = createFeature(projectKey, release, demand.getIssueKey(), featureType, line);
                featureIssueKey = featureIssue.key;

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                importerNotifier.notifyLineError(line, asList(errorMessage));
                log.error("Failed to import spreadsheet line " + line.getRowNumber(), e);
                continue;
            }

            importerNotifier.notifyLineImportFinished(line, featureIssueKey);
        }

        importerNotifier.notifySheetImportFinished();
    }

    private List<String> getValidationErrors(SizingImportLineScope line, Map<String, JiraCreateIssue.IssueTypeMetadata> featureTypesByName) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(line.getPhase()))
            errors.add("Phase should be informed");

        if (StringUtils.isBlank(line.getDemand()))
            errors.add("Demand should be informed");

        if (StringUtils.isBlank(line.getFeature()))
            errors.add("Feature should be informed");
        
        if (StringUtils.isBlank(line.getType()))
            errors.add("Type should be informed");

        if (isTimeBoxFeature(line.getType()) && isInvalidTimeboxValue(line.getTimebox()))
            errors.add("Timebox should be informed correctly");

        JiraCreateIssue.IssueTypeMetadata featureType = featureTypesByName.get(line.getType());
        
        if (featureType == null) {
            errors.add("Type should be one of the following: " + featureTypesByName.keySet().stream().sorted().collect(joining(", ")));
            return errors;
        }

        List<JiraCreateIssue.FieldInfoMetadata> sizingFields = featureType.getFields().stream()
                .filter(f -> jiraFacade.getSizingFieldIds().contains(f.id))
                .collect(toList());

        errors.addAll(getRequiredFieldErrors(
                sizingFields, 
                field -> line.getValue(c -> c.getDefinition().hasTag(SIZING_FIELD_ID_TAG, field.id))));

        List<JiraCreateIssue.FieldInfoMetadata> extraFields = importConfig.getSheetMap().getExtraFields().stream()
                .map(ef -> featureType.getField(ef.getFieldId()))
                .filter(Objects::nonNull)
                .collect(toList());
        
        errors.addAll(getRequiredFieldErrors(
                extraFields, 
                field -> line.getValue(c -> c.getDefinition().hasTag(EXTRA_FIELD_ID_TAG, field.id))));

        errors.addAll(getUnsupportedFieldErrors(line, featureType));

        return errors;
    }

    private List<String> getUnsupportedFieldErrors(SizingImportLineScope line, JiraCreateIssue.IssueTypeMetadata featureType) {
        return line.getImportValues().stream()
                .map(ImportValue::getColumnDefinition)
                .filter(columnDefinition -> {
                    String fieldId;

                    if (columnDefinition.hasTag(SIZING_FIELD_ID_TAG)) {
                        fieldId = columnDefinition.getTagValue(SIZING_FIELD_ID_TAG);

                    } else if (columnDefinition.hasTag(EXTRA_FIELD_ID_TAG)) {
                        fieldId = columnDefinition.getTagValue(EXTRA_FIELD_ID_TAG);

                    } else {
                        return false;
                    }

                    return !featureType.containsFieldId(fieldId);
                })
                .map(cd -> String.format("Column “%s” is not valid for the type %s and should be left blank", cd.getName(), line.getType()))
                .collect(toList());
    }

    private List<String> getRequiredFieldErrors(Collection<JiraCreateIssue.FieldInfoMetadata> fields, Function<JiraCreateIssue.FieldInfoMetadata, Optional<String>> valueSupplier) {
        return fields.stream()
                .filter(f -> f.required)
                .filter(f -> isBlank(valueSupplier.apply(f).orElse(null)))
                .map(f -> f.name + " should be informed")
                .collect(toList());
    }

    private Map<Name, Version> recoverImportedVersions(JiraProject project) {
        return project.versions.stream()
                .collect(toMap(v -> new Name(v.name), v -> v));
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

    private Map<Name, ImportedDemand> recoverImportedDemands(JiraProject project, List<SizingImportLineScope> allLines) {
        return allLines.parallelStream()
                .map(line -> new Name(line.getDemand()))
                .distinct()
                .map(demand -> jiraFacade.findDemandBySummary(project.key, demand.value))
                .filter(Optional::isPresent)
                .map(result -> {
                    JiraIssueDto demand = result.get();
                    return new ImportedDemand(demand.getKey(), new Name(demand.getSummary()));
                })
                .collect(toMap(ImportedDemand::getName, d -> d));
    }

    private ImportedDemand getOrCreateDemand(String projectKey, Version release, String demandName, Map<Name, ImportedDemand> importedDemands) {
        ImportedDemand previouslyImportedDemand = importedDemands.get(new Name(demandName));
        
        if (previouslyImportedDemand != null)
            return previouslyImportedDemand;
        
        log.debug("creating Demand: {}", demandName);

        JiraIssue demandIssue = jiraFacade.createDemand(projectKey, demandName, release);
        ImportedDemand importedDemand = new ImportedDemand(demandIssue.key, new Name(demandName));
        importedDemands.put(importedDemand.getName(), importedDemand);
        
        return importedDemand;
    }

    private JiraIssue createFeature(String projectKey, Version release, String demandKey, JiraCreateIssue.IssueTypeMetadata featureType, SizingImportLineScope line) {
        String featureName = line.getFeature();
        String timeboxHours = line.getTimebox() + "h";
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

        if (isTimeBoxFeature(featureType.name))
            return jiraFacade.createTimebox(projectKey, demandKey, featureType.id, featureName, release, fieldValues, timeboxHours);

        return jiraFacade.createFeature(projectKey, demandKey, featureType.id, featureName, release, fieldValues);
    }

    private boolean isTimeBoxFeature(final String typeLine) {
            return TIMEBOX.getName().equalsIgnoreCase(typeLine);
    }

    private boolean isInvalidTimeboxValue(final String timeboxValue) {
        if (StringUtils.isBlank(timeboxValue))
            return true;

        return new Long(timeboxValue) <= 0;
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

}