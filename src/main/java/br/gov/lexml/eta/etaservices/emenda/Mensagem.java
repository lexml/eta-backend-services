package br.gov.lexml.eta.etaservices.emenda;

public interface Mensagem {
    String getTipo();
    String getDescricao();
    Boolean getFix();
}