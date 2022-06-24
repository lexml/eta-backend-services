package br.gov.lexml.eta.etaservices.printing;

public interface OpcoesImpressao {
    boolean isImprimirBrasao();

    String getTextoCabecalho();

    boolean isReduzirEspacoEntreLinhas();
}
