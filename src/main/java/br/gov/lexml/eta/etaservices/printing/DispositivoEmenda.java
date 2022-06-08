package br.gov.lexml.eta.etaservices.printing;

import java.util.Optional;

public interface DispositivoEmenda {
    String tipo();
    String id();
    Optional<String> rotulo();
}
