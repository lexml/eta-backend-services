package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public interface ComandoEmenda {
    String getCabecalhoComum();

    List<? extends ItemComandoEmenda> getComandos();
}
