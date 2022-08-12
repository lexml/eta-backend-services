package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoColegiado {
    COMISSAO("Comissão"),
    PLENARIO("Plenário");

    @JsonValue
    private final String descricao;

    TipoColegiado(final String descricao) {
        this.descricao = descricao;
    }

    public static TipoColegiado parse(String valueOf) {
        switch (valueOf) {
            case "Comissão":
                return COMISSAO;
            case "Plenário":
                return PLENARIO;
            default:
                return null;
        }
    }

    public String getDescricao() {
        return descricao;
    }
}
