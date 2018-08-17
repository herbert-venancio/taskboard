package objective.taskboard.sizingImport.cost;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.sizingImport.JiraFacade;
import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingSheetImporterNotifier;

public class CostImporter {

    private final Logger log = LoggerFactory.getLogger(CostImporter.class);

    private final SizingImportConfig importConfig;
    private final JiraFacade jiraFacade;
    private final SizingSheetImporterNotifier importerNotifier;

    public CostImporter(SizingImportConfig importConfig, JiraFacade jiraFacade, SizingSheetImporterNotifier importerNotifier) {
        this.importConfig = importConfig;
        this.jiraFacade = jiraFacade;
        this.importerNotifier = importerNotifier;
    }

    public void executeImport(String projectKey, List<SizingImportLineCost> allLines) {
        List<SizingImportLineCost> linesToImport = allLines.stream()
                .filter(SizingImportLineCost::isNotImported)
                .collect(toList());

        importerNotifier.notifySheetImportStarted(SHEET_COST, allLines.size(), linesToImport.size());

        for (SizingImportLineCost line : linesToImport) {
            importerNotifier.notifyLineImportStarted(line);

            List<String> errors = getValidationErrors(line);
            if (!errors.isEmpty()) {
                importerNotifier.notifyLineError(line, errors);
                continue;
            }

            String indirectCostIssueKey;
            try {
                JiraIssue indirectCostIssue = createIndirectCost(projectKey, line);
                indirectCostIssueKey = indirectCostIssue.key;

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                importerNotifier.notifyLineError(line, asList(errorMessage));
                log.error("Failed to import spreadsheet line " + line.getRowNumber(), e);
                continue;
            }

            importerNotifier.notifyLineImportFinished(line, indirectCostIssueKey);
        }

        importerNotifier.notifySheetImportFinished();
    }

    private List<String> getValidationErrors(SizingImportLineCost line) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(line.getIndirectCosts()))
            errors.add("Indirect Costs should be informed");

        if (StringUtils.isBlank(line.getEffort()))
            errors.add("Effort should be informed");

        return errors;
    }

    private JiraIssue createIndirectCost(String projectKey, SizingImportLineCost line) {
        String indirectCost = line.getIndirectCosts();
        log.debug("creating Indirect Cost: {}", indirectCost);

        String indirectCostEffort = line.getEffort() + "h";
        Long parentTypeId = importConfig.getIndirectCosts().getParentTypeId();
        Long subtaskTypeId = importConfig.getIndirectCosts().getSubtaskTypeId();
        return jiraFacade.createIndirectCost(projectKey, parentTypeId, subtaskTypeId, indirectCost, indirectCostEffort);
    }

}
