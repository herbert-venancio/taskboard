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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_JSON;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_ZIP;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.FILE_NAME_FORMAT;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.PATH_FOLLOWUP_HISTORY;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joda.time.DateTime;
import org.junit.Test;

public class FollowupGeneratorUiIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenFollowupGeneratorButtonIsClicked_OpenDialogAndTestFollowupGeneratorWorks() {
        Integer projectIndex = 0;
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage
            .openFollowUp()
            .assertDateDropdownIsInvisible()
            .assertNoMatchingTemplateWarningIsInvisible()
            .assertGenerateButtonIsDisabled()
            .selectAProject(projectIndex)
            .assertDateDropdownIsInvisible()
            .assertNoMatchingTemplateWarningIsVisible()
            .assertGenerateButtonIsDisabled()
            .clickSetTemplateLink()
            .createATemplate("Template Success Test", projectIndex)
            .close();

        mainPage
            .openFollowUp()
            .selectAProject(projectIndex)
            .assertDateDropdownIsInvisible()
            .assertNoMatchingTemplateWarningIsInvisible()
            .assertGenerateButtonIsEnabled();
    }

    @Test
    public void givenAProjectWithHistory_whenSelectThisProject_thenTheDateFieldAppears() throws IOException {
        Path pathZip = null;
        try {
            Path pathProject = Paths.get(PATH_FOLLOWUP_HISTORY, "TASKB");
            createDirectories(pathProject);

            DateTime yesterday = DateTime.now().minusDays(1);
            String yesterdayString = yesterday.toString(FILE_NAME_FORMAT);
            pathZip = pathProject.resolve(yesterdayString + EXTENSION_JSON + EXTENSION_ZIP);
            if (!pathZip.toFile().exists())
                createFile(pathZip);

            Integer projectTASKBIndex = 0;
            Integer projectPROJ1Index = 1;
            MainPage mainPage = MainPage.produce(webDriver);

            mainPage
                .openTemplateFollowUpDialog()
                .createATemplate("Template Success Test", projectTASKBIndex)
                .createATemplate("Template Success Test 2", projectTASKBIndex, projectPROJ1Index)
                .close();

            mainPage
                .openFollowUp()
                .selectAProject(projectTASKBIndex)
                .selectADate(yesterdayString)
                .assertGenerateButtonIsEnabled()
                .clickClearDate()
                .assertDateIsToday()
                .assertMultipleProjectsWarningIsInvisible()
                .assertGenerateButtonIsEnabled()
                .selectAProject(projectPROJ1Index)
                .assertDateDropdownIsInvisible()
                .assertMultipleProjectsWarningIsVisible()
                .assertGenerateButtonIsEnabled()
                .close();
        } finally {
            if (pathZip != null)
                deleteQuietly(pathZip.toFile());
        }
    }
}
