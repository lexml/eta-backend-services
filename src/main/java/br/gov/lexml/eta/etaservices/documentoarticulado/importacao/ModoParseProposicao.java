package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

/**
 * Objetivo do parseamento de proposições em DOCX e TXT (e normalização de XML).
 */
public enum ModoParseProposicao {

    /**
     * Cenário 2 (padrão): aproveitamento de ementa e articulação para criação de nova proposição no
     * {@code lexml-eta}, ignorando epígrafes/preâmbulos de proposições ou normas existentes e tolerando
     * avisos não-fatais de técnica legislativa.
     */
    APROVEITAMENTO,

    /**
     * Cenário 1: parseamento estrutural completo (epígrafe, ementa, preâmbulo e articulação) de uma
     * proposição existente utilizando um perfil específico do {@code lexml-parser-projeto-lei}.
     */
    COMPLETO

}

