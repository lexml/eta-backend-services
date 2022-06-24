package br.gov.lexml.eta.etaservices.printing;

public interface DispositivoEmendaModificado extends DispositivoEmenda {
    String getTexto();

    Boolean isTextoOmitido();

    Boolean isAbreAspas();

    Boolean isFechaAspas();

    NotaAlteracao getNotaAlteracao();
}
