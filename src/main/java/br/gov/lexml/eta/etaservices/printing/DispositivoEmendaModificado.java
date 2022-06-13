package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class DispositivoEmendaModificado implements DispositivoEmenda {
    private String tipo;
    private String id;
    private String rotulo;
    private String texto;
    private Boolean textoOmitido;
    private Boolean abreAspas;
    private Boolean fechaAspas;
    private NotaAlteracao notaAlteracao;

    public DispositivoEmendaModificado(
            final String tipo,
            final String id,
            final String rotulo,
            final String texto,
            final Boolean textoOmitido,
            final Boolean abreAspas,
            final Boolean fechaAspas,
            final NotaAlteracao notaAlteracao
    ) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
        this.texto = texto;
        this.textoOmitido = textoOmitido;
        this.abreAspas = abreAspas;
        this.fechaAspas = fechaAspas;
        this.notaAlteracao = notaAlteracao;
    }

    public DispositivoEmendaModificado() {

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

    public String getTexto() {
        return texto;
    }

    public Boolean getTextoOmitido() {
        return textoOmitido;
    }

    public Boolean getAbreAspas() {
        return abreAspas;
    }

    public Boolean getFechaAspas() {
        return fechaAspas;
    }

    public NotaAlteracao getNotaAlteracao() {
        return notaAlteracao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DispositivoEmendaModificado) obj;
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
