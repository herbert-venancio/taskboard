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
package objective.taskboard.followup;

import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Optional;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Sheet;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.followup.data.Template;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.followup.impl.FollowUpTemplateStorage;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.rules.CleanupDataFolderRule;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpFacadeTest {

    @Rule
    public CleanupDataFolderRule clean = new CleanupDataFolderRule(Paths.get("data/followup-templates"));

    private static final String FORMULA = "someFormula";
    private static final String DATE_NULL = "12/31/99 0:00";

    @Mock
    private TemplateService templateService;

    @Mock
    private FollowUpDataProviderFromCurrentState provider;

    @Mock
    private DataBaseDirectory dataBaseDirectory;

    @Mock
    private JiraProperties jiraProperties;

    @Spy
    @InjectMocks
    private FollowUpTemplateStorage followUpTemplateStorage = new FollowUpTemplateStorage();

    @InjectMocks
    private FollowUpFacade followUpFacade = new FollowUpFacade();

    private static final String TEMPLATE_NAME = "OkFollowupTemplate.xlsm";
    private static final String PROJECTS = "TASKB,PROJ1";
    private static final String[] INCLUDED_PROJECTS = PROJECTS.split(",");
    private Template template;

    @Before
    public void setup() throws IOException {
        when(dataBaseDirectory.path(anyString())).thenReturn(Paths.get("data/followup-templates"));

        MultipartFile file = new MockMultipartFile("file", FollowUpFacadeTest.class.getResourceAsStream(TEMPLATE_NAME));
        followUpFacade.createTemplate(TEMPLATE_NAME, PROJECTS, file);

        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateService, atLeastOnce()).saveTemplate(any(), any(), argCaptor.capture());
        String path = argCaptor.getValue();
        template = mock(Template.class);
        when(template.getPath()).thenReturn(path);

        String[] statusOrder = new String[] { "Done", "Doing", "To Do" };
        JiraProperties.StatusPriorityOrder statusPriorityOrder = new JiraProperties.StatusPriorityOrder();
        statusPriorityOrder.setDemands(statusOrder);
        statusPriorityOrder.setTasks(statusOrder);
        statusPriorityOrder.setSubtasks(statusOrder);
        when(jiraProperties.getStatusPriorityOrder()).thenReturn(statusPriorityOrder);
    }

    @Test
    public void okTemplateGenerate() throws Exception {
        given(templateService.getTemplate(TEMPLATE_NAME)).willReturn(template);
        given(provider.getJiraData(INCLUDED_PROJECTS, ZoneId.systemDefault())).willReturn(getDefaultFollowupData());

        // when
        FollowUpGenerator followupGenerator = followUpFacade.getGenerator(TEMPLATE_NAME, Optional.empty());
        Resource resource = followupGenerator.generate(INCLUDED_PROJECTS, ZoneId.systemDefault());

        // then
        assertThat(resource, hasExpectedContent());
    }

    private Matcher<Resource> hasExpectedContent() {
        return new BaseMatcher<Resource>() {

            public BaseMatcher<String[]> matcherDelegate;

            @Override
            public void describeTo(Description description) {
                matcherDelegate.describeTo(description);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                matcherDelegate.describeMismatch(item, description);
            }

            @Override
            public boolean matches(Object o) {
                Resource resource = (Resource) o;
                try {
                    SpreadsheetMLPackage excelDoc = readFile(resource);
                    String[] expectedRowContent = expectedRowContentOfFromJira();
                    String[] actualRowContent = formattedContentOfFirstRowOfFromJiraWorksheet(excelDoc);
                    matcherDelegate = (BaseMatcher<String[]>) equalTo(expectedRowContent);
                    assertThat(actualRowContent, matcherDelegate);

                    expectedRowContent = expectedRowContentOfAnalytics();
                    actualRowContent = formattedContentOfFirstRowOfAnalyticsWorksheet(excelDoc);
                    matcherDelegate = (BaseMatcher<String[]>) equalTo(expectedRowContent);
                    assertThat(actualRowContent, matcherDelegate);

                    expectedRowContent = expectedRowContentOfSyntetics();
                    actualRowContent = formattedContentOfFirstRowOfSynteticsWorksheet(excelDoc);
                    matcherDelegate = (BaseMatcher<String[]>) equalTo(expectedRowContent);
                    assertThat(actualRowContent, matcherDelegate);
                    return true;
                } catch (Exception e) {
                    throw new AssertionError(e.getMessage());
                }
            }
        };
    }

    private String[] expectedRowContentOfFromJira() {
        return new String[]{
                "PROJECT TEST", "Demand", "Doing", "I-1", "Summary Demand"
                , "Description Demand", "Feature", "Doing", "I-2", "Summary Feature"
                , "Description Feature", "Full Description Feature", "Sub-task", "Doing"
                , "I-3", "Summary Sub-task", "Description Sub-task", "Full Description Sub-task"
                , "1", "2", "3", "Ballpark", "Release", "1", "1", "1", "1", "M", "Type"
                , FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA
                , FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA
                , FORMULA, FORMULA, "9/27/17 0:00", "9/26/17 0:00", "9/25/17 0:00"
                , DATE_NULL, "9/26/17 0:00", "9/25/17 0:00", DATE_NULL, DATE_NULL, "9/25/17 0:00"};
    }

    private String[] expectedRowContentOfAnalytics() {
        return new String[]{
                "I-1", "Demand", "9/27/17 0:00", "9/26/17 0:00", "9/25/17 0:00"};
    }

    private String[] expectedRowContentOfSyntetics() {
        return new String[] {"9/25/17 0:00", "Demand", "0", "0", "1"};
    }

    private SpreadsheetMLPackage readFile(Resource resource) throws IOException, Docx4JException {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        return SpreadsheetMLPackage.load(resource.getInputStream());
    }

    private String[] formattedContentOfFirstRowOfFromJiraWorksheet(SpreadsheetMLPackage excelDoc) throws Docx4JException, Xlsx4jException {
        return extractRowContent(excelDoc, "From Jira", 1);
    }

    private String[] formattedContentOfFirstRowOfAnalyticsWorksheet(SpreadsheetMLPackage excelDoc) throws Docx4JException, Xlsx4jException {
        return extractRowContent(excelDoc, "Analytic - Demand", 1);
    }

    private String[] formattedContentOfFirstRowOfSynteticsWorksheet(SpreadsheetMLPackage excelDoc) throws Docx4JException, Xlsx4jException {
        return extractRowContent(excelDoc, "Synthetic - Demand", 1);
    }

    private WorksheetPart getWorksheetByName(SpreadsheetMLPackage excelDoc, String name) throws Docx4JException, Xlsx4jException {
        for(int i = 0; i < excelDoc.getWorkbookPart().getContents().getSheets().getSheet().size(); ++i) {
            Sheet sheet = excelDoc.getWorkbookPart().getContents().getSheets().getSheet().get(i);
            if(name.equals(sheet.getName()))
                return excelDoc.getWorkbookPart().getWorksheet(i);
        }
        throw new RuntimeException("Sheet with name '" + name + "' not found");
    }

    private String[] extractRowContent(SpreadsheetMLPackage excelDoc, String sheetName, int rowIndex) throws Docx4JException, Xlsx4jException {
        WorksheetPart analytics = getWorksheetByName(excelDoc, sheetName);
        Row row = analytics.getContents().getSheetData().getRow().get(rowIndex);
        return extractRowContent(row);
    }

    private String[] extractRowContent(Row row) {
        DataFormatter formatter = new DataFormatter();
        String[] actualRowContent = new String[row.getC().size()];
        for(int i = 0; i < actualRowContent.length; ++i) {
            Cell c = row.getC().get(i);
            if(c.getF() != null) {
                actualRowContent[i] = FORMULA;
            } else {
                actualRowContent[i] = formatter.formatCellValue(c);
            }
        }
        return actualRowContent;
    }
}
