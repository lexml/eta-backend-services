package br.gov.lexml.eta.etaservices.printing;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoColegiado {
    COMISSAO("Comissão"),
    @SuppressWarnings("unused")
    PLENARIO("Plenário");

    @JsonValue
    private final String descricao;

    TipoColegiado(String descricao) {
        this.descricao = descricao;
    }

    @SuppressWarnings("unused")
    public String getDescricao() {
        return descricao;
    }
}
