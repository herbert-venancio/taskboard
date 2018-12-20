package objective.taskboard.sizingImport;

import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.customField;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.demand;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.demandType;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.featureType;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.phase;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL2.withProject;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.assertj.core.api.OptionalAssert;
import org.junit.Test;

import objective.taskboard.domain.converter.IssueFieldsExtractor;
import objective.taskboard.jira.client.JiraIssueDto;

public class ScopeImporterTest {

    private ScopeImporterTestDSL dsl = new ScopeImporterTestDSL();
    private ScopeImporterTestDSL2 dsl2 = new ScopeImporterTestDSL2();

    @Test
    public void importNoLines_shouldFinishWithSuccessWithoutLinesToImport() {
        dsl.jira()
            .withProject()
                .key("PX")
            .eoP();

        dsl.sizing()
            .importedToProject("PX")
            .then()
                .withLinesToImport(0)
                .importIsFinished()
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importALineAlreadyImported_shouldFinishWithSuccessWithoutLinesToImport() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .isDemand()
                    .eoI()
                .eoIs()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .key("PX-10")
                .eoL()
            .eoLs()
            .importedToProject("PX")
            .then()
                .withLinesToImport(0)
                .importIsFinished()
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importLineMissingFeatureValue_shouldRejectLineAndReportError() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .withLinesToImport(1)
                .withError("Feature should be informed")
                .importIsFinished()
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithInvalidType_shouldRejectLineAndReportError() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Banana")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                    .eoF()
                    .feature()
                        .name("Task")
                    .eoF()
                    .feature()
                        .name("Timebox")
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .name("Banana")
                    .type("Bug")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .withLinesToImport(1)
                .withError("Type should be one of the following: Feature, Task, Timebox")
                .importIsFinished()
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithMissingRequiredSizingFieldValue_ShouldRejectLineAndReportError() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                        .withCustomField()
                            .name("Dev TSize")
                            .withId("f1")
                            .isRequired()
                        .eoCf()
                        .withCustomField()
                            .name("UAT TSize")
                            .withId("f2")
                        .eoCf()
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                .eoL()
            .eoLs()
            .importedToProject("PX")
            .then()
                .rejectLine(1)
                .withError("Dev TSize should be informed")
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithMissingRequiredExtraFieldValue_ShouldRejectLineAndReportError() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Blue")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                        .withCustomField()
                            .name("Use Cases")
                            .withId("f9")
                            .isRequired()
                        .eoCf()
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .rejectLine(1)
                .withError("Use Cases should be informed")
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithoutSizingAndExtraFields_shouldImportWithSuccessAndReportIt() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Blue")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-15")
                        .name("Banana")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .importIsFinished()
                .withSuccessfulIssueImported("PX-15");
    }

    @Test
    public void importTimeboxLine_shouldImportWithSuccessAndReportHim() {
        dsl.jira()
            .withProject()
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Blue")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-15")
                        .name("Banana")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Timebox")
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .config()
                .withTimeboxColumnLetter("S")
            .eoC()
            .lines()
                .line()
                    .phase("One")
                    .demand("Infrastructure")
                    .name("Proof of Concept")
                    .timebox("80")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .importIsFinished()
                .withSuccessfulIssueImported("PX-15");
    }

    @Test
    public void importFourLinesWithOneLineAlreadyImported_shouldImportThreeIssuesAndReportTheSuccess() {
        dsl.jira()
            .withProject()
                .name("Project X")
                .key("PX")
                .withIssues()
                    .issue()
                        .key("PX-2")
                        .name("Blue")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-3")
                        .name("Red")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-15")
                        .name("Banana")
                    .eoI()
                    .issue()
                        .key("PX-16")
                        .name("Lemon")
                    .eoI()
                    .issue()
                        .key("PX-17")
                        .name("Grape")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                        .withCustomField()
                            .name("Dev TSize")
                            .withId("f1")
                        .eoCf()
                        .withCustomField()
                            .name("UAT TSize")
                            .withId("f2")
                        .eoCf()
                        .withCustomField()
                            .name("Use Cases")
                            .withId("f3")
                        .eoCf()
                    .eoF()
                    .feature()
                        .name("Task")
                        .withCustomField()
                            .name("Task TSize")
                            .withId("f5")
                        .eoCf()
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .config()
                .extraField()
                    .columnHeader("Dev TSize")
                    .id("f1")
                    .mappedToColumn("E")
                .eoEf()
                .extraField()
                    .columnHeader("UAT TSize")
                    .id("f2")
                    .mappedToColumn("F")
                .eoEf()
                .extraField()
                    .columnHeader("Task TSize")
                    .id("f5")
                    .mappedToColumn("G")
                .eoEf()
                .extraField()
                    .columnHeader("Use Cases")
                    .id("f3")
                    .mappedToColumn("H")
                .eoEf()
            .eoC()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                .eoL()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .task("Lemon")
                    .withExtraColumns()
                        .column()
                            .name("Task TSize")
                            .value("M")
                            .isTypeSizing()
                        .eoC()
                    .eoEc()
                .eoL()
                .line()
                    .phase("One")
                    .demand("Red")
                    .feature("Grape")
                    .withExtraColumns()
                        .column()
                            .name("Dev TSize")
                            .value("X")
                            .isTypeSizing()
                        .eoC()
                        .column()
                            .name("UAT TSize")
                            .value("S")
                        .eoC()
                        .column()
                            .name("Use Cases")
                            .value("User picks and eats")
                        .eoC()
                    .eoEc()
                .eoL()
                .line()
                    .phase("One")
                    .demand("White")
                    .feature("Jackfruit")
                    .key("PX-1")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .withSuccessfulIssuesImported("PX-15", "PX-16", "PX-17")
                .importIsFinished();
    }

    @Test
    public void importLineWithUnsupportedFields_shouldRejectLineReportingErrorToTwoExtraValuesOfLine() {
        dsl.jira()
            .withProject()
                .key("PX")
                .name("Project X")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Blue")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-15")
                        .name("Banana")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                        .withCustomField()
                            .name("Dev TSize")
                            .withId("f1")
                        .eoCf()
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .config()
                .extraField()
                    .id("f1")
                    .columnHeader("Dev TSize")
                    .mappedToColumn("E")
                .eoEf()
                .extraField()
                    .id("f3")
                    .columnHeader("Use Cases")
                    .mappedToColumn("G")
                .eoEf()
            .eoC()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .withExtraColumns()
                        .column()
                            .name("Dev TSize")
                            .value("X")
                            .isTypeSizing()
                        .eoC()
                        .column()
                            .name("UAT TSize")
                            .value("S")
                            .isTypeSizing()
                        .eoC()
                        .column()
                            .name("Use Cases")
                            .value("User picks and eats")
                        .eoC()
                    .eoEc()
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .rejectLine(1)
                .withError(
                    "Column “UAT TSize” is not valid for the type Feature and should be left blank; " +
                    "Column “Use Cases” is not valid for the type Feature and should be left blank"
                )
            .and()
                .noIssuesHaveBeenCreated();
    }

    @Test
    public void importALineWithExistingVersion_ensureToNotCreateAVersion() {
        dsl.jira()
            .withProject()
                .key("PX")
                .name("Project X")
                .withVersion("One")
                .withIssues()
                    .issue()
                        .key("PX-1")
                        .name("Blue")
                        .isDemand()
                    .eoI()
                    .issue()
                        .key("PX-15")
                        .name("Lemon")
                    .eoI()
                .eoIs()
                .withFeatureType()
                    .feature()
                        .name("Feature")
                    .eoF()
                .eoFt()
            .eoP();

        dsl.sizing()
            .lines()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .key("PX-10")
                .eoL()
                .line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Lemon")
                    .key("")
                .eoL()
            .eoLs()
                .importedToProject("PX")
            .then()
                .importIsFinished()
                .withSuccessfulIssueImported("PX-15")
            .and()
                .noVersionHaveBeenCreated();
    }

    @Test
    public void whenImportLineWithExistingDemand_shouldBeLinkedToDemand() {
        givenJira(
                withProject()
                        .key("PX")
                        .withVersion("One")
                        .withIssueTypes(
                                demandType()
                                        .name("Demand")
                                        .withCustomFields(
                                                customField().name("Release")
                                        ),
                                featureType()
                                        .name("Feature")
                                        .withCustomFields(
                                                customField().name("Release")
                                        )
                        )
                        .withIssues(
                                demand().key("PX-1")
                        )
        );

        whenImportSizing(
                phase("One").demand("Demand Summary").feature("Feature Summary")
        ).intoProject("PX");

        assertThatIssue("PX-2")
                .isPresent()
                .hasValueSatisfying(issue -> {
                    String parentKey = extractParentKey(issue);
                    assertThat(parentKey)
                            .isEqualTo("PX-1");
                });
    }

    private void givenJira(ScopeImporterTestDSL2.JiraProjectBuilder... builders) {
        dsl2.jira(builders);
    }

    private ScopeImporterTestDSL2.SizingInvocationBuilder whenImportSizing(ScopeImporterTestDSL2.SizingLineBuilder... builders) {
        return dsl2.sizing(builders);
    }

    private OptionalAssert<JiraIssueDto> assertThatIssue(String key) {
        return dsl2.assertThatIssue(key);
    }

    private String extractParentKey(JiraIssueDto issue) {
        return IssueFieldsExtractor.extractParentKey(dsl2.jiraProperties, issue, Collections.singletonList("is demanded by"));
    }
}
