package objective.taskboard.it;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LaneFragment extends AbstractUiFragment {
    WebElement laneRoot;

    public LaneFragment(WebDriver driver, String laneName) {
        super(driver);
        laneRoot = driver.findElement(By.cssSelector("[data-lane-name='"+laneName+"']"));
    }

    public BoardStepFragment boardStep(String stepName) {
        WebElement webStepElement = laneRoot.findElement(By.cssSelector("[data-board-step='board-step-" + stepName + "']"));;
        return new BoardStepFragment(webDriver, stepName, webStepElement);
    }

    public static LaneFragment laneName(WebDriver webDriver, String laneName) {
        return new LaneFragment(webDriver, laneName);
    }

}
