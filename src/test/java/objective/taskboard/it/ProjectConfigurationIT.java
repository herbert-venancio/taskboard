package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

public class ProjectConfigurationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;
    
    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }
    
    @Test
    public void shouldValidate() {
        ProjectConfigurationOperator.openFromMainMenu(mainPage, TASKB)
            .setStartDate("99999")
            .setDeliveryDate("99/20/2018")
            .setRisk("-50")
            .update()
            .assertStartDateError("Please enter a valid date")
            .assertDeliveryDateError("Please enter a valid date")
            .assertRiskError("Please enter a positive value or zero")

            .setStartDate("01/20/2018")
            .setDeliveryDate("01/15/2018")
            .setRisk("0")
            .update()
            .assertGlobalError("End Date should be greater than Start Date");
    }
    
    @Test
    public void shouldPersist() {
        ProjectConfigurationOperator.openFromMainMenu(mainPage, TASKB)
            .setStartDate("01/25/2018")
            .setDeliveryDate("10/20/2018")
            .setRisk("60.99")
            .setBaselineDate("06/01/2017")
            .update()
            .assertSuccessAlertIsOpen()
            .closeSuccessAlert();
        
        ProjectConfigurationOperator.openFromMainMenu(mainPage, TASKB)
            .assertStartDate("01/25/2018")
            .assertDeliveryDate("10/20/2018")
            .assertRisk("60.99")
            .assertBaselineDate("06/01/2017");
    }
    
    @Test
    public void shouldGoToEditProfile() {
        ProjectConfigurationOperator.openFromMainMenu(mainPage, TASKB)
            .openAdvancedConfigurations()
            .selectProfileConfiguration()
            .assertTabIsOpen();
    }

    @Test
    public void whenFormHasPendingChanges_ShouldConfirmSaveBeforeGoToEditProfile() {
        ProjectConfigurationOperator.openFromMainMenu(mainPage, TASKB)
            .setRisk("10.1")
            .clickAdvancedConfigurationsExpectingConfirmation()
            .assertSaveConfirmModalIsOpen()
            .confirmSave();

        new ProjectAdvancedConfigurationsPage(webDriver, TASKB)
            .selectProfileConfiguration()
            .backToProject()
            .assertRisk("10.1");
    }
}
