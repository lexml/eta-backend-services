package br.gov.lexml.eta.etaservices.emenda;

public interface RefProposicaoEmendada {
    String getUrn();

    String getSigla();

    String getNumero();

    String getAno();

    String getEmenta();

    String getIdentificacaoTexto();

    boolean isEmendarTextoSubstitutivo();
}
