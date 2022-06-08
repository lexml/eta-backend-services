package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public record DispositivosEmenda(
        List<DispositivoEmendaSuprimido> dispositivosSuprimidos,
        List<DispositivoEmendaModificado> dispositivosModificados,
        List<DispositivoEmendaAdicionado> dispositivosAdicionados
) {
}
