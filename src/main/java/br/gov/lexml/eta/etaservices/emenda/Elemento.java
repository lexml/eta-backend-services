package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface Elemento extends Referencia {
	int getNivel();
	String getNumero();
	String getRotulo();
	boolean isAgrupador();
	boolean isEditavel();
	List<? extends Mensagem> getMensagens();
	Hierarquia getHierarquia();
	boolean isSendoEditado();
	int getIndex();
	String getNorma();
	boolean isAbreAspas();
	boolean isFechaAspas();
	String getNotaAlteracao();
	boolean isDispositivoAlteracao();
	String getTipoOmissis();
	boolean isPodeEditarNotaAlteracao();
	boolean isManterNoMesmoGrupoDeAspas();
	String getArtigoDefinido();
	Referencia getElementoAnteriorNaSequenciaDeLeitura();
	Revisao getRevisao();
}
