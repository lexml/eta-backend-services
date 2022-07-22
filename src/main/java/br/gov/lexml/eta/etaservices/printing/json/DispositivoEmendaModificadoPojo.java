package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.printing.NotaAlteracao;

import java.util.Objects;

public class DispositivoEmendaModificadoPojo implements DispositivoEmendaModificado {
    private String tipo;
    private String id;
    private String rotulo;
    private String texto;
    private boolean textoOmitido;
    private boolean abreAspas;
    private boolean fechaAspas;
    private NotaAlteracao notaAlteracao;

    @Override
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public Boolean isTextoOmitido() {
        return textoOmitido;
    }

    public void setTextoOmitido(boolean textoOmitido) {
        this.textoOmitido = textoOmitido;
    }

    @Override
    public Boolean isAbreAspas() {
        return abreAspas;
    }

    public void setAbreAspas(boolean abreAspas) {
        this.abreAspas = abreAspas;
    }

    @Override
    public Boolean isFechaAspas() {
        return fechaAspas;
    }

    public void setFechaAspas(boolean fechaAspas) {
        this.fechaAspas = fechaAspas;
    }

    @Override
    public NotaAlteracao getNotaAlteracao() {
        return notaAlteracao;
    }

    public void setNotaAlteracao(NotaAlteracao notaAlteracao) {
        this.notaAlteracao = notaAlteracao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoEmendaModificadoPojo that = (DispositivoEmendaModificadoPojo) o;
        return textoOmitido == that.textoOmitido && abreAspas == that.abreAspas && fechaAspas == that.fechaAspas && tipo.equals(that.tipo) && id.equals(that.id) && rotulo.equals(that.rotulo) && texto.equals(that.texto) && notaAlteracao == that.notaAlteracao;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaModificadoPojo{" +
                "tipo='" + tipo + '\'' +
                ", id='" + id + '\'' +
                ", rotulo='" + rotulo + '\'' +
                ", texto='" + texto + '\'' +
                ", textoOmitido=" + textoOmitido +
                ", abreAspas=" + abreAspas +
                ", fechaAspas=" + fechaAspas +
                ", notaAlteracao=" + notaAlteracao +
                '}';
    }
}
