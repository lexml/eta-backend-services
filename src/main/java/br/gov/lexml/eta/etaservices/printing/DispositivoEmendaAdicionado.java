package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public interface DispositivoEmendaAdicionado extends DispositivoEmendaModificado {
    Boolean isOndeCouber();

    String getIdPai();

    String getIdIrmaoAnterior();

    String getUrnNormaAlterada();

    Boolean isExisteNaNormaAlterada();

    List<? extends DispositivoEmendaAdicionado> filhos();
}
