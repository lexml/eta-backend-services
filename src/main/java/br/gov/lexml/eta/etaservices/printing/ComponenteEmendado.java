package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public record ComponenteEmendado(
        String urn,
        boolean articulado,
        Optional<String> rotuloAnexo,
        Optional<String> tituloAnexo,
        DispositivosEmenda dispositivos
) {
}
