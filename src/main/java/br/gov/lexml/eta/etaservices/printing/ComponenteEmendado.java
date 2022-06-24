package br.gov.lexml.eta.etaservices.printing;

public interface ComponenteEmendado {
    String getUrn();

    boolean isArticulado();

    String getRotuloAnexo();

    String getTituloAnexo();

    DispositivosEmenda getDispositivos();
}
