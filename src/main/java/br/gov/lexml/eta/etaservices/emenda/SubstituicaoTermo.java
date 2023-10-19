package br.gov.lexml.eta.etaservices.emenda;

public interface SubstituicaoTermo {
	
	  TipoSubstituicaoTermo getTipo();
	  String getTermo();
	  String getNovoTermo();
	  boolean isFlexaoGenero();
	  boolean isFlexaoNumero();  

}
