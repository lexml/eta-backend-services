package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.emenda.NotaAlteracao;

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
    private String urnNormaAlterada;

    @Override
    public String getTipo() {
        return tipo;
    }

    @SuppressWarnings("unused")
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    @SuppressWarnings("unused")
    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getTexto() {
        return texto;
    }

    @SuppressWarnings("unused")
    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public Boolean isTextoOmitido() {
        return textoOmitido;
    }

    @SuppressWarnings("unused")
    public void setTextoOmitido(boolean textoOmitido) {
        this.textoOmitido = textoOmitido;
    }

    @Override
    public Boolean isAbreAspas() {
        return abreAspas;
    }

    @SuppressWarnings("unused")
    public void setAbreAspas(boolean abreAspas) {
        this.abreAspas = abreAspas;
    }

    @Override
    public Boolean isFechaAspas() {
        return fechaAspas;
    }

    @SuppressWarnings("unused")
    public void setFechaAspas(boolean fechaAspas) {
        this.fechaAspas = fechaAspas;
    }

    @Override
    public NotaAlteracao getNotaAlteracao() {
        return notaAlteracao;
    }

    @SuppressWarnings("unused")
    public void setNotaAlteracao(NotaAlteracao notaAlteracao) {
        this.notaAlteracao = notaAlteracao;
    }

	@Override
    public String getUrnNormaAlterada() {
        return urnNormaAlterada;
    }

	@SuppressWarnings("unused")
    public String setUrnNormaAlterada(String urnNormaAlterada) {
        return this.urnNormaAlterada = urnNormaAlterada;
    }    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoEmendaModificadoPojo that = (DispositivoEmendaModificadoPojo) o;
        return textoOmitido == that.textoOmitido && abreAspas == that.abreAspas && fechaAspas == that.fechaAspas && tipo.equals(that.tipo) && id.equals(that.id) && rotulo.equals(that.rotulo) && texto.equals(that.texto) && notaAlteracao == that.notaAlteracao && urnNormaAlterada.equals(that.urnNormaAlterada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao, urnNormaAlterada);
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
                ", urnNormaAlterada='" + urnNormaAlterada + '\'' +
                '}';
    }
}
