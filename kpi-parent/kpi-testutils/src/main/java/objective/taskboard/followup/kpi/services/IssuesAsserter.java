package objective.taskboard.followup.kpi.services;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiAsserter;
import objective.taskboard.followup.kpi.KpiLevel;

public class IssuesAsserter {

    private Map<String,InnerKpiAsserter> issuesAsserter;

    public IssuesAsserter(List<IssueKpi> issuesKpi,KpiEnvironment environment) {
        this.issuesAsserter = map(issuesKpi,environment);
    }

    public IssuesAsserter amountOfIssueIs(int size) {
        assertThat(issuesAsserter.values().size(), is(size));
        return this;
    }

    public InnerKpiAsserter givenIssue(String key) {
        return issuesAsserter.get(key);
    }

    private Map<String, InnerKpiAsserter> map(List<IssueKpi> issuesKpi, KpiEnvironment environment) {
        return issuesKpi.stream()
                .map(i -> new InnerKpiAsserter(i, environment))
                .collect(Collectors.toMap(InnerKpiAsserter::pKey, Function.identity()));
    }

    public class InnerKpiAsserter extends IssueKpiAsserter<IssuesAsserter>{

        public InnerKpiAsserter(IssueKpi subject, KpiEnvironment environment) {
            super(subject, environment,IssuesAsserter.this);
        }

        public InnerKpiAsserter hasLevel(KpiLevel level) {
            assertThat(subject.getLevel(), is(level));
            return this;
        }

        public InnerKpiAsserter hasType(String type) {
            super.hasType(type);
            return this;
        }

        public String pKey() {
            return subject.getIssueKey();
        }
    }
}