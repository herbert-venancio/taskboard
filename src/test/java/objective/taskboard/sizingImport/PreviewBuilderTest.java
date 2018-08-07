package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.PreviewBuilder.ImportSheetPreview;
import objective.taskboard.sizingImport.SheetColumnDefinition.PreviewBehavior;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;
import objective.taskboard.sizingImport.cost.SizingImportLineCost;

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

        List<SizingImportLineScope> scopeLines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Blue"),
                        new ImportValue(featureCol,    "Banana"),
                        new ImportValue(acceptanceCol, "Monkey"),
                        new ImportValue(keyCol,        "ZOO-1"),
                        new ImportValue(uxCol,         "U0"),
                        new ImportValue(devCol,        "D0"))),
        
                new SizingImportLineScope(1, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Blue"),
                        new ImportValue(featureCol,    "Lemon"),
                        new ImportValue(acceptanceCol, "Elephant"),
                        new ImportValue(uxCol,         "U1"),
                        new ImportValue(devCol,        "D1"))),
                
                new SizingImportLineScope(1, asList(
                        new ImportValue(phaseCol,      "One"),
                        new ImportValue(demandCol,     "Red"),
                        new ImportValue(featureCol,    "Grape"),
                        new ImportValue(acceptanceCol, "Lion"),
                        new ImportValue(devCol,        "D2"))));

        SheetColumnDefinition indirectCostsColDefinition    = new SheetColumnDefinition("Indirect Costs");
        SheetColumnDefinition indirectCostsKeyColDefinition = new SheetColumnDefinition("Key", PreviewBehavior.HIDE);
        SheetColumnDefinition effortColDefinition           = new SheetColumnDefinition("Effort");

        SheetColumn indirectCostsCol    = new SheetColumn(indirectCostsColDefinition,    "A");
        SheetColumn indirectCostsKeyCol = new SheetColumn(indirectCostsKeyColDefinition, "B");
        SheetColumn effortCol           = new SheetColumn(effortColDefinition,           "C");

        List<SizingImportLineCost> costLines = asList(
                new SizingImportLineCost(0, asList(
                        new ImportValue(indirectCostsCol,    "Management"),
                        new ImportValue(indirectCostsKeyCol, "ZOO-1"),
                        new ImportValue(effortCol,           "7"))),

                new SizingImportLineCost(1, asList(
                        new ImportValue(indirectCostsCol,    "Coach"),
                        new ImportValue(indirectCostsKeyCol, "ZOO-2"),
                        new ImportValue(effortCol,           "8"))));

        ImportPreview preview = new PreviewBuilder()
                .setScopeLines(scopeLines)
                .setCostLines(costLines)
                .build();
        
        ImportSheetPreview scopePreview = preview.getScopePreview();
        assertNotNull(scopePreview);
        assertEquals("Scope", scopePreview.getSheetTitle());
        assertEquals(asList("Phase", "Demand", "Feature", "Acceptance", "Dev", "UX"), scopePreview.getHeaders());
        assertRows(scopePreview.getRows(), asList(
                asList("One",   "Blue",   "Banana",  "Monkey",     "D0",  "U0"),
                asList("One",   "Blue",   "Lemon",   "Elephant",   "D1",  "U1"),
                asList("One",   "Red",    "Grape",   "Lion",       "D2",  ""  )));
        assertEquals(3, scopePreview.getTotalLinesCount());

        ImportSheetPreview costPreview = preview.getCostPreview();
        assertNotNull(costPreview);
        assertEquals("Cost", costPreview.getSheetTitle());
        assertEquals(asList("Indirect Costs", "Effort"), costPreview.getHeaders());
        assertRows(costPreview.getRows(), asList(
                asList("Management", "7"),
                asList("Coach",      "8")));
        assertEquals(2, costPreview.getTotalLinesCount());

        ImportPreview previewLimited = new PreviewBuilder()
                .setScopeLines(scopeLines)
                .setLinesLimit(2)
                .build();

        ImportSheetPreview scopePreviewLimited = previewLimited.getScopePreview();
        assertNotNull(scopePreviewLimited);
        assertRows(scopePreviewLimited.getRows(), asList(
                asList("One",   "Blue",   "Banana",  "Monkey",     "D0",  "U0"),
                asList("One",   "Blue",   "Lemon",   "Elephant",   "D1",  "U1")));
        assertEquals(3, scopePreviewLimited.getTotalLinesCount());
    }

    private static void assertRows(List<List<String>> actual, List<List<String>> expected) {
        assertEquals(
                collectionToString(expected, l -> collectionToString(l, ", "), "\n"), 
                collectionToString(actual, l -> collectionToString(l, ", "), "\n"));

    }
}
