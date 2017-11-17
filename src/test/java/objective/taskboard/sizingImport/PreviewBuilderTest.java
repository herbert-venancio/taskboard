package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.SheetColumnDefinition.PreviewBehavior;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

public class PreviewBuilderTest {

    @Test
    public void test() {
        SheetColumnDefinition phaseColDefinition      = new SheetColumnDefinition("Phase");
        SheetColumnDefinition demandColDefinition     = new SheetColumnDefinition("Demand");
        SheetColumnDefinition featureColDefinition    = new SheetColumnDefinition("Feature");
        SheetColumnDefinition acceptanceColDefinition = new SheetColumnDefinition("Acceptance");
        SheetColumnDefinition keyColDefinition        = new SheetColumnDefinition("Key", PreviewBehavior.HIDE);
        SheetColumnDefinition uxColDefinition         = new SheetColumnDefinition("UX");
        SheetColumnDefinition devColDefinition        = new SheetColumnDefinition("Dev");

        SheetColumn phaseCol      = new SheetColumn(phaseColDefinition,      "A");
        SheetColumn demandCol     = new SheetColumn(demandColDefinition,     "B");
        SheetColumn featureCol    = new SheetColumn(featureColDefinition,    "C");
        SheetColumn acceptanceCol = new SheetColumn(acceptanceColDefinition, "D");
        SheetColumn keyCol        = new SheetColumn(keyColDefinition,        "E");
        SheetColumn uxCol         = new SheetColumn(uxColDefinition,         "S");
        SheetColumn devCol        = new SheetColumn(devColDefinition,        "R");

        List<SizingImportLine> data = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Blue"),
                        new ImportValue(featureCol,    "Banana"),
                        new ImportValue(acceptanceCol, "Monkey"),
                        new ImportValue(keyCol,        "ZOO-1"),
                        new ImportValue(uxCol,         "U0"),
                        new ImportValue(devCol,        "D0"))),
        
                new SizingImportLine(1, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Blue"),
                        new ImportValue(featureCol,    "Lemon"),
                        new ImportValue(acceptanceCol, "Elephant"),
                        new ImportValue(uxCol,         "U1"),
                        new ImportValue(devCol,        "D1"))),
                
                new SizingImportLine(1, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Red"),
                        new ImportValue(featureCol,    "Grape"),
                        new ImportValue(acceptanceCol, "Lion"),
                        new ImportValue(devCol,        "D2"))));

        ImportPreview preview = new PreviewBuilder()
                .setData(data)
                .build();
        
        assertEquals(asList("Phase", "Demand", "Feature", "Acceptance", "Dev", "UX"), preview.getHeaders());
        assertRows(preview.getRows(), asList(
                asList("One",   "Blue",   "Banana",  "Monkey",     "D0",  "U0"),
                asList("One",   "Blue",   "Lemon",   "Elephant",   "D1",  "U1"),
                asList("One",   "Red",    "Grape",   "Lion",       "D2",  ""  )));
    }

    private static void assertRows(List<List<String>> actual, List<List<String>> expected) {
        assertEquals(
                collectionToString(expected, l -> collectionToString(l, ", "), "\n"), 
                collectionToString(actual, l -> collectionToString(l, ", "), "\n"));

    }
}
