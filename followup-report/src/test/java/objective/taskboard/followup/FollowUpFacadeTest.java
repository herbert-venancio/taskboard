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

import static java.util.Arrays.asList;
import static objective.taskboard.followup.FollowUpReportHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpReportHelper.getDefaultThroughputDataSet;
import static objective.taskboard.followup.FollowUpReportHelper.getDefaultWipDataSet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.org.apache.poi.util.LocaleUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowupClusterProvider;
import objective.taskboard.followup.data.Template;
import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.jira.FieldMetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.FinalStatuses;
import objective.taskboard.jira.properties.StatusConfiguration.StatusCountingOnWip;
import objective.taskboard.rules.CleanupDataFolderRule;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpFacadeTest {

    private static final String FROM_JIRA = "From Jira";
    private static final String ANALYTIC_DEMAND = "Analytic - Demand";
    private static final String SYNTHETIC_DEMAND = "Synthetic - Demand";
    private static final String WIP_DEMAND = "Wip MetaData - Demand";
    private static final String THROUGHPUT_DEMAND = "Throughput MetaData - Demand";

    @Rule
    public CleanupDataFolderRule clean = new CleanupDataFolderRule(Paths.get("data/followup-templates"));

    private static final String FORMULA = "someFormula";

    @Mock
    private TemplateService templateService;

    @Mock
    private DataBaseDirectory dataBaseDirectory;
    
    @Mock
    private FollowupClusterProvider clusterProvider;
    
    @Mock
    private KpiDataService kpiService;
    
    @Mock
    private ReleaseHistoryProvider releaseHistoryProvider;

    @Spy
    @InjectMocks
    private FollowUpTemplateStorage followUpTemplateStorage = new FollowUpTemplateStorage();
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private FollowUpDataRepository historyRepository;

    @Mock
    private FieldMetadataService fieldMetadataService;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private WipKPIService wipKpiService;
    
    @Mock
    private ThroughputKPIService tpKpiService;
    
    @InjectMocks
    private FollowUpFacade followUpFacade = new FollowUpFacade();

    private static final String TEMPLATE_NAME = "OkFollowupTemplate.xlsm";
    private static final String PROJECT = "TASKB";
    private static final List<String> ROLES = asList("Role");
    private Template template;

    @Before
    public void setup() throws IOException {
        when(dataBaseDirectory.path(anyString())).thenReturn(Paths.get("data/followup-templates"));

        MultipartFile file = new MockMultipartFile("file", FollowUpFacadeTest.class.getResourceAsStream(TEMPLATE_NAME));
        followUpFacade.createTemplate(TEMPLATE_NAME, ROLES, file);

        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateService, atLeastOnce()).saveTemplate(any(), any(), argCaptor.capture());
        String path = argCaptor.getValue();
        template = mock(Template.class);
        when(template.getPath()).thenReturn(path);
        
        when(releaseHistoryProvider.get(any())).thenReturn(Collections.emptyList());
        when(projectService.getTaskboardProjectOrCry(PROJECT)).thenReturn(new ProjectFilterConfiguration(PROJECT, 1L));
        when(historyRepository.getFirstDate(PROJECT)).thenReturn(Optional.empty());
        
        StatusCountingOnWip statusCounting = new StatusCountingOnWip();
        statusCounting.setDemands(new String[] { "Doing"});
        statusCounting.setTasks(new String[] { "QAing", "To QA", "Feature Reviewing", "To Feature Review",
                "Alpha Testing", "To Alpha Test", "Doing" });
        statusCounting.setSubtasks(new String[] { "Reviewing", "To Review", "Doing"});
        
        when(jiraProperties.getStatusCountingOnWip()).thenReturn(statusCounting);
        
        FinalStatuses finalStatuses = new FinalStatuses();
        String[] terminalStatuses = new String[] {"Done","Cancelled"};
        finalStatuses.setDemands(terminalStatuses);
        finalStatuses.setTasks(terminalStatuses);
        finalStatuses.setSubtasks(terminalStatuses);
        
        when(jiraProperties.getFinalStatuses()).thenReturn(finalStatuses);
        
        when(wipKpiService.getData(any())).thenReturn(getDefaultWipDataSet());
        when(tpKpiService.getData(any())).thenReturn(getDefaultThroughputDataSet());
        
    }

    @Before
    public void setLocale() {
        LocaleUtil.setUserLocale(new Locale("PT", "br"));
    }

    @Test
    public void okTemplateGenerate() throws Exception {
        LocalDate date = LocalDate.parse("2017-10-01");
        FollowUpSnapshot snapshot = new FollowUpSnapshotMockBuilder().timeline(date).data(getDefaultFollowupData()).build();

        given(templateService.getTemplate(TEMPLATE_NAME)).willReturn(template);
        given(kpiService.getSnapshot(any(), any(), Mockito.eq(PROJECT))).willReturn(snapshot);
        
        // when
        Resource resource = followUpFacade.generateReport(TEMPLATE_NAME, Optional.of(date), ZoneId.systemDefault(), PROJECT);

        // then
        SpreadsheetMLPackage excelDoc = readFile(resource);
        assertThat(excelDoc).sheet(FROM_JIRA).row(1).has(expectedRowContentOfFromJira());
        assertThat(excelDoc).sheet(ANALYTIC_DEMAND).row(1).has(expectedRowContentOfAnalytics());
        assertThat(excelDoc).sheet(SYNTHETIC_DEMAND).row(1).has(expectedRowContentOfSyntetics());
        assertThat(excelDoc).sheet(WIP_DEMAND).row(1).has(expectedRowContentOfWip("9/25/17 0:00","Demand", 0));
        assertThat(excelDoc).sheet(WIP_DEMAND).row(2).has(expectedRowContentOfWip("9/25/17 0:00","OS", 0));
        assertThat(excelDoc).sheet(WIP_DEMAND).row(3).has(expectedRowContentOfWip("9/26/17 0:00","Demand", 1));
        assertThat(excelDoc).sheet(WIP_DEMAND).row(4).has(expectedRowContentOfWip("9/26/17 0:00","OS", 1));
        assertThat(excelDoc).sheet(WIP_DEMAND).row(5).has(expectedRowContentOfWip("9/27/17 0:00","Demand", 0));
        assertThat(excelDoc).sheet(WIP_DEMAND).row(6).has(expectedRowContentOfWip("9/27/17 0:00","OS", 1));
        
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(1).has(expectedRowContentOfThroughput("9/25/17 0:00","Demand", 0));
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(2).has(expectedRowContentOfThroughput("9/25/17 0:00","OS", 0));
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(3).has(expectedRowContentOfThroughput("9/26/17 0:00","Demand", 0));
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(4).has(expectedRowContentOfThroughput("9/26/17 0:00","OS", 0));
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(5).has(expectedRowContentOfThroughput("9/27/17 0:00","Demand", 1));
        assertThat(excelDoc).sheet(THROUGHPUT_DEMAND).row(6).has(expectedRowContentOfThroughput("9/27/17 0:00","OS", 0));
    }

    private static SpreadsheetMLPackage readFile(Resource resource) throws IOException, Docx4JException {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        return SpreadsheetMLPackage.load(resource.getInputStream());
    }

    private static String[] expectedRowContentOfFromJira() {
        return new String[]{
                "PROJECT TEST", "Demand", "Doing", "I-1", "Summary Demand", "Description Demand",
                "0", "assignee.demand.test", "5/25/25 0:00", "1/1/12 0:00", "", "", "reporter.demand.test", "", "Standard", "2/1/12 0:00", "1,111111", "false", "Demand last block reason",
                "Feature", "Doing", "I-2", "Summary Feature", "Description Feature", "Full Description Feature", "80",
                "0", "assignee.task.test", "5/24/25 0:00", "1/2/12 0:00", "", "", "reporter.demand.test", "", "Standard", "2/2/12 0:00", "2,222222", "false", "Task last block reason",
                "Sub-task", "Doing", "I-3", "Summary Sub-task", "Description Sub-task", "Full Description Sub-task",
                "0", "assignee.subtask.test", "5/23/25 0:00", "1/3/12 0:00", "", "", "reporter.subtask.test", "", "Standard", "2/3/12 0:00", "3,333333", "false", "Subtask last block reason",
                "1", "2", "3", "Ballpark", "Release", "1", "1", "1", "1", "M", "Type",
                "5", "0", "3", "0", "0", "0", "2", "0", "0", "0", "0", "0", "0", "0", "0", "0",
                "9/27/17 0:00", "9/26/17 0:00", "9/25/17 0:00", "", "9/26/17 0:00", "9/25/17 0:00", "", "", "9/25/17 0:00"};
    }

    private static String[] expectedRowContentOfAnalytics() {
        return new String[]{
                "I-1", "Demand", "9/27/17 0:00", "9/26/17 0:00", "9/25/17 0:00"};
    }

    private static String[] expectedRowContentOfSyntetics() {
        return new String[]{"9/25/17 0:00", "Demand", "0", "0", "1"};
    }
    
    private static String[] expectedRowContentOfWip(String date, String issueType, int count) {
        return new String[]{date,issueType, "Doing", String.valueOf(count)};
    }
    
    private static String[] expectedRowContentOfThroughput(String date, String issueType, int count) {
        return new String[]{date,issueType, String.valueOf(count)};
    }

    public SpreadsheetAssert assertThat(SpreadsheetMLPackage actual) {
        return new SpreadsheetAssert(actual);
    }

    public static class SpreadsheetAssert extends AbstractAssert<SpreadsheetAssert, SpreadsheetMLPackage> {

        public SpreadsheetAssert(SpreadsheetMLPackage actual) {
            super(actual, SpreadsheetAssert.class);
        }

        public SheetAssert sheet(String name) {
            WorksheetPart sheet = getWorksheetByName(actual, name);
            return new SheetAssert(sheet);
        }

        private WorksheetPart getWorksheetByName(SpreadsheetMLPackage excelDoc, String name) {
            try {
                for (int i = 0; i < excelDoc.getWorkbookPart().getContents().getSheets().getSheet().size(); ++i) {
                    Sheet sheet = excelDoc.getWorkbookPart().getContents().getSheets().getSheet().get(i);
                    if (name.equals(sheet.getName()))
                        return excelDoc.getWorkbookPart().getWorksheet(i);
                }
                failWithMessage("Sheet with name <%s> not found", name);
            } catch (Docx4JException | Xlsx4jException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    public static class SheetAssert extends AbstractAssert<SheetAssert, WorksheetPart> {
        private SheetAssert(WorksheetPart actual) {
            super(actual, SheetAssert.class);
        }

        public RowAssert row(int index) throws Docx4JException {
            Row row = actual.getContents().getSheetData().getRow().get(index);
            return new RowAssert(row);
        }
    }

    public static class RowAssert extends AbstractAssert<RowAssert, Row> {
        private RowAssert(Row actual) {
            super(actual, RowAssert.class);
        }

        public RowAssert has(String[] expectedRowContent) {
            String[] actualRowContent = extractRowContent();
            Assertions.assertThat(actualRowContent).isEqualTo(expectedRowContent);
            return this;
        }

        private String[] extractRowContent() {
            return extractRowContent(actual);
        }

        private static String[] extractRowContent(Row row) {
            DataFormatter formatter = new DataFormatter();
            String[] actualRowContent = new String[row.getC().size()];
            for (int i = 0; i < actualRowContent.length; ++i) {
                Cell c = row.getC().get(i);
                if (c.getF() != null) {
                    actualRowContent[i] = FORMULA;
                } else {
                    actualRowContent[i] = formatter.formatCellValue(c);
                }
            }
            return actualRowContent;
        }
    }
}