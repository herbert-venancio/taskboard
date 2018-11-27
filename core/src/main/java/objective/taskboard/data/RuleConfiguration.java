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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import objective.taskboard.domain.Rule;

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

    public String getChave() {
        return this.chave;
    }

    public String getValor() {
        return this.valor;
    }

    public void setChave(final String chave) {
        this.chave = chave;
    }

    public void setValor(final String valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuleConfiguration that = (RuleConfiguration) o;

        if (chave != null ? !chave.equals(that.chave) : that.chave != null) return false;
        return valor != null ? valor.equals(that.valor) : that.valor == null;
    }

    @Override
    public int hashCode() {
        int result = chave != null ? chave.hashCode() : 0;
        result = 31 * result + (valor != null ? valor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RuleConfiguration{" +
                "chave='" + chave + '\'' +
                ", valor='" + valor + '\'' +
                '}';
    }

    public RuleConfiguration() {
    }

    @java.beans.ConstructorProperties({"chave", "valor"})
    private RuleConfiguration(final String chave, final String valor) {
        this.chave = chave;
        this.valor = valor;
    }
}
