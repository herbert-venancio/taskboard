package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionRowProvider;
import objective.taskboard.sizingImport.cost.SizingImportLineCost;
import objective.taskboard.sizingImport.cost.CostImporter;

@Component
public class SizingImporter {

    private final SimpMessagingTemplate messagingTemplate;
    private final JiraFacade jiraFacade;
    private final SizingImportConfig importConfig;
    private final CostColumnMappingDefinitionRowProvider costRowProvider;

    @Autowired
    public SizingImporter(
            SimpMessagingTemplate messagingTemplate,
            JiraFacade jiraFacade,
            SizingImportConfig importConfig,
            CostColumnMappingDefinitionRowProvider costRowProvider) {

        this.messagingTemplate = messagingTemplate;
        this.jiraFacade = jiraFacade;
        this.importConfig = importConfig;
        this.costRowProvider = costRowProvider;
    }

    public void executeImport(
            String spreadsheetId,
            SpreadsheetsManager spreadsheetsManager,
            String projectKey,
            List<SizingImportLineScope> scopeLines,
            Optional<List<SizingImportLineCost>> costLines) {

        int scopeLinesToImportCount = scopeLines.stream()
                .filter(SizingImportLineScope::isNotImported)
                .collect(toList())
                .size();

        int costLinesToImportCount = !costLines.isPresent() ? 0 : costLines.get().stream()
                .filter(SizingImportLineCost::isNotImported)
                .collect(toList())
                .size();

        SizingImporterSocketStatusEmmiter socketStatusEmmiter = new SizingImporterSocketStatusEmmiter(messagingTemplate);
        socketStatusEmmiter.onImportStarted(scopeLinesToImportCount + costLinesToImportCount);

        executeScopeImport(socketStatusEmmiter, spreadsheetId, spreadsheetsManager, projectKey, scopeLines);
        executeCostImport(socketStatusEmmiter, spreadsheetId, spreadsheetsManager, projectKey, costLines);
    }

    private void executeScopeImport(
            SizingImporterSocketStatusEmmiter socketStatusEmmiter,
            String spreadsheetId,
            SpreadsheetsManager spreadsheetsManager,
            String projectKey,
            List<SizingImportLineScope> scopeLines) {

        SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();
        importerNotifier.addListener(socketStatusEmmiter);
        importerNotifier.addListener(new SizingImporterSheetUpdater(
                spreadsheetId,
                SHEET_SCOPE,
                spreadsheetsManager,
                jiraFacade.getJiraUrl(),
                importConfig.getSheetMap().getIssueKey(),
                importConfig.getDataStartingRowIndex()));

        ScopeImporter importerScope = new ScopeImporter(importConfig, jiraFacade, importerNotifier);
        importerScope.executeImport(projectKey, scopeLines);
    }

    private void executeCostImport(
            SizingImporterSocketStatusEmmiter socketStatusEmmiter,
            String spreadsheetId,
            SpreadsheetsManager spreadsheetsManager,
            String projectKey,
            Optional<List<SizingImportLineCost>> costLines) {

        if (!costLines.isPresent())
            return;

        SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();
        importerNotifier.addListener(socketStatusEmmiter);
        importerNotifier.addListener(new SizingImporterSheetUpdater(
                spreadsheetId,
                SHEET_COST,
                spreadsheetsManager,
                jiraFacade.getJiraUrl(),
                importConfig.getIndirectCosts().getIssueKeyColumn(),
                costRowProvider.getDataStartingRowIndex(spreadsheetId),
                costRowProvider.getDataEndingRowIndex(spreadsheetId)));

        CostImporter importerCost = new CostImporter(importConfig, jiraFacade, importerNotifier);
        importerCost.executeImport(projectKey, costLines.get());
    }

}
