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
package objective.taskboard.data;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CardFieldFilter {

    private FieldSelector fieldSelector;
    private List<FilterFieldValue> filterFieldsValues;

    public CardFieldFilter(final FieldSelector fieldSelector, final List<FilterFieldValue> filterFieldsValues) {
        this.fieldSelector = fieldSelector;
        this.filterFieldsValues = filterFieldsValues;
    }

    public boolean isIssueSelected(Issue issue) {
        return getFieldSelector().isIssueSelected(getFilterFieldsValues(), issue);
    }

    public FieldSelector getFieldSelector() {
        return this.fieldSelector;
    }

    public List<FilterFieldValue> getFilterFieldsValues() {
        return this.filterFieldsValues;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum FieldSelector {
        ISSUE_TYPE("Issue Type", (filterFieldsValues, issue) -> issueValueExistsAndIsSelect(filterFieldsValues, String.valueOf(issue.getType()))),

        PROJECT("Project", (filterFieldsValues, issue) -> issueValueExistsAndIsSelect(filterFieldsValues, issue.getProjectKey())),

        TEAM("Team", (filterFieldsValues, issue) -> {
            List<String> valuesToTest = issue.getTeams().stream()
                    .map(t -> t.name)
                    .collect(toList());

            List<FilterFieldValue> teamsVisibleToUserOnIssue = filterFieldsValues.stream()
                .filter(filterFieldValue -> valuesToTest.contains(filterFieldValue.getValue()))
                .collect(toList());

            return teamsVisibleToUserOnIssue.isEmpty()
                    || teamsVisibleToUserOnIssue.stream().anyMatch(filterFieldValue -> filterFieldValue.isSelected());
        });

        private final String name;
        private final BiPredicate<List<FilterFieldValue>, Issue> isIssueSelected;

        FieldSelector(String name, BiPredicate<List<FilterFieldValue>, Issue> isIssueSelected) {
            this.name = name;
            this.isIssueSelected = isIssueSelected;
        }

        public String getName() {
            return name;
        }

        public boolean isIssueSelected(List<FilterFieldValue> filterFieldsValues, Issue issue) {
            return isIssueSelected.test(filterFieldsValues, issue);
        }

        private static boolean issueValueExistsAndIsSelect(List<FilterFieldValue> filterFieldsValues, String issueValueToTest) {
            return filterFieldsValues.stream()
                .anyMatch(filterFieldValue -> {
                    return issueValueToTest.equals(filterFieldValue.getValue()) && filterFieldValue.isSelected();
                });
        }
    }

}
