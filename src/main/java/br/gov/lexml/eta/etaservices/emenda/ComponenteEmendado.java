package br.gov.lexml.eta.etaservices.emenda;

public interface ComponenteEmendado {
    String getUrn();

    boolean isArticulado();

    String getRotuloAnexo();

    String getTituloAnexo();

    DispositivosEmenda getDispositivos();
}
