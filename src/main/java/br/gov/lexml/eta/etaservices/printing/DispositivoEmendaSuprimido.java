package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class DispositivoEmendaSuprimido implements DispositivoEmenda {
    private String tipo;
    private String id;
    private String rotulo;

    public DispositivoEmendaSuprimido(
            final String tipo,
            final String id,
            final String rotulo
    ) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
    }

    public DispositivoEmendaSuprimido() {

    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DispositivoEmendaSuprimido) obj;
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
