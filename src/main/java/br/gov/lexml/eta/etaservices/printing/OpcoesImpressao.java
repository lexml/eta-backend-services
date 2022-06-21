package br.gov.lexml.eta.etaservices.printing;

public record OpcoesImpressao(
        boolean imprimirBrasao,
        String textoCabecalho,
        boolean reduzirEspacoEntreLinhas) {

}
