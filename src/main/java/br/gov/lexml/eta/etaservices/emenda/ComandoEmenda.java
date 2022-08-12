package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface ComandoEmenda {
    String getCabecalhoComum();

    List<? extends ItemComandoEmenda> getComandos();
}
