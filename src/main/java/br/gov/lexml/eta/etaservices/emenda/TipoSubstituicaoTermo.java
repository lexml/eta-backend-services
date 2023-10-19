package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoSubstituicaoTermo {
	EXPRESSAO("Expressão"),
	PALAVRA("Palavra"),
	NUMERO("Número");
	
	private final String descricao;
	
	TipoSubstituicaoTermo(String descricao) {
		this.descricao = descricao;
	}
	
    public static TipoSubstituicaoTermo parse(String tipo) {
        switch (tipo) {
            case "Expressão":
                return EXPRESSAO;
            case "Palavra":
                return PALAVRA;
            case "Número":
                return NUMERO;
            default:
                return null;
        }
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }	
}
