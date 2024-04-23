package br.gov.lexml.eta.etaservices.emenda;

public enum TipoEmenda {
	MPV("Medida Provisória"),
	PDN("PDN"),
	PRN("PRN");
	
	private final String descricao;
	
	TipoEmenda(String descricao) {
		this.descricao = descricao;
	}

    public String getDescricao() {
        return descricao;
    }	
}
