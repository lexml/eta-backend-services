package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.printing.NotaAlteracao;

import java.util.Objects;

public final class DispositivoEmendaModificadoRecord implements DispositivoEmendaModificado {
    private final String tipo;
    private final String id;
    private final String rotulo;
    private final String texto;
    private final Boolean textoOmitido;
    private final Boolean abreAspas;
    private final Boolean fechaAspas;
    private final NotaAlteracao notaAlteracao;

    public DispositivoEmendaModificadoRecord(
            String tipo,
            String id,
            String rotulo,
            String texto,
            Boolean textoOmitido,
            Boolean abreAspas,
            Boolean fechaAspas,
            NotaAlteracao notaAlteracao) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
        this.texto = texto;
        this.textoOmitido = textoOmitido;
        this.abreAspas = abreAspas;
        this.fechaAspas = fechaAspas;
        this.notaAlteracao = notaAlteracao;
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

    public String getTexto() {
        return texto;
    }

    public Boolean isTextoOmitido() {
        return textoOmitido;
    }

    public Boolean isAbreAspas() {
        return abreAspas;
    }

    public Boolean isFechaAspas() {
        return fechaAspas;
    }

    public NotaAlteracao getNotaAlteracao() {
        return notaAlteracao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        DispositivoEmendaModificadoRecord that = (DispositivoEmendaModificadoRecord) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.rotulo, that.rotulo) &&
                Objects.equals(this.texto, that.texto) &&
                Objects.equals(this.textoOmitido, that.textoOmitido) &&
                Objects.equals(this.abreAspas, that.abreAspas) &&
                Objects.equals(this.fechaAspas, that.fechaAspas) &&
                Objects.equals(this.notaAlteracao, that.notaAlteracao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaModificado[" +
                "tipo=" + tipo + ", " +
                "id=" + id + ", " +
                "rotulo=" + rotulo + ", " +
                "texto=" + texto + ", " +
                "textoOmitido=" + textoOmitido + ", " +
                "abreAspas=" + abreAspas + ", " +
                "fechaAspas=" + fechaAspas + ", " +
                "notaAlteracao=" + notaAlteracao + ']';
    }


}
