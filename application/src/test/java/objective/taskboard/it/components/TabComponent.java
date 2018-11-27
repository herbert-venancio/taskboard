package objective.taskboard.it.components;

import com.google.common.base.Suppliers;
import objective.taskboard.it.AbstractUiFragment;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.util.function.Supplier;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

public class TabComponent<T extends AbstractUiFragment> extends AbstractComponent {

    public final TabsRouterComponent tabs;
    public final String tabName;
    public final String componentName;
    public final Supplier<T> fragment;

    public TabComponent(
            WebDriver webDriver,
            TabsRouterComponent tabs,
            String tabName,
            String componentName,
            TabsRouterComponent.TabFactory<T> fragment) {
        super(webDriver, new ByChained(tabs.componentSelector, cssSelector(TabsRouterComponent.TAB_ROUTER_TAG +"[name=\""+ tabName +"\"]")));
        this.tabs = tabs;
        this.tabName = tabName;
        this.componentName = componentName;
        this.fragment = Suppliers.memoize(() -> fragment.apply(webDriver, this));
    }

    public <TO extends AbstractUiFragment> TabsRouterComponent.SelectTab<T, TO> selectTab(String to) {
        return tabs.selectTab(this.tabName, to);
    }

    public T waitIsSelected() {
        waitTabHasActiveClass(component());
        waitContentUpdated(componentName);
        return fragment.get();
    }

    private void waitTabHasActiveClass(WebElement tab) {
        waitAttributeValueInElementContains(tab, "class", "active");
    }

    private void waitContentUpdated(String componentTag) {
        WebElement tabContent = getChildElementWhenExists(tabs.component(), cssSelector(".content"));
        waitUntilChildElementExists(tabContent, tagName(componentTag));
    }

    public void waitUntilExists() {
        waitUntilElementExists(componentSelector);
    }
}
