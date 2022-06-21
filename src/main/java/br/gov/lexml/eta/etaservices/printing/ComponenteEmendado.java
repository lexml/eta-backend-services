package br.gov.lexml.eta.etaservices.printing;

public record ComponenteEmendado(
    String urn,
    boolean articulado,
    String rotuloAnexo,
    String tituloAnexo,
    DispositivosEmenda dispositivos) {

}
