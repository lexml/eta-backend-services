package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface DispositivoEmendaAdicionado extends DispositivoEmendaModificado {
    Boolean isOndeCouber();

    String getIdPai();

    String getIdIrmaoAnterior();
    
    String getIdPosicaoAgrupador();

    Boolean isExisteNaNormaAlterada();

    List<? extends DispositivoEmendaAdicionado> getFilhos();
}
