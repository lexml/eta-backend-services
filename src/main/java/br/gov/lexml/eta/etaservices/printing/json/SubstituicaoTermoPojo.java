package br.gov.lexml.eta.etaservices.printing.json;

import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.SubstituicaoTermo;
import br.gov.lexml.eta.etaservices.emenda.TipoSubstituicaoTermo;

public class SubstituicaoTermoPojo implements SubstituicaoTermo {

    private TipoSubstituicaoTermo tipo;
    private String termo;
    private String novoTermo;
    private boolean flexaoGenero;
    private boolean flexaoNumero;

    @Override
    public TipoSubstituicaoTermo getTipo() {
        return tipo;
    }

    public void setTipo(TipoSubstituicaoTermo tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getTermo() {
        return termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    @Override
    public String getNovoTermo() {
        return novoTermo;
    }

    public void setNovoTermo(String novoTermo) {
        this.novoTermo = novoTermo;
    }

    @Override
    public boolean isFlexaoGenero() {
        return flexaoGenero;
    }

    public void setFlexaoGenero(boolean flexaoGenero) {
        this.flexaoGenero = flexaoGenero;
    }

    @Override
    public boolean isFlexaoNumero() {
        return flexaoNumero;
    }

    public void setFlexaoNumero(boolean flexaoNumero) {
        this.flexaoNumero = flexaoNumero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubstituicaoTermoPojo that = (SubstituicaoTermoPojo) o;
        return flexaoGenero == that.flexaoGenero &&
                flexaoNumero == that.flexaoNumero &&
                Objects.equals(tipo, that.tipo) &&
                Objects.equals(termo, that.termo) &&
                Objects.equals(novoTermo, that.novoTermo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, termo, novoTermo, flexaoGenero, flexaoNumero);
    }
    
    @Override
    public String toString() {
        return "SubstituicaoTermoPojo{" +
                "tipo=" + tipo +
                ", termo='" + termo + '\'' +
                ", novoTermo='" + novoTermo + '\'' +
                ", flexaoGenero=" + flexaoGenero +
                ", flexaoNumero=" + flexaoNumero +
                '}';
    }

}
