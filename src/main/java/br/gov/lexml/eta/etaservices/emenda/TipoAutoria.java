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

    public static TipoAutoria parse(String tipo) {
        switch (tipo) {
            case "Não identificado":
                return NAO_IDENTIFICADO;
            case "Parlamentar":
                return PARLAMENTAR;
            case "Comissão":
                return COMISSAO;
            case "Casa Legislativa":
                return CASA_LEGISLATIVA;
            default:
                return null;
        }
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }
}
