package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoAutoria {
    NAO_IDENTIFICADO("Não identificado"),
    PARLAMENTAR("Parlamentar"),
    COMISSAO("Comissão"),
    CASA_LEGISLATIVA("Casa Legislativa");

    private final String descricao;

    TipoAutoria(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }
}
