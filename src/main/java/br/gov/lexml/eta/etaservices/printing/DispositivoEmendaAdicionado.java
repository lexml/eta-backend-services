package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public record DispositivoEmendaAdicionado(
    String tipo,
    String id,
    String rotulo,
    Boolean ondeCouber,
    String idPai,
    String idIrmaoAnterior,
    String urnNormaAlterada,
    Boolean existeNaNormaAlterada,
    List<DispositivoEmendaAdicionado> filhos) implements DispositivoEmenda {

}
