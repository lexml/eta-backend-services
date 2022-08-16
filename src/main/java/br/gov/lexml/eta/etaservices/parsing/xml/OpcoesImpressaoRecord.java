package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;

import java.util.Objects;

public final class OpcoesImpressaoRecord implements OpcoesImpressao {
    private final boolean imprimirBrasao;
    private final String textoCabecalho;
    private final boolean reduzirEspacoEntreLinhas;

    public OpcoesImpressaoRecord(
            boolean imprimirBrasao,
            String textoCabecalho,
            boolean reduzirEspacoEntreLinhas) {
        this.imprimirBrasao = imprimirBrasao;
        this.textoCabecalho = textoCabecalho;
        this.reduzirEspacoEntreLinhas = reduzirEspacoEntreLinhas;
    }

    public boolean isImprimirBrasao() {
        return imprimirBrasao;
    }

    public String getTextoCabecalho() {
        return textoCabecalho;
    }

    public boolean isReduzirEspacoEntreLinhas() {
        return reduzirEspacoEntreLinhas;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        OpcoesImpressaoRecord that = (OpcoesImpressaoRecord) obj;
        return this.imprimirBrasao == that.imprimirBrasao &&
                Objects.equals(this.textoCabecalho, that.textoCabecalho) &&
                this.reduzirEspacoEntreLinhas == that.reduzirEspacoEntreLinhas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imprimirBrasao, textoCabecalho, reduzirEspacoEntreLinhas);
    }

    @Override
    public String toString() {
        return "OpcoesImpressaoRecord[" +
                "imprimirBrasao=" + imprimirBrasao + ", " +
                "textoCabecalho=" + textoCabecalho + ", " +
                "reduzirEspacoEntreLinhas=" + reduzirEspacoEntreLinhas + ']';
    }


}
