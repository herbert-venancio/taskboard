package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SizingImportLine.JiraField;
import objective.taskboard.sizingImport.SizingImportService.ImportPreview;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

public class PreviewBuilderTest {

    @Test
    public void test() {
        List<SheetColumnDefinition> dynamicColumnsDefinition = asList(
                new SheetColumnDefinition("f1", "UX",   true,  "F"),
                new SheetColumnDefinition("f2", "Dev",  true),
                new SheetColumnDefinition("f3", "Test", false, "H"));
        
        List<SheetColumnMapping> dynamicColumnsMapping = asList(
                new SheetColumnMapping("f1", "F"),
                new SheetColumnMapping("f2", "D"));
        
        List<SizingImportLine> data = new ArrayList<>();
        
        SizingImportLine line = new SizingImportLine();
        line.setPhase("One");
        line.setDemand("Blue");
        line.setFeature("Banana");
        line.addField(new JiraField("f1", "U0"));
        line.addField(new JiraField("f2", "D0"));
        data.add(line);
        
        line = new SizingImportLine();
        line.setPhase("One");
        line.setDemand("Blue");
        line.setFeature("Lemon");
        line.addField(new JiraField("f1", "U1"));
        line.addField(new JiraField("f2", "D1"));
        data.add(line);
        
        line = new SizingImportLine();
        line.setPhase("One");
        line.setDemand("Red");
        line.setFeature("Grape");
        line.addField(new JiraField("f1", "U2"));
        line.addField(new JiraField("f2", "D2"));
        data.add(line);
        
        ImportPreview preview = new PreviewBuilder(dynamicColumnsDefinition, dynamicColumnsMapping)
                .setData(data)
                .build();
        
        assertEquals(asList("Phase", "Demand", "Feature", "Dev", "UX"), preview.getHeaders());
        assertEquals(asList("One",   "Blue",   "Banana",  "D0",  "U0"), preview.getRows().get(0));
        assertEquals(asList("One",   "Blue",   "Lemon",   "D1",  "U1"), preview.getRows().get(1));
        assertEquals(asList("One",   "Red",    "Grape",   "D2",  "U2"), preview.getRows().get(2));
        
    }

}
