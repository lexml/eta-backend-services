package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.SubstituicaoTermo;
import br.gov.lexml.eta.etaservices.emenda.TipoSubstituicaoTermo;

public final class SubstituicaoTermoRecord implements SubstituicaoTermo {
    private final TipoSubstituicaoTermo tipo;
    private final String termo;
    private final String novoTermo;
    private final boolean flexaoGenero;
    private final boolean flexaoNumero;

    public SubstituicaoTermoRecord(
    		TipoSubstituicaoTermo tipo,
    		String termo,
    		String novoTermo,
    		boolean flexaoGenero,
    		boolean flexaoNumero) {
        this.tipo = tipo;
        this.termo = termo;
        this.novoTermo = novoTermo;
        this.flexaoGenero = flexaoGenero;
        this.flexaoNumero = flexaoNumero;
    }

    @Override
    public TipoSubstituicaoTermo getTipo() {
        return tipo;
    }

    public String getTermo() {
        return termo;
    }

    public String getNovoTermo() {
        return novoTermo;
    }

    public boolean isFlexaoGenero() {
        return flexaoGenero;
    }

    public boolean isFlexaoNumero() {
        return flexaoNumero;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SubstituicaoTermoRecord that = (SubstituicaoTermoRecord) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.termo, that.termo) &&
                Objects.equals(this.novoTermo, that.novoTermo) &&
                this.flexaoGenero == that.flexaoGenero &&
                this.flexaoNumero == that.flexaoNumero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, termo, novoTermo, flexaoGenero, flexaoNumero);
    }

    @Override
    public String toString() {
        return "SubstituicaoTermoRecord[" +
                "tipo=" + tipo + ", " +
                "termo=" + termo + ", " +
                "novoTermo=" + novoTermo + ", " +
                "flexaoGenero=" + flexaoGenero + ", " +
                "flexaoNumero=" + flexaoNumero + ']';
    }

}
