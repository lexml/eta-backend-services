package br.gov.lexml.eta.etaservices.printing;

public record DispositivoEmendaModificado(
    String tipo,
    String id,
    String rotulo,
    String texto,
    Boolean textoOmitido,
    Boolean abreAspas,
    Boolean fechaAspas,
    NotaAlteracao notaAlteracao) implements DispositivoEmenda {

}
