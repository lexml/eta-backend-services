package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public record Autoria(
    TipoAutoria tipo,
    boolean imprimirPartidoUF,
    int quantidadeAssinaturasAdicionaisSenadores,
    int quantidadeAssinaturasAdicionaisDeputados,
    List<Parlamentar> parlamentares,
    ColegiadoAutor colegiado) {
}
