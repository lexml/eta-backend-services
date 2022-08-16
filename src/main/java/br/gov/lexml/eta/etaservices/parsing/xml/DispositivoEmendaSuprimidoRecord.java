package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaSuprimido;

import java.util.Objects;

public final class DispositivoEmendaSuprimidoRecord implements
        DispositivoEmendaSuprimido {
    private final String tipo;
    private final String id;
    private final String rotulo;

    public DispositivoEmendaSuprimidoRecord(String tipo, String id, String rotulo) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getId() {
        return id;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        DispositivoEmendaSuprimidoRecord that = (DispositivoEmendaSuprimidoRecord) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.rotulo, that.rotulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaSuprimido[" +
                "tipo=" + tipo + ", " +
                "id=" + id + ", " +
                "rotulo=" + rotulo + ']';
    }


}
