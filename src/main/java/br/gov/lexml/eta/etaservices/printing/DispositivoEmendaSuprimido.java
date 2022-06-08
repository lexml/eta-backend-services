package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public record DispositivoEmendaSuprimido(
        String tipo,
        String id,
        Optional<String> rotulo
) implements DispositivoEmenda {
}
