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

import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehavior;
import objective.taskboard.followup.kpi.enviroment.GenerateAnalyticsDataSets;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class WipKPIServiceTest {

    @Test
    public void checkWipRows() {
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
               .appliesBehavior(generateDataSet())
           .then()
               .withDataSet(TYPE_DEMAND)
                   .hasSize(3)
                       .row(0).hasDate("2017-09-25").hasType("Demand").hasStatus("Doing").hasTotalWip(0L).eoR()
                       .row(1).hasDate("2017-09-26").hasType("Demand").hasStatus("Doing").hasTotalWip(1L).eoR()
                       .row(2).hasDate("2017-09-27").hasType("Demand").hasStatus("Doing").hasTotalWip(0L).eoR()
                   .eoDS()
               .withDataSet(TYPE_FEATURES)
                   .hasSize(4)
                       .row(0).hasDate("2017-09-25").hasType("Feature").hasStatus("Doing").hasTotalWip(0L).eoR()
                       .row(1).hasDate("2017-09-25").hasType("OS").hasStatus("Doing").hasTotalWip(0L).eoR()
                       .row(2).hasDate("2017-09-26").hasType("Feature").hasStatus("Doing").hasTotalWip(1L).eoR()
                       .row(3).hasDate("2017-09-26").hasType("OS").hasStatus("Doing").hasTotalWip(1L).eoR()
                   .eoDS()
               .withDataSet(TYPE_SUBTASKS)
                   .hasSize(1)
                       .row(0).hasDate("2017-09-25").hasType("Subtask").hasStatus("Doing").hasTotalWip(0L).eoR()
               .eoDS();
    }
    
    @Test
    public void checkEmptyDataSets() {
        dsl()
        .when()
           .appliesBehavior(generateDataSet())
       .then()
           .withDataSet(TYPE_DEMAND).isEmpty().eoDS()
           .withDataSet(TYPE_FEATURES).isEmpty().eoDS()
           .withDataSet(TYPE_SUBTASKS).isEmpty().eoDS();
    }

    private GenerateWipDataSets generateDataSet() {
        return new GenerateWipDataSets();
    }
    
    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl
            .environment()
                .withJiraProperties()
                    .statusCountingOnWip()
                        .onDemand("Doing")
                        .onFeature("Doing")
                        .onSubtasks("Doing")
                    .eoSCW()
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

    private class GenerateWipDataSets implements DSLSimpleBehavior<WipAllSetsAsserter> {
        private WipAllSetsAsserter asserter;

        @Override
        public void behave(KpiEnvironment environment) {
            GenerateAnalyticsDataSets datasetFactory = new GenerateAnalyticsDataSets(environment);
            
            environment.services().issueKpi().prepareFromDataSet(datasetFactory);
            
            WipKPIService subject = new WipKPIService(environment.getJiraProperties(), environment.services().issueKpi().getService());
            
            this.asserter = new WipAllSetsAsserter(subject.getData(datasetFactory.buildFollowupData()));
        }

        @Override
        public WipAllSetsAsserter then() {
            return this.asserter;
        }
    }

    private class WipAllSetsAsserter {
        
        private Map<String, WipDataSet> dataSets;
        
        public WipAllSetsAsserter(List<WipDataSet> dataSets) {
            this.dataSets = dataSets.stream().collect(Collectors.toMap(d -> d.issueType, Function.identity()));
        }

        public WipDataSetAsserter withDataSet(String type) {
            WipDataSet throughputDataSet = dataSets.get(type);
           assertThat(throughputDataSet).as("DataSet with issueType %s not found",type).isNotNull();
           
           return new WipDataSetAsserter(throughputDataSet);
        }
        
        private class WipDataSetAsserter {
            private WipDataSet subject;

            private WipDataSetAsserter(WipDataSet subject) {
                this.subject = subject;
            }

            public WipDataSetAsserter isEmpty() {
                return hasSize(0);
            }

            public WipAllSetsAsserter eoDS() {
                return WipAllSetsAsserter.this;
            }

            private WipDataSetAsserter hasSize(int size) {
                assertExistsRows();
                assertThat(subject.rows).hasSize(size);
                return this;
            }

            private void assertExistsRows() {
                assertThat(subject.rows).isNotNull();
            }
            
            private WipDataRowAsserter row(int row) {
                assertExistsRows();
                assertThat(row).isLessThan(subject.rows.size());
                return new WipDataRowAsserter(subject.rows.get(row));
            }
            
            private class WipDataRowAsserter {
                private WipRow row;

                public WipDataRowAsserter(WipRow row) {
                    this.row = row;
                }

                public WipDataSetAsserter eoR() {
                    return WipDataSetAsserter.this;
                }

                public WipDataRowAsserter hasTotalWip(long value) {
                    assertThat(row.count).isEqualTo(value);
                    return this;
                }

                public WipDataRowAsserter hasType(String type) {
                    assertThat(row.type).isEqualTo(type);
                    return this;
                }

                public WipDataRowAsserter hasDate(String date) {
                    assertThat(row.date.toLocalDate().toString()).isEqualTo(date);
                    return this;
                }

                public WipDataRowAsserter hasStatus(String status) {
                    assertThat(row.status).isEqualTo(status);
                    return this;
                }
            }
        }
    }

}
