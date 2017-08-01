package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SizingImportService.ImportPreview;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

class PreviewBuilder {
    
    private final List<SheetColumnDefinition> dynamicColumnsDefinition;
    private final List<SheetColumnMapping> dynamicColumnsMapping;
    private List<SizingImportLine> data = Collections.emptyList();
    private int linesLimit = 10;
    
    public PreviewBuilder(List<SheetColumnDefinition> dynamicColumnsDefinition, List<SheetColumnMapping> dynamicColumnsMapping) {
        this.dynamicColumnsDefinition = dynamicColumnsDefinition;
        this.dynamicColumnsMapping = dynamicColumnsMapping;
    }

    public PreviewBuilder setData(List<SizingImportLine> data) {
        this.data = data;
        return this;
    }

    public PreviewBuilder setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
        return this;
    }
    
    public ImportPreview build() {
        Map<String, String> dynamicColumnsNameByFieldId = dynamicColumnsDefinition.stream()
                .collect(toMap(SheetColumnDefinition::getFieldId, SheetColumnDefinition::getName));
        
        List<SheetColumnMapping> sortedDynamicColumnsMapping = dynamicColumnsMapping.stream()
                .sorted(Comparator.comparing(SheetColumnMapping::getColumnLetter))
                .collect(toList());
        
        List<String> headers = new ArrayList<>();
        headers.add(SheetStaticColumns.PHASE_NAME);
        headers.add(SheetStaticColumns.DEMAND_NAME);
        headers.add(SheetStaticColumns.FEATURE_NAME);

        headers.addAll(sortedDynamicColumnsMapping.stream()
                .map(m -> dynamicColumnsNameByFieldId.get(m.getFieldId()))
                .collect(toList()));

        List<List<String>> rows = data.stream()
                .limit(linesLimit)
                .map(line -> {
                    List<String> linePreview = new ArrayList<>();
                    linePreview.add(line.getPhase());
                    linePreview.add(line.getDemand());
                    linePreview.add(line.getFeature());
                    
                    linePreview.addAll(sortedDynamicColumnsMapping.stream()
                            .map(cm -> line.getFieldValue(cm.getFieldId()).orElse(""))
                            .collect(toList()));
                    
                    return linePreview;
                })
                .collect(toList());

        return new ImportPreview(headers, rows, data.size());
    }
}
