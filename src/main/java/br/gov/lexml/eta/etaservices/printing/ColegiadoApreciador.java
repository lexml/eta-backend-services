package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public record ColegiadoApreciador(
        SiglaCasaLegislativa siglaCasaLegislativa,
        TipoColegiado tipoColegiado,
        Optional<String> siglaComissao
) {
}
