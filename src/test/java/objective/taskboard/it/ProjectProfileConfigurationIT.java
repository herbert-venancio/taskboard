package objective.taskboard.it;

import static objective.taskboard.testUtils.ProjectInfo.TASKB;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.testUtils.ProjectInfo;

public class ProjectProfileConfigurationIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage;
    
    @Before
    public void setup() {
        mainPage = MainPage.produce(webDriver);
    }
    
    @Test
    public void shouldPersist() {
        openFromProjectConfig(TASKB)
            .addItem()
            .setRoleName(0, "Dev")
            .setPeopleCount(0, "10")
            .setAllocationStart(0, "01/01/2018")
            .setAllocationEnd(0, "03/25/2018")
            
            .addItem()
            .setRoleName(0, "UX")
            .setPeopleCount(0, "1.5")
            .setAllocationStart(0, "01/01/2018")
            .setAllocationEnd(0, "01/15/2018")
            
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()
            .assertItems(
                    "Dev | 10 | 01/01/2018 | 03/25/2018",
                    "UX | 1.5 | 01/01/2018 | 01/15/2018");
    }
    
    @Test
    public void shouldEdit() {
        openFromProjectConfig(TASKB)
            .addItem()
            .setRoleName(0, "Dev")
            .setPeopleCount(0, "10")
            .setAllocationStart(0, "01/01/2018")
            .setAllocationEnd(0, "03/25/2018")
            
            .addItem()
            .setRoleName(0, "UX")
            .setPeopleCount(0, "1.5")
            .setAllocationStart(0, "01/01/2018")
            .setAllocationEnd(0, "01/15/2018")
            
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()
            
            .remove(1)
            .setRoleName(0, "X-Dev")
            .setPeopleCount(0, "1")
            .setAllocationStart(0, "03/24/2018")
            .save()
            .assertSnackbarSavedIsOpen()
            .refresh()

            .assertItems("X-Dev | 1 | 03/24/2018 | 03/25/2018");
    }

    private ProjectProfileConfigurationTab openFromProjectConfig(ProjectInfo projectInfo) {
        return ProjectConfigurationOperator.openFromMainMenu(mainPage, projectInfo)
            .openAdvancedConfigurations()
            .selectProfileTab()
            .assertTabIsOpen();
    }
}
