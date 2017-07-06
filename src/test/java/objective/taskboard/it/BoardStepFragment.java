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
package objective.taskboard.it;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BoardStepFragment extends AbstractUiFragment {

    private WebElement boardStepRoot;
    private String stepName;

    public BoardStepFragment(WebDriver driver, String stepName, WebElement root) {
        super(driver);
        this.stepName = stepName;
        this.boardStepRoot = root;
    }

    public void assertIssueList(String ...expectedIssueKeyList) {
        waitUntil((w) ->                
            boardStepRoot.findElements(By.cssSelector("paper-material.issue")).size() ==
            expectedIssueKeyList.length);
        
        List<WebElement> findElements = boardStepRoot.findElements(By.cssSelector("paper-material.issue"));
        ArrayList<String> actualIssueKeyList = new ArrayList<String>(); 
        for (WebElement webElement : findElements) 
            actualIssueKeyList.add( webElement.findElement(By.cssSelector(".key.issue-item")).getText().trim());
        Arrays.sort(expectedIssueKeyList);
        Collections.sort(actualIssueKeyList);
        
        assertEquals(join(expectedIssueKeyList,"\n"), join(actualIssueKeyList,"\n"));
    }

    public void issueCountBadge(int i) {
        webDriver.findElement(By.cssSelector("[data-step-count-name='"+stepName+"']"));
    }

}
