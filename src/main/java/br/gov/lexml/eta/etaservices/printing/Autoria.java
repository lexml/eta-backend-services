package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public interface Autoria {
    TipoAutoria getTipo();

    boolean isImprimirPartidoUF();

    int getQuantidadeAssinaturasAdicionaisSenadores();

    int getQuantidadeAssinaturasAdicionaisDeputados();

    List<? extends Parlamentar> getParlamentares();

    @SuppressWarnings("unused")
    default boolean hasOddNumberOfParlamentares() {
        if (getParlamentares() == null) {
            return false;
        }

        return getParlamentares().size() % 2 == 1;

    }

    ColegiadoAutor getColegiado();
}
