package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Optional;

public record ComandoEmenda(
        Optional<String> cabecalhoComum,
        List<ItemComandoEmenda> comandos
) {
}
