package objective.taskboard.followup.kpi.transformer;

import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;

import org.junit.Test;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class IssueKpiDataItemAdapterFactoryTest {

    @Test
    public void convertIssues() {
        
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Dev")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").noDate()
                    .eoT()
                .eoI()
                .givenSubtask("I-2")
                    .type("Alpha")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").date("2020-01-04")
                    .eoT()
                .eoI()
            .when()
                .appliesBehavior(buildItemAdaptersFromIssues())
            .then()
                .amountOfBuiltItems(2)
                .givenItem("I-1")
                    .isSubtask()
                    .hasType("Dev")
                    .status("Open").hasDate("2020-01-01")
                    .status("To Do").hasDate("2020-01-02")
                    .status("Doing").hasDate("2020-01-03")
                    .status("Done").hasNoDate()
                .eoIA()
                .givenItem("I-2")
                    .isSubtask()
                    .hasType("Alpha")
                    .status("Open").hasDate("2020-01-01")
                    .status("To Do").hasDate("2020-01-02")
                    .status("Doing").hasDate("2020-01-03")
                    .status("Done").hasDate("2020-01-04")
                .eoIA();
    }
    
    @Test
    public void getIssuesFromAnalytic_happyDay() {
        KpiEnvironment context = dslToAnalyticSets()
            .environment()
                .givenDemand("I-1")
                    .type("Demand")
                    .project("PROJ")
                    .withTransitions()
                        .status("To Do").date("2017-09-25")
                        .status("Doing").date("2017-09-26")
                        .status("Done").date("2017-09-27")
                    .eoT()
                    .feature("I-4")
                        .type("OS")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").noDate()
                        .eoT()
                    .endOfFeature()
                    .feature("I-2")
                        .type("Feature")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").noDate()
                        .eoT()
                        .subtask("I-3")
                            .type("Subtask")
                            .withTransitions()
                                .status("To Do").date("2017-09-25")
                                .status("Doing").noDate()
                                .status("Done").noDate()
                            .eoT()
                        .endOfSubtask()
                    .endOfFeature()
                .eoI();
        context.when()
                    .appliesBehavior(buildItemAdaptersFromDatasets(KpiLevel.DEMAND))
                .then()
                    .amountOfBuiltItems(1)
                    .givenItem("I-1")
                        .isDemand()
                        .hasType("Demand")
                        .status("To Do").hasDate("2017-09-25")
                        .status("Doing").hasDate("2017-09-26")
                        .status("Done").hasDate("2017-09-27")
                    .eoIA();
        context.when()
                    .appliesBehavior(buildItemAdaptersFromDatasets(KpiLevel.FEATURES))
                .then()
                    .amountOfBuiltItems(2)
                    .givenItem("I-4")
                        .isFeature()
                        .hasType("OS")
                        .status("To Do").hasDate("2017-09-25")
                        .status("Doing").hasDate("2017-09-26")
                        .status("Done").hasNoDate()
                    .eoIA()
                    .givenItem("I-2")
                        .isFeature()
                        .hasType("Feature")
                        .status("To Do").hasDate("2017-09-25")
                        .status("Doing").hasDate("2017-09-26")
                        .status("Done").hasNoDate()
                    .eoIA();
        
        context.when()
                    .appliesBehavior(buildItemAdaptersFromDatasets(KpiLevel.SUBTASKS))
                .then()
                    .amountOfBuiltItems(1)
                    .givenItem("I-3")
                        .isSubtask()
                        .hasType("Subtask")
                        .status("To Do").hasDate("2017-09-25")
                        .status("Doing").hasNoDate()
                        .status("Done").hasNoDate()
                    .eoIA();
    }

    @Test
    public void getIssuesFromAnalytic_emptySet() {
        dslToAnalyticSets()
            .withNoIssuesConfigured()
            .when()
                .appliesBehavior(buildItemAdaptersFromDatasets(SUBTASKS))
            .then()
                .amountOfBuiltItems(0);
    }
    
    @Test
    public void getIssuesFromAnalytic_inexitentLevelTye() {
        dslToAnalyticSets()
            .environment()
                .types()
                    .addUnmapped("Inexistent")
                .eoT()
                .givenSubtask("I-2")
                    .unmappedLevel()
                    .project("PROJ")
                    .type("Inexistent")
                    .withTransitions()
                        .status("To Do").date("2017-09-25")
                        .status("Doing").date("2017-09-26")
                        .status("Done").noDate()
                    .eoT()
                .eoI()
            .when()
                .appliesBehavior(buildItemAdaptersFromDatasets(KpiLevel.UNMAPPED))
            .then()
                .amountOfBuiltItems(1)
                .givenItem("I-2")
                    .hasType("Inexistent")
                    .isUnmapped()
                .eoIA();
    }
    
    @Test
    public void getIssuesFromService_inexistentLevelType() {
        dsl()
            .environment()
                .types()
                    .addUnmapped("Continuous")
                    .addUnmapped("Subtask Continuous")
                .eoT()
                .givenFeature("I-1")
                    .unmappedLevel()
                    .type("Continuous")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").noDate()
                    .eoT()
                    .subtask("I-2")
                        .unmappedLevel()
                        .type("Subtask Continuous")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").date("2020-01-03")
                            .status("Done").date("2020-01-04")
                        .eoT()
                    .endOfSubtask()
                .eoI()
                .givenFeature("I-3")
                    .type("Feature")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").date("2020-01-04")
                    .eoT()
                .eoI()
            .eoE()
            .when()
                .appliesBehavior(buildItemAdaptersFromIssues())
            .then()
                .amountOfBuiltItems(1)
                .givenItem("I-3")
                    .isFeature()
                    .hasType("Feature")
                    .status("Open").hasDate("2020-01-01")
                    .status("To Do").hasDate("2020-01-02")
                    .status("Doing").hasDate("2020-01-03")
                    .status("Done").hasDate("2020-01-04")
                .eoIA();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("Open").isNotProgressing()
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("Done").isNotProgressing()
            .withDemandType("Demand")
            .withFeatureType("OS")
            .withFeatureType("Feature")
            .withSubtaskType("Dev")
            .withSubtaskType("Subtask")
            .withSubtaskType("Alpha")
            .withJiraProperties()
                .withDemandStatusPriorityOrder("Done","Doing","To Do","Open")
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Open")
                .withSubtaskStatusPriorityOrder("Done","Doing","To Do","Open")
            .eoJp();
        return dsl;
    }
    
    private DSLKpi dslToAnalyticSets() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("Done").isNotProgressing()
            .withDemandType("Demand")
            .withFeatureType("OS")
            .withFeatureType("Feature")
            .withSubtaskType("Subtask")
            .withJiraProperties()
                .withDemandStatusPriorityOrder("Done","Doing","To Do")
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do")
                .withSubtaskStatusPriorityOrder("Done","Doing","To Do")
            .eoJp();
        return dsl;
    }

    private IssueKpiDataItemAdapterFactoryBehavior buildItemAdaptersFromIssues() {
        return new DataItemFactoryFromIssuesBehavior();
    }
    
    private IssueKpiDataItemAdapterFactoryBehavior buildItemAdaptersFromDatasets(KpiLevel level) {
        return new DataItemFactoryFromDataSetBehavior(level);
    }
   
}
