package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public interface Autoria {
    TipoAutoria getTipo();

    boolean isImprimirPartidoUF();

    int getQuantidadeAssinaturasAdicionaisSenadores();

    int getQuantidadeAssinaturasAdicionaisDeputados();

    List<? extends Parlamentar> getParlamentares();

    ColegiadoAutor getColegiado();
}
