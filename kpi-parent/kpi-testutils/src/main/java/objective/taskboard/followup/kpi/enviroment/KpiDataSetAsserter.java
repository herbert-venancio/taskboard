package objective.taskboard.followup.kpi.enviroment;

import java.util.Iterator;
import java.util.List;

import org.assertj.core.api.Assertions;

public abstract class KpiDataSetAsserter<DP, EDPB extends KpiExpectedDataPointBuilder<DP>> {
    private List<DP> dataSet;

    public KpiDataSetAsserter(List<DP> dataSet) {
        this.dataSet = dataSet;
    }

    public KpiDataSetAsserter<DP, EDPB> hasSize(int size) {
        Assertions.assertThat(dataSet).hasSize(size);
        return this;
    }

    public void emptyDataSet() {
        hasSize(0);
    }

    @SafeVarargs
    public final KpiDataSetAsserter<DP, EDPB> hasPoints(EDPB ...expectedPointsBuilders) {
        Assertions.assertThat(expectedPointsBuilders.length).isEqualTo(dataSet.size());
        List<KpiDataPointAsserter<DP>> asserters = buildAsserters(expectedPointsBuilders);
        assertAllPoints(asserters);
        return this;
    }

    protected abstract List<KpiDataPointAsserter<DP>> buildAsserters(EDPB[] expectedPointsBuilders);

    private void assertAllPoints(List<KpiDataPointAsserter<DP>> asserters) {
        Iterator<KpiDataPointAsserter<DP>> assertersIterator = asserters.iterator();
        Iterator<DP> dataSetIterator = dataSet.iterator();
        while (assertersIterator.hasNext()) {
            KpiDataPointAsserter<DP> asserter = assertersIterator.next();
            DP subject = dataSetIterator.next();
            asserter.doAssert(subject);
        }
    }
}