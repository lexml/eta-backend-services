package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.emenda.NotaAlteracao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DispositivoEmendaAdicionadoPojo implements DispositivoEmendaAdicionado {
    private String tipo;
    private String id;
    private String rotulo;
    private String texto;
    private Boolean textoOmitido;
    private Boolean abreAspas;
    private Boolean fechaAspas;
    private NotaAlteracao notaAlteracao;
    private Boolean ondeCouber;
    private String idPai;
    private String idIrmaoAnterior;
    private String urnNormaAlterada;
    private Boolean existeNaNormaAlterada;
    private List<DispositivoEmendaAdicionadoPojo> filhos = new ArrayList<>();

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

    public Boolean isTextoOmitido() {
        return textoOmitido;
    }

    @SuppressWarnings("unused")
    public void setTextoOmitido(Boolean textoOmitido) {
        this.textoOmitido = textoOmitido;
    }

    public Boolean isAbreAspas() {
        return abreAspas;
    }

    @SuppressWarnings("unused")
    public void setAbreAspas(Boolean abreAspas) {
        this.abreAspas = abreAspas;
    }

    public Boolean isFechaAspas() {
        return fechaAspas;
    }

    @SuppressWarnings("unused")
    public void setFechaAspas(Boolean fechaAspas) {
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

    public Boolean isOndeCouber() {
        return ondeCouber;
    }

    @SuppressWarnings("unused")
    public void setOndeCouber(Boolean ondeCouber) {
        this.ondeCouber = ondeCouber;
    }

    @Override
    public String getIdPai() {
        return idPai;
    }

    @SuppressWarnings("unused")
    public void setIdPai(String idPai) {
        this.idPai = idPai;
    }

    @Override
    public String getIdIrmaoAnterior() {
        return idIrmaoAnterior;
    }

    @SuppressWarnings("unused")
    public void setIdIrmaoAnterior(String idIrmaoAnterior) {
        this.idIrmaoAnterior = idIrmaoAnterior;
    }

    @Override
    public String getUrnNormaAlterada() {
        return urnNormaAlterada;
    }

    @SuppressWarnings("unused")
    public void setUrnNormaAlterada(String urnNormaAlterada) {
        this.urnNormaAlterada = urnNormaAlterada;
    }

    public Boolean isExisteNaNormaAlterada() {
        return existeNaNormaAlterada;
    }

    @SuppressWarnings("unused")
    public void setExisteNaNormaAlterada(Boolean existeNaNormaAlterada) {
        this.existeNaNormaAlterada = existeNaNormaAlterada;
    }

    public List<? extends DispositivoEmendaAdicionado> getFilhos() {
        return filhos;
    }

    @SuppressWarnings("unused")
    public void setFilhos(List<DispositivoEmendaAdicionadoPojo> filhos) {
        this.filhos = filhos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoEmendaAdicionadoPojo that = (DispositivoEmendaAdicionadoPojo) o;
        return tipo.equals(that.tipo) && id.equals(that.id) && rotulo.equals(that.rotulo) && texto.equals(that.texto) && textoOmitido.equals(that.textoOmitido) && abreAspas.equals(that.abreAspas) && fechaAspas.equals(that.fechaAspas) && notaAlteracao == that.notaAlteracao && ondeCouber.equals(that.ondeCouber) && idPai.equals(that.idPai) && idIrmaoAnterior.equals(that.idIrmaoAnterior) && urnNormaAlterada.equals(that.urnNormaAlterada) && existeNaNormaAlterada.equals(that.existeNaNormaAlterada) && filhos.equals(that.filhos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao, ondeCouber, idPai, idIrmaoAnterior, urnNormaAlterada, existeNaNormaAlterada, filhos);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaAdicionadoPojo{" +
                "tipo='" + tipo + '\'' +
                ", id='" + id + '\'' +
                ", rotulo='" + rotulo + '\'' +
                ", texto='" + texto + '\'' +
                ", textoOmitido=" + textoOmitido +
                ", abreAspas=" + abreAspas +
                ", fechaAspas=" + fechaAspas +
                ", notaAlteracao=" + notaAlteracao +
                ", ondeCouber=" + ondeCouber +
                ", idPai='" + idPai + '\'' +
                ", idIrmaoAnterior='" + idIrmaoAnterior + '\'' +
                ", urnNormaAlterada='" + urnNormaAlterada + '\'' +
                ", existeNaNormaAlterada=" + existeNaNormaAlterada +
                ", filhos=" + filhos +
                '}';
    }
}
