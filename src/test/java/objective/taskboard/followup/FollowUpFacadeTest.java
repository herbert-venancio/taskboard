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

import objective.taskboard.followup.data.Template;
import objective.taskboard.followup.impl.DefaultFollowUpFacade;
import objective.taskboard.followup.impl.DefaultFollowUpTemplateStorage;
import objective.taskboard.followup.impl.DefaultUpdateFollowUpService;
import objective.taskboard.rules.CleanupDataFolderRule;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpFacadeTest {

    @Rule
    public CleanupDataFolderRule clean = new CleanupDataFolderRule(Paths.get("data/followup-templates"));

    private static final String FORMULA = "someFormula";

    @Mock
    private TemplateService templateService;

    @Mock
    private FollowupDataProvider provider;

    @Spy
    private DefaultUpdateFollowUpService updateFollowUpService = new DefaultUpdateFollowUpService();

    @Spy
    @InjectMocks
    private DefaultFollowUpTemplateStorage followUpTemplateStorage = new DefaultFollowUpTemplateStorage();

    @InjectMocks
    private DefaultFollowUpFacade followUpFacade = new DefaultFollowUpFacade();

    private static final String TEMPLATE_NAME = "OkFollowupTemplate.xlsm";
    private static final String PROJECTS = "TASKB,PROJ1";
    private static final String[] INCLUDED_PROJECTS = PROJECTS.split(",");
    private Template template;

    @Before
    public void setup() throws IOException {
        MultipartFile file = new MockMultipartFile("file", FollowUpFacadeTest.class.getResourceAsStream("OkFollowupTemplate.xlsm"));
        followUpFacade.createTemplate(TEMPLATE_NAME, PROJECTS, file);

        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateService, atLeastOnce()).saveTemplate(any(), any(), argCaptor.capture());
        String path = argCaptor.getValue();
        template = mock(Template.class);
        when(template.getPath()).thenReturn(path);
    }

    @Test
    public void generate() throws Exception {
        given(templateService.getTemplate(TEMPLATE_NAME)).willReturn(template);
        given(provider.getJiraData(INCLUDED_PROJECTS)).willReturn(FollowUpHelper.getFollowUpDataDefaultList());

        FollowUpGenerator followupGenerator = followUpFacade.getGenerator(TEMPLATE_NAME);
        Resource resource = followupGenerator.generate(INCLUDED_PROJECTS);

        String[] actualRowContent = formattedContentOfFirstRowOfFromJiraWorksheet(resource);

        assertArrayEquals(expectedRowContent(), actualRowContent);
    }

    private String[] expectedRowContent() {
        return new String[]{
                "PROJECT TEST", "Demand", "Doing", "I-1", "Summary Demand"
                , "Description Demand", "Feature", "Doing", "I-2", "Summary Feature"
                , "Description Feature", "Full Description Feature", "Sub-task", "Doing"
                , "I-3", "Summary Sub-task", "Description Sub-task", "Full Description Sub-task"
                , "1", "2", "3", "Ballpark", "Release", "1", "1", "1", "1", "M", "Type"
                , FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA
                , FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA
                , FORMULA, FORMULA, FORMULA, FORMULA, FORMULA, FORMULA};
    }

    private String[] formattedContentOfFirstRowOfFromJiraWorksheet(Resource resource) throws Docx4JException, IOException, Xlsx4jException {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        SpreadsheetMLPackage excelDoc = SpreadsheetMLPackage.load(resource.getInputStream());
        WorksheetPart fromJira = excelDoc.getWorkbookPart().getWorksheet(6);

        Row row = fromJira.getContents().getSheetData().getRow().get(1);
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
