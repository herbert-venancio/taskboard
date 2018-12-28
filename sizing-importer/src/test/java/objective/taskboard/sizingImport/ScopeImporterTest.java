package objective.taskboard.sizingImport;

import static objective.taskboard.sizingImport.ScopeImporterTestDSL.column;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.customField;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.demand;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.feature;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.featureType;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.line;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.phase;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.tshirtSizeField;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.withProject;
import static objective.taskboard.sizingImport.ScopeImporterTestDSL.withRequiredExtraField;

import org.junit.Test;

public class ScopeImporterTest {

    private ScopeImporterTestDSL dsl = new ScopeImporterTestDSL();

    @Test
    public void importNoLines_shouldFinishWithSuccessWithoutLinesToImport() {
        dsl.jira(
            withProject()
                .key("PX")
        );

        dsl.sizing()
            .importedToProject("PX");

        then().importEvents()
                .assertThatLinesToImportIs(0)
                .importFinished();
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importALineAlreadyImported_shouldFinishWithSuccessWithoutLinesToImport() {
        dsl.jira(
            withProject()
                .key("PX")
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .key("PX-10")
        )
            .importedToProject("PX");

        then().importEvents()
                .assertThatLinesToImportIs(0)
                .importFinished();
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importLineMissingFeatureValue_shouldRejectLineAndReportError() {
        dsl.jira(
            withProject()
                .key("PX")
                .withIssues(
                    demand()
                        .key("PX-1"),
                    feature()
                        .summary("Feature")
                )
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("")
        )
                .importedToProject("PX");

        then().importEvents()
                .assertThatLinesToImportIs(1)
                .assertThatContainsError("Feature should be informed")
                .importFinished();
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithInvalidType_shouldRejectLineAndReportError() {
        dsl.jira(
            withProject()
                .key("PX")
                .withIssues(
                    feature()
                        .key("PX-1")
                        .summary("Banana")
                )
                .withIssueTypes(
                    featureType()
                        .name("Feature"),
                    featureType()
                        .name("Task"),
                    featureType()
                        .name("Timebox")
                )
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .name("Banana")
                    .type("Bug")
        )
                .importedToProject("PX");

        then().importEvents()
                .assertThatLinesToImportIs(1)
                .assertThatContainsError("Type should be one of the following: Feature, Task, Timebox")
                .importFinished();
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithMissingRequiredSizingFieldValue_ShouldRejectLineAndReportError() {
        dsl.jira(
            withProject()
                .key("PX")
                .withIssues(
                    feature()
                        .key("PX-1")
                )
                .withIssueTypes(
                    featureType().name("Feature")
                    .withCustomFields(
                            tshirtSizeField()
                                .name("Dev TSize")
                                .required(true),
                            tshirtSizeField()
                                .name("UAT TSize")
                    )
                )
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
        )
            .importedToProject("PX");

        then().importEvents()
                .assertThatLineWithErrorAt(1)
                .assertThatContainsError("Dev TSize should be informed");
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithMissingRequiredExtraFieldValue_ShouldRejectLineAndReportError() {
        dsl.jira(
            withProject()
                .key("PX")
                .withIssues(
                    feature()
                        .key("PX-1")
                        .summary("Blue")
                )
                .withIssueTypes(
                    featureType()
                        .name("Feature")
                        .withCustomFields(
                            customField()
                            .name("Use Cases")
                            .required(true)
                        )
                )
        );

        givenImportConfig(
            withRequiredExtraField("Use Cases")
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
        )
                .importedToProject("PX");

        then().importEvents()
                .assertThatLineWithErrorAt(1)
                .assertThatContainsError("Use Cases should be informed");
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importLineWithoutSizingAndExtraFields_shouldImportWithSuccessAndReportIt() {
        dsl.jira(
            withProject()
                .key("PX")
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
        )
                .importedToProject("PX");

        then().importEvents()
                .importFinished()
                .withSuccessfulIssueImported("PX-2");
    }

    @Test
    public void importTimeboxLine_shouldImportWithSuccessAndReportHim() {
        dsl.jira(
            withProject()
                .key("PX")
                .withIssueTypes(
                    featureType()
                        .name("Timebox")
                )
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Infrastructure")
                    .name("Proof of Concept")
                    .timebox("80")
        )
                .importedToProject("PX");

        then().importEvents()
                .importFinished()
                .withSuccessfulIssueImported("PX-2");
    }

    @Test
    public void importFourLinesWithOneLineAlreadyImported_shouldImportThreeIssuesAndReportTheSuccess() {
        dsl.jira(
            withProject()
                .name("Project X")
                .key("PX")
                .withIssues(
                    demand().key("PX-2").summary("White"),
                    feature().key("PX-1")
                )
                .withIssueTypes(
                    featureType()
                        .name("Feature")
                        .withCustomFields(
                            tshirtSizeField()
                                .name("Dev TSize"),
                            tshirtSizeField()
                                .name("UAT TSize"),
                            customField()
                                .name("Use Cases")
                        ),
                    featureType()
                        .name("Task")
                        .withCustomFields(
                            tshirtSizeField()
                                .name("Task TSize")
                        )
                    )
        );

        givenImportConfig(
                withRequiredExtraField("Use Cases")
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana"),
                line()
                    .phase("One")
                    .demand("Blue")
                    .task("Lemon")
                    .with(
                        column()
                            .forTShirtField("Task TSize")
                            .value("M")
                    ),
                line()
                    .phase("One")
                    .demand("Red")
                    .feature("Grape")
                    .with(
                        column()
                            .forTShirtField("Dev TSize")
                            .value("XL"),
                        column()
                            .forTShirtField("UAT TSize")
                            .value("S"),
                        column()
                            .forCustomField("Use Cases")
                            .value("User picks and eats")
                    ),
                line()
                    .phase("One")
                    .demand("White")
                    .feature("Jackfruit")
                    .key("PX-1")
        )
                .importedToProject("PX");

        then().importEvents()
                .withSuccessfulIssuesImported("PX-4", "PX-5", "PX-7")
                .importFinished();
    }

    @Test
    public void importLineWithUnsupportedFields_shouldRejectLineReportingErrorToTwoExtraValuesOfLine() {
        dsl.jira(
            withProject()
                .key("PX")
                .name("Project X")
                .withIssueTypes(
                    featureType()
                        .name("Feature")
                        .withCustomFields(
                            customField()
                            .name("Dev TSize")
                        )
                )
                .withCustomFields(
                    customField().name("Use Cases"),
                    customField().name("UAT TSize")
                )
        );

        givenImportConfig(
                withRequiredExtraField("Dev TSize"),
                withRequiredExtraField("Use Cases")
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .with(
                        column()
                            .forTShirtField("Dev TSize")
                            .value("XS"),
                        column()
                            .forTShirtField("UAT TSize")
                            .value("S"),
                        column()
                            .forCustomField("Use Cases")
                            .value("User picks and eats")
                    )
        )
                .importedToProject("PX");

        then().importEvents()
                .assertThatLineWithErrorAt(1)
                .assertThatContainsError("Column “UAT TSize” is not valid for the type Feature and should be left blank")
                .assertThatContainsError("Column “Use Cases” is not valid for the type Feature and should be left blank");
        then().jira()
                .assertThatNoIssuesHaveBeenCreated();
    }

    @Test
    public void importALineWithExistingVersion_ensureToNotCreateAVersion() {
        dsl.jira(
            withProject()
                .key("PX")
                .name("Project X")
                .withVersion("One")
                .withIssues(
                    demand()
                        .key("PX-1")
                        .summary("Blue"),
                    feature()
                        .key("PX-10")
                        .summary("Banana")
                )
        );

        dsl.sizing(
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Banana")
                    .key("PX-10"),
                line()
                    .phase("One")
                    .demand("Blue")
                    .feature("Lemon")
                    .key("")
        )
                .importedToProject("PX");

        then().importEvents()
                .importFinished()
                .withSuccessfulIssueImported("PX-11");
        then().jira()
                .assertThatNoVersionHaveBeenCreated();
    }

    @Test
    public void whenImportLineWithExistingDemand_shouldBeLinkedToDemand() {
        givenJira(
                withProject()
                        .key("PX")
                        .withVersion("One")
                        .withIssues(
                                demand().key("PX-1").summary("Demand Summary")
                        )
        );

        whenImportSizing(
                phase("One").demand("Demand Summary").feature("Feature Summary")
        ).intoProject("PX");

        then().importEvents()
                .withSuccessfulIssueImported("PX-2")
                .importFinished();
        then().jira()
                .assertThatIssue("PX-2")
                .isPresent()
                .hasLink("is demanded by", "PX-1");
        then().jira()
                .assertThatIssue("PX-3")
                .isNotPresent();
    }

    private void givenJira(ScopeImporterTestDSL.JiraProjectBuilder... builders) {
        dsl.jira(builders);
    }

    private void givenImportConfig(ScopeImporterTestDSL.ImportConfigBuilder... builders) {
        dsl.importConfig(builders);
    }

    private ScopeImporterTestDSL.SizingInvocationBuilder whenImportSizing(ScopeImporterTestDSL.SizingLineBuilder... builders) {
        return dsl.sizing(builders);
    }

    private ScopeImporterTestDSL.AssertWrapper then() {
        return dsl.then();
    }
}
