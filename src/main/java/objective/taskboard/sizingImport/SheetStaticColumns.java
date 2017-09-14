package objective.taskboard.sizingImport;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.google.SpreadsheetUtils.COLUMN_LETTER_COMPARATOR;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.sizingImport.SheetDefinition.SheetStaticColumn;

@Component
class SheetStaticColumns {

    public static final String PHASE_NAME = "Phase";
    public static final String DEMAND_NAME = "Demand";
    public static final String FEATURE_NAME = "Feature";
    public static final String KEY_NAME = "Key";
    public static final String ACCEPTANCE_CRITERIA = "Acceptance Criteria";
    public static final String INCLUDE_NAME = "Include";

    private final List<SheetStaticColumn> columns;

    @Autowired
    public SheetStaticColumns(SizingImportConfig importConfig) {
        List<SheetStaticColumn> columns = Arrays.asList(
                new SheetStaticColumn(PHASE_NAME, importConfig.getSheetMap().getIssuePhase()),
                new SheetStaticColumn(DEMAND_NAME, importConfig.getSheetMap().getIssueDemand()),
                new SheetStaticColumn(FEATURE_NAME, importConfig.getSheetMap().getIssueFeature()),
                new SheetStaticColumn(KEY_NAME, importConfig.getSheetMap().getIssueKey()),
                new SheetStaticColumn(ACCEPTANCE_CRITERIA, importConfig.getSheetMap().getIssueAcceptanceCriteria()),
                new SheetStaticColumn(INCLUDE_NAME, importConfig.getSheetMap().getInclude()));
        
        this.columns = unmodifiableList(columns.stream()
                .sorted(Comparator.comparing(SheetStaticColumn::getColumnLetter, COLUMN_LETTER_COMPARATOR))
                .collect(toList()));
    }

    public List<SheetStaticColumn> get() {
        return columns;
    }

}
