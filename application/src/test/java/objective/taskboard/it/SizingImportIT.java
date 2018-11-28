package objective.taskboard.it;

import org.junit.Before;
import org.junit.Test;

public class SizingImportIT extends AuthenticatedIntegrationTest {

    private final static String SPREADSHEET_URL_VALID = "https://docs.google.com/spreadsheets/d/1iJqwMxLWOr1fDcYDNuV-CUp-1smHpYtYqvaTk14Nwek";
    private final static String SPREADSHEET_URL_INVALID = "https://docs.google.com/spreadsheets/d/";

    private final static String LAST_COLUMN = "AB";
    private final static String INVALID_COLUMN = "ZZZZ";
    
    private MainPage mainPage;
    
    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }

    @Test
    public void whenFinishAllSteps_ImportSizingAndClose() {
        SizingImportUi sizing = mainPage.openSizingImport();
        sizing.assertIsOpen();

        sizing.waitForStepOne()
            .withSpreadsheetUrl(SPREADSHEET_URL_VALID)
            .submitStep();

        sizing.waitForStepTwo().submitStep();

        sizing
            .waitForStepThree()
            .assertScopeTabIsActive()
            .assertHeader("Phase | Demand | Feature | Type | Accep. Criter. | Feature Planning | UX | Development | " +
                    "Tech Planning | Alpha Test | QA Support | Feature Review | T-Shirt Size")
            .selectCostTab()
            .assertHeader("Indirect Costs | Effort")
            .submitStep();

        sizing.waitForStepFour()
            .assertSummary(" | Scope | Cost",
                    "Total lines | 12 | 5",
                    "Lines to import | 12 | 5",
                    "Imported lines | 0 | 3",
                    "Failed lines | 12 | 2")
            .assertShowingButtonsAfterFinish()
            .returnToTaskboard();

        sizing.assertIsClosed();
    }

    @Test
    public void whenSubmitStepOneWithoutFillSpreadsheetUrl_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.waitForStepOne().submitStep();
        
        sizing.assertErrorMessage("Please, inform the Google Spreadsheet URL to import.");
        sizing.waitForStepOne();
    }
    
    @Test
    public void whenSubmitStepOneUsingASpreadsheetWithoutScopeSheet_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();
        
        sizing.waitForStepOne()
            .withSpreadsheetUrl("https://docs.google.com/spreadsheets/d/spreadsheet-without-scope-sheet")
            .submitStep();
        
        sizing.assertErrorMessage(
                "Invalid spreadsheet format: Specified URL should contain a sheet with title “Scope”.", 
                "Found sheets: Timeline, Cost");
        
        sizing.waitForStepOne();
    }

    @Test
    public void whenSubmitStepOneUsingASpreadsheetWithoutCostSheet_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.waitForStepOne()
            .withSpreadsheetUrl("https://docs.google.com/spreadsheets/d/spreadsheet-without-cost-sheet")
            .submitStep();

        sizing.assertErrorMessage(
                "Invalid spreadsheet format: Specified URL should contain a sheet with title “Cost”.", 
                "Found sheets: Scope, Timeline");

        sizing.waitForStepOne();
    }

    @Test
    public void whenInputtingColumnValues_CheckIfTheValuesAreCorrect() {
        SizingImportUi sizing = mainPage.openSizingImport();
        
        sizing.waitForStepOne()
            .withSpreadsheetUrl(SPREADSHEET_URL_VALID)
            .submitStep();
        
        sizing.waitForStepTwo()
                .openAdvancedMapping()
                .withColumnValue(0, INVALID_COLUMN)
                .withColumnValue(1, LAST_COLUMN)
                .assertColumnValueEquals(0, INVALID_COLUMN, false)
                .assertColumnValueEquals(1, LAST_COLUMN, true);

        sizing.assertNoErrorMessage();
        
        sizing.waitForStepTwo().submitStep();
        sizing.waitForStepThree();
    }

    @Test
    public void whenSubmitStepOneWithInvalidSpreadsheetUrl_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.waitForStepOne()
            .withSpreadsheetUrl(SPREADSHEET_URL_INVALID)
            .submitStep();

        sizing.assertErrorMessage("Invalid Google Spreadsheet URL. Please, try another.");
        sizing.waitForStepOne();
    }

    @Test
    public void whenSubmitStepTwoWithRepeatedColumnField_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.waitForStepOne()
            .withSpreadsheetUrl(SPREADSHEET_URL_VALID)
            .submitStep();

        sizing.waitForStepTwo()
            .openAdvancedMapping()
            .withRepeatedColumns()
            .submitStep();

        sizing.assertErrorMessage("Column repeated.");
        sizing.waitForStepTwo();
    }

    @Test
    public void whenSubmitStepTwoWithoutFillRequiredFields_ShowError() {
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.waitForStepOne()
            .withSpreadsheetUrl(SPREADSHEET_URL_VALID)
            .submitStep();
        
        sizing.waitForStepTwo()
            .openAdvancedMapping()
            .withRequiredFieldsWithoutValues()
            .submitStep();

        sizing.assertErrorMessage("All required fields must have columns associated.");
        sizing.waitForStepTwo();
    }

}