package objective.taskboard.it;

import static java.util.Arrays.asList;

import org.junit.Test;

import objective.taskboard.it.FollowupReportType.FollowupReportTypeData;

public class FollowupReportUiIT extends AuthenticatedIntegrationTest {
    private static final String DEV = "Dev";
    private static final String REPORT_TYPE_TEST = "Report Type Test";

    @Test
    public void whenCreateANewFollowupReportType_VerifyIfItHasNameFileAndRoles() {
        MainPage mainPage = MainPage.produce(webDriver);
        FollowupReport reportWindow = mainPage.assertFollowupButtonIsVisible()
            .openFollowUpReport();

        reportWindow
            .assertReportTypes(asList())
            .clickAddLink()
            .tryCreateReportType(new FollowupReportTypeData())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withName())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withName().withFile())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withName().withRoles())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withFile())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withFile().withRoles())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withRoles())
                .assertErrorMessage()
            .tryCreateReportType(new FollowupReportTypeData().withName().withFile().withRoles())
                .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST));
    }

    @Test
    public void whenEditAFollowupReportType_VerifyIfItWasEdited() {
        MainPage mainPage = MainPage.produce(webDriver);
        FollowupReport reportWindow = mainPage.assertFollowupButtonIsVisible()
            .openFollowUpReport();

        reportWindow
            .clickAddLink()
            .tryCreateReportType(new FollowupReportTypeData().withName().withFile().withRoles())
            .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST))
            .clickEditReportType()
            .close()
            .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST))
            .clickEditReportType()
            .setName("Report Type Test edited")
            .close()
                .clickOnCancelDiscardMessage()
            .close()
                .clickOnConfirmDiscardMessage()
            .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST))
            .clickEditReportType()
            .editReportType();

        reportWindow
            .assertReportTypes(asList("Report Type Test edited"));
    }

    @Test
    public void whenDeleteAFollowupReportType_VerifyIfItWasDeleted() {
        MainPage mainPage = MainPage.produce(webDriver);
        FollowupReport reportWindow = mainPage.assertFollowupButtonIsVisible()
            .openFollowUpReport();

        reportWindow
            .clickAddLink()
            .tryCreateReportType(new FollowupReportTypeData().withName().withFile().withRoles())
            .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST))
            .clickDeleteReportType()
            .clickOnCancelDelete()
                .assertProjectIsEnabled()
                .assertReportTypes(asList(REPORT_TYPE_TEST))
            .clickDeleteReportType()
            .clickOnConfirmDelete()
                .assertReportTypes(asList())
                .assertProjectIsDisabled()
                .assertGenerateButtonIsDisabled();
    }

    @Test
    public void whenDoesNotHaveAProjectSelected_VerifyDateAndGenerateButtonAreDisabled() {
        MainPage mainPage = MainPage.produce(webDriver);
        FollowupReport reportWindow = mainPage.assertFollowupButtonIsVisible()
            .openFollowUpReport();

        reportWindow
            .assertReportTypes(asList())
            .assertProjectIsDisabled()
            .assertDateDropdownIsDisabled()
            .assertGenerateButtonIsDisabled()
            .clickAddLink()
            .tryCreateReportType(new FollowupReportTypeData().withName().withFile().withRoles())
            .waitClose();

        reportWindow
            .assertReportTypes(asList(REPORT_TYPE_TEST))
            .selectReportType(REPORT_TYPE_TEST)
                .assertProjectIsEnabled()
                .assertDateIsToday()
                .assertDateDropdownIsDisabled()
                .assertGenerateButtonIsDisabled()
            .setProject("PROJ1")
                .assertDateIsToday()
                .assertDateDropdownIsEnabled()
                .assertGenerateButtonIsEnabled()
            .selectDate("06/01/2017")
                .assertGenerateButtonIsEnabled()
            .clickClearDate()
                .assertDateIsToday()
                .assertGenerateButtonIsEnabled()
            .selectDate("06/01/2017")
            .setProject("")
                .assertDateIsToday()
                .assertDateDropdownIsDisabled()
                .assertGenerateButtonIsDisabled();

        reportWindow
            .clickAddLink()
            .tryCreateReportType(new FollowupReportTypeData().withName(DEV).withFile().withRoles("Developers"))
            .waitClose();

        reportWindow
                .assertReportTypes(asList(DEV, REPORT_TYPE_TEST))
                .assertProjectIsDisabled()
                .assertDateDropdownIsDisabled()
                .assertGenerateButtonIsDisabled()
            .selectReportType(REPORT_TYPE_TEST)
            .setProject("PROJ3")
                .assertProjectNotFoundIsNotVisible()
            .selectReportType(DEV)
                .assertDateDropdownIsDisabled()
                .assertGenerateButtonIsDisabled()
            .setProject("PROJ3")
                .assertProjectNotFoundIsVisible()
                .assertDateDropdownIsDisabled()
                .assertGenerateButtonIsDisabled()
            .setProject("PROJ1")
                .assertProjectNotFoundIsNotVisible()
                .assertDateDropdownIsEnabled()
                .assertGenerateButtonIsEnabled();
    }

}
