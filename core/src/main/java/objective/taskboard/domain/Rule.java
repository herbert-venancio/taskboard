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
}
