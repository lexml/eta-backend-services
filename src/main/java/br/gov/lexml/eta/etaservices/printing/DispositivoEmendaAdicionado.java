package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Optional;

public record DispositivoEmendaAdicionado(
        String tipo,
        String id,
        Optional<String> rotulo,
        Optional<String> idPai,
        Optional<String> idIrmaoAnterior,
        Optional<String> urnNormaAlterada,
        Optional<Boolean> existeNaNormaAlterada,
        List<DispositivoEmendaAdicionado> filhos
) implements DispositivoEmenda {
}
