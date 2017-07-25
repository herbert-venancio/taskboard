package objective.taskboard.sizingImport;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SheetDefinition.SheetStaticColumn;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

@Component
class SizingDataProvider {
    
    private final SheetStaticColumns staticColumns;
    private final SizingSheetParser parser;
    
    @Autowired
    public SizingDataProvider(SheetStaticColumns staticColumns, SizingSheetParser parser) {
        this.staticColumns = staticColumns;
        this.parser = parser;
    }

    public List<SizingImportLine> getData(
            SpreadsheetsManager spreadsheetsManager, 
            String spreadsheetId, 
            List<SheetColumnMapping> dynamicColumnsMapping) {
        
        String endColumn = maxColumn(dynamicColumnsMapping);
        String range = "A:" + endColumn;
        
        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, range);
        return parser.getSpreedsheetData(rows, dynamicColumnsMapping);
    }

    private String maxColumn(List<SheetColumnMapping> dynamicColumnsMapping) {
        Stream<String> staticLetters = staticColumns.get().stream().map(SheetStaticColumn::getColumnLetter);
        Stream<String> dynamicLetters = dynamicColumnsMapping.stream().map(SheetColumnMapping::getColumnLetter);

        return Stream.concat(staticLetters, dynamicLetters)
                .max(Comparator.naturalOrder())
                .get();
    }
}
