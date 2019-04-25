package objective.taskboard.filterPreferences;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.FilterFieldValue;

public class CardFieldFilterUtils {

    public static final String ISSUE_TYPE_1_VALUE = "1";
    public static final String ISSUE_TYPE_2_VALUE = "2";
    public static final String ISSUE_TYPE_3_VALUE = "3";

    public static final String PROJECT_1_VALUE = "PROJ_1";
    public static final String PROJECT_2_VALUE = "PROJ_2";
    public static final String PROJECT_3_VALUE = "PROJ_3";

    public static final String TEAM_1_VALUE = "TEAM_1";
    public static final String TEAM_2_VALUE = "TEAM_2";
    public static final String TEAM_3_VALUE = "TEAM_3";

    public static final String VALUE_NOT_REGISTERED = "9999999999";

    public static List<CardFieldFilter> cardFieldFiltersAllValuesSelected() {
        return asList(
                new CardFieldFilter(FieldSelector.ISSUE_TYPE, issueTypeFilterFieldsValuesAllSelected()),
                new CardFieldFilter(FieldSelector.PROJECT, projectFilterFieldsValuesAllSelected()),
                new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected())
                );
    }

    public static List<CardFieldFilter> cardFieldFiltersAllValuesSelectedButProjectAllFalse() {
        return asList(
                new CardFieldFilter(FieldSelector.ISSUE_TYPE, issueTypeFilterFieldsValuesAllSelected()),
                new CardFieldFilter(FieldSelector.PROJECT, projectFilterFieldsValuesAllFalse()),
                new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected())
        );
    }

    public static List<FilterFieldValue> issueTypeFilterFieldsValuesAllSelected() {
        return asList(
                new FilterFieldValue(ISSUE_TYPE_1_VALUE, ISSUE_TYPE_1_VALUE, null, true),
                new FilterFieldValue(ISSUE_TYPE_2_VALUE, ISSUE_TYPE_2_VALUE, null, true),
                new FilterFieldValue(ISSUE_TYPE_2_VALUE, ISSUE_TYPE_3_VALUE, null, true)
                );
    }

    public static List<FilterFieldValue> projectFilterFieldsValuesAllSelected() {
        return asList(
                new FilterFieldValue(PROJECT_1_VALUE, PROJECT_1_VALUE, null, true),
                new FilterFieldValue(PROJECT_2_VALUE, PROJECT_2_VALUE, null, true),
                new FilterFieldValue(PROJECT_3_VALUE, PROJECT_3_VALUE, null, true)
                );
    }

    public static List<FilterFieldValue> projectFilterFieldsValuesAllFalse() {
        return asList(
                new FilterFieldValue(PROJECT_1_VALUE, PROJECT_1_VALUE, null, false),
                new FilterFieldValue(PROJECT_2_VALUE, PROJECT_2_VALUE, null, false),
                new FilterFieldValue(PROJECT_3_VALUE, PROJECT_3_VALUE, null, false)
        );
    }

    public static List<FilterFieldValue> teamFilterFieldsValuesAllSelected() {
        return asList(
                new FilterFieldValue(TEAM_1_VALUE, TEAM_1_VALUE, null, true),
                new FilterFieldValue(TEAM_2_VALUE, TEAM_2_VALUE, null, true),
                new FilterFieldValue(TEAM_3_VALUE, TEAM_3_VALUE, null, true)
                );
    }

    public static FilterFieldValue getFilterFieldValue(List<CardFieldFilter> cardFieldFilters, FieldSelector fieldSelector, String value) {
        CardFieldFilter cardFieldFilterSelected = cardFieldFilters.stream()
            .filter(cardFieldFilter -> cardFieldFilter.getFieldSelector() == fieldSelector)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No \"FieldSelector\" found with "+ fieldSelector + " value."));
        return getFilterFieldValue(cardFieldFilterSelected, value);
    }

    public static FilterFieldValue getFilterFieldValue(CardFieldFilter cardFieldFilter, String value) {
        return cardFieldFilter.getFilterFieldsValues().stream()
            .filter(filterFieldValue -> filterFieldValue.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No \"FilterFieldValue\" found with "+ value + " value."));
    }

}
