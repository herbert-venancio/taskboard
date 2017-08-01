package objective.taskboard.it;

import org.junit.Test;

import objective.taskboard.it.SizingImportUi.SizingStepFour;
import objective.taskboard.it.SizingImportUi.SizingStepOne;
import objective.taskboard.it.SizingImportUi.SizingStepThree;
import objective.taskboard.it.SizingImportUi.SizingStepTwo;

public class SizingImportIT extends AuthenticatedIntegrationTest {

    final static String SPREADSHEET_URL_VALID = "https://docs.google.com/spreadsheets/d/1iJqwMxLWOr1fDcYDNuV-CUp-1smHpYtYqvaTk14Nwek";
    final static String SPREADSHEET_URL_INVALID = "https://docs.google.com/spreadsheets/d/";

    final static String LAST_COLUMN = "AB";
    final static String INVALID_COLUMN = "AAAA";

    @Test
    public void whenFinishAllSteps_ImportSizingAndClose() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();

        SizingStepOne stepOne = sizing.showStepOne();

        sizing.assertShowingCorrectStep(1);
        sizing.asserWithShowingSizing(true);

        SizingStepTwo stepTwo = stepOne.
                withSpreadsheetUrl(SPREADSHEET_URL_VALID).
                submitStep().
                showStepTwo();
        sizing.assertShowingCorrectStep(2);

        SizingStepThree stepThree = stepTwo.
                submitStep().
                showStepThree();
        sizing.assertShowingCorrectStep(3);

        SizingStepFour stepFour = stepThree.
                submitStep().
                showStepFour();
        sizing.assertShowingCorrectStep(4);

        stepFour.assertShowingButtonsAfterFinish();
        sizing.assertWithShowingError(false);

        stepFour.returnToTaskboard();
        sizing.asserWithShowingSizing(false);
    }

    @Test
    public void whenSubmitStepOneWithoutFillSpreadsheetUrl_ShowError() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.
            showStepOne().
            submitStep();
        
        sizing.assertWithShowingError(true);
        sizing.assertShowingCorrectStep(1);
    }
	
    @Test
    public void whenInputtingColumnValues_CheckIfTheValuesAreCorrect() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();
        
        SizingStepTwo stepTwo = sizing.
            showStepOne().
            withSpreadsheetUrl(SPREADSHEET_URL_VALID).
            submitStep().
            showStepTwo().
            withColumnValue(0, INVALID_COLUMN).
            withColumnValue(1, LAST_COLUMN);

        stepTwo.assertColumnValueEquals(0, INVALID_COLUMN, false);
        stepTwo.assertColumnValueEquals(1, LAST_COLUMN, true);

        sizing.assertWithShowingError(false);
    }

    @Test
    public void whenSubmitStepOneWithInvalidSpreadsheetUrl_ShowError() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.
            showStepOne().
            withSpreadsheetUrl(SPREADSHEET_URL_INVALID).
            submitStep();

        sizing.assertWithShowingError(true);
        sizing.assertShowingCorrectStep(1);
    }

    @Test
    public void whenSubmitStepTwoWithRepeatedColumnField_ShowError() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.
            showStepOne().
            withSpreadsheetUrl(SPREADSHEET_URL_VALID).
            submitStep().
            showStepTwo().
            withRepeatedColumns().
            submitStep();

        sizing.assertWithShowingError(true);
        sizing.assertShowingCorrectStep(2);
    }

    @Test
    public void whenSubmitStepTwoWithoutFillRequiredFields_ShowError() {
        MainPage mainPage = MainPage.produce(webDriver);
        SizingImportUi sizing = mainPage.openSizingImport();

        sizing.
            showStepOne().
            withSpreadsheetUrl(SPREADSHEET_URL_VALID).
            submitStep().
            showStepTwo().
            withRequiredFieldsWithoutValues().
            submitStep();

        sizing.assertWithShowingError(true);
        sizing.assertShowingCorrectStep(2);
    }

}