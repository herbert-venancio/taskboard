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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CardFieldFilter {

    private FieldSelector fieldSelector;
    private List<FilterFieldValue> filterFieldsValues;

    public CardFieldFilter(final FieldSelector fieldSelector, final List<FilterFieldValue> filterFieldsValues) {
        this.fieldSelector = fieldSelector;
        this.filterFieldsValues = filterFieldsValues;
    }

    public boolean isIssueSelected(Issue issue) {
        List<String> valuesToTest = getFieldSelector().issueValuesToFilter(issue);
        return getFilterFieldsValues().stream()
            .anyMatch(filterFieldValue -> {
                return valuesToTest.contains(filterFieldValue.getValue()) && filterFieldValue.isSelected();
            });
    }

    public FieldSelector getFieldSelector() {
        return this.fieldSelector;
    }

    public List<FilterFieldValue> getFilterFieldsValues() {
        return this.filterFieldsValues;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum FieldSelector {
        ISSUE_TYPE("Issue Type", (issue) -> asList(String.valueOf(issue.getType()))),

        PROJECT("Project", (issue) -> asList(issue.getProjectKey())),

        TEAM("Team", (issue) -> issue.getTeams().stream()
                .map(t -> t.name)
                .distinct()
                .collect(toList()));

        private final String name;
        private final Function<Issue, List<String>> fieldValuesCollector;

        FieldSelector(String name, Function<Issue, List<String>> fieldValuesCollector) {
            this.name = name;
            this.fieldValuesCollector = fieldValuesCollector;
        }

        public String getName() {
            return name;
        }

        public List<String> issueValuesToFilter(Issue issue) {
            return fieldValuesCollector.apply(issue);
        }
    }

}
