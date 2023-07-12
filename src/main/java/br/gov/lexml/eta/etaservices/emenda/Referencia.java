package br.gov.lexml.eta.etaservices.emenda;

public interface Referencia {
    String getTipo();
    Integer getUuid();
    String getUuid2();
    String getLexmlId();
    Conteudo getConteudo();
    String getDescricaoSituacao();
    Integer getUuidAlteracao();
    String getUuid2Alteracao();
    Boolean isExisteNaNormaAlterada();
}