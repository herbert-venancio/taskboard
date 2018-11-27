package objective.taskboard.it;

import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class AbstractAppUiFragment extends AbstractUiFragment {

    private By loader = cssSelector("tb-page-spinner");

    public AbstractAppUiFragment(WebDriver driver) {
        super(driver);
    }

    protected void waitPageLoadBeShowed() {
        waitElementExistenceAndVisibilityIs(true, loader);
    }

    protected void waitPageLoaderBeHide() {
        waitElementNotExistsOrInvisible(loader);
    }

}
