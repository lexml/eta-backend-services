package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoColegiado {
    COMISSAO("Comissão"),
    PLENARIO("Plenário"),
	PLENARIO_VIA_COMISSAO("Plenário via Comissão");
	
    private final String descricao;

    TipoColegiado(final String descricao) {
        this.descricao = descricao;
    }

    public static TipoColegiado parse(String valueOf) {
    	if (valueOf == null) {
    		return null;
    	}
    	for(TipoColegiado tc: TipoColegiado.values()) {
    		if(tc.descricao.equals(valueOf)) {
    			return tc;
    		}
    	}
    	return null;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }
}
