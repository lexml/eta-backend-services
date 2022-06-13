package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class OpcoesImpressao {
    private boolean imprimirBrasao;
    private String textoCabecalho;
    private boolean reduzirEspacoEntreLinhas;

    public OpcoesImpressao(
            final boolean imprimirBrasao,
            final String textoCabecalho,
            final boolean reduzirEspacoEntreLinhas
    ) {
        this.imprimirBrasao = imprimirBrasao;
        this.textoCabecalho = textoCabecalho;
        this.reduzirEspacoEntreLinhas = reduzirEspacoEntreLinhas;
    }

    public OpcoesImpressao() {
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
        var that = (OpcoesImpressao) obj;
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
        return "OpcoesImpressao[" +
                "imprimirBrasao=" + imprimirBrasao + ", " +
                "textoCabecalho=" + textoCabecalho + ", " +
                "reduzirEspacoEntreLinhas=" + reduzirEspacoEntreLinhas + ']';
    }

}
