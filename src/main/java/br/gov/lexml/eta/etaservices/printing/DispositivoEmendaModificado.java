package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public record DispositivoEmendaModificado(
        String tipo,
        String id,
        Optional<String> rotulo,
        Optional<String> texto,
        Optional<Boolean> textoOmitido,
        Optional<Boolean> abreAspas,
        Optional<Boolean> fechaAspas,
        Optional<NotaAlteracao> notaAlteracao
) implements DispositivoEmenda {
}
