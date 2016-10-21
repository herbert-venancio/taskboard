package objective.taskboard.data;

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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import objective.taskboard.domain.Rule;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleConfiguration implements Serializable {

    private static final long serialVersionUID = 142384677393751503L;

    private String chave;
    private String valor;

    public static RuleConfiguration from(Rule rule) {
        return new RuleConfiguration(rule.getChave(), rule.getValor());
    }

    public static Collection<RuleConfiguration> from(String jsonRules) throws JSONException {
        Collection<RuleConfiguration> ruleConfiguration = new ArrayList<RuleConfiguration>();
        JSONArray json = new JSONArray("[" + jsonRules + "]");

        for (int i = 0; i < json.length(); i++) {
            Rule rule = new Rule(json.getJSONObject(i));
            ruleConfiguration.add(RuleConfiguration.from(rule));
        }

        return ruleConfiguration;
    }

}