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
package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
public class Rule extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chave;

    private String valor;

    @ManyToOne
    @JoinColumn(name = "lane")
    private Lane lane;

    public Rule(JSONObject json) throws JSONException {
        this.chave = json.getString("chave");
        this.valor = json.getString("valor");
    }

    public String getChave() {
        return this.chave;
    }

    public String getValor() {
        return this.valor;
    }

    public Lane getLane() {
        return this.lane;
    }

    public void setChave(final String chave) {
        this.chave = chave;
    }

    public void setValor(final String valor) {
        this.valor = valor;
    }

    public void setLane(final Lane lane) {
        this.lane = lane;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "chave='" + chave + '\'' +
                ", valor='" + valor + '\'' +
                ", lane=" + lane +
                '}';
    }

    public Rule() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rule rule = (Rule) o;

        if (chave != null ? !chave.equals(rule.chave) : rule.chave != null) return false;
        if (valor != null ? !valor.equals(rule.valor) : rule.valor != null) return false;
        return lane != null ? lane.equals(rule.lane) : rule.lane == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (chave != null ? chave.hashCode() : 0);
        result = 31 * result + (valor != null ? valor.hashCode() : 0);
        result = 31 * result + (lane != null ? lane.hashCode() : 0);
        return result;
    }
}
