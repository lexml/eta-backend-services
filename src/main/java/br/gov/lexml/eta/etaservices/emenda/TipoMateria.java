package br.gov.lexml.eta.etaservices.emenda;

import lombok.Getter;

@Getter
public enum TipoMateria {
	MPV("Medida Provisória"),
	PDN("Projeto de Decreto Legislativo (CN)"),
	PRN("Projeto de Resolução (CN)");
	
	private final String descricao;
	
	TipoMateria(String descricao) {
		this.descricao = descricao;
	}
}
