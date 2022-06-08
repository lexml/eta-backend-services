package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Optional;

public record Autoria(
        TipoAutoria tipo,
        boolean imprimirPartidoUF,
        int quantidadeAssinaturasAdicionaisSenadores,
        int quantidadeAssinaturasAdicionaisDeputados,
        List<Parlamentar> parlamentares,
        Optional<ColegiadoAutor> colegiado

) {
}
