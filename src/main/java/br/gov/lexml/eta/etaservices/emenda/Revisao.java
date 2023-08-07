package br.gov.lexml.eta.etaservices.emenda;

public interface Revisao {
	String getId();

	Usuario getUsuario();

	String getDataHora();

	String getDescricao();
}
