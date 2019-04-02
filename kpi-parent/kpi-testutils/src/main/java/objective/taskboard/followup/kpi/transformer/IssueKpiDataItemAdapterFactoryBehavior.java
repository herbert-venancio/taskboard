package objective.taskboard.followup.kpi.transformer;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.enviroment.snapshot.AnalyticsDataSetsGenerator;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;

public abstract class IssueKpiDataItemAdapterFactoryBehavior implements DSLSimpleBehaviorWithAsserter<ItemsAsserter> {
        
        private ItemsAsserter asserter;
        
        @Override
        public void behave(KpiEnvironment environment) {
            JiraProperties jiraProperties = environment.getJiraProperties();
            MetadataService metadataService = environment.services().metadata().getService();
            IssueTransitionService transitionService = environment.services().issuesTransition().getService();
            
            IssueKpiDataItemAdapterFactory subject = new IssueKpiDataItemAdapterFactory(metadataService,jiraProperties,transitionService);
            
            asserter = new ItemsAsserter(getItems(environment, subject));
        }

        protected abstract List<IssueKpiDataItemAdapter> getItems(KpiEnvironment environment, IssueKpiDataItemAdapterFactory subject);
        
        @Override
        public ItemsAsserter then() {
            return asserter;
        }
        
    }
    
    class DataItemFactoryFromIssuesBehavior extends IssueKpiDataItemAdapterFactoryBehavior {

        @Override
        protected List<IssueKpiDataItemAdapter> getItems(KpiEnvironment environment,IssueKpiDataItemAdapterFactory subject) {
            List<Issue> issues = environment.services().issuesBuffer().getIssues();
            return subject.getItems(issues, environment.getTimezone());
        }
    }
    
    class DataItemFactoryFromDataSetBehavior extends IssueKpiDataItemAdapterFactoryBehavior {
        
        private KpiLevel level;

        DataItemFactoryFromDataSetBehavior(KpiLevel level) {
            this.level = level;
        }

        @Override
        protected List<IssueKpiDataItemAdapter> getItems(KpiEnvironment environment,IssueKpiDataItemAdapterFactory subject) {
            AnalyticsDataSetsGenerator datasetFactory = new AnalyticsDataSetsGenerator(environment);
            return subject.getItems(datasetFactory.getOptionalDataSetForLevel(level));
        }
    }


    class ItemsAsserter {
    
        private Map<String, IssueKpiDataItemAdapterAsserter> items;
    
        ItemsAsserter(List<IssueKpiDataItemAdapter> items) {
            this.items = items.stream().collect(Collectors.toMap(IssueKpiDataItemAdapter::getIssueKey, IssueKpiDataItemAdapterAsserter::new));
        }
        
        ItemsAsserter amountOfBuiltItems(int size) {
            assertThat(items.values().size(),is(size));
            return this;
        }
        
        IssueKpiDataItemAdapterAsserter givenItem(String pkey) {
            return Optional.ofNullable(items.get(pkey))
                        .orElseThrow(() -> new AssertionError("Issue not found: "+pkey));
        }
        
        class IssueKpiDataItemAdapterAsserter {
            private IssueKpiDataItemAdapter subject;
            private List<String> testedStatuses = new LinkedList<>();
    
            private IssueKpiDataItemAdapterAsserter(IssueKpiDataItemAdapter subject) {
                this.subject = subject;
            }
            
            IssueKpiDataItemAdapterAsserter noMoreStatuses() {
                Set<String> actualStatuses = subject.getTransitions().keySet();
                assertThat(testedStatuses).containsAll(actualStatuses);
                return this;
            }
    
            IssueKpiDataItemAdapterAsserter isSubtask() {
                assertThat(subject.getLevel(), is(SUBTASKS));
                return this;
            }
            
            IssueKpiDataItemAdapterAsserter isDemand() {
                assertThat(subject.getLevel(), is(DEMAND));
                return this;
            }
            
            IssueKpiDataItemAdapterAsserter isFeature() {
                assertThat(subject.getLevel(), is(FEATURES));
                return this;
            }
            
            IssueKpiDataItemAdapterAsserter isUnmapped() {
                assertThat(subject.getLevel(), is(UNMAPPED));
                return this;
            }
            
            IssueKpiDataItemAdapterAsserter hasType(String type) {
                IssueTypeKpi itemType = subject.getIssueType().orElseThrow(() -> missingType());
                assertThat(itemType.getType(),is(type));
                return this;
            }
            
            DateAsserter status(String status) {
                this.testedStatuses.add(status);
                Map<String, ZonedDateTime> transitions = subject.getTransitions();
                if(!transitions.containsKey(status))
                    Assert.fail(String.format("Status %s for issue %s not configured", status,subject.getIssueKey()));
                
                ZonedDateTime date = transitions.get(status);
                return new DateAsserter(date);
            }
    
            AssertionError missingType() {
                return new AssertionError(String.format("Type for issue %s not configured",subject.getIssueKey()));
            }
            
            ItemsAsserter eoIA(){
                noMoreStatuses();
                return ItemsAsserter.this;
            }
            
            class DateAsserter {
                private ZonedDateTime date;
                
                private DateAsserter(ZonedDateTime date) {
                    this.date = date;
                }
                
                IssueKpiDataItemAdapterAsserter hasNoDate() {
                    Assert.assertNull(date);
                    return IssueKpiDataItemAdapterAsserter.this;
                }
    
                IssueKpiDataItemAdapterAsserter hasDate(String otherDate) {
                    assertThat(date.toLocalDate().toString(), is(otherDate));
                    return IssueKpiDataItemAdapterAsserter.this;
                }
                
            }

            
        }
        
}
