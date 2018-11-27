package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.testUtils.ProjectInfo;

public class ProjectChangeRequestConfigurationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;

    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }

    @Test
    public void shouldPersistWhenValidRowsAreInserted() {
        openFromProjectConfig(TASKB)
            .addItem()
            .setName(0, "Dev")
            .setDate(0, "01/01/2018")
            .setBudgetIncrease(0, "26")
            
            .addItem()
            .setName(0, "Doc")
            .setDate(0, "01/01/2019")
            .setBudgetIncrease(0, "12")
            
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()
            .assertItems(
                    "Doc | 01/01/2019 | 12",
                    "Dev | 01/01/2018 | 26",
                    "38");
    }

    @Test
    public void shouldEditWhenValidRowsAreInsertedAndReplace() {
        openFromProjectConfig(TASKB)
            .addItem()
            .setName(0, "Dev")
            .setDate(0, "01/01/2018")
            .setBudgetIncrease(0, "35")
            
            .addItem()
            .setName(0, "Doc")
            .setDate(0, "01/01/2019")
            .setBudgetIncrease(0, "10")
            
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()
            
            .remove(1)
            .setName(0, "Dev")
            .setDate(0, "01/05/2018")
            .setBudgetIncrease(0, "46")
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()

            .assertItems(
                    "Dev | 01/05/2018 | 46",
                    "46");
    }

    private ProjectChangeRequestConfigurationTab openFromProjectConfig(ProjectInfo projectInfo) {
        return ProjectConfigurationOperator.openFromMainMenu(mainPage, projectInfo)
            .openAdvancedConfigurations()
            .selectChangeRequestConfiguration()
            .assertTabIsOpen();
    }
}
