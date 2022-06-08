package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public record ItemComandoEmenda(
        String cabecalho,
        Optional<String> citacao,
        Optional<String> rotulo,
        Optional<String> complemento
) {
}
