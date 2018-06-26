package objective.taskboard.it;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MenuFilters extends AbstractUiFragment {
    @FindBy(tagName = "aspects-filter")
    private WebElement aspectsFilterButton;

    @FindBy(css = ".aspect-item-filter.config-item-title")
    private List<WebElement> aspectItemFilters;

    @FindBy(tagName = "aspect-subitem-filter")
    private List<WebElement> aspectSubitemFilters;

    @FindBy(tagName = "config-projects")
    private WebElement projectsConfigurationButton;

    @FindBy(css = ".config-projects.config-item-project")
    private List<WebElement> projectsConfigurationItem;

    public MenuFilters(WebDriver webDriver) {
        super(webDriver);
    }

    public MenuFilters openAspectsFilter() {
        waitForClick(aspectsFilterButton);
        return this;
    }

    public MenuFilters openProjectsConfiguration() {
        waitForClick(projectsConfigurationButton);
        return this;
    }

    public MenuFilters clickCheckAllFilter(String filterName) {
        WebElement checkAll = aspectItemFilters.stream()
            .filter(i -> filterName.equals(i.getText()))
            .map(i -> i.findElement(By.id("checkAll")))
            .findFirst().orElse(null);

        if (checkAll == null)
            throw new IllegalArgumentException("Element \"checkAll\" of " + filterName + " filter  not found");

        waitForClick(checkAll);
        return this;
    }

    public ProjectConfigurationDialog openProjectConfigurationModal(String projectKey) {
        WebElement projectItemButton = projectsConfigurationItem.stream().filter(el -> projectKey.equals(el.getText())).findFirst().orElse(null);

        if (projectItemButton == null)
            throw new IllegalArgumentException("Element  for project key " + projectKey + " filter  not found");

        waitForClick(projectItemButton);

        return new ProjectConfigurationDialog(webDriver, projectKey).assertIsOpen();
    }

    public MenuFilters clickAspectSubitemFilter(String filterName, String subitemFilterName) {
        WebElement aspectItemFilter = aspectItemFilters.stream()
            .filter(item -> filterName.equals(item.getText()))
            .findFirst().orElse(null);

        if (aspectItemFilter == null)
            throw new IllegalArgumentException(filterName + " filter  not found");

        waitForClick(aspectItemFilter);

        WebElement aspectSubitemFilter = aspectSubitemFilters.stream()
            .filter(subitem -> subitemFilterName.equals(subitem.getText()))
            .findFirst().orElse(null);

        if (aspectSubitemFilter == null)
            throw new IllegalArgumentException(subitemFilterName + " filter  not found");

        waitForClick(aspectSubitemFilter);
        return this;
    }

    public void closeMenuFilters() {
        waitForClick(webDriver.findElement(By.id("scrim")));
        waitInvisibilityOfElement(aspectsFilterButton);
    }
}
