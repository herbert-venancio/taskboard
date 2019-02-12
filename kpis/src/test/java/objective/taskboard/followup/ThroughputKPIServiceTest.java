package objective.taskboard.followup;

import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_DEMAND;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.GenerateAnalyticsDataSets;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class ThroughputKPIServiceTest {

    @Test
    public void checkThroughputRows() {
        dsl()
            .environment()
                .givenDemand("I-1")
                    .type("Demand")
                    .project("PROJ")
                    .withTransitions()
                        .status("To Do").date("2017-09-25")
                        .status("Doing").date("2017-09-26")
                        .status("Done").date("2017-09-27")
                    .eoT()
                    .feature("I-2")
                        .type("OS")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").noDate()
                        .eoT()
                        .endOfFeature()
                    .feature("I-3")
                        .type("Feature")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").noDate()
                        .eoT()
                        .subtask("I-4")
                            .type("Subtask")
                            .withTransitions()
                                .status("To Do").date("2017-09-25")
                                .status("Doing").noDate()
                                .status("Done").noDate()
                            .eoT()
                        .endOfSubtask()
                    .endOfFeature()
                .eoI()
            .when()
                .appliesBehavior(generateThroughputDataSet())
            .then()
                .withDataSet(TYPE_DEMAND)
                    .hasSize(3)
                        .row(0).hasDate("2017-09-25").hasType("Demand").hasTotalThroughput(0l).eoR()
                        .row(1).hasDate("2017-09-26").hasType("Demand").hasTotalThroughput(0l).eoR()
                        .row(2).hasDate("2017-09-27").hasType("Demand").hasTotalThroughput(1l).eoR()
                    .eoDS()
                .withDataSet(TYPE_FEATURES)
                    .hasSize(4)
                        .row(0).hasDate("2017-09-25").hasType("Feature").hasTotalThroughput(0l).eoR()
                        .row(1).hasDate("2017-09-25").hasType("OS").hasTotalThroughput(0l).eoR()
                        .row(2).hasDate("2017-09-26").hasType("Feature").hasTotalThroughput(0l).eoR()
                        .row(3).hasDate("2017-09-26").hasType("OS").hasTotalThroughput(0l).eoR()
                    .eoDS()
                .withDataSet(TYPE_SUBTASKS)
                    .hasSize(1)
                        .row(0).hasDate("2017-09-25").hasType("Subtask").hasTotalThroughput(0l).eoR()
                    .eoDS();
    }

    @Test
    public void checkEmptyDataSets() {
        dsl()
        .when()
            .appliesBehavior(generateThroughputDataSet())
        .then()
            .withDataSet(TYPE_DEMAND).isEmpty().eoDS()
            .withDataSet(TYPE_FEATURES).isEmpty().eoDS()
            .withDataSet(TYPE_SUBTASKS).isEmpty().eoDS();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl
            .environment()
                .withJiraProperties()
                    .finalStatuses()
                        .onDemand("Done")
                        .onFeature("Done")
                        .onSubtasks("Done")
                    .eoFS()
                    .withDemandStatusPriorityOrder("Done","Doing","To Do")
                    .withFeaturesStatusPriorityOrder("Done","Doing","To Do")
                    .withSubtaskStatusPriorityOrder("Done","Doing","To Do")
                .eoJp()
                .statuses()
                    .withNotProgressingStatuses("To Do","Done")
                    .withProgressingStatuses("Doing")
                .eoS()
                .withDemandType("Demand")
                .withFeatureType("OS")
                .withFeatureType("Feature")
                .withSubtaskType("Subtask");
        return dsl;
    }

    private GenerateThroughputDataSets generateThroughputDataSet() {
        return new GenerateThroughputDataSets();
    }

    private class GenerateThroughputDataSets implements DSLSimpleBehaviorWithAsserter<ThroughputAllSetsAsserter> {
        private ThroughputAllSetsAsserter asserter;

        @Override
        public void behave(KpiEnvironment environment) {
            GenerateAnalyticsDataSets datasetFactory = new GenerateAnalyticsDataSets(environment);
            
            environment.services().issueKpi().prepareFromDataSet(datasetFactory);
            
            ThroughputKPIService subject = new ThroughputKPIService(environment.getJiraProperties(), environment.services().issueKpi().getService());
            
            this.asserter = new ThroughputAllSetsAsserter(subject.getData(datasetFactory.buildFollowupData()));
        }

        @Override
        public ThroughputAllSetsAsserter then() {
            return this.asserter;
        }
    }
    private class ThroughputAllSetsAsserter {
        
        private Map<String, ThroughputDataSet> dataSets;
        
        public ThroughputAllSetsAsserter(List<ThroughputDataSet> dataSets) {
            this.dataSets = dataSets.stream().collect(Collectors.toMap(d -> d.issueType, Function.identity()));
        }

        public ThroughputDataSetAsserter withDataSet(String type) {
           ThroughputDataSet throughputDataSet = dataSets.get(type);
           assertThat(throughputDataSet).as("DataSet with issueType %s not found",type).isNotNull();
           
           return new ThroughputDataSetAsserter(throughputDataSet);
        }
        
        private class ThroughputDataSetAsserter {
            private ThroughputDataSet subject;

            private ThroughputDataSetAsserter(ThroughputDataSet subject) {
                this.subject = subject;
            }

            public ThroughputDataSetAsserter isEmpty() {
                return hasSize(0);
            }

            public ThroughputAllSetsAsserter eoDS() {
                return ThroughputAllSetsAsserter.this;
            }

            private ThroughputDataSetAsserter hasSize(int size) {
                assertExistsRows();
                assertThat(subject.rows).hasSize(size);
                return this;
            }

            private void assertExistsRows() {
                assertThat(subject.rows).isNotNull();
            }
            
            private ThroughputDataRowAsserter row(int row) {
                assertExistsRows();
                assertThat(row).isLessThan(subject.rows.size());
                return new ThroughputDataRowAsserter(subject.rows.get(row));
            }
            
            private class ThroughputDataRowAsserter {
                private ThroughputRow row;

                public ThroughputDataRowAsserter(ThroughputRow row) {
                    this.row = row;
                }

                public ThroughputDataSetAsserter eoR() {
                    return ThroughputDataSetAsserter.this;
                }

                public ThroughputDataRowAsserter hasTotalThroughput(long value) {
                    assertThat(row.count).isEqualTo(value);
                    return this;
                }

                public ThroughputDataRowAsserter hasType(String type) {
                    assertThat(row.issueType).isEqualTo(type);
                    return this;
                }

                public ThroughputDataRowAsserter hasDate(String date) {
                    assertThat(row.date.toLocalDate().toString()).isEqualTo(date);
                    return this;
                }
            }
        }
    }

}
