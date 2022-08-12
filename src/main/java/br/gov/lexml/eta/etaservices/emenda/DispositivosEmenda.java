package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface DispositivosEmenda {
    List<? extends DispositivoEmendaSuprimido> getDispositivosSuprimidos();

    List<? extends DispositivoEmendaModificado> getDispositivosModificados();

    List<? extends DispositivoEmendaAdicionado> getDispositivosAdicionados();
}
