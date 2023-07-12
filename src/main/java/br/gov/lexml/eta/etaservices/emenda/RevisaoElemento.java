package br.gov.lexml.eta.etaservices.emenda;

public interface RevisaoElemento extends Revisao {
	String getActionType();

	String getStateType();

	Elemento getElementoAntesRevisao();

	Elemento getElementoAposRevisao();

	String getIdRevisaoElementoPai();

	String getIdRevisaoElementoPrincipal();
}
