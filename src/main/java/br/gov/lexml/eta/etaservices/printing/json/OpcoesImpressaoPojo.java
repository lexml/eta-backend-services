package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.OpcoesImpressao;

import java.util.Objects;

public class OpcoesImpressaoPojo implements OpcoesImpressao {
    private boolean imprimirBrasao;
    private String textoCabecalho;
    private boolean reduzirEspacoEntreLinhas;

    @Override
    public boolean isImprimirBrasao() {
        return imprimirBrasao;
    }

    @SuppressWarnings("unused")
    public void setImprimirBrasao(boolean imprimirBrasao) {
        this.imprimirBrasao = imprimirBrasao;
    }

    @Override
    public String getTextoCabecalho() {
        return textoCabecalho;
    }

    @SuppressWarnings("unused")
    public void setTextoCabecalho(String textoCabecalho) {
        this.textoCabecalho = textoCabecalho;
    }

    @Override
    public boolean isReduzirEspacoEntreLinhas() {
        return reduzirEspacoEntreLinhas;
    }

    @SuppressWarnings("unused")
    public void setReduzirEspacoEntreLinhas(boolean reduzirEspacoEntreLinhas) {
        this.reduzirEspacoEntreLinhas = reduzirEspacoEntreLinhas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpcoesImpressaoPojo that = (OpcoesImpressaoPojo) o;
        return imprimirBrasao == that.imprimirBrasao && reduzirEspacoEntreLinhas == that.reduzirEspacoEntreLinhas && Objects.equals(textoCabecalho, that.textoCabecalho);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imprimirBrasao, textoCabecalho, reduzirEspacoEntreLinhas);
    }

    @Override
    public String toString() {
        return "OpcoesImpressaoPojo{" +
                "imprimirBrasao=" + imprimirBrasao +
                ", textoCabecalho='" + textoCabecalho + '\'' +
                ", reduzirEspacoEntreLinhas=" + reduzirEspacoEntreLinhas +
                '}';
    }
}

