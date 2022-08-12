package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaSuprimido;

import java.util.Objects;

public class DispositivoEmendaSuprimidoPojo implements DispositivoEmendaSuprimido {
    private String tipo;
    private String id;
    private String rotulo;

    @Override
    public String getTipo() {
        return tipo;
    }

    @SuppressWarnings("unused")
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    public String getRotulo() {
        return rotulo;
    }

    @SuppressWarnings("unused")
    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoEmendaSuprimidoPojo that = (DispositivoEmendaSuprimidoPojo) o;
        return tipo.equals(that.tipo) && id.equals(that.id) && rotulo.equals(that.rotulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaSuprimidoPojo{" +
                "tipo='" + tipo + '\'' +
                ", id='" + id + '\'' +
                ", rotulo='" + rotulo + '\'' +
                '}';
    }
}

